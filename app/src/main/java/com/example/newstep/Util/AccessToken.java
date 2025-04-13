package com.example.newstep.Util;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    private static final String FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    public String getAccessToken(){
        try{
            String jsonString="{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"new-step-app\",\n" +
                    "  \"private_key_id\": \"71370e7335199a8d562d6474dc4d4001a69f36f7\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCutDuPH1yEqnhV\\nPsxBtpGqnMN3tYO50ZdA5Xhr4lOwRNWj14PHK17ERgOuxjJExL+GBjnf9Jy8wrl0\\ny/4ExPUwrCM2s0P8+2FSC2nlAhAyTQZvOtazOsIsGP8WKekbRtTUzsKH4qUP30Hx\\n93YT54jpzqnJy39r8QjR+AxRIsCwkXizUWQVsFsOcW0HvN9zdlA041VgLivFt3xk\\nLLY/nQXf4/fLhAaX7tcixSaq/gEC1RpL2UZcuac7YR90tku/VEBTtUIQPEviG212\\n5yfj/fdeJxnMsRiH7Sgl9QRVgRdepeERtY/qm1k3TIjuM4gI1LE7BKTbwevPbrje\\n1kcOeJMnAgMBAAECggEAIKe7wkLgbkPW/kggsd/IVpW6y7N4DdGpbg32dmP1tKsJ\\nK/jqvt3SdumHYrHVT5lfG1QqmTwF2Bqlw7kloAqYsiJa6qyTdsQeKtHLV20NTfmr\\nzDq5SwMerFN3ytLGqbyRp72GCxRIOjFHCE1zGsFajCl+/yM0jB6fwW+WDcxrLBWV\\n4vbUY0T+y5eWUb69IWbpFtPbPiV+x+98ecJHzXOOw03IzpmbaCzIroTReTrD+4ci\\nkX7x+b46jsYWGFAw0EQJ+DBlCHA6hApoA+/NL8mynM5YcuVuq8Js3Typ2tt5aR10\\nTin/tRLuAIehlXKBMGMaQ1TDF0TuvyfuGWjdtdFPIQKBgQDWSaWJA3WOENvUXcD8\\nwSxqRJkhxresQCgPul4I2zFXY26OqeVGCjBU8UkAurguKuezVhoZafX/KGuMTQMy\\n6ioVWJfggHSgJejD428BPFqxHtsv/Lj3x6CewN/VSRwQXUn4THK//EXE0B+efzsS\\nQRuzmxsxPmePepBKAVXT2upuPwKBgQDQtg8tKASsdgg+kOJfKWf7IneMdSB2dsJI\\nENw6dYdN4Lcx8+MBN25+R3Ax2XJ6Zaz+H2K76vG1mFEMRXPLYBOX2sHSj3YGAjqE\\n1IymOXywOy42ReDF51rxeUexNf7fXtaiWjccsAuJYVTpfc5mbCREavoT3AveGyJz\\nWeh05pZxGQKBgFnG1xx1H44akA8EDHOYdwQ6ozFccN0wr9gPC5CITL7BALOyiET0\\nxdUAlcZBdCKbhSFEZIDbPN3e0Fgid8GgC2uQB2leR/5Eg6V2Jq6M264zMD+FkPT2\\nZNu+EMjp7WEJxJDLbpPynERdsbGUumnmbzWrORRDgd3ByqvGrW+p2ue/AoGBALJn\\nLUnYL0aMDjgM+LOHXU6aA76YUAtzMFcRsm52EBZNw8p7QsvDajufLRQvX1+okdsX\\nUPp4tewYKw9mPrjVg2oGoC8wWh6AwSzR0vgzo5WwwKu7x4rjXCWVGecs1JRzKNzW\\naM2o3fOsszxzQX7jaYziNKnNKofMggJAFCf5AxfhAoGBAMqNIOKzdaS1caZyCdPu\\nbij7EajIwyQwfmA7Xw6Keo48mTWTSd6djtnXeKbCeMJ1I8MyyGUjKCeCAwXtDmGs\\ngM1l8XfUIXH5InH8Z0MedYMieztwUfGgkAhN0O2YXi6PcaXZQjK3VHMgdANyfUMJ\\nst8eGdAaTjShI7qPOkxFRMW1\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-fbsvc@new-step-app.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"105080291395589552256\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40new-step-app.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";
            InputStream stream=new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials=GoogleCredentials.fromStream(stream).createScoped(Lists.newArrayList(FIREBASE_MESSAGING_SCOPE));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            Log.e("error", "getAccessToken: "+e.getMessage() );
            return null;
        }
    }
}
