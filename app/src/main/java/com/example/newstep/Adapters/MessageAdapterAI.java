package com.example.newstep.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Models.MessageAI;
import com.example.newstep.R;
import com.example.newstep.Util.Utilities;

import java.util.List;

public class MessageAdapterAI extends RecyclerView.Adapter<MessageAdapterAI.MyViewHolder>{
List<MessageAI> messageList;

    public MessageAdapterAI(List<MessageAI> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_ai_row,null);
        MyViewHolder myViewHolder=new MyViewHolder(chatView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
MessageAI message=messageList.get(position);
if(message.getSentBy()==MessageAI.SENT_BY_ME){
    holder.leftChatLayout.setVisibility(View.GONE);
    holder.rightChatLayout.setVisibility(View.VISIBLE);
    holder.rightMsg.setText(message.getMessage());
}else{
    holder.leftChatLayout.setVisibility(View.VISIBLE);
    holder.rightChatLayout.setVisibility(View.GONE);
    holder.leftMsg.setText(Utilities.formatMarkdown(message.getMessage()));
}
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        ImageView pfp;
        TextView leftMsg, rightMsg;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            pfp = itemView.findViewById(R.id.pfp);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftMsg = itemView.findViewById(R.id.left_chat_textView);
            rightMsg = itemView.findViewById(R.id.right_chat_textView);
        }

    }

}
