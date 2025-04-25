package com.example.newstep.Fragments;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.newstep.Models.HabitModel;
import com.example.newstep.R;
import com.example.newstep.Databases.MyHabitsDatabaseHelper;
import com.example.newstep.Util.NotifReceiver;
import com.example.newstep.Util.Utilities;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddHabitFragment extends Fragment {
    EditText name,emergency;
    Button addBtn;
    TextView lastTime,cat,reminderTime;
    String catIn="";
    MyHabitsDatabaseHelper db;
    SQLiteDatabase notifDB;
    String ti="Stay on track !";
    String desc=" : Don't forget to log your day";
    int hour,minute=-1;
    DatePickerDialog datePickerDialog;
    Timestamp t;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_habit, container, false);
initDatePicker();
        notifDB = requireContext().openOrCreateDatabase("reminderDB", Context.MODE_PRIVATE, null);
        notifDB.execSQL("CREATE TABLE IF NOT EXISTS Reminder(Time TEXT, Title TEXT, Description TEXT);");
        db=new MyHabitsDatabaseHelper(getContext());
        addBtn=rootView.findViewById(R.id.addHabitBtn);
        reminderTime=rootView.findViewById(R.id.reminderTime_TextView);
        name=rootView.findViewById(R.id.name_EditText);
        lastTime=rootView.findViewById(R.id.lastTime_TextView);
        emergency=rootView.findViewById(R.id.emergency_EditText);
        cat=rootView.findViewById(R.id.cat_EditText);
        reminderTime.setOnClickListener(v->{popTimePicker(reminderTime);});
cat.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        PopupMenu popupMenu=new PopupMenu(getContext(),cat);
        popupMenu.getMenuInflater().inflate(R.menu.cat_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                catIn = String.valueOf(menuItem.getTitle());
                cat.setText(menuItem.getTitle());
                return true;
            }
        });

        popupMenu.show();
    }
});
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtName=name.getText().toString();
                String txtEmergency=emergency.getText().toString();
                String txtCat=cat.getText().toString();

                if(txtName.equals("")){
                    name.setError("Missing name");
                }else if(txtCat.equals("")){
                    cat.setError("Missing input");
                }else if(lastTime.getText().toString().equals("When was the last time ?")){
                    lastTime.setError("Missing input");
                } else {
                    if(minute!=-1){

                        notifDB.execSQL("DELETE FROM Reminder;");
                        String query = "INSERT INTO Reminder (Time, Title, Description) VALUES (?, ?, ?)";
                        notifDB.execSQL(query, new Object[]{hour + ":" + minute, ti, txtName + desc});
                        scheduleNotification(hour, minute);
                    }
                    HabitModel habitModel = new HabitModel(txtName, txtCat, txtEmergency, 0);
                    db.addHabit(habitModel);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    Fragment existingFragment = fragmentManager.findFragmentByTag(MyHabitsFragment.class.getSimpleName());
                    if (existingFragment != null) {
                        fragmentManager.beginTransaction().remove(existingFragment).commit();
                    }

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    MyHabitsFragment myHabitsFragment = new MyHabitsFragment();
                    transaction.replace(R.id.fragment_container, myHabitsFragment, MyHabitsFragment.class.getSimpleName());
                    transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();
                }}
        });
        lastTime.setOnClickListener(v->{
            openDatePicker(lastTime);
        });
    return  rootView;
    }

    private void scheduleNotification(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), NotifReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }


    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                String date = Utilities.getMonthFormat(month) + " " + dayOfMonth + " " + year;
                t=createTimestamp(year,month,dayOfMonth);
                lastTime.setText(date);
            }
        };
        Calendar cal=Calendar.getInstance();
        final int y = cal.get(Calendar.YEAR);
        final int m = cal.get(Calendar.MONTH);
        final int d = cal.get(Calendar.DAY_OF_MONTH);
        int style= AlertDialog.THEME_HOLO_DARK;
        datePickerDialog = new DatePickerDialog(getContext(),style, dateSetListener, y, m, d);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }
    public void openDatePicker(View view){
        datePickerDialog.show();
    }
    public void popTimePicker(View view){
        TimePickerDialog.OnTimeSetListener onTimeSetListener=new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int h, int m) {
                hour=h;
                minute=m;
                reminderTime.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
                Log.d( "texttime h m: ",""+hour+" hi "+minute);

            }
        };
        TimePickerDialog timePickerDialog=new TimePickerDialog(getContext(),onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle("Choose a time for your daily reminder");
        timePickerDialog.show();
    }
    public Timestamp createTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0); // month - 1 because Calendar months are 0-based
        Date date = calendar.getTime();
        return new Timestamp(date);
    }



}