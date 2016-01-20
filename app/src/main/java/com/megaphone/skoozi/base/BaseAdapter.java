package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

abstract public class BaseAdapter<T, VH extends BaseVhConductor.BaseViewHolder>
        extends RecyclerView.Adapter<BaseVhConductor.BaseViewHolder>{

    protected BaseVhConductor<T, VH> vhConductor;

    @Override
    public BaseVhConductor.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhConductor.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhConductor.BaseViewHolder viewHolder, int position) {
        vhConductor.bind((VH) viewHolder, getItem(position));
    }

    abstract protected T getItem(int position);
}
