package com.day.config;

import com.sun.jna.ptr.IntByReference;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class Constants {
    // 构造16K 16BIT 单声道音频
    public static final String APPID = "5e11538f";  // APPID
    public static final String WORK_DIR = "src/main/resources";

    // 1、唤醒相关  ssb_param，一定注意IVW_SSB_PARAMS的fo|xxx资源的路径，xxx取值是指WORK_DIR目录下/msc/xxx   xxx是以后的路径开始拼接的！！！！！！！！！！！
    public static final AudioFormat IVW_ASR_AUDIO_FORMAT = new AudioFormat(16000F, 16, 1, true, false);
    public static final String IVW_DLL_PATH = "src/main/resources/ivw_msc_x64.dll"; // windows动态库路径
    public static final String IVW_LOGIN_PARAMS = "appid = " + APPID + ", work_dir = " + WORK_DIR;
    public static final String IVW_SSB_PARAMS = "ivw_threshold=0:1500,sst=wakeup,ivw_shot_word=1,ivw_res_path =fo|res/ivw/wakeupresource.jet";
    public static IntByReference IVW_ERROR_CODE = new IntByReference(-100);
    public static Integer IVW_FRAME_SIZE = 6400;  // 一定要每200ms写10帧,否则会出现唤醒一段时间后无法唤醒的问题，一帧的大小为640B,其他大小可能导致无法唤醒。
    public static Integer IVW_AUDIO_STATUS = 1;
    public static DataLine.Info IVW_ASR_DATA_LINE_INFO = new DataLine.Info(TargetDataLine.class, IVW_ASR_AUDIO_FORMAT);
    public static TargetDataLine IVW_ASR_TARGET_DATA_LINE; // 录音

    static {
        try {
            IVW_ASR_TARGET_DATA_LINE = (TargetDataLine) AudioSystem.getLine(IVW_ASR_DATA_LINE_INFO);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 2、合成相关
    public static final AudioFormat TTS_AUDIO_FORMAT = new AudioFormat(16000F, 16, 1, true, false);
    public static final String TTS_DLL_PATH = "src/main/resources/tts_msc_x64.dll"; // windows动态库路径
    public static final String TTS_LOGIN_PARAMS = "appid = " + APPID + ", work_dir = " + WORK_DIR;
    public static final String TTS_SESSION_BEGIN_PARAMS = "engine_type = local, voice_name = xiaoyuan, text_encoding = UTF8," + " tts_res_path = fo|res/tts/xiaoyuan.jet;fo|res/tts/common.jet, sample_rate = 16000, speed = 50, volume = 50, pitch = 50, rdn = 2";
    public static IntByReference TTS_ERROR_CODE = new IntByReference(-100);
    public static IntByReference TTS_AUDIO_LEN = new IntByReference(-100);
    public static IntByReference TTS_SYNTH_STATUS = new IntByReference(-100);
    public static String TTS_TEXT; // 合成文本
    public static Integer TTS_TOTAL_AUDIO_LENGTH; // 合成音频长度
    public static ByteArrayOutputStream TTS_BYTE_ARRAY_OUTPUT_STREAM; // 合成音频流
    public static DataLine.Info TTS_DATA_LINE_INFO = new DataLine.Info(SourceDataLine.class, TTS_AUDIO_FORMAT, AudioSystem.NOT_SPECIFIED);
    public static SourceDataLine TTS_SOURCE_DATA_LINE; // 播放

    static {
        try {
            TTS_SOURCE_DATA_LINE = (SourceDataLine) AudioSystem.getLine(Constants.TTS_DATA_LINE_INFO);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static final String YELLOW_BACKGROUND = "\u001B[43m"; // ANSI code for yellow background
    public static final String RESET = "\u001B[0m"; // ANSI code to reset to default
}
