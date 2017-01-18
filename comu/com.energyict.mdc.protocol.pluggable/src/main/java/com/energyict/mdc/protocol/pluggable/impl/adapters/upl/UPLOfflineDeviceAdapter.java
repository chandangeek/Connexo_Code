package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.LoadProfileType;
import com.energyict.mdc.upl.meterdata.RegisterGroup;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineCalendar;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/01/2017 - 10:52
 */
public class UPLOfflineDeviceAdapter implements OfflineDevice {

    private final com.energyict.mdc.upl.offline.OfflineDevice uplOfflineDevice;

    public UPLOfflineDeviceAdapter(com.energyict.mdc.upl.offline.OfflineDevice uplOfflineDevice) {
        this.uplOfflineDevice = uplOfflineDevice;
    }

    @Override
    public List<OfflineDeviceMessage> getAllInvalidPendingDeviceMessages() {
        return uplOfflineDevice.getAllInvalidPendingDeviceMessages();
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        // Todo: let's wait and see, I think we can get away with this
        return null;
    }

    @Override
    public long getId() {
        return uplOfflineDevice.getId();
    }

    @Override
    public TimeZone getTimeZone() {
        return uplOfflineDevice.getTimeZone();
    }

    @Override
    public String getSerialNumber() {
        return uplOfflineDevice.getSerialNumber();
    }

    @Override
    public String getExternalName() {
        return uplOfflineDevice.getExternalName();
    }

    @Override
    public String getLocation() {
        return uplOfflineDevice.getLocation();
    }

    @Override
    public String getUsagePoint() {
        return uplOfflineDevice.getUsagePoint();
    }

    @Override
    public TypedProperties getAllProperties() {
        return uplOfflineDevice.getAllProperties();
    }

    @Override
    public List<? extends com.energyict.mdc.upl.offline.OfflineDevice> getAllSlaveDevices() {
        return uplOfflineDevice.getAllSlaveDevices();
    }

    @Override
    public List<OfflineLoadProfile> getMasterOfflineLoadProfiles() {
        return uplOfflineDevice.getMasterOfflineLoadProfiles();
    }

    @Override
    public List<OfflineLoadProfile> getAllOfflineLoadProfiles() {
        return uplOfflineDevice.getAllOfflineLoadProfiles();
    }

    @Override
    public List<OfflineLogBook> getAllOfflineLogBooks() {
        return uplOfflineDevice.getAllOfflineLogBooks();
    }

    @Override
    public List<OfflineRegister> getAllOfflineRegisters() {
        return uplOfflineDevice.getAllOfflineRegisters();
    }

    @Override
    public List<OfflineRegister> getRegistersForRegisterGroup(List<RegisterGroup> rtuRegisterGroups) {
        return uplOfflineDevice.getRegistersForRegisterGroup(rtuRegisterGroups);
    }

    @Override
    public List<OfflineDeviceMessage> getAllPendingDeviceMessages() {
        return uplOfflineDevice.getAllPendingDeviceMessages();
    }

    @Override
    public List<OfflineDeviceMessage> getAllSentDeviceMessages() {
        return uplOfflineDevice.getAllSentDeviceMessages();
    }

    @Override
    public List<OfflineLoadProfile> getLoadProfilesForLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        return uplOfflineDevice.getLoadProfilesForLoadProfileTypes(loadProfileTypes);
    }

    @Override
    public DeviceProtocolCache getDeviceProtocolCache() {
        return uplOfflineDevice.getDeviceProtocolCache();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return uplOfflineDevice.getDeviceIdentifier();
    }

    @Override
    public List<OfflineCalendar> getCalendars() {
        return uplOfflineDevice.getCalendars();
    }

    @Override
    public boolean touCalendarManagementAllowed() {
        return uplOfflineDevice.touCalendarManagementAllowed();
    }

    @Override
    public boolean firmwareVersionManagementAllowed() {
        return uplOfflineDevice.firmwareVersionManagementAllowed();
    }
}