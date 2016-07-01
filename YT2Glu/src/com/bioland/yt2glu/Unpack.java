package com.bioland.yt2glu;

import java.io.File;
import java.io.FileWriter;

import com.miracle.widget.sharedpreferences.MyPreference;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Unpack {

	public int temp = 0;
	public int step = 0;
	public int countdown = 0;
	public int time = 0;
	public int value[] = new int[5];
	public int value_count = 0;
	public int sn = 0;
	public int originalValue[][] = new int[5][3];
	private byte snHigh = 0;
	private byte snMiddle = 0;
	private byte snLow = 0;
	private byte dataHigh = 0;
	private byte dataMiddle = 0;
	private byte dataLow = 0;
	private boolean isValue = false;

	// 1、插入试纸，2、获取到sn值，3、获取到倒计时，4、发送测量模式，5、获取到结果,6、获取时间,7、星星闪烁
	public final static byte GAIN_NOTHING = 0;
	public final static byte GAIN_INSERT = 1;
	public final static byte GAIN_GETSN = 2;
	public final static byte GAIN_COUNTDOWN = 3;
	public final static byte GAIN_GETMODE = 4;
	public final static byte GAIN_GETVALUE = 5;
	public final static byte GAIN_GETTIME = 6;
	public final static byte GAIN_STARBLINK = 7;
	// 1、试纸失效，2、血糖值高，3、血糖值低，4、温度超标，5、电池电压不足，6、试纸插入开机错误
	public final static byte STATE_NORMAL = 48;
	public final static byte STATE_ISOK = 49;
	public final static byte STATE_VALUE_HIGH = 50;
	public final static byte STATE_VALUE_LOW = 51;
	public final static byte STATE_TEMP = 52;
	public final static byte STATE_BATT = 53;
	public final static byte STATE_START = 54;

	private volatile static Unpack instance = null;
	private static String TAG = "Unpack";
	public static Context contexts;

	public String errorCode = "";

	public Unpack() {
	}

	public static Unpack getInstance(Context context) {
		if (instance == null) {
			synchronized (Unpack.class) {
				if (instance == null) {
					instance = new Unpack();
					contexts = context;
				}
			}
		}
		return instance;
	}

	private String myGetString(int id) {
		return contexts.getResources().getString(id);
	}

	/**
	 * 数组向左移位
	 * 
	 * @param buffer
	 * @param size
	 * @param start
	 * @return
	 */
	public byte[] bufShifting(byte[] buffer, int size, int start) {
		for (int i = 0; i + start < size; i++) {
			buffer[i] = buffer[i + start];
		}
		return buffer;
	}

	private void initValue() {
		isValue = false;
		step = 0;// 表示插入试纸,获得的温度
	}

	/**
	 * 保存获取的有效数据
	 * 
	 * @param buffer
	 */
	public Boolean setData(final byte[] buffer) {
		// F表示插入试纸，获取温度
		if (buffer[1] == 70) {
			if (checkSum(buffer, 8)) {
				temp = asciiToNum(buffer[3]) * 100 + asciiToNum(buffer[4]) * 10 + asciiToNum(buffer[5]);
				// 初始化状态值
				initValue();
				step = GAIN_INSERT;
				return true;
			}
		}
		// B获取序号,收到的sn数据时十进制的，相或后可直接显示，求校验和时需要改为16进制后相加
		if (buffer[1] == 66) {
			if (checkSumSN(buffer, 8)) {
				snHigh = buffer[3];
				snMiddle = buffer[4];
				snLow = buffer[5];
				sn = (snHigh << 16) | (snMiddle << 8) | snLow;
				step = GAIN_GETSN;
				return true;
			}
		}
		// A获取时间和数值
		if (buffer[1] == 65) {
			switch (buffer[2]) {
			case STATE_NORMAL:
				// buffer[3]，buffer[4]，buffer[5]为需要保存的值
				if (checkSumDecode(buffer, 8)) {
					int data = dataHigh * 100 + dataMiddle * 10 + dataLow;
					if (data > 15) {
						value_count = asciiToNum((byte) buffer[6]);
						for (int i = 0; i < 3; i++) {
							originalValue[value_count][i] = buffer[i + 3];
						}
						value[value_count] = dataHigh * 100 + dataMiddle * 10 + dataLow;
						step = GAIN_GETVALUE;
						return true;
					} else {
						countdown = dataHigh * 100 + dataMiddle * 10 + dataLow;
						step = GAIN_COUNTDOWN;
						return true;
					}
				}
				break;
			case STATE_ISOK:
				// 试纸已失效
				showToast(buffer, R.string.hint_disabled, "1");
				break;
			case STATE_VALUE_HIGH:
				// 血糖值太高
				showToast(buffer, R.string.hint_glu_high, "2");
				break;
			case STATE_VALUE_LOW:
				// 血糖值太低
				showToast(buffer, R.string.hint_glu_low, "3");
				break;
			case STATE_TEMP:
				// 温度超标
				showToast(buffer, R.string.hint_temp_high, "4");
				break;
			case STATE_BATT:
				// 电池电压不足
				showToast(buffer, R.string.hint_batt, "5");
				break;
			case STATE_START:
				// 试纸插入开机错误// 先插入试纸再打开软件
				showToast(buffer, R.string.hint_start, "6");
				break;
			}
		}
		// E获取模式
		// if (buffer[1] == 69) {
		// if (checkSumDecode(buffer, 8)) {
		// isValue = true;
		// step = GAIN_GETMODE;
		// return true;
		// }
		// }
		// G获取时间
		if (buffer[1] == 71) {
			if (checkSumDecode(buffer, 8)) {
				time = dataHigh * 100 + dataMiddle * 10 + dataLow;
				step = GAIN_GETTIME;
				return true;
			}
		}
		return false;
	}

	// private void saveErr(String number) {
	//
	// StringBuilder stringBuilder = new StringBuilder(number);
	// // for (byte b : value) {
	// // stringBuilder.append(String.format("%02X ", b));
	// // }
	// Log.e("read", " =176==" + stringBuilder.toString());
	// stringBuilder.append("\n");
	// File sdCard = Environment.getDataDirectory();
	// try {
	// FileWriter fw = new FileWriter(sdCard + "/aa.txt");
	// fw.flush();
	// fw.write(stringBuilder.toString());
	// fw.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	private void showToast(final byte[] buffer, int id, String str) {
		if (checkSum(buffer, 8)) {
			initValue();
			Toast.makeText(contexts, myGetString(id), Toast.LENGTH_SHORT).show();
			saveError(str);
		}
	}

	private MyPreference mPreference;

	private void saveError(String str) {
		mPreference = MyPreference.instance(contexts);
		String str1 = mPreference.getString(MyPreference.ERROR_CODE);
		str = str1 + "," + str;
		mPreference.putString(MyPreference.ERROR_CODE, str);
	}

	/**
	 * 检查校验和是否正确,需要包的大小
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	private boolean checkSum(final byte[] buffer, final int size) {
		int sum = 0;
		for (int i = 0; i < size - 1; i++) {
			sum += asciiToNum(buffer[i]);
		}
		if ((sum & 0xf) == asciiToNum(buffer[size - 1])) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查序号命令的校验和
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	private boolean checkSumSN(final byte[] buffer, final int size) {
		int sum = 0;
		for (int i = 0; i < size - 1; i++) {
			if (i == 3 || i == 4 || i == 5) {
				sum = sum + buffer[i] / 16 + buffer[i] % 16;
			} else {
				sum += asciiToNum(buffer[i]);
			}
		}
		if ((sum & 0xf) == asciiToNum(buffer[size - 1])) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查数据命令的校验和
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	private boolean checkSumDecode(final byte[] buffer, final int size) {
		int sum = 0;
		for (int i = 0; i < size - 1; i++) {
			if ((i != 3) && (i != 4) && (i != 5)) {
				sum += asciiToNum(buffer[i]);
			}
		}
		setAsciiXor(buffer[3], buffer[4], buffer[5]);
		sum = sum + dataHigh + dataMiddle + dataLow;
		if ((sum & 0xf) == asciiToNum(buffer[size - 1])) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 异或sn解析加密的值并求对应ascii
	 * 
	 * @param data1
	 * @param data2
	 * @param data3
	 */
	private void setAsciiXor(byte data1, byte data2, byte data3) {
		dataHigh = asciiToNum((byte) (data1 ^ snHigh));
		dataMiddle = asciiToNum((byte) (data2 ^ snMiddle));
		dataLow = asciiToNum((byte) (data3 ^ snLow));
	}

	/**
	 * 将ASCII码转为对应的数字
	 * 
	 * @param b
	 * @return
	 */
	private byte asciiToNum(byte b) {
		byte data = 0;
		if (b >= 48 && b <= 57) {
			data = (byte) (b - 48);
		} else if (b >= 65 && b <= 71) {
			data = (byte) (b - 65 + 10);
		}
		return data;
	}
}
