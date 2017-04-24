package com.energyict.protocolimpl.generic.csvhandling;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.generic.ParseUtils;

public class TestObject {

	private static final int TYPE = 0;
	private static final int DATA = 1;
	private static final int A = 2;
	private static final int B = 3;
	private static final int C = 4;
	private static final int D = 5;
	private static final int E = 6;
	private static final int F = 7;
	private static final int CLASSID = 8;
	private static final int ATTRIBUTE = 9;
	private static final int METHOD = 10;
	private static final int RESULT = 11;
	private static final int EXPECTED = 12;
	private static final int STARTTIME = 13;

	public static final int GET = 0;
	public static final int SET = 1;
	public static final int ACTION = 2;
	private static final int MESSAGE = 3;
	public static final int WAIT = 4;
	private static final int EMPTY = 5;

	private String[] testRow;
	private boolean validData = false;

	public TestObject(String subString) {
		if(ParseUtils.countEqualSignsInString(subString, ";") >= 1){
			this.testRow = subString.split(";");
		} else if (ParseUtils.countEqualSignsInString(subString, ",") >= 1){
			this.testRow = subString.split(",");
		} else {
			this.testRow = new String[]{subString};
		}
	}

	private boolean validData(){
		return validData;
	}

	private void setValidData(){
		this.validData = true;
	}

	public int size(){
		return this.testRow.length;
	}

	public int getType(){
		if(this.testRow[TYPE].equalsIgnoreCase("Get")){
			return GET;
		} else if(this.testRow[TYPE].equalsIgnoreCase("Set")){
			return SET;
		} else if(this.testRow[TYPE].equalsIgnoreCase("Action")){
			return ACTION;
		} else if(this.testRow[TYPE].equalsIgnoreCase("Message")){
			return MESSAGE;
		} else if(this.testRow[TYPE].equalsIgnoreCase("Wait")){
			return WAIT;
//		} else if(this.testRow[TYPE].isEmpty()){
//			return EMPTY;
//		} else if(this.testRow[TYPE].equalsIgnoreCase("\r\n")){
//			return EMPTY;
		} else {
//			throw new ApplicationException("Type " + this.testRow[TYPE] + " is not a valid type, please review your csv file for errors.");
			return EMPTY;
		}

	}

	public String getData(){
		if(validData()){
			return returnData();
		} else {
			if(this.testRow[DATA].indexOf("\"") != -1){
				int offset = 0;
				byte[] b = this.testRow[DATA].getBytes();
				byte[] result = new byte[b.length];
				for(int i = 0; i < b.length; i++){
					if(b[i] == 34){
						if((i != b.length-1) && (b[i+1] == 34)){
							result[offset++] = b[i];
						}
					} else {
						result[offset++] = b[i];
					}
				}
				this.testRow[DATA] = new String(trimByteArray(result));
			}
			setValidData();
			return returnData();
		}
	}

	private String returnData(){
		if(this.testRow[DATA].indexOf("0x") != -1){
			return this.testRow[DATA].substring(this.testRow[DATA].indexOf("0x")+2);
		} else {
			return this.testRow[DATA];
		}
	}

	private byte[] trimByteArray(byte[] byteArray){
		int last = byteArray.length-1;
		while((last >= 0) && (byteArray[last] == 0)){
			last--;
		}
		byte[] b = new byte[last+1];
		System.arraycopy(byteArray, 0, b, 0, last + 1);
		return b;
	}

	public ObisCode getObisCode(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(this.testRow[A]); strBuff.append(".");
		strBuff.append(this.testRow[B]); strBuff.append(".");
		strBuff.append(this.testRow[C]); strBuff.append(".");
		strBuff.append(this.testRow[D]); strBuff.append(".");
		strBuff.append(this.testRow[E]); strBuff.append(".");
		strBuff.append(this.testRow[F]);
		return ObisCode.fromString(strBuff.toString());
	}

	public int getClassId(){
		return Integer.parseInt(this.testRow[CLASSID]);
	}

	public int getAttribute(){
		return Integer.parseInt(this.testRow[ATTRIBUTE]);
	}

	public String getStringAttribute(){
		return this.testRow[ATTRIBUTE];
	}

	public int getMethod(){
		return Integer.parseInt(this.testRow[METHOD]);
	}

	public void setResult(String result) {
		if(this.testRow.length <= STARTTIME){
			String[] temp = this.testRow;
			this.testRow = new String[STARTTIME+1];
			System.arraycopy(temp, 0, this.testRow, 0, temp.length);
		}
		this.testRow[RESULT] = result;
	}

	public String getString(int j) {
		if(this.testRow.length <= j){
			String[] temp = this.testRow;
			this.testRow = new String[j];
			System.arraycopy(temp, 0, this.testRow, 0, temp.length);
		}
		return (this.testRow[j] == null)?"":this.testRow[j];
	}

	public String getExpected() {
		if(this.testRow.length <= STARTTIME){
			String[] temp = this.testRow;
			this.testRow = new String[STARTTIME+1];
			System.arraycopy(temp, 0, this.testRow, 0, temp.length);
		}
		return this.testRow[EXPECTED];
	}

	public void setTime(long l) {
		if(this.testRow.length <= STARTTIME){
			String[] temp = this.testRow;
			this.testRow = new String[STARTTIME+1];
			System.arraycopy(temp, 0, this.testRow, 0, temp.length);
		}
		this.testRow[STARTTIME] = Long.toString(l);
	}

	public String getResult() {
		if(this.testRow.length <= STARTTIME){
			String[] temp = this.testRow;
			this.testRow = new String[STARTTIME+1];
			System.arraycopy(temp, 0, this.testRow, 0, temp.length);
		}
		if(this.testRow[RESULT] == null){
			setResult("");
		}
		return this.testRow[RESULT];
	}
}
