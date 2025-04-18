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
    Context context;
    String chatroomId;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMsgModel> options, Context context, String chatroomId) {
        super(options);
        this.context = context;
        this.chatroomId = chatroomId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMsgModel model) {
        // Set message content and visibility
        if (Objects.equals(model.getSenderId(), FirebaseUtil.getCurrentUserId())) {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.pfp.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(model.getMessage());
            holder.leftLikeContainer.setVisibility(View.GONE);
        } else {
            FirebaseUtil.loadPfp(model.getSenderId(), holder.pfp);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(model.getMessage());
            holder.rightLikeContainer.setVisibility(View.GONE);
        }

        // Update like UI
        updateLikeUI(holder, model);

        // Set double-click listener for likes
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < 300) { // Double-click detection
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        handleLikeAction(holder, currentPosition);
                    }
                    lastClickTime = 0;
                } else {
                    lastClickTime = clickTime;
                }
            }
        });

        // Set long-click listener for message deletion
        holder.rightChatLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Utilities.vibratePhone(context);
                new AlertDialog.Builder(context)
                        .setMessage("Delete message?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseUtil.getChatroomMsgRef(chatroomId)
                                    .document(model.getMessageId())
                                    .delete()
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete message", Toast.LENGTH_SHORT).show();
                                        Log.e("DELETE_ERROR", "Failed to delete message", e);
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
        });
    }

    private void handleLikeAction(ChatModelViewHolder holder, int position) {
        ChatMsgModel model = getItem(position);
        if (model == null) return;

        String currentUserId = FirebaseUtil.getCurrentUserId();
        List<String> newLikedBy = new ArrayList<>(model.getLikedBy());

        boolean wasLiked = newLikedBy.contains(currentUserId);
        if (wasLiked) {
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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get fresh position and model reference
                        int updatedPosition = holder.getAdapterPosition();
                        if (updatedPosition != RecyclerView.NO_POSITION) {
                            ChatMsgModel updatedModel = getItem(updatedPosition);
                            if (updatedModel != null) {
                                updatedModel.setLikedBy(newLikedBy);
                                updatedModel.setLikeCount(newLikedBy.size());
                                updateLikeUI(holder, updatedModel);
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show();
                        Log.e("LIKE_ERROR", "Like update failed", task.getException());
                    }
                });
    }

    private void updateLikeUI(ChatModelViewHolder holder, ChatMsgModel model) {
        int likeCount = model.getLikeCount();

        if (Objects.equals(model.getSenderId(), FirebaseUtil.getCurrentUserId())) {
            // Right side (sent messages)
            holder.rightLikeCount.setText(String.valueOf(likeCount));
            holder.rightLikeContainer.setVisibility(likeCount > 0 ? View.VISIBLE : View.GONE);
        } else {
            // Left side (received messages)
            holder.leftLikeCount.setText(String.valueOf(likeCount));
            holder.leftLikeContainer.setVisibility(likeCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatmsg_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        ImageView pfp;
        TextView leftMsg, rightMsg;
        LinearLayout leftLikeContainer, rightLikeContainer;
        TextView leftLikeEmoji, rightLikeEmoji;
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
            leftLikeEmoji = itemView.findViewById(R.id.left_like_emoji);
            rightLikeEmoji = itemView.findViewById(R.id.right_like_emoji);
            leftLikeCount = itemView.findViewById(R.id.left_like_count);
            rightLikeCount = itemView.findViewById(R.id.right_like_count);
        }
    }
}