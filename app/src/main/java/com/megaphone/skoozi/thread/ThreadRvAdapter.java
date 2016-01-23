package com.megaphone.skoozi.thread;


import android.view.ViewGroup;

import com.megaphone.skoozi.base.BaseRvAdapter;
import com.megaphone.skoozi.base.BaseVhSupplier;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;

import java.util.ArrayList;
import java.util.List;

public class ThreadRvAdapter extends BaseRvAdapter<ThreadVhSupplier.TypeContract, ThreadVhSupplier.ViewHolder> {

    private static final int ROW_ANSWER_TYPE = 2000;

    private List<Answer> threadAnswers;
    private List<Question> threadQuestions;

    public ThreadRvAdapter() {
        vhSupplier = new ThreadVhSupplier<>();
    }

    @Override
    public BaseVhSupplier.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhSupplier.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhSupplier.BaseViewHolder sectionViewHolder, int position) {
        vhSupplier.bind((ThreadVhSupplier.ViewHolder) sectionViewHolder, getItem(position));
    }

    @Override
    protected ThreadVhSupplier.TypeContract getItem(int position) {
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

    public void add(int position, ThreadVhSupplier.TypeContract item) {
        if (item instanceof Answer) {
            threadAnswers.add(position, (Answer) item);
        } else if (item instanceof Question) {
            threadQuestions.add(position, (Question) item);
        }
        notifyItemInserted(position);
    }

    public void setAnswers(List<Answer> answers) {
        threadAnswers = (answers == null) ? (new ArrayList<Answer>() ) : answers;
    }

    public void setQuestions(List<Question> questions) {
        threadQuestions = (questions == null) ? (new ArrayList<Question>() ) : questions;
    }
    public void remove(Answer item) {
        int position = threadAnswers.indexOf(item);
        threadAnswers.remove(position);
        notifyItemRemoved(position);
    }
}
