package com.energyict.protocolimpl.kenda.meteor;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.util.Calendar;
import java.util.TimeZone;

public class MeteorFullPersonalityTable extends Parsers implements MeteorCommandAbstract{
	/*
	 * Implementation of pg 7/16 minus the secure personality details => full expands part
	 * char has been used instead of byte to define 8 bit wide symbols because byte has a sign bit,
	 * drawback is that in java char is 16 bit wide
	 */
	protected MeteorTelno[] destab=new MeteorTelno[4];	// position 1->64
	protected char dumax=0;							// pos 65
	protected short duwait=0;						// pos 66
	protected short lotim=0;						// pos 68
	protected char tabsum=0;						// pos 70
	protected char[] iptab= new char[48];			// pos 71->118
	protected char[] dialexp= new char[48];			// pos 119->166
	protected char[] totexp= new char[48];			// pos 167->214
	protected char[] dialmlt= new char[48];			// pos 215->262
	protected short[] dialdiv= new short[48];		// pos 263->358
	protected MeteorUPI[] sumupi= new MeteorUPI[48];	// pos 259->454
	protected char[] sumtabs=new char[255];		// pos 455->582
	protected char ptv=0;							// pos 710
	protected MeteorRlytab[] rlytab=new MeteorRlytab[16];// pos 711->750
	protected char tots=0;
	protected char demper=0;
	protected short myCode=0;
	protected char slave=0;
	protected char ds1=0;
	protected char ds2=0;
	protected long personality=0;  // byte signed integer (should be 4 byte unsigned)
	protected char wdbattlow=0;
	protected char[] filler=new char[19];
	protected TimeZone timezone;
	protected ProtocolChannelMap meterChannelMap;


	// constructors
	MeteorFullPersonalityTable(){
		char[] c=new char[821];
		for(int i=0; i<821; i++){
			c[i]=0;
		}
		process(c);
	}

