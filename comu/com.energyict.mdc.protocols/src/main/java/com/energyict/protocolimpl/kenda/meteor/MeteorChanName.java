package com.energyict.protocolimpl.kenda.meteor;

public class MeteorChanName extends Parsers implements MeteorCommandAbstract {
	String chanArray;
	public MeteorChanName(char[] charArray) {
		process(charArray);
	}
	private void process(char[] c){
		chanArray=new String(c);
	}
	public byte[] parseToByteArray() {
		return parseCArraytoBArray(chanArray.toCharArray());
	}

	public void printData() {
		System.out.print(chanArray+" ");
	}

}
