package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * @author gna
 *
 */
@Deprecated
public class DLMSZMD_EXT extends DLMSZMD {

	private int profileInterval;

    @Inject
    public DLMSZMD_EXT(PropertySpecService propertySpecService, OrmClient ormClient, CodeFactory codeFactory, UserFileFactory userFileFactory) {
        super(propertySpecService, ormClient, codeFactory, userFileFactory);
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

    /** ProtocolVersion **/
    public String getProtocolVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

}
