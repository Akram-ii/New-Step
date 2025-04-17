package com.example.newstep;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

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
import androidx.fragment.app.Fragment;

import com.example.newstep.Fragments.AboutFragment;
import com.example.newstep.Fragments.ChatsFragment;
import com.example.newstep.Fragments.CommunityFragment;
import com.example.newstep.Fragments.HomeFragment;
import com.example.newstep.Fragments.LoginFragment;
import com.example.newstep.Fragments.MyHabitsFragment;
import com.example.newstep.Fragments.SettingsFragment;
import com.example.newstep.Util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.OnSuccessListener;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView pfp;
    TextView userName;
    Toolbar toolbar;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;
    View headerView;
    ImageButton admin;

int test;
FirebaseAuth firebaseAuth;
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

        firebaseAuth=FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String lastFragment = sharedPreferences.getString("lastFragment", "default_value");

        toolbar = findViewById(R.id.toolbar);
        bottomView= findViewById(R.id.bottomMenu);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.navView);
        headerView = navigationView.getHeaderView(0);
        admin=headerView.findViewById(R.id.admin);
        userName = headerView.findViewById(R.id.drawerUsername);
        pfp = headerView.findViewById(R.id.drawerpfp);
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Log.d("Auth Check", "User not logged in - Redirecting to LoginFragment");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
            }
        };


        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        if(Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))){
                            admin.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });

        firebaseAuth.addAuthStateListener(authStateListener);
        bottomView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.nav_home){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }else if(item.getItemId()==R.id.nav_chats){
               checkUserAuthentication(new ChatsFragment());
                navigationView.setCheckedItem(R.id.nav_chats);
            }else if(item.getItemId()==R.id.nav_my_habits){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MyHabitsFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_my_habits);
            }else if(item.getItemId()== R.id.nav_community){
               checkUserAuthentication(new CommunityFragment());
                navigationView.setCheckedItem(R.id.nav_community);
            }
            return true;
        });

        if(lastFragment.equals("ChatsFragment")){
            checkUserAuthentication(new ChatsFragment());
            navigationView.setCheckedItem(R.id.nav_chats);
        }

        loadUserProfile();

        View headerView = navigationView.getHeaderView(0);
        RelativeLayout profileButton = headerView.findViewById(R.id.Profile_button);

        profileButton.setOnClickListener(v -> {
            if(userIsLoggedIn()){

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

            }else{

                LoginFragment loginFragment = new LoginFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, loginFragment).addToBackStack(null).commit();

            }

        });



    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }



    private boolean userIsLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null;
    }


    private void checkUserAuthentication(Fragment fragment) {
        if (firebaseAuth.getCurrentUser() != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }else if(item.getItemId()==R.id.nav_community){
            checkUserAuthentication(new CommunityFragment());
navigationView.setCheckedItem(R.id.nav_community);
        }else if(item.getItemId()==R.id.nav_chats){
            checkUserAuthentication(new ChatsFragment());
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
    } // idafat profile fragmant min agal wad3iha fi hadihi al dala li ana al fragment la touda3 mitl activity

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {super.onBackPressed();}
    }







    public void loadUserProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();


        if (auth.getCurrentUser() == null) {
            Log.e("Firestore", "User is not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();


        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String userName1 = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImage");

                        if (userName1 == null || userName1.trim().isEmpty()) {
                            userName1 = "Unknown user";
                        }
                        userName.setText(userName1);


                        Glide.with(pfp.getContext())
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.pfp_purple)
                                .error(R.drawable.pfp_purple)
                                .into(pfp);
                    } else {
                        Log.e("Firestore", "Document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch user data", e);
                });
    }



}