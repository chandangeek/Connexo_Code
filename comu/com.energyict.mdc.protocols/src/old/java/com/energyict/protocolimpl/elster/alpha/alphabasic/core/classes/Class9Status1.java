/*
 * Class9Status1.java
 *
 * Created on 20 juli 2005, 11:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author koen
 */
public class Class9Status1 extends AbstractClass {

	private ClassIdentification classIdentification = new ClassIdentification(9,48,false);

	private int XUOMLO;
	private int SYSERR;
	private int SYSWARN;
	private int SYSSTAT;
	private int CUMDDR;
	private int CUMDPUL;
	private int PWRLOG;
	private Date PSTART;
	private Date PEND;
	private int SEARAT;
	private int DOY;
	private Date TD;
	private int TRI;
	private int DATATR;
	private int DATREP;
	private int DATMOD;
	private int CUMDR;
	private int CUMCOMM;
	private int CUMOUT;

	@Override
	public String toString() {
		return "Class9Status1: XUOMLO=0x"+Integer.toHexString(this.XUOMLO)+", "+
		"SYSERR=0x"+Integer.toHexString(this.SYSERR)+", "+
		"SYSWARN=0x"+Integer.toHexString(this.SYSWARN)+", "+
		"SYSSTAT=0x"+Integer.toHexString(this.SYSSTAT)+", "+
		"CUMDDR="+this.CUMDDR+", "+
		"CUMDPUL="+this.CUMDPUL+", "+
		"PWRLOG=0x"+Integer.toHexString(this.PWRLOG)+", "+
		"PSTART="+this.PSTART+", "+
		"PEND="+this.PEND+", "+
		"SEARAT="+Integer.toHexString(this.SEARAT)+", "+
		"DOY="+Integer.toHexString(this.DOY)+", "+
		"TD="+this.TD+", "+
		"TRI="+this.TRI+", "+
		"DATATR="+this.DATATR+", "+
		"DATREP="+this.DATREP+", "+
		"DATMOD="+this.DATMOD+", "+
		"CUMDR="+this.CUMDR+", "+
		"CUMCOMM="+this.CUMCOMM+", "+
		"CUMOUT="+this.CUMOUT;
	}

	/** Creates a new instance of Class9Status1 */
	public Class9Status1(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.XUOMLO = ProtocolUtils.getInt(data,0, 1);
		this.SYSERR = ProtocolUtils.getInt(data,1, 3);
		this.SYSWARN = ProtocolUtils.getInt(data,4, 1);
		this.SYSSTAT = ProtocolUtils.getInt(data,5, 1);
		this.CUMDDR = ProtocolUtils.getBCD2Int(data,6,1);
		this.CUMDPUL = ProtocolUtils.getBCD2Int(data,7,1);
		this.PWRLOG = ProtocolUtils.getInt(data,8, 4);
		this.PSTART = ClassParseUtils.getDate6(data,12, getClassFactory().getAlpha().getTimeZone());
		this.PEND = ClassParseUtils.getDate6(data,18, getClassFactory().getAlpha().getTimeZone());
		this.SEARAT = ProtocolUtils.getInt(data,24, 1);
		this.DOY = ProtocolUtils.getInt(data,25, 2);
		this.TD = ClassParseUtils.getDate6(data,27, getClassFactory().getAlpha().getTimeZone());
		this.TRI = ProtocolUtils.getBCD2Int(data,33,2);
		this.DATATR = ProtocolUtils.getBCD2Int(data,35,3);
		this.DATREP = ProtocolUtils.getBCD2Int(data,38,3);
		this.DATMOD = ProtocolUtils.getBCD2Int(data,41,3);
		this.CUMDR = ProtocolUtils.getBCD2Int(data,44,1);
		this.CUMCOMM = ProtocolUtils.getBCD2Int(data,45,1);
		this.CUMOUT = ProtocolUtils.getBCD2Int(data,46,2);
	}



	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}

	public int getXUOMLO() {
		return this.XUOMLO;
	}

	public int getSYSERR() {
		return this.SYSERR;
	}

	public int getSYSWARN() {
		return this.SYSWARN;
	}

	public int getSYSSTAT() {
		return this.SYSSTAT;
	}

	public int getCUMDDR() {
		return this.CUMDDR;
	}

	public int getCUMDPUL() {
		return this.CUMDPUL;
	}

	public int getPWRLOG() {
		return this.PWRLOG;
	}

	public Date getPSTART() {
		return this.PSTART;
	}

	public Date getPEND() {
		return this.PEND;
	}

	public int getSEARAT() {
		return this.SEARAT;
	}

	public int getDOY() {
		return this.DOY;
	}

	public Date getTD() {
		return this.TD;
	}

	public int getTRI() {
		return this.TRI;
	}

	public int getDATATR() {
		return this.DATATR;
	}

	public int getDATREP() {
		return this.DATREP;
	}

	public int getDATMOD() {
		return this.DATMOD;
	}

	public int getCUMDR() {
		return this.CUMDR;
	}

	public int getCUMCOMM() {
		return this.CUMCOMM;
	}

	public int getCUMOUT() {
		return this.CUMOUT;
	}

}
