package com.canopus.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private String[] starDataUsers;
    private String[] starDataMessages;
    private String[] starDataTimings;
    private String cUsername;

//    private final StarAdapter.OnItemClickListener listener;
//
//    /**
//     * Provide a reference to the type of views that you are using
//     * (custom ViewHolder).
//     */
//
//    public interface OnItemClickListener {
//        void onItemClick(String starName, String starPassword);
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rightLl, leftLl;
        private final TextView userRightLbl;
        private final TextView messageRightLbl;
        private final TextView timeRightLbl;
        private final TextView userLeftLbl;
        private final TextView messageLeftLbl;
        private final TextView timeLeftLbl;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            userRightLbl = (TextView) view.findViewById(R.id.lblUserRight);
            messageRightLbl = (TextView) view.findViewById(R.id.lblMessageRight);
            timeRightLbl = (TextView) view.findViewById(R.id.lblTimeRight);

            userLeftLbl = (TextView) view.findViewById(R.id.lblUserLeft);
            messageLeftLbl = (TextView) view.findViewById(R.id.lblMessageLeft);
            timeLeftLbl = (TextView) view.findViewById(R.id.lblTimeLeft);

            leftLl = (LinearLayout) view.findViewById(R.id.llLeft);
            rightLl = (LinearLayout) view.findViewById(R.id.llRight);
        }

        public TextView getUserRightLbl() {
            return userRightLbl;
        }
        public TextView getMessageRightLbl() {
            return messageRightLbl;
        }
        public TextView getTimeRightLbl() {
            return timeRightLbl;
        }

        public TextView getUserLeftLbl() {
            return userLeftLbl;
        }
        public TextView getMessageLeftLbl() {
            return messageLeftLbl;
        }
        public TextView getTimeLeftLbl() {
            return timeLeftLbl;
        }

        public LinearLayout getRightLl() {
            return rightLl;
        }

        public LinearLayout getLeftLl() {
            return leftLl;
        }

        public void bind(final String sUser, final String sMessage, final String sTime, final String sUsername){
            if(sUser.equals(sUsername)){
                getLeftLl().setVisibility(View.GONE);
                getRightLl().setVisibility(View.VISIBLE);
                getUserRightLbl().setText("You");
                getMessageRightLbl().setText(sMessage);
                getTimeRightLbl().setText(sTime);
            }
            else{
                getRightLl().setVisibility(View.GONE);
                getLeftLl().setVisibility(View.VISIBLE);
                getUserLeftLbl().setText(sUser);
                getMessageLeftLbl().setText(sMessage);
                getTimeLeftLbl().setText(sTime);
            }

        }

    }

    public ChatAdapter(String[] dataSetUsers, String [] dataSetMessages, String [] dataSetTimings, String currentUsername) {
        starDataUsers = dataSetUsers;
        starDataMessages = dataSetMessages;
        starDataTimings = dataSetTimings;
        this.cUsername = currentUsername;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_view, viewGroup, false);

        return new ChatAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder viewHolder, final int position) {

        viewHolder.bind(starDataUsers[position], starDataMessages[position], starDataTimings[position], cUsername);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return starDataUsers.length;
    }
}
