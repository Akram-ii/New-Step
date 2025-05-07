package com.example.newstep;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.example.newstep.Databases.MyHabitsDatabaseHelper;
import com.example.newstep.Util.Utilities;
import com.google.firebase.Timestamp;

import java.util.List;

public class HabitDetailsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    List<DailyNoteModel> dailyNoteModel;
    int habitId;
    MyHabitsDatabaseHelper db;
    DailyNotesAdapter adapter;
    Button log;
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
        layout=findViewById(R.id.main);
        name=findViewById(R.id.name);
        back=findViewById(R.id.back_ImageButton);
        name.setText(getIntent().getStringExtra("habitName"));
        back.setOnClickListener(v->{
            onBackPressed();
        });
        db=new MyHabitsDatabaseHelper(this);
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
    }
 
    private void popUpWindow() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HabitDetailsActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HabitDetailsActivity.this);
        View popUpView = inflater.inflate(R.layout.log_day, null);
        builder.setView(popUpView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.PopupWindowAnimation;
        }
        dialog.show();
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
            dialog.dismiss();

        });
        cancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void loadAdapter() {
        dailyNoteModel=db.getNotesForHabit(habitId);
        adapter=new DailyNotesAdapter(this,dailyNoteModel,db);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3 , LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

    }
}