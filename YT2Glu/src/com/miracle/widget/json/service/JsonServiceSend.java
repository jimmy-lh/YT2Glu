package com.miracle.widget.json.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.bioland.app.utils.LogUtil;
import com.bioland.yt2glu.ReceiveActivity;
import com.miracle.db.bean.GluValue;
import com.miracle.widget.sharedpreferences.MyPreference;

public class JsonServiceSend implements Runnable {
	/** 地址 */
	private static final String INNER_URL = "http://www.dydtrip.com/yt2.php?pscode=1c1746a1d66ae089d0cf1a64fd8286a0";
	/** TAG */
	private final static String TAG = "JsonServiceSend";
	private List<NameValuePair> lists = new ArrayList<NameValuePair>();
	public static int backData = 0;
	private Thread mThread = null;
	private MyPreference mPreference;

	/***
	 * 客户端调用的方法：传递参数向服务器中发送请求
	 * 
	 * @param userId
	 * @param userName
	 * @return
	 * @throws JSONException
	 */
	public void sendData(GluValue gluValue) {
		mPreference = MyPreference.getMyPreference();

		lists.removeAll(lists);
		JSONObject jsonObj0 = new JSONObject();
		JSONObject results = new JSONObject();

		try {
			jsonObj0.put("MachineCode", gluValue.getMachineCode());// 机器码
			jsonObj0.put("Lac", String.valueOf(gluValue.getLac()));// 基站码
			jsonObj0.put("Cid", String.valueOf(gluValue.getCid()));// 小区码
			jsonObj0.put("Count", String.valueOf(gluValue.getCount()));// 总的测量次数
			jsonObj0.put("Time", String.valueOf(gluValue.getMeasureTime()));// 吸血时间
			jsonObj0.put("Date", String.valueOf(gluValue.getDate()));// 当前时间
			jsonObj0.put("Temperature", String.valueOf(gluValue.getTemperature()));// 温度

			jsonObj0.put("Result", String.valueOf(gluValue.getResult()));// 结果
			jsonObj0.put("Value0", String.valueOf(gluValue.getValue0()));// 中间值
			jsonObj0.put("Value1", String.valueOf(gluValue.getValue1()));// 中间值
			jsonObj0.put("Value2", String.valueOf(gluValue.getValue2()));// 中间值
			jsonObj0.put("Value3", String.valueOf(gluValue.getValue3()));// 中间值
			jsonObj0.put("Value4", String.valueOf(gluValue.getValue4()));// 中间值
			jsonObj0.put("Reserve1", gluValue.getReserve1());// 保留位
			jsonObj0.put("Reserve2", gluValue.getReserve2());// 保留位
			jsonObj0.put("Reserve3", gluValue.getReserve3());// 保留位
			jsonObj0.put("Reserve4", gluValue.getReserve4());// 保留位
			jsonObj0.put("Reserve5", gluValue.getReserve5());// 保留位
			jsonObj0.put("Reserve6", gluValue.getReserve6());// 保留位
			jsonObj0.put("Reserve7", gluValue.getReserve7());// 保留位
			jsonObj0.put("Reserve8", gluValue.getReserve8());// 保留位
			jsonObj0.put("Reserve9", gluValue.getReserve9());// 保留位
			jsonObj0.put("Reserve10", gluValue.getReserve10());// 保留位

			results.put("code", "1000");
			results.put("data", jsonObj0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		lists.add(new BasicNameValuePair("yt_data", results.toString()));
		if (mThread == null) {
			mThread = new Thread(this);
		}
		if (!mThread.isAlive()) {
			mThread.start();
		}
	}

	@Override
	public void run() {
		try {
			doPost(lists);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mThread = null;
	}

	/**
	 * 请求服务器的方法
	 * 
	 * @param model
	 * @param lists
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws JSONException
	 */
	private int doPost(List<NameValuePair> lists) throws ClientProtocolException, IOException, JSONException {
		// 1.创建请求对象
		HttpPost httpPost = new HttpPost(INNER_URL);
		// 2.创建客户端对象
		httpPost.setEntity(new UrlEncodedFormEntity(lists, "utf-8"));

		try {
			// 没进入if语句，返回-2，一般是没有网络
			backData = -2;
			// 服务器端返回请求的数据
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
			// 解析请求返回的数据
			if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
				String element = EntityUtils.toString(httpResponse.getEntity());
				LogUtil.e(TAG + 130, "返回ack element = " + element);
				if (element.startsWith("{")) {
					try {
						JSONObject result = new JSONObject(element);
						String info = result.getString("ack");
						if (info.equals("0")) {
							backData = 0;
							// 如果传输成功且软件是第一次运行，则isFirst = false
							if ((backData == 0) && (ReceiveActivity.isFirst)) {
								LogUtil.e(TAG + 595, "第一次运行代码且上传数据成功");
								ReceiveActivity.isFirst = false;
								mPreference.putBoolean(MyPreference.IS_FIRST, ReceiveActivity.isFirst);
							}
							return backData;
						} else {
							backData = -1;
							return backData;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return backData;
	}
}
