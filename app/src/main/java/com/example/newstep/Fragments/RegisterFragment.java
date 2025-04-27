package com.example.newstep.Fragments;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstep.R;
import com.example.newstep.RulesActivity;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegisterFragment extends Fragment {

    TextInputEditText Email,Password,confirm,userName;
    TextView login;
    Button register;
    FirebaseAuth auth=FirebaseAuth.getInstance();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    ProgressDialog p;
    String token;
    String  privacySetting="public";
RadioGroup radioGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        RadioGroup radioGroup=rootView.findViewById(R.id.radioGroup);
        p=new ProgressDialog(getContext());
        userName=rootView.findViewById(R.id.user);
        register=rootView.findViewById(R.id.registerBTN);
        Email=rootView.findViewById(R.id.email);
        Password=rootView.findViewById(R.id.password);
        confirm=rootView.findViewById(R.id.confirm);
        login=rootView.findViewById(R.id.loginTextView);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(LoginFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                LoginFragment login= new LoginFragment();
                transaction.replace(R.id.fragment_container, login, LoginFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.radioPublic) {
                    privacySetting = "public";
                } else if (checkedId == R.id.radioPrivate) {
                    privacySetting = "private";
                } else {
                    return;
                }}
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.setMessage("Please wait");
                p.show();
                String txtEmail = Email.getText().toString();
                String txtPassword = Password.getText().toString();
                String txtConfirm = confirm.getText().toString();
                String txtUserName = userName.getText().toString();
                if (txtUserName.length()<3 ) {
                    p.dismiss();
                    userName.setError("Too short");
                    p.dismiss();
                } else if (txtUserName.length()>15 ) {
                    p.dismiss();
                    userName.setError("Too long");
                }else if (txtEmail.isEmpty() ) {
                    p.dismiss();
                    Email.setError("Enter your E-mail");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()){
                    p.dismiss();
                    Email.setError("Not a valid E-mail");
                }else if(txtPassword.isEmpty()){
                    p.dismiss();
                    Toast.makeText(getContext(), "Enter your password", Toast.LENGTH_SHORT).show();
                } else if(txtPassword.length()<6){
                    p.dismiss();
                    Toast.makeText(getContext(), "Password too short", Toast.LENGTH_SHORT).show();
                }else if (!txtPassword.equals(txtConfirm)){
                    p.dismiss();
                    Toast.makeText(getContext(), "Password does not match", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if(task.isSuccessful()){
                                token=task.getResult();
                                Log.d("token user here ","   :::"+task.getResult());
                                checkUsernameExists(txtEmail,txtPassword,txtUserName, task.getResult());
                            }
                        }
                    });
                }
            }
        });
    return rootView;
    }

    private void checkUsernameExists(String email,String pwd,String username,String token) {
     FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("username", username.trim())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreDebug", "Documents found: " + querySnapshot.size());
                    if (!querySnapshot.isEmpty()) {
                        userName.setError("Username is already taken");
                    } else {
                        Intent intent=new Intent(getContext(), RulesActivity.class);
                        intent.putExtra("email",email);
                        intent.putExtra("pwd",pwd);
                        intent.putExtra("username",username);
                        intent.putExtra("token",token);
                        intent.putExtra("privacy",privacySetting);
                        p.dismiss();
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking username", Toast.LENGTH_SHORT).show();
                });
    }

    private void registerUser(String email,String password,String username,String token1) {
        p.setMessage("Please wait");
        p.show();
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
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

                userInfo.put("isBannedComments",false);
                userInfo.put("isBannedPosts",false);
                userInfo.put("isRestricted",false);
                userInfo.put("isAdmin",true);
                userInfo.put("token",token1);

                db.collection("Users").document(auth.getCurrentUser().getUid()).set(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        p.dismiss();
                        if (task.isSuccessful()) {
                            auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "A confirmation e-mail has been sent to "+email, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure:email not sent "+e.getMessage());
                                }
                            });

                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                            Fragment existingFragment = fragmentManager.findFragmentByTag(LoginFragment.class.getSimpleName());
                            if (existingFragment != null) {
                                fragmentManager.beginTransaction().remove(existingFragment).commit();
                            }

                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            LoginFragment login= new LoginFragment();
                            transaction.replace(R.id.fragment_container, login, LoginFragment.class.getSimpleName());
                            transaction.addToBackStack(null);
                            transaction.commitAllowingStateLoss();

                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}