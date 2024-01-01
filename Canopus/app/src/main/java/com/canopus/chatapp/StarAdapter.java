package com.canopus.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StarAdapter extends RecyclerView.Adapter<StarAdapter.ViewHolder> {

    private String[] starDataNames;
    private String[] starDataPasswords;
    private final OnItemClickListener listener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public interface OnItemClickListener {
        void onItemClick(String starName, String starPassword);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView starNameLbl;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            starNameLbl = (TextView) view.findViewById(R.id.lblStarName);
        }

        public TextView getTextView() {
            return starNameLbl;
        }

        public void bind(final String sName, final String sPassword, final OnItemClickListener listener){
            getTextView().setText(sName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(sName, sPassword);
                }
            });
        }

    }

    public StarAdapter(String[] dataSetNames, String [] dataSetPasswords, OnItemClickListener l) {
        starDataNames = dataSetNames;
        starDataPasswords = dataSetPasswords;
        listener = l;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.star_view, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.getTextView().setText(starDataName[position]);
        viewHolder.bind(starDataNames[position], starDataPasswords[position], listener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return starDataNames.length;
    }

}
