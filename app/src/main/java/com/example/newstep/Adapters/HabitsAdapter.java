package com.example.newstep.Adapters;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.example.newstep.HabitDetailsActivity;
import com.example.newstep.MainActivity;
import com.example.newstep.Models.HabitModel;
import com.example.newstep.R;
import com.example.newstep.Util.DatabaseHelper;
import com.example.newstep.Util.NotifReceiver;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitsViewHolder> {
    Context context;
    List<HabitModel> list;
    String desc=" : Don't forget to log your day";

SQLiteDatabase notifDB;
    @NonNull
    @Override
    public HabitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HabitsViewHolder(LayoutInflater.from(context).inflate(R.layout.habit,parent,false));
    }

    public HabitsAdapter(Context context, List<HabitModel> list) {
        this.context = context;
        this.list = list;
        notifDB = context.openOrCreateDatabase("reminderDB", Context.MODE_PRIVATE, null);
        notifDB.execSQL("CREATE TABLE IF NOT EXISTS Reminder(Time TEXT, Title TEXT, Description TEXT);");
    }

    @Override
    public void onBindViewHolder(@NonNull HabitsViewHolder holder, int position) {
        HabitModel habitModel=list.get(position);
        holder.name.setText(habitModel.getHabit_name());
        holder.cat.setText(habitModel.getCat());
        if(habitModel.getTotalDays()==1){
            holder.streak.setText("You resisted " + habitModel.getTotalDays() + " day");
        }else {
            holder.streak.setText("You resisted " + habitModel.getTotalDays() + " days");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, HabitDetailsActivity.class);
                intent.putExtra("habit_id",habitModel.getId());
                intent.putExtra("habitName",habitModel.getHabit_name());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDialog(holder.getAdapterPosition(),habitModel);
                return true;
            }
        });
    }

    private void showDialog(int adapterPosition, HabitModel habitModel) {
        new AlertDialog.Builder(context).setMessage("What do you want to do ?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteHabit(adapterPosition);
                })
                .setNegativeButton("Edit reminder time", (dialog,which)->{

                })
                .setNeutralButton("Cancel",null)
                .show();
    }




    @Override
    public int getItemCount() {
        return list.size();
    }
    class HabitsViewHolder extends RecyclerView.ViewHolder{
        private TextView name,streak,cat;
        public HabitsViewHolder(@NonNull View itemView) {
            super(itemView);
            cat=itemView.findViewById(R.id.cat);
            streak=itemView.findViewById(R.id.days);
            name=itemView.findViewById(R.id.name);
        }
    }
    private void deleteHabit(int position) {
        DatabaseHelper db = new DatabaseHelper(context);
        HabitModel habit = list.get(position);
        db.deleteHabit(habit.getId());
        list.remove(position);
        notifyItemRemoved(position);
    }


}

