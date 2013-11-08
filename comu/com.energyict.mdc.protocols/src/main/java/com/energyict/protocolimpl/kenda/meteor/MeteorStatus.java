package com.energyict.protocolimpl.kenda.meteor;

import java.util.TimeZone;

public class MeteorStatus extends Parsers implements MeteorCommandAbstract{
	// is private to enable inheritance in METEOR protocol, if not used set private
	/*
	 * Page 9/16 & 10/16
	 */
	
	private MeteorCLK clk=new MeteorCLK();
	private long statusReads=0;
	private long lastCleared=0;
	private long demCNT=0;
	private long otns=0;
	private long omns=0;
	private int ramTop=0;
	private int sc1=0;
	private int sc2=0;
	private long ot =0;
	private long nt =0;
	private int tCNT=0;
	private int tnsCNT=0;
	private long om=0;
	private long nm=0;
	private int mCNT=0;
	private int mnsCNT=0;
	private short relays=0;
	private char mtrs=0;
	private char eopday=0;
	private char clkSet=0;
	private char loVolt=0;
	private char empty=0;	
	private char lout=0;
	private char[] alarm= new char[48];
	private char prec=0;
	private char batLow=0;
	private char romBat=0;
	private char ramBat=0;
	private char tabInv=0;
	private char datInv=0;
	private char alert=0;
	private char pfCNT=0;
	private char gfCNT=0;
	private char mueCNT=0;
	private char iueCNT=0;
	private char pueCNT=0;
	private char duCNT=0;
	private char ceCNT=0;
	private long trimTime=0;
	private char trimmedBy=0;
	private char fepeStat=0;
	private char feCNT=0;
	private char peCNT=0;
	private char[] stFiller=new char[12];
	private TimeZone tz;

	MeteorStatus(){}
	
	MeteorStatus(byte[] b, TimeZone tz){
		this.tz=tz;
		process(parseBArraytoCArray(b));
	}
	MeteorStatus(char[] c, TimeZone tz){
		this.tz=tz;
		process(c);
	}

