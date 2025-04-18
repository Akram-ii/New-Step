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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.ChatMsgModel;
import com.example.newstep.R;
import com.example.newstep.Util.FirebaseUtil;
import com.example.newstep.Util.Utilities;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMsgModel, ChatRecyclerAdapter.ChatModelViewHolder> {
    private Context context;
    private String chatroomId;
    private long lastClickTime = 0;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMsgModel> options, Context context, String chatroomId) {
        super(options);
        this.context = context;
        this.chatroomId = chatroomId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMsgModel model) {

        if (Objects.equals(model.getSenderId(), FirebaseUtil.getCurrentUserId())) {

            holder.leftChatLayout.setVisibility(View.GONE);
            holder.pfp.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(model.getMessage());
            holder.leftLikeContainer.setVisibility(View.GONE);


            holder.rightChatLayout.setOnClickListener(v -> handleMessageClick(holder));
            holder.rightChatLayout.setOnLongClickListener(v -> {
                showDeleteDialog(model);
                return true;
            });
        } else {

            FirebaseUtil.loadPfp(model.getSenderId(), holder.pfp);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(model.getMessage());
            holder.rightLikeContainer.setVisibility(View.GONE);


            holder.leftChatLayout.setOnClickListener(v -> handleMessageClick(holder));
        }


        updateLikeUI(holder, model);
    }

    private void handleMessageClick(ChatModelViewHolder holder) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 300) { // Double click detection
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                handleLikeAction(holder, position);
            }
            lastClickTime = 0;
        } else {
            lastClickTime = currentTime;
        }
    }

    private void handleLikeAction(ChatModelViewHolder holder, int position) {
        ChatMsgModel model = getItem(position);
        if (model == null) return;

        String currentUserId = FirebaseUtil.getCurrentUserId();
        List<String> newLikedBy = new ArrayList<>(model.getLikedBy());


        if (newLikedBy.contains(currentUserId)) {
            newLikedBy.remove(currentUserId);
        } else {
            newLikedBy.add(currentUserId);
        }


        Map<String, Object> updates = new HashMap<>();
        updates.put("likedBy", newLikedBy);
        updates.put("likeCount", newLikedBy.size());
        updates.put("lastUpdated", FieldValue.serverTimestamp());


        FirebaseUtil.getChatroomMsgRef(chatroomId)
                .document(model.getMessageId())
                .update(updates)
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show();
                    Log.e("LIKE_ERROR", "Update failed", e);
                });
    }

    private void updateLikeUI(ChatModelViewHolder holder, ChatMsgModel model) {
        int likeCount = model.getLikeCount();

        if (Objects.equals(model.getSenderId(), FirebaseUtil.getCurrentUserId())) {

            holder.rightLikeCount.setText(String.valueOf(likeCount));
            holder.rightLikeContainer.setVisibility(likeCount > 0 ? View.VISIBLE : View.GONE);
        } else {

            holder.leftLikeCount.setText(String.valueOf(likeCount));
            holder.leftLikeContainer.setVisibility(likeCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void showDeleteDialog(ChatMsgModel model) {
        Utilities.vibratePhone(context);
        new AlertDialog.Builder(context)
                .setMessage("Delete message?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMessage(model))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessage(ChatMsgModel model) {
        FirebaseUtil.getChatroomMsgRef(chatroomId)
                .document(model.getMessageId())
                .delete()
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete message", Toast.LENGTH_SHORT).show();
                    Log.e("DELETE_ERROR", "Delete failed", e);
                });
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatmsg_recycler_row, parent, false);
        view.setBackgroundResource(android.R.color.transparent); // Disable ripple effect
        return new ChatModelViewHolder(view);
    }

    static class ChatModelViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        ImageView pfp;
        TextView leftMsg, rightMsg;
        LinearLayout leftLikeContainer, rightLikeContainer;
        TextView leftLikeCount, rightLikeCount;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.pfp);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftMsg = itemView.findViewById(R.id.left_chat_textView);
            rightMsg = itemView.findViewById(R.id.right_chat_textView);
            leftLikeContainer = itemView.findViewById(R.id.left_like_container);
            rightLikeContainer = itemView.findViewById(R.id.right_like_container);
            leftLikeCount = itemView.findViewById(R.id.left_like_count);
            rightLikeCount = itemView.findViewById(R.id.right_like_count);
        }
    }
}