package com.example.newstep;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ConvAIActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageButton sendMSG, back;
    TextView intr,milo;
    ImageView pfp;
    EditText msg;
    List<MessageAI> messageList;
    MessageAdapterAI adapter;
    List<JSONObject> conversationHistory;
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conv_aiactivity);

        msg = findViewById(R.id.msg_EditText);
        milo = findViewById(R.id.milo);
        pfp = findViewById(R.id.pfp);
        pfp.setOnClickListener(v->{


            setupBottomDialog(getString(R.string.milo_title),getString(R.string.milo_desc));
        });
        milo.setOnClickListener(v->{
            setupBottomDialog(getString(R.string.milo_title),getString(R.string.milo_desc));
        });
        recyclerView = findViewById(R.id.msgsRecyclerView);
        back = findViewById(R.id.back_ImageButton);
        sendMSG = findViewById(R.id.send_ImageButton);
        messageList = new ArrayList<>();
        back.setOnClickListener(v->{onBackPressed();});
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
                SharedPreferences langPrefs = getSharedPreferences("Language", Context.MODE_PRIVATE);
                String savedLang = langPrefs.getString("lang", "en");
                if(savedLang.equals("en")){
                    systemMsg.put("content", "You are an AI assistant named Milo in the app called Alter. " +
                            "Alter is a social media app for people with bad habits/addictions. Be friendly, helpful, and motivating. " +
                            "Never say you're from Llama or mention the model you are based on.");
                }else if (savedLang.equals("fr")){
                    systemMsg.put("content", "Tu es un assistant IA nommé Milo dans l'application appelée Alter. " +
                            "Alter est une application de réseau social pour les personnes ayant de mauvaises habitudes ou des addictions. Sois amical, serviable et motivant. " +
                            "Ne dis jamais que tu viens de Llama ni ne mentionne le modèle sur lequel tu es basé.");
                }else{
                    systemMsg.put("content", "أنت مساعد ذكي يُدعى ميلو في تطبيق يُدعى Alter " +
                            ". Alter هو تطبيق تواصل اجتماعي مخصص للأشخاص الذين يعانون من عادات سيئة أو إدمان. كُن ودودًا، مُعينًا، ومُحفزًا" +
                            "لا تذكر أبدًا أنك من Llama أو تشير إلى النموذج الذي تم تدريبك عليه..");
                }
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
            jsonObject.put("model", "llama-3.3-70b-versatile");
            jsonObject.put("messages", messageArr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resBody = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(resBody);

                    if (jsonObject.has("choices")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        addResponse(result.trim());

                        // Add assistant reply to conversation
                        JSONObject botReply = new JSONObject();
                        botReply.put("role", "assistant");
                        botReply.put("content", result.trim());
                        conversationHistory.add(botReply);

                    } else if (jsonObject.has("error")) {
                        String err = jsonObject.getJSONObject("error")
                                .optString("message", "Unknown error");
                        addResponse("API Error: " + err);
                    } else {
                        addResponse("Unexpected response format: " + resBody);
                    }
                } catch (JSONException e) {
                    addResponse("JSON parse error: " + e.getMessage());
                }
            }
        });
    }
    public void setupBottomDialog(String title,String desc){
        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
        View view1= LayoutInflater.from(this).inflate(R.layout.bottom_dialog,null);
        bottomSheetDialog.setContentView(view1);
        TextView title1=view1.findViewById(R.id.title),desc1=view1.findViewById(R.id.desc);
        title1.setText(title);
        desc1.setText(desc);
        bottomSheetDialog.show();

    }

}