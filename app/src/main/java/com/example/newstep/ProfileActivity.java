package com.example.newstep;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.newstep.Adapters.ProfileAdapter;

import com.example.newstep.Fragments.CommunityFragment;
import com.example.newstep.Fragments.EditProfile_Fragment;
import com.example.newstep.Fragments.HomeFragment;
import com.example.newstep.Fragments.MyPostsFragment;
import com.example.newstep.Fragments.SettingsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.viewpager2.widget.ViewPager2;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.content.Intent;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;



public class ProfileActivity extends AppCompatActivity implements MyPostsFragment.CommentDialogListener {


    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfileAdapter adapter;
    private TextView fullNameTextView;
    private ImageButton BackButton;
    private TextView toolusername;
    private Toolbar toolbar;
    private MaterialButton editeprofilebutton;
    private ImageView Back_image;
    private ImageView pro_image;
    private TextView biooo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {




        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.ToolBar_Profile));
        editeprofilebutton = findViewById(R.id.editProfileButton);
        toolusername = findViewById(R.id.tool_username);
        BackButton = findViewById(R.id.Back_Button);
        fullNameTextView = findViewById(R.id.fullName);
        tabLayout = findViewById(R.id.profileTabLayout);
        viewPager = findViewById(R.id.viewPager);
        Back_image = findViewById(R.id.backGround_image);
        pro_image = findViewById(R.id.profileImage);
        biooo = findViewById(R.id.bio);

        loadUserProfile();
        getusername();

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });






        editeprofilebutton.setOnClickListener(view -> {

            findViewById(R.id.viewPager).setVisibility(View.GONE);
            findViewById(R.id.editProfileButton).setVisibility(View.GONE);

            Fragment fragment = new EditProfile_Fragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);


        });



        adapter = new ProfileAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {

            if (position == 0) {
                tab.setText("My Posts");
            } else if (position == 1) {
                tab.setText("Liked Posts");
            }

        }).attach();

        getSupportActionBar().setTitle("");



    }


    @Override
    protected void onResume() {

        super.onResume();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean shouldRefresh = prefs.getBoolean("needRefresh", false);

        if (shouldRefresh) {
            loadUserProfile();
            getusername();
            prefs.edit().putBoolean("needRefresh", false).apply();
        }


    }






    @Override
    public void showCommentDialog(String postId) {

        CommunityFragment communityFragment  = (CommunityFragment) getSupportFragmentManager().findFragmentByTag("FragmentA_TAG");
        if (communityFragment != null) {
            communityFragment.showCommentDialog(postId);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);



        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            findViewById(R.id.profileTabLayout).setVisibility(View.GONE);
            findViewById(R.id.viewPager).setVisibility(View.GONE);
            findViewById(R.id.editProfileButton).setVisibility(View.GONE);
            findViewById(R.id.Back_Button).setVisibility(View.GONE);
            findViewById(R.id.tool_username).setVisibility(View.GONE);
            findViewById(R.id.ToolBar_Profile).setVisibility(View.GONE);
            findViewById(R.id.backGround_image).setVisibility(View.GONE);
            findViewById(R.id.profileImage).setVisibility(View.GONE);
            findViewById(R.id.fullName).setVisibility(View.GONE);
            findViewById(R.id.bio).setVisibility(View.GONE);



            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);


            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .commit();

            return true;
        } else if (id == R.id.action_logout) {


            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();


            if (!isFinishing() && !isDestroyed()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }



            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {

        /*مشكل في حجم الشاشة */

        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer.getVisibility() == View.VISIBLE) {


            fragmentContainer.setVisibility(View.GONE);


            findViewById(R.id.profileTabLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
            findViewById(R.id.editProfileButton).setVisibility(View.VISIBLE);
            findViewById(R.id.Back_Button).setVisibility(View.VISIBLE);
            findViewById(R.id.tool_username).setVisibility(View.VISIBLE);
            findViewById(R.id.ToolBar_Profile).setVisibility(View.VISIBLE);
            findViewById(R.id.backGround_image).setVisibility(View.VISIBLE);
            findViewById(R.id.profileImage).setVisibility(View.VISIBLE);
            findViewById(R.id.fullName).setVisibility(View.VISIBLE);
            findViewById(R.id.bio).setVisibility(View.VISIBLE);

        } else {

            super.onBackPressed();
        }
    }



    private void getusername(){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if(auth.getCurrentUser() != null){

            String userId = auth.getCurrentUser().getUid();

            firestore.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if(documentSnapshot.exists()) {

                            String fullname = documentSnapshot.getString("username");
                            fullNameTextView.setText(fullname != null ? fullname : "Name not available");
                            toolusername.setText(fullname != null ? fullname : "Name not available");

                        }
                    })
            .addOnFailureListener(e -> {
                fullNameTextView.setText("Error fetching name");
            });

        }
    }



    private void loadUserProfile() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            firestore.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // تحميل صورة البروفايل
                            if (documentSnapshot.contains("profileImage")) {
                                String profileImageUrl = documentSnapshot.getString("profileImage");
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(this).load(profileImageUrl).into(pro_image);
                                }
                            }

                            // تحميل صورة الخلفية
                            if (documentSnapshot.contains("coverImage")) {
                                String coverImageUrl = documentSnapshot.getString("coverImage");
                                if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                                    Glide.with(this).load(coverImageUrl).into(Back_image);
                                }
                            }

                            // تحميل البيو
                            if (documentSnapshot.contains("bio")) {
                                String userBio = documentSnapshot.getString("bio");
                                if (userBio != null) {
                                    biooo.setText(userBio);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Data loading failed!"));
        }
    }


    private void showToast(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }



}


