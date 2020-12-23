package com.example.uredittext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.Stack;

/**
 * Author: Dong
 * Time: 2020/12/22
 * 自定义EditText，拓展:撤销与反撤销
 * - 键盘输入的单字符
 */
@SuppressWarnings("unused")
public class UREditText extends AppCompatEditText {
//    private static final String TAG = "CustomEditText";

//    @SuppressWarnings("WeakerAccess")
//    @Retention(RetentionPolicy.CLASS)
//    @IntDef({KEY_VIEW_REDO, KEY_VIEW_UNDO})
//    public @interface ViewKey {
//    }
//
//    /**
//     * 作为相应redo()和undo()操作的绑定组件，CustomEditText持有该View的引用
//     * 也可以在Activity(或Fragment)中调用CustomEditText的undo()和redo()方法，做保留
//     */
//    public static final int KEY_VIEW_UNDO = 0x11;
//    public static final int KEY_VIEW_REDO = 0x12;
//    private SparseArray<View> viewSparseArray = new SparseArray<>();

    /**
     * redo栈。自定义子类，实现对pop()和push()方法的监听
     */
    private final Stack<Step> redoStack = new ObservableStack<>();

    /**
     * undo栈。自定义子类，实现对pop()和push()方法的监听
     */
    private final Stack<Step> undoStack = new ObservableStack<>();

    /**
     * redo()和undo()对EditText内容修改会触发监听器
     */
    private boolean flag = false;

//    /**
//     * undo(),redo()绑定View
//     */
//    private final OnClickListener onClickListener = v -> {
//        @ViewKey
//        int key = viewSparseArray.keyAt(viewSparseArray.indexOfValue(v));
//        switch (key) {
//            case KEY_VIEW_REDO:
//                redo();
//                break;
//            case KEY_VIEW_UNDO:
//                undo();
//                break;
//        }
//    };

    /**
     *
     */
    private OnUpdateEnableListener onUpdateEnableListener;

    public interface OnUpdateEnableListener {
        void onUpdateEnable(boolean canUndo, boolean canRedo);
    }

    public UREditText(Context context) {
        this(context, null);
    }

    public UREditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextChangedListener(new CustomTextWatcher());
    }

    public void undo() {
        flag = true;
        if (canUndo()) {
            Step pop = undoStack.pop();
            redoStack.push(pop);
//            Log.d(TAG, "undoStack: " + undoStack);
//            Log.d(TAG, "redoStack: " + redoStack);
//            Log.d(TAG, "undo:pop:" + pop.isConverse() + ";" +
//                    "start:" + pop.getStart() + "; " +
//                    "before:" + pop.getBeforeCharSequence() + "; " +
//                    "after:" + pop.getAfterCharSequence());
            if (pop.isConverse()) {
                getEditableText().replace(pop.getStart(),
                        pop.getStart() + pop.getAfterCharSequence().length(),
                        pop.getBeforeCharSequence());
            } else {
                getEditableText().replace(pop.getStart(),
                        pop.getStart() + pop.getBeforeCharSequence().length(),
                        pop.getAfterCharSequence());
            }
        }
        flag = false;
    }

    public void redo() {
        flag = true;
        if (canRedo()) {
            Step pop = redoStack.pop();
            undoStack.push(pop);
//            Log.d(TAG, "redo:pop:" + pop.isConverse() + ";" +
//                    "start:" + pop.getStart() + "; " +
//                    "before:" + pop.getBeforeCharSequence() + "; " +
//                    "after:" + pop.getAfterCharSequence());
            if (pop.isConverse()) {
                getEditableText().replace(pop.getStart(),
                        pop.getStart() + pop.getBeforeCharSequence().length(),
                        pop.getAfterCharSequence());
            } else {
                getEditableText().replace(pop.getStart(),
                        pop.getStart() + pop.getAfterCharSequence().length(),
                        pop.getBeforeCharSequence());
            }
        }
        flag = false;
    }

    public boolean canUndo() {
        return !undoStack.empty();
    }

    public boolean canRedo() {
        return !redoStack.empty();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public void update() {
        if (onUpdateEnableListener != null) {
            onUpdateEnableListener.onUpdateEnable(canUndo(), canRedo());
        }
    }

//    @SuppressWarnings("unused")
//    public void bindView(@ViewKey int key, @NonNull View view) {
//        viewSparseArray.put(key, view);
//        viewSparseArray.get(key).setOnClickListener(onClickListener);
//    }

    public void setOnUpdateEnableListener(OnUpdateEnableListener onUpdateEnableListener) {
        this.onUpdateEnableListener = onUpdateEnableListener;
    }

    private final class CustomTextWatcher implements TextWatcher {
        private Step step = new Step();

        /**
         * @param s     charSequence of Editable
         * @param start the start position of CharSequence in changing
         * @param count the length of beforeCharSequence
         * @param after the length of afterCharSequence
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (flag) return;
            step.setBeforeCharSequence(s.subSequence(start, start + count));
            step.setStart(start);
        }

        /**
         * @param s      charSequence of Editable
         * @param start  the start position of CharSequence in changing
         * @param before the length of afterCharSequence
         * @param count  the length of beforeCharSequence
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (flag) return;
            step.setAfterCharSequence(s.subSequence(start, start + count));
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (flag) return;
            if (!redoStack.empty()) {
                for (int i = redoStack.size() - 1; i >= 0; i--) {
                    undoStack.push(redoStack.get(i));
                }
                for (int i = 0; i < redoStack.size(); i++) {
                    try {
                        Step step = (Step) redoStack.get(i).clone();
                        step.updateConverse();
                        undoStack.push(step);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
                redoStack.clear();
            }
            try {
                undoStack.push((Step) step.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressWarnings("unused")
    private final class Step implements Cloneable {
        private CharSequence beforeCharSequence;
        private CharSequence afterCharSequence;
        private int start;
        /**
         * 栈中状态标记符
         * 若redo栈中元素全部加入undo栈时，converse取反值
         */
        private boolean converse = true;

        @SuppressWarnings("WeakerAccess")
        public Step() {
        }

        public Step(CharSequence beforeCharSequence, CharSequence afterCharSequence, int start) {
            this.beforeCharSequence = beforeCharSequence;
            this.afterCharSequence = afterCharSequence;
            this.start = start;
        }

        @SuppressWarnings("WeakerAccess")
        public void updateConverse() {
            this.converse = !converse;
        }

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @SuppressWarnings("WeakerAccess")
        public CharSequence getBeforeCharSequence() {
            return beforeCharSequence;
        }

        @SuppressWarnings("WeakerAccess")
        public void setBeforeCharSequence(CharSequence beforeCharSequence) {
            this.beforeCharSequence = beforeCharSequence;
        }

        @SuppressWarnings("WeakerAccess")
        public CharSequence getAfterCharSequence() {
            return afterCharSequence;
        }

        @SuppressWarnings("WeakerAccess")
        public void setAfterCharSequence(CharSequence afterCharSequence) {
            this.afterCharSequence = afterCharSequence;
        }

        @SuppressWarnings("WeakerAccess")
        public int getStart() {
            return start;
        }

        @SuppressWarnings("WeakerAccess")
        public void setStart(int start) {
            this.start = start;
        }

        @NonNull
        @Override
        public String toString() {
//            return "beforeCharSequence:" + beforeCharSequence + "\n" +
//                    "afterCharSequence:" + afterCharSequence + "\n" +
//                    "stat:" + start + "\n" +
//                    "type:" + type;
            return (isConverse() ? "+" : "-") + "[" + getStart() + "][" + getBeforeCharSequence() + "," + getAfterCharSequence() + "]";
        }

        @SuppressWarnings("WeakerAccess")
        public boolean isConverse() {
            return converse;
        }

    }

    /**
     * 监听栈的push()和pop()方法
     *
     * @param <E>
     */
    private final class ObservableStack<E> extends Stack<E> {
        @Override
        public E push(E item) {
            update();
            return super.push(item);
        }

        @Override
        public synchronized E pop() {
            update();
            return super.pop();
        }
    }
}