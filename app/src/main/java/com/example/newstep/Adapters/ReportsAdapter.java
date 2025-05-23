package com.example.newstep.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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

import com.example.newstep.Models.ReportsModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class ReportsAdapter extends FirestoreRecyclerAdapter<ReportsModel,ReportsAdapter.ReportsViewHolder> {


    private Context context;

    public ReportsAdapter(@NonNull FirestoreRecyclerOptions<ReportsModel> options , Context context) {
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportsViewHolder holder, int position, @NonNull ReportsModel model) {
      holder.Rusername.setText(model.getRusername());
      holder.Rtitle.setText(model.getRtitle());
      holder.Rdesc.setText(model.getRdesc());
      holder.Rcategory.setText(model.getRcat());
      holder.Rtime.setText(Utilities.getRelativeTime(model.getRtime()));
      holder.info.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              showUserInfo(model.getRusername());
          }
      });
      holder.delete.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              deleteReport(model.getId());
          }
      });
    }

    @NonNull
    @Override
    public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.report_item,parent,false);
        return new ReportsAdapter.ReportsViewHolder(view);
    }

    class ReportsViewHolder extends RecyclerView.ViewHolder{

        TextView Rusername,Rtitle,Rdesc,Rcategory,Rtime;
        ImageView info,delete;


         public ReportsViewHolder(@NonNull View itemView) {
             super(itemView);
             Rusername=itemView.findViewById(R.id.RuserName);
             Rtitle=itemView.findViewById(R.id.Rtitle);
             Rdesc=itemView.findViewById(R.id.Rdesc);
             Rcategory=itemView.findViewById(R.id.Rcat);
             Rtime=itemView.findViewById(R.id.Rtime);
             info=itemView.findViewById(R.id.Ruserinfo);
             delete=itemView.findViewById(R.id.btn_delete_report);
         }
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
    private void deleteReport(String Rid) {


        new AlertDialog.Builder(context)
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report ?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    FirebaseUtil.allcontactCollectionRef().document(Rid)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Report deleted successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete the report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void dimBackground(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
    }
}
