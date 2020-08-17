package com.cra.cra;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Posts_Adapter extends RecyclerView.Adapter<Posts_Adapter.ViewHolder> {
    private OnPostListener onpostlistener;
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView Name_Text,Description_Text;
        ConstraintLayout background;
        OnPostListener onPostListener;
        public ViewHolder(@NonNull View itemView, OnPostListener onPostListener) {
            super(itemView);
            Name_Text = itemView.findViewById(R.id.Blue_Post_book_Name);
            Description_Text = itemView.findViewById(R.id.Blue_Post_book_Desc);
            background = itemView.findViewById(R.id.post_design_layout);
            this.onPostListener = onPostListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onPostListener.OnPostClick(getAdapterPosition());
        }
    }

    private Context context;
    private List<Book> post_list;

    public Posts_Adapter(Context context, List<Book> post_list,OnPostListener onpostlistener) {
        this.context = context;
        this.post_list = post_list;
        this.onpostlistener = onpostlistener;
    }

    @NonNull
    @Override
    public Posts_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.post_design,parent,false);
        return new ViewHolder(v,onpostlistener);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull Posts_Adapter.ViewHolder holder, int position) {
        Book post = post_list.get(position);
        if (position % 2 == 0)
        {
            holder.Name_Text.setTextColor(Color.parseColor("#2A373D"));
            holder.background.setBackgroundResource(R.drawable.button);
        }
        else
        {
            holder.Name_Text.setTextColor(Color.parseColor("#A94543"));
            holder.background.setBackgroundResource(R.drawable.button1);
        }
        holder.Name_Text.setText(post.getName().substring(0,post.getName().length()-7));
        String post_desc = post.getDescription();
        if (post_desc.length() > 100)
            post_desc = post_desc.substring(0,100) + "...";
        holder.Description_Text.setText(post_desc);
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    public interface OnPostListener{
        void OnPostClick(int position);
    }
}
