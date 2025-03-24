package com.example.newstep.Adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.ReportComment;
import com.example.newstep.Models.UserModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ReportCommentAdapter extends FirestoreRecyclerAdapter<ReportComment,ReportCommentAdapter.ReportCommentViewHolder> {
  private Context context;
  private SearchUserRecyclerAdapter adapter;

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
       if(model.getNbReports()==1){
           holder.reportCount.setText("Reported by one user");
       }
       else{
           holder.reportCount.setText("Reported by "+model.getNbReports()+" users");
       }
        holder.cUsername.setText(model.getcUsername());
        holder.cContent.setText(model.getcContent());
        Query query = FirebaseUtil.allUserCollectionRef()
                .whereIn("userId", model.getUserIds())
                .orderBy("username");
        FirestoreRecyclerOptions<UserModel> options=new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query,UserModel.class).build();
        adapter=new SearchUserRecyclerAdapter(options,context);
        holder.usersRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.usersRecycler.setAdapter(adapter);
        adapter.startListening();

            holder.cUsername.setText(model.getcUsername());
            holder.cContent.setText(model.getcContent());

    }
    class ReportCommentViewHolder extends RecyclerView.ViewHolder{
TextView pContent,pUsername;
TextView cContent,cUsername;
TextView lastReportTime,reportCount;
RecyclerView usersRecycler;
        public ReportCommentViewHolder(@NonNull View itemView){
            super(itemView);
            pUsername=itemView.findViewById(R.id.pUserName);
            pContent=itemView.findViewById(R.id.pContent);

            cUsername=itemView.findViewById(R.id.cUsername);
            cContent=itemView.findViewById(R.id.cContent);

            lastReportTime=itemView.findViewById(R.id.last_report_time);
            reportCount=itemView.findViewById(R.id.nb_reports);
            usersRecycler=itemView.findViewById(R.id.users_recycler_view);

        }
    }
}
