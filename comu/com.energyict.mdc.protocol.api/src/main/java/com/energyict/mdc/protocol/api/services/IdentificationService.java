/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.meterdata.LogBook;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import aQute.bnd.annotation.ProviderType;
import com.energyict.obis.ObisCode;

/**
 * Provides functionality to create finders for MDC objects.
 */
@ProviderType
public interface IdentificationService {

    DeviceIdentifier createDeviceIdentifierByDatabaseId(long id);

    DeviceIdentifier createDeviceIdentifierByMRID(String mRID);

    DeviceIdentifier createDeviceIdentifierByDeviceName(String deviceName);

    /**
     * Creates a DeviceIdentifier based on the serialNumber of the device.
     * <b>Note:</b> Be careful when using this identifier. Not all devices <i>have</i> a unique serialnumber.
     * Some don't even have a serialnumber at all.
     *
     * @param serialNumber the serialNumber of the device
     * @return the Identifier based on the given serialnumber
     */
    DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber);

    DeviceIdentifier createDeviceIdentifierByCallHomeId(String callHomeId);

    DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(Device device);

    RegisterIdentifier createRegisterIdentifierByAlreadyKnownRegister(Register register);

    DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue);

    DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue);

    LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id, ObisCode obisCode, DeviceIdentifier deviceIdentifier);

    LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(LoadProfile loadProfile, ObisCode obisCode);

    LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier);

    LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier, ObisCode obisCode);

    LogBookIdentifier createLogbookIdentifierByDatabaseId(long id, ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier);

    LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier);

    LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(LogBook logBook, DeviceIdentifier deviceIdentifier);

    MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage);

}