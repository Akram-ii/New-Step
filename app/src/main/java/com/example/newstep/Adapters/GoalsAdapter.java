package com.example.newstep.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.GoalModel;
import com.example.newstep.R;

import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    Context context;
    List<GoalModel> goalList;

    public GoalsAdapter(Context context, List<GoalModel> goalList) {
        this.context = context;
        this.goalList = goalList;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GoalViewHolder(LayoutInflater.from(context).inflate(R.layout.goal_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalModel goal = goalList.get(position);
        if(goal.getProgress() == 100){
            holder.completed.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.progressTextView.setVisibility(View.GONE);
        }else{
            holder.progressTextView.setText(goal.getProgress() + "%");
            holder.progressBar.setProgress(goal.getProgress());
        }
        holder.title.setText(goal.getTitle());
        holder.desc.setText(goal.getDesc());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
    if(holder.completed.getVisibility() == View.VISIBLE){
        Toast.makeText(context,"You completed this Goal",Toast.LENGTH_SHORT).show();
    }
            }
        });

    }



    @Override
    public int getItemCount() {
        return goalList.size();
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, progressTextView;
        ProgressBar  progressBar;
        ImageView completed;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            desc= itemView.findViewById(R.id.desc);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progressTextView= itemView.findViewById(R.id.progress_text_view);
            completed = itemView.findViewById(R.id.completed);
        }
    }
}
