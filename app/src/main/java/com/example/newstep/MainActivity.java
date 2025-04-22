package com.example.newstep;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
    Toolbar toolbar;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;
    View headerView;
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
        if(FirebaseUtil.getCurrentUserId()!= null){// zidto bah maysrach null pointer exception ki maykonch user mdayer login


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newToken = task.getResult();
                        FirebaseUtil.allUserCollectionRef()
                                .document(FirebaseUtil.getCurrentUserId())
                                .update("token", newToken);
                    }
                });}else{
            Toast.makeText(this, "user not found !", Toast.LENGTH_SHORT).show();
        }
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                Log.d( "token curr ",""+task.getResult());
            }
        });


firebaseAuth=FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String lastFragment = sharedPreferences.getString("lastFragment", "default_value");
        toolbar = findViewById(R.id.toolbar);
        bottomView= findViewById(R.id.bottomMenu);
        setSupportActionBar(toolbar);
        navigationView=findViewById(R.id.navView);
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
                   SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                   prefs.edit().putString("currUserName", documentSnapshot.getString("username")).apply();
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
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new HomeFragment()).commit();
            }else if(item.getItemId()==R.id.nav_chats){
               checkUserAuthentication(new ChatsFragment());
            }else if(item.getItemId()==R.id.nav_my_habits){
                getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new MyHabitsFragment()).commit();
            }else if(item.getItemId()== R.id.nav_community){
               checkUserAuthentication(new CommunityFragment());
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
            if(userIsLoggedIn()){

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

            }else{

                LoginFragment loginFragment = new LoginFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, loginFragment).addToBackStack(null).commit();

            }

        });

    }




    private boolean userIsLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
    private void checkUserAuthentication(Fragment fragment) {
        if (firebaseAuth.getCurrentUser() != null ) {
            String fragmentTag = fragment.getClass().getSimpleName();
            if(fragmentTag.equals("CommunityFragment")){
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container, fragment).commit();
            drawerLayout.closeDrawer(GravityCompat.START);}
            else{
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                drawerLayout.closeDrawer(GravityCompat.START);}


        } else {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container, new LoginFragment()).commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        }else if(item.getItemId()==R.id.nav_community){
            checkUserAuthentication(new CommunityFragment());
        }else if(item.getItemId()==R.id.nav_chats){
            checkUserAuthentication(new ChatsFragment());
        }else if(item.getItemId()==R.id.nav_settings){
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new SettingsFragment()).commit();
        }else if(item.getItemId()== R.id.nav_my_habits){
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new MyHabitsFragment()).commit();
        }else if(item.getItemId()== R.id.nav_my_goals){
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new GoalsFragment()).commit();
        }else if(item.getItemId()== R.id.nav_profile){
            startActivity(new Intent(MainActivity.this,ProfileActivity.class));
        }
        else if(item.getItemId()== R.id.nav_my_badge){
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_container,new BadgesFragment()).commit();
        }else if(item.getItemId()== R.id.nav_contact){
        popupContact();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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