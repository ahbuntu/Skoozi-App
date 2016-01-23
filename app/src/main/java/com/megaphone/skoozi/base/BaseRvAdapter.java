package com.megaphone.skoozi.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

abstract public class BaseRvAdapter<T, VH extends BaseVhSupplier.BaseViewHolder>
        extends RecyclerView.Adapter<BaseVhSupplier.BaseViewHolder>{

    protected BaseVhSupplier<T, VH> vhSupplier;

    @Override
    public BaseVhSupplier.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhSupplier.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhSupplier.BaseViewHolder viewHolder, int position) {
        vhSupplier.bind((VH) viewHolder, getItem(position));
    }

    abstract protected T getItem(int position);
}
