/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.identifiers.DeviceIdentifierByConnectionTypeAndProperty;
import com.energyict.mdc.identifiers.DeviceIdentifierByDeviceName;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierByMRID;
import com.energyict.mdc.identifiers.DeviceIdentifierByPropertyValue;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.*;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.identifiers.LoadProfileIdentifierFirstOnDevice;
import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.identifiers.LogBookIdentifierById;
import com.energyict.mdc.identifiers.LogBookIdentifierForAlreadyKnowLogBook;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.meterdata.LogBook;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;

/**
 * Straightforward implementation of the {@link IdentificationService} interface.
 */
@Component(name = "com.energyict.mdc.device.data.impl.IdentificationServiceImpl", service = IdentificationService.class, immediate = true)
public class IdentificationServiceImpl implements IdentificationService {

    // For OSGi purposes only
    public IdentificationServiceImpl() {
        super();
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByDatabaseId(long id) {
        return new DeviceIdentifierById(id);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByMRID(String mRID) {
        return new DeviceIdentifierByMRID(mRID);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByDeviceName(String deviceName){
        return new DeviceIdentifierByDeviceName(deviceName);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber) {
        return new DeviceIdentifierBySerialNumber(serialNumber);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByCallHomeId(String callHomeId) {
        return new DeviceIdentifierByPropertyValue(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeId);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(long deviceId, String deviceMrId) {
        return new DeviceIdentifierForAlreadyKnownDevice(deviceId, deviceMrId);
    }

    @Override
    public RegisterIdentifier createRegisterIdentifierByAlreadyKnownRegister(Register register) {
        return new RegisterDataIdentifierByObisCodeAndDevice(register);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue) {
        return new DeviceIdentifierByPropertyValue(propertyName, propertyValue);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue) {
        return new DeviceIdentifierByConnectionTypeAndProperty(connectionTypeClass, propertyName, propertyValue);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id, ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
        return new LoadProfileIdentifierById(id, obisCode, deviceIdentifier);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(LoadProfile loadProfile, ObisCode obisCode) {
        return new LoadProfileIdentifierByObisCodeAndDevice(loadProfile, obisCode);  //Downcast to the Connexo LoadProfile
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        return new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier, ObisCode obisCode) {
        return new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, obisCode);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierByDatabaseId(long id, ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
        return new LogBookIdentifierById(id, obisCode, deviceIdentifier);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier) {
        return new LogBookIdentifierByDeviceAndObisCode(deviceIdentifier, logbookObisCode);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(LogBook logBook, DeviceIdentifier deviceIdentifier) {
        return new LogBookIdentifierForAlreadyKnowLogBook((com.energyict.mdc.common.device.data.LogBook) logBook, deviceIdentifier);     //Downcast to the Connexo LogBook
    }

    @Override
    public MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage) {
        return new DeviceMessageIdentifierById(deviceMessage);
    }

}