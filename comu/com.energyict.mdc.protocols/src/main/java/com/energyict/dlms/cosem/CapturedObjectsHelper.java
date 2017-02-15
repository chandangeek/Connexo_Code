/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.ProtocolException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CapturedObjectsHelper {

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
				if (id == channelId) {
					return capturedObjects.get(index);
				}
				id++;
			}
		}
		throw new ProtocolException("CapturedObjectsHelper, invalid channelId "
				+ channelId);
	}

	public ObisCode getProfileDataChannelObisCode(int channelId)
			throws IOException {
		int id = 0;
		for (int index = 0; index < getNrOfCapturedObjects(); index++) {
			if (isChannelData(index)) {
				if (id == channelId) {
					return capturedObjects.get(index).getLogicalName()
							.getObisCode();
				}
				id++;
			}
		}
		throw new ProtocolException("CapturedObjectsHelper, invalid channelId "
				+ channelId);

	} // public ObisCode getProfileDataChannel(int channelId) throws
		// IOException

	public boolean isClassIdData(int index) throws IOException {
		if (index >= capturedObjects.size()) {
			throw new ProtocolException("CapturedObjectsHelper, invalid index "+ index);
		}
		CapturedObject co = capturedObjects.get(index);
		return co.getClassId() == DLMSClassId.DATA.getClassId();
	}

	public boolean isChannelData(int index) throws IOException {
		if (index >= capturedObjects.size()) {
			throw new ProtocolException("CapturedObjectsHelper, invalid index "
					+ index);
		}
		CapturedObject co = capturedObjects.get(index);
		return isChannelData(co);
	}

	public boolean isChannelData(CapturedObject co) {
		// Default decision logic...
		// A is not equal to 0 (which is for abstract objects.
		// B is between 1 and 64 (channels) or between 128 and 199 (manufacturer specific).
		// The class should be REGISTER of DEMAND REGISTER.
		//
		// Blue book 5.3
		if ((co.getLogicalName().getA() != 0)
			&& (((co.getLogicalName().getB() >= 0) && (co.getLogicalName().getB() <= 64)) || ((co.getLogicalName().getB() >= 128) && (co.getLogicalName().getB() <= 199)))
			&& ((co.getClassId() == DLMSClassId.REGISTER.getClassId()) || (co.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()))) {
			return true;
		}

		// Changed GN 29022008 to add the extended register for the Iskra MBus
		// meter
		else if (((co.getLogicalName().getA() == 0) || ((co.getLogicalName()
				.getA()) == 7))
				&& (co.getLogicalName().getB() == 1)
				&& (co.getLogicalName().getC() == (byte) 0x80)
				&& (co.getLogicalName().getD() == 50)
				&& (co.getLogicalName().getE() >= 0)
				&& (co.getLogicalName().getE() <= 3)
				&& (co.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId())) {
			return true;
		}
		// Changed KV 11022009 to add the mbus register 0-1:24.3.0.255
		else if ((co.getLogicalName().getA() == 0)
				&& (co.getLogicalName().getB() >= 0)
				&& (co.getLogicalName().getC() == 24)
				&& (co.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId())
                && (co.getAttributeIndex() != 4)) {
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
				if (isChannelData(co)) {
					nrOfChannels++;
				}
			}
		}
		return nrOfChannels;

	} // public int getNrOfchannels()

}
