package com.example.newstep.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstep.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {
    TextInputEditText password,email;
    TextInputLayout i;
    Button login;
    TextView forgot,register;
    FirebaseAuth auth= FirebaseAuth.getInstance();
    ProgressDialog p;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        p=new ProgressDialog(getContext());
        i=rootView.findViewById(R.id.ptexti);
        login=rootView.findViewById(R.id.loginBTN);
        email=rootView.findViewById(R.id.email);
        password=rootView.findViewById(R.id.password);
        forgot=rootView.findViewById(R.id.forgotPassword);
        register=rootView.findViewById(R.id.registerTextView);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                Fragment existingFragment = fragmentManager.findFragmentByTag(RegisterFragment.class.getSimpleName());
                if (existingFragment != null) {
                    fragmentManager.beginTransaction().remove(existingFragment).commit();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                RegisterFragment RegisterFragment = new RegisterFragment();
                transaction.replace(R.id.fragment_container, RegisterFragment, RegisterFragment.class.getSimpleName());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail=new EditText(v.getContext());

                AlertDialog.Builder passwordResetDialog=new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset password?");
                passwordResetDialog.setMessage("Enter your email to receive a reset link");
                passwordResetDialog.setView(resetMail);
                passwordResetDialog.setPositiveButton("send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail=resetMail.getText().toString();
                        if(mail.isEmpty()){
                            Toast.makeText(getContext(),"You have to enter your email",Toast.LENGTH_SHORT).show();
                        }else {
                            auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Reset link sent to "+mail, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "link not sent because "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });}
                    }
                });
                passwordResetDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = passwordResetDialog.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtEmail=email.getText().toString();
                String txtPassword=password.getText().toString();
                if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Empty credentials", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()){
                    email.setError("Not a valid E-mail");
                } else if(txtPassword.length()<6){
                    Toast.makeText(getContext(), "Password too short", Toast.LENGTH_SHORT).show();
                }else {
                    loginUser(txtEmail,txtPassword);
                }
            }
        });
    return rootView;
    }
    private void loginUser(String email, String password) {
        p.setMessage("Please wait");
        p.show();
        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                p.dismiss();
                FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
                if(user.isEmailVerified()){
                    Toast.makeText(getContext(),"Welcome!",Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    Fragment existingFragment = fragmentManager.findFragmentByTag(HomeFragment.class.getSimpleName());
                    if (existingFragment != null) {
                        fragmentManager.beginTransaction().remove(existingFragment).commit();
                    }

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    HomeFragment homeFragment = new HomeFragment();
                    transaction.replace(R.id.fragment_container, homeFragment, HomeFragment.class.getSimpleName());
                    transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();    
                }else{
                    showAlertDialog();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.dismiss();
                Toast.makeText(getContext(),"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Email not verified yet");
        builder.setMessage("Please verify your email.you can not login without email verification ");

        builder.setPositiveButton("Verify now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Resend email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseUser u=FirebaseAuth.getInstance().getCurrentUser();
                dialog.dismiss();
                u.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(),"Verification Email sent to "+email.getText().toString(),Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNeutralButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
    }

  
}