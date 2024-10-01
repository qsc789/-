package com.day.ability;

import com.day.AIMain;
import com.day.config.Constants;
import com.day.service.IvwService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

// 麦克风传流听写

public class IatMic extends WebSocketListener {
    private static final String hostUrl = "https://iat-api.xfyun.cn/v2/iat"; //中英文，http url 不支持解析 ws/wss schema
    // private static final String hostUrl = "https://iat-niche-api.xfyun.cn/v2/iat";//小语种
    private static final String appid = ""; //在控制台-我的应用获取
    private static final String apiSecret = ""; //在控制台-我的应用-语音听写（流式版）获取
    private static final String apiKey = ""; //在控制台-我的应用-语音听写（流式版）获取

    //private static final String file = "./zMusic/pcm/科大讯飞.pcm"; // 中文
    public static final int StatusFirstFrame = 0;
    public static final int StatusContinueFrame = 1;
    public static final int StatusLastFrame = 2;
    public static final Gson json = new Gson();
    Decoder decoder = new Decoder();
    // 开始时间
    private static Date dateBegin = new Date();
    // 结束时间
    private static Date dateEnd = new Date();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
    static int status = 0;  // 音频的状态
    public static boolean IAT_FLAG = true;

    public static String fileName = "";

    public static void main(String[] args) throws Exception {
        iatWork();
    }


    static class MyThread extends Thread {
        public void run() {
           /* // 录制用户说话
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            long startTime1 = System.currentTimeMillis();
            long endTime1 = startTime1 + 500; // 10 seconds
            while (System.currentTimeMillis() < endTime1) {
                System.out.print("");
            }
            // Step 4: Start recording
            byte[] buffer = new byte[Constants.IVW_ASR_TARGET_DATA_LINE.getBufferSize() / 5]; // Adjust buffer size as needed
            int bytesRead;
            long startTime = System.currentTimeMillis();
            long endTime = startTime + 4000; // 10 seconds
            // Step 5: Loop until recording time reaches 10 seconds
            while (System.currentTimeMillis() < endTime) {
                bytesRead = Constants.IVW_ASR_TARGET_DATA_LINE.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            byte[] audioBytes = outputStream.toByteArray();
            // Step 9: Write byte array to audio file or other destination using AudioSystem.write method
            // Example: Save audioBytes to a WAV file
            try {
                File audioFile = new File("src/main/resources/1.wav");
                AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioBytes), Constants.IVW_ASR_AUDIO_FORMAT, audioBytes.length / Constants.IVW_ASR_AUDIO_FORMAT.getFrameSize());
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileName = "src/main/resources/1.wav";*/
            // 需要初始化的参数都在这里添加
            IatMic.IAT_FLAG = true;
            status = 0;
            // 结束初始化
            IatMic iatMic = new IatMic();
            // 构建鉴权url
            String authUrl = null;
            try {
                authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            OkHttpClient client = new OkHttpClient.Builder().build();
            //将url中的 schema http://和https://分别替换为ws:// 和 wss://
            String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
            // System.err.println(url);
            Request request = new Request.Builder().url(url).build();
            WebSocket webSocket = client.newWebSocket(request, iatMic);
        }
    }

