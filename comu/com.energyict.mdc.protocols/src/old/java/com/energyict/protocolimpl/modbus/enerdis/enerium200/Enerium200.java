/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.profile.Profile;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Class that implements the Enerdis Enerium200 modbus protocol.
 * @author  jme
 *
 * Changes:
 * jme:	17/11/2008	->	Initial release of the Enerium 200 protocol
 * jme:	19/03/2009	->	Override of the default values for timing/retries properties with the most
 * 						obvious and working values. The following properties are changed: InterframeTimeout,
 * 						PhysicalLayer, ResponseTimeout, Timeout and Retries.
 *
 */

public class Enerium200 extends Modbus {

	@Override
	public String getProtocolDescription() {
		return "Enerdis Enerium 200 Modbus";
	}

	private static final int NUMBER_OF_CHANNELS = 8;
	private final Clock clock;

	private MeterInfo meterInfo 	= null;
	private Profile profile 		= null;

	@Inject
	public Enerium200(PropertySpecService propertySpecService, Clock clock) {
		super(propertySpecService);
		this.clock = clock;
	}

	protected void doTheConnect() throws IOException {}
	protected void doTheDisConnect() throws IOException {}

	protected List<String> doTheGetOptionalKeys() {
		return Collections.singletonList("Connection");
	}

	protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","500").trim()));
        setInfoTypePhysicalLayer(Integer.parseInt(properties.getProperty("PhysicalLayer","1").trim()));
        setInfoTypeResponseTimeout(Integer.parseInt(properties.getProperty("ResponseTimeout","2000").trim()));
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","5000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty("Retries","5").trim()));

		try {
			Integer.parseInt(getInfoTypeDeviceID());
		} catch (Exception e) {
			setInfoTypeDeviceID("1");
		}

		try {
			getRegistersInfo(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String getRegistersInfo(int extendedLogging) throws IOException {
		String returnValue = "\n******************* Extended logging *******************\n";
		for (int i = 0; i < RegisterFactory.enerium200Registers.size(); i++) {
			Enerium200Register er = (Enerium200Register) RegisterFactory.enerium200Registers.get(i);
			returnValue += er.toString() + "\n";
		}
		returnValue += "********************************************************\n";
		return returnValue;
	}

	protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
        getRegisterFactory().setZeroBased(false);
	}

	public DiscoverResult discover(DiscoverTools discoverTools) {
		DiscoverResult discover = new DiscoverResult();

		return discover;
	}

	/*
	 * Private getters, setters and methods
	 */

	public RegisterFactory getRegFactory() {
		return (RegisterFactory)getRegisterFactory();
	}

	public Profile getProfile() throws IOException {
		if (this.profile == null) {
			this.profile = new Profile(this);
		}
		return this.profile;
	}

    public String getProtocolVersion() {
		return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
	}

	/*
	 * Public methods
	 */

    public Date getTime() throws IOException {
    	long timeDiff = this.clock.millis() - getMeterInfo().getReadTime().getTime();
	    return new Date(getMeterInfo().getMeterTime().getTime() + timeDiff);
    }

    public void setTime() throws IOException {
    	byte[] rawDate = TimeDateParser.getBytesFromDate(new Date());
    	Utils.writeRawByteValues(getRegFactory().writeFunctionReg.getReg(), Utils.SETCLOCK , rawDate, this);
    }

    private String getSerialNumber() throws IOException {
    	return getMeterInfo().getSerialNumber();
    }

    public String getFirmwareVersion() throws IOException {
    	return "Enerium 200 " + getMeterInfo().getVersion();
    }

    protected void validateSerialNumber() throws IOException {
       if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
	       return;
       }
       String sn = getSerialNumber();
       if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
	       return;
       }
       throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }

    public int getNumberOfChannels() throws IOException {
    	return NUMBER_OF_CHANNELS;
    }

    public int getProfileInterval() throws IOException {
    	return getProfile().getProfileInterval();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
    	ProfileData profileData = new ProfileData();
    	List channelInfos;
    	List intervalDatas;
    	List meterEvents = new ArrayList(0);

    	if (to == null) {
		    to = new Date();
	    }

    	channelInfos = getProfile().getChannelInfos();
    	intervalDatas = getProfile().getIntervalDatas(from, to, includeEvents);
    	if (includeEvents) {
    		meterEvents = getProfile().getMeterEvents();
    	}

    	profileData.setChannelInfos(channelInfos);
    	profileData.setIntervalDatas(intervalDatas);
    	profileData.setMeterEvents(meterEvents);

    	if (includeEvents) {
    		profileData.applyEvents(getProfileInterval() / 60);
    	}

    	return profileData;
    }

	/*
	 * Public getters and setters
	 */

    protected MeterInfo getMeterInfo() throws IOException {
		if (this.meterInfo == null) {
	    	meterInfo = (MeterInfo) getRegFactory().meterInfo.value();
		}
		return meterInfo;
	}

}
