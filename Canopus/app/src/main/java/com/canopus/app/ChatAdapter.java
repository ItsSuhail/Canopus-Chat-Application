package com.canopus.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private String[] starDataUser;
    private String[] starDataMsg;
    private String[] starDataTimeMsg;
    private String userName;

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
        private final LinearLayout parentRight, parentLeft;
        private final TextView textViewUR;
        private final TextView textViewMR;
        private final TextView textViewTR;
        private final TextView textViewUL;
        private final TextView textViewML;
        private final TextView textViewTL;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textViewUR = (TextView) view.findViewById(R.id.userRight);
            textViewMR = (TextView) view.findViewById(R.id.msgOfUserRight);
            textViewTR = (TextView) view.findViewById(R.id.timeOfMsgRight);

            textViewUL = (TextView) view.findViewById(R.id.userLeft);
            textViewML = (TextView) view.findViewById(R.id.msgOfUserLeft);
            textViewTL = (TextView) view.findViewById(R.id.timeOfMsgLeft);

            parentLeft = (LinearLayout) view.findViewById(R.id.pLinearLayoutLeft);
            parentRight = (LinearLayout) view.findViewById(R.id.pLinearLayoutRight);
        }

        public TextView getTextViewUserR() {
            return textViewUR;
        }
        public TextView getTextViewMsgR() {
            return textViewMR;
        }
        public TextView getTextViewTimeR() {
            return textViewTR;
        }


        public TextView getTextViewUserL() {
            return textViewUL;
        }
        public TextView getTextViewMsgL() {
            return textViewML;
        }
        public TextView getTextViewTimeL() { return textViewTL; }

        public LinearLayout getParentRight() {
            return parentRight;
        }

        public LinearLayout getParentLeft() {
            return parentLeft;
        }

        public void bind(final String sUser, final String sMessage, final String sTime, final String userName){
            if(sUser.equals(userName)){
                getParentLeft().setVisibility(View.GONE);
                getParentRight().setVisibility(View.VISIBLE);
                getTextViewUserR().setText("You");
                getTextViewMsgR().setText(sMessage);
                getTextViewTimeR().setText(sTime);
            }
            else{
                getParentRight().setVisibility(View.GONE);
                getParentLeft().setVisibility(View.VISIBLE);
                getTextViewUserL().setText(sUser);
                getTextViewMsgL().setText(sMessage);
                getTextViewTimeL().setText(sTime);
            }

        }

    }

    public ChatAdapter(String[] dataSetUser, String [] dataSetMsg, String [] dataSetTime, String userName) {
        starDataUser = dataSetUser;
        starDataMsg = dataSetMsg;
        starDataTimeMsg = dataSetTime;
        this.userName = userName;
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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.getTextView().setText(starDataName[position]);
        viewHolder.bind(starDataUser[position], starDataMsg[position], starDataTimeMsg[position], userName);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return starDataUser.length;
    }
}
