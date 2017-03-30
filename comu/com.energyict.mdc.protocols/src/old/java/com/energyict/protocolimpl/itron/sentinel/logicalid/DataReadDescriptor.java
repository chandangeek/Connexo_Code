/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataReadDescriptor.java
 *
 * Created on 3 november 2006, 9:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

/**
 * 
 * @author Koen
 */
public class DataReadDescriptor {

	private int mode;
	private int count;
	private long[] lids;

	/** Creates a new instance of DataReadDescriptor */
	public DataReadDescriptor(int mode, int count, long[] lids) {
		this.mode = mode;
		this.count = count;
		this.lids = lids;
	}

	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("DataReadDescriptor:\n");
		strBuff.append("   count=" + getCount() + "\n");
		for (int i = 0; i < getLids().length; i++) {
			strBuff.append("       lids[" + i + "]=0x" + Long.toHexString(getLids()[i]) + "\n");
		}
		strBuff.append("   mode=" + getMode() + "\n");
		return strBuff.toString();
	}

	public int getMode() {
		return this.mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long[] getLids() {
		return this.lids;
	}

	public void setLids(long[] lids) {
		this.lids = lids;
	}

}
