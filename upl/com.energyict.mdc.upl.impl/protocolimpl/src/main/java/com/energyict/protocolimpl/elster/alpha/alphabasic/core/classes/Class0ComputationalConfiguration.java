/*
 * Class0ComputationalConfiguration.java
 *
 * Created on 8 juli 2005, 11:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class Class0ComputationalConfiguration extends AbstractClass {

	private ClassIdentification classIdentification = new ClassIdentification(0,15,true);

	private BigDecimal UKH;
	private int UPR;
	private BigDecimal UKE;
	private int INTNORM;
	private int INTTEST;
	private int DPLOCE;  // decimal location applied to all energy values, 1 byte; 0 = no decimal displayed, 1 to 4 = decimal location on display beginning at the right.
	private int DPLOCD; //decimal location applied to all demand values, 1 byte; 0 = no decimal displayed, 1 to 4 = decimal location on display beginning at the right.
	private int NUMSBI;

	@Override
	public String toString() {
		return "Class0ComputationalConfiguration: UKH="+this.UKH+", UPR="+this.UPR+", UKE="+this.UKE+", INTNORM="+this.INTNORM+", INTTEST="+this.INTTEST+", DPLOCE="+this.DPLOCE+", DPLOCD="+this.DPLOCD+", NUMSBI=0x"+Integer.toHexString(this.NUMSBI);
	}

	/** Creates a new instance of Class0ComputationalConfiguration */
	public Class0ComputationalConfiguration(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.UKH = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 0, 3),3);
		this.UPR = ProtocolUtils.getBCD2Int(data, 3, 1);
		this.UKE = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 4, 5),6);
		this.INTNORM = ProtocolUtils.getInt(data,9,1);
		this.INTTEST = ProtocolUtils.getInt(data,10,1);
		this.DPLOCE = ProtocolUtils.getInt(data,11,1);
		this.DPLOCD = ProtocolUtils.getInt(data,12,1);
		this.NUMSBI = ProtocolUtils.getInt(data,13,1);
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}

	public BigDecimal getUKH() {
		return this.UKH;
	}

	public int getUPR() {
		return this.UPR;
	}

	public BigDecimal getUKE() {
		return this.UKE;
	}

	public int getINTNORM() {
		return this.INTNORM;
	}

	public int getINTTEST() {
		return this.INTTEST;
	}

	public int getDPLOCE() {
		return this.DPLOCE;
	}

	public int getDPLOCD() {
		return this.DPLOCD;
	}

	public int getNUMSBI() {
		return this.NUMSBI;
	}

}
