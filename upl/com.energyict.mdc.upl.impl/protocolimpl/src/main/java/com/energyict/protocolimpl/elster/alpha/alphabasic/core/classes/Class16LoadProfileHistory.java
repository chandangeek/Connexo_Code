/*
 * Class16LoadProfileHistory.java
 *
 * Created on 25 juli 2005, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author koen
 */
public class Class16LoadProfileHistory extends AbstractClass {

	ClassIdentification classIdentification = new ClassIdentification(16,48,false);

	private Date pfstrtl; // 6 bytes start time/date of most recent power outage
	private Date pfstPL; // 6 bytes end time/date of most recent power outage
	private Date pfstRT2; // 6 bytes start time/date of next most recent power outage
	private Date pfstP2; // 6 bytes end time/date of next most recent power outage
	private Date olpmrd; // 5 bytes time/date of last optical lp data read
	private Date rlpmrd; // 5 bytes time/date of last remote lp data read
	private int fullRed; // 2 bytes number of lp words available for a full load profile read (class 17)
	private int partRed; // 2 bytes number of lp words available for a partial load profile read (class 18)
	private int maxLpm; // 2 bytes maximum number of 16 bit words available for load profile recording (reserved for future use)
	//spares 8 set to zero

	int validDates=0;

	/** Creates a new instance of Class16LoadProfileHistory */
	public Class16LoadProfileHistory(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	public String toString() {
		return "Class16LoadProfileHistory: PFSTRTL="+this.pfstrtl+", PFSTPL="+this.pfstPL+", PFSTRT2="+this.pfstRT2+", PFSTP2="+this.pfstP2+", OLPMRD="+this.olpmrd+", RLPMRD="+this.rlpmrd+", FULLRED="+this.fullRed+", PARTRED="+this.partRed+", MAXLPM="+this.maxLpm;
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		if (ProtocolUtils.getLong(data,0,6) != 0) {
			this.validDates |= 0x01;
		}
		if (ProtocolUtils.getLong(data,6,6) != 0) {
			this.validDates |= 0x02;
		}
		if (ProtocolUtils.getLong(data,12,6) != 0) {
			this.validDates |= 0x04;
		}
		if (ProtocolUtils.getLong(data,18,6) != 0) {
			this.validDates |= 0x08;
		}
		//if (ProtocolUtils.getLong(data,24,5) != 0) validDates |= 0x10;
		//if (ProtocolUtils.getLong(data,29,5) != 0) validDates |= 0x20;
		setPFSTRTL(ClassParseUtils.getDate6(data, 0, getClassFactory().getAlpha().getTimeZone()));
		setPFSTPL(ClassParseUtils.getDate6(data, 6, getClassFactory().getAlpha().getTimeZone()));
		setPFSTRT2(ClassParseUtils.getDate6(data, 12, getClassFactory().getAlpha().getTimeZone()));
		setPFSTP2(ClassParseUtils.getDate6(data, 18, getClassFactory().getAlpha().getTimeZone()));
		setOLPMRD(ClassParseUtils.getDate5(data, 24, getClassFactory().getAlpha().getTimeZone()));
		setRLPMRD(ClassParseUtils.getDate5(data, 29, getClassFactory().getAlpha().getTimeZone()));
		setFULLRED(ProtocolUtils.getInt(data,34, 2));
		setPARTRED(ProtocolUtils.getInt(data,36, 2));
		setMAXLPM(ProtocolUtils.getInt(data,38, 2));



	}


	public List getMeterEvents() {
		List meterEvents = new ArrayList();
		if ((this.validDates & 0x01) == 0x01) {
			meterEvents.add(new MeterEvent(getPFSTRTL(),MeterEvent.POWERDOWN));
		}
		if ((this.validDates & 0x02) == 0x02) {
			meterEvents.add(new MeterEvent(getPFSTPL(),MeterEvent.POWERUP));
		}
		if ((this.validDates & 0x04) == 0x04) {
			meterEvents.add(new MeterEvent(getPFSTRT2(),MeterEvent.POWERDOWN));
		}
		if ((this.validDates & 0x08) == 0x08) {
			meterEvents.add(new MeterEvent(getPFSTP2(),MeterEvent.POWERUP));
		}
		return meterEvents;
	}

	@Override
	protected void prepareBuild() throws IOException {
		//        classIdentification.setLength(classFactory.getClass15EventLogConfiguration().getEVSIZE());
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}

	public Date getPFSTRTL() {
		return this.pfstrtl;
	}

	public void setPFSTRTL(Date PFSTRTL) {
		this.pfstrtl = PFSTRTL;
	}

	public Date getPFSTPL() {
		return this.pfstPL;
	}

	public void setPFSTPL(Date PFSTPL) {
		this.pfstPL = PFSTPL;
	}

	public Date getPFSTRT2() {
		return this.pfstRT2;
	}

	public void setPFSTRT2(Date PFSTRT2) {
		this.pfstRT2 = PFSTRT2;
	}

	public Date getPFSTP2() {
		return this.pfstP2;
	}

	public void setPFSTP2(Date PFSTP2) {
		this.pfstP2 = PFSTP2;
	}

	public Date getOLPMRD() {
		return this.olpmrd;
	}

	public void setOLPMRD(Date OLPMRD) {
		this.olpmrd = OLPMRD;
	}

	public Date getRLPMRD() {
		return this.rlpmrd;
	}

	public void setRLPMRD(Date RLPMRD) {
		this.rlpmrd = RLPMRD;
	}

	public int getFULLRED() {
		return this.fullRed;
	}

	public void setFULLRED(int FULLRED) {
		this.fullRed = FULLRED;
	}

	public int getPARTRED() {
		return this.partRed;
	}

	public void setPARTRED(int PARTRED) {
		this.partRed = PARTRED;
	}

	public int getMAXLPM() {
		return this.maxLpm;
	}

	public void setMAXLPM(int MAXLPM) {
		this.maxLpm = MAXLPM;
	}


}