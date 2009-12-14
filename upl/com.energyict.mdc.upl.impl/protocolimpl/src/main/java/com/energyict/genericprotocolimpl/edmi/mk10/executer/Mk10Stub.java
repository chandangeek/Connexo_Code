package com.energyict.genericprotocolimpl.edmi.mk10.executer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.edmi.mk10.MK10;

/**
 * Stub MK10Protocol used for testing
 *
 * @author jme
 *
 */
public class Mk10Stub extends MK10 {

	private static final String		SERIAL_NUMBER		= "209435639";
	private static final String		FIRMWARE_VERSION	= "MK10 dummy firmware version v1.0";
	private static final boolean	FAIL_ON_REGISTERS	= false;
	private static final int		PROFILE_INTERVAL	= 1800;
	private static final int		NUMBER_OF_CHANNELS	= 4;
	private static final int		TIME_DIFF			= 3;

	@Override
	protected void doConnect() throws IOException {
		// Do nothing
	}

	@Override
	protected void doDisConnect() throws IOException {
		// Do nothing
	}

	@Override
	public void setTime() throws IOException {
		// Do nothing
	}

	@Override
	public Date getTime() throws IOException {
		Date now = new Date();
		return new Date(now.getTime() - (1000 * TIME_DIFF));
	}

	@Override
	public String getSerialNumber() throws IOException {
		return SERIAL_NUMBER;
	}

	@Override
	public int getProfileInterval() throws UnsupportedException, IOException {
		return PROFILE_INTERVAL;
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return FIRMWARE_VERSION;
	}

	@Override
	public int getNumberOfChannels() throws UnsupportedException, IOException {
		return NUMBER_OF_CHANNELS;
	}

	@Override
	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		return getProfileData(new Date(), includeEvents);
	}

	@Override
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	@Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		ProfileData profileData = new ProfileData();
		return profileData;
	}

	@Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		if (FAIL_ON_REGISTERS) {
			throw new NoSuchRegisterException(obisCode + " not found.");
		} else {
			RegisterValue registerValue = new RegisterValue(obisCode);
			BigDecimal value = new BigDecimal(1503.36F + obisCode.getF());
			Unit unit = Unit.get("kWh");
			registerValue.setQuantity(new Quantity(value, unit));
			return registerValue;
		}
	}

}
