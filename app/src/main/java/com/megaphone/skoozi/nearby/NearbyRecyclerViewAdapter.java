package com.megaphone.skoozi.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.maps.GoogleMap;
import com.megaphone.skoozi.R;
import com.megaphone.skoozi.thread.ThreadActivity;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.PresentationUtil;

import java.util.List;

/**
 * Created by ahmadulhassan on 2015-06-27.
 */
public class NearbyRecyclerViewAdapter extends RecyclerView.Adapter<NearbyRecyclerViewAdapter.NearbyViewHolder> {

    private Context context;
    private List<Question> nearbyQuestions;
    private GoogleMap nearbyMap;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view vhSupplier
    public class NearbyViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener{
        // each data item is just a string in this case
        TextView nearbyTimestamp;
        TextView nearbyContent;
        TextView nearbyUserName;
        ImageView nearbyNameImage;

        public NearbyViewHolder(View itemView) {
            super(itemView);
            nearbyTimestamp = (TextView) itemView.findViewById(R.id.nearby_list_question_duration);
            nearbyUserName = (TextView) itemView.findViewById(R.id.nearby_list_profile_name);
            nearbyContent = (TextView) itemView.findViewById(R.id.nearby_list_question);
            nearbyNameImage = (ImageView) itemView.findViewById(R.id.nearby_list_name_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition(); // gets item position
            Question clickedQuestion = nearbyQuestions.get(position);
            Intent threadIntent = new Intent(context, ThreadActivity.class);
            Bundle questionBundle = new Bundle();
            questionBundle.putParcelable(ThreadActivity.EXTRA_QUESTION, clickedQuestion);
            threadIntent.putExtras(questionBundle);
            context.startActivity(threadIntent);
        }
    }

    /**
     *
     * @param context
     * @param questions NULL value is acceptable.
     */
    public NearbyRecyclerViewAdapter(Context context, List<Question> questions, GoogleMap map) {
        this.context = context;
        nearbyQuestions = questions;
        nearbyMap = map;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NearbyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nearby_item_row_new, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final NearbyViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        Question questionItem = nearbyQuestions.get(position);
        holder.nearbyTimestamp.setText(PresentationUtil.unixTimestampAge(questionItem.timestamp));
        holder.nearbyUserName.setText(questionItem.author);
        holder.nearbyContent.setText(questionItem.content);

        //todo: decision to display user image or letter should be made here
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int nameImageColor = generator.getRandomColor();
        TextDrawable nameDrawable = TextDrawable.builder()
                .buildRound(questionItem.author.substring(0,1).toUpperCase(), nameImageColor);
//        vhSupplier.nearbyNameImage.setImageDrawable(nameDrawable);
//        vhSupplier.nearbyNameImage.setVisibility(View.VISIBLE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return nearbyQuestions == null ? 0 : nearbyQuestions.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public void updateNearbyQuestions(List<Question> questions) {
        nearbyQuestions = questions;
        notifyDataSetChanged();
    }
}
