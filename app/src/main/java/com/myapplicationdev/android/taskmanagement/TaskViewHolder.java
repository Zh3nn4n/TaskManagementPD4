package com.myapplicationdev.android.taskmanagement;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.myapplicationdev.android.taskmanagement.Interface.ItemClickListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView title, desc, date;
    public ItemClickListener listener;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.taskTitle);
        desc = (TextView) itemView.findViewById(R.id.taskDesc);
        date = (TextView) itemView.findViewById(R.id.taskDate);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v,getAdapterPosition(),false);

    }
}
