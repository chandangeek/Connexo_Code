/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

import com.energyict.mdc.protocol.api.InvalidPropertyException;

import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.util.TimeZone;


public class MedoPartPersonalityTable  extends Parsers{
	/*
	 * Implementation of pg 7/16 minus the secure personality details => full expands part
	 * char has been used instead of byte to define 8 bit wide symbols because byte has a sign bit,
	 * drawback is that in java char is 16 bit wide
	 */
	protected MedoTelno[] destab=new MedoTelno[4];	// position 1->64
	protected char dumax=0;							// pos 65
	protected short duwait=0;						// pos 66
	protected short lotim=0;						// pos 68
	protected char tabsum=0;						// pos 70
	protected char[] iptab= new char[48];			// pos 71->118
	protected char[] dialexp= new char[48];			// pos 119->166
	protected char[] totexp= new char[48];			// pos 167->214
	protected char[] dialmlt= new char[48];			// pos 215->262
	protected short[] dialdiv= new short[48];		// pos 263->358
	protected MedoUPI[] sumupi= new MedoUPI[48];	// pos 259->454
	protected String[] sumtabs=new String[4];		// pos 455->582
	protected char[] spare0= new char[32];			// pos 583->614
	protected MedoCLK comtad= new MedoCLK();  		// pos 615->620
	protected char[] spare1= new char[87];			// pos 621->707
	protected char rdghr1=0;						// pos 708
	protected char rdghr2=0;						// pos 709
	protected char ptv=0;							// pos 710
	protected MedoRlytab[] rlytab=new MedoRlytab[8];// pos 711->750
	protected ProtocolChannelMap meterChannelMap;
	protected TimeZone tz;

	// constructors
	MedoPartPersonalityTable(){
		char[] c=new char[750];
		for(int i=0; i<750; i++){
			c[i]=0;
		}
		tz=TimeZone.getTimeZone("GMT");
		processPersonalityTable(c);
	}

	MedoPartPersonalityTable(byte[] b, TimeZone tz){
		this.tz=tz;
		// incoming data, move them into the right position
		// parse first to char array, then to string
		processPersonalityTable(parseBArraytoCArray(b));
	}
	MedoPartPersonalityTable(char[] c, TimeZone tz){
		this.tz=tz;
		processPersonalityTable(c);
	}

	private void processPersonalityTable(char[] c) {
		// dumps byte array in the right fields
		String s = new String(c);
		MedoTelno mt;
		for(int i=0; i<4; i++){
			mt=new MedoTelno(s.substring(i*16, 16+i*16));
			destab[i]=mt;
		}
		dumax=s.charAt(64);
		duwait=parseCharToShort(s.substring(65,67).toCharArray());
		lotim=parseCharToShort(s.substring(67,69).toCharArray());
		tabsum=s.charAt(69);
		iptab=s.substring(70, 118).toCharArray();
		dialexp=s.substring(118, 166).toCharArray();
		totexp=s.substring(166, 214).toCharArray();
		dialmlt=s.substring(214, 262).toCharArray();
		for (int i=0; i<48;i++){
			dialdiv[i]=parseCharToShort(s.substring(262+i*2,262+2+i*2).toCharArray());
		}
		MedoUPI mu;
		for (int i=0; i<48;i++){
			mu=new MedoUPI(s.charAt(358+i),s.charAt(359+i));
			sumupi[i]=mu;
		}
		String mc;
		for (int i=0; i<4;i++){
			mc=new String(s.substring(454+i*32,454+32+i*32));
			sumtabs[i]=mc;
		}
		spare0=s.substring(582,614).toCharArray();
		comtad=new MedoCLK(s.substring(614, 620).toCharArray(),tz);
		spare1=s.substring(620, 707).toCharArray();
		rdghr1=s.charAt(707);
		rdghr2=s.charAt(708);
		ptv=s.charAt(709);
		MedoRlytab mr;
		for (int i=0; i<8;i++){
			mr=new MedoRlytab(s.charAt(710+i*5),s.charAt(711+i*5),
					s.charAt(712+i*5),
					parseCharToShort(s.substring(713+i*5,715+i*5).toCharArray()));
			rlytab[i]=mr;
		}
		//iptab processing
		String strmap="";
		for(int i=0; i<iptab.length-1; i++){
			strmap+=iptab[i]+":";
		}
		strmap+=iptab[iptab.length-1];
		try {
			meterChannelMap=new ProtocolChannelMap(strmap);
		} catch (InvalidPropertyException e) {
		}
	}

