package com.example.newstep.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Databases.MyGoalsDatabaseHelper;
import com.example.newstep.Models.GoalModel;
import com.example.newstep.R;

import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    Context context;
    List<GoalModel> goalList;
    MyGoalsDatabaseHelper db;
    public GoalsAdapter(Context context, List<GoalModel> goalList) {
        this.context = context;
        this.goalList = goalList;
        db=new MyGoalsDatabaseHelper(context);
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
        }else{
        popupWindowEdit(goal.getId(),goal.getTitle(),goal.getDesc(),goal.getProgress());
    }
            }
        });
holder.itemView.setOnLongClickListener(v->{deleteGoal(goal.getId()); return true;});
    }

    private void deleteGoal(int id) {
    db.deleteGoal(id);
    goalList.remove(id);
    notifyDataSetChanged();
    Toast.makeText(context,"Goal Deleted ",Toast.LENGTH_SHORT);
    }

    private void popupWindowEdit(int id,String title1,String desc1,int progress1) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View popUpView = inflater.inflate(R.layout.add_goal_popup, null);
            dimBackground(0.5f);
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;

            final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
            View rootLayout = ((Activity) context).findViewById(android.R.id.content);
            popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
            popupWindow.setOnDismissListener(() -> {
                WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
                layoutParams.alpha = 1.0f;
                ((Activity) context).getWindow().setAttributes(layoutParams);
            });
            popupWindow.setOnDismissListener(() -> dimBackground(1.0f));
            EditText name=popUpView.findViewById(R.id.goal_name),desc=popUpView.findViewById(R.id.goal_desc);
            TextView title=popUpView.findViewById(R.id.title),progress_text_view=popUpView.findViewById(R.id.progress_text_view);
            Button save=popUpView.findViewById(R.id.btn_add_goal);
            SeekBar progress=popUpView.findViewById(R.id.progress);
            ImageView back=popUpView.findViewById(R.id.back);
            back.setOnClickListener(v->{popupWindow.dismiss();});
            title.setText("Update Your Goal");
            name.setText(title1);
            desc.setText(desc1);
            save.setText("Save Goal");
            progress.setProgress(progress1);
            progress_text_view.setVisibility(View.VISIBLE);
            progress_text_view.setText(progress1+"%");
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress_text_view.setVisibility(View.VISIBLE);
                progress_text_view.setText(i+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String txtName=name.getText().toString();
                    String txtDesc=desc.getText().toString();
                    int seek=progress.getProgress();
                    if(txtName.isEmpty()){
                        Toast.makeText(context,"Please enter a goal name",Toast.LENGTH_SHORT).show();
                    }else{
                        GoalModel model=new GoalModel();
                        model.setDesc(txtDesc);
                        model.setTitle(txtName);
                        model.setProgress(seek);

                        db.updateGoal(id,txtName,txtDesc,seek);
                        for (int i = 0; i < goalList.size(); i++) {
                            if (goalList.get(i).getId() == id) {
                                goalList.get(i).setTitle(txtName);
                                goalList.get(i).setDesc(txtDesc);
                                goalList.get(i).setProgress(seek);
                                break;
                            }
                        }

                        notifyDataSetChanged();
                        Toast.makeText(context,"Goal Updated",Toast.LENGTH_SHORT).show();
                        popupWindow.dismiss();
                }}
            });
    }


    @Override
    public int getItemCount() {
        return goalList.size();
    }
    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
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
