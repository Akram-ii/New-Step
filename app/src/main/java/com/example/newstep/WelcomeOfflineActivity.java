package com.example.newstep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WelcomeOfflineActivity extends AppCompatActivity {
    TextView skip;
    ImageButton next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_offline);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        skip=findViewById(R.id.skipText);
        next=findViewById(R.id.nextButton);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(WelcomeOfflineActivity.this)
                        .setTitle("Skip the intro? ")
                        .setMessage("Are you sure you want to skip the initial setup ?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("firstTime",false).apply();
                            startActivity(new Intent(WelcomeOfflineActivity.this, MainActivity.class));
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("firstTime",false).apply();
                startActivity(new Intent(WelcomeOfflineActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}