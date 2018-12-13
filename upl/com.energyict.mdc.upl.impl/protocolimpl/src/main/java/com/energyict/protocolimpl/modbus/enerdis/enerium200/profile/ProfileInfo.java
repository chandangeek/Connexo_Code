package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileInfo {

	private static final int DEBUG 						= 0;
	private static final int PROFILEINFOENTRIES			= 32;
	private static final int PROFILEINFOENTRY_LENGTH	= 16;

	private static final int PROFILE_INFO_ADDRESS		= 0x6300;
	private static final int PROFILE_INFO_LENGTH		= 0x0100;

	private List profileEntries				= null;
	private byte[] rawData					= null;
	private Modbus modBus					= null;

	/*
	 * Constructors
	 */

	public ProfileInfo(Modbus modBus) throws IOException {
		this.modBus = modBus;
		parse(readProfileInfo());
	}

	/*
	 * Private getters, setters and methods
	 */

	// TODO Auto-generated Private getters, setters and methods stub

	/*
	 * Public methods
	 */

	private void parse(byte[] byteArray) throws IOException {
		if (byteArray.length != (PROFILEINFOENTRY_LENGTH * PROFILEINFOENTRIES)) {
			throw new ProtocolException(
					" Error in ProfileInfo.parse(). Wrong data length: " + byteArray.length +
					" Data = " + ProtocolUtils.getResponseData(byteArray)
					);
		}

		profileEntries = new ArrayList(0);

		for (int i = 0; i < PROFILEINFOENTRIES; i++) {
			byte[] tempData = ProtocolUtils.getSubArray2(byteArray, i * PROFILEINFOENTRY_LENGTH, PROFILEINFOENTRY_LENGTH);
			profileEntries.add(new ProfileInfoEntry(tempData, this.modBus));
		}

		for (int i = 0; i < profileEntries.size(); i++) {
			ProfileInfoEntry b = (ProfileInfoEntry) profileEntries.get(i);
			if (DEBUG >= 1) System.out.println(b.toString());
		}

	}

	private byte[] readProfileInfo() throws IOException {
		return Utils.readByteValues(PROFILE_INFO_ADDRESS, PROFILE_INFO_LENGTH, this.modBus);
	}

	public int getProfileInterval() {
		ProfileInfoEntry pie = (ProfileInfoEntry) profileEntries.get(0);
		return pie.getInterval();
	}

	/*
	 * Public getters and setters
	 */

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProfileInfo:\n");
        for (int i = 0; i < profileEntries.size(); i++) {
			strBuff.append(" " + profileEntries.get(i).toString());
		}

        return strBuff.toString();
    }

	public List generateProfileParts() {
		List returnList = new ArrayList(0);
		for (int i = 0; i < profileEntries.size(); i++) {
			returnList.add(new ProfilePart((ProfileInfoEntry) profileEntries.get(i), modBus));
		}
		return returnList;
	}

}
