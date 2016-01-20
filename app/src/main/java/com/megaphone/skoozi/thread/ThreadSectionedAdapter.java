package com.megaphone.skoozi.thread;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.megaphone.skoozi.R;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.PresentationUtil;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by ahmadulhassan on 2015-06-28.
 * Big shout out to:
 * https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 */
public class ThreadSectionedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SECTION_TYPE = 0;
    private final Context mContext;


    private LayoutInflater mLayoutInflater;
    private int mSectionResourceId;
    private int mQuesTitleResId;
    private int mQuesTimestampResId;
    private RecyclerView.Adapter mBaseAdapter;
    private boolean mValid = true;
    private SparseArray<Section> mSections = new SparseArray<Section>();

    public ThreadSectionedAdapter(Context context, int sectionResourceId, int questionTitleResId,
                                  int questionTimestampResId, RecyclerView.Adapter baseAdapter) {

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSectionResourceId = sectionResourceId;
        mQuesTitleResId = questionTitleResId;
        mQuesTimestampResId = questionTimestampResId;
        mBaseAdapter = baseAdapter;
        mContext = context;

        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }


    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView timestamp;

        public SectionViewHolder(View view, int questionTitleResId,
                                 int questionTimestampResId) {
            super(view);
            title = (TextView) view.findViewById(questionTitleResId);
            timestamp = (TextView) view.findViewById(questionTimestampResId);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
            return new SectionViewHolder(view, mQuesTitleResId, mQuesTimestampResId);
        }else{
            return mBaseAdapter.onCreateViewHolder(parent, typeView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            ((SectionViewHolder)sectionViewHolder).title.setText(mSections.get(position).title);
            ((SectionViewHolder)sectionViewHolder).timestamp.setText(mContext.getString(R.string.thread_question_timestamp,
                    PresentationUtil.unixTimestampAsDateTime(mSections.get(position).timestamp)));
        }else{
            mBaseAdapter.onBindViewHolder(sectionViewHolder,sectionedPositionToPosition(position));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position));
    }


    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;
        Long timestamp;

        public Section(int firstPosition, Question question) {
            this.firstPosition = firstPosition;
            this.title = question.content;
            this.timestamp = question.timestamp;
        }
    }


    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }


    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }


}
