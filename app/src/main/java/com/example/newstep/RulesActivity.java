package com.example.newstep;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.newstep.Fragments.LoginFragment;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RulesActivity extends AppCompatActivity {
CheckBox agree;
Button next;
ImageView back;
ProgressDialog p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rules);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        p=new ProgressDialog(RulesActivity.this);
        back=findViewById(R.id.back_imageView);
        agree=findViewById(R.id.checkboxAgree);
        next=findViewById(R.id.continueBtn);
        back.setOnClickListener(v->{onBackPressed();});
        agree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                next.setVisibility(Button.VISIBLE);
            } else {
                next.setVisibility(Button.GONE);}
            });
        next.setOnClickListener(v->{createAccount();});
    }

    private void createAccount() {
       p.setMessage("Please wait");
       p.show();

        String email = getIntent().getStringExtra("email");
        String pwd = getIntent().getStringExtra("pwd");
        String username = getIntent().getStringExtra("username");
        String token1 = getIntent().getStringExtra("token");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", username);
                userInfo.put("id", Objects.requireNonNull(auth.getCurrentUser()).getUid());
                String currentDate = Utilities.timestampToStringNoDetail(Timestamp.now());
                userInfo.put("registerDate",currentDate);
                userInfo.put("isBanned",false);
                userInfo.put("nb_reports",0);
                userInfo.put("isBanned",false);
                userInfo.put("whenBannedComments",Timestamp.now());
                userInfo.put("whenBannedPosts",Timestamp.now());
                userInfo.put("points",0);
                userInfo.put("isBannedComments",false);
                userInfo.put("isBannedPosts",false);
                userInfo.put("isRestricted",false);
                userInfo.put("isAdmin",true);
                userInfo.put("token",token1);

                FirebaseUtil.allUserCollectionRef().document(auth.getCurrentUser().getUid()).set(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        p.dismiss();
                        if (task.isSuccessful()) {
                            auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(RulesActivity.this, "A confirmation e-mail has been sent to "+email, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RulesActivity.this,MainActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure:email not sent "+e.getMessage());
                                }
                            });
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.dismiss();
                Toast.makeText(RulesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
        
    }