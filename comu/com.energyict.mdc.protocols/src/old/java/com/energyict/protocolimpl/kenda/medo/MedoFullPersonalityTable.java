package com.energyict.protocolimpl.kenda.medo;

import java.util.TimeZone;

public class MedoFullPersonalityTable extends MedoPartPersonalityTable{
	protected char tots=0;
	protected char demper=0;
	protected short mycode=0;
	protected char slave=0;
	
	MedoFullPersonalityTable(){
		super();
	}
	
	MedoFullPersonalityTable(byte[] b, TimeZone tz){
		super(b,tz);
		processSecurePersonalityDetails(parseBArraytoCArray(b));
	}
	
	private void processSecurePersonalityDetails(char[] c) {
		tots=c[750];
		demper=c[751];
		char[] ct=new char[2];
		ct[0]=c[752];
		ct[1]=c[753];
		mycode=parseCharToShort(ct);
		slave=c[754];
	}

	// expand parsing
	
	public byte[] parseToByteArray(){
		byte[] b = super.parseToByteArray();
		// add rest
		byte[] bext=new byte[b.length+5];
		for (int i=0; i<b.length; i++){ // deep copy
			bext[i]=b[i];
		}
		bext[b.length]=(byte) tots;
		bext[b.length+1]=(byte) demper;
		char[] c = parseShortToChar(mycode);
		bext[b.length+2]=(byte) c[0];
		bext[b.length+3]=(byte) c[1];
		bext[b.length+4]=(byte) slave;
		return bext;		
	}
	public void printData() {
		super.printData();
		System.out.println("tots:            "+NumberToString(tots));
		System.out.println("demper:          "+NumberToString(demper));
		System.out.println("mycode:          "+NumberToString(mycode));
		System.out.println("slave:           "+NumberToString(slave));
		
	}
	/**
	 * @return the tots
	 */
	public char getTots() {
		return tots;
	}

	/**
	 * @param tots the tots to set
	 */
	public void setTots(char tots) {
		this.tots = tots;
	}

	/**
	 * @return the demper
	 */
	public char getDemper() {
		return demper;
	}

	/**
	 * @param demper the demper to set
	 */
	public void setDemper(char demper) {
		this.demper = demper;
	}

	/**
	 * @return the mycode
	 */
	public short getMycode() {
		return mycode;
	}

	/**
	 * @param mycode the mycode to set
	 */
	public void setMycode(short mycode) {
		this.mycode = mycode;
	}

	/**
	 * @return the slave
	 */
	public char getSlave() {
		return slave;
	}

	/**
	 * @param slave the slave to set
	 */
	public void setSlave(char slave) {
		this.slave = slave;
	}


	
	
}
