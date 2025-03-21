package repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;







public class UserRepository {

    // دالة بلا فائدة لحد الان
    public void saveUserToFirestore(String userName) {


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            String uid = user.getUid();


            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("name", userName);
            userData.put("email", user.getEmail());
            userData.put("profileImage", "default");


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener(aVoid -> {

                        Log.d("Firestore", "Save user data then successfully");
                    })
                    .addOnFailureListener(e -> {

                        Log.e("Firestore", "Saving user data then failed", e);
                    });
        }
    }


    public void saveGoogleUserToFirestore() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            String uid = user.getUid();


            String name = user.getDisplayName();
            String email = user.getEmail();
            String profileImage = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "default";


            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("name", name);
            userData.put("email", email);
            userData.put("profileImage", profileImage);


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener(aVoid -> {

                        Log.d("Firestore", "User data saved successfully");
                    })
                    .addOnFailureListener(e -> {

                        Log.e("Firestore", "Failed to save user data", e);
                    });
        }
    }

    private DatabaseReference userRef;
    private FirebaseAuth auth;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void getUserData(UserDataCallback callback) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();


            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String profileImage = documentSnapshot.getString("profileImage");

                            callback.onUserDataReceived(name, profileImage);
                        } else {
                            callback.onUserDataReceived("User", "default_image_url");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Failed to get user data", e);
                    });
        }

    }



    public void getUserFromUsersCollection(String userId, UserDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");  // تأكد من أن الحقل هنا صحيح
                        String profileImage = documentSnapshot.getString("profileImage");
                        callback.onUserDataReceived(name, profileImage);
                    } else {
                        callback.onUserDataReceived("User", "default_image_url");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to get user data", e);
                });
    }










    public interface UserDataCallback {
        void onUserDataReceived(String name, String profileImage);
    }

}






