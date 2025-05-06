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
    q1.setOnClickListener(v->{setupBottomDialog(getString(R.string.badges_title),
            getString(R.string.badges_description)
    );});
    q2=rootView.findViewById(R.id.q2);
    q2.setOnClickListener(v->{setupBottomDialog(getString(R.string.points_earning_title),
            getString(R.string.points_earning_info)
            );});
    q3=rootView.findViewById(R.id.q3);
    q3.setOnClickListener(v->{setupBottomDialog(getString(R.string.badges_see_title),
            getString(R.string.badges_see_info)
            );});
    q4=rootView.findViewById(R.id.q4);
    q4.setOnClickListener(v->{setupBottomDialog(getString(R.string.points_need_title),
            getString(R.string.points_info)
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
            currPoints.setText(getString(R.string.points_display, points.intValue()));

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