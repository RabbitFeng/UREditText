package com.example.uredittext;

import androidx.annotation.NonNull;

public class Step implements Cloneable{
    private CharSequence beforeCharSequence;
    private CharSequence afterCharSequence;
    private int start;
    /**
     * 栈中状态标记符
     * 若redo栈中元素全部加入undo栈时，converse取false
     */
    private boolean converse = true;

    public Step() {
    }

    public Step(CharSequence beforeCharSequence, CharSequence afterCharSequence, int start) {
        this.beforeCharSequence = beforeCharSequence;
        this.afterCharSequence = afterCharSequence;
        this.start = start;
    }

    public void updateConverse() {
        this.converse = !converse;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public CharSequence getBeforeCharSequence() {
        return beforeCharSequence;
    }

    public void setBeforeCharSequence(CharSequence beforeCharSequence) {
        this.beforeCharSequence = beforeCharSequence;
    }

    public CharSequence getAfterCharSequence() {
        return afterCharSequence;
    }

    public void setAfterCharSequence(CharSequence afterCharSequence) {
        this.afterCharSequence = afterCharSequence;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @NonNull
    @Override
    public String toString() {
        return (isConverse() ? "+" : "-") + "[" + getStart() + "][" + getBeforeCharSequence() + "," + getAfterCharSequence() + "]";
    }

    public boolean isConverse() {
        return converse;
    }
}
