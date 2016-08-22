package com.miracle.widget.dialog;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 设置EditText的上限值
 * 
 * @author Administrator
 *
 */
public class MaxValueTextWatcher implements TextWatcher {

	private EditText editText = null;
	private EditText editTextNext = null;

	private int maxValue = 0;

	/**
	 * 调用此构造函数，输入值不会超过 maxValue
	 * @param editText
	 * @param maxValue
	 */
	public MaxValueTextWatcher(EditText editText, int maxValue) {
		super();
		this.editText = editText;
		this.maxValue = maxValue;
	}

	/**
	 * 调用此构造函数，输入值不会超过 maxValue，焦点会自动跳到 editTextNext
	 * @param editText
	 * @param editTextNext
	 * @param maxValue
	 */
	public MaxValueTextWatcher(EditText editText, EditText editTextNext, int maxValue) {
		super();
		this.editText = editText;
		this.editTextNext = editTextNext;
		this.maxValue = maxValue;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		int intEdit = 0;
		Editable editable = editText.getText();
		if (!editable.toString().equals("")) {
			String strEdit = editable.toString();
			intEdit = Integer.parseInt(strEdit);
		}

		if (intEdit > maxValue) {
			int selEndIndex = Selection.getSelectionEnd(editable);
			String str = editable.toString();
			// 截取新字符串
			String newStr = str.substring(0, editable.length() - 1);
			editText.setText(newStr);
			editable = editText.getText();

			// 新字符串的长度
			int newLen = editable.length();
			// 旧光标位置超过字符串长度
			if (selEndIndex > newLen) {
				selEndIndex = editable.length();
			}
			// 设置新光标所在的位置
			Selection.setSelection(editable, selEndIndex);
			if (editTextNext != null) {
				editTextNext.requestFocus();
			}
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

}
