package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.protocol.MeterExceptionInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

/** @author fbo */

class DataIdentityFactory {
   
	private Map rawRegisters; 
	private PPM ppm = null; 
	private MeterExceptionInfo meterExceptionInfo = null;
	//private PPMMeterType meterType; // KV 22072005 unused

	public DataIdentityFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo,
			PPMMeterType meterType) {

		this.ppm = ppm;
		this.meterExceptionInfo = meterExceptionInfo;
		//this.meterType = meterType; // KV 22072005 unused

	}
 
	{
		rawRegisters = new HashMap();

		add("878", 3, 1, DataIdentity.NORMAL);
		add("850", 7, 1, DataIdentity.NORMAL);
		add("860", 7, 1, DataIdentity.NORMAL);
		add("861", 7, 1, DataIdentity.NORMAL);
		add("798", 16,1, DataIdentity.NORMAL);
		add("795", 8, 1, DataIdentity.NORMAL);
		add("704", 1, 1, DataIdentity.NORMAL);
		add("774", 1, 1, DataIdentity.NORMAL);
		add("501", 25, 1, DataIdentity.NORMAL);
		add("502", 40, 1, DataIdentity.NORMAL);
		add("503", 128, 2, DataIdentity.NORMAL);
		add("504", 32, 1, DataIdentity.NORMAL);
		add("741", 1, 1, DataIdentity.NORMAL );
		add("742", 1, 1, DataIdentity.NORMAL );
		add("743", 1, 1, DataIdentity.NORMAL );
		add("744", 1, 1, DataIdentity.NORMAL );
		add("745", 1, 1, DataIdentity.NORMAL ); 
		add("751", 1, 1, DataIdentity.NORMAL );
		add("752", 1, 1, DataIdentity.NORMAL );
		add("753", 1, 1, DataIdentity.NORMAL );
		add("754", 1, 1, DataIdentity.NORMAL );
		add("755", 1, 1, DataIdentity.NORMAL );
		add("540", 1024, 16, DataIdentity.NORMAL );
                add("541", 256, 4, DataIdentity.NORMAL );
		add("550", 0, 16, DataIdentity.PROFILE); 
        // 550 has dummy packet length, see Profile

		
	}

	byte[] getDataIdentity(String dataID, boolean cached, int dataLength,
			int set) throws IOException {

		try {
			DataIdentity rawRegister = findRawRegister(dataID);
			return rawRegister.readRegister(dataID, cached, (dataLength == -1
					? rawRegister.getLength()
					: dataLength), set);
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("DataIdentityFactory, getDataIdentity, "
					+ e.getMessage());
		}

	}

	void setDataIdentity(String dataID, String value) throws IOException {

		try {
			DataIdentity rawRegister = findRawRegister(dataID);
			rawRegister.writeRegister(dataID, value);
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("DataIdentityFactory, setDataIdentity, "
					+ e.getMessage());
		}

	}

	MeterExceptionInfo getMeterExceptionInfo() {
		return meterExceptionInfo;
	}

	Map getRawRegisters() {
		return rawRegisters;
	}

	PPM getPpm() {
		return ppm;
	}

	private void add(String name, int length, int nrPackets, boolean streameable) {
		DataIdentity di = new DataIdentity(name, length, nrPackets, streameable);
		addRawRegister(di);
		di.setDataIdentityFactory(this);
	}

	private void addRawRegister(DataIdentity di) {
		rawRegisters.put(di.getName(), di);
	}

	private DataIdentity findRawRegister(String dataID) throws IOException {

		DataIdentity rawRegister = (DataIdentity) rawRegisters.get(dataID);
		if (rawRegister == null)
			throw new IOException("DataIdentityFactory, findRawRegister, "
					+ dataID + " does not exist!");
		else
			return rawRegister;

	}

}