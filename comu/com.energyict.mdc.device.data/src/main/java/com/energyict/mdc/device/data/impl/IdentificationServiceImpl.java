package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByConnectionTypeAndProperty;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByMRID;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByPropertyValue;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.device.data.impl.identifiers.DeviceMessageIdentifierForAlreadyKnownMessage;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierFirstOnDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierForAlreadyKnownLoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierForAlreadyKnowLogBook;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Straightforward implementation of the IdentificationService
 */
@Component(name = "com.energyict.mdc.device.data.impl.IdentificationServiceImpl", service = IdentificationService.class, immediate = true)
public class IdentificationServiceImpl implements IdentificationService {

    private volatile DeviceService deviceService;
    private volatile LogBookService logBookService;
    private volatile LoadProfileService loadProfileService;

    private Injector injector;

    // For OSGi purposes only
    public IdentificationServiceImpl() {
    }

    @Inject
    public IdentificationServiceImpl(DeviceService deviceService) {
        this.deviceService = deviceService;
        activate();
    }

    @Activate
    public void activate() {
        Module module = this.getModule();
        this.injector = Guice.createInjector(module);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceService.class).toInstance(deviceService);
                bind(LogBookService.class).toInstance(logBookService);
                bind(LoadProfileService.class).toInstance(loadProfileService);
                bind(IdentificationService.class).toInstance(IdentificationServiceImpl.this);
            }
        };
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
    public DeviceIdentifier createDeviceIdentifierForAlreadyKnownDevice(BaseDevice device) {
        return new DeviceIdentifierForAlreadyKnownDevice((Device) device);
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
    public LoadProfileIdentifier createLoadProfileIdentifierByDatabaseId(long id) {
        return new LoadProfileIdentifierById(id, loadProfileService);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierForAlreadyKnownLoadProfile(BaseLoadProfile loadProfile) {
        return new LoadProfileIdentifierForAlreadyKnownLoadProfile((LoadProfile) loadProfile);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierByObisCodeAndDeviceIdentifier(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        return new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier);
    }

    @Override
    public LoadProfileIdentifier createLoadProfileIdentifierForFirstLoadProfileOnDevice(DeviceIdentifier deviceIdentifier) {
        return new LoadProfileIdentifierFirstOnDevice(deviceIdentifier);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierByDatabaseId(long id) {
        return new LogBookIdentifierById(id, logBookService);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierByObisCodeAndDeviceIdentifier(ObisCode logbookObisCode, DeviceIdentifier deviceIdentifier) {
        return new LogBookIdentifierByDeviceAndObisCode(deviceIdentifier, logbookObisCode);
    }

    @Override
    public LogBookIdentifier createLogbookIdentifierForAlreadyKnownLogbook(BaseLogBook logBook) {
        return new LogBookIdentifierForAlreadyKnowLogBook((LogBook) logBook);
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
