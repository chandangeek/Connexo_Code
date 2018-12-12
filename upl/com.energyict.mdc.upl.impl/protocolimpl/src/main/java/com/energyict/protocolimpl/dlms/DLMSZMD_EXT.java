package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * <B>Description :</B><BR>
 * Class that extends the implemented Siemens ZMD DLMS protocol.
 * ZMD meters that don't support loadprofile readings must use this class instead of his superclass.
 * From EIServer 8.0 the methods getProfileInterval and getNumberOfChannels will not always be read anymore.
 * <BR>
 *
 * @author gna
 * @version 1.1
 */
@Deprecated
public class DLMSZMD_EXT extends DLMSZMD {

    private int profileInterval;

    public DLMSZMD_EXT(PropertySpecService propertySpecService, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor, NlsService nlsService) {
        super(propertySpecService, messageFileFinder, deviceMessageFileExtractor, nlsService);
    }

    @Override
    public int getProfileInterval() {
        return profileInterval;
    }

    @Override
    public int getNumberOfChannels() {
        return -1;
    }

    @Override
    protected void doSetProperties(TypedProperties properties) throws PropertyValidationException {
        profileInterval = Integer.parseInt((String) properties.getProperty("ProfileInterval", "900"));
        super.doSetProperties(properties);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        if (profileInterval != super.getProfileInterval()) {
            throw new IOException("profile interval setting in eiserver configuration (" + profileInterval + "sec) is different then requested from the meter (" + super.getProfileInterval() + "sec)");
        }
        super.getNumberOfChannels();
        return super.getProfileData(lastReading, includeEvents);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

}