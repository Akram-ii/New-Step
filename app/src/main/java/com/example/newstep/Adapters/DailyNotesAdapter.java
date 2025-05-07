package com.example.newstep.Adapters;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;



import com.example.newstep.Models.DailyNoteModel;
import com.example.newstep.R;
import com.example.newstep.Databases.MyHabitsDatabaseHelper;

import java.util.List;

public class DailyNotesAdapter extends RecyclerView.Adapter<DailyNotesAdapter.NoteViewHolder> {
    Context context;
    List<DailyNoteModel> list;
    MyHabitsDatabaseHelper db;

    public DailyNotesAdapter(Context context, List<DailyNoteModel> noteList, MyHabitsDatabaseHelper db) {
        this.context = context;
        this.list= noteList;
        this.db=db;
    }
    @NonNull
    @Override
    public DailyNotesAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DailyNotesAdapter.NoteViewHolder(LayoutInflater.from(context).inflate(R.layout.day_row,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull DailyNotesAdapter.NoteViewHolder holder, int position) {

        DailyNoteModel dailyNoteModel=list.get(position);
        Log.d("CLICK_DE", "Opening LogActivity for habit ID: " + dailyNoteModel);
        if(dailyNoteModel.getResisted()!=null && !dailyNoteModel.getResisted()){
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_for_buttons));
        }
        holder.day.setText("Day "+position);

        //holder.mood.setText("You were feeling "+dailyNoteModel.getMood());
        holder.mood.setText(dailyNoteModel.getMood());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();


popUpWindow(dailyNoteModel.getId(), dailyNoteModel.getMood(), dailyNoteModel.getNote(), dailyNoteModel.getDate(),holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView mood,day;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mood=itemView.findViewById(R.id.mood);
            day=itemView.findViewById(R.id.day);
        }
    }
    private void popUpWindow(int noteId,String inputMood,String inputNote,String date,int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUpView = inflater.inflate(R.layout.log_day, null);
        builder.setView(popUpView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        }
        dialog.show();
        TextView info=popUpView.findViewById(R.id.info);
        info.setText(date);
        EditText mood = popUpView.findViewById(R.id.mood);
        EditText note = popUpView.findViewById(R.id.note);
        Button save = popUpView.findViewById(R.id.save);
        Button cancel = popUpView.findViewById(R.id.cancel);
        mood.setText(inputMood);
        note.setText(inputNote);
        RelativeLayout relativeLayout=popUpView.findViewById(R.id.l1);
        relativeLayout.setVisibility(View.GONE);
        save.setOnClickListener(v -> {
            String txtMood = mood.getText().toString();
            String txtNote = note.getText().toString();
            list.get(position).setNote(txtNote);
            list.get(position).setMood(txtMood);
            list.get(position).setMood(txtMood);
            db.updateDailyNote(noteId, txtNote, txtMood);

            dialog.dismiss();
            notifyItemChanged(position);
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
    }
}
