package com.megaphone.skoozi.thread;


import android.util.Log;
import android.view.ViewGroup;

import com.megaphone.skoozi.base.BaseRvAdapter;
import com.megaphone.skoozi.base.BaseVhBinder;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;

import java.util.ArrayList;
import java.util.List;

public class ThreadRvAdapter extends BaseRvAdapter<ThreadItemVhBinder.TypeContract, ThreadItemVhBinder.ViewHolder> {
    private static final String TAG = ThreadRvAdapter.class.getSimpleName();
    private List<Answer> threadAnswers;
    private Question threadQuestion;

    public ThreadRvAdapter() {
        vhBinder = new ThreadItemVhBinder<>();
    }

    @Override
    public BaseVhBinder.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return vhBinder.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseVhBinder.BaseViewHolder sectionViewHolder, int position) {
        vhBinder.bind((ThreadItemVhBinder.ViewHolder) sectionViewHolder, getItem(position));
    }

    @Override
    protected ThreadItemVhBinder.TypeContract getItem(int position) {
        if (position == 0) {
            return threadQuestion;
        } else {
            return threadAnswers.get(position - 1);
        }
    }

    @Override
    public int getItemCount() {
        return 1 + (threadAnswers == null ? 1 : threadAnswers.size());
    }

    @Override
    public int getItemViewType(int position) {
        ThreadItemVhBinder.TypeContract item =  getItem(position);
        if (item instanceof Question) {
            return ThreadItemVhBinder.THREAD_QUESTION_TYPE;
        } else {
            return ThreadItemVhBinder.THREAD_ANSWER_TYPE;
        }
    }

    public void add(int position, ThreadItemVhBinder.TypeContract item) {
        if (item instanceof Answer) {
            threadAnswers.add(position, (Answer) item);
        } else {
            Log.e(TAG, "add: unexpected TypeContract received. only expecting Answers.");
        }
        notifyItemInserted(position);
    }

    public void setAnswers(List<Answer> answers) {
        threadAnswers = (answers == null) ? (new ArrayList<Answer>() ) : answers;
    }

    public void setQuestion(Question question) {
        threadQuestion = question;
    }
    public void remove(Answer item) {
        int position = threadAnswers.indexOf(item);
        threadAnswers.remove(position);
        notifyItemRemoved(position);
    }
}
