package com.bioland.app.utils;

import com.bioland.yt2glu.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class VoiceUtil {
	private SoundPool soundPool;
	private int music = 1;
	private Context context;

	public VoiceUtil(Context context) {
		super();
		this.context = context;
		initVoice();
	}

	/** 哪里要调用就执行这行代码 **/
	public void play_voice() {
		soundPool.play(music, 1, 1, 0, 0, 1);
	}

	/** 播放声音初始化 */
	public void initVoice() {
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);// 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
		music = soundPool.load(context, R.raw.beep, 1); // 把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
	}
}
