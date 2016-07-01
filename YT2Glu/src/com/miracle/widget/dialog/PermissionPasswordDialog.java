package com.miracle.widget.dialog;

import com.bioland.app.utils.LogUtil;
import com.bioland.yt2glu.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PermissionPasswordDialog implements OnClickListener {

	// dialog控件
	private AlertDialog mDialog;
	private EditText mEditTextPassword;
	private String strPassword = "";
	public boolean isRightPassword = false;

	private Context context;

	public PermissionPasswordDialog(Context context) {
		super();
		this.context = context;
	}

	/**
	 * 获取输入的值
	 */
	private void getEditTextValue() {
		if (!mEditTextPassword.getText().toString().equals("")) {
			strPassword = mEditTextPassword.getText().toString();
		}
		// mDialog.dismiss();
	}

	/**
	 * 自定义dialog
	 * 
	 * @param context
	 */
	public void showDialogLayout() {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.dialog_permission_password, null);

		// 获取控件
		mEditTextPassword = (EditText) view.findViewById(R.id.edt_password);
		Button aButtonVerify = (Button) view.findViewById(R.id.btn_input_verify);
		aButtonVerify = (Button) view.findViewById(R.id.btn_input_verify);
		// 设置按键监听事件
		aButtonVerify.setOnClickListener(this);
		// 设置dialog
		AlertDialog.Builder build = new AlertDialog.Builder(context);
		build.setView(view);
		// 设置为false点击dialog周围，dialog不消失
		build.setCancelable(false);
		mDialog = build.show();
	}

	private void showHintDialog(final Context context, String str) {
		AlertDialog.Builder build = new AlertDialog.Builder(context);
		build.setTitle("提示");
		build.setMessage(str);
		// 设置为false点击dialog周围，dialog不消失
		build.setCancelable(false);
		build.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				LogUtil.e("hahahah", "jialia le ");
			}
		});
		build.show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_input_verify:
			if (verifyPassword()) {
				mDialog.dismiss();
				isRightPassword = true;
			}
			break;
		default:
			break;
		}
	}

	private boolean verifyPassword() {
		getEditTextValue();
		if (strPassword.equals("67810")) {
			strPassword = "";
			return true;
		} else {
			mEditTextPassword.setText("");
		}
		return false;
	}

}
