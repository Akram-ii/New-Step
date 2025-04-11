package com.example.newstep.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newstep.Databases.MyHabitsDatabaseHelper;
import com.example.newstep.Models.HabitModel;
import com.example.newstep.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


public class ProgressFragment extends Fragment {

    private BarChart barChart;
    private MyHabitsDatabaseHelper hdbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_progress, container, false);



        barChart = view.findViewById(R.id.barChart);
        hdbHelper = new MyHabitsDatabaseHelper(getContext());

        List<HabitModel> habits = hdbHelper.getAllHabits();
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> habitsNames = new ArrayList<>();

        for (int i = 0; i < habits.size(); i++) {
            HabitModel habit = habits.get(i);
            entries.add(new BarEntry(i, habit.getTotalDays()));
            habitsNames.add(habit.getHabit_name());
        }

        if (entries.isEmpty()) {
            barChart.setNoDataText("No data yet, add a habit to view progress.");
            return view;
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Total Resistance Days");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);
        barChart.setData(data);
        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.animateX(1000);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        if (habits.size() <= 8) {
            barChart.setVisibleXRangeMaximum(habits.size());
        } else {
            barChart.setVisibleXRangeMaximum(8);
        }
        barChart.moveViewToX(0);
        barChart.invalidate();


        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(habitsNames));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(true);
        x.setLabelRotationAngle(-10);
        x.setGranularity(1f);
        x.setLabelCount(habitsNames.size());

        YAxis y = barChart.getAxisLeft();
        y.setAxisMinimum(0f);
        y.setGranularity(1f);
        y.setGranularityEnabled(true);
        y.setLabelCount(6, false);
        y.setDrawGridLines(true);





        int maxDays = 0;
        for(int i=0; i< habits.size() ; i++){
            HabitModel habit =habits.get(i);
            if(habit.getTotalDays()> maxDays){
                maxDays=habit.getTotalDays();
            }
        }



        ArrayList<String> bestHabits = new ArrayList<>();
       for(int i =0 ;i< habits.size();i++){
           HabitModel habit =habits.get(i);
           if(habit.getTotalDays()== maxDays ){
               if(maxDays==1){
                   bestHabits.add(habit.getHabit_name() + ": " + habit.getTotalDays() + " day");
               }
               else{
                   bestHabits.add(habit.getHabit_name() + ": " + habit.getTotalDays() + " days");
               }

           }
       }


        TextView topHabitNames = view.findViewById(R.id.topHabitNames);
        StringBuilder topHabitText = new StringBuilder();


        for (String habit : bestHabits) {
            topHabitText.append("• ").append(habit).append("\n");
        }
        topHabitNames.setText(topHabitText.toString().trim());


        return view;
    }
}