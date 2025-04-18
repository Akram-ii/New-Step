package com.example.newstep.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
    private static final int DOUBLE_CLICK_DELAY = 300;
    private static final float MESSAGE_LIFT_VALUE = -16f;
    private static final int ANIMATION_DURATION = 200;

    private final Context context;
    private final String chatroomId;
    private long lastClickTime = 0;
    private final Handler clickHandler = new Handler(Looper.getMainLooper());
    private Runnable clickRunnable;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMsgModel> options, Context context, String chatroomId) {
        super(options);
        this.context = context;
        this.chatroomId = chatroomId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMsgModel model) {

        if (Objects.equals(model.getSenderId(), FirebaseUtil.getCurrentUserId())) {
            showRightMessage(holder, model);
        } else {
            showLeftMessage(holder, model);
        }

        updateLikeUI(holder, model);
        updateDateVisibility(holder, model);


        View.OnClickListener clickListener = v -> handleMessageClick(holder, position);
        holder.leftChatLayout.setOnClickListener(clickListener);
        holder.rightChatLayout.setOnClickListener(clickListener);

        holder.rightChatLayout.setOnLongClickListener(v -> {
            showDeleteDialog(model);
            return true;
        });
    }

    private void showRightMessage(ChatModelViewHolder holder, ChatMsgModel model) {
        holder.leftChatLayout.setVisibility(View.GONE);
        holder.pfp.setVisibility(View.GONE);
        holder.rightChatLayout.setVisibility(View.VISIBLE);
        holder.rightMsg.setText(model.getMessage());
        holder.leftLikeContainer.setVisibility(View.GONE);
        holder.leftDateTime.setVisibility(View.GONE);
    }

    private void showLeftMessage(ChatModelViewHolder holder, ChatMsgModel model) {
        FirebaseUtil.loadPfp(model.getSenderId(), holder.pfp);
        holder.leftChatLayout.setVisibility(View.VISIBLE);
        holder.rightChatLayout.setVisibility(View.GONE);
        holder.leftMsg.setText(model.getMessage());
        holder.rightLikeContainer.setVisibility(View.GONE);
        holder.rightDateTime.setVisibility(View.GONE);
    }

    private void updateDateVisibility(ChatModelViewHolder holder, ChatMsgModel model) {
        if (model.isDateVisible()) {
            if (holder.leftChatLayout.getVisibility() == View.VISIBLE) {
                holder.leftDateTime.setVisibility(View.VISIBLE);
                holder.leftDateTime.setText(Utilities.formatTimestamp(model.getTimestamp()));
                holder.leftChatLayout.setTranslationY(MESSAGE_LIFT_VALUE);
            } else {
                holder.rightDateTime.setVisibility(View.VISIBLE);
                holder.rightDateTime.setText(Utilities.formatTimestamp(model.getTimestamp()));
                holder.rightChatLayout.setTranslationY(MESSAGE_LIFT_VALUE);
            }
        } else {
            holder.leftDateTime.setVisibility(View.GONE);
            holder.rightDateTime.setVisibility(View.GONE);
            holder.leftChatLayout.setTranslationY(0);
            holder.rightChatLayout.setTranslationY(0);
        }
    }

    private void handleMessageClick(ChatModelViewHolder holder, int position) {
        long currentTime = System.currentTimeMillis();
        ChatMsgModel model = getItem(position);

        if (model == null) return;

        if (clickRunnable != null) {
            clickHandler.removeCallbacks(clickRunnable);
        }

        if (currentTime - lastClickTime < DOUBLE_CLICK_DELAY) {
            lastClickTime = 0;
            performLikeAction(holder, position);
        } else {
            lastClickTime = currentTime;
            clickRunnable = () -> {
                model.toggleDateVisibility();
                notifyItemChanged(position);
            };
            clickHandler.postDelayed(clickRunnable, DOUBLE_CLICK_DELAY);
        }
    }

    private void performLikeAction(ChatModelViewHolder holder, int position) {
        ChatMsgModel model = getItem(position);
        if (model == null || model.getMessageId() == null) return;

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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        model.setLikedBy(newLikedBy);
                        model.setLikeCount(newLikedBy.size());
                        notifyItemChanged(position);
                    }
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
        if (model.getMessageId() == null) return;

        FirebaseUtil.getChatroomMsgRef(chatroomId)
                .document(model.getMessageId())
                .delete();
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatmsg_recycler_row, parent, false);
        view.setBackgroundResource(android.R.color.transparent);
        return new ChatModelViewHolder(view);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        clickHandler.removeCallbacksAndMessages(null);
    }

    static class ChatModelViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        ImageView pfp;
        TextView leftMsg, rightMsg;
        LinearLayout leftLikeContainer, rightLikeContainer;
        TextView leftLikeCount, rightLikeCount;
        TextView leftDateTime, rightDateTime;

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
            leftDateTime = itemView.findViewById(R.id.left_date_time);
            rightDateTime = itemView.findViewById(R.id.right_date_time);
        }
    }
}

