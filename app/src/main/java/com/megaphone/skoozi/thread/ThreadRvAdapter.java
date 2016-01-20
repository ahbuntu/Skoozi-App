package com.megaphone.skoozi.thread;


import com.megaphone.skoozi.base.BaseAdapter;
import com.megaphone.skoozi.model.Answer;

import java.util.ArrayList;
import java.util.List;

public class ThreadRvAdapter extends BaseAdapter<Answer, ThreadVhConductor.AnswerViewHolder> {

    private static final int ROW_ANSWER_TYPE = 2000;

    private List<Answer> threadAnswers;
    /**
     *
     * @param answers NULL value is acceptable. will display card accordingly
     */
    public ThreadRvAdapter(List<Answer> answers) {
        threadAnswers = (answers == null) ? (new ArrayList<Answer>() ) : answers;
        vhConductor = new ThreadVhConductor<>();
    }

    @Override
    protected Answer getItem(int position) {
        return threadAnswers.get(position);
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
