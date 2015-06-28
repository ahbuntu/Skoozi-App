package com.megaphone.skoozi;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ahmadulhassan on 2015-06-27.
 */
public class ThreadRecyclerViewAdapter extends RecyclerView.Adapter<ThreadRecyclerViewAdapter.AnswerViewHolder> {

    private Context mContext;
    private List<Answer> threadAnswers;

    // Provide a reference to the views for each data item
    // you provide access to all the views for a data item in a view holder
    public static class AnswerViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView mCardView;
        TextView threadTimestamp;
        TextView threadContent;
        TextView threadUserName;
        public AnswerViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.thread_answer_card);
            threadTimestamp = (TextView) itemView.findViewById(R.id.thread_answer_timestamp);
            threadUserName = (TextView) itemView.findViewById(R.id.thread_answer_profile_name);
            threadContent = (TextView) itemView.findViewById(R.id.thread_answer_content);

        }
    }

    public ThreadRecyclerViewAdapter(Context context, List<Answer> answers) {
        mContext = context;
        threadAnswers = answers;
    }

    //region AnswerViewHolder Lifecycle callbacks

    // Create new views (invoked by the layout manager)
    @Override
    public AnswerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_thread_answer, parent, false);
        AnswerViewHolder vh = new AnswerViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final AnswerViewHolder holder, int position) {
//        final int viewType = holder.getItemViewType();
        Answer answerItem = threadAnswers.get(position);
        holder.threadTimestamp.setText( answerItem.getTimestamp());
        holder.threadUserName.setText(answerItem.getAuthor());
        holder.threadContent.setText(answerItem.getContent());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return threadAnswers.size();
    }

    //endregion
}
