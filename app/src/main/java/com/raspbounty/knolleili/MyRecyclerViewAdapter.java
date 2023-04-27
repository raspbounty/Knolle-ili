package com.raspbounty.knolleili;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private final List<Chest> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final Context mContext;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<Chest> data) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chest item = mData.get(position);
        holder.contentTv.setText(item.content);
        holder.shelfTv.setText(item.shelfLong);
        holder.storageTv.setText(item.roomLong);
        holder.coordsTv.setText(item.coordsAsString);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView contentTv, storageTv, coordsTv, shelfTv;
        ImageView ivEdit;

        ViewHolder(View itemView) {
            super(itemView);
            contentTv = itemView.findViewById(R.id.tv_content);
            storageTv = itemView.findViewById(R.id.tv_storage);
            shelfTv = itemView.findViewById(R.id.tv_shelf);
            coordsTv = itemView.findViewById(R.id.tv_coords);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            itemView.setOnClickListener(this);
            contentTv.setOnClickListener(this);
            storageTv.setOnClickListener(this);
            coordsTv.setOnClickListener(this);
            shelfTv.setOnClickListener(this);
            ivEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Chest getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
