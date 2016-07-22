/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.bioland.yt2glu;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.bioland.app.utils.LogUtil;
import com.bioland.app.utils.VoiceUtil;
import com.miracle.db.bean.GluValue;
import com.miracle.db.dao.GluValueDao;
import com.miracle.widget.dialog.PermissionPasswordDialog;
import com.miracle.widget.json.service.JsonServiceGet;
import com.miracle.widget.json.service.JsonServiceSend;
import com.miracle.widget.sharedpreferences.MyPreference;
import com.miracle.widget.telephonymanager.MyTelephonyManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiveActivity extends SerialPortActivity implements OnClickListener {

	private CheckBox mCheckBoxBlood, mCheckBoxValue, mCheckBoxResistance, mCheckBoxBiochemistry;
	private ImageView mImageViewStar;
	private ImageView mImageViewPaper;
	private static final String TAG = "ReceiveActivity";
	private Unpack myUnpack;
	private static String dataBlood = "5C0304";// 血糖模式
	private static String dataValue = "5C0315";// 值控模式
	private static String dataResistance = "5C0326";// 电阻片模式（无温补）
	private static String sleepBlood = "5C0001";// 血糖模式
	private static String sleepValue = "5C0102";// 值控模式
	private static String sleepResistance = "5C0203";// 电阻片模式（无温补）
	private static int measureMode = 0;// 0:血糖模式；1：值控模式;2:电阻片模式;3:生化模式
	private boolean isKeyDown = false;
	private boolean isBlink = false; // 星星闪烁的标志位
	private boolean isGluMeasuring = false; // 是否正在测量血糖

	private ThreadBlinkStar mThreadStar = null;
	private LinearLayout layoutVersion3;
	private TextView mTextViewVersion3Value;
	private float mResult;
	private float mResultX, mResultZ, mResultF;

	// 数据库参数
	private GluValueDao mGluValueDao;
	// SharedPreferences存储
	private MyPreference mPreference;
	// TelephonyManager参数
	private MyTelephonyManager mTelephony;
	// dialog参数
	private PermissionPasswordDialog mPermissionDialog;
	private boolean isShowDialog = false; // 是否正在显示dialog

	// 数据上传服务器相关参数
	private JsonServiceGet mJsonServiceGet;
	private JsonServiceSend mJsonServiceSend;
	private String MachineCode;
	private int measureCount = 0;// 测量次数
	private int netMeasureCount = 0;// 服务器上的最大count
	private boolean isNetOk = false;// 网络是否连接
	private ThreadSendServiceData mThreadSendData = null;
	public static boolean isFirst = true;// 软件是否第一次运行
	// 声音相关参数
	private VoiceUtil mVoiceUtil;
	private int mVoiceCount = 0;// 声音播放定时器，当值为n是播放声音

	/**
	 * 调用此活动的类需要用此方法调用此活动
	 * 
	 * @param context
	 * @param data1
	 */
	public static void actionStart(Context context, int data1) {
		Intent intent = new Intent(context, ReceiveActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receive);
		myUnpack = Unpack.getInstance(ReceiveActivity.this);

		initView();
		// 初始化网络状态参数
		initIntentFilter();
		// 初始化服务器参数
		initJsonService();
		// 初始化数据库
		initDB();
		// 初始化Telephony
		initTelephony();
		// 初始化dialog
		initDialog();
		// 初始化声音
		initVoice();
	}

	private void initView() {
		mCheckBoxBlood = (CheckBox) findViewById(R.id.mode_check_blood);
		mCheckBoxValue = (CheckBox) findViewById(R.id.mode_check_value);
		mCheckBoxResistance = (CheckBox) findViewById(R.id.mode_check_resistance);
		mCheckBoxBiochemistry = (CheckBox) findViewById(R.id.mode_check_biochemistry);

		mImageViewStar = (ImageView) findViewById(R.id.img_star);

		mImageViewPaper = (ImageView) findViewById(R.id.img_test_paper);

		mCheckBoxBlood.setOnClickListener(this);
		mCheckBoxValue.setOnClickListener(this);
		mCheckBoxResistance.setOnClickListener(this);
		mCheckBoxBiochemistry.setOnClickListener(this);

		layoutVersion3 = (LinearLayout) findViewById(R.id.layout_yt2_version3);
		mTextViewVersion3Value = (TextView) findViewById(R.id.txt_version3_value);
	}

	// 初始化数据库和SharedPreferences
	private void initDB() {
		mGluValueDao = new GluValueDao(this);
		mPreference = MyPreference.instance(this);
		measureCount = mPreference.getInt(MyPreference.MEASURE_COUNT, 0);
		isFirst = mPreference.getBoolean(MyPreference.IS_FIRST, true);
	}

	// 初始化网络状态参数
	private void initIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectionReceiver, intentFilter);
	}

	// 初始化服务器参数
	private void initJsonService() {
		MachineCode = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		LogUtil.e(TAG, "Machine===" + MachineCode);
		mJsonServiceGet = new JsonServiceGet();
		mJsonServiceSend = new JsonServiceSend();
	}

	// 初始化Telephony
	private void initTelephony() {
		mTelephony = new MyTelephonyManager(this);
	}

	// 初始化dialog
	private void initDialog() {
		mPermissionDialog = new PermissionPasswordDialog(this);
	}

	// 初始化声音
	private void initVoice() {
		mVoiceUtil = new VoiceUtil(this);
	}

	/**
	 * 串口接收数据
	 */
	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				int i = 0, j = 0;
				byte[] buf = buffer;
				// 打印接收到的数据
				// for (int k = 0; k < buf.length; k++) {
				// if (buf[k] != 0)
				// LogUtil.e(TAG, "156 buf[" + k + "]=" +
				// String.valueOf(buf[k]));
				// }
				while (i < size) {
					if (buf[j] == 53) {
						buf = myUnpack.bufShifting(buffer, size, j);
						j = 0;
						// 判断是否为true，解决没有收到有效数据但是收到53时，重复发送信号
						if (myUnpack.setData(buf)) {
							Message msg = new Message();
							msg.what = myUnpack.step;
							mHandler.sendMessage(msg);
						}
					}
					i++;
					j++;
				}
			}
		});
	}

	// 星星闪烁线程
	class ThreadBlinkStar extends Thread {
		@Override
		public void run() {
			// 控制星星闪烁
			while (isBlink) {
				Message msg = new Message();
				msg.what = Unpack.GAIN_STARBLINK;
				mHandler.sendMessage(msg);
				try {
					Thread.sleep(500);
					mVoiceCount++;
					if (mVoiceCount == 4) {
						// 播放声音
						mVoiceUtil.play_voice();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			mThreadStar = null;
		}
	}

	// 上传数据到服务器线程
	class ThreadSendServiceData extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			netMeasureCount = JsonServiceGet.backData;
			LogUtil.e(TAG + 249, "netMeasureCount = " + JsonServiceGet.backData);
			// 解决机器第一次安装软件,因为服务器没有对应机器码，所以 netMeasureCount=-1 导致的上传问题
			// 如果软件在这台机子运行过，卸载后安装，又是第一次运行，则netMeasureCount将它赋0值
			if ((isFirst) && (netMeasureCount != -2)) {
				LogUtil.e(TAG + 254, "软件第一次运行，赋0值");
				netMeasureCount = 0;
			}
			while (isNetOk) {
				while (isGluMeasuring) {
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 必须初始化JsonServiceSend.backData，否则在发送数据中途断网，因为此处break，将不再发送数据,在退出线程时初始化
				// 如果接收码JsonServiceGet.backData错误，则退出线程
				if ((netMeasureCount == -2) || (netMeasureCount == -1) || (JsonServiceSend.backData == -2)
						|| (JsonServiceSend.backData == -1)) {
					break;
				} else if ((measureCount > netMeasureCount) && (measureCount > 0)) {
					saveNetJsonService();
				} else {
					// 如果measureCount <= netMeasureCount 表示发送完成，退出线程
					break;
				}
			}
			LogUtil.e(TAG + 273, "exit thread...");
			JsonServiceSend.backData = 0;
			mThreadSendData = null;
		}
	}

	// 收到数据后对应处理
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Unpack.GAIN_INSERT:
				isGluMeasuring = true;// 插入试纸测量开始
				// 初始化isShowDialog
				if (mPermissionDialog.isRightPassword) {
					isShowDialog = false;
				}
				// 不重复显示密码dialog
				if (!isShowDialog) {
					showDialog();
				}
				break;
			case Unpack.GAIN_GETSN:
				Toast.makeText(ReceiveActivity.this, "sn:" + myUnpack.sn, Toast.LENGTH_SHORT).show();
				break;
			// 获取倒计时
			case Unpack.GAIN_COUNTDOWN:
				// 开始滴血测量就打开线程，使star闪烁
				if (mThreadStar == null) {
					mThreadStar = new ThreadBlinkStar();
				}
				if (!mThreadStar.isAlive()) {
					isBlink = true;
					mThreadStar.start();
				}
				break;
			// 收到读取模式命令，则发送当前模式数据
			case Unpack.GAIN_GETMODE:
				// 收到读取模式命令，则发送当前模式数据
				// sendDataMode();
				break;
			// 收到测量完成数值，发送命令，休眠血糖小板
			case Unpack.GAIN_GETVALUE:
				// 保留两位小数
				// mTextViewGluValue[myUnpack.value_count].setText(
				// String.valueOf((float)
				// Math.round((myUnpack.value[myUnpack.value_count] / 18.0) *
				// 100) / 100));
				// mTextViewGluValueMg[myUnpack.value_count].setText(String.valueOf(myUnpack.value[myUnpack.value_count]));
				// 收到测量完成数值，发送命令，休眠血糖小板
				// sendDataSleep();
				break;
			// 获取时间
			case Unpack.GAIN_GETTIME:
				// 停止星星闪烁
				isBlink = false;
				// 版本0.03增加显示数值和提示信息
				if (measureMode != 3) {
					getResult();
				} else {
					getBiochemistryResult();
				}
				// 提示血液异常
				getAlarm();
				// 测试完成,保存数据
				measureCount++;
				mPreference.putInt(MyPreference.MEASURE_COUNT, measureCount);
				saveUserDB(myUnpack.time, String.valueOf(myUnpack.temp), myUnpack.value, String.valueOf(mResult));
				break;
			case Unpack.GAIN_STARBLINK:
				if (mImageViewStar.getVisibility() == View.INVISIBLE) {
					mImageViewStar.setVisibility(View.VISIBLE);
				} else {
					mImageViewStar.setVisibility(View.INVISIBLE);
				}
				break;
			default:
				// Toast.makeText(ReceiveActivity.this, "step=" + myUnpack.step
				// + ",错误!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	/**
	 * 根据测量模式发送不同的数据 ;0是血液模式，1是值控模式，2是电阻试片模式
	 */
	private void sendDataMode() {
		switch (measureMode) {
		case 0:
			sendData(dataBlood);
			break;
		case 1:
			sendData(dataValue);
			break;
		case 2:
			sendData(dataResistance);
			break;
		}
	}

	/**
	 * 收到数据后，根据不同模式发送不同数据，让血糖板睡眠 ;0是血液模式，1是值控模式，2是电阻试片模式
	 */
	private void sendDataSleep() {
		switch (measureMode) {
		case 0:
			sendData(sleepBlood);
			break;
		case 1:
			sendData(sleepValue);
			break;
		case 2:
			sendData(sleepResistance);
			break;
		}
	}

	// 发送数据
	private void sendData(String data) {
		try {
			mOutputStream.write(data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mode_check_blood:
			if (mCheckBoxBlood.isChecked()) {
				checkNoChecked();
				mCheckBoxBlood.setChecked(true);
			} else {
				mCheckBoxBlood.setChecked(true);
			}
			measureMode = 0;
			Toast.makeText(this, "血液模式", Toast.LENGTH_SHORT).show();
			break;

		case R.id.mode_check_value:
			if (mCheckBoxValue.isChecked()) {
				checkNoChecked();
				mCheckBoxValue.setChecked(true);
			} else {
				mCheckBoxValue.setChecked(true);
			}
			measureMode = 1;
			Toast.makeText(this, "质控模式", Toast.LENGTH_SHORT).show();
			break;

		case R.id.mode_check_resistance:
			if (mCheckBoxResistance.isChecked()) {
				checkNoChecked();
				mCheckBoxResistance.setChecked(true);
			} else {
				mCheckBoxResistance.setChecked(true);
			}
			measureMode = 2;
			Toast.makeText(this, "电阻试片模式", Toast.LENGTH_SHORT).show();
			break;
		case R.id.mode_check_biochemistry:
			if (mCheckBoxBiochemistry.isChecked()) {
				checkNoChecked();
				mCheckBoxBiochemistry.setChecked(true);
			} else {
				mCheckBoxBiochemistry.setChecked(true);
			}
			measureMode = 3;
			Toast.makeText(this, "生化模式", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	// 设置所有checkBox为没被选择
	private void checkNoChecked() {
		mCheckBoxBlood.setChecked(false);
		mCheckBoxValue.setChecked(false);
		mCheckBoxResistance.setChecked(false);
		mCheckBoxBiochemistry.setChecked(false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 插入试纸对应的按键响应
		if (keyCode == KeyEvent.KEYCODE_MUTE && !isKeyDown) {
			LogUtil.e(TAG + "411", "插入试纸");
			isKeyDown = true;
			// 显示试纸图标
			mImageViewPaper.setVisibility(ImageView.VISIBLE);
			// 清空最终值的显示；
			mTextViewVersion3Value.setText("--.-");
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// 拔出试纸对应的按键响应
		if (keyCode == KeyEvent.KEYCODE_MUTE) {
			LogUtil.e(TAG + "424", "拔出试纸");
			isKeyDown = false;
			isGluMeasuring = false;// 拔出试纸，测量结束
			mVoiceCount = 0;// 初始化声音定时器
			// 隐藏试纸图标的显示
			mImageViewPaper.setVisibility(ImageView.INVISIBLE);
			// 测量中途拔出，关闭星星闪烁,代码放在隐藏星星图标代码的前面
			isBlink = false;
			// 隐藏星星图标
			mImageViewStar.setVisibility(View.INVISIBLE);
			// 保存数据到服务器
			if (isNetOk) {
				if (mThreadSendData == null) {
					mThreadSendData = new ThreadSendServiceData();
				}
				if (!mThreadSendData.isAlive()) {
					LogUtil.e(TAG + 476, "Thread is not alive");
					mJsonServiceGet.getData(MachineCode);// 获取服务器上的count
					mThreadSendData.start();
				}
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private int[] mNode = { 10, 35, 39, 47, 70, 117, 194, 253, 305, 600 };

	private float[][] mFactor = { { (float) 0, (float) 0 }, { (float) 0.6, (float) 0.95 },
			{ (float) 0.95, (float) 0.95 }, { (float) 0.95, (float) 1.45 }, { (float) 1.45, (float) 1.65 },
			{ (float) 1.65, (float) 1.65 }, { (float) 1.65, (float) 1.65 }, { (float) 1.65, (float) 1.65 },
			{ (float) 1.65, (float) 1.65 }, { (float) 1.65, (float) 1.65 } };

	private int[] mNodeBio = { 10, 35, 39, 47, 70, 117, 194, 253, 350, 600 };
	private float[][] mFactorBio = { { (float) 0, (float) 0 }, { (float) 0.6, (float) 1.5 },
			{ (float) 1.5, (float) 1.5 }, { (float) 1.5, (float) 1.77 }, { (float) 1.77, (float) 1.8 },
			{ (float) 1.8, (float) 1.8 }, { (float) 1.8, (float) 1.8 }, { (float) 1.8, (float) 1.75 },
			{ (float) 1.75, (float) 1.5 }, { (float) 1.5, (float) 1.5 } };

	/**
	 * (X的范围20-600;X的节点为（39,47,70,117,194,253,305）);
	 * 先通过五个值算出X的值，然后根据X的值得出F系数的值；X小于等于39，F为0.8；
	 * 因为在20-600这个范围内，F的值基本是线性增加的，所以用下面方法求F：
	 * X大于39小于等于47，F为(0.8+(1.3-0.8)/(47-39)*(X-39));
	 * X大于47小于等于70，F为(1.3+(1.6-0.3)/(70-47)*(X-47)); X大于70小于等于117， F为1.6;
	 * X大于117小于等于194， F为1.6; X大于194小于等于253， F为1.6; X大于253小于等于305， F为1.6;
	 * X大于305小于等于600， F为1.6;
	 */
	/**
	 * 血液模式下，X为70-200正常，否则报血液异常;值控液模式下，X为100以下正常，否则报值控液异常
	 */
	private void getResult() {
		mResultX = myUnpack.value[0] * (float) 0.0 + myUnpack.value[1] * (float) 0.2 + myUnpack.value[2] * (float) 0.2
				+ myUnpack.value[3] * (float) 0.3 + myUnpack.value[4] * (float) 0.3;
		if ((mResultX - mNode[0]) <= 0) {
			mResultF = getResultF(0, mNode[0], mFactor[0][0], mFactor[0][1]);
		} else if ((mResultX - mNode[1]) <= 0) {
			mResultF = getResultF(mNode[0], mNode[1], mFactor[1][0], mFactor[1][1]);
		} else if ((mResultX - mNode[2]) <= 0) {
			mResultF = getResultF(mNode[1], mNode[2], mFactor[2][0], mFactor[2][1]);
		} else if (mResultX - mNode[3] <= 0) {
			mResultF = getResultF(mNode[2], mNode[3], mFactor[3][0], mFactor[3][1]);
		} else if (mResultX - mNode[4] <= 0) {
			mResultF = getResultF(mNode[3], mNode[4], mFactor[4][0], mFactor[4][1]);
		} else if (mResultX - mNode[5] <= 0) {
			mResultF = getResultF(mNode[4], mNode[5], mFactor[5][0], mFactor[5][1]);
		} else if (mResultX - mNode[6] <= 0) {
			mResultF = getResultF(mNode[5], mNode[6], mFactor[6][0], mFactor[6][1]);
		} else if (mResultX - mNode[7] <= 0) {
			mResultF = getResultF(mNode[6], mNode[7], mFactor[7][0], mFactor[7][1]);
		} else if (mResultX - mNode[8] <= 0) {
			mResultF = getResultF(mNode[7], mNode[8], mFactor[8][0], mFactor[8][1]);
		} else if (mResultX - mNode[9] <= 0) {
			mResultF = getResultF(mNode[8], mNode[9], mFactor[9][0], mFactor[9][1]);
		}
		mResultZ = mResultX * mResultF;
		mResult = (float) Math.round((mResultZ / 18.0) * 10) / 10;
		// 数值显示到界面
		if ((mResultZ < 10) || (mResultX < 10)) {
			mTextViewVersion3Value.setText("Lo");
		} else if ((mResultZ > 600) || (mResultX > 600)) {
			mTextViewVersion3Value.setText("Hi");
		} else {
			mTextViewVersion3Value.setText(String.valueOf(mResult));
		}
	}

	/**
	 * 生化模式
	 */
	private void getBiochemistryResult() {
		mResultX = myUnpack.value[0] * (float) 0.2 + myUnpack.value[1] * (float) 0.2 + myUnpack.value[2] * (float) 0.3
				+ myUnpack.value[3] * (float) 0.3 + myUnpack.value[4] * (float) 0.0;
		if ((mResultX - mNodeBio[0]) <= 0) {
			mResultF = getResultF(0, mNodeBio[0], mFactorBio[0][0], mFactorBio[0][1]);
		} else if ((mResultX - mNodeBio[1]) <= 0) {
			mResultF = getResultF(mNodeBio[0], mNodeBio[1], mFactorBio[1][0], mFactorBio[1][1]);
		} else if ((mResultX - mNodeBio[2]) <= 0) {
			mResultF = getResultF(mNodeBio[1], mNodeBio[2], mFactorBio[2][0], mFactorBio[2][1]);
		} else if (mResultX - mNodeBio[3] <= 0) {
			mResultF = getResultF(mNodeBio[2], mNodeBio[3], mFactorBio[3][0], mFactorBio[3][1]);
		} else if (mResultX - mNodeBio[4] <= 0) {
			mResultF = getResultF(mNodeBio[3], mNodeBio[4], mFactorBio[4][0], mFactorBio[4][1]);
		} else if (mResultX - mNodeBio[5] <= 0) {
			mResultF = getResultF(mNodeBio[4], mNodeBio[5], mFactorBio[5][0], mFactorBio[5][1]);
		} else if (mResultX - mNodeBio[6] <= 0) {
			mResultF = getResultF(mNodeBio[5], mNodeBio[6], mFactorBio[6][0], mFactorBio[6][1]);
		} else if (mResultX - mNodeBio[7] <= 0) {
			mResultF = getResultF(mNodeBio[6], mNodeBio[7], mFactorBio[7][0], mFactorBio[7][1]);
		} else if (mResultX - mNodeBio[8] <= 0) {
			mResultF = getResultF(mNodeBio[7], mNodeBio[8], mFactorBio[8][0], mFactorBio[8][1]);
		} else if (mResultX - mNodeBio[9] <= 0) {
			mResultF = getResultF(mNodeBio[8], mNodeBio[9], mFactorBio[9][0], mFactorBio[9][1]);
		}
		mResultZ = mResultX * mResultF;
		mResult = (float) Math.round((mResultZ / 18.0) * 10) / 10;
		// 数值显示到界面
		if ((mResultZ < 10) || (mResultX < 10)) {
			mTextViewVersion3Value.setText("Lo");
		} else if ((mResultZ > 600) || (mResultX > 600)) {
			mTextViewVersion3Value.setText("Hi");
		} else {
			mTextViewVersion3Value.setText(String.valueOf(mResult));
		}
	}

	private float getResultF(float m0, float m1, float n0, float n1) {
		return (float) (n0 + (n1 - n0) / (m1 - m0) * (mResultX - m0));
	}

	// 0是血液模式，1是值控模式，2是电阻试片模式
	private void getAlarm() {
		switch (measureMode) {
		case 0:
			if (myUnpack.time < 70 || myUnpack.time > 200) {
				Toast.makeText(this, "血液异常", Toast.LENGTH_SHORT).show();
			}
			break;
		case 1:
			if (myUnpack.time > 100) {
				Toast.makeText(this, "质控液异常", Toast.LENGTH_SHORT).show();
			}
			break;
		case 2:
			break;
		default:
			break;
		}
	}

	// 设置数据库的实体类,并保存
	private void saveUserDB(int measureTime, String temperature, int[] value, String result) {
		// 设置时间显示格式 ，并且获取当前时间
		SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String aDate = mFormatter.format(new Date());
		GluValue gluValue = new GluValue(MachineCode, mTelephony.getmLac(), mTelephony.getmCid(), measureCount,
				measureTime, aDate, temperature, result, value, "", "", "", "", "", "", "", "", "", "");
		mGluValueDao.insert(gluValue);
	}

	private void saveNetJsonService() {
		List<GluValue> gluValues = mGluValueDao.queryAll();
		int i = 0;
		LogUtil.e(TAG + "571", "准备发送数据");
		for (GluValue gluValue : gluValues) {
			if (netMeasureCount >= gluValue.getCount()) {
				i++;
				// LogUtil.e(TAG + "544", "返回继续.." + i);
				continue;
			}
			netMeasureCount++;
			LogUtil.e(TAG + "579", "发送数据");
			mJsonServiceSend.sendData(gluValue);
			break;
		}
	}

	private void showDialog() {
		LogUtil.e(TAG + "484", "measureCount ====" + measureCount);
		// 通过measureCount的值，判断是否显示CodeDialog
		// mPermissionDialog.isRightPassword用来控制密码通过后，再次插拔试纸diaolog又显示bug
		if (measureCount != 0) {
			if ((measureCount % 100 == 0) && (!mPermissionDialog.isRightPassword)) {
				isShowDialog = true;// 不重复显示dialog
				mPermissionDialog.showDialogLayout();
			} else if ((measureCount % 100 != 0) && (mPermissionDialog.isRightPassword)) {
				mPermissionDialog.isRightPassword = false;
			}
		}
	}

	BroadcastReceiver connectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
				isNetOk = false;
				LogUtil.e(TAG + "504", "unconnect...");
				// unconnect network
			} else {
				isNetOk = true;
				LogUtil.e(TAG + "507", "connect...");
				// connect network
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (connectionReceiver != null) {
			unregisterReceiver(connectionReceiver);
		}
	}

}
