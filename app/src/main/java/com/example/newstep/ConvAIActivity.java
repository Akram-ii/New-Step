package com.example.newstep;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstep.Adapters.MessageAdapterAI;
import com.example.newstep.Models.MessageAI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ConvAIActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageButton sendMSG, back;
    TextView intr;
    EditText msg;
    List<MessageAI> messageList;
    MessageAdapterAI adapter;
    List<JSONObject> conversationHistory;
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conv_aiactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EdgeToEdge.enable(this);
        msg = findViewById(R.id.msg_EditText);
        recyclerView = findViewById(R.id.msgsRecyclerView);
        back = findViewById(R.id.back_ImageButton);
        sendMSG = findViewById(R.id.send_ImageButton);
        messageList = new ArrayList<>();
        adapter = new MessageAdapterAI(messageList);
        intr = findViewById(R.id.intr);
        recyclerView.setAdapter(adapter);
        conversationHistory=new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        sendMSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtMsg = msg.getText().toString();
                if (txtMsg.isEmpty()) {
                    Toast.makeText(ConvAIActivity.this, "You need to write something", Toast.LENGTH_SHORT).show();
                } else {
                    addToChat(txtMsg, MessageAI.SENT_BY_ME);
                    intr.setVisibility(View.GONE);
                    msg.setText("");
                CallApi(txtMsg);
                }
            }
        });

    }

    public void addToChat(String msg, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new MessageAI(msg, sentBy));
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });
    }
public void addResponse(String msg){
        messageList.remove(messageList.size()-1);
        addToChat(msg,MessageAI.SENT_BY_BOT);
}
    public void CallApi(String msg) {
        messageList.add(new MessageAI("Typing...", MessageAI.SENT_BY_BOT));

        // Add system message only once
        if (conversationHistory.isEmpty()) {
            try {
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", "You are an AI assistant named Milo in the app called Alter.Alter is a social media app for people with bad habits/addictions,be friendly,helpful and motivatiing" +
                        "Never say you're from DeepSeek or mention the model you are based on.");
                conversationHistory.add(systemMsg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Add user message
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", msg);
            conversationHistory.add(userMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Convert conversation history to JSON array
        JSONArray messageArr = new JSONArray(conversationHistory);

        // Create request JSON
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "deepseek/deepseek-chat-v3-0324:free");
            jsonObject.put("messages", messageArr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer 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")
                .header("HTTP-Referer", "https://newstep.app")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim());

                        // Add assistant response to conversation history
                        JSONObject botReply = new JSONObject();
                        botReply.put("role", "assistant");
                        botReply.put("content", result.trim());
                        conversationHistory.add(botReply);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    addResponse("Failed to respond due to " + response.body().string());
                }
            }
        });
    }


}