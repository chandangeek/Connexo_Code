package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.mdw.core.*;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.energyict.genericprotocolimpl.elster.ctr.validation.ValidationUtils.getMeteringWarehouse;

/**
 * Copyrights EnergyICT
 * Date: 15/03/11
 * Time: 9:54
 */
public class ChannelDataExportProperties extends AbstractProtocolProperties {

    private Logger logger;

    private static final String GROUP_EXT_NAME = "GroupExternalName";
    private Group group = null;
    private List<Device> rtus;

    public ChannelDataExportProperties(String propertiesFileName) {
        super(new Properties());
        initProperties(propertiesFileName);
    }

    private void initProperties(String propertiesFileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(propertiesFileName);
            getProtocolProperties().load(fileInputStream);
        } catch (IOException e) {
            getLogger().severe("Unable to load properties from [" + propertiesFileName + "]. " + e.getMessage());
        }
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("channelDataExport");
        }
        return logger;
    }

    public List<String> getOptionalKeys() {
        return new ArrayList<String>();
    }

    public List<String> getRequiredKeys() {
        return new ArrayList<String>();
    }

    @ProtocolProperty
    public String getGroupExternalName() {
        return getStringValue(GROUP_EXT_NAME, null);
    }

    @ProtocolProperty
    public Group getGroup() {
        if (group == null) {
            if (getGroupExternalName() != null) {
                group = ValidationUtils.getMeteringWarehouse().getGroupFactory().findByExternalName(getGroupExternalName());
                if (group == null) {
                    getLogger().severe("Group with external name [" + getGroupExternalName() + "] not found in EIServer. Using all devices as default.");
                }
            }
        }
        return group;
    }

    public List<Device> getRtus() {
        if (rtus == null) {
            rtus = new ArrayList<Device>();
            if (getGroup() != null) {
                List groupMembers = getGroup().getMembers();
                for (Object member : groupMembers) {
                    if (member instanceof Device) {
                        rtus.add((Device) member);
                    } else {
                        getLogger().warning("[" + member.getClass().getSimpleName() + "] is not an Device. Ignoring.");
                    }
                }
            } else {
                getMeteringWarehouse().getDeviceFactory().findAllInTree(getParentFolder());
            }
        }
        return rtus;
    }

    @ProtocolProperty
    private Folder getParentFolder() {
        Folder folder = getMeteringWarehouse().getFolderFactory().findByExternalName(getMeteringPointsFolderExtName());
        if (folder == null) {
            getLogger().severe("Unable to find folder with external name [" + getMeteringPointsFolderExtName() + "]. Using root folder as default.");
            return getMeteringWarehouse().getFolderFactory().findRoot();
        } else {
            return folder;
        }
    }

    @ProtocolProperty
    private String getMeteringPointsFolderExtName() {
        return "meteringPoints";
    }

    @ProtocolProperty
    public String getOutputFolder() {
        String folder = getStringValue("OutputFolder", "output\\");
        folder = folder.trim();
        if (!folder.endsWith("/") && !folder.endsWith("\\")) {
            folder += "\\";
        }
        return folder;
    }

    @ProtocolProperty
    public int[] getChannelsToExport() {
        String channelsToExport = getStringValue("ChannelsToExport", "1,2,3,4,5,6,7,8,9,10,11,12,13,14");
        String[] channelIds = channelsToExport.split(",");
        int size = 0;
        for (int i = 0; i < channelIds.length; i++) {
            try {
                Integer.valueOf(channelIds[i]);
                size++;
            } catch (NumberFormatException e) {
                // Absorb
            }
        }

        int[] ids = new int[size];
        size = 0;
        for (int i = 0; i < channelIds.length; i++) {
            try {
                int value = Integer.valueOf(channelIds[i]);
                ids[size] = value;
                size++;
            } catch (NumberFormatException e) {
                // Absorb
            }
        }
        return ids;
    }

    public boolean needToExportChannel(Channel channel) {
        for (int channelId : getChannelsToExport()) {
            if (channel.getLoadProfileIndex() == channelId) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }
}
