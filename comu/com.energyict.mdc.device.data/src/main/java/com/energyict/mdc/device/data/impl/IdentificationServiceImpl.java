package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByConnectionTypeAndProperty;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByMRID;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByPropertyValue;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDeviceByMrID;
import com.energyict.mdc.device.data.impl.identifiers.DeviceMessageIdentifierForAlreadyKnownMessage;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierFirstOnDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierForAlreadyKnownLoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierForAlreadyKnowLogBook;
import com.energyict.mdc.device.data.impl.identifiers.RegisterIdentifierByAlreadyKnownRegister;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
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
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Straightforward implementation of the {@link IdentificationService} interface.
 */
@Component(name = "com.energyict.mdc.device.data.impl.IdentificationServiceImpl", service = IdentificationService.class, immediate = true)
public class IdentificationServiceImpl implements IdentificationService {

    private volatile DeviceService deviceService;
    private volatile LogBookService logBookService;
    private volatile LoadProfileService loadProfileService;

    // For OSGi purposes only
    public IdentificationServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public IdentificationServiceImpl(DeviceService deviceService, LogBookService logBookService, LoadProfileService loadProfileService) {
        this();
        this.setDeviceService(deviceService);
        this.setLogBookService(logBookService);
        this.setLoadProfileService(loadProfileService);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByDatabaseId(long id) {
        return new DeviceIdentifierById(id, deviceService);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByMRID(String mRID) {
        return new DeviceIdentifierByMRID(mRID, deviceService);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierBySerialNumber(String serialNumber) {
        return new DeviceIdentifierBySerialNumber(serialNumber, deviceService);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByCallHomeId(String callHomeId) {
        return new DeviceIdentifierByPropertyValue(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), callHomeId, deviceService);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(com.energyict.mdc.upl.meterdata.Device device) {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(device);
    }

    @Override
    public RegisterIdentifier createRegisterIdentifierByAlreadyKnownRegister(Register register) {
        return new RegisterIdentifierByAlreadyKnownRegister(register);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByProperty(String propertyName, String propertyValue) {
        return new DeviceIdentifierByPropertyValue(propertyName, propertyValue, deviceService);
    }

    @Override
    public DeviceIdentifier createDeviceIdentifierByConnectionTaskProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue) {
        return new DeviceIdentifierByConnectionTypeAndProperty(connectionTypeClass, propertyName, propertyValue, deviceService);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id, ObisCode obisCode) {
        return new LoadProfileIdentifierById(id, obisCode);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(LoadProfile loadProfile, ObisCode obisCode) {
        return new LoadProfileIdentifierForAlreadyKnownLoadProfile((com.energyict.mdc.device.data.LoadProfile) loadProfile, obisCode);  //Downcast to the Connexo LoadProfile
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
    public LogBookIdentifier createLogbookIdentifierByDatabaseId(long id, ObisCode obisCode) {
        return new LogBookIdentifierById(id, obisCode);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier) {
        return new LogBookIdentifierByDeviceAndObisCode(deviceIdentifier, logbookObisCode);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(LogBook logBook) {
        return new LogBookIdentifierForAlreadyKnowLogBook((com.energyict.mdc.device.data.LogBook) logBook);     //Downcast to the Connexo LogBook
    }

    @Override
    public MessageIdentifier createMessageIdentifierByDatabaseId(long id) {
        //TODO
        return null;
    }

    @Override
    public MessageIdentifier createMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage) {
        return new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage);
    }

    @Override
    public MessageIdentifier createMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts) {
        //TODO
        return null;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setLoadProfileService(LoadProfileService loadProfileService) {
        this.loadProfileService = loadProfileService;
    }

}