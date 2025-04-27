package com.example.newstep;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.newstep.Util.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

        if (id == R.id.action_account) {
popupAccount();
            
            return true;
        } else if (id == R.id.action_logout) {
            AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
            builder.setTitle("Logout");
            builder.setMessage("Are you sure");
            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ProfileActivity.this,SplashActivity.class));
                    finish();
                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void popupAccount() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_account, null);

        PopupWindow popupWindow = new PopupWindow(
                popUpView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f;
            getWindow().setAttributes(originalParams);
        });
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        ImageView back = popUpView.findViewById(R.id.back);
        ImageButton editEmail=popUpView.findViewById(R.id.emailEdit),editPwd=popUpView.findViewById(R.id.passwordEdit);
        RadioGroup radioGroup=popUpView.findViewById(R.id.radioGroup);
        Button logout=popUpView.findViewById(R.id.logout);
        TextView delete=popUpView.findViewById(R.id.delete),email=popUpView.findViewById(R.id.emailTextView);
        email.setText(user.getEmail());
        FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String privacy=documentSnapshot.getString("privacy");
                if(privacy.equals("public")){
                    radioGroup.check(R.id.radioPublic);
                }else {
                    radioGroup.check(R.id.radioPrivate);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String privacySetting;

                if (checkedId == R.id.radioPublic) {
                    privacySetting = "public";
                } else if (checkedId == R.id.radioPrivate) {
                    privacySetting = "private";
                } else {
                    return;
                }
                FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId())
                        .update("privacy", privacySetting)
                        .addOnSuccessListener(aVoid ->
                                Log.d("Firestore", "Privacy updated to " + privacySetting))
                        .addOnFailureListener(e ->
                                Log.e("Firestore", "Error updating privacy", e));

            }
        });
        editPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText passwordEditText = new EditText(v.getContext());

                AlertDialog.Builder updatePasswordDialog = new AlertDialog.Builder(v.getContext());
                updatePasswordDialog.setTitle("Change password?");
                updatePasswordDialog.setMessage("Enter your new password");
                updatePasswordDialog.setView(passwordEditText);
                updatePasswordDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPassword = passwordEditText.getText().toString();
                        if (newPassword.length() < 6) {
                            Toast.makeText(ProfileActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                        } else {
                            user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileActivity.this, "Password successfully updated", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });
                updatePasswordDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = updatePasswordDialog.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailEditText = new EditText(v.getContext());

                AlertDialog.Builder updateEmailDialog = new AlertDialog.Builder(v.getContext());
                updateEmailDialog.setTitle("Change E-mail?");
                updateEmailDialog.setMessage("Enter your new E-mail");
                updateEmailDialog.setView(emailEditText);
                updateEmailDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newEmail = emailEditText.getText().toString();
                        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                            Toast.makeText(ProfileActivity.this, "Not a valid email", Toast.LENGTH_SHORT).show();
                        } else if (newEmail == user.getEmail()) {
                            Toast.makeText(ProfileActivity.this, "Already using " + newEmail, Toast.LENGTH_SHORT).show();
                        } else {
                            user.verifyBeforeUpdateEmail(newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileActivity.this, "Verification email sent to " + newEmail, Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Error sending verification email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });
                updateEmailDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = updateEmailDialog.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                emailEditText.setText(user.getEmail());
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure");
                builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(ProfileActivity.this,SplashActivity.class));
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Are you sure ?");
                builder.setMessage("Deleting your account will result in completely removing your data from the app");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).delete();
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ProfileActivity.this,"Account deleted",Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(ProfileActivity.this,SplashActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });

        back.setOnClickListener(v -> popupWindow.dismiss());

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