	private void process(char[] c) {
		String s = new String(c);
		clk=new MeteorCLK(s.substring(0, 6).toCharArray(),tz);
		statusReads=parseCharToLong(s.substring(6,10).toCharArray());
		lastCleared=parseCharToLong(s.substring(10,14).toCharArray());
		demCNT=parseCharToLong(s.substring(14,18).toCharArray());
		otns=parseCharToLong(s.substring(18,22).toCharArray());
		omns=parseCharToLong(s.substring(22,26).toCharArray());
		ramTop=parseCharToInt(s.substring(26,30).toCharArray());
		sc1=parseCharToInt(s.substring(30, 34).toCharArray());
		sc2=parseCharToInt(s.substring(34, 38).toCharArray());
		ot=parseCharToLong(s.substring(38,42).toCharArray());
		nt=parseCharToLong(s.substring(42,46).toCharArray());
		tCNT=parseCharToInt(s.substring(46,50).toCharArray());
		tnsCNT=parseCharToInt(s.substring(50,54).toCharArray());
		om=parseCharToLong(s.substring(54,58).toCharArray());
		nm=parseCharToLong(s.substring(58,62).toCharArray());
		mCNT=parseCharToInt(s.substring(62, 66).toCharArray());
		mnsCNT=parseCharToInt(s.substring(66, 70).toCharArray());
		relays=parseCharToShort(s.substring(70,72).toCharArray());
		mtrs=s.charAt(72);
		eopday=s.charAt(73);
		clkSet=s.charAt(74);
		loVolt=s.charAt(75);
		empty=s.charAt(76);
		lout=s.charAt(77);
		alarm=s.substring(78,78+48).toCharArray();
		prec=s.charAt(126);
		batLow=s.charAt(127);
		romBat=s.charAt(128);
		ramBat=s.charAt(129);
		tabInv=s.charAt(130);
		datInv=s.charAt(131);
		alert=s.charAt(132);
		pfCNT=s.charAt(133);
		gfCNT=s.charAt(134);
		mueCNT=s.charAt(135);
		iueCNT=s.charAt(136);
		pueCNT=s.charAt(137);
		duCNT=s.charAt(138);
		ceCNT=s.charAt(139);
		trimTime=parseCharToLong(s.substring(140,144).toCharArray());
		trimmedBy=s.charAt(144);
		fepeStat=s.charAt(145);
		feCNT=s.charAt(146);
		peCNT=s.charAt(147);
		stFiller=s.substring(148,148+12).toCharArray();
	}
	/* Status can not be written to the device	
	public byte[] parseToByteArray(){

	}
	*/
	public void printData(){
		System.out.println("clk:             "+  clk.toString());
		
		System.out.println("statusReads:     "+  NumberToString(statusReads));
		System.out.println("lastCleared:     "+  NumberToString(lastCleared));
		System.out.println("demCNT:          "+  NumberToString(demCNT));
		System.out.println("otns:            "+  NumberToString(otns));
		System.out.println("omns:            "+  NumberToString(omns));
		System.out.println("ramTop:          "+  NumberToString(ramTop));
		System.out.println("sc1:             "+  NumberToString(sc1));
		System.out.println("sc2:             "+  NumberToString(sc2));
		System.out.println("ot:              "+  NumberToString(ot));
		System.out.println("nt:              "+  NumberToString(nt));
		System.out.println("tCNT:            "+  NumberToString(tCNT));
		System.out.println("tnsCNT:          "+  NumberToString(tnsCNT));
		System.out.println("om:              "+  NumberToString(om));
		System.out.println("nm:              "+  NumberToString(nm));
		System.out.println("mCNT:            "+  NumberToString(mCNT));
		System.out.println("mnsCNT:          "+  NumberToString(mnsCNT));
		System.out.println("relays:          "+  NumberToString(relays));
		System.out.println("mtrs:            "+  NumberToString(mtrs));
		System.out.println("eopDay:          "+  NumberToString(eopday));
		System.out.println("loVolt:          "+  NumberToString(loVolt));
		System.out.println("empty:           "+  NumberToString(empty));
		System.out.println("lout:            "+  NumberToString(lout));
		System.out.print  ("alarm:           ");
		for (int i=0;i<48; i++){
			System.out.print((int) alarm[i]+" ");
		}
		System.out.println();
		System.out.println("prec:            "+  NumberToString(prec));
		System.out.println("batLow:          "+  NumberToString(batLow));
		System.out.println("romBat:          "+  NumberToString(romBat));
		System.out.println("ramBat:          "+  NumberToString(ramBat));
		System.out.println("tabInv:          "+  NumberToString(tabInv));
		System.out.println("datInv:          "+  NumberToString(datInv));
		System.out.println("alert:           "+  NumberToString(alert));
		System.out.println("pfCNT:           "+  NumberToString(pfCNT));
		System.out.println("gfCNT:           "+  NumberToString(gfCNT));
		System.out.println("mueCNT:          "+  NumberToString(mueCNT));
		System.out.println("iueCNT:          "+  NumberToString(iueCNT));
		System.out.println("pueCNT:          "+  NumberToString(pueCNT));		
		System.out.println("duCNT:           "+  NumberToString(duCNT));
		System.out.println("ceCNT:           "+  NumberToString(ceCNT));
		System.out.println("trimTime:        "+  NumberToString(trimTime));
		System.out.println("trimmedBy:       "+  NumberToString(trimmedBy));
		System.out.println("fepeStat:        "+  NumberToString(fepeStat));
		System.out.println("feCNT:           "+  NumberToString(feCNT));
		System.out.println("peCNT:           "+  NumberToString(peCNT));
		System.out.println("stFiller:        "+  new String(stFiller));
	}
	public byte[] parseToByteArray() {
		return null;
	}

	/**
	 * @return the clk
	 */
	public MeteorCLK getClk() {
		return clk;
	}

	/**
	 * @return the statusReads
	 */
	public long getStatusReads() {
		return statusReads;
	}

	/**
	 * @return the lastCleared
	 */
	public long getLastCleared() {
		return lastCleared;
	}

	/**
	 * @return the demCNT
	 */
	public long getDemCNT() {
		return demCNT;
	}

	/**
	 * @return the otns
	 */
	public long getOtns() {
		return otns;
	}

	/**
	 * @return the omns
	 */
	public long getOmns() {
		return omns;
	}

	/**
	 * @return the ramTop
	 */
	public int getRamTop() {
		return ramTop;
	}

	/**
	 * @return the sc1
	 */
	public int getSc1() {
		return sc1;
	}

	/**
	 * @return the sc2
	 */
	public int getSc2() {
		return sc2;
	}

	/**
	 * @return the ot
	 */
	public long getOt() {
		return ot;
	}

	/**
	 * @return the nt
	 */
	public long getNt() {
		return nt;
	}

	/**
	 * @return the tCNT
	 */
	public int getTCNT() {
		return tCNT;
	}

	/**
	 * @return the tnsCNT
	 */
	public int getTnsCNT() {
		return tnsCNT;
	}

	/**
	 * @return the om
	 */
	public long getOm() {
		return om;
	}

	/**
	 * @return the nm
	 */
	public long getNm() {
		return nm;
	}

	/**
	 * @return the mCNT
	 */
	public int getMCNT() {
		return mCNT;
	}

	/**
	 * @return the mnsCNT
	 */
	public int getMnsCNT() {
		return mnsCNT;
	}

