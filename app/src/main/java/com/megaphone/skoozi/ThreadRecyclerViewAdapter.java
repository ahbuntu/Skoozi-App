package com.megaphone.skoozi;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.megaphone.skoozi.util.PresentationUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ahmadulhassan on 2015-06-27.
 */
public class ThreadRecyclerViewAdapter extends RecyclerView.Adapter<ThreadRecyclerViewAdapter.AnswerViewHolder> {

    private static final int CARD_EMPTY_TYPE = 1000;
    private static final int CARD_ANSWER_TYPE = 2000;

    private Context mContext;
    private List<Answer> threadAnswers;

    // Provide a reference to the views for each data item
    public static class AnswerViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView threadTimestamp;
        TextView threadContent;
        TextView threadUserName;

        /**
         * initializes all the views for a data item in a view holder
         * @param itemView
         * @param populateAnswer - pass TRUE only if there are answers for the thread
         */
        public AnswerViewHolder(View itemView, boolean populateAnswer) {
            super(itemView);
            if (populateAnswer) {
                threadTimestamp = (TextView) itemView.findViewById(R.id.thread_answer_timestamp);
                threadUserName = (TextView) itemView.findViewById(R.id.thread_answer_profile_name);
                threadContent = (TextView) itemView.findViewById(R.id.thread_answer_content);
            }
        }
    }

    /**
     *
     * @param context
     * @param answers NULL value is acceptable. will display card accordingly
     */
    public ThreadRecyclerViewAdapter(Context context, List<Answer> answers) {
        mContext = context;
        threadAnswers = answers;
    }

    //region AnswerViewHolder Lifecycle callbacks

    // Create new views (invoked by the layout manager)
    @Override
    public AnswerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case CARD_ANSWER_TYPE:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_thread_list, parent, false);
                return new AnswerViewHolder(v, true);

            case CARD_EMPTY_TYPE:
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_thread_empty, parent, false);
                return new AnswerViewHolder(v, false);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final AnswerViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        switch (viewType) {
            case CARD_ANSWER_TYPE:
                Answer answerItem = threadAnswers.get(position);
                holder.threadTimestamp.setText(PresentationUtil.unixTimestampAge(answerItem.getTimestamp()));
                holder.threadUserName.setText(answerItem.getAuthor());
                holder.threadContent.setText(answerItem.getContent());

            case CARD_EMPTY_TYPE:
            default:
                break;
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return threadAnswers == null ? 1 : threadAnswers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return threadAnswers == null ? CARD_EMPTY_TYPE : CARD_ANSWER_TYPE;
    }
    //endregion

}
