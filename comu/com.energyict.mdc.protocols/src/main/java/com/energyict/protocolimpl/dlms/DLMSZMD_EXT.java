package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * @author gna
 *
 */
public class DLMSZMD_EXT extends DLMSZMD{

	private int profileInterval;

    @Inject
    public DLMSZMD_EXT(OrmClient ormClient) {
        super(ormClient);
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public int getNumberOfChannels() {
    	return -1;
    }

    public void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException{
    	profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "900"));
    	super.doValidateProperties(properties);
    }

    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
    	if (profileInterval != super.getProfileInterval()){
    		throw new IOException("profile interval setting in eiserver configuration (" + profileInterval + "sec) is different then requested from the meter (" + super.getProfileInterval() + "sec)");
    	}
    	super.getNumberOfChannels();
        return super.getProfileData(lastReading, includeEvents);
    }

    @Override
    public String getProtocolDescription() {
        return "L&G/Siemens ZMD DLMS-SN_EXT";
    }

    /** ProtocolVersion **/
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

}