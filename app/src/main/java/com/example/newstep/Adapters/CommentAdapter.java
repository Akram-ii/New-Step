package com.example.newstep.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import android.app.AlertDialog;

import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.PostModel;
import com.example.newstep.Models.ReportComment;
import com.example.newstep.Models.Comment;
import com.example.newstep.R;
import com.example.newstep.Util.NotifOnline;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private Context context;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        String username = (comment.getUsername() != null && !comment.getUsername().isEmpty())
                ? comment.getUsername()
                : "Unknown user";


        holder.textUsername.setText(username);
        holder.textComment.setText(comment.getText());

        holder.btn_report.setOnClickListener(v -> {
            showReportPopup(comment.getPostId(), comment.getCommentId(),
                    comment.getpUsername(), comment.getpContent(),
                    comment.getUsername(), comment.getText());
        });


        holder.textDate.setText(getFormattedTime(comment.getTimestamp()));


        List<String> likes = comment.getLikes() != null ? comment.getLikes() : new ArrayList<>();
        holder.textLikeCount.setText(String.valueOf(likes.size()));


        boolean hasLiked = likes.contains(currentUserId);


        holder.buttonLikeComment.setColorFilter(hasLiked ? Color.RED : Color.GRAY);

        db.collection("posts").document(comment.getPostId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        final String pUsername = documentSnapshot.getString("username") != null ? documentSnapshot.getString("username") : "Unknown";
                        final String pContent = documentSnapshot.getString("content") != null ? documentSnapshot.getString("content") : "No content";


                        holder.btn_report.setOnClickListener(v -> {
                            showReportPopup(comment.getPostId(), comment.getCommentId(),
                                    pUsername, pContent, comment.getUsername(), comment.getText());
                        });
                    }
                });

        holder.buttonLikeComment.setOnClickListener(v -> {
            DocumentReference commentRef = db.collection("posts")
                    .document(comment.getPostId())
                    .collection("comments")
                    .document(comment.getCommentId());

            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(commentRef);
                Comment updatedComment = snapshot.toObject(Comment.class);
                if (updatedComment == null) return null;

                List<String> updatedLikes = updatedComment.getLikes() != null ? updatedComment.getLikes() : new ArrayList<>();
                if (updatedLikes.contains(currentUserId)) {
                    updatedLikes.remove(currentUserId);
                } else {
                    updatedLikes.add(currentUserId);

                    String commentOwnerId = comment.getUserId();
                    if (!commentOwnerId.equals(currentUserId)) {
                        FirebaseFirestore.getInstance().collection("Users").document(commentOwnerId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String ownerFCMToken = userDoc.getString("token");


                                        FirebaseFirestore.getInstance().collection("Users").document(currentUserId)
                                                .get()
                                                .addOnSuccessListener(currentUserDoc -> {
                                                    String likerUsername = "Someone";
                                                    if (currentUserDoc.exists() && currentUserDoc.contains("username")) {
                                                        likerUsername = currentUserDoc.getString("username");
                                                    }

                                                    String title = "New Like on Your Comment";
                                                    String body = likerUsername + " liked your comment.";

                                                    NotifOnline notif = new NotifOnline(ownerFCMToken, title, body, context);
                                                    notif.sendNotif();
                                                });
                                    }
                                });
                    }
                }

                transaction.update(commentRef, "likes", updatedLikes);
                return updatedLikes;
            }).addOnSuccessListener(updatedLikes -> {
                comment.setLikes(updatedLikes);
                notifyItemChanged(position);
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error ", Toast.LENGTH_SHORT).show();
            });
        });


        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getUserId().equals(currentUserId)) {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Delete your comment")
                        .setMessage("Do you want to delete your comment?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            db.collection("posts")
                                    .document(comment.getPostId())
                                    .collection("comments")
                                    .document(comment.getCommentId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                Toast.makeText(context, "You can only delete your own comment", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textComment, textLikeCount, textDate;
        ImageView buttonLikeComment;
       ImageView  btn_report;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textComment = itemView.findViewById(R.id.textComment);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            textDate = itemView.findViewById(R.id.textDate);
            buttonLikeComment = itemView.findViewById(R.id.buttonLikeComment);
            btn_report = itemView.findViewById(R.id.btn_report);
        }
    }


    private String getFormattedTime(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;


        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else {

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            return sdf.format(new Date(timestamp));
        }
    }
    private void showReportPopup(String postId, String commentId, String pUsername, String pContent, String cUsername, String cContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.popup_commentrepport, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnReport = view.findViewById(R.id.btnReport);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnReport.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            checkIfAlreadyReported(postId, commentId, userId, pUsername, pContent, cUsername, cContent);
            dialog.dismiss();
        });
    }


    private void checkIfAlreadyReported(String postId, String commentId, String userId, String pUsername, String pContent, String cUsername, String cContent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reportRef = db.collection("ReportComments").document(commentId);

        reportRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> userIds = (List<String>) documentSnapshot.get("userIds");
                if (userIds != null && userIds.contains(userId)) {
                    Toast.makeText(context, "You have already reported this comment", Toast.LENGTH_SHORT).show();
                } else {
                    updateExistingReport(reportRef, userId);
                }
            } else {
                createNewReport(postId, commentId, userId, pUsername, pContent, cUsername, cContent);
            }
        });
    }


    private void createNewReport(String postId, String commentId, String userId, String pUsername, String pContent, String cUsername, String cContent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String reportId = UUID.randomUUID().toString();

        ReportComment report = new ReportComment(reportId, postId, commentId, Timestamp.now(), new ArrayList<>());
        report.getUserIds().add(userId);
        report.setNbReports(1);
        report.setpUsername(pUsername);
        report.setpContent(pContent);
        report.setcUsername(cUsername);
        report.setcContent(cContent);

        db.collection("ReportComments").document(commentId).set(report)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Comment reported successfully", Toast.LENGTH_SHORT).show()
                );
    }
    private void updateExistingReport(DocumentReference reportRef, String userId) {
        reportRef.update("userIds", FieldValue.arrayUnion(userId),
                        "nbReports", FieldValue.increment(1),
                        "lastReportTime", Timestamp.now())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Comment reported successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to report comment", Toast.LENGTH_SHORT).show()
                );
    }

}