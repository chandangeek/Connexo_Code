/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadProfileCompactArray {

	private static final int					UNSIGNED32_LENGTH				= 4;
	private List<LoadProfileCompactArrayEntry>	loadProfileCompactArrayEntries	= new ArrayList<LoadProfileCompactArrayEntry>();

	public LoadProfileCompactArray() {

	}

	public void parse(byte[] data) throws IOException {
		int offset = 0;

		if (data[offset] != 19) {
			return; // if the load profile is empty...
			//throw new IOException("No compact array!");
		}

		offset += 5; // skip compact array tag AND TypeDescription

		int length = (int) DLMSUtils.getAXDRLength(data, offset);
		offset += DLMSUtils.getAXDRLengthOffset(data, offset);

		if ((length % UNSIGNED32_LENGTH) != 0) {
			throw new IOException("Not an integer number of unsigned32 data values in the compact array!");
		}

		for (int i = 0; i < length / UNSIGNED32_LENGTH; i++) {
			Long value = new Long(ProtocolUtils.getLong(data, offset, UNSIGNED32_LENGTH));
			offset += UNSIGNED32_LENGTH;
			loadProfileCompactArrayEntries.add(new LoadProfileCompactArrayEntry(value));
		}

	}

	public List<LoadProfileCompactArrayEntry> getLoadProfileCompactArrayEntries() {
		return loadProfileCompactArrayEntries;
	}

}
