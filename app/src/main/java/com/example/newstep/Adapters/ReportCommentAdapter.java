package com.example.newstep.Adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.ReportComment;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ReportCommentAdapter extends FirestoreRecyclerAdapter<ReportComment,ReportCommentAdapter.ReportCommentViewHolder> {
  private Context context;

    public ReportCommentAdapter(@NonNull FirestoreRecyclerOptions<ReportComment> options,Context context) {
        super(options);
        this.context=context;
    }

    @NonNull
    @Override
    public ReportCommentAdapter.ReportCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.report_comment_adapter,parent,false);
        return new ReportCommentAdapter.ReportCommentViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ReportCommentAdapter.ReportCommentViewHolder holder, int position, @NonNull ReportComment model) {

       holder.pUsername.setText(model.getpUsername());
       holder.pContent.setText(model.getpContent());
        holder.reportCount.setText(""+model.getNbReports());
       if(model.getNbReports()==1){
       holder.userTextView.setText(" user");
       }
       else{
           holder.userTextView.setText(" users");
       }
        holder.cUsername.setText(model.getcUsername());
        holder.cContent.setText(model.getcContent());

            holder.cUsername.setText(model.getcUsername());
            holder.cContent.setText(model.getcContent());

            holder.infoUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showUserInfo(model.getcUsername());
                }
            });
            holder.dUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("Ban user")
                            .setMessage("This action cannot be undone")
                            .setPositiveButton("Ban", (dialog, which) -> {
                                disableAccount(model.getcUsername(),model.getCommentId());
                                dialog.dismiss();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        holder.lastReportTime.setText("Last report was "+Utilities.getRelativeTime(model.getLastReportTime()));
holder.dComment.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        new AlertDialog.Builder(context)
                .setTitle("Delete comment")
                .setMessage("This action cannot be undone")
                .setPositiveButton("Delete", (dialog, which) -> {
                   deleteComment(model.getPostId(),model.getCommentId());
                   dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
});
holder.dFromComment.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        new AlertDialog.Builder(context)
                .setTitle("Ban user from commenting")
                .setMessage("This action cannot be undone")
                .setPositiveButton("Ban", (dialog, which) -> {
                    banFromCommenting(model.getcUsername(),model.getCommentId());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
});
    }

    private void banFromCommenting(String userName, String commentId) {
        FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("username", userName.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String userId = userDoc.getId();

                        FirebaseUtil.allUserCollectionRef().document(userId)
                                .update("isRestricted",true,"isBannedComments", true,"whenBannedComments", Timestamp.now())
                                .addOnSuccessListener(aVoid -> {Toast.makeText(context, "Account disabled", Toast.LENGTH_SHORT).show();
                                    FirebaseUtil.allCommentReportCollectionRef().document(commentId).delete().addOnSuccessListener(v->{
                                        notifyDataSetChanged();
                                    }).addOnFailureListener(v->{Toast.makeText(context,"Error"+v.getMessage(),Toast.LENGTH_LONG).show();});
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error :"+e.getMessage(), Toast.LENGTH_SHORT).show());

                    } else {
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void disableAccount(String userName,String reportId) {
        FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("username", userName.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String userId = userDoc.getId();

                        FirebaseUtil.allUserCollectionRef().document(userId)
                                .update("isBanned", true,"isRestricted",false,"isBannedComments",false,"isBannedPosts",false,"whenBanned", Timestamp.now())
                                .addOnSuccessListener(aVoid -> {Toast.makeText(context, "Account disabled", Toast.LENGTH_SHORT).show();
                                    FirebaseUtil.allCommentReportCollectionRef().document(reportId).delete().addOnSuccessListener(v->{
                                       notifyDataSetChanged();
                                    }).addOnFailureListener(v->{});
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error :"+e.getMessage(), Toast.LENGTH_SHORT).show());

                    } else {
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteComment(String postId, String commentId) {
        FirebaseUtil.allPostsCollectionRef().document(postId).collection("comments").document(commentId)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context,"Comment deleted",Toast.LENGTH_SHORT).show();
                        FirebaseUtil.allCommentReportCollectionRef().document(commentId).delete().addOnSuccessListener(v->{
                        notifyDataSetChanged();
                        }).addOnFailureListener(v->{});
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void showUserInfo(String userName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUpView = inflater.inflate(R.layout.see_user_info_popup, null);
        dimBackground(0.5f);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        View rootLayout = ((Activity) context).findViewById(android.R.id.content);
        popupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
            layoutParams.alpha = 1.0f;
            ((Activity) context).getWindow().setAttributes(layoutParams);
        });
        popupWindow.setOnDismissListener(() -> dimBackground(1.0f));
        TextView username=popUpView.findViewById(R.id.username);
        TextView nb_reports=popUpView.findViewById(R.id.nb_reports);
        TextView registerTime=popUpView.findViewById(R.id.register_time);
        ImageView pfp=popUpView.findViewById(R.id.pfp);
        ImageButton back=popUpView.findViewById(R.id.back);
        username.setText(userName);
      FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("username", userName)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

                        Long nbReports = userDoc.getLong("nb_reports");
                        if (nbReports != null && nbReports == 1) {
                            nb_reports.setText("Reported by one user");
                        }else{
                            nb_reports.setText("Reported by "+userDoc.getLong("nb_reports")+" users");
                        }
                        registerTime.setText("Member since "+userDoc.getString("registerDate"));
                    } else {
                        Toast.makeText(context,"User not found",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context,"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
        back.setOnClickListener(v->{popupWindow.dismiss();});

    }

    class ReportCommentViewHolder extends RecyclerView.ViewHolder{
TextView pContent,pUsername;
TextView cContent,cUsername;
TextView lastReportTime,reportCount,userTextView;
ImageView dComment,dUser,dFromComment;
ImageButton infoUser;
        public ReportCommentViewHolder(@NonNull View itemView){
            super(itemView);
            pUsername=itemView.findViewById(R.id.pUserName);
            pContent=itemView.findViewById(R.id.pContent);

            cUsername=itemView.findViewById(R.id.cUsername);
            cContent=itemView.findViewById(R.id.cContent);

            lastReportTime=itemView.findViewById(R.id.last_report_time);
            reportCount=itemView.findViewById(R.id.nb_reports);

            dComment=itemView.findViewById(R.id.delete_comment_btn);
            dUser=itemView.findViewById(R.id.disable_user_btn);
            dComment=itemView.findViewById(R.id.delete_comment_btn);
            infoUser=itemView.findViewById(R.id.user_info_btn);
            dFromComment=itemView.findViewById(R.id.ban_from_comments);
            userTextView=itemView.findViewById(R.id.users_textview);
        }
    }
    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
    }
    private void refreshReports() {
        if (getSnapshots() != null) {
            getSnapshots().getSnapshot(0).getReference().getParent().get().addOnSuccessListener(querySnapshot -> {
                notifyDataSetChanged();
            });
        }
    }
}
