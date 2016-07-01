package com.miracle.widget.telephonymanager;

import android.app.Activity;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class MyTelephonyManager {

	private Context context;
	private Activity activity;
	private int mCid;
	private int mLac;

	public MyTelephonyManager(Context context) {
		super();
		this.context = context;
		init();
	}

	private void init() {
		TelephonyManager tel = (TelephonyManager) ((Activity) context).getSystemService(context.TELEPHONY_SERVICE);
		CellLocation cel = tel.getCellLocation();
		int nPhoneType = tel.getPhoneType();
		// 移动联通 GsmCellLocation
		if (nPhoneType == TelephonyManager.PHONE_TYPE_GSM && cel instanceof GsmCellLocation) {
			GsmCellLocation gsmCellLocation = (GsmCellLocation) cel;
			int nGSMCID = gsmCellLocation.getCid();
			if (nGSMCID > 0) {
				if (nGSMCID != 65535) {
					this.mLac = gsmCellLocation.getLac();
					this.mCid = nGSMCID;
				}
			}
		}
	}

	public int getmCid() {
		return mCid;
	}

	public void setmCid(int mCid) {
		this.mCid = mCid;
	}

	public int getmLac() {
		return mLac;
	}

	public void setmLac(int mLac) {
		this.mLac = mLac;
	}
}
