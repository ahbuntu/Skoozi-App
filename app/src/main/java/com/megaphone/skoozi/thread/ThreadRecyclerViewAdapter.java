package com.megaphone.skoozi.thread;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.megaphone.skoozi.BaseHolder;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.thread.ThreadHolder;

import java.util.ArrayList;
import java.util.List;

public class ThreadRecyclerViewAdapter extends RecyclerView.Adapter<BaseHolder.ViewHolder> {

    private static final int ROW_ANSWER_TYPE = 2000;

    private List<Answer> threadAnswers;
    private ThreadHolder<Answer> holder;

    /**
     *
     * @param answers NULL value is acceptable. will display card accordingly
     */
    public ThreadRecyclerViewAdapter(List<Answer> answers) {
        threadAnswers = (answers == null) ? (new ArrayList<Answer>() ) : answers;
        holder = new ThreadHolder<>();
    }

    @Override
    public BaseHolder.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return holder.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseHolder.ViewHolder holder, int position) {
        Answer answerItem = threadAnswers.get(position);
        this.holder.bind((ThreadHolder.AnswerViewHolder) holder, answerItem);
    }

    @Override
    public int getItemCount() {
        return threadAnswers == null ? 1 : threadAnswers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ROW_ANSWER_TYPE;
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
}
