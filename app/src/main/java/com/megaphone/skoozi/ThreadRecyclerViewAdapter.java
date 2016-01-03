package com.megaphone.skoozi;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.util.PresentationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmadulhassan on 2015-06-27.
 */
public class ThreadRecyclerViewAdapter extends RecyclerView.Adapter<ThreadRecyclerViewAdapter.AnswerViewHolder> {

    private static final int ROW_EMPTY_TYPE = 1000;
    private static final int ROW_ANSWER_TYPE = 2000;

    private Context mContext;
    private List<Answer> threadAnswers;

    // Provide a reference to the views for each data item
    public static class AnswerViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView threadTimestamp;
        TextView threadContent;
        TextView threadUserName;
        ImageView threadNameImage;
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
                threadNameImage = (ImageView) itemView.findViewById(R.id.thread_list_name_image);
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
        threadAnswers = (answers == null) ? (new ArrayList<Answer>() ) : answers;
    }

    //region AnswerViewHolder Lifecycle callbacks

    // Create new views (invoked by the layout manager)
    @Override
    public AnswerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case ROW_ANSWER_TYPE:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_thread_list, parent, false);
                return new AnswerViewHolder(v, true);

            case ROW_EMPTY_TYPE:
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_thread_empty, parent, false);
                return new AnswerViewHolder(v, false);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final AnswerViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        switch (viewType) {
            case ROW_ANSWER_TYPE:
                Answer answerItem = threadAnswers.get(position);
                holder.threadTimestamp.setText(PresentationUtil.unixTimestampAge(answerItem.timestamp));
                holder.threadUserName.setText(answerItem.author);
                holder.threadContent.setText(answerItem.content);

                //todo: decision to display user image or letter should be made here
                ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                // generate random color
                int nameImageColor = generator.getRandomColor();
                TextDrawable nameDrawable = TextDrawable.builder()
                        .buildRound(answerItem.author.substring(0, 1).toUpperCase(), nameImageColor);
                holder.threadNameImage.setImageDrawable(nameDrawable);
                holder.threadNameImage.setVisibility(View.VISIBLE);
            case ROW_EMPTY_TYPE:
            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return threadAnswers == null ? 1 : threadAnswers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return threadAnswers == null ? ROW_EMPTY_TYPE : ROW_ANSWER_TYPE;
    }

    public void add(int position, Answer item) {
        threadAnswers.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Answer item) {
        int position = threadAnswers.indexOf(item);
        threadAnswers.remove(position);
        notifyItemRemoved(position);
    }
    //endregion

}
