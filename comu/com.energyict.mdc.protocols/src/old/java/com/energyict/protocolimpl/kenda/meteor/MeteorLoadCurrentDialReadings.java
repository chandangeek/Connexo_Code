/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

public class MeteorLoadCurrentDialReadings extends Parsers implements MeteorCommandAbstract {
	/*
	 * not yet implemented, not used
	 */
	private char firstDial=0;
	private char dialCNT=0;
	private MeteorDialSettings meteorDialSettings=new MeteorDialSettings();
	
	MeteorLoadCurrentDialReadings(){}
	MeteorLoadCurrentDialReadings(char[] c){process(c);}
	MeteorLoadCurrentDialReadings(byte[] b){process(parseBArraytoCArray(b));}
	
	private void process(char[] c){
		char[] temp=new char[c.length-2];
		firstDial=c[0];
		dialCNT=c[1];
		for(int i=0; i<c.length-2; i++){
			temp[i]=c[i+2];
		}
		meteorDialSettings=new MeteorDialSettings(temp);
	}
	
	public byte[] parseToByteArray() {
		byte b[] = new byte[2+dialCNT*4];
		// TODO Auto-generated method stub
		return null;
	}

	public void printData() {
		// TODO Auto-generated method stub

	}

}
