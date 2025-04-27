package com.example.newstep.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.example.newstep.Models.ReportPost;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportPostAdapter extends FirestoreRecyclerAdapter<ReportPost,ReportPostAdapter.ReportPostViewHolder> {


   private Context context;
    public ReportPostAdapter(@NonNull FirestoreRecyclerOptions<ReportPost> options,Context context) {
        super(options);
        this.context=context;
    }

    @NonNull
    @Override
    public ReportPostAdapter.ReportPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.report_post_adapter,parent,false);
        return new ReportPostAdapter.ReportPostViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportPostAdapter.ReportPostViewHolder holder, int position, @NonNull ReportPost model) {
          holder.PostUserName.setVisibility(View.VISIBLE);
          holder.PostUserName.setText(model.getPusername());
          holder.Content.setText(model.getPcontent());

          if(model.getLastReportPostTime()!=null){
              holder.lastReportTime.setText("last report was: "+Utilities.getRelativeTime(model.getLastReportPostTime()));
          }
          else{
              holder.lastReportTime.setText("unknown");
          }
          holder.deletePost.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  deletePost(model.getPostId());}
              });

          if(model.getReportCount()==1){
              holder.reportCount.setText("one user");

          }
          else{
              holder.reportCount.setText(""+String.valueOf(model.getReportCount())+ "users");
          }


           holder.infoUser.setVisibility(View.VISIBLE);
        holder.infoUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showUserInfo(model.getPusername());
            }
        });

        holder.BanFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Ban user from Posting ")
                        .setMessage("This action cannot be undone")
                        .setPositiveButton("Ban", (dialog, which) -> {
                            banUserFromPosting(model.getPusername(),model.getPostId());
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        holder.DisableUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Ban account")
                        .setMessage("This action cannot be undone")
                        .setPositiveButton("Ban", (dialog, which) -> {
                            disableAccount(model.getPusername(),model.getPostId());
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });



    }


    class ReportPostViewHolder extends RecyclerView.ViewHolder{
        private TextView PostUserName,Content,reportCount,lastReportTime;
        private ImageView deletePost,BanFP,DisableUser,infoUser;



        public ReportPostViewHolder(@NonNull View itemView) {
            super(itemView);
            PostUserName=itemView.findViewById(R.id.pUserName);
            Content=itemView.findViewById(R.id.pContent);
            reportCount=itemView.findViewById(R.id.nb_reports);
            lastReportTime=itemView.findViewById(R.id.last_report_time_OfPosts);
            deletePost=itemView.findViewById(R.id.btn_delete_post);
            BanFP=itemView.findViewById(R.id.ban_user_from_post);
            infoUser=itemView.findViewById(R.id.infoIconPost);
            DisableUser=itemView.findViewById(R.id.disable_user_icon);



        }

    }


    private void deletePost(String pId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        new AlertDialog.Builder(context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post from the app?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    db.collection("posts").document(pId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {

                                db.collection("reportPosts").document(pId)
                                        .delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(context, "Post deleted successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to delete report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
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
    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
    }
    private void disableAccount(String userName,String PostId) {
        FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("username", userName.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String userId = userDoc.getId();

                        FirebaseUtil.allUserCollectionRef().document(userId)
                                .update("isBanned", true,"whenBanned",Timestamp.now(),"isRestricted",false,"isBannedComments",false,"isBannedPosts",false)
                                .addOnSuccessListener(aVoid -> {Toast.makeText(context, "Account disabled", Toast.LENGTH_SHORT).show();
                                    FirebaseUtil.allPostsReportCollectionRef().document(PostId).delete().addOnSuccessListener(v->{
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
    private void banUserFromPosting(String userName, String PostId) {
        FirebaseUtil.allUserCollectionRef()
                .whereEqualTo("username", userName.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String userId = userDoc.getId();

                        FirebaseUtil.allUserCollectionRef().document(userId)
                                .update("isRestricted",true,"isBannedPosts", true,"whenBannedPosts", Timestamp.now())
                                .addOnSuccessListener(aVoid -> {Toast.makeText(context, "Posting disabled", Toast.LENGTH_SHORT).show();
                                    FirebaseUtil.allPostsReportCollectionRef().document(PostId).delete().addOnSuccessListener(v->{
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

}
