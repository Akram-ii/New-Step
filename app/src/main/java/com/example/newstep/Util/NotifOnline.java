package com.example.newstep.Util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotifOnline {

    private  String userFCMToken,title,body;
    private  String postUrl="https://fcm.googleapis.com/v1/projects/new-step-app/messages:send";
    private Context context;

    public NotifOnline() {

    }

    public NotifOnline(String userFCMToken, String title, String body, Context context) {
        this.userFCMToken = userFCMToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }

    public void sendNotif(){
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        JSONObject mainbObj=new JSONObject();
        try{
            JSONObject messageObj=new JSONObject();
            JSONObject notifObj=new JSONObject();
            notifObj.put("title",title);
            notifObj.put("body",body);

            messageObj.put("token",userFCMToken);
            messageObj.put("notification",notifObj);

            mainbObj.put("message",messageObj);
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,postUrl,mainbObj, response -> {

            },volleyError->{

            }){
                @NonNull
                @Override
                public Map<String,String> getHeaders(){
                    AccessToken accessToken=new AccessToken();
                    String accessKey=accessToken.getAccessToken();
                    Map<String,String> header=new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","Bearer "+accessKey);
                    return header;

                };
            };
            requestQueue.add(request);
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public String getUserFCMToken() {
        return userFCMToken;
    }

    public void setUserFCMToken(String userFCMToken) {
        this.userFCMToken = userFCMToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
