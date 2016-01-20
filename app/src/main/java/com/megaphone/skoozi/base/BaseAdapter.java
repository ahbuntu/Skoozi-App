package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

abstract public class BaseAdapter<T, VH extends BaseHolder.BaseViewHolder>
        extends RecyclerView.Adapter<BaseHolder.BaseViewHolder>{

    protected BaseHolder<T, VH> holder;

    @Override
    public BaseHolder.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return holder.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseHolder.BaseViewHolder viewHolder, int position) {
        holder.bind((VH) viewHolder, getItem(position));
    }

    abstract protected T getItem(int position);
}
