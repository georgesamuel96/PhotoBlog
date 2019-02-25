package com.example.georgesamuel.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private ArrayList<Comments> list = new ArrayList<>();
    private Context context;

    public CommentAdapter(ArrayList<Comments> list){
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_list_item, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        context = viewGroup.getContext();
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.setIsRecyclable(false);

        myViewHolder.message.setText(list.get(i).getMessage());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyy");
        String timeFormat = sdf.format(list.get(i).getTimestamp());
        myViewHolder.date.setText(timeFormat);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView message, date;

        public MyViewHolder(View view){
            super(view);

            message = (TextView) view.findViewById(R.id.comment_message);
            date = (TextView) view.findViewById(R.id.time);
        }
    }
}
