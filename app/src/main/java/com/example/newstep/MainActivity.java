package com.example.newstep;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.newstep.CustomViews.CurvedBottomNavigationView;
import com.example.newstep.Fragments.AboutFragment;
import com.example.newstep.Fragments.BadgesFragment;
import com.example.newstep.Fragments.ChatsFragment;
import com.example.newstep.Fragments.GoalsFragment;
import com.example.newstep.Fragments.PrivateChatsFragment;
import com.example.newstep.Fragments.CommunityFragment;
import com.example.newstep.Fragments.HomeFragment;
import com.example.newstep.Fragments.LoginFragment;
import com.example.newstep.Fragments.MyHabitsFragment;
import com.example.newstep.Fragments.SettingsFragment;
import com.example.newstep.Util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView pfp;
    ImageButton admin;
    TextView userName;
    TextView userEmail;
    int index;
    Toolbar toolbar;
    String lastFragment;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;
    View headerView;
    int test;
    FirebaseAuth firebaseAuth;
    CurvedBottomNavigationView bottomView;

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
        lastFragment = sharedPreferences.getString("lastFragment", "default_value");
        toolbar = findViewById(R.id.toolbar);
        bottomView= findViewById(R.id.bottomMenu);
        setSupportActionBar(toolbar);
        bottomView.setSelectedPosition(R.id.nav_home);
        navigationView=findViewById(R.id.navView);
        headerView = navigationView.getHeaderView(0);
        admin=headerView.findViewById(R.id.admin);
        userName = headerView.findViewById(R.id.drawerUsername);
        pfp = headerView.findViewById(R.id.drawerpfp);
        userEmail = headerView.findViewById(R.id.drawerEmail);
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
            }
        };

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
               if(documentSnapshot.exists()){
                   SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                   prefs.edit().putString("currUserName", documentSnapshot.getString("username")).apply();
                   if(Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))){
                       admin.setVisibility(View.VISIBLE);
                   }
               }
                    if(documentSnapshot.exists()){
                        if(Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))){
                            admin.setVisibility(View.VISIBLE);
                        }
                        if(Boolean.TRUE.equals(documentSnapshot.getBoolean("isBanned"))){
                            FirebaseAuth.getInstance().signOut();
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


        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });
        firebaseAuth.addAuthStateListener(authStateListener);

        bottomView.setOnItemSelectedListener(item -> {
                 index = -1;

                if (item.getItemId() == R.id.nav_home) {
                    index = 0;
                    navigationView.setCheckedItem(R.id.nav_home);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    navigationView.setCheckedItem(R.id.nav_home);
                }else if (item.getItemId() == R.id.nav_my_habits) {
                    index = 1;
                    navigationView.setCheckedItem(R.id.nav_my_habits);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MyHabitsFragment())
                            .commit();
                    navigationView.setCheckedItem(R.id.nav_my_habits);
                }else if (item.getItemId() == R.id.nav_community) {
                    index = 2;
                    checkUserAuthentication(new CommunityFragment());
                    navigationView.setCheckedItem(R.id.nav_community);

                } else if (item.getItemId() == R.id.nav_chats) {
                    index = 3;
                    checkUserAuthentication(new ChatsFragment());
                    navigationView.setCheckedItem(R.id.nav_chats);
                }


                if (index != -1) {
                    bottomView.setSelectedPosition(index);



                    View iconView = bottomView.findViewById(item.getItemId());
                    if (iconView != null) {
                        Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale_anim);
                        iconView.startAnimation(scale);
                    }
                }


                return true;


            });
        if(lastFragment.equals("ChatsFragment")){
            checkUserAuthentication(new ChatsFragment());
            navigationView.setCheckedItem(R.id.nav_chats);
        }else{
            navigationView.setCheckedItem(R.id.nav_home);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        }

        loadUserProfile();

        View headerView = navigationView.getHeaderView(0);
        RelativeLayout profileButton = headerView.findViewById(R.id.Profile_button);

        profileButton.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser()!=null){

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

            }else{
                Toast.makeText(MainActivity.this,"You need to log in first",Toast.LENGTH_SHORT).show();
                LoginFragment loginFragment = new LoginFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, loginFragment).addToBackStack(null).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
            }

        });

    }




    private boolean userIsLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if( currentUser != null)
        {
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
    public void checkUserAuthentication(Fragment fragment) {
        String fragmentTag = fragment.getClass().getSimpleName();
        if (firebaseAuth.getCurrentUser() != null ) {

            if(fragmentTag.equals("CommunityFragment")){
                navigationView.setCheckedItem(R.id.nav_community);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);}
            else{
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                drawerLayout.closeDrawer(GravityCompat.START);}


        } else {

            if(fragmentTag.equals("CommunityFragment")){
                navigationView.setCheckedItem(R.id.nav_community);

            }
            else{
                navigationView.setCheckedItem(R.id.nav_chats);
            }
            Toast.makeText(MainActivity.this,"You need to log in first",Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
public void badgesSelected(){
    bottomView.setSelectedPosition(R.id.nav_my_badge);
    navigationView.setCheckedItem(R.id.nav_my_badge);
}
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_home){
            bottomView.setSelectedPosition(R.id.nav_home);
            navigationView.setCheckedItem(R.id.nav_home);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        }else if(item.getItemId()==R.id.nav_community){
            checkUserAuthentication(new CommunityFragment());
            navigationView.setCheckedItem(R.id.nav_community);
        }else if(item.getItemId()==R.id.nav_chats){
            checkUserAuthentication(new ChatsFragment());
        }else if(item.getItemId()==R.id.nav_account){
            if(FirebaseAuth.getInstance().getCurrentUser()==null){
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new LoginFragment()).commit();
            }else{
                drawerLayout.closeDrawer(GravityCompat.START);
popupAccount();
            }
        }else if(item.getItemId()== R.id.nav_my_habits){
            bottomView.setSelectedPosition(R.id.nav_my_habits);
            navigationView.setCheckedItem(R.id.nav_my_habits);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new MyHabitsFragment()).commit();
        }else if(item.getItemId()== R.id.nav_my_goals){
            navigationView.setCheckedItem(R.id.nav_my_goals);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new GoalsFragment()).commit();
        }else if(item.getItemId()== R.id.nav_profile){
            if(FirebaseAuth.getInstance().getCurrentUser()==null){
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new LoginFragment()).commit();
            }else{
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));
            }
        }
        else if(item.getItemId()== R.id.nav_my_badge){
            checkUserAuthentication(new BadgesFragment());
        }else if(item.getItemId()== R.id.nav_contact){
            if(FirebaseAuth.getInstance().getCurrentUser()==null){
                Toast.makeText(MainActivity.this,"You need to log in first",Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else{
                popupContact();
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        layoutParams.alpha = 0.5f;
        getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f;
            getWindow().setAttributes(originalParams);
        });

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
                            Toast.makeText(MainActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                        } else {
                            user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Password successfully updated", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainActivity.this, "Not a valid email", Toast.LENGTH_SHORT).show();
                        } else if (newEmail == user.getEmail()) {
                            Toast.makeText(MainActivity.this, "Already using " + newEmail, Toast.LENGTH_SHORT).show();
                        } else {
                            user.verifyBeforeUpdateEmail(newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Verification email sent to " + newEmail, Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(MainActivity.this, SplashActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error sending verification email: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure");
                builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this,SplashActivity.class));
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
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure ?");
                builder.setMessage("Deleting your account will result in completely removing your data from the app");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).delete();
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MainActivity.this,"Account deleted",Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(MainActivity.this,SplashActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();

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
    private void popupContact() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.contact_us_popup, null);

        PopupWindow popupWindow = new PopupWindow(
                popUpView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.5f; // Reduce brightness
        getWindow().setAttributes(layoutParams);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f; // Restore full brightness
            getWindow().setAttributes(originalParams);
        });
        ImageView back = popUpView.findViewById(R.id.back);
        EditText title=popUpView.findViewById(R.id.title),desc=popUpView.findViewById(R.id.desc);
        Spinner spinner=popUpView.findViewById(R.id.spinner);
        Button send=popUpView.findViewById(R.id.send);

        List<String> categories = new ArrayList<>();
        categories.add("Technical Issue");
        categories.add("Account Help");
        categories.add("Feedback or Suggestions");
        categories.add("Report a Problem");
        categories.add("Feature Request");
        categories.add("Other");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,categories);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleText=title.getText().toString();
                String descText=desc.getText().toString();
                if(titleText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                }else if(descText.isEmpty()){
                    Toast.makeText(MainActivity.this, "You have to give more details", Toast.LENGTH_SHORT).show();
                }else{
                    String cat=spinner.getSelectedItem().toString();
                    Map<String,Object> element=new HashMap<>();
                    element.put("Rtitle",titleText);
                    element.put("Rtime", Timestamp.now());
                    element.put("Rcat",cat);
                    element.put("Rusername",FirebaseUtil.getCurrentUsername(MainActivity.this));
                    element.put("Rdesc",descText);
                    element.put("RuserId",FirebaseUtil.getCurrentUserId());

                    FirebaseFirestore.getInstance().collection("contact").add(element).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String generatedDocumentId = documentReference.getId();
                            element.put("id", generatedDocumentId);
                            documentReference.set(element, SetOptions.merge());
                            Toast.makeText(MainActivity.this, "We’ve got your message. Thanks for reaching out!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Couldn't send the message: "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        back.setOnClickListener(v -> popupWindow.dismiss());
    }

    public void popupBan() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.pop_up_banned, null);

        // Create a PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popUpView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );

        // Set transparent background
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);

        // Dim the background
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.5f; // Reduce brightness
        getWindow().setAttributes(layoutParams);

        // Show popup
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Restore brightness when popup is dismissed
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams originalParams = getWindow().getAttributes();
            originalParams.alpha = 1.0f; // Restore full brightness
            getWindow().setAttributes(originalParams);
        });

        // Close button
        ImageView closePopup = popUpView.findViewById(R.id.back_imageView);
        closePopup.setOnClickListener(v -> popupWindow.dismiss());
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {super.onBackPressed();}
    }
    public void loadUserProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


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
                                .placeholder(R.drawable.pfp_light_blue)
                                .error(R.drawable.pfp_light_blue)
                                .into(pfp);
                    } else {
                        Log.e("Firestore", "Document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch user data", e);
                });


        if (user != null) {
            String email = user.getEmail();
            userEmail.setText(email.substring(0, email.indexOf("@")));
        }


    }

}