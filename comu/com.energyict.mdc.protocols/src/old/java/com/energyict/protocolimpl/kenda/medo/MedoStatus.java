/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

import java.util.TimeZone;


public class MedoStatus extends Parsers{
	// is protected to enable inheritance in METEOR protocol, if not used set private
	/*
	 * Page 9/16 & 10/16
	 */
	
	protected MedoCLK clk=new MedoCLK();
	protected char totals=0;
	protected int demCNT=0;
	protected int demLyr=0;
	protected int demTyr=0;
	protected short ramtop=0;
	protected short sc1=0;
	protected int om=0;
	protected int mm=0;
	protected short mCNT=0;
	protected short mnsCNT=0;
	protected char mtrs=0;
	protected char relays=0;
	protected char[] states= new char[6];
	protected char rdghr1=0;
	protected char rdghr2=0;
	protected char eopDay=0;
	protected char loVolt=0;
	protected char empty=0;
	protected char lout=0;
	protected char[] alarm= new char[45];
	protected char mwCNT=0;
	protected char prwCNT=0;	
	protected char liuAcc=0;
	protected char prec=0;
	protected char batlow=0;
	protected char romBat=0;
	protected char ramBat=0;
	protected char tabInv=0;
	protected char datInv=0;
	protected char alert=0;
	protected char pfCNT=0;
	protected char gfoCNT=0;
	protected char mueCNT=0;
	protected char iueCNT=0;
	protected char duCNT=0;
	protected char ceCNT=0;
	protected TimeZone tz;

	MedoStatus(){}
	
	MedoStatus(byte[] b, TimeZone tz){
		this.tz=tz;
		processStatus(parseBArraytoCArray(b));
	}
	MedoStatus(char[] c, TimeZone tz){
		this.tz=tz;
		processStatus(c);
	}

	private void processStatus(char[] c) {
		String s = new String(c);
		clk=new MedoCLK(s.substring(0, 6).toCharArray(), tz);
		totals=s.charAt(6);
		demCNT=parseCharToInt(s.substring(7, 11).toCharArray());
		demLyr=parseCharToInt(s.substring(11, 15).toCharArray());
		demTyr=parseCharToInt(s.substring(15, 19).toCharArray());
		ramtop=parseCharToShort(s.substring(19, 21).toCharArray());
		sc1=parseCharToShort(s.substring(21, 23).toCharArray());
		om=parseCharToInt(s.substring(23, 27).toCharArray());
		mm=parseCharToInt(s.substring(27, 31).toCharArray());
		mCNT=parseCharToShort(s.substring(31, 33).toCharArray());
		mnsCNT=parseCharToShort(s.substring(33, 35).toCharArray());
		mtrs=s.charAt(35);
		relays=s.charAt(36);
		states=s.substring(37, 43).toCharArray();
		rdghr1=s.charAt(43);
		rdghr2=s.charAt(44);
		eopDay=s.charAt(45);
		loVolt=s.charAt(46);
		empty=s.charAt(47);
		lout=s.charAt(48);
		alarm=s.substring(49, 94).toCharArray();
		mwCNT=s.charAt(94);
		prwCNT=s.charAt(95);	
		liuAcc=s.charAt(96);
		prec=s.charAt(97);
		batlow=s.charAt(98);
		romBat=s.charAt(99);
		ramBat=s.charAt(100);
		tabInv=s.charAt(101);
		datInv=s.charAt(102);
		alert=s.charAt(103);
		pfCNT=s.charAt(104);
		gfoCNT=s.charAt(105);
		mueCNT=s.charAt(106);
		iueCNT=s.charAt(107);
		duCNT=s.charAt(108);
		ceCNT=s.charAt(109);
	}
	
