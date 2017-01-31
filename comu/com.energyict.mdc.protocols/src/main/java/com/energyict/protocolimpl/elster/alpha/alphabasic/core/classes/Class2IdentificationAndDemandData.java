/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * class1IdentificationAndDemandData.java
 *
 * Created on 12 juli 2005, 10:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class2IdentificationAndDemandData extends AbstractClass {

	ClassIdentification classIdentification = new ClassIdentification(2,52,true);


	static public final int IMPORT = 1;
	static public final int EXPORT = 0;

	private long umtrSn;
	private String acctId;
	// pad 14 bytes
	private int kwovrl;
	private int kwthrsA;
	private int kwthrsB;
	private int kwthrsC;
	private int kwthrsD;
	private int e2kyzdv;
	private int emetflg;
	private int eatrVal;

	@Override
	public String toString() {
		return "Class2IdentificationAndDemandData: UMTRSN="+this.umtrSn+", ACCTID="+this.acctId+", KWOVRL="+this.kwovrl+", KWTHRSA="+this.kwthrsA+", KWTHRSB="+this.kwthrsB+", KWTHRSC="+this.kwthrsC+", KWTHRSD="+this.kwthrsD+", E2KYZDV="+this.e2kyzdv+
		", EMETFLG=0x"+Integer.toHexString(this.emetflg)+", EATRVAL="+this.eatrVal+", isSingleRate="+isSingleRate();
	}

	/** Creates a new instance of class1IdentificationAndDemandData */
	public Class2IdentificationAndDemandData(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.umtrSn = ParseUtils.getBCD2Long(data,0, 5);
		this.acctId = new String(ProtocolUtils.getSubArray2(data, 5,14));
		this.kwovrl = ProtocolUtils.getBCD2Int(data, 33, 3);
		this.kwthrsA = ProtocolUtils.getBCD2Int(data, 36, 3);
		this.kwthrsB = ProtocolUtils.getBCD2Int(data, 39, 3);
		this.kwthrsC = ProtocolUtils.getBCD2Int(data, 42, 3);
		this.kwthrsD = ProtocolUtils.getBCD2Int(data, 45, 3);
		this.e2kyzdv = ProtocolUtils.getInt(data,48,1);
		this.emetflg = ProtocolUtils.getInt(data,49,1);
		this.eatrVal = ProtocolUtils.getInt(data,50,1);
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}


	public long getUMTRSN() {
		return this.umtrSn;
	}

	public String getACCTID() {
		return this.acctId;
	}

	public int getKWOVRL() {
		return this.kwovrl;
	}

	public int getKWTHRSA() {
		return this.kwthrsA;
	}

	public int getKWTHRSB() {
		return this.kwthrsB;
	}

	public int getKWTHRSC() {
		return this.kwthrsC;
	}

	public int getKWTHRSD() {
		return this.kwthrsD;
	}

	public int getE2KYZDV() {
		return this.e2kyzdv;
	}

	public int getEMETFLG() {
		return this.emetflg;
	}

	public int getEATRVAL() {
		return this.eatrVal;
	}

	/*
	 *  @result boolean single or 4 rate meter
	 */
	public boolean isSingleRate() {
		return (getEMETFLG() & 0x0400) == 0x0400;
	}

	/*
	 *  @result int zero based single rate rate
	 */
	public int getSingleRate() {
		return (getEMETFLG() >> 6) & 0x0003;
	}





}
