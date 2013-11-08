package com.energyict.protocolimpl.kenda.meteor;;

public class MeteorSetupInfo extends Parsers implements MeteorCommandAbstract{
	private String data;
	public MeteorSetupInfo(char[] charArray) {
		process(charArray);
	}
	private void process(char[] c){
		data=new String(c);
	}
	public byte[] parseToByteArray() {
		return parseCArraytoBArray(data.toCharArray());
	}

	public void printData() {
		System.out.println(data);
	}

}
