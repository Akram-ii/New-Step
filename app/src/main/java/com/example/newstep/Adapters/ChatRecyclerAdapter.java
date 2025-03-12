package com.example.newstep.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.newstep.Models.ChatMsgModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.Objects;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMsgModel, ChatRecyclerAdapter.ChatModelViewHolder> {
    Context context;
    String chatroomId;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMsgModel> options,Context context,String chatroomId) {
        super(options);
        this.context=context;
        this.chatroomId=chatroomId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMsgModel model) {
        if (Objects.equals(model.getSenderId(), FirebaseUtil.getCurrentUserId())) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.pfp.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(model.getMessage());
        } else {
            FirebaseUtil.loadPfp(model.getSenderId(), holder.pfp);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(model.getMessage());
        }
holder.rightChatLayout.setOnLongClickListener(new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
        Utilities.vibratePhone(context);
        new AlertDialog.Builder(context).setMessage("Delete message ?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Log.d("test message id ",model.getMessageId());
                   FirebaseUtil.allChatroomCollectionRef().document(chatroomId).collection("chats").document(model.getMessageId()).delete();
                   notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
        return true;
    }
});

    }
    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.chatmsg_recycler_row,parent,false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftChatLayout,rightChatLayout;
        ImageView pfp;
        TextView leftMsg,rightMsg;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp=itemView.findViewById(R.id.pfp);
            leftChatLayout=itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout=itemView.findViewById(R.id.right_chat_layout);
            leftMsg=itemView.findViewById(R.id.left_chat_textView);
            rightMsg=itemView.findViewById(R.id.right_chat_textView);

        }
    }

}

