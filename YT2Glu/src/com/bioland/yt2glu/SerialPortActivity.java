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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.bioland.app.utils.LogUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {

	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SerialPort mSerialPort = null;
	private static final String TAG = "SerialPortActivity";

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				int size = 0;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) {
						return;
					}

					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SerialPortActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		}
	}

	public void getSerialPort() throws SecurityException, IOException {
		if (mSerialPort == null) {
			mSerialPort = new SerialPort(new File("/dev/ttyS2"), 2400, 0);
		}
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);

	// @Override
	// protected void onPause() {
	// super.onPause();
	// LogUtil.e(TAG + "115", "关闭串口");
	// onDestroy();
	// }

	// @Override
	// protected void onDestroy() {
	// if (mReadThread != null)
	// mReadThread.interrupt();
	// closeSerialPort();
	// mSerialPort = null;
	// super.onDestroy();
	// System.exit(0);// finish();只是将程序放到后台，等资源不足才会释放。想直接退出程序，用System.exit(0);
	// }
}
