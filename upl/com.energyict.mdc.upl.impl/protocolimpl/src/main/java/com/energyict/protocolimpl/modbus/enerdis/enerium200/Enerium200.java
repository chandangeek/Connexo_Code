package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.profile.Profile;

public class Enerium200 extends Modbus {

	private static final int DEBUG 	= 0;

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
	
	protected void doTheConnect() throws IOException {

	}

	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	protected List doTheGetOptionalKeys() {
		List returnList = new ArrayList(0);
		// TODO Auto-generated method stub
		return returnList;
	}

	protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
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

	public RegisterFactory getRegisterFactory() {
		return (RegisterFactory) super.getRegisterFactory();
	}
	
	public Profile getProfile() throws IOException {
		if (this.profile == null) this.profile = new Profile(this);
		return this.profile;
	}
	
	/*
	 * Public methods
	 */

    public Date getTime() throws IOException {
    	return getMeterInfo().getTime();
    }

    public void setTime() throws IOException {
    	byte[] rawDate = TimeDateParser.getBytesFromDate(new Date());
    	Utils.writeRawByteValues(getRegisterFactory().writeFunctionReg.getReg(), Utils.SETCLOCK , rawDate, this);
    }
    
    private String getSerialNumber() throws IOException {
    	return getMeterInfo().getSerialNumber();
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
    	intervalDatas = getProfile().getIntervalDatas(from, to);
    	if (includeEvents) {
    		meterEvents = getProfile().createEvents(intervalDatas);
    	}
    	
    	profileData.setChannelInfos(channelInfos);
    	profileData.setIntervalDatas(intervalDatas);
    	profileData.setMeterEvents(meterEvents);

    	if (includeEvents) {
    		profileData.applyEvents(getProfileInterval());
    	}
    	
    	return profileData;
    }
    
	/*
	 * Public getters and setters
	 */

    private MeterInfo getMeterInfo() throws IOException {
		if (this.meterInfo == null) {
	    	meterInfo = (MeterInfo) getRegisterFactory().meterInfo.value();
	    	meterInfo.printInfo();
		}
		return meterInfo;
	}

}