	/**
	 * @return the relays
	 */
	public short getRelays() {
		return relays;
	}

	/**
	 * @return the mtrs
	 */
	public char getMtrs() {
		return mtrs;
	}

	/**
	 * @return the eopday
	 */
	public char getEopday() {
		return eopday;
	}

	/**
	 * @return the clkSet
	 */
	public char getClkSet() {
		return clkSet;
	}

	/**
	 * @return the loVolt
	 */
	public char getLoVolt() {
		return loVolt;
	}

	/**
	 * @return the empty
	 */
	public char getEmpty() {
		return empty;
	}

	/**
	 * @return the lout
	 */
	public char getLout() {
		return lout;
	}

	/**
	 * @return the alarm
	 */
	public char[] getAlarm() {
		return alarm;
	}

	/**
	 * @return the prec
	 */
	public char getPrec() {
		return prec;
	}

	/**
	 * @return the batLow
	 */
	public char getBatLow() {
		return batLow;
	}

	/**
	 * @return the romBat
	 */
	public char getRomBat() {
		return romBat;
	}

	/**
	 * @return the ramBat
	 */
	public char getRamBat() {
		return ramBat;
	}

	/**
	 * @return the tabInv
	 */
	public char getTabInv() {
		return tabInv;
	}

	/**
	 * @return the datInv
	 */
	public char getDatInv() {
		return datInv;
	}

	/**
	 * @return the alert
	 */
	public char getAlert() {
		return alert;
	}

	/**
	 * @return the pfCNT
	 */
	public char getPfCNT() {
		return pfCNT;
	}

	/**
	 * @return the gfCNT
	 */
	public char getGfCNT() {
		return gfCNT;
	}

	/**
	 * @return the mueCNT
	 */
	public char getMueCNT() {
		return mueCNT;
	}

	/**
	 * @return the iueCNT
	 */
	public char getIueCNT() {
		return iueCNT;
	}

	/**
	 * @return the pueCNT
	 */
	public char getPueCNT() {
		return pueCNT;
	}

	/**
	 * @return the duCNT
	 */
	public char getDuCNT() {
		return duCNT;
	}

	/**
	 * @return the ceCNT
	 */
	public char getCeCNT() {
		return ceCNT;
	}

	/**
	 * @return the trimTime
	 */
	public long getTrimTime() {
		return trimTime;
	}

	/**
	 * @return the trimmedBy
	 */
	public char getTrimmedBy() {
		return trimmedBy;
	}

	/**
	 * @return the fepeStat
	 */
	public char getFepeStat() {
		return fepeStat;
	}

	/**
	 * @return the feCNT
	 */
	public char getFeCNT() {
		return feCNT;
	}

	/**
	 * @return the peCNT
	 */
	public char getPeCNT() {
		return peCNT;
	}

	/**
	 * @return the stFiller
	 */
	public char[] getStFiller() {
		return stFiller;
	}
	
	/*
	 * Setters for testing purposes only
	 */
	
	/**
	 * @param clk the clk to set
	 */
	public void setClk(MeteorCLK clk) {
		this.clk = clk;
	}

	/**
	 * @param statusReads the statusReads to set
	 */
	public void setStatusReads(long statusReads) {
		this.statusReads = statusReads;
	}

	/**
	 * @param lastCleared the lastCleared to set
	 */
	public void setLastCleared(long lastCleared) {
		this.lastCleared = lastCleared;
	}

	/**
	 * @param demCNT the demCNT to set
	 */
	public void setDemCNT(long demCNT) {
		this.demCNT = demCNT;
	}

	/**
	 * @param otns the otns to set
	 */
	public void setOtns(long otns) {
		this.otns = otns;
	}

	/**
	 * @param omns the omns to set
	 */
	public void setOmns(long omns) {
		this.omns = omns;
	}

	/**
	 * @param ramTop the ramTop to set
	 */
	public void setRamTop(int ramTop) {
		this.ramTop = ramTop;
	}

	/**
	 * @param sc1 the sc1 to set
	 */
	public void setSc1(int sc1) {
		this.sc1 = sc1;
	}

	/**
	 * @param sc2 the sc2 to set
	 */
	public void setSc2(int sc2) {
		this.sc2 = sc2;
	}

	/**
	 * @param ot the ot to set
	 */
	public void setOt(long ot) {
		this.ot = ot;
	}

	/**
	 * @param nt the nt to set
	 */
	public void setNt(long nt) {
		this.nt = nt;
	}

	/**
	 * @param tcnt the tCNT to set
	 */
	public void setTCNT(int tcnt) {
		tCNT = tcnt;
	}

	/**
	 * @param tnsCNT the tnsCNT to set
	 */
	public void setTnsCNT(int tnsCNT) {
		this.tnsCNT = tnsCNT;
	}

