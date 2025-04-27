package com.example.newstep.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstep.Adapters.GoalsAdapter;
import com.example.newstep.Adapters.HabitsAdapter;
import com.example.newstep.Databases.MyGoalsDatabaseHelper;
import com.example.newstep.Models.GoalModel;
import com.example.newstep.Models.HabitModel;
import com.example.newstep.R;
import com.example.newstep.Util.Utilities;
import com.google.android.material.chip.ChipGroup;

import java.util.List;


public class GoalsFragment extends Fragment {
TextView nb_completed,nb_in_progress,nb_goals,average_progress,textUnder;
ImageView add_goal;
RecyclerView recycler_goals;
List<GoalModel> goals;
MyGoalsDatabaseHelper db;
GoalsAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_goals, container, false);
        db=new MyGoalsDatabaseHelper(getContext());
        goals=db.getAllGoals();
        nb_goals=rootView.findViewById(R.id.nb_goals);
        nb_completed=rootView.findViewById(R.id.nb_completed);
        nb_in_progress=rootView.findViewById(R.id.nb_in_progress);
        average_progress=rootView.findViewById(R.id.average_progress);
        add_goal=rootView.findViewById(R.id.add_goal);
        recycler_goals=rootView.findViewById(R.id.recycler_goals);
        textUnder=rootView.findViewById(R.id.text_under_nb);
if(goals.size()==0){
    textUnder.setText("Your first goal is the first step toward greatness!");
}else  if(goals.size()>0 && goals.size()<5){
    textUnder.setText("Ambition looks good on you!");
}else if (goals.size()>5 ){
    textUnder.setText("Your dedication is inspiring!");
}

        nb_goals.setText(""+db.getTotalGoals());
        nb_completed.setText(""+db.getCompletedGoals());
        nb_in_progress.setText(""+db.getInProgressGoals());
        average_progress.setText(db.getAverageProgress()+"%");

        add_goal.setOnClickListener(v->addGoal());
        setupRecycler();
        return rootView;

    }

    private void addGoal() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View popUpView = inflater.inflate(R.layout.add_goal_popup, null);
        dimBackground(0.5f);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        View rootLayout = getActivity().findViewById(android.R.id.content);
        popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
            layoutParams.alpha = 1.0f;
            getActivity().getWindow().setAttributes(layoutParams);
        });
        popupWindow.setOnDismissListener(() -> dimBackground(1.0f));
        EditText goal_name=popUpView.findViewById(R.id.goal_name);
        EditText goal_desc=popUpView.findViewById(R.id.goal_desc);
        ChipGroup chipGroupIcon=popUpView.findViewById(R.id.chipGroupIcon);
        TextView progress_text_view=popUpView.findViewById(R.id.progress_text_view);
        SeekBar progress=popUpView.findViewById(R.id.progress);
        ImageView back=popUpView.findViewById(R.id.back);
        back.setOnClickListener(v->{popupWindow.dismiss();});
        Button add_goal=popUpView.findViewById(R.id.btn_add_goal);
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
add_goal.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String txtName=goal_name.getText().toString();
        String txtDesc=goal_desc.getText().toString();
        int seek=progress.getProgress();
        if(txtName.isEmpty()){
            Toast.makeText(getContext(),"Please enter a goal name",Toast.LENGTH_SHORT).show();
        }else{
        GoalModel model=new GoalModel();
        model.setDesc(txtDesc);
        model.setTitle(txtName);
        model.setProgress(seek);
            int iconId=chipGroupIcon.getCheckedChipId();
            String iconName=getResources().getResourceEntryName(iconId);
            Log.d("iconGoal: ",""+iconName);
            model.setIcon(iconName);
            db.addGoal(model);
            updateStatsAndMessage();
            goals.add(model);
            adapter.notifyItemInserted(goals.size() - 1);
        popupWindow.dismiss();
        setupRecycler();
    }}
});
    }

    public void setupRecycler() {
        goals = db.getAllGoals();
        adapter = new GoalsAdapter(getContext(), goals);
        recycler_goals.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_goals.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recycler_goals);

        adapter.notifyDataSetChanged();
    }

    ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
            builder.setTitle("Delete Goal");
            builder.setMessage("Are you sure ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int p=viewHolder.getAdapterPosition();
                    GoalModel goal = goals.get(p);
                    db.deleteGoal(goal.getId());
                    goals.remove(p);
                    updateStatsAndMessage();
                    adapter.notifyItemRemoved(p);
                    setupRecycler();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int p=viewHolder.getAdapterPosition();
                    adapter.notifyItemChanged(p);
                    setupRecycler();
                }
            });
            builder.show();
        }
    };
    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = alpha;
        getActivity().getWindow().setAttributes(layoutParams);
    }
    private void updateStatsAndMessage() {
        nb_goals.setText("" + db.getTotalGoals());
        nb_completed.setText("" + db.getCompletedGoals());
        nb_in_progress.setText("" + db.getInProgressGoals());
        average_progress.setText(db.getAverageProgress() + "%");

        if (goals.size() == 0) {
            textUnder.setText("Your first goal is the first step toward greatness!");
        } else if (goals.size() < 5) {
            textUnder.setText("Ambition looks good on you!");
        } else {
            textUnder.setText("Your dedication is inspiring!");
        }
    }
}