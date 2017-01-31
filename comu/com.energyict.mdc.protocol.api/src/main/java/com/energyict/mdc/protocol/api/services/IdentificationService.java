/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

/**
 * Provides functionality to create finders for MDC objects
 */
public interface IdentificationService {

    DeviceIdentifier createDeviceIdentifierByDatabaseId(long id);

    DeviceIdentifier createDeviceIdentifierByMRID(String mRID);

    /**
     * Creates a DeviceIdentifier based on the serialNumber of the device.
     * <b>Note:</b> Be careful when using this identifier. Not all devices <i>have</i> a unique serialnumber.
     * Some don't have a serialnumber at all.
     *
     * @param serialNumber the serialNumber of the device
     * @return the Identifier based on the given serialnumber
     */
    DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber);

    DeviceIdentifier createDeviceIdentifierByCallHomeId(String callHomeId);

    DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(BaseDevice device);

    DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue);

    DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue);

    LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id, ObisCode obisCode);

    LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(BaseLoadProfile loadProfile, ObisCode obisCode);

    LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier);

    LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier, ObisCode obisCode);

    LogBookIdentifier createLogbookIdentifierByDatabaseId(long id, ObisCode logbookObisCode);

    LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier);

    LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(BaseLogBook logBook);

    MessageIdentifier createMessageIdentifierByDatabaseId(long id);

    MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage);

    MessageIdentifier createMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts);

}
