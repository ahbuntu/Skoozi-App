package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

abstract public class BaseHolder<T, VH extends BaseHolder.BaseViewHolder> {

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }
    abstract public BaseViewHolder create(ViewGroup parent, int viewType);
    abstract public void bind(final VH holder, T item);
}
