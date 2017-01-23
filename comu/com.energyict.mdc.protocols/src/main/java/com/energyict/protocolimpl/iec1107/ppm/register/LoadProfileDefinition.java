/*
 * Created on Jul 23, 2004
 *
 */
package com.energyict.protocolimpl.iec1107.ppm.register;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** @author fbo */

public class LoadProfileDefinition {

	private boolean importKW = false;
	private boolean exportKW = false;
	private boolean importKvar = false;
	private boolean exportKvar = false;
	private boolean totalKVA = false;

	private int nrOfChannels = 0;

	private ArrayList list = null;
	private ArrayList channelInfoList = null;

	final static Unit[] units = { Unit.get(BaseUnit.WATT, 3), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3), Unit.get(BaseUnit.VOLTAMPERE, 3) };

	public LoadProfileDefinition(boolean importKW, boolean exportKW, boolean importKvar, boolean exportKvar, boolean totalKVA) {

		this.importKW = importKW;
		this.exportKW = exportKW;
		this.importKvar = importKvar;
		this.exportKvar = exportKvar;
		this.totalKVA = totalKVA;

		if (importKW) {
			this.nrOfChannels++;
		}
		if (exportKW) {
			this.nrOfChannels++;
		}
		if (importKvar) {
			this.nrOfChannels++;
		}
		if (exportKvar) {
			this.nrOfChannels++;
		}
		if (totalKVA) {
			this.nrOfChannels++;
		}

	}

	public LoadProfileDefinition(byte[] ba) throws IOException {
		long channelMask = ba[0] & 0xFF;

		long i = 1;
		if ((channelMask & i) != 0) {
			this.importKW = true;
			this.nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			this.exportKW = true;
			this.nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			this.importKvar = true;
			this.nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			this.exportKvar = true;
			this.nrOfChannels++;
		}

		i <<= 1;
		if ((channelMask & i) != 0) {
			this.totalKVA = true;
			this.nrOfChannels++;
		}

	}

	public boolean hasImportKW() {
		return this.importKW;
	}

	public boolean hasExportKW() {
		return this.exportKW;
	}

	public boolean hasImportKvar() {
		return this.importKvar;
	}

	public boolean hasExportKvar() {
		return this.exportKvar;
	}

	public boolean hasTotalKVA() {
		return this.totalKVA;
	}

	public int getNrOfChannels() {
		return this.nrOfChannels;
	}

	public List toList() {
		if (this.list == null) {
			this.list = new ArrayList();
			int i = 0;
			if (hasImportKW()) {
				this.list.add(i++, units[0]);
			}
			if (hasExportKW()) {
				this.list.add(i++, units[0]);
			}
			if (hasImportKvar()) {
				this.list.add(i++, units[1]);
			}
			if (hasExportKvar()) {
				this.list.add(i++, units[1]);
			}
			if (hasTotalKVA()) {
				this.list.add(i, units[2]);
			}
		}
		return this.list;
	}

	public List toChannelInfoList() {
		if (this.channelInfoList == null) {
			this.channelInfoList = new ArrayList();
			int i = 0;
			if (hasImportKW()) {
				this.channelInfoList.add(new ChannelInfo(i, "Import KW", units[0]));
				i++;
			}
			if (hasExportKW()) {
				this.channelInfoList.add(new ChannelInfo(i, "Export KW", units[0]));
				i++;
			}
			if (hasImportKvar()) {
				this.channelInfoList.add(new ChannelInfo(i, "Import kvar", units[1]));
				i++;
			}
			if (hasExportKvar()) {
				this.channelInfoList.add(new ChannelInfo(i, "Export kvar", units[1]));
				i++;
			}
			if (hasTotalKVA()) {
				this.channelInfoList.add(new ChannelInfo(i, "Total kVA", units[2]));
				i++;
			}
		}
		return this.channelInfoList;
	}

	public String toString() {
		String ret = "LoadProfileDefinition ";
		if (this.importKW) {
			ret += "importKw=true,";
		}
		if (this.exportKW) {
			ret += "exportKW=true,";
		}
		if (this.importKvar) {
			ret += "importKvar=true,";
		}
		if (this.exportKvar) {
			ret += "exportKvar=true,";
		}
		if (this.totalKVA) {
			ret += "totalKVA=true,";
		}
		ret += " NrOfChannels=" + this.nrOfChannels;
		return ret;
	}

}