    public static void iatWork() throws Exception {
        // 用线程方式启动，不影响唤醒，里面不要执行任何长时间的代码
        MyThread myThread = new MyThread();
        myThread.start();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        // System.out.println("建立连接成功");
        System.out.println(Constants.YELLOW_BACKGROUND + "机器人正在听，您请说：" + Constants.RESET);
        super.onOpen(webSocket, response);
        new Thread(() -> {
            //连接成功，开始发送数据
            //  int interval = 200;
            try {
                Constants.IVW_ASR_TARGET_DATA_LINE.open(Constants.IVW_ASR_AUDIO_FORMAT);
                Constants.IVW_ASR_TARGET_DATA_LINE.start();
                while (true) {
                    // System.err.println(AIMain.len + "" + AIMain.audioDataByteArray);
                    if (AIMain.len == -1) {
                        status = 2;// 标志读取完毕
                    }
                    switch (status) {
                        case StatusFirstFrame:   // 第一帧音频status = 0
                            JsonObject frame = new JsonObject();
                            JsonObject business = new JsonObject();  //第一帧必须发送
                            JsonObject common = new JsonObject();  //第一帧必须发送
                            JsonObject data = new JsonObject();  //每一帧都要发送
                            // 填充common
                            common.addProperty("app_id", appid);
                            //填充business
                            business.addProperty("language", "zh_cn");//
                            //business.addProperty("language", "en_us");//英文
                            //business.addProperty("language", "ja_jp");//日语，在控制台可添加试用或购买
                            //business.addProperty("language", "ko_kr");//韩语，在控制台可添加试用或购买
                            //business.addProperty("language", "ru-ru");//俄语，在控制台可添加试用或购买
                            //business.addProperty("ptt", 1);
                            business.addProperty("domain", "iat");
                            //mandarin中文普通话  广东话cantonese
                            business.addProperty("accent", "mandarin");//中文方言请在控制台添加试用，添加后即展示相应参数值cantonese//mandarin
                            //business.addProperty("nunum", 0);
                            //business.addProperty("ptt", 1);//标点符号
                            //business.addProperty("rlang", "zh-hk"); // zh-cn :简体中文（默认值）zh-hk :繁体香港(若未授权不生效，在控制台可免费开通)
                            business.addProperty("vinfo", 1);
                            business.addProperty("dwa", "wpgs");//动态修正(若未授权不生效，在控制台可免费开通)
                            business.addProperty("vad_eos", 3000);
                            //business.addProperty("fa_nbest", true);
                            //business.addProperty("fa_sch", true);
                            //business.addProperty("vinfo", 1);
                            //business.addProperty("speex_size", 70);
                            //business.addProperty("nbest", 5);// 句子多候选(若未授权不生效，在控制台可免费开通)
                            //business.addProperty("wbest", 3);// 词级多候选(若未授权不生效，在控制台可免费开通)
                            //填充data
                            data.addProperty("status", StatusFirstFrame);
                            data.addProperty("format", "audio/L16;rate=16000");
                            //data.addProperty("encoding", "speex-wb");
                            data.addProperty("encoding", "raw");
                            data.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(AIMain.audioDataByteArray, AIMain.len)));
                            //填充frame
                            frame.add("common", common);
                            frame.add("business", business);
                            frame.add("data", data);
                            // System.out.println("即将发送第一帧数据...");
                            // System.err.println(frame.toString());
                            webSocket.send(frame.toString());
                            status = StatusContinueFrame;  // 发送完第一帧改变status 为 1
                            break;
                        case StatusContinueFrame:  //中间帧status = 1
                            JsonObject frame1 = new JsonObject();
                            JsonObject data1 = new JsonObject();
                            data1.addProperty("status", StatusContinueFrame);
                            data1.addProperty("format", "audio/L16;rate=16000");
                            //data1.addProperty("encoding", "speex-wb");
                            data1.addProperty("encoding", "raw");
                            String temp = Base64.getEncoder().encodeToString(Arrays.copyOf(AIMain.audioDataByteArray, AIMain.len));
                            data1.addProperty("audio", temp);
                            frame1.add("data", data1);
                            //System.out.println(temp);
                            webSocket.send(frame1.toString());
                            break;
                    }
                    try {
                        Thread.sleep(200);
                        if (!IAT_FLAG) {
                            //System.out.println("本次会话结束");
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //说明读完了
                status = StatusLastFrame;
                JsonObject frame2 = new JsonObject();
                JsonObject data2 = new JsonObject();
                data2.addProperty("status", StatusLastFrame);
                data2.addProperty("audio", "");
                data2.addProperty("format", "audio/L16;rate=16000");
                //data2.addProperty("encoding", "speex-wb");
                data2.addProperty("encoding", "raw");
                frame2.add("data", data2);
                webSocket.send(frame2.toString());
                // System.err.println(frame2.toString());
                // System.out.println("all data is send");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // System.out.println(text);
        super.onMessage(webSocket, text);
        ResponseData resp = json.fromJson(text, ResponseData.class);
        if (resp != null) {
            if (resp.getCode() != 0) {
                AIMain.ivwFlag = true; // 如果报错也需要恢复唤醒
                System.out.println("code=>" + resp.getCode() + " error=>" + resp.getMessage() + " sid=" + resp.getSid());
                System.out.println("错误码查询链接：https://www.xfyun.cn/document/error-code");
                return;
            }
            if (resp.getData() != null) {
                if (resp.getData().getResult() != null) {
                    Text te = resp.getData().getResult().getText();
                    //System.out.println(te.toString());
                    try {
                        decoder.decode(te);
                        dateEnd = new Date();
                        // System.out.println("耗时:" + (dateEnd.getTime() - dateBegin.getTime()) + "ms");
                        System.out.println(Constants.YELLOW_BACKGROUND + "用户说话识别中：" + decoder.toString() + Constants.RESET);
                        //System.err.println("中间识别JSON结果 ----" + text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (resp.getData().getStatus() == 2) {
                    // todo  resp.data.status ==2 说明数据全部返回完毕，可以关闭连接，释放资源
                    //System.err.println("我的getStatus() == 2");
                    // System.out.println("session end ");
                    dateEnd = new Date();
                    // System.out.println(sdf.format(dateBegin) + "开始");
                    // System.out.println(sdf.format(dateEnd) + "结束");
                    //  System.out.println("耗时:" + (dateEnd.getTime() - dateBegin.getTime()) + "ms");
                    System.out.println(Constants.YELLOW_BACKGROUND + "用户说话识别最终结果：" + decoder.toString() + Constants.RESET);

                    AIMain.ivwFlag = true; // 恢复唤醒
                    // System.out.println("本次识别sid ==》" + resp.getSid());
                    try {
                        BigModelNew.doSpark(decoder.toString()); // 调用大模型回答问题！！！
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    decoder.discard();
                    webSocket.close(1000, "");
                    IatMic.IAT_FLAG = false;
                    // System.exit(0);
                } else {
                    // todo 根据返回的数据处理
                }
            }
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                System.out.println("onFailure code:" + code);
                System.out.println("onFailure body:" + response.body().string());
                if (101 != code) {
                    System.out.println("connection failed");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        //String date = format.format(new Date());
        //System.err.println(date);
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        //System.err.println(builder);
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        //System.err.println(sha);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        //System.err.println(authorization);
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();
        return httpUrl.toString();
    }

    public static class ResponseData {
        private int code;
        private String message;
        private String sid;
        private Data data;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return this.message;
        }

        public String getSid() {
            return sid;
        }

        public Data getData() {
            return data;
        }
    }

    public static class Data {
        private int status;
        private Result result;

        public int getStatus() {
            return status;
        }

        public Result getResult() {
            return result;
        }
    }

    public static class Result {
        int bg;
        int ed;
        String pgs;
        int[] rg;
        int sn;
        Ws[] ws;
        boolean ls;
        JsonObject vad;

        public Text getText() {
            Text text = new Text();
            StringBuilder sb = new StringBuilder();
            for (Ws ws : this.ws) {
                sb.append(ws.cw[0].w);
            }
            text.sn = this.sn;
            text.text = sb.toString();
            text.sn = this.sn;
            text.rg = this.rg;
            text.pgs = this.pgs;
            text.bg = this.bg;
            text.ed = this.ed;
            text.ls = this.ls;
            text.vad = this.vad == null ? null : this.vad;
            return text;
        }
    }

    public static class Ws {
        Cw[] cw;
        int bg;
        int ed;
    }

    public static class Cw {
        int sc;
        String w;
    }

    public static class Text {
        int sn;
        int bg;
        int ed;
        String text;
        String pgs;
        int[] rg;
        boolean deleted;
        boolean ls;
        JsonObject vad;

        @Override
        public String toString() {
            return "Text{" + "bg=" + bg + ", ed=" + ed + ", ls=" + ls + ", sn=" + sn + ", text='" + text + '\'' + ", pgs=" + pgs + ", rg=" + Arrays.toString(rg) + ", deleted=" + deleted + ", vad=" + (vad == null ? "null" : vad.getAsJsonArray("ws").toString()) + '}';
        }
    }

    //解析返回数据，仅供参考
    public static class Decoder {
        private Text[] texts;
        private int defc = 10;

        public Decoder() {
            this.texts = new Text[this.defc];
        }

        public synchronized void decode(Text text) {
            if (text.sn >= this.defc) {
                this.resize();
            }
            if ("rpl".equals(text.pgs)) {
                for (int i = text.rg[0]; i <= text.rg[1]; i++) {
                    this.texts[i].deleted = true;
                }
            }
            this.texts[text.sn] = text;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Text t : this.texts) {
                if (t != null && !t.deleted) {
                    sb.append(t.text);
                }
            }
            return sb.toString();
        }

        public void resize() {
            int oc = this.defc;
            this.defc <<= 1;
            Text[] old = this.texts;
            this.texts = new Text[this.defc];
            for (int i = 0; i < oc; i++) {
                this.texts[i] = old[i];
            }
        }

        public void discard() {
            for (int i = 0; i < this.texts.length; i++) {
                this.texts[i] = null;
            }
        }
    }
}