	public byte[] parseToByteArray(){
		String serial="";
		serial+=new String(clk.getcharArray());
		serial+=""+totals;
		serial+=new String(parseIntToChar(demCNT));
		serial+=new String(parseIntToChar(demLyr));
		serial+=new String(parseIntToChar(demTyr));
		serial+=new String(parseShortToChar(ramtop));
		serial+=new String(parseShortToChar(sc1));
		serial+=new String(parseIntToChar(om));
		serial+=new String(parseIntToChar(mm));
		serial+=new String(parseShortToChar(mCNT));
		serial+=new String(parseShortToChar(mnsCNT));
		serial+=""+mtrs;
		serial+=""+relays;
		serial+=new String(states);
		serial+=""+rdghr1;
		serial+=""+rdghr2;
		serial+=""+eopDay;
		serial+=""+loVolt;
		serial+=""+empty;
		serial+=""+lout;
		serial+=new String(alarm);
		serial+=""+mwCNT;
		serial+=""+prwCNT;
		serial+=""+liuAcc;
		serial+=""+prec;
		serial+=""+batlow;
		serial+=""+romBat;
		serial+=""+ramBat;
		serial+=""+tabInv;
		serial+=""+datInv;
		serial+=""+alert;
		serial+=""+pfCNT;
		serial+=""+gfoCNT;
		serial+=""+mueCNT;
		serial+=""+iueCNT;
		serial+=""+duCNT;
		serial+=""+ceCNT;
		return parseCArraytoBArray(serial.toCharArray());
	}
	public void printData(){
		System.out.println("clk:             "+ clk.toString());
		
		System.out.println("totals:          "+  NumberToString(totals));
		System.out.println("demCNT:          "+  NumberToString(demCNT));
		System.out.println("demLyr:          "+  NumberToString(demLyr));
		System.out.println("demTyr:          "+  NumberToString(demTyr));
		System.out.println("ramtop:          "+  NumberToString(ramtop));
		System.out.println("sc1:             "+  NumberToString(sc1));
		System.out.println("om:              "+  NumberToString(om));
		System.out.println("mm:              "+  NumberToString(mm));
		System.out.println("mCNT:            "+  NumberToString(mCNT));
		System.out.println("mnsCNT:          "+  NumberToString(mnsCNT));
		System.out.println("mtrs:            "+  NumberToString(mtrs));
		System.out.println("relays:          "+  NumberToString(relays));
		System.out.print  ("states:          ");
		for (int i=0;i<6; i++){
			System.out.print(NumberToString(states[i])+" ");
		}
		System.out.println();
		System.out.println("rdghr1:          "+NumberToString(rdghr1));
		System.out.println("rdghr2:          "+NumberToString(rdghr2));
		System.out.println("eopDay:          "+NumberToString(eopDay));
		System.out.println("loVolt:          "+NumberToString(loVolt));
		System.out.println("empty:           "+NumberToString(empty));
		System.out.println("lout:            "+NumberToString(lout));
		System.out.print  ("alarm:           ");
		for (int i=0;i<45; i++){
			System.out.print((int) alarm[i]+" ");
		}
		System.out.println();
		System.out.println("mwCNT:           "+NumberToString(mwCNT));
		System.out.println("prwCNT:	         "+NumberToString(prwCNT));
		System.out.println("liuAcc:          "+NumberToString(liuAcc));
		System.out.println("prec:            "+NumberToString(prec));
		System.out.println("batlow:          "+NumberToString(batlow));
		System.out.println("romBat:          "+NumberToString(romBat));
		System.out.println("ramBat:          "+NumberToString(ramBat));
		System.out.println("tabInv:          "+NumberToString(tabInv));
		System.out.println("datInv:          "+NumberToString(datInv));
		System.out.println("alert:           "+NumberToString(alert));
		System.out.println("pfCNT:           "+NumberToString(pfCNT));
		System.out.println("gfoCNT:          "+NumberToString(gfoCNT));
		System.out.println("mueCNT:          "+NumberToString(mueCNT));
		System.out.println("iueCNT:          "+NumberToString(iueCNT));
		System.out.println("duCNT:           "+NumberToString(duCNT));
		System.out.println("ceCNT:           "+NumberToString(ceCNT));

	}
	/**
	 * @return the clk
	 */
	public MedoCLK getClk() {
		return clk;
	}

	/**
	 * @param clk the clk to set
	 */
	public void setClk(MedoCLK clk) {
		this.clk = clk;
	}

	/**
	 * @return the totals
	 */
	public char getTotals() {
		return totals;
	}

	/**
	 * @param totals the totals to set
	 */
	public void setTotals(char totals) {
		this.totals = totals;
	}

	/**
	 * @return the demCNT
	 */
	public int getDemCNT() {
		return demCNT;
	}

