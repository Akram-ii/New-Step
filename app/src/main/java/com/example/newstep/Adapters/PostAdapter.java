package com.example.newstep.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.PostModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.NotifOnline;
import com.example.newstep.Util.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final Context context;
    private final List<PostModel> postList;
    private final FirebaseFirestore firestore;
    private final String userId;
    private final OnCommentClickListener  commentClickListener;
    public interface OnCommentClickListener {
        void onCommentClick(String postId);
    }



    public PostAdapter(Context context, List<PostModel> postList,OnCommentClickListener commentClickListener) {
        this.context = context;
        this.postList = postList;
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.commentClickListener = commentClickListener;
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

        holder.postContent.setText(post.getContent());
        holder.username.setText(post.getUserName());
        holder.timestampPost.setText(Utilities.getRelativeTime(post.getTimestamp()));
        holder.likeCount.setText(String.valueOf(post.getLikes()));
        holder.dislikeCount.setText(String.valueOf(post.getDislikes()));

        List<String> likedBy = post.getLikedBy() != null ? new ArrayList<>(post.getLikedBy()) : new ArrayList<>();
        List<String> dislikedBy = post.getDislikedBy() != null ? new ArrayList<>(post.getDislikedBy()) : new ArrayList<>();

        boolean isLiked = likedBy.contains(userId);
        boolean isDisliked = dislikedBy.contains(userId);


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
            } else {
                likedBy.add(userId);
                post.setLikes(post.getLikes() + 1);
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
        TextView postContent, likeCount, dislikeCount, username, timestampPost, commentCount;
        ImageView btnLike, btnDislike,comment_btn;

        public PostViewHolder(View itemView) {
            super(itemView);
            postContent = itemView.findViewById(R.id.postContent);
            username = itemView.findViewById(R.id.userName);
            likeCount = itemView.findViewById(R.id.likeCount);
            timestampPost = itemView.findViewById(R.id.postDate);
            dislikeCount = itemView.findViewById(R.id.dislikeCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            comment_btn = itemView.findViewById(R.id.comment_btn);
            commentCount = itemView.findViewById(R.id.commentCount);
        }
    }
}