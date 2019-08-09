package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;

import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2014 - 15:42
 */
public class DeviceInfo {

    private final OfflineDevice offlineDevice;
    private final Device device;

    public DeviceInfo(Device device, OfflineDevice offlineDevice) {
        this.device = device;
        this.offlineDevice = offlineDevice;
    }

    public String getName() {
        return device.getName();
    }

    public String getExternalName() {
        return offlineDevice.getExternalName();
    }

    public String getSerialNumber() {
        return offlineDevice.getSerialNumber();
    }

    public String getLocation() {
        return offlineDevice.getLocation();
    }

    public String getUsagePoint() {
        return offlineDevice.getUsagePoint();
    }

    public String getIntervalInSeconds() {
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        if (allOfflineLoadProfiles != null && allOfflineLoadProfiles.size() > 0) {
            return String.valueOf(allOfflineLoadProfiles.get(0).getInterval()) + " seconds";
        } else {
            return "N/A";
        }
    }

    public String getTimeZone() {
        TimeZone timeZone = offlineDevice.getTimeZone();
        if (timeZone == null) {
            return "N/A";
        } else {
            return timeZone.getDisplayName();
        }
    }

//    public String getDeviceTimeZone() {
//        return offlineDevice.getAllProperties().toStringProperties().getProperty(AdapterDeviceProtocolProperties.DEVICE_TIMEZONE_PROPERTY_NAME);
//    }

    public String getLastReading() {
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        if (allOfflineLoadProfiles != null && allOfflineLoadProfiles.size() > 0 && allOfflineLoadProfiles.get(0).getLastReading() != null) {
            return allOfflineLoadProfiles.get(0).getLastReading().toString();
        } else {
            return "N/A";
        }
    }

    public String getLastLogbook() {
        List<OfflineLogBook> allOfflineLogBooks = offlineDevice.getAllOfflineLogBooks();
        if (allOfflineLogBooks != null && allOfflineLogBooks.size() > 0 && allOfflineLogBooks.get(0).getLastReading() != null) {
            return allOfflineLogBooks.get(0).getLastReading().toString();
        } else {
            return "N/A";
        }
    }

//    public String getDeviceId() {
//        return offlineDevice.getAllProperties().toStringProperties().getProperty(MeterProtocol.ADDRESS);
//    }

    public String getDeviceType() {
        return device.getDeviceType().getName();
    }

    public String getDeviceConfig() {
        return device.getDeviceConfiguration().getName();
    }
}