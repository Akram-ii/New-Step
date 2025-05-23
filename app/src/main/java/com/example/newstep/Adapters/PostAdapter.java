package com.example.newstep.Adapters;


import android.content.ClipData;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newstep.Models.GoalModel;
import com.example.newstep.Models.PostModel;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.UserProfilePopup;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.NotifOnline;
import com.example.newstep.Util.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import android.content.ClipboardManager;

import io.opencensus.metrics.export.Summary;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final Context context;
    private final List<PostModel> postList;
    private final FirebaseFirestore firestore;
    private final String userId;
    private final OnCommentClickListener commentClickListener;
    private final OnReportClickListener reportClickListener;

    public interface OnCommentClickListener {
        void onCommentClick(String postId);
    }

    public interface OnReportClickListener {
        void onReportClick(String postId, String pUsername, String pContent);
    }

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.commentClickListener = new OnCommentClickListener() {
            @Override
            public void onCommentClick(String postId) {

            }
        };
        this.reportClickListener = new OnReportClickListener() {
            @Override
            public void onReportClick(String postId, String pUsername, String pContent) {

            }
        };
    }

    public PostAdapter(Context context, List<PostModel> postList, OnCommentClickListener commentClickListener, OnReportClickListener reportClickListener) {
        this.context = context;
        this.postList = postList;
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.commentClickListener = commentClickListener;
        this.reportClickListener = reportClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = postList.get(position);
        String postId = post.getId();
if(post.getUserId()==FirebaseUtil.getCurrentUserId()){
    holder.btnReport.setVisibility(View.GONE);
}
if(!post.getAnonymous()) {
    Glide.with(holder.itemView.getContext())
            .load(post.getProfileImageUrl())
            .placeholder(R.drawable.pfp_purple)
            .error(R.drawable.pfp_purple)
            .circleCrop()
            .into(holder.P_image);
}else{
    holder.P_image.setImageResource(R.drawable.pfp_purple);
}
        holder.P_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
if(post.getAnonymous()){
    return;
}else{
                UserModel user = new UserModel();
                user.setId(post.getUserId());
                user.setUsername(post.getUserName());
                user.setProfileImage(post.getProfileImageUrl());

                FirebaseFirestore.getInstance().collection("Users")
                        .document(post.getUserId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {

                                String bio = documentSnapshot.getString("bio");
                                String registerDate = documentSnapshot.getString("registerDate");
                                String coverImage = documentSnapshot.getString("coverImage");
                                Long pointsLong = documentSnapshot.getLong("points");
                                int points = pointsLong != null ? pointsLong.intValue() : 0;

                                user.setBio(bio);
                                user.setRegisterDate(registerDate);
                                user.setCoverImage(coverImage);
                                user.setPoints(points);


                            }else{
                                Toast.makeText(context, "nooooo", Toast.LENGTH_SHORT).show();

                            }

                            UserProfilePopup.show(context, v, user);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("PopupUser", "Error getting info", e);

                            UserProfilePopup.show(context, v, user);
                        });
            }}
        });


        //String cat = post.getCategory().toLowerCase();
        String cat = (post.getCategory() != null) ? post.getCategory().toLowerCase() : "";
        int resId;

        switch (cat) {
            case "my experience":
                resId = R.string.setting22;
                break;
            case "ask for help":
                resId = R.string.setting23;
                break;
            case "motivation":
                resId = R.string.setting24;
                break;
            case "reflection":
                resId = R.string.setting25;
                break;
            case "progress update":
                resId = R.string.setting26;
                break;
            case "challenges":
                resId = R.string.setting27;
                break;
            case "advices":
                resId = R.string.setting28;
                break;
            case "other":
                resId = R.string.setting29;
                break;
            default:
                resId = R.string.setting22;
                break;
        }



        if(post.getCategory()!=null){
            //holder.cat.setText(post.getCategory());
            holder.cat.setText(context.getString(resId));
        }




        holder.postContent.setText(post.getContent());
