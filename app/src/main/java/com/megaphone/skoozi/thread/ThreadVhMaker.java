package com.megaphone.skoozi.thread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.megaphone.skoozi.base.BaseVhMaker;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.model.Answer;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.PresentationUtil;

public class ThreadVhMaker<T extends ThreadVhMaker.TypeContract> extends BaseVhMaker<T, ThreadVhMaker.ContractViewHolder> {

    public interface TypeContract {}

    public static class ContractViewHolder extends BaseVhMaker.BaseViewHolder {
        TextView threadTimestamp;
        TextView threadContent;
        TextView threadUserName;
        ImageView threadNameImage;

        public ContractViewHolder(View itemView) {
            super(itemView);
            threadTimestamp = (TextView) itemView.findViewById(R.id.thread_answer_timestamp);
            threadUserName = (TextView) itemView.findViewById(R.id.thread_answer_profile_name);
            threadContent = (TextView) itemView.findViewById(R.id.thread_answer_content);
            threadNameImage = (ImageView) itemView.findViewById(R.id.thread_list_name_image);
        }
    }

    @Override
    public BaseViewHolder create(ViewGroup parent, int viewType) {
        return new ContractViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_thread_list, parent));
    }

    @Override
    public void bind(final ContractViewHolder holder, T item) {
        if (item instanceof Question) {
            bindQuestion(holder, (Question) item);
        } else if (item instanceof Answer) {
            bindAnswer(holder, (Answer) item);
        }
    }

    private void bindQuestion(final ContractViewHolder holder, Question item) {
        throw new RuntimeException("bind Question not implemented yet");
    }

    private void bindAnswer(final ContractViewHolder holder, Answer item) {
        holder.threadTimestamp.setText(PresentationUtil.unixTimestampAge(item.timestamp));
        holder.threadUserName.setText(item.author);
        holder.threadContent.setText(item.content);

        //todo: decision to display user image or letter should be made here
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int nameImageColor = generator.getRandomColor();
        TextDrawable nameDrawable = TextDrawable.builder()
                .buildRound(item.author.substring(0, 1).toUpperCase(), nameImageColor);
        holder.threadNameImage.setImageDrawable(nameDrawable);
        holder.threadNameImage.setVisibility(View.VISIBLE);
    }
}
