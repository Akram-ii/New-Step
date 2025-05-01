package com.example.newstep.Util;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.newstep.ConvActivity;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;

import de.hdodenhof.circleimageview.CircleImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfilePopup {

    public static void show(Context context, View anchor, UserModel user) {

        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_user_profile, null);


        CircleImageView profileImage = popupView.findViewById(R.id.profileImage);
        TextView nameText = popupView.findViewById(R.id.nameText);
        TextView bioText = popupView.findViewById(R.id.bio);
        TextView memberSinceText = popupView.findViewById(R.id.memberSince);
        ImageView backimage = popupView.findViewById(R.id.headerBackground);
        Button messageButton = popupView.findViewById(R.id.messageButton);
        TextView  point = popupView.findViewById(R.id.point);
        ImageView  badge = popupView.findViewById(R.id.badge);


        badge.setImageResource(Utilities.getCurrBadgesImageId(user.getPoints()));
        point.setText(Integer.toString(user.getPoints()));

        Glide.with(context)
                .load(user.getProfileImage())
                .into(profileImage);

        Glide.with(context)
                .load(user.getCoverImage())
                .into(backimage);


        nameText.setText(user.getUsername());
        bioText.setText(user.getBio());
        memberSinceText.setText(user.getRegisterDate());


        Log.d("CHECK_USER", "user id before popup: " + user.getId());
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();

                    if (user.getId().equals(currentUserId)) {
                        Toast.makeText(context, "You can't text yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Log.d("DEBUG", "Clicked on user ID: " + user.getId());
                Toast.makeText(context, "Going to chat with " + user.getUsername(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, ConvActivity.class);

                intent.putExtra("username", user.getUsername());

                intent.putExtra("userId", user.getId());

                intent.putExtra("availability", user.getAvailability());

                context.startActivity(intent);
            }

        });


        //Create popup window
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);

        popupWindow.setAnimationStyle(R.style.PopupSmoothAnimation);


        //popupWindow.showAtLocation(anchor, android.view.Gravity.CENTER, 0, 0);


        //int offsetX = 30;
        //int offsetY = 10;

        //popupWindow.showAsDropDown(anchor, offsetX, offsetY);




        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];


        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;


        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();


        boolean showAbove = (anchorY + anchor.getHeight() + popupHeight > screenHeight);


        int offsetX = 30;
        int offsetY = 10;

        if (showAbove) {

            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY,
                    anchorX + offsetX, anchorY - popupHeight - offsetY);
        } else {

            popupWindow.showAsDropDown(anchor, offsetX, offsetY);
        }



    }


}
