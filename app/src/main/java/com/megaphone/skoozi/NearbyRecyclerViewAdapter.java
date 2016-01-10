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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.megaphone.skoozi.model.Question;
import com.megaphone.skoozi.util.PresentationUtil;

import java.util.List;

/**
 * Created by ahmadulhassan on 2015-06-27.
 */
public class NearbyRecyclerViewAdapter extends RecyclerView.Adapter<NearbyRecyclerViewAdapter.NearbyViewHolder> {


    public interface OnQuestionItemSelected{
        void onQuestionSelected(Question mQuestion);
    }

    private Context mContext;
    private List<Question> nearbyQuestions;
    private GoogleMap nearbyMap;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class NearbyViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener{
        // each data item is just a string in this case
        TextView nearbyTimestamp;
        TextView nearbyContent;
        TextView nearbyUserName;
        ImageView nearbyNameImage;

        private OnQuestionItemSelected mQuestionItemCallback;
        /**
         * initializes all the views for a data item in a view holder
         * @param itemView
         */
        public NearbyViewHolder(Context context, View itemView) {
            super(itemView);
            nearbyTimestamp = (TextView) itemView.findViewById(R.id.nearby_list_question_duration);
            nearbyUserName = (TextView) itemView.findViewById(R.id.nearby_list_profile_name);
            nearbyContent = (TextView) itemView.findViewById(R.id.nearby_list_question);
            nearbyNameImage = (ImageView) itemView.findViewById(R.id.nearby_list_name_image);
//            this.context = context;
//            mQuestionItemCallback = (OnQuestionItemSelected) context;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition(); // gets item position
            Question clickedQuestion = nearbyQuestions.get(position);
//            mQuestionItemCallback.onQuestionSelected(clickedQuestion);
        }

        protected void displayOnMap(double locLat, double locLon) {
            if (nearbyMap == null) return;

            nearbyMap.addMarker(new MarkerOptions()
                    .position(new LatLng(locLat, locLon))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        }

    }

    /**
     *
     * @param context
     * @param questions NULL value is acceptable.
     */
    public NearbyRecyclerViewAdapter(Context context, List<Question> questions, GoogleMap map) {
        mContext = context;
        nearbyQuestions = questions;
        nearbyMap = map;
    }

    //region AnswerViewHolder Lifecycle callbacks

    // Create new views (invoked by the layout manager)
    @Override
    public NearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_nearby_list, parent, false);
        return new NearbyViewHolder(mContext, v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final NearbyViewHolder holder, int position) {
        final int viewType = holder.getItemViewType();
        Question questionItem = nearbyQuestions.get(position);
        holder.nearbyTimestamp.setText(PresentationUtil.unixTimestampAge(questionItem.timestamp));
        holder.nearbyUserName.setText(questionItem.author);
        holder.nearbyContent.setText(questionItem.content);
        holder.displayOnMap(questionItem.locationLat, questionItem.locationLon);

        //todo: decision to display user image or letter should be made here
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int nameImageColor = generator.getRandomColor();
        TextDrawable nameDrawable = TextDrawable.builder()
                .buildRound(questionItem.author.substring(0,1).toUpperCase(), nameImageColor);
        holder.nearbyNameImage.setImageDrawable(nameDrawable);
        holder.nearbyNameImage.setVisibility(View.VISIBLE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return nearbyQuestions == null ? 0 : nearbyQuestions.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return threadAnswers == null ? CARD_EMPTY_TYPE : CARD_ANSWER_TYPE;
        return 0;
    }
    //endregion

    public void updateNearbyQuestions(List<Question> questions) {
        nearbyQuestions = questions;
        notifyDataSetChanged();
    }
}