	// work methods (parsing and serializing)
	// parse to byte array
	public byte[] parseToByteArray(){
		// send all to a stringbuffer and build a byte array from
		// the resulting string
		String serial="";
		char[] charArray,ca;
		for(int i=0; i<4; i++){
			serial+= new String(destab[i].getcharArray());
		}
		serial+=""+dumax;
		ca = parseShortToChar(duwait);
		serial+=""+ca[0]+""+ca[1];
		ca = parseShortToChar(lotim);
		serial+=""+ca[0]+""+ca[1];
		serial+=""+tabsum;
		for (int i=0; i<iptab.length; i++){
			serial+=""+iptab[i];
		}
		for (int i=0; i<dialexp.length; i++){
			serial+=""+dialexp[i];
		}
		for (int i=0; i<totexp.length; i++){
			serial+=""+totexp[i];
		}
		for (int i=0; i<dialmlt.length; i++){
			serial+=""+dialmlt[i];
		}
		for (int i=0; i<dialdiv.length; i++){
			ca = parseShortToChar(dialdiv[i]);
			serial+=""+ca[0]+""+ ca[1];
		}
		for (int i=0; i<sumupi.length; i++){
			serial+=new String(sumupi[i].getcharArray());
		}
		for (int i=0; i<sumtabs.length; i++){
			serial+=""+sumtabs[i];
		}
		for (int i=0; i<spare0.length; i++){
			serial+=""+spare0[i];
		}
		serial+=new String(comtad.getcharArray());
		for (int i=0; i<spare1.length; i++){
			serial+=""+spare1[i];
		}
		serial+=""+rdghr1;
		serial+=""+rdghr2;
		serial+=""+ptv;
		for (int i=0; i<rlytab.length; i++){
			serial+=new String(rlytab[i].getcharArray());
		}
		charArray=serial.toCharArray();
		return parseCArraytoBArray(charArray);
	}

