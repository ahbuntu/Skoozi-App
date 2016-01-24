package com.megaphone.skoozi.thread;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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
        TextView sectionTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            sectionTitle = (TextView) itemView.findViewById(R.id.section_left);
        }
    }

    @Override
    public BaseViewHolder create(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thread_section, parent, false));
    }

    @Override
    public void bind(final ViewHolder holder, ThreadSection item) {
        final Context context = holder.itemView.getContext();
        if (TextUtils.isEmpty(item.title)) {
            if (item.question == null) return;
            holder.sectionTitle.setTextColor(ContextCompat.getColor(context, R.color.primary_dark));
            holder.sectionTitle.setText(holder.itemView.getContext().getString(R.string.thread_question_timestamp,
                    PresentationUtil.unixTimestampAsDateTime(item.question.timestamp)));
        } else {
            holder.sectionTitle.setTextColor(ContextCompat.getColor(context, R.color.accent));
            String title = item.title;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                holder.sectionTitle.setFontFeatureSettings("smcp");
                holder.sectionTitle.setText(title.toUpperCase());
            }
            else
                holder.sectionTitle.setText(title.toUpperCase());
        }
    }
}
