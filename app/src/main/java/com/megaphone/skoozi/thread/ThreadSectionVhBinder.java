package com.megaphone.skoozi.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.megaphone.skoozi.R;
import com.megaphone.skoozi.base.BaseVhBinder;
import com.megaphone.skoozi.util.PresentationUtil;

public class ThreadSectionVhBinder extends
        BaseVhBinder<ThreadSection, ThreadSectionVhBinder.ViewHolder> {

    public static class ViewHolder extends BaseVhBinder.BaseViewHolder {
        TextView timestamp;

        public ViewHolder(View itemView) {
            super(itemView);
            timestamp = (TextView) itemView.findViewById(R.id.section_question_timestamp);
        }
    }

    @Override
    public BaseViewHolder create(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thread_section, parent, false));
    }

    @Override
    public void bind(final ViewHolder holder, ThreadSection item) {
        if (item.question == null) return;

        holder.timestamp.setText(holder.itemView.getContext().getString(R.string.thread_question_timestamp,
                PresentationUtil.unixTimestampAsDateTime(item.question.timestamp)));
    }
}
