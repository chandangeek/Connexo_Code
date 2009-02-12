package com.energyict.dlms.cosem;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.obis.ObisCode;

public class CapturedObjectsHelper implements DLMSCOSEMGlobals {

	List<CapturedObject> capturedObjects;

	// lazy
	int nrOfChannels = -1;

	public CapturedObjectsHelper(List<CapturedObject> capturedObjects) {
		this.capturedObjects = capturedObjects;
	}

	public int getNrOfCapturedObjects() {
		return capturedObjects.size();
	}

	public CapturedObject getProfileDataChannelCapturedObject(int channelId)
			throws IOException {
		int id = 0;
		for (int index = 0; index < getNrOfCapturedObjects(); index++) {
			if (isChannelData(index)) {
				if (id == channelId)
					return capturedObjects.get(index);
				id++;
			}
		}
		throw new IOException("CapturedObjectsHelper, invalid channelId "
				+ channelId);
	}

	public ObisCode getProfileDataChannelObisCode(int channelId)
			throws IOException {
		int id = 0;
		for (int index = 0; index < getNrOfCapturedObjects(); index++) {
			if (isChannelData(index)) {
				if (id == channelId)
					return capturedObjects.get(index).getLogicalName()
							.getObisCode();
				id++;
			}
		}
		throw new IOException("CapturedObjectsHelper, invalid channelId "
				+ channelId);

	} // public ObisCode getProfileDataChannel(int channelId) throws
		// IOException

	public boolean isClassIdData(int index) throws IOException {
		if (index >= capturedObjects.size())
			throw new IOException("CapturedObjectsHelper, invalid index "+ index);
		CapturedObject co = capturedObjects.get(index);
		return co.getClassId() == DLMSCOSEMGlobals.ICID_DATA;
	}
	
	public boolean isChannelData(int index) throws IOException {
		if (index >= capturedObjects.size())
			throw new IOException("CapturedObjectsHelper, invalid index "
					+ index);
		CapturedObject co = capturedObjects.get(index);
		return isChannelData(co);
	}

	public boolean isChannelData(CapturedObject co) {
		if ((co.getLogicalName().getA() != 0)
				&& // == LN_A_ELECTRICITY_RELATED_OBJECTS) &&
				(co.getLogicalName().getB() >= 0)
				&& // was 1 (KV 06032007)
				(co.getLogicalName().getB() <= 64)
				&& ((co.getClassId() == ICID_REGISTER) || (co.getClassId() == ICID_DEMAND_REGISTER))) {
			return true;
		}
		// Changed GN 29022008 to add the extended register for the Iskra MBus
		// meter
		else if (((co.getLogicalName().getA() == 0) || (co.getLogicalName()
				.getA()) == 7)
				&& (co.getLogicalName().getB() == 1)
				&& (co.getLogicalName().getC() == (byte) 0x80)
				&& (co.getLogicalName().getD() == 50)
				&& (co.getLogicalName().getE() >= 0)
				&& (co.getLogicalName().getE() <= 3)
				&& (co.getClassId() == ICID_EXTENDED_REGISTER)) {
			return true;
		}
		// Changed KV 11022009 to add the mbus register 0-1:24.3.0.255
		else if ((co.getLogicalName().getA() == 0)
				&& (co.getLogicalName().getB() > 0)
				&& (co.getLogicalName().getC() == 24)
				&& (co.getClassId() == ICID_EXTENDED_REGISTER)) {
			return true;
		}
		return false;
	}

	public int getNrOfchannels() {

		if (nrOfChannels == -1) {
			nrOfChannels = 0;
			Iterator<CapturedObject> it = capturedObjects.iterator();
			while (it.hasNext()) {
				CapturedObject co = it.next();
				if (isChannelData(co))
					nrOfChannels++;
			}
		}
		return nrOfChannels;

	} // public int getNrOfchannels()

}
