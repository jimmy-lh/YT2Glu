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

public class JsonServiceGet implements Runnable {
	/** 地址 */
	private static final String INNER_URL = "http://www.dydtrip.com/yt2.php?pscode=1c1746a1d66ae089d0cf1a64fd8286a0";
	/** TAG */
	private final static String TAG = "JsonServiceGet";
	private List<NameValuePair> lists = new ArrayList<NameValuePair>();
	public static int backData = 0;
	private Thread mThread = null;

	// 获取数据
	public void getData(String mCode) {
		lists.removeAll(lists);
		JSONObject results = new JSONObject();
		JSONObject jsonObj0 = new JSONObject();
		try {
			jsonObj0.put("MachineCode", mCode);
			results.put("code", "1001");
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
				LogUtil.e(TAG + 87, "返回ack element = " + element);
				if (element.startsWith("{")) {
					try {
						JSONObject result = new JSONObject(element);
						String info = result.getString("ack");
						if (info.equals("0")) {
							backData = Integer.parseInt(result.getString("count"));
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
