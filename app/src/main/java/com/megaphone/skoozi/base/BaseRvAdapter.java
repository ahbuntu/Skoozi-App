package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

abstract public class BaseRvAdapter<T, VH extends BaseVhMaker.BaseViewHolder>
        extends RecyclerView.Adapter<BaseVhMaker.BaseViewHolder>{

    protected BaseVhMaker<T, VH> vhMaker;

    @Override
    public BaseVhMaker.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhMaker.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhMaker.BaseViewHolder viewHolder, int position) {
        vhMaker.bind((VH) viewHolder, getItem(position));
    }

    abstract protected T getItem(int position);
}
