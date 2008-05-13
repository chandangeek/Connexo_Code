/**
 * @version 1.1
 * @author gna
 * <P>
 * <B>Description :</B><BR>
 * Class that extends the implemented Siemens ZMD DLMS protocol.
 * ZMD meters that don't support loadprofile readings must use this class instead of his superclass.
 * From EIServer 8.0 the methods getProfileInterval and getNumberOfChannels will not always be read anymore.
 * <BR>
 */
package com.energyict.protocolimpl.dlms;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author gna
 *
 */
public class DLMSZMD_EXT extends DLMSZMD{
	
	private int profileInterval;
	
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
    
}
