package com.megaphone.skoozi.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.megaphone.skoozi.R;
import com.megaphone.skoozi.base.BaseVhMaker;

public class ThreadSectionVhMaker<T extends ThreadSection>
        extends BaseVhMaker<T, ThreadSectionVhMaker.SectionViewHolder> {

    public static class SectionViewHolder extends BaseVhMaker.BaseViewHolder {
        TextView title;
        TextView timestamp;

        public SectionViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.section_question_content);
            timestamp = (TextView) itemView.findViewById(R.id.section_question_timestamp);
        }
    }

    @Override
    public BaseViewHolder create(ViewGroup parent, int viewType) {
        return new SectionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_thread, parent));
    }

    @Override
    public void bind(final SectionViewHolder holder, T item) {
        holder.title.setText(item.title);
//            vhConductor.timestamp.setText(mContext.getString(R.string.thread_question_timestamp,
//                    PresentationUtil.unixTimestampAsDateTime(mSections.get(position).timestamp)));
    }
}
