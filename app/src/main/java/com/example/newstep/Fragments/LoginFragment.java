package com.example.newstep.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstep.MainActivity;
import com.example.newstep.R;
import com.example.newstep.Util.Utilities;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.example.newstep.Util.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.example.newstep.Util.FirebaseUtil;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import com.facebook.AccessToken;


public class LoginFragment extends Fragment {
    TextInputEditText password,email;
    TextInputLayout i;
    Button login;
    TextView forgot,register;
    FirebaseAuth auth= FirebaseAuth.getInstance();
    ProgressDialog p;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static final int GOOGLE_SIGN_IN_REQUEST_CODE = 100;
    private String token1;
    private CallbackManager callbackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        FacebookSdk.sdkInitialize(requireContext());


        p=new ProgressDialog(getContext());
        i=rootView.findViewById(R.id.ptexti);
        login=rootView.findViewById(R.id.loginBTN);
        email=rootView.findViewById(R.id.email);
        password=rootView.findViewById(R.id.password);
        forgot=rootView.findViewById(R.id.forgotPassword);
        register=rootView.findViewById(R.id.registerTextView);
        callbackManager = CallbackManager.Factory.create();

        SignInButton googleSignInButton = rootView.findViewById(R.id.btnGoogleSignIn);
        LoginButton facebookLoginButton = rootView.findViewById(R.id.btnFacebookLogin);
        facebookLoginButton.setPermissions("email", "public_profile");

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), googleSignInOptions);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token1 = task.getResult();
                    }
                });


        googleSignInButton.setOnClickListener(v -> signInWithGoogle());




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


        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getContext(), "Facebook login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), "Facebook login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        try {
            PackageInfo info = requireActivity()
                    .getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);

            Signature[] signatures;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                signatures = info.signingInfo.getApkContentsSigners();
            } else {
                signatures = info.signatures;
            }

            for (Signature signature : signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("KeyHash", "KeyHash: " + keyHash);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void loginUser(String email, String password) {
        p.setMessage("Please wait");
        p.show();
        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                p.dismiss();
                FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
                FirebaseUtil.allUserCollectionRef().document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(Boolean.TRUE.equals(documentSnapshot.getBoolean("isBanned"))){
                            FirebaseAuth.getInstance().signOut();
                            ((MainActivity) requireContext()).popupBan();
                        }else{
                if(user.isEmailVerified()){
                    Toast.makeText(getContext(),"Welcome!",Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("updateDrawer", true);
                    startActivity(intent);
                    getActivity().finish();




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
                    }
                });

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



    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }



    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken(), token1);
                        }
                    } catch (ApiException e) {
                        Toast.makeText(getContext(), "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });




    private void firebaseAuthWithGoogle(String idToken, String token1) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if (user != null) {
                            String userId = user.getUid();
                            String defaultUsername = user.getDisplayName() != null ? user.getDisplayName() : "Unknown";
                            String registerDate = Utilities.timestampToStringNoDetail(Timestamp.now());

                            DocumentReference userRef = db.collection("Users").document(userId);

                            userRef.get().addOnSuccessListener(documentSnapshot -> {
                                String profileImageUrl;
                                String username;

                                if (documentSnapshot.exists()) {


                                    String storedProfileImage = documentSnapshot.getString("profileImage");
                                    profileImageUrl = (storedProfileImage != null && !storedProfileImage.isEmpty())
                                            ? storedProfileImage
                                            : (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");


                                    String storedUsername = documentSnapshot.getString("username");
                                    username = (storedUsername != null && !storedUsername.isEmpty())
                                            ? storedUsername
                                            : defaultUsername;

                                } else {

                                    profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                                    username = defaultUsername;
                                }


                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("id", userId);
                                userData.put("registerDate", registerDate);
                                userData.put("token", token1);
                                userData.put("profileImage", profileImageUrl);
                                userData.put("isBanned",false);
                                userData.put("nb_reports",0);
                                userData.put("isBanned",false);
                                userData.put("whenBannedComments",Timestamp.now());
                                userData.put("whenBannedPosts",Timestamp.now());
                                userData.put("points",0);
                                userData.put("privacy","public");
                                userData.put("isBannedComments",false);
                                userData.put("isBannedPosts",false);
                                userData.put("isRestricted",false);
                                userData.put("isAdmin",false);


                                userRef.set(userData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getContext(), MainActivity.class));
                                            requireActivity().finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to fetch existing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void firebaseAuthWithFacebook(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            String defaultUsername = user.getDisplayName() != null ? user.getDisplayName() : "Unknown";
                            String registerDate = Utilities.timestampToStringNoDetail(Timestamp.now());

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference userRef = db.collection("Users").document(userId);

                            userRef.get().addOnSuccessListener(documentSnapshot -> {
                                String profileImageUrl;
                                String username;

                                if (documentSnapshot.exists()) {
                                    String storedProfileImage = documentSnapshot.getString("profileImage");
                                    profileImageUrl = (storedProfileImage != null && !storedProfileImage.isEmpty())
                                            ? storedProfileImage
                                            : (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

                                    String storedUsername = documentSnapshot.getString("username");
                                    username = (storedUsername != null && !storedUsername.isEmpty())
                                            ? storedUsername
                                            : defaultUsername;
                                } else {
                                    profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                                    username = defaultUsername;
                                }


                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("id", userId);
                                userData.put("registerDate", registerDate);
                                userData.put("profileImage", profileImageUrl);
                                userData.put("isBanned",false);
                                userData.put("nb_reports",0);
                                userData.put("isBanned",false);
                                userData.put("whenBannedComments",Timestamp.now());
                                userData.put("whenBannedPosts",Timestamp.now());
                                userData.put("points",0);
                                userData.put("privacy","public");
                                userData.put("isBannedComments",false);
                                userData.put("isBannedPosts",false);
                                userData.put("isRestricted",false);
                                userData.put("isAdmin",false);

                                userRef.set(userData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getContext(), MainActivity.class));
                                            requireActivity().finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to fetch existing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}