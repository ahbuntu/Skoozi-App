package com.megaphone.skoozi.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.megaphone.skoozi.base.BaseVhBinder;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.PresentationUtil;

public class ThreadItemVhBinder<T extends ThreadItemVhBinder.TypeContract> extends BaseVhBinder<T, ThreadItemVhBinder.ViewHolder> {

    public static final int THREAD_QUESTION_TYPE = 491;
    public static final int THREAD_ANSWER_TYPE = 596;

    public interface TypeContract {}

//    public static class ViewHolder extends BaseVhBinder.BaseViewHolder {
//        // Question View Type
//        TextView questionContent;
//
//        // Answer View Type
//        TextView threadTimestamp;
//        TextView threadContent;
//        TextView threadUserName;
//        ImageView threadNameImage;
//
//        public ViewHolder(View itemView, int viewType) {
//            super(itemView);
//            switch (viewType) {
//                case THREAD_QUESTION_TYPE:
//                    initQuestionFields();
//                    break;
//
//                default:
//                case THREAD_ANSWER_TYPE:
//                    initAnswerFields();
//                    break;
//            }
//        }
//
//        private void initQuestionFields() {
//            questionContent = (TextView) itemView.findViewById(R.id.thread_question_content);
//        }
//
//        private void initAnswerFields() {
//            threadTimestamp = (TextView) itemView.findViewById(R.id.thread_answer_timestamp);
//            threadUserName = (TextView) itemView.findViewById(R.id.thread_answer_profile_name);
//            threadContent = (TextView) itemView.findViewById(R.id.thread_answer_content);
//            threadNameImage = (ImageView) itemView.findViewById(R.id.thread_list_name_image);
//        }
//    }

    abstract public static class ViewHolder extends BaseVhBinder.BaseViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class QuestionViewHolder extends ViewHolder {
        // Question View Type
        TextView questionContent;

        public QuestionViewHolder(View itemView) {
            super(itemView);
            initQuestionFields();
        }

        private void initQuestionFields() {
            questionContent = (TextView) itemView.findViewById(R.id.thread_question_content);
        }
    }

    public static class AnswerViewHolder extends ViewHolder {
        // Answer View Type
        TextView threadTimestamp;
        TextView threadContent;
        TextView threadUserName;
        ImageView threadNameImage;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            initAnswerFields();
        }

        private void initAnswerFields() {
            threadTimestamp = (TextView) itemView.findViewById(R.id.thread_answer_timestamp);
            threadUserName = (TextView) itemView.findViewById(R.id.thread_answer_profile_name);
            threadContent = (TextView) itemView.findViewById(R.id.thread_answer_content);
            threadNameImage = (ImageView) itemView.findViewById(R.id.thread_list_name_image);
        }
    }

    @Override
    public BaseViewHolder create(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case THREAD_QUESTION_TYPE:
                return new QuestionViewHolder(inflater.inflate(R.layout.thread_question_item,
                        parent, false));
            default:
            case THREAD_ANSWER_TYPE:
                return new AnswerViewHolder(inflater.inflate(R.layout.thread_answer_item,
                        parent, false));
        }
    }

    @Override
    public void bind(final ViewHolder holder, T item) {
        if (item instanceof Question) {
            bindQuestion(holder, (Question) item);
        } else if (item instanceof Answer) {
            bindAnswer(holder, (Answer) item);
        }
    }

    private void bindQuestion(final ViewHolder viewHolder, Question question) {
        QuestionViewHolder holder = (QuestionViewHolder) viewHolder;
        holder.questionContent.setText(question.content);
    }

    private void bindAnswer(final ViewHolder viewHolder, Answer answer) {
        AnswerViewHolder holder = (AnswerViewHolder) viewHolder;

        holder.threadTimestamp.setText(PresentationUtil.unixTimestampAge(answer.timestamp));
        holder.threadUserName.setText(answer.author);
        holder.threadContent.setText(answer.content);

        //todo: decision to display user image or letter should be made here
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int nameImageColor = generator.getRandomColor();
        TextDrawable nameDrawable = TextDrawable.builder()
                .buildRound(answer.author.substring(0, 1).toUpperCase(), nameImageColor);
        holder.threadNameImage.setImageDrawable(nameDrawable);
        holder.threadNameImage.setVisibility(View.VISIBLE);
    }
}