	public void printData() {
		System.out.print  ("destab:          ");
		for(int i=0; i<4; i++){
			System.out.print(destab[i].toStringASCII());
		}
		System.out.println();
		System.out.println("dumax:           "+NumberToString(dumax));
		System.out.println("duwait:          "+NumberToString(duwait));
		System.out.println("lotim:           "+NumberToString(lotim));
		System.out.println("tabsum:          "+NumberToString(tabsum));
		System.out.print  ("iptab:           ");
		for(int i=0; i<48; i++){
			System.out.print(NumberToString(iptab[i])+" ");
		}
		System.out.println();
		System.out.print  ("dialexp:         ");
		for(int i=0; i<48; i++){
			System.out.print(NumberToString(dialexp[i])+" ");
		}
		System.out.println();
		System.out.print  ("totexp:          ");
		for(int i=0; i<48; i++){
			System.out.print(NumberToString(totexp[i])+" ");
		}
		System.out.println();
		System.out.print  ("dialmlt:         ");
		for(int i=0; i<48; i++){
			System.out.print(NumberToString(dialmlt[i])+" ");
		}
		System.out.println();
		System.out.print  ("dialdiv:         ");
		for(int i=0; i<48; i++){
			System.out.print(NumberToString(dialdiv[i])+" ");
		}
		System.out.println();
		System.out.print  ("sumupi:          ");
		for(int i=0; i<32; i++){
			System.out.print(sumupi[i].toString()+" ");
		}
		System.out.println();
		System.out.print("sumtabs:           ");
		for(int ii=0; ii<sumtabs.length; ii++){
			String s=sumtabs[ii];
			System.out.print(s);
		}
		System.out.println();
		System.out.print  ("spare0:          ");
		for(int i=0; i<32; i++){
			System.out.print(NumberToString(spare0[i]));
		}
		System.out.println();
		System.out.println(comtad.toString());
		System.out.print  ("spare1:          ");
		for(int i=0; i<87; i++){
			System.out.print(NumberToString(spare1[i]));
		}
		System.out.println();
		System.out.println("rdghr1:          "+NumberToString(rdghr1));
		System.out.println("rdghr2:          "+NumberToString(rdghr2));
		System.out.println("ptv:             "+NumberToString(ptv));

		System.out.print  ("rlytab:          ");
		for(int i=0; i<8; i++){
			System.out.print(rlytab[i].toString());
		}
		System.out.println();
	}
	// setters and getters autogenerated
	/**
	 * @return the destab
	 */
	public MedoTelno[] getDestab() {
		return destab;
	}
	/**
	 * @param destab the destab to set
	 */
	public void setDestab(MedoTelno[] destab) {
		this.destab = destab;
	}
	/**
	 * @return the dumax
	 */
	public char getDumax() {
		return dumax;
	}
	/**
	 * @param dumax the dumax to set
	 */
	public void setDumax(char dumax) {
		this.dumax = dumax;
	}
	/**
	 * @return the duwait
	 */
	public short getDuwait() {
		return duwait;
	}
	/**
	 * @param duwait the duwait to set
	 */
	public void setDuwait(short duwait) {
		this.duwait = duwait;
	}
	/**
	 * @return the lotim
	 */
	public short getLotim() {
		return lotim;
	}
	/**
	 * @param lotim the lotim to set
	 */
	public void setLotim(short lotim) {
		this.lotim = lotim;
	}
	/**
	 * @return the tabsum
	 */
	public char getTabsum() {
		return tabsum;
	}
	/**
	 * @param tabsum the tabsum to set
	 */
	public void setTabsum(char tabsum) {
		this.tabsum = tabsum;
	}
	/**
	 * @return the iptab
	 */
	public char[] getIptab() {
		return iptab;
	}
	/**
	 * @param iptab the iptab to set
	 */
	public void setIptab(char[] iptab) {
		this.iptab = iptab;
	}
	/**
	 * @return the dialexp
	 */
	public char[] getDialexp() {
		return dialexp;
	}
	/**
	 * @param dialexp the dialexp to set
	 */
	public void setDialexp(char[] dialexp) {
		this.dialexp = dialexp;
	}
	/**
	 * @return the totexp
	 */
	public char[] getTotexp() {
		return totexp;
	}
	/**
	 * @param totexp the totexp to set
	 */
	public void setTotexp(char[] totexp) {
		this.totexp = totexp;
	}
	/**
	 * @return the dialmlt
	 */
	public char[] getDialmlt() {
		return dialmlt;
	}
	/**
	 * @param dialmlt the dialmlt to set
	 */
	public void setDialmlt(char[] dialmlt) {
		this.dialmlt = dialmlt;
	}
	/**
	 * @return the dialdiv
	 */
	public short[] getDialdiv() {
		return dialdiv;
	}
	/**
	 * @param dialdiv the dialdiv to set
	 */
	public void setDialdiv(short[] dialdiv) {
		this.dialdiv = dialdiv;
	}
	/**
	 * @return the sumupi
	 */
	public MedoUPI[] getSumupi() {
		return sumupi;
	}
	/**
	 * @param sumupi the sumupi to set
	 */
	public void setSumupi(MedoUPI[] sumupi) {
		this.sumupi = sumupi;
	}
	/**
	 * @return the sumtabs
	 */
	public String[] getSumtabs() {
		return sumtabs;
	}
	/**
	 * @param sumtabs the sumtabs to set
	 */
	public void setSumtabs(String[] sumtabs) {
		this.sumtabs = sumtabs;
	}
	/**
	 * @return the spare0
	 */
	public char[] getSpare0() {
		return spare0;
	}
	/**
	 * @param spare0 the spare0 to set
	 */
	public void setSpare0(char[] spare0) {
		this.spare0 = spare0;
	}
	/**
	 * @return the comtad
	 */
	public MedoCLK getComtad() {
		return comtad;
	}
	/**
	 * @param comtad the comtad to set
	 */
	public void setComtad(MedoCLK comtad) {
		this.comtad = comtad;
	}
	/**
	 * @return the spare1
	 */
	public char[] getSpare1() {
		return spare1;
	}
	/**
	 * @param spare1 the spare1 to set
	 */
	public void setSpare1(char[] spare1) {
		this.spare1 = spare1;
	}
	/**
	 * @return the rdghr1
	 */
	public char getRdghr1() {
		return rdghr1;
	}
	/**
	 * @param rdghr1 the rdghr1 to set
	 */
	public void setRdghr1(char rdghr1) {
		this.rdghr1 = rdghr1;
	}
	/**
	 * @return the rdghr2
	 */
	public char getRdghr2() {
		return rdghr2;
	}
	/**
	 * @param rdghr2 the rdghr2 to set
	 */
	public void setRdghr2(char rdghr2) {
		this.rdghr2 = rdghr2;
	}
	/**
	 * @return the ptv
	 */
	public char getPtv() {
		return ptv;
	}
	/**
	 * @param ptv the ptv to set
	 */
	public void setPtv(char ptv) {
		this.ptv = ptv;
	}
	/**
	 * @return the rlytab
	 */
	public MedoRlytab[] getRlytab() {
		return rlytab;
	}
	/**
	 * @param rlytab the rlytab to set
	 */
	public void setRlytab(MedoRlytab[] rlytab) {
		this.rlytab = rlytab;
	}

	public ProtocolChannelMap getMeterChannelMap() {
		return meterChannelMap;
	}

	public TimeZone getTz() {
		return tz;
	}

	public void setTz(TimeZone tz) {
		this.tz = tz;
	}


}
