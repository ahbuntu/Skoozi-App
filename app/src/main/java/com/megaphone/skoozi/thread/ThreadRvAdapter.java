package com.megaphone.skoozi.thread;


import android.view.ViewGroup;

import com.megaphone.skoozi.base.BaseRvAdapter;
import com.megaphone.skoozi.base.BaseVhMaker;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;

import java.util.ArrayList;
import java.util.List;

public class ThreadRvAdapter extends BaseRvAdapter<ThreadVhMaker.TypeContract, ThreadVhMaker.ViewHolder> {

    private static final int ROW_ANSWER_TYPE = 2000;

    private List<Answer> threadAnswers;
    private List<Question> threadQuestions;

    public ThreadRvAdapter() {
        vhMaker = new ThreadVhMaker<>();
    }

    @Override
    public BaseVhMaker.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhMaker.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhMaker.BaseViewHolder sectionViewHolder, int position) {
        vhMaker.bind((ThreadVhMaker.ViewHolder) sectionViewHolder, getItem(position));
    }

    @Override
    protected ThreadVhMaker.TypeContract getItem(int position) {
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

    public void add(int position, ThreadVhMaker.TypeContract item) {
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
