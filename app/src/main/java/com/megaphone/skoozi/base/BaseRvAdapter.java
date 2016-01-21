package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

abstract public class BaseRvAdapter<T, VH extends BaseVhMaker.BaseViewHolder>
        extends RecyclerView.Adapter<BaseVhMaker.BaseViewHolder>{

    protected BaseVhMaker<T, VH> vhConductor;

    @Override
    public BaseVhMaker.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhConductor.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhMaker.BaseViewHolder viewHolder, int position) {
        vhConductor.bind((VH) viewHolder, getItem(position));
    }

    abstract protected T getItem(int position);
}
