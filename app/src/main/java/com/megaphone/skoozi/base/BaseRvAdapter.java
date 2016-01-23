package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

abstract public class BaseRvAdapter<T, VH extends BaseVhBinder.BaseViewHolder>
        extends RecyclerView.Adapter<BaseVhBinder.BaseViewHolder>{

    protected BaseVhBinder<T, VH> vhBinder;

    @Override
    public BaseVhBinder.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhBinder.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhBinder.BaseViewHolder viewHolder, int position) {
        vhBinder.bind((VH) viewHolder, getItem(position));
    }

    abstract protected T getItem(int position);
}
