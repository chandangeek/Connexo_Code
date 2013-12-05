package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import com.energyict.mdc.protocol.device.data.ProfileData;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.profile.Profile;

import java.io.IOException;
import java.util.ArrayList;
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

	private static final int DEBUG 	= 1;
	private static final int NUMBER_OF_CHANNELS = 8;

	private MeterInfo meterInfo 	= null;
	private Profile profile 		= null;

	/*
	 * Constructors
	 */

	public Enerium200() {}


	/*
	 * Abstract methods from ModBus class
	 */

	protected void doTheConnect() throws IOException {}
	protected void doTheDisConnect() throws IOException {}

	protected List doTheGetOptionalKeys() {
		List returnList = new ArrayList();
        returnList.add("Connection");
		return returnList;
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
		if (this.profile == null) this.profile = new Profile(this);
		return this.profile;
	}

    @Override
    public String getProtocolDescription() {
        return "Enerdis Enerium 200";
    }

	public String getProtocolVersion() {
		return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
	}

	/*
	 * Public methods
	 */

    public Date getTime() throws IOException {
    	long timeDiff = new Date().getTime() - getMeterInfo().getReadTime().getTime();
    	Date correctedTime = new Date(getMeterInfo().getMeterTime().getTime() + timeDiff);
    	return correctedTime;
    }

    public void setTime() throws IOException {
    	byte[] rawDate = TimeDateParser.getBytesFromDate(new Date());
    	Utils.writeRawByteValues(getRegFactory().writeFunctionReg.getReg(), Utils.SETCLOCK , rawDate, this);
    }

    private String getSerialNumber() throws IOException {
    	return getMeterInfo().getSerialNumber();
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
    	return "Enerium 200 " + getMeterInfo().getVersion();
    }

    protected void validateSerialNumber() throws IOException {
       if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
       String sn = getSerialNumber();
       if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
       throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
    	return NUMBER_OF_CHANNELS;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
    	return getProfile().getProfileInterval();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
    	ProfileData profileData = new ProfileData();
    	List channelInfos = new ArrayList(0);
    	List intervalDatas = new ArrayList(0);
    	List meterEvents = new ArrayList(0);

    	if (to == null) to = new Date();

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