if(post.getAnonymous()){
    holder.username.setText("Anonymous User");
}else {
    holder.username.setText(post.getUserName());
}
        holder.username.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Context context = v.getContext();
                String username = holder.username.getText().toString();

                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Username", username);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Username copied", Toast.LENGTH_SHORT).show();

                return true;
            }
        });



        holder.timestampPost.setText(Utilities.getRelativeTime(post.getTimestamp()));
        holder.likeCount.setText(String.valueOf(post.getLikes()));
        holder.dislikeCount.setText(String.valueOf(post.getDislikes()));

        List<String> likedBy = post.getLikedBy() != null ? new ArrayList<>(post.getLikedBy()) : new ArrayList<>();
        List<String> dislikedBy = post.getDislikedBy() != null ? new ArrayList<>(post.getDislikedBy()) : new ArrayList<>();

        boolean isLiked = likedBy.contains(userId);
        boolean isDisliked = dislikedBy.contains(userId);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(post.getUserId().equals(FirebaseUtil.getCurrentUserId())){
                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                    builder.setTitle("Delete Post");
                    builder.setMessage("This action cannot be undone");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUtil.allPostsCollectionRef().document(post.getId()).delete();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                }else{}
                return true;
            }
        });
        holder.btnLike.setColorFilter(isLiked ? Color.RED : Color.DKGRAY);
        holder.btnDislike.setColorFilter(isDisliked ? Color.BLUE : Color.DKGRAY);

        firestore.collection("posts").document(postId).collection("comments")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        int commentCount = value.size();
                        holder.commentCount.setText(String.valueOf(commentCount));
                    }
                });
        holder.btnLike.setOnClickListener(v -> {
            if (isLiked) {
                likedBy.remove(userId);
                post.setLikes(post.getLikes() - 1);
                Utilities.addPointsToUsers(userId,-2);
                Utilities.addPointsToUsers(post.getUserId(),-5);
            } else {
                likedBy.add(userId);
                post.setLikes(post.getLikes() + 1);
                Utilities.addPointsToUsers(userId,2);
                Utilities.addPointsToUsers(post.getUserId(),5);
                Log.d( "idmo pos ",""+post.getUserId());
                 FirebaseUtil.allUserCollectionRef().document(post.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        NotifOnline notif=new NotifOnline(documentSnapshot.getString("token"),FirebaseUtil.getCurrentUsername(context)+" liked your post",post.getContent(),context);
                        notif.sendNotif();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                if (isDisliked) {
                    dislikedBy.remove(userId);
                    post.setDislikes(post.getDislikes() - 1);
                }
            }
            updateFirestore(postId, post, likedBy, dislikedBy, holder, position);
        });

        holder.btnDislike.setOnClickListener(v -> {
            if (isDisliked) {
                dislikedBy.remove(userId);
                post.setDislikes(post.getDislikes() - 1);
            } else {
                dislikedBy.add(userId);
                post.setDislikes(post.getDislikes() + 1);
                FirebaseUtil.allUserCollectionRef().document(post.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        NotifOnline notif=new NotifOnline(documentSnapshot.getString("token"),FirebaseUtil.getCurrentUsername(context)+" disliked your post",post.getContent(),context);
                        notif.sendNotif();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                if (isLiked) {
                    likedBy.remove(userId);
                    post.setLikes(post.getLikes() - 1);
                }
            }
            updateFirestore(postId, post, likedBy, dislikedBy, holder, position);
        });

        holder.comment_btn.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(post.getId());
            }
        });

        holder.btnReport.setOnClickListener(v -> {
            if (reportClickListener != null) {
                reportClickListener.onReportClick(postId, post.getUserName(), post.getContent());
            }
        });
    }

    private void updateFirestore(String postId, PostModel post, List<String> likedBy, List<String> dislikedBy, PostViewHolder holder, int position) {
        firestore.collection("posts").document(postId)
                .update("likes", post.getLikes(),
                        "dislikes", post.getDislikes(),
                        "likedBy", likedBy,
                        "dislikedBy", dislikedBy)
                .addOnSuccessListener(aVoid -> {
                    post.setLikedBy(new ArrayList<>(likedBy));
                    post.setDislikedBy(new ArrayList<>(dislikedBy));
                    notifyItemChanged(position);
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postContent, cat,likeCount, dislikeCount, username, timestampPost, commentCount;
        ImageView btnLike, btnDislike,comment_btn,P_image,btnReport;

        public PostViewHolder(View itemView) {
            super(itemView);
            cat=itemView.findViewById(R.id.postCat);
            postContent = itemView.findViewById(R.id.postContent);
            username = itemView.findViewById(R.id.userName);
            likeCount = itemView.findViewById(R.id.likeCount);
            timestampPost = itemView.findViewById(R.id.postDate);
            dislikeCount = itemView.findViewById(R.id.dislikeCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            comment_btn = itemView.findViewById(R.id.comment_btn);
            commentCount = itemView.findViewById(R.id.commentCount);
            P_image = itemView.findViewById(R.id.userPicture);
            btnReport = itemView.findViewById(R.id.btn_report);
        }
    }
}