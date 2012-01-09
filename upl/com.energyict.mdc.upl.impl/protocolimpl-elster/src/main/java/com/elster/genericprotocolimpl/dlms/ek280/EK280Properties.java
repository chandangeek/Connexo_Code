package com.elster.genericprotocolimpl.dlms.ek280;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 13/12/11
 * Time: 11:20
 */
public class EK280Properties extends AbstractProtocolProperties {

    public static final String RTU_TYPE = "RtuType";
    public static final String FOLDER_EXT_NAME = "FolderExtName";
    public static final String CHANNEL_BACKLOG = "ChannelBacklog";
    public static final String FAST_DEPLOYMENT = "FastDeployment";
    public static final String EXTRACT_INSTALLATION_DATE = "ExtractInstallationDate";

    public static final String DEFAULT_RTU_TYPE = null;
    public static final String DEFAULT_FOLDER_EXT_NAME = null;
    public static final String DEFAULT_CHANNEL_BACKLOG = "85";
    public static final String DEFAULT_FAST_DEPLOYMENT = "0";
    public static final String DEFAULT_EXTRACT_INSTALLATION_DATE = "1";

    public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add(RTU_TYPE);
        optionalKeys.add(FOLDER_EXT_NAME);
        optionalKeys.add(CHANNEL_BACKLOG);
        optionalKeys.add(FAST_DEPLOYMENT);
        optionalKeys.add(EXTRACT_INSTALLATION_DATE);
        return optionalKeys;
    }

    public List<String> getRequiredKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        // Add required keys here
        return optionalKeys;
    }

    @ProtocolProperty
    public String getRtuTypeName() {
        return getStringValue(RTU_TYPE, DEFAULT_RTU_TYPE);
    }

    @ProtocolProperty
    public String getFolderExternalName() {
        return getStringValue(FOLDER_EXT_NAME, DEFAULT_FOLDER_EXT_NAME);
    }
    
    @ProtocolProperty
    public int getChannelBackLog() {
        return getIntProperty(CHANNEL_BACKLOG, DEFAULT_CHANNEL_BACKLOG);
    }

    @ProtocolProperty
    public Date getChannelBackLogDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, (getChannelBackLog()) * (-1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @ProtocolProperty
    public boolean isFastDeployment() {
        return getBooleanProperty(FAST_DEPLOYMENT, DEFAULT_FAST_DEPLOYMENT);
    }

    @ProtocolProperty
    public boolean isExtractInstallationDate() {
        return getBooleanProperty(EXTRACT_INSTALLATION_DATE, DEFAULT_EXTRACT_INSTALLATION_DATE);
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        // nothing to do
    }

}