	/**
	 * @param demCNT the demCNT to set
	 */
	public void setDemCNT(int demCNT) {
		this.demCNT = demCNT;
	}

	/**
	 * @return the demLyr
	 */
	public int getDemLyr() {
		return demLyr;
	}

	/**
	 * @param demLyr the demLyr to set
	 */
	public void setDemLyr(int demLyr) {
		this.demLyr = demLyr;
	}

	/**
	 * @return the demTyr
	 */
	public int getDemTyr() {
		return demTyr;
	}

	/**
	 * @param demTyr the demTyr to set
	 */
	public void setDemTyr(int demTyr) {
		this.demTyr = demTyr;
	}

	/**
	 * @return the ramtop
	 */
	public short getRamtop() {
		return ramtop;
	}

	/**
	 * @param ramtop the ramtop to set
	 */
	public void setRamtop(short ramtop) {
		this.ramtop = ramtop;
	}

	/**
	 * @return the sc1
	 */
	public short getSc1() {
		return sc1;
	}

	/**
	 * @param sc1 the sc1 to set
	 */
	public void setSc1(short sc1) {
		this.sc1 = sc1;
	}

	/**
	 * @return the om
	 */
	public int getOm() {
		return om;
	}

	/**
	 * @param om the om to set
	 */
	public void setOm(int om) {
		this.om = om;
	}

	/**
	 * @return the mm
	 */
	public int getMm() {
		return mm;
	}

	/**
	 * @param mm the mm to set
	 */
	public void setMm(int mm) {
		this.mm = mm;
	}

	/**
	 * @return the mCNT
	 */
	public short getMCNT() {
		return mCNT;
	}

	/**
	 * @param mcnt the mCNT to set
	 */
	public void setMCNT(short mcnt) {
		mCNT = mcnt;
	}

	/**
	 * @return the mnsCNT
	 */
	public short getMnsCNT() {
		return mnsCNT;
	}

	/**
	 * @param mnsCNT the mnsCNT to set
	 */
	public void setMnsCNT(short mnsCNT) {
		this.mnsCNT = mnsCNT;
	}

	/**
	 * @return the mtrs
	 */
	public char getMtrs() {
		return mtrs;
	}

	/**
	 * @param mtrs the mtrs to set
	 */
	public void setMtrs(char mtrs) {
		this.mtrs = mtrs;
	}

	/**
	 * @return the relays
	 */
	public char getRelays() {
		return relays;
	}

	/**
	 * @param relays the relays to set
	 */
	public void setRelays(char relays) {
		this.relays = relays;
	}

	/**
	 * @return the states
	 */
	public char[] getStates() {
		return states;
	}