	/**
	 * @param om the om to set
	 */
	public void setOm(long om) {
		this.om = om;
	}

	/**
	 * @param nm the nm to set
	 */
	public void setNm(long nm) {
		this.nm = nm;
	}

	/**
	 * @param mcnt the mCNT to set
	 */
	public void setMCNT(int mcnt) {
		mCNT = mcnt;
	}

	/**
	 * @param mnsCNT the mnsCNT to set
	 */
	public void setMnsCNT(int mnsCNT) {
		this.mnsCNT = mnsCNT;
	}

	/**
	 * @param relays the relays to set
	 */
	public void setRelays(short relays) {
		this.relays = relays;
	}

	/**
	 * @param mtrs the mtrs to set
	 */
	public void setMtrs(char mtrs) {
		this.mtrs = mtrs;
	}

	/**
	 * @param eopday the eopday to set
	 */
	public void setEopday(char eopday) {
		this.eopday = eopday;
	}

	/**
	 * @param clkSet the clkSet to set
	 */
	public void setClkSet(char clkSet) {
		this.clkSet = clkSet;
	}

	/**
	 * @param loVolt the loVolt to set
	 */
	public void setLoVolt(char loVolt) {
		this.loVolt = loVolt;
	}

	/**
	 * @param empty the empty to set
	 */
	public void setEmpty(char empty) {
		this.empty = empty;
	}

	/**
	 * @param lout the lout to set
	 */
	public void setLout(char lout) {
		this.lout = lout;
	}

	/**
	 * @param alarm the alarm to set
	 */
	public void setAlarm(char[] alarm) {
		this.alarm = alarm;
	}

	/**
	 * @param prec the prec to set
	 */
	public void setPrec(char prec) {
		this.prec = prec;
	}

	/**
	 * @param batLow the batLow to set
	 */
	public void setBatLow(char batLow) {
		this.batLow = batLow;
	}

	/**
	 * @param romBat the romBat to set
	 */
	public void setRomBat(char romBat) {
		this.romBat = romBat;
	}

	/**
	 * @param ramBat the ramBat to set
	 */
	public void setRamBat(char ramBat) {
		this.ramBat = ramBat;
	}

	/**
	 * @param tabInv the tabInv to set
	 */
	public void setTabInv(char tabInv) {
		this.tabInv = tabInv;
	}

	/**
	 * @param datInv the datInv to set
	 */
	public void setDatInv(char datInv) {
		this.datInv = datInv;
	}

	/**
	 * @param alert the alert to set
	 */
	public void setAlert(char alert) {
		this.alert = alert;
	}

	/**
	 * @param pfCNT the pfCNT to set
	 */
	public void setPfCNT(char pfCNT) {
		this.pfCNT = pfCNT;
	}

	/**
	 * @param gfCNT the gfCNT to set
	 */
	public void setGfCNT(char gfCNT) {
		this.gfCNT = gfCNT;
	}

	/**
	 * @param mueCNT the mueCNT to set
	 */
	public void setMueCNT(char mueCNT) {
		this.mueCNT = mueCNT;
	}

	/**
	 * @param iueCNT the iueCNT to set
	 */
	public void setIueCNT(char iueCNT) {
		this.iueCNT = iueCNT;
	}

	/**
	 * @param pueCNT the pueCNT to set
	 */
	public void setPueCNT(char pueCNT) {
		this.pueCNT = pueCNT;
	}

	/**
	 * @param duCNT the duCNT to set
	 */
	public void setDuCNT(char duCNT) {
		this.duCNT = duCNT;
	}

	/**
	 * @param ceCNT the ceCNT to set
	 */
	public void setCeCNT(char ceCNT) {
		this.ceCNT = ceCNT;
	}

	/**
	 * @param trimTime the trimTime to set
	 */
	public void setTrimTime(long trimTime) {
		this.trimTime = trimTime;
	}

	/**
	 * @param trimmedBy the trimmedBy to set
	 */
	public void setTrimmedBy(char trimmedBy) {
		this.trimmedBy = trimmedBy;
	}

	/**
	 * @param fepeStat the fepeStat to set
	 */
	public void setFepeStat(char fepeStat) {
		this.fepeStat = fepeStat;
	}

	/**
	 * @param feCNT the feCNT to set
	 */
	public void setFeCNT(char feCNT) {
		this.feCNT = feCNT;
	}

	/**
	 * @param peCNT the peCNT to set
	 */
	public void setPeCNT(char peCNT) {
		this.peCNT = peCNT;
	}

	/**
	 * @param stFiller the stFiller to set
	 */
	public void setStFiller(char[] stFiller) {
		this.stFiller = stFiller;
	}
	public TimeZone getTz() {
		return tz;
	}

	public void setTz(TimeZone tz) {
		this.tz = tz;
	}

}
