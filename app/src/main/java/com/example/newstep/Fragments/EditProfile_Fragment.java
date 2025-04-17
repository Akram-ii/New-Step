package com.example.newstep.Fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.newstep.ProfileActivity;
import com.example.newstep.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import android.os.Handler;


public class EditProfile_Fragment extends Fragment {


    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int PROFILE_IMAGE = 3;
    private static final int COVER_IMAGE = 4;
    private Uri imageUri;
    private int currentImageType;
    private ImageButton background_image;
    private ImageView couverture_image;

    private ImageButton editprofil_image;
    private ImageView profile_image;
    private Button saveEdit_button;
    private TextInputEditText etUsername;
    private TextInputEditText Bio_Edit;
    private ImageButton back;
    private FrameLayout loadingOverlay;

    private static final String DEFAULT_PROFILE_IMAGE = "https://dummyimage.com/500x500/ffffff/ffffff.png";
    private static final String DEFAULT_COVER_IMAGE = "https://dummyimage.com/500x500/E7E7E7/E7E7E7.png";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile_, container, false);

        background_image = view.findViewById(R.id.editCoverButton);
        couverture_image = view.findViewById(R.id.backGroundEdit_image);
        editprofil_image = view.findViewById(R.id.editProfileButton);
        profile_image = view.findViewById(R.id.profileEdit_Image);
        saveEdit_button = view.findViewById(R.id.SaveEdit_button);
        etUsername = view.findViewById(R.id.et_Username);
        Bio_Edit = view.findViewById(R.id.Bioo);
        back = view.findViewById(R.id.BackEdit_Button);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        background_image.setOnClickListener(v -> showImagePickerDialog(COVER_IMAGE));
        editprofil_image.setOnClickListener(v -> showImagePickerDialog(PROFILE_IMAGE));

        loadUserImages();
        loadUserInfo();


        saveEdit_button.setOnClickListener(v -> {

            if(isAdded()) {

                loadingOverlay.setVisibility(View.VISIBLE);


                if (profile_image.getDrawable() != null) {
                    Uri profileImageUri = getUriFromImageView(profile_image);
                    uploadImageToCloudinary(profileImageUri, "profileImage");
                } else {
                        uploadDefaultImage("profileImage", DEFAULT_PROFILE_IMAGE);
                }

                if (couverture_image.getDrawable() != null) {
                    Uri coverImageUri = getUriFromImageView(couverture_image);
                    uploadImageToCloudinary(coverImageUri, "coverImage");
                } else {
                        uploadDefaultImage("coverImage", DEFAULT_COVER_IMAGE);
                }

                if (etUsername != null && etUsername.getText() != null) {
                    String etUsername2 = etUsername.getText().toString().trim();
                    updateUserName(etUsername2);
                } else {
                    Log.e("Error", "etUsername or its text is null");
                }


                if (Bio_Edit != null && Bio_Edit.getText() != null) {
                    String newBio = Bio_Edit.getText().toString().trim();
                    updateUserBio(newBio);
                } else {
                    Log.e("Error", "newBio or its text is null");
                }


                SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("needRefresh", true).apply();

                new Handler().postDelayed(() -> {

                    loadingOverlay.setVisibility(View.GONE);

                    if (getActivity() != null) {
                        Intent intent = new Intent(requireContext(), ProfileActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                }, 10000);

            }else{
                Log.e("Error", "Fragment not attached to an activity.");
            }


        });

        back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack()

        );



        return view;

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.editProfileButton).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == PICK_IMAGE || requestCode == PROFILE_IMAGE || requestCode == COVER_IMAGE) {
                    if (data != null && data.getData() != null) {
                        Uri sourceUri = data.getData();
                        Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "cropped.jpg"));

                        UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(currentImageType == PROFILE_IMAGE ? 1 : 16,
                                        currentImageType == PROFILE_IMAGE ? 1 : 9)
                                .withMaxResultSize(1080, 1080)
                                .start(requireContext(), this);
                    } else {
                        showToast("No image selected!");
                    }
                } else if (requestCode == TAKE_PHOTO) {
                    if (data != null && data.getExtras() != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        if (photo != null) {
                            Uri tempUri = getImageUri(requireContext(), photo);
                            Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "cropped.jpg"));

                            UCrop.of(tempUri, destinationUri)
                                    .withAspectRatio(currentImageType == PROFILE_IMAGE ? 1 : 16,
                                            currentImageType == PROFILE_IMAGE ? 1 : 9)
                                    .withMaxResultSize(1080, 1080)
                                    .start(requireContext(), this);
                        } else {
                            showToast("Failed to capture image!");
                        }
                    }
                } else if (requestCode == UCrop.REQUEST_CROP) {
                    if (data != null) {
                        Uri resultUri = UCrop.getOutput(data);
                        if (resultUri != null) {
                            if (currentImageType == PROFILE_IMAGE) {
                                profile_image.setImageDrawable(null);
                                profile_image.setImageURI(resultUri);
                            } else if (currentImageType == COVER_IMAGE) {
                                couverture_image.setImageDrawable(null);
                                couverture_image.setImageURI(resultUri);
                            }
                        } else {
                            showToast("Failed to get cropped image!");
                        }
                    }
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                if (data != null) {
                    Throwable error = UCrop.getError(data);
                    showToast("Cropping error: " + (error != null ? error.getMessage() : "Unknown error!"));
                }
            }
        } catch (Exception e) {
            Log.e("UCropError", "Unexpected error in onActivityResult", e);
            e.printStackTrace();
            showToast("An unexpected error occurred: Check Logcat!");
        }
    }



    private void uploadImageToCloudinary(Uri imageUri, String imageType) {
        new Thread(() -> {
            try {
                if (imageUri == null) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> showToast("Image URI is null!"));
                    }
                    return;
                }

                String imagePath = getRealPathFromUri(imageUri);
                if (imagePath == null) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> showToast("Invalid image path!"));
                    }
                    return;
                }

                File imageFile = new File(imagePath);

                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "def4scxo9",
                        "api_key", "623974366749324",
                        "api_secret", "pHoK735JLDIPMB6eUix47_OicjQ"
                ));

                Map uploadResult = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap());

                if (uploadResult.containsKey("secure_url")) {
                    String imageUrl = (String) uploadResult.get("secure_url");

                    Activity activity = getActivity();
                    if (activity != null && isAdded()) {
                        activity.runOnUiThread(() -> {
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                String userId = user.getUid();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put(imageType, imageUrl);

                                firestore.collection("Users").document(userId)
                                        .update(userData)
                                        .addOnSuccessListener(aVoid -> showToast(imageType + " uploaded successfully"))
                                        .addOnFailureListener(e -> showToast("Failed to upload " + imageType));
                            } else {
                                showToast("User is not logged in!");
                            }
                        });
                    }
                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> showToast("Failed to upload image!"));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> showToast("Upload failed: " + e.getMessage()));
                }
            }
        }).start();
    }




    private String getRealPathFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile.getAbsolutePath(); // هذا هو المسار الحقيقي للصورة المؤقتة
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }





    private void updateUserName(String newUserName) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user1 = auth.getCurrentUser();

        if(user1 != null) {

            String userId1 = user1.getUid();


            Map<String, Object> userData = new HashMap<>();
            userData.put("username", newUserName);

            firestore.collection("Users").document(userId1)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> showToast("Username updated successfully"))
                    .addOnFailureListener(e -> showToast("Failed to update username"));
        }else {
            showToast("User is not logged in!");
        }
    }


    private void updateUserBio(String newBio) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user2 = auth.getCurrentUser();

        if(user2 != null) {
            String userId2 = user2.getUid();

            db.collection("Users").document(userId2)
                    .update("bio", newBio)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Bio updated successfully!");
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to update bio: " + e.getMessage());
                    });
        }else{
            showToast("User is not logged in!");
        }
    }


    private Uri getImageUri(Context context, Bitmap bitmap) {
        OutputStream outputStream;
        Uri imageUri = null;

        try {
            // نحدد نوع الصورة ومكانها (MediaStore في الـ Pictures)
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "CapturedImage_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            ContentResolver resolver = context.getContentResolver();
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }else{
                    Log.e("getImageUri", "OutputStream is null!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageUri;
    }



    private Uri getUriFromImageView(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        return getImageUri(requireContext(), bitmap);
    }


    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void pickProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PROFILE_IMAGE);
    }


    private void pickCoverImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, COVER_IMAGE);
    }



    private void showImagePickerDialog(int imageType) {

        currentImageType = imageType;


        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select an image from:");
        String[] options = {"Take a picture", "Choose from the gallery"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });

        builder.show();
    }


    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, TAKE_PHOTO);
    }


    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, PICK_IMAGE);
    }



    private void loadUserInfo() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user0 = auth.getCurrentUser();

        if(user0 != null) {

            String userId = user0.getUid();

            firestore.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String Bio = documentSnapshot.getString("bio");
                            if (username != null) {
                                etUsername.setText(username);
                            }
                            if (Bio != null) {
                                Bio_Edit.setText(Bio);
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Failed to load user info!"));
        }else{
            showToast("User is not logged in!");
        }
    }





    private void loadUserImages() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            firestore.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("profileImage")) {
                                String profileImageUrl = documentSnapshot.getString("profileImage");
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(requireContext()).load(profileImageUrl).into(profile_image);
                                }
                            }

                            if (documentSnapshot.contains("coverImage")) {
                                String coverImageUrl = documentSnapshot.getString("coverImage");
                                if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                                    Glide.with(requireContext()).load(coverImageUrl).into(couverture_image);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Failed to load images!"));
        }
    }


    private void uploadDefaultImage(String imageType, String imageUrl) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            Map<String, Object> data = new HashMap<>();
            data.put(imageType, imageUrl);

            firestore.collection("Users").document(userId)
                    .update(data)
                    .addOnSuccessListener(aVoid -> Log.d("Upload", imageType + " default image uploaded."))
                    .addOnFailureListener(e -> Log.e("Upload", "Failed to upload default image for " + imageType));
        }
    }


}
