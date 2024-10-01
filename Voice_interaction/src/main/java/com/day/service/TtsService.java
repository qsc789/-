package com.day.service;

import com.day.config.Constants;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface TtsService extends Library {
    /**
     * 重点：
     * 1.char *   对应  String
     * 2.int *    对应  IntByReference
     * 3.void *   对应  byte[]/Pointer，回调函数里此类型需用String来对应。
     * 4.int      对应  int
     * 5.无参     对应  void
     * 6.回调函数  对应  根据文档自定义回调函数，实现接口Callback，离线语音合成无回调
     */
    //加载dll动态库并实例化,从而使用其内部的方法
    TtsService INSTANCE = Native.loadLibrary(Constants.TTS_DLL_PATH, TtsService.class);

    //定义登录方法
    public Integer MSPLogin(String usr, String pwd, String params);

    //开始一次普通离线语音合成
    public String QTTSSessionBegin(String params, IntByReference errorCode);

    //写入需要合成的文本
    public Integer QTTSTextPut(String sessionID, String textString, int textLen, String params);

    //获取离线合成的音频
    public Pointer QTTSAudioGet(String sessionID, IntByReference audioLen, IntByReference synthStatus, IntByReference errorCode);

    //结束本次普通离线语音合成
    public Integer QTTSSessionEnd(String sessionID, String hints);

    //定义退出方法
    public Integer MSPLogout();
}
