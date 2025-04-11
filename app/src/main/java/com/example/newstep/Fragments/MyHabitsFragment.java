package com.example.newstep.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.newstep.Adapters.HabitsAdapter;
import com.example.newstep.Models.HabitModel;
import com.example.newstep.R;
import com.example.newstep.Databases.MyHabitsDatabaseHelper;

import java.util.List;


public class MyHabitsFragment extends Fragment {
    Button addBtn;
    MyHabitsDatabaseHelper db;
    RecyclerView habitsRecyclerView;
    List<HabitModel> list;
    HabitsAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_habits, container, false);
        db=new MyHabitsDatabaseHelper(getContext());
        list=db.getAllHabits();
        habitsRecyclerView=rootView.findViewById(R.id.habits_recyclerView);
        addBtn=rootView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(AddHabitFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                AddHabitFragment addHabitFragment = new AddHabitFragment();
                transaction.replace(R.id.fragment_container, addHabitFragment, AddHabitFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });


        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHabits();
   return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadHabits();
    }

    private void loadHabits() {
        List<HabitModel> models=db.getAllHabits();
        adapter = new HabitsAdapter(getContext(),models);
        habitsRecyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(habitsRecyclerView);
        adapter.notifyDataSetChanged();
        list=db.getAllHabits();
    }
    public void showDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete all habits")
                .setMessage("Are you sure you want to delete everything ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.deleteEverything();

                })
                .setNegativeButton("No", null)
                .show();
    }
    ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
            builder.setTitle("Delete Habit");
            builder.setMessage("Are you sure ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int p=viewHolder.getAdapterPosition();
                    HabitModel habit = list.get(p);
                    db.deleteHabit(habit.getId());
                    list.remove(p);
                    adapter.notifyItemRemoved(p);
                    loadHabits();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int p=viewHolder.getAdapterPosition();
                    adapter.notifyItemChanged(p);
                    loadHabits();
                }
            });
            builder.show();
        }
    };

}