	MeteorFullPersonalityTable(byte[] b){
		// incoming data, move them into the right position
		// parse first to char array, then to string
		process(parseBArraytoCArray(b));
	}
	MeteorFullPersonalityTable(char[] c){
		process(c);
	}
	public void processFullPersonalityTable(byte[] b){
		process(parseBArraytoCArray(b));
	}
	public void processFullPersonalityTable(char[] c){
		process(c);
	}
	private void process(char[] c) {
		// dumps byte array in the right fields
		String s = new String(c);
		MeteorTelno mt;
		for(int i=0; i<4; i++){
			mt=new MeteorTelno(s.substring(i*16, 16+i*16));
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
		MeteorUPI mu;
		for (int i=0; i<48;i++){
			mu=new MeteorUPI(s.charAt(358+i),s.charAt(359+i));
			sumupi[i]=mu;
		}
		sumtabs=s.substring(454, 709).toCharArray();
		ptv=s.charAt(709);
		MeteorRlytab mr;
		for (int i=0; i<16;i++){
			mr=new MeteorRlytab(s.charAt(710+i*5),s.charAt(711+i*5),
					s.charAt(712+i*5),
					parseCharToShort(s.substring(713+i*5,715+i*5).toCharArray()));
			rlytab[i]=mr;
		}
		tots=s.charAt(790);
		demper=s.charAt(791);
		myCode=parseCharToShort(s.substring(792,794).toCharArray());
		slave=s.charAt(794);
		ds1=s.charAt(795);
		ds2=s.charAt(796);
		personality=parseCharToLong(s.substring(797, 801).toCharArray());
		wdbattlow=s.charAt(801);
		filler=s.substring(802,820).toCharArray();
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
		serial+=""+ptv;
		for (int i=0; i<rlytab.length; i++){
			serial+=new String(rlytab[i].getcharArray());
		}
		serial+=""+tots;
		serial+=""+demper;
		ca = parseShortToChar(myCode);
		serial+=""+ca[0]+""+ca[1];
		serial+=""+slave;
		serial+=""+ds1;
		serial+=""+ds2;
		serial+=""+new String(parseLongToChar(personality));
		serial+=""+wdbattlow;
		serial+=new String(filler);

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
		for(int i=0; i<48; i++){
			System.out.print(sumupi[i].toString()+" ");
		}
		System.out.println();
		System.out.print("sumtabs:           ");
		for(int ii=0; ii<sumtabs.length; ii++){
			char c=sumtabs[ii];
			System.out.print(c); // is string
		}
		System.out.println();
		System.out.println("ptv:             "+NumberToString(ptv));

		System.out.print  ("rlytab:          ");
		for(int i=0; i<8; i++){
			System.out.print(rlytab[i].toString());
		}
		System.out.println();
		System.out.println("tots:            "+NumberToString(tots));
		System.out.println("demper:          "+NumberToString(demper));
		System.out.println("myCode:          "+NumberToString(myCode));
		System.out.println("slave:           "+NumberToString(slave));
		System.out.println("ds1:             "+NumberToString(ds1));
		System.out.println("ds2:             "+NumberToString(ds2));
		// get datum
		Calendar cal=Calendar.getInstance(timezone);
		cal.set(1970, 0, 1, 0, 0, 0);
		long secs=(long) Math.floor(cal.getTimeInMillis()/1000)+personality;
		cal.setTimeInMillis(1000*secs);
		System.out.println("personality:     "+cal.getTime().toGMTString());
		System.out.println("filler:          "+new String(filler));
	}
	// setters and getters autogenerated

	/**
	 * @return the destab
	 */
	public MeteorTelno[] getDestab() {
		return destab;
	}

	/**
	 * @param destab the destab to set
	 */
	public void setDestab(MeteorTelno[] destab) {
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
	public MeteorUPI[] getSumupi() {
		return sumupi;
	}

	/**
	 * @param sumupi the sumupi to set
	 */
	public void setSumupi(MeteorUPI[] sumupi) {
		this.sumupi = sumupi;
	}

	/**
	 * @return the sumtabs
	 */
	public char[] getSumtabs() {
		return sumtabs;
	}

	/**
	 * @param sumtabs the sumtabs to set
	 */
	public void setSumtabs(char[] sumtabs) {
		this.sumtabs = sumtabs;
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
	public MeteorRlytab[] getRlytab() {
		return rlytab;
	}

	/**
	 * @param rlytab the rlytab to set
	 */
	public void setRlytab(MeteorRlytab[] rlytab) {
		this.rlytab = rlytab;
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
	 * @return the myCode
	 */
	public short getMyCode() {
		return myCode;
	}

	/**
	 * @param myCode the myCode to set
	 */
	public void setMyCode(short myCode) {
		this.myCode = myCode;
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

	/**
	 * @return the ds1
	 */
	public char getDs1() {
		return ds1;
	}

	/**
	 * @param ds1 the ds1 to set
	 */
	public void setDs1(char ds1) {
		this.ds1 = ds1;
	}

	/**
	 * @return the ds2
	 */
	public char getDs2() {
		return ds2;
	}

	/**
	 * @param ds2 the ds2 to set
	 */
	public void setDs2(char ds2) {
		this.ds2 = ds2;
	}

	/**
	 * @return the personality
	 */
	public long getPersonality() {
		return personality;
	}

	/**
	 * @param personality the personality to set
	 */
	public void setPersonality(long personality) {
		this.personality = personality;
	}

	/**
	 * @return the wdbattlow
	 */
	public char getWdbattlow() {
		return wdbattlow;
	}

	/**
	 * @param wdbattlow the wdbattlow to set
	 */
	public void setWdbattlow(char wdbattlow) {
		this.wdbattlow = wdbattlow;
	}

	/**
	 * @return the filler
	 */
	public char[] getFiller() {
		return filler;
	}

	/**
	 * @param filler the filler to set
	 */
	public void setFiller(char[] filler) {
		this.filler = filler;
	}

	public TimeZone getTimezone() {
		return timezone;
	}

	public void setTimezone(TimeZone timezone) {
		this.timezone = timezone;
	}

	public ProtocolChannelMap getMeterChannelMap() {
		return meterChannelMap;
	}
}