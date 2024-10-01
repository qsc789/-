package com.day;


import com.day.config.Constants;
import com.day.service.IvwService;
import com.day.service.TtsService;
import com.day.service.imp.IvwCallback;
import com.sun.jna.Pointer;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

// 主函数入口
public class AIMain {
    public static boolean ttsFlag = false;
    public static boolean ivwFlag = false;

    public static byte[] audioDataByteArray;

    public static int len;

    public static void main(String[] args) throws Exception {
        // 调用流程：唤醒--->
        // System.out.println(Constants.yellowBackground + "呼叫大飞" + Constants.reset);
        // 以线程的方式启动唤醒
        MyThread myThread = new MyThread();
        myThread.start();
    }

    static class MyThread extends Thread {
        public void run() {
            startIvw();
        }
    }
}
