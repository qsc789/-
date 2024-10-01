package com.day.ability;


import com.day.AIMain;
import com.day.util.MyUtil;
import com.google.gson.Gson;
import okhttp3.HttpUrl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

// 主函数入口
public class BigModelNew {
    public static final String hostUrl = "https://spark-api.xf-yun.com/v3/completions";
    private static final String appid = "";
    private static final String apiSecret = "";
    private static final String apiKey = "";

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        doSpark("我想吃鸡。");
    }

    public static void doSpark(String content) throws Exception {
        MyThread myThread = new MyThread(content);
        myThread.start();
    }

    static class MyThread extends Thread {
        String content;

        public MyThread(String content) {
            this.content = content;
        }

        public void run() {
            String authUrl = null;
            try {
                authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // URL地址正确
            // System.err.println(authUrl);
            String json = "{\n" + "  \"app_id\": \"" + appid + "\",\n" + "  \"uid\": \"" + UUID.randomUUID().toString().substring(0, 10) + "\",\n" + "  \"domain\": \"generalv2\",\n" + "  \"temperature\": 0.5,\n" + "  \"max_tokens\": 4096,\n" + "  \"auditing\": \"default\",\n" + "  \"stream\": true,\n" + "  \"messages\": [\n" + "    {\n" + "      \"role\": \"user\",\n" + "     \"content\": \"" + content + "\"\n" + "    }\n" + "  ]\n" + "}";
            // 发起Post请求
            String res = MyUtil.doPostJson(authUrl, null, json);
            String finalRes = "";
            String[] resArray = res.split("\n");
            for (int i = 0; i < resArray.length; i++) {
                if (resArray[i].contains("data:")) {
                    String jsonStr = resArray[i].replace("data:", "");
                    BigJsonParse bigJsonParse = gson.fromJson(jsonStr, BigJsonParse.class);
                    List<Choices> choicesList = bigJsonParse.choices;
                    if (choicesList != null && choicesList.size() > 0) {
                        for (Choices choice : choicesList) {
                            finalRes = finalRes + choice.content;
                        }
                    } else {
                        finalRes = "您好，我是讯飞星火认知大模型";
                    }
                }
            }
            System.out.println(finalRes);
            String temp = finalRes.replaceAll("\r\n", "").replaceAll("\n", "");
            System.out.println("*****************************************************************************************************");
            AIMain.startTts(temp);
        }
    }

    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // date="Thu, 12 Oct 2023 03:05:28 GMT";
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "POST " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }
}

// JSON
class BigJsonParse {
    List<Choices> choices;
}

class Choices {
    String content;
}
