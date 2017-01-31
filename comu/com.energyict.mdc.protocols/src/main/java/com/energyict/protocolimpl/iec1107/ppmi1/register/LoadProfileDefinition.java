/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Created on Jul 23, 2004
 *
 */
package com.energyict.protocolimpl.iec1107.ppmi1.register;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** @author fbo */

public class LoadProfileDefinition {

	private boolean		importKW		= false;
	private boolean		exportKW		= false;
	private boolean		importKvar		= false;
	private boolean		exportKvar		= false;
	private boolean		totalKVA		= false;

	private int			nrOfChannels	= 0;
	private List		list			= null;
	private List		channelInfoList	= null;

	private static final int BYTE_MASK	= 0x0FF;

	private static final Unit[] UNITS = {
		Unit.get(BaseUnit.WATT, 3),
		Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3),
		Unit.get(BaseUnit.VOLTAMPERE, 3)
	};

	public LoadProfileDefinition(boolean importKW, boolean exportKW,
			boolean importKvar, boolean exportKvar, boolean totalKVA) {

		this.importKW = importKW;
		this.exportKW = exportKW;
		this.importKvar = importKvar;
		this.exportKvar = exportKvar;
		this.totalKVA = totalKVA;

		if( importKW ) {
			nrOfChannels ++;
		}
		if( exportKW ) {
			nrOfChannels ++;
		}
		if( importKvar ) {
			nrOfChannels ++;
		}
		if( exportKvar ) {
			nrOfChannels ++;
		}
		if( totalKVA ) {
			nrOfChannels ++;
		}

	}

	public LoadProfileDefinition(byte[] ba) throws IOException {
		long channelMask = ba[0] & BYTE_MASK;

		long i = 1;
		if ((channelMask & i) != 0) {
			importKW = true;
			nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			exportKW = true;
			nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			importKvar = true;
			nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			exportKvar = true;
			nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			totalKVA = true;
			nrOfChannels++;
		}

	}

	public boolean hasImportKW() {
		return importKW;
	}

	public boolean hasExportKW() {
		return exportKW;
	}

	public boolean hasImportKvar() {
		return importKvar;
	}

	public boolean hasExportKvar() {
		return exportKvar;
	}

	public boolean hasTotalKVA() {
		return totalKVA;
	}

	public int getNrOfChannels() {
		return nrOfChannels;
	}

	public List toList() {
		if (this.list == null) {
			list = new ArrayList();
			int i = 0;
			if (hasImportKW()) {
				list.add(i++, UNITS[0]);
			}
			if (hasExportKW()) {
				list.add(i++, UNITS[0]);
			}
			if (hasImportKvar()) {
				list.add(i++, UNITS[1]);
			}
			if (hasExportKvar()) {
				list.add(i++, UNITS[1]);
			}
			if (hasTotalKVA()) {
				list.add(i, UNITS[2]);
			}
		}
		return list;
	}

	public List toChannelInfoList() {
		if (this.channelInfoList == null) {
			channelInfoList = new ArrayList();
			int i = 0;
			if (hasImportKW()) {
				channelInfoList.add(new ChannelInfo(i, "Import KW", UNITS[0]));
				i++;
			}
			if (hasExportKW()) {
				channelInfoList.add(new ChannelInfo(i, "Export KW", UNITS[0]));
				i++;
			}
			if (hasImportKvar()) {
				channelInfoList.add(new ChannelInfo(i, "Import kvar", UNITS[1]));
				i++;
			}
			if (hasExportKvar()) {
				channelInfoList.add(new ChannelInfo(i, "Export kvar", UNITS[1]));
				i++;
			}
			if (hasTotalKVA()) {
				channelInfoList.add(new ChannelInfo(i, "Total kVA", UNITS[2]));
				i++;
			}
		}
		return channelInfoList;
	}


	public String toString() {
		String ret = "LoadProfileDefinition ";
		if (importKW) {
			ret += "importKw=true,";
		}
		if (exportKW) {
			ret += "exportKW=true,";
		}
		if (importKvar) {
			ret += "importKvar=true,";
		}
		if (exportKvar) {
			ret += "exportKvar=true,";
		}
		if (totalKVA) {
			ret += "totalKVA=true,";
		}
		ret += " NrOfChannels=" + this.nrOfChannels;
		return ret;
	}

}