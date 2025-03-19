package com.example.newstep;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.newstep.Fragments.AdminCommentsFragment;
import com.example.newstep.Fragments.AdminPostsFragment;
import com.example.newstep.Fragments.ChatsFragment;
import com.example.newstep.Fragments.CommunityFragment;
import com.example.newstep.Fragments.HomeFragment;
import com.example.newstep.Fragments.MyHabitsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {
    BottomNavigationView bottomView;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
        bottomView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.nav_comments){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AdminCommentsFragment()).commit();
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
}