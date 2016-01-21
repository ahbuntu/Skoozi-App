package com.megaphone.skoozi.thread;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.megaphone.skoozi.base.BaseAdapter;
import com.megaphone.skoozi.base.BaseVhMaker;
import com.megaphone.skoozi.base.BaseSection;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by ahmadulhassan on 2015-06-28.
 * Big shout out to:
 * https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 */
public class ThreadSectionAdapter extends BaseAdapter<ThreadSection, ThreadSectionVhMaker.SectionViewHolder> {

    private static final int SECTION_TYPE = 0;

    private ThreadRvAdapter threadRvAdapter;
    private boolean mValid = true;
    private SparseArray<ThreadSection> mSections = new SparseArray<>();

    public ThreadSectionAdapter(ThreadRvAdapter baseAdapter) {
        threadRvAdapter = baseAdapter;
        threadRvAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = threadRvAdapter.getItemCount() > 0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = threadRvAdapter.getItemCount() > 0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = threadRvAdapter.getItemCount() > 0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = threadRvAdapter.getItemCount() > 0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }


    @Override
    public BaseVhMaker.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SECTION_TYPE) {
            return vhConductor.create(parent, viewType);
        }else{
            return threadRvAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(BaseVhMaker.BaseViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            vhConductor.bind((ThreadSectionVhMaker.SectionViewHolder) sectionViewHolder, getItem(position));
        }else{
            threadRvAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
        }
    }

    @Override
    protected ThreadSection getItem(int position) {
        return mSections.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : threadRvAdapter.getItemViewType(sectionedPositionToPosition(position));
    }

    public void setSections(BaseSection[] baseSections) {
        ThreadSection[] sections = (ThreadSection[]) baseSections;
        mSections.clear();

        Arrays.sort(sections, new Comparator<BaseSection>() {
            @Override
            public int compare(BaseSection o, BaseSection o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (ThreadSection section : sections) {
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
                : threadRvAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? threadRvAdapter.getItemCount() + mSections.size() : 0);
    }


}
