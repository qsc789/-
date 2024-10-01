package com.day.service.imp;

import com.day.AIMain;
import com.day.ability.IatMic;
import com.day.config.Constants;
import com.sun.jna.Callback;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class IvwCallback implements Callback {
    public int cb_ivw_msg_proc(String sessionID, int msg, int param1, int param2, String info, String userData) throws Exception {
        System.out.println("机器人二狗：主人，您请说~");
        AIMain.startTts("主人，您请说~");
        // 先录音后调用听写
        IatMic.iatWork();
        return 0;
    }
}