	/**
	 * @param states the states to set
	 */
	public void setStates(char[] states) {
		this.states = states;
		if(states.length>6){
			String s=new String(states);
			this.states=s.substring(0, 6).toCharArray();
		}else if (states.length<6){
			String s=new String(states)+"      ";
			this.states=s.substring(0, 6).toCharArray();			
		}
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
	 * @return the eopDay
	 */
	public char getEopDay() {
		return eopDay;
	}

	/**
	 * @param eopDay the eopDay to set
	 */
	public void setEopDay(char eopDay) {
		this.eopDay = eopDay;
	}

	/**
	 * @return the loVolt
	 */
	public char getLoVolt() {
		return loVolt;
	}

	/**
	 * @param loVolt the loVolt to set
	 */
	public void setLoVolt(char loVolt) {
		this.loVolt = loVolt;
	}

	/**
	 * @return the empty
	 */
	public char getEmpty() {
		return empty;
	}

	/**
	 * @param empty the empty to set
	 */
	public void setEmpty(char empty) {
		this.empty = empty;
	}

	/**
	 * @return the lout
	 */
	public char getLout() {
		return lout;
	}

	/**
	 * @param lout the lout to set
	 */
	public void setLout(char lout) {
		this.lout = lout;
	}

	/**
	 * @return the alarm
	 */
	public char[] getAlarm() {
		return alarm;
	}

	/**
	 * @param alarm the alarm to set
	 */
	public void setAlarm(char[] alarm) {
		this.alarm = alarm;
		if(alarm.length>45){
			String s=new String(alarm);
			this.alarm=s.substring(0, 45).toCharArray();
		}else if (alarm.length<45){
			String s=new String(alarm);
			for(int i=0; i<45; i++){s+=" ";}
			this.alarm=s.substring(0, 45).toCharArray();			
		}
	}

	/**
	 * @return the mwCNT
	 */
	public char getMwCNT() {
		return mwCNT;
	}

	/**
	 * @param mwCNT the mwCNT to set
	 */
	public void setMwCNT(char mwCNT) {
		this.mwCNT = mwCNT;
	}

	/**
	 * @return the prwCNT
	 */
	public char getPrwCNT() {
		return prwCNT;
	}

	/**
	 * @param prwCNT the prwCNT to set
	 */
	public void setPrwCNT(char prwCNT) {
		this.prwCNT = prwCNT;
	}

	/**
	 * @return the liuAcc
	 */
	public char getLiuAcc() {
		return liuAcc;
	}

	/**
	 * @param liuAcc the liuAcc to set
	 */
	public void setLiuAcc(char liuAcc) {
		this.liuAcc = liuAcc;
	}

	/**
	 * @return the prec
	 */
	public char getPrec() {
		return prec;
	}

	/**
	 * @param prec the prec to set
	 */
	public void setPrec(char prec) {
		this.prec = prec;
	}

	/**
	 * @return the batlow
	 */
	public char getBatlow() {
		return batlow;
	}

	/**
	 * @param batlow the batlow to set
	 */
	public void setBatlow(char batlow) {
		this.batlow = batlow;
	}

	/**
	 * @return the romBat
	 */
	public char getRomBat() {
		return romBat;
	}

	/**
	 * @param romBat the romBat to set
	 */
	public void setRomBat(char romBat) {
		this.romBat = romBat;
	}

	/**
	 * @return the ramBat
	 */
	public char getRamBat() {
		return ramBat;
	}

	/**
	 * @param ramBat the ramBat to set
	 */
	public void setRamBat(char ramBat) {
		this.ramBat = ramBat;
	}

	/**
	 * @return the tabInv
	 */
	public char getTabInv() {
		return tabInv;
	}

	/**
	 * @param tabInv the tabInv to set
	 */
	public void setTabInv(char tabInv) {
		this.tabInv = tabInv;
	}

	/**
	 * @return the datInv
	 */
	public char getDatInv() {
		return datInv;
	}

	/**
	 * @param datInv the datInv to set
	 */
	public void setDatInv(char datInv) {
		this.datInv = datInv;
	}

	/**
	 * @return the alert
	 */
	public char getAlert() {
		return alert;
	}

	/**
	 * @param alert the alert to set
	 */
	public void setAlert(char alert) {
		this.alert = alert;
	}

	/**
	 * @return the pfCNT
	 */
	public char getPfCNT() {
		return pfCNT;
	}

	/**
	 * @param pfCNT the pfCNT to set
	 */
	public void setPfCNT(char pfCNT) {
		this.pfCNT = pfCNT;
	}

	/**
	 * @return the gfoCNT
	 */
	public char getGfoCNT() {
		return gfoCNT;
	}

	/**
	 * @param gfoCNT the gfoCNT to set
	 */
	public void setGfoCNT(char gfoCNT) {
		this.gfoCNT = gfoCNT;
	}

	/**
	 * @return the mueCNT
	 */
	public char getMueCNT() {
		return mueCNT;
	}

	/**
	 * @param mueCNT the mueCNT to set
	 */
	public void setMueCNT(char mueCNT) {
		this.mueCNT = mueCNT;
	}

	/**
	 * @return the iueCNT
	 */
	public char getIueCNT() {
		return iueCNT;
	}

	/**
	 * @param iueCNT the iueCNT to set
	 */
	public void setIueCNT(char iueCNT) {
		this.iueCNT = iueCNT;
	}

	/**
	 * @return the duCNT
	 */
	public char getDuCNT() {
		return duCNT;
	}

	/**
	 * @param duCNT the duCNT to set
	 */
	public void setDuCNT(char duCNT) {
		this.duCNT = duCNT;
	}

	/**
	 * @return the ceCNT
	 */
	public char getCeCNT() {
		return ceCNT;
	}

	/**
	 * @param ceCNT the ceCNT to set
	 */
	public void setCeCNT(char ceCNT) {
		this.ceCNT = ceCNT;
	}
	
}
