package com.example.newstep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.newstep.Fragments.AboutFragment;
import com.example.newstep.Fragments.ChatsFragment;
import com.example.newstep.Fragments.CommunityFragment;
import com.example.newstep.Fragments.HomeFragment;
import com.example.newstep.Fragments.MyHabitsFragment;
import com.example.newstep.Fragments.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView pfp;
    TextView userName;
    Toolbar toolbar;
    View headerView;
int test;
    BottomNavigationView bottomView;
    static Boolean isMediaInit=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
test=1;
        toolbar = findViewById(R.id.toolbar);
        bottomView= findViewById(R.id.bottomMenu);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.navView);
        headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.drawerUsername);
        pfp = headerView.findViewById(R.id.drawerpfp);
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.nav_home){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }else if(item.getItemId()==R.id.nav_chats){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ChatsFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_chats);
            }else if(item.getItemId()==R.id.nav_my_habits){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MyHabitsFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_my_habits);
            }else if(item.getItemId()== R.id.nav_community){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new CommunityFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_community);
            }
            return true;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }else if(item.getItemId()==R.id.nav_community){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new CommunityFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_community);
        }else if(item.getItemId()==R.id.nav_chats){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ChatsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_chats);
        }else if(item.getItemId()==R.id.nav_settings){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SettingsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_settings);
        }else if(item.getItemId()==R.id.nav_about){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AboutFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_about);
        }else if(item.getItemId()== R.id.nav_my_habits){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MyHabitsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_my_habits);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {super.onBackPressed();}
    }
}