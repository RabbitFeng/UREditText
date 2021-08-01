package com.example.uredittext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.Stack;

/**
 * Author: Rabbit
 * Time: 2020/12/22
 * 扩展EditText功能:撤销与反撤销
 */
public class UREditText extends AppCompatEditText implements ObservableStack.OnStackChangedListener {
    private static final String TAG = "UREditText";
    /**
     * redo栈。自定义子类，实现对pop()和push()方法的监听
     */
    private final Stack<Step> redoStack = new ObservableStack<Step>(this);

    /**
     * undo栈。自定义子类，实现对pop()和push()方法的监听
     */
    private final Stack<Step> undoStack = new ObservableStack<Step>(this);

    /**
     * redo()和undo()对EditText内容修改会触发监听器，置flag标志位
     */
    private boolean flag;

    /**
     * 回调接口
     */
    @Nullable
    private OnUpdateEnableListener onUpdateEnableListener;

    public UREditText(Context context) {
        super(context);
    }

    public UREditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UREditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 是否可执行撤销
     *
     * @return canUndo
     */
    public boolean canUndo() {
        return !undoStack.empty();
    }

    /**
     * 是否可执行重做
     *
     * @return canRedo
     */
    public boolean canRedo() {
        return !redoStack.empty();
    }

    /**
     * 清空栈
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * 获取canUndo和canRedo
     */
    public void update() {
        if (onUpdateEnableListener != null) {
            onUpdateEnableListener.onUpdateEnable(canUndo(), canRedo());
        }
    }

    /**
     * 撤销
     */
    public void undo() {
        flag = true;
        if (canUndo()) {
            Step pop = undoStack.pop();
            redoStack.push(pop);
            Log.d(TAG, "undo:pop:" + pop.isConverse() + ";" +
                    "start:" + pop.getStart() + "; " +
                    "before:" + pop.getBeforeCharSequence() + "; " +
                    "after:" + pop.getAfterCharSequence());
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

    /**
     * 重做
     */
    public void redo() {
        flag = true;
        if (canRedo()) {
            Step pop = redoStack.pop();
            undoStack.push(pop);
            Log.d(TAG, "redo:pop:" + pop.isConverse() + ";" +
                    "start:" + pop.getStart() + "; " +
                    "before:" + pop.getBeforeCharSequence() + "; " +
                    "after:" + pop.getAfterCharSequence());
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

    public void setOnUpdateEnableListener(@Nullable OnUpdateEnableListener onUpdateEnableListener) {
        this.onUpdateEnableListener = onUpdateEnableListener;
    }

    @Override
    public void onChange() {
        update();
    }

    /**
     * 接口，用于获取canUndo和canRedo状态
     */
    public interface OnUpdateEnableListener {
        void onUpdateEnable(boolean canUndo, boolean canRedo);
    }

    private class CustomTextWatcher implements TextWatcher {
        private final Step step = new Step();

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
}
