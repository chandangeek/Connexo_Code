/*
 * Class33ModemConfigurationInfo.java
 *
 * Created on 11 juli 2005, 15:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class33ModemConfigurationInfo extends AbstractClass {

	private ClassIdentification classIdentification = new ClassIdentification(33,64,false);
	private String comId;
	private String initStr;
	private int twind3;
	private int autoAns;
	private int initDel;
	private int tryDel;
	private int rSpeed;
	private int devNum;
	//RESERVED [12]

	@Override
	public String toString() {
		return "Class33ModemConfigurationInfo: COMID="+this.comId+", INITSTR="+this.initStr+", TWIND3="+this.twind3+", AUTOANS="+this.autoAns+", INITDEL="+this.initDel+", TRYDEL="+this.tryDel+", RSPEED="+this.rSpeed+", DEVNUM="+this.devNum;
	}

	/** Creates a new instance of Class33ModemConfigurationInfo */
	public Class33ModemConfigurationInfo(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected void parse(byte[] data) throws IOException {
		this.comId = new String(ProtocolUtils.getSubArray(data,0, 7));
		this.initStr = new String(ProtocolUtils.getSubArray(data,8, 43));
		this.twind3 = ProtocolUtils.getBCD2Int(data, 44, 2);
		this.autoAns = ProtocolUtils.getInt(data,46, 1);
		this.initDel = ProtocolUtils.getInt(data,47, 1);
		this.tryDel = ProtocolUtils.getInt(data,48, 1);
		this.rSpeed = ProtocolUtils.getInt(data,49, 1);
		this.devNum = ProtocolUtils.getInt(data,50, 1);
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}

	public String getCOMID() {
		return this.comId;
	}

	public String getINITSTR() {
		return this.initStr;
	}

	public int getTWIND3() {
		return this.twind3;
	}

	public int getAUTOANS() {
		return this.autoAns;
	}

	public int getINITDEL() {
		return this.initDel;
	}

	public int getTRYDEL() {
		return this.tryDel;
	}

	public int getRSPEED() {
		return this.rSpeed;
	}

	public int getDEVNUM() {
		return this.devNum;
	}


}
