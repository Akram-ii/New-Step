package com.example.newstep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.newstep.Adapters.DailyNotesAdapter;
import com.example.newstep.Models.DailyNoteModel;
import com.example.newstep.R;
import com.example.newstep.Util.DatabaseHelper;
import com.example.newstep.Util.Utilities;
import com.google.firebase.Timestamp;

import java.util.List;

public class HabitDetailsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<DailyNoteModel> dailyNoteModel;
    int habitId;
    DatabaseHelper db;
    DailyNotesAdapter adapter;
    Button log,emergency;
    ImageButton back;
    TextView name;

    RelativeLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        emergency=findViewById(R.id.emergncyBtn);
        layout=findViewById(R.id.main);
        name=findViewById(R.id.name);
        back=findViewById(R.id.back_ImageButton);
        name.setText(getIntent().getStringExtra("habitName"));
        back.setOnClickListener(v->{
            onBackPressed();
        });
        db=new DatabaseHelper(this);
        recyclerView=findViewById(R.id.recyclerView);
        log=findViewById(R.id.logBtn);
        habitId=getIntent().getIntExtra("habit_id",-1);
        Log.d("CLICK_DEBUG", "Opening LogActivity for habit ID: " + habitId);
        loadAdapter();
        Log.d("NOTES_DEBUG", "Notes retrieved: " + dailyNoteModel.size());
        Timestamp timestamp=Timestamp.now();
        String todayDate = Utilities.timestampToStringNoDetail(timestamp);
        if (db.hasNoteForToday(habitId, todayDate)) {
            log.setVisibility(View.GONE);
        }
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               popUpWindow();
            }
        });
        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popUpWindowEmergency();
            }
        });
   emergency.setOnLongClickListener(new View.OnLongClickListener() {
       @Override
       public boolean onLongClick(View view) {
           popUpWindowEmergencyEdit();
           return true;
       }
   });
    }

    private void popUpWindowEmergencyEdit() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.edit_text_popup, null);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f;
            getWindow().setAttributes(originalParams);
        });
        TextView title=popUpView.findViewById(R.id.title);
        title.setText("Edit the emergency message");
        EditText inputE=popUpView.findViewById(R.id.userInput);
        inputE.setText(getIntent().getStringExtra("habitEmergency"));
        Button save=popUpView.findViewById(R.id.save);
        Button cancel=popUpView.findViewById(R.id.cancel);
        cancel.setOnClickListener(v->{
            popupWindow.dismiss();
        });
        save.setOnClickListener(v->{
            db.updateEmergencyMsg(habitId,inputE.getText().toString());
            popupWindow.dismiss();
            Toast.makeText(this,"Emergency message updated",Toast.LENGTH_SHORT).show();
        });

    }

    private void popUpWindowEmergency() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.emergency_popup, null);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f;
            getWindow().setAttributes(originalParams);
        });
TextView emergencyMsg=popUpView.findViewById(R.id.emergency_msg);
        emergencyMsg.setText(getIntent().getStringExtra("habitEmergency"));
    }
    private void popUpWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.log_day, null);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f;
            getWindow().setAttributes(originalParams);
        });
        EditText mood = popUpView.findViewById(R.id.mood);
        EditText note = popUpView.findViewById(R.id.note);
        Button save = popUpView.findViewById(R.id.save);
        Button cancel = popUpView.findViewById(R.id.cancel);
        save.setOnClickListener(v -> {
            log.setVisibility(View.GONE);
            String txtMood = mood.getText().toString();
            String txtNote = note.getText().toString();
            Timestamp timestamp = Timestamp.now();
            String date = Utilities.timestampToStringNoDetail(timestamp);
            db.insertDailyNote(habitId, date, txtNote, txtMood);
            loadAdapter();
            popupWindow.dismiss();

        });
        cancel.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void loadAdapter() {
        dailyNoteModel=db.getNotesForHabit(habitId);
        adapter=new DailyNotesAdapter(this,dailyNoteModel,db);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3 , LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

    }
}