package com.megaphone.skoozi.thread;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.megaphone.skoozi.base.BaseRvAdapter;
import com.megaphone.skoozi.base.BaseVhMaker;
import com.megaphone.skoozi.base.BaseSection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Big shout out to:
 * https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 */
public class ThreadSectionedAdapter<K extends ThreadRvAdapter>
        extends BaseRvAdapter<ThreadSection, ThreadSectionVhMaker.ViewHolder> {

    private static final int SECTION_TYPE = 0;

    private K rvAdapter;
    private boolean rvAdapterHasItems = true;
    private SparseArray<ThreadSection> sections = new SparseArray<>();

    public ThreadSectionedAdapter(K baseAdapter) {
        vhMaker = new ThreadSectionVhMaker();
        rvAdapter = baseAdapter;
        rvAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                rvAdapterHasItems = rvAdapter.getItemCount() > 0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                rvAdapterHasItems = rvAdapter.getItemCount() > 0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                rvAdapterHasItems = rvAdapter.getItemCount() > 0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                rvAdapterHasItems = rvAdapter.getItemCount() > 0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    @Override
    public BaseVhMaker.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SECTION_TYPE) {
            return vhMaker.create(parent, viewType);
        }else{
            return rvAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(BaseVhMaker.BaseViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            vhMaker.bind((ThreadSectionVhMaker.ViewHolder) sectionViewHolder, getItem(position));
        }else{
            rvAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
        }
    }

    @Override
    protected ThreadSection getItem(int position) {
        return sections.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : rvAdapter.getItemViewType(sectionedPositionToPosition(position));
    }

    public void setSections(List<ThreadSection> threadSections) {
        sections.clear();

        Collections.sort(threadSections, new Comparator<BaseSection>() {
            @Override
            public int compare(BaseSection o, BaseSection o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (ThreadSection section : threadSections) {
            section.sectionedPosition = section.firstPosition + offset;
            sections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt(i).firstPosition > position) {
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
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return sections.get(position) != null;
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - sections.indexOfKey(position)
                : rvAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (rvAdapterHasItems ? rvAdapter.getItemCount() + sections.size() : 0);
    }


}
