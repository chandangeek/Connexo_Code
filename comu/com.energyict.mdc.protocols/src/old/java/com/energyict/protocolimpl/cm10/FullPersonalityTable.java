/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class FullPersonalityTable {

	private CM10 cm10Protocol;

	private int duMax;
	private int duWait;
	private int loTim;
	private int tabSum;
	private int[] ipTab = new int[48];
	private int[] dExp = new int[48];  // power of ten factors for each dial reading
	private int[] totExp = new int[48];
	private int[] dMlt = new int[48]; // multipliers = dMlt * dExp
	private int[] dDiv = new int[48];
	private int demper; // interval


	public FullPersonalityTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}

	public void parse(byte[] data) throws IOException {
		// skip phone number (4 * 16 bytes)
		duMax = data[64];
		duWait = ProtocolUtils.getIntLE(data, 65, 2);
		loTim = ProtocolUtils.getIntLE(data, 67, 2);
		tabSum = data[69];
		int startIndex = 70;
		for (int i = 0; i < 48; i++)
			ipTab[i] = data[startIndex + i];
		startIndex = startIndex + 48;
		for (int i = 0; i < 48; i++)
			dExp[i] = data[startIndex + i];
		startIndex = startIndex + 48;
		for (int i = 0; i < 48; i++)
			totExp[i] = data[startIndex + i];
		startIndex = startIndex + 48;
		for (int i = 0; i < 48; i++)
			dMlt[i] = data[startIndex + i];
		demper = data[data.length - 3 - 1];
	}

	public int getIntervalInMinutes() {
		return demper;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("");
		buf.append("duMax: " + duMax).append("\n");
		buf.append("duWait: " + duWait).append("\n");
		buf.append("loTim: " + loTim).append("\n");
		buf.append("tabSum: " + tabSum).append("\n");
		for (int i = 0; i < 48; i++)
			buf.append("ipTab " + i + ": " + ipTab[i]).append("\n");

		for (int i = 0; i < 48; i++)
			buf.append("dExp " + i + ": " + dExp[i]).append("\n");

		for (int i = 0; i < 48; i++)
			buf.append("totExp " + i + ": " + totExp[i]).append("\n");

		for (int i = 0; i < 48; i++)
			buf.append("dMlt " + i + ": " + dMlt[i]).append("\n");

		buf.append("demper: " + demper).append("\n");
		return buf.toString();
	}

	public int[] getMultipliers() {
		int[] multipliers = new int[48];
		for (int i = 0; i < 48; i++) {
			multipliers[i] = (int) (Math.pow(10, dExp[i]) * dMlt[i]);
			//cm10Protocol.getLogger().info("multiplier " + i + ": " + multipliers[i]);
		}
		return multipliers;
	}

}
