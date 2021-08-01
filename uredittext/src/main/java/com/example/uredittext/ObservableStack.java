package com.example.uredittext;

import java.util.Stack;

public class ObservableStack<E> extends Stack<E> {

    private OnStackChangedListener onStackChangedListener;

    public ObservableStack(OnStackChangedListener onStackChangedListener) {
        this.onStackChangedListener = onStackChangedListener;
    }

    @Override
    public E push(E item) {
        if(onStackChangedListener!=null){
            onStackChangedListener.onChange();
        }
        return super.push(item);
    }

    @Override
    public synchronized E pop() {
        if(onStackChangedListener!=null){
            onStackChangedListener.onChange();
        }
        return super.pop();
    }

    /**
     * Stack执行push或pop时的回调接口
     */
    public interface OnStackChangedListener {
        void onChange();
    }
}
