/**
 * @author zhoushengtao
 * @since 2013-7-16 下午7:39:05
 */

package com.miracle.db.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 数据库对应的pojo类，注意一下三点 1、填写表的名称 @DatabaseTable 2、填写表中持久化项的 @DatabaseField
 * 还可使顺便设置其属性 3、保留一个无参的构造函数
 */
// 表名称
@DatabaseTable(tableName = "gluValue")
public class GluValue {
	// 主键 id 自增长
	@DatabaseField(generatedId = true)
	private int Id;
	// MachineCode,机器码；可为空
	@DatabaseField(canBeNull = true)
	private String MachineCode;
	// LAC；可为空
	@DatabaseField(canBeNull = true)
	private int Lac;
	// CID；可为空
	@DatabaseField(canBeNull = true)
	private int Cid;
	// Count；可为空
	@DatabaseField(canBeNull = true)
	private int Count;
	// 吸血时间
	@DatabaseField(canBeNull = true)
	private int MeasureTime;
	// 当前时间
	@DatabaseField(canBeNull = true)
	private String Date;
	// mTemp,温度；可为空
	@DatabaseField(canBeNull = true)
	private String Temperature;
	// 结果；可为空
	@DatabaseField(canBeNull = true)
	private String Result;
	// 中间结果0；可为空
	@DatabaseField(canBeNull = true)
	private int Value0;
	// 中间结果1；可为空
	@DatabaseField(canBeNull = true)
	private int Value1;
	// 中间结果2；可为空
	@DatabaseField(canBeNull = true)
	private int Value2;
	// 中间结果3；可为空
	@DatabaseField(canBeNull = true)
	private int Value3;
	// 中间结果4；可为空
	@DatabaseField(canBeNull = true)
	private int Value4;
	// Reserve1；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve1;
	// Reserve2；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve2;
	// Reserve3；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve3;
	// Reserve4；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve4;
	// Reserve5；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve5;
	// Reserve6；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve6;
	// Reserve7；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve7;
	// Reserve8；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve8;
	// Reserve9；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve9;
	// Reserve10；可为空
	@DatabaseField(canBeNull = true)
	private String Reserve10;

	public GluValue() {
		// ORMLite 需要一个无参构造
	}

	/**
	 * 
	 * @param measureTime吸血时间
	 * @param temperature温度
	 * @param value中间值
	 * @param result结果
	 * @param Lac基站码
	 * @param Cid小区码
	 * @param Count总测量次数
	 * @param mCode机器码
	 * @param Reserve1保留位
	 * @param Reserve2保留位
	 * @param Reserve3保留位
	 * @param Reserve4保留位
	 * @param Reserve5保留位
	 * @param Reserve6保留位
	 * @param Reserve7保留位
	 * @param Reserve8保留位
	 * @param Reserve9保留位
	 * @param Reserve10保留位
	 */
	public GluValue(String MachineCode, int Lac, int Cid, int Count, int MeasureTime, String Date, String Temperature,
			String Result, int[] value, String Reserve1, String Reserve2, String Reserve3, String Reserve4,
			String Reserve5, String Reserve6, String Reserve7, String Reserve8, String Reserve9, String Reserve10) {
		super();

		this.Result = Result;
		Value0 = value[0];
		Value1 = value[1];
		Value2 = value[2];
		Value3 = value[3];
		Value4 = value[4];

		this.MachineCode = MachineCode;
		this.Lac = Lac;
		this.Cid = Cid;
		this.Count = Count;
		this.MeasureTime = MeasureTime;
		this.Date = Date;
		this.Temperature = Temperature;

		this.Reserve1 = Reserve1;
		this.Reserve2 = Reserve2;
		this.Reserve3 = Reserve3;
		this.Reserve4 = Reserve4;
		this.Reserve5 = Reserve5;
		this.Reserve6 = Reserve6;
		this.Reserve7 = Reserve7;
		this.Reserve8 = Reserve8;
		this.Reserve9 = Reserve9;
		this.Reserve10 = Reserve10;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getMachineCode() {
		return MachineCode;
	}

	public void setMachineCode(String machineCode) {
		MachineCode = machineCode;
	}

	public int getLac() {
		return Lac;
	}

	public void setLac(int lac) {
		Lac = lac;
	}

	public int getCid() {
		return Cid;
	}

	public void setCid(int cid) {
		Cid = cid;
	}

	public int getCount() {
		return Count;
	}

	public void setCount(int count) {
		Count = count;
	}

	public int getMeasureTime() {
		return MeasureTime;
	}

	public void setMeasureTime(int measureTime) {
		MeasureTime = measureTime;
	}

	public String getDate() {
		return Date;
	}

	public void setDate(String date) {
		Date = date;
	}

	public String getTemperature() {
		return Temperature;
	}

	public void setTemperature(String temperature) {
		Temperature = temperature;
	}

	public String getResult() {
		return Result;
	}

	public void setResult(String result) {
		Result = result;
	}

	public int getValue0() {
		return Value0;
	}

	public void setValue0(int value0) {
		Value0 = value0;
	}

	public int getValue1() {
		return Value1;
	}

	public void setValue1(int value1) {
		Value1 = value1;
	}

	public int getValue2() {
		return Value2;
	}

	public void setValue2(int value2) {
		Value2 = value2;
	}

	public int getValue3() {
		return Value3;
	}

	public void setValue3(int value3) {
		Value3 = value3;
	}

	public int getValue4() {
		return Value4;
	}

	public void setValue4(int value4) {
		Value4 = value4;
	}

	public String getReserve1() {
		return Reserve1;
	}

	public void setReserve1(String reserve1) {
		Reserve1 = reserve1;
	}

	public String getReserve2() {
		return Reserve2;
	}

	public void setReserve2(String reserve2) {
		Reserve2 = reserve2;
	}

	public String getReserve3() {
		return Reserve3;
	}

	public void setReserve3(String reserve3) {
		Reserve3 = reserve3;
	}

	public String getReserve4() {
		return Reserve4;
	}

	public void setReserve4(String reserve4) {
		Reserve4 = reserve4;
	}

	public String getReserve5() {
		return Reserve5;
	}

	public void setReserve5(String reserve5) {
		Reserve5 = reserve5;
	}

	public String getReserve6() {
		return Reserve6;
	}

	public void setReserve6(String reserve6) {
		Reserve6 = reserve6;
	}

	public String getReserve7() {
		return Reserve7;
	}

	public void setReserve7(String reserve7) {
		Reserve7 = reserve7;
	}

	public String getReserve8() {
		return Reserve8;
	}

	public void setReserve8(String reserve8) {
		Reserve8 = reserve8;
	}

	public String getReserve9() {
		return Reserve9;
	}

	public void setReserve9(String reserve9) {
		Reserve9 = reserve9;
	}

	public String getReserve10() {
		return Reserve10;
	}

	public void setReserve10(String reserve10) {
		Reserve10 = reserve10;
	}

	@Override
	public String toString() {
		String text = "";
		String count = "";

		text += "\nid = " + Id;
		count += ",count = " + Count;
		return text + count;
	}

}
