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

    DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber);

    DeviceIdentifier createDeviceIdentifierByCallHomeId(String serialNumber);

    DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(BaseDevice device);

    DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue);

    DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue);


    LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id);

    LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(BaseLoadProfile loadProfile);

    LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier);

    LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier);


    LogBookIdentifier createLogbookIdentifierByDatabaseId(long id);

    LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier);

    LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(BaseLogBook logBook);


    MessageIdentifier createMessageIdentifierByDatabaseId(long id);

    MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage);

    MessageIdentifier createMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts);

}
