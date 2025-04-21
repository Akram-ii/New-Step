package com.example.newstep.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstep.Adapters.BadgesAdapter;
import com.example.newstep.ConvActivity;
import com.example.newstep.Models.BadgeModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class BadgesFragment extends Fragment {
RecyclerView recyclerBadges;
ImageView currBadge,nextBadge;
List<BadgeModel> badges;
TextView currPoints;
BadgesAdapter adapter;
Button q1,q2,q3,q4;
ProgressBar pBar;
    private ListenerRegistration registration;
@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_badges, container, false);
    recyclerBadges=rootView.findViewById(R.id.recyclerBadges);
    pBar=rootView.findViewById(R.id.progressBar);
    currBadge=rootView.findViewById(R.id.currbadge);

    q1=rootView.findViewById(R.id.q1);
    q1.setOnClickListener(v->{setupBottomDialog("What are Badges",
            "Badges are special milestones you earn by being active in the community and supporting your own growth journey.\n" +
                    "\n" +
                    "They're not just for show — they’re a way to track your progress, celebrate your consistency, and encourage positive habits."
            );});
    q2=rootView.findViewById(R.id.q2);
    q2.setOnClickListener(v->{setupBottomDialog("What actions give me points?",
            "You earn points by:\n" +
                    "\n" +
                    "✦ Liking posts or comments\n" +
                    "\n" +
                    "✦ Posting your own updates or reflections\n" +
                    "\n" +
                    "✦ Commenting on others’ posts\n" +
                    "\n" +
                    "✦ Receiving likes on your posts or comments\n" +
                    "\n" +
                    "✦ Being consistent in your activity\n"
            );});
    q3=rootView.findViewById(R.id.q3);
    q3.setOnClickListener(v->{setupBottomDialog("Can i see other people's badge?",
            "Yes! When you view someone’s profile or one of their posts, you’ll see the \uD83C\uDFC5 badge they’ve earned so far.\n" +
                    "\n" +
                    "It’s a fun and meaningful way to:\n" +
                    "✦ Celebrate each other’s progress\n" +
                    "✦ Recognize the effort others are putting in\n" +
                    "✦ Feel more connected to the community\n" +
                    "\n" +
                    "Each badge tells a story — a reminder that we’re all growing \uD83C\uDF31, step by step, together \uD83E\uDD1D."
            );});
    q4=rootView.findViewById(R.id.q4);
    q4.setOnClickListener(v->{setupBottomDialog("Why do i need points?",
            "Points are a way to track your growth and activity in the community. Every time you post, comment, like something, or get support from others, you earn points.\n" +
                    "\n" +
                    "▪ These points help you unlock badges, which represent different stages in your journey. They’re here to motivate you, show your progress, and remind you that even the small actions you take matter.\n" +
                    "\n" +
                    "▪ The more you interact, the more you grow — and your points reflect that."
    );});

    nextBadge=rootView.findViewById(R.id.nextBadge);
    currPoints=rootView.findViewById(R.id.currPoints);

badges=new ArrayList<>();
    badges.add(new BadgeModel("First Step", 0, R.drawable.first_step_badge));
    badges.add(new BadgeModel("Starting to See", 25, R.drawable.fog_badge));
    badges.add(new BadgeModel("Inner Spark", 75, R.drawable.fire_badge));
    badges.add(new BadgeModel("Steady Glow", 150, R.drawable.lighthouse_badge));
    badges.add(new BadgeModel("Beacon", 300, R.drawable.sun_badge));
    badges.add(new BadgeModel("Warm Shelter", 500, R.drawable.nest_badge));
    badges.add(new BadgeModel("Safe Place", 1000, R.drawable.safe_badge));
    badges.add(new BadgeModel("Gathering Light", 2000, R.drawable.star_badge));
    badges.add(new BadgeModel("Healer", 4000, R.drawable.healer_badge));
    badges.add(new BadgeModel("Phoenix", 10000, R.drawable.phoenix_badge));
setupRecycler();
    registration=FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).addSnapshotListener((snapshot,e)->{
        if (e != null) {
            return;
        }

        if (snapshot != null && snapshot.exists()) {
        Long points=snapshot.getLong("points");
            currBadge.setImageResource(Utilities.getCurrBadgesImageId(points.intValue()));
            nextBadge.setImageResource(Utilities.getNextBadgeImageId(points.intValue()));
            int previousPointsForBadge=Utilities.getMinBadge(points.intValue());
            int nextPointsForBadge=Utilities.getNextBadge(points.intValue());
            int progress=(int) (((float)(points.intValue() - previousPointsForBadge) /
                    (nextPointsForBadge- previousPointsForBadge)) * 100);
            pBar.setProgress(progress);
            currPoints.setText(points.intValue()+" Points");

        }
    });


return rootView;
    }

public void setupBottomDialog(String title,String desc){
    BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(getContext());
    View view1=LayoutInflater.from(getContext()).inflate(R.layout.bottom_dialog,null);
    bottomSheetDialog.setContentView(view1);
    TextView title1=view1.findViewById(R.id.title),desc1=view1.findViewById(R.id.desc);
    title1.setText(title);
    desc1.setText(desc);
    bottomSheetDialog.show();

}


    private void setupRecycler() {
         adapter = new BadgesAdapter(badges);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerBadges.setLayoutManager(layoutManager);
        recyclerBadges.setAdapter(adapter);
}
}