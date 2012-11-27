package com.elster.genericprotocolimpl.dlms.ek280.deployment;

import com.elster.genericprotocolimpl.dlms.ek280.EK280;
import com.elster.genericprotocolimpl.dlms.ek280.EK280Properties;
import com.elster.genericprotocolimpl.dlms.ek280.discovery.DeviceDiscoverInfo;
import com.energyict.cbo.BusinessException;
import com.energyict.cpo.ShadowList;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.DeviceShadow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 8/06/11
 * Time: 8:51
 */
public class DeviceDeployment {

    private Logger logger;
    private EK280Properties properties;
    private boolean deployed = false;
    private boolean invalidData = false;

    public DeviceDeployment(EK280 ek280) {
        this.logger = ek280.getLogger();
        this.properties = ek280.getProperties();
    }

    /**
     * Create a new device in EIServer, according to the info
     * we just received from the device during the discovery phase.
     *
     * @param info - data of request string from device
     * @return reference to rtu object calling
     * @throws java.io.IOException - in case of errors
     */
    public Device deployDevice(DeviceDiscoverInfo info) throws IOException {
        String callHomeId = info.getCallHomeId();
        if (callHomeId != null) {
            List<Device> rtus = MeteringWarehouse.getCurrent().getRtuFactory().findByDialHomeId(callHomeId);
            if (rtus.isEmpty()) {
                if (properties.isFastDeployment()) {
                    getLogger().warning("Device with call home id [" + callHomeId + "] not found! Starting deployment ...");
                    Device newRtu = createRtuAndAddFields(info);
                    setDeployed(newRtu != null);
                    return newRtu;
                } else {
                    String msg = "Unknown device [" + callHomeId + "]. Fast deployment disabled.";
                    getLogger().severe(msg);
                    throw new IOException(msg);
                }
            } else if (rtus.size() > 1) {
                throw new IOException("Duplicate call home id's: [" + callHomeId + "]");
            } else {
                return rtus.get(0);
            }
        } else {
            throw new IOException("Could not complete deployment: Incomplete discovery information, callHomeId was 'null'.");
        }
    }

    /**
     * Create a new rtu
     *
     * @param info - data of request string from device
     * @return reference to newly created rtu object for calling device
     * @throws java.io.IOException - in case of errors
     */
    private Device createRtuAndAddFields(DeviceDiscoverInfo info) throws IOException {
        setInvalidData(true);
        if (info.getRtuType() == null) {
            throw new IOException("DeviceType was 'null'. Unable to do discovery and create device without (valid) device type.");
        }

        getLogger().warning("Creating new device of type [" + info.getRtuType() + "] ...");

        try {
            DeviceShadow shadow = info.getRtuType().newRtuShadow();
            shadow.setName(createDeviceName(info)); // EK280 serial
            shadow.setExternalName(createExternalName(info)); // "rtu/" + EK280serial
            shadow.setDeviceId(createDeviceId(info)); // EK280 serial number
            shadow.setDialHomeId(createDialHomeId(info)); // EK280 serial number

            // The following values will be updated after we establish an association to the device
            shadow.setNodeAddress(""); // Keep it empty for now. Should contain PDR number.
            shadow.setNodeAddress(""); // Keep it empty for now. Should contain PDR number.
            shadow.setPhoneNumber(""); // Keep it empty for now. Should contain device phone number.

            // For now, enter (the calculated last reading. This will be updated after we sign on and fetch the installation date
            Date channelBackLogDate = getProperties().getChannelBackLogDate();
            shadow.setLastReading(channelBackLogDate);
            shadow.setLastLogbook(channelBackLogDate);
            ShadowList<ChannelShadow> channelShadows = shadow.getChannelShadows();
            for (ChannelShadow channelShadow : channelShadows) {
                channelShadow.setLastReading(channelBackLogDate);
            }

            if (info.getFolder() != null) {
                shadow.setFolderId(info.getFolder().getId());
            }
            if (info.getRtuType().getPrototypeRtu() == null) {
                shadow.setIntervalInSeconds(3600);
                if (info.getFolder() == null) {
                    throw new IOException("DeviceType [" + info.getRtuType().getName() + "] has no prototype AND no folder. Unable to create device.");
                }
            }
            Device rtu = MeteringWarehouse.getCurrent().getRtuFactory().create(shadow);
            setInvalidData(false);
            return rtu;
        } catch (SQLException e) {
            throw new IOException("Unable to create rtu: " + e.getMessage());
        } catch (BusinessException e) {
            throw new IOException("Unable to create rtu" + e.getMessage());
        }
    }

    private String createDialHomeId(DeviceDiscoverInfo info) {
        return info.getCallHomeId();
    }

    private String createDeviceId(DeviceDiscoverInfo info) {
        return info.getSerialNumber();
    }

    private String createDeviceName(DeviceDiscoverInfo info) {
        return info.getSerialNumber();
    }

    private String createExternalName(DeviceDiscoverInfo info) {
        return "rtu/" + info.getSerialNumber();
    }


    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public boolean isInvalidData() {
        return invalidData;
    }

    public void setInvalidData(boolean invalidData) {
        this.invalidData = invalidData;
    }

    public EK280Properties getProperties() {
        return properties;
    }


}
