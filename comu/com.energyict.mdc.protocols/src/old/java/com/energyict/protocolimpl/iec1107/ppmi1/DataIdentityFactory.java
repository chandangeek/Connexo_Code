/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author fbo */

class DataIdentityFactory {

	private Map	rawRegisters;
	private PPM	ppm	= null;

	{
		rawRegisters = new HashMap();

		/* Timeswitch functions */
		add("870", 7, 1, DataIdentity.NORMAL);
		/* Integration period */
		add("878", 3, 1, DataIdentity.NORMAL);
		/* Time sets via rs232 port */
		add("850", 7, 1, DataIdentity.NORMAL);
		/* Clock/Calendar functions */
		add("860", 7, 1, DataIdentity.NORMAL);
		/* Time and date */
		add("861", 7, 1, DataIdentity.NORMAL);
		/* Addresses and codes */
		add("790", 29, 1, DataIdentity.NORMAL);
		/* Register category */
		add("704", 1, 1, DataIdentity.NORMAL);
		/* Miscellaneous configuration details */
		add("770", 6, 1, DataIdentity.NORMAL);
		/* Current Register Data */
		add("500", 225, 5, DataIdentity.NORMAL);
		/* protected configuration data */
		add("700", 25, 1, DataIdentity.NORMAL);
		/* Time of use register allocation */
		add("740", 5, 1, DataIdentity.NORMAL);
		/* Maximum demand register allocation */
		add("750", 5, 1, DataIdentity.NORMAL);
		/* Historical data */
		add("540", 1024, 16, DataIdentity.NORMAL);
		add("550", 0, 16, DataIdentity.PROFILE);

		add("501", 25, 1, DataIdentity.NORMAL);
		add("502", 40, 1, DataIdentity.NORMAL);
		add("503", 128, 2, DataIdentity.NORMAL);
		add("504", 32, 1, DataIdentity.NORMAL);
		add("741", 1, 1, DataIdentity.NORMAL);
		add("742", 1, 1, DataIdentity.NORMAL);
		add("743", 1, 1, DataIdentity.NORMAL);
		add("744", 1, 1, DataIdentity.NORMAL);
		add("745", 1, 1, DataIdentity.NORMAL);
		add("751", 1, 1, DataIdentity.NORMAL);
		add("752", 1, 1, DataIdentity.NORMAL);
		add("753", 1, 1, DataIdentity.NORMAL);
		add("754", 1, 1, DataIdentity.NORMAL);
		add("755", 1, 1, DataIdentity.NORMAL);
		add("774", 1, 1, DataIdentity.NORMAL);
		add("798", 16, 1, DataIdentity.NORMAL);
		add("799", 8, 1, DataIdentity.NORMAL);
		add("795", 8, 1, DataIdentity.NORMAL);
		add("878", 3, 1, DataIdentity.NORMAL);
		add("862", 1, 1, DataIdentity.NORMAL);

	}

	/**
	 * @param ppm
	 */
	public DataIdentityFactory(PPM ppm) {
		this.ppm = ppm;
	}

	/**
	 * @param dataID
	 * @param cached
	 * @param dataLength
	 * @param set
	 * @return
	 * @throws IOException
	 */
	public byte[] getDataIdentity(String dataID, boolean cached, int dataLength, int set) throws IOException {
		try {
			DataIdentity rawRegister = findRawRegister(dataID);
			return rawRegister.readRegister(dataID, cached, (dataLength == -1 ? rawRegister.getLength() : dataLength), set);
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("DataIdentityFactory, getDataIdentity, " + e.getMessage());
		}
	}

	/**
	 * @param dataID
	 * @param value
	 * @throws IOException
	 */
	public void setDataIdentity(String dataID, String value) throws IOException {
		try {
			DataIdentity rawRegister = findRawRegister(dataID);
			rawRegister.writeRegister(dataID, value);
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("DataIdentityFactory, setDataIdentity, " + e.getMessage());
		}
	}

	/**
	 * @return
	 */
	public PPM getPpm() {
		return ppm;
	}

	/**
	 * @param name
	 * @param length
	 * @param nrPackets
	 * @param reverseIndexing
	 */
	private void add(String name, int length, int nrPackets, boolean reverseIndexing) {
		DataIdentity di = new DataIdentity(name, length, nrPackets, reverseIndexing);
		addRawRegister(di);
		di.setDataIdentityFactory(this);
	}

	/**
	 * @param di
	 */
	private void addRawRegister(DataIdentity di) {
		rawRegisters.put(di.getName(), di);
	}

	/**
	 * @param dataID
	 * @return
	 * @throws IOException
	 */
	private DataIdentity findRawRegister(String dataID) throws IOException {
		DataIdentity rawRegister = (DataIdentity) rawRegisters.get(dataID);
		if (rawRegister == null) {
			throw new IOException("DataIdentityFactory, findRawRegister, " + dataID + " does not exist!");
		} else {
			return rawRegister;
		}
	}

}