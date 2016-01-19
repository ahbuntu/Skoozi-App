package com.megaphone.skoozi;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

abstract public class BaseHolder<T, VH extends BaseHolder.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
    abstract public VH create(ViewGroup parent, int viewType);
    abstract public void bind(final VH holder, T item);
}
