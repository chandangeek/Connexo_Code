package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.profile.Profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

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

public class Enerium200 extends Modbus implements SerialNumberSupport {

	private static final int DEBUG 	= 1;
	private static final int NUMBER_OF_CHANNELS = 8;

	private MeterInfo meterInfo 	= null;
	private Profile profile 		= null;

	public Enerium200(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheConnect() throws IOException {}

	@Override
	protected void doTheDisConnect() throws IOException {}

    @Override
	public void setProperties(TypedProperties properties) throws PropertyValidationException {
		super.setProperties(properties);
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "500").trim()));
        setInfoTypePhysicalLayer(Integer.parseInt(properties.getTypedProperty(PK_PHYSICAL_LAYER, "1").trim()));
        setInfoTypeResponseTimeout(Integer.parseInt(properties.getTypedProperty(PK_RESPONSE_TIMEOUT, "2000").trim()));
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(),"5000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim()));

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

    @Override
	protected String getRegistersInfo(int extendedLogging) throws IOException {
		String returnValue = "\n******************* Extended logging *******************\n";
		for (int i = 0; i < RegisterFactory.enerium200Registers.size(); i++) {
			Enerium200Register er = (Enerium200Register) RegisterFactory.enerium200Registers.get(i);
			returnValue += er.toString() + "\n";
		}
		returnValue += "********************************************************\n";
		return returnValue;
	}

    @Override
	protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
        getRegisterFactory().setZeroBased(false);
	}

    @Override
	public DiscoverResult discover(DiscoverTools discoverTools) {
        return new DiscoverResult();
	}

	private RegisterFactory getRegFactory() {
		return (RegisterFactory)getRegisterFactory();
	}

	public Profile getProfile() throws IOException {
		if (this.profile == null) {
            this.profile = new Profile(this);
        }
		return this.profile;
	}

    @Override
    public String getProtocolVersion() {
		return "$Date: 2015-11-26 15:25:15 +0200 (Thu, 26 Nov 2015)$";
	}

    @Override
    public Date getTime() throws IOException {
    	long timeDiff = new Date().getTime() - getMeterInfo().getReadTime().getTime();
        return new Date(getMeterInfo().getMeterTime().getTime() + timeDiff);
    }

    @Override
    public void setTime() throws IOException {
    	byte[] rawDate = TimeDateParser.getBytesFromDate(new Date());
    	Utils.writeRawByteValues(getRegFactory().writeFunctionReg.getReg(), Utils.SETCLOCK , rawDate, this);
    }

    @Override
    public String getSerialNumber()  {
        try {
            return getMeterInfo().getSerialNumber();
        } catch (IOException e){
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
    	return "Enerium 200 " + getMeterInfo().getVersion();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
    	return NUMBER_OF_CHANNELS;
    }

    @Override
    public int getProfileInterval() throws IOException {
    	return getProfile().getProfileInterval();
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
    	ProfileData profileData = new ProfileData();
    	List<ChannelInfo> channelInfos;
    	List<IntervalData> intervalDatas;
    	List<MeterEvent> meterEvents = new ArrayList<>();

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

    protected MeterInfo getMeterInfo() throws IOException {
		if (this.meterInfo == null) {
	    	meterInfo = (MeterInfo) getRegFactory().meterInfo.value();
		}
		return meterInfo;
	}

}