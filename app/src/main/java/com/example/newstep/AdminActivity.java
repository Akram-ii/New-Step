package com.example.newstep;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.newstep.Fragments.AdminCommentsFragment;
import com.example.newstep.Fragments.AdminPostsFragment;
import com.example.newstep.Fragments.AdminRestrictedAccountsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {
    BottomNavigationView bottomView;
    TextView textView;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AdminPostsFragment())
                    .commit();
        }
        bottomView=findViewById(R.id.bottomMenu);
        textView=findViewById(R.id.title_textview);
        applyGradientToText(textView);
        bottomView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.nav_comments){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AdminCommentsFragment()).commit();
            }else if(item.getItemId()==R.id.nav_restricted){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AdminRestrictedAccountsFragment()).commit();
            }else if(item.getItemId()==R.id.nav_posts){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AdminPostsFragment()).commit();
            }
            back=findViewById(R.id.back);
            back.setOnClickListener(v->{
                onBackPressed();
            });
            return true;
        });
    }

    private void applyGradientToText(TextView textView) {
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{0xFFFFFFFF, 0xFFFF69B4, 0xFF800080},
                null, Shader.TileMode.CLAMP);

        textView.getPaint().setShader(textShader);
    }
}