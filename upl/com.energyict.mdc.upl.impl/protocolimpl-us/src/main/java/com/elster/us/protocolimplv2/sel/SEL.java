package com.elster.us.protocolimplv2.sel;

import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.elster.us.protocolimplv2.sel.frame.ResponseFrame;
import com.elster.us.protocolimplv2.sel.frame.data.DeviceIDReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.SingleReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.TimeReadResponseData;
import com.elster.us.protocolimplv2.sel.profiles.LoadProfileBuilder;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_DATE;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_ID;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_TIME;

public class SEL implements DeviceProtocol {

    private SELConnection connection;
    private final PropertySpecService propertySpecService;
    private final CollectedDataFactory collectedDataFactory;
    private final NlsService nlsService;

    private SELProperties properties;
    private OfflineDevice offlineDevice;
    Logger logger = Logger.getLogger(this.getClass().getName());
    private LoadProfileBuilder loadProfileBuilder;

    public SEL(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.collectedDataFactory = collectedDataFactory;
        this.nlsService = nlsService;
    }

    public SELConnection getConnection() {
        return connection;
    }

    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    public SELProperties getProperties() {
        if (properties == null) {
            properties = new SELProperties(propertySpecService);
        }
        return properties;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    private TimeZone getTimeZone() {
        if (getProperties().getTimezone() != null) {
            return TimeZone.getTimeZone(getProperties().getTimezone());
        } else {
            return null;
        }
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        connection = new SELConnection((SerialPortComChannel) comChannel, getProperties(), logger, collectedDataFactory);
    }

    @Override
    public void terminate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.getProperties().setAllProperties(properties);
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }

    @Override
    public void daisyChainedLogOff() {
        // Not Implemented
    }

    @Override
    public void daisyChainedLogOn() {
        // Not Implemented

    }

    @Override
    public void logOff() {
        getConnection().doDisconnect();
    }

    @Override
    public void logOn() {
        connection.doConnect();
    }

    @Override
    public void setTime(Date date) {
        boolean success = getConnection().setTime(date);
        try {
            if (!success) {
                throw new IOException("Failed to set the device time");
            }
        } catch (IOException ioe) {
            getLogger().warning("Failed to set device time for SEL");
        }

    }

    @Override
    public Date getTime() {
        ResponseFrame dateResponse = getConnection().readSingleRegisterValue(COMMAND_DATE);
        SingleReadResponseData data = (SingleReadResponseData) dateResponse.getData();
        String dateStr = data.getValue().trim();

        ResponseFrame timeResponse = getConnection().readSingleRegisterValue(COMMAND_TIME);
        TimeReadResponseData timData = (TimeReadResponseData) timeResponse.getData();
        String timeStr = timData.getValue().trim();

        Date d = null;
        try {
            d = new SimpleDateFormat("MM/dd/yyHH:mm:ss").parse(dateStr + timeStr);
        } catch (ParseException e1) {
            getLogger().warning("Failed to parse the date from the device: " + dateStr);
        }

        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmmss");
        String str = format.format(d);

        format.setTimeZone(TimeZone.getTimeZone(getProperties().getDeviceTimezone()));

        Date d1 = null;
        try {
            d1 = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(getProperties().getDeviceTimezone()));
        cal.setTime(d1);
        TimeZone tz = getTimeZone(); //Get the timezone that we are running in
        if (tz != null) {
            cal.setTimeZone(tz);
        } else {
            cal.setTimeZone(TimeZone.getDefault());
        }

        return cal.getTime();
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getProperties().setAllProperties(dialectProperties);

    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.<DeviceProtocolDialect>singletonList(new NoParamsDeviceProtocolDialect(nlsService));
    }

    @Override
    public String getSerialNumber() {
        ResponseFrame responseFrame = connection.readSingleRegisterValue(COMMAND_ID);
        return ((DeviceIDReadResponseData) responseFrame.getData()).getValue();
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfiles);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        //return connection.readLoadProfileData(loadProfiles);
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return connection.readEvents(logBooks);
        //return Collections.emptyList();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return connection.readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String format(OfflineDevice arg0, OfflineDeviceMessage arg1, PropertySpec arg2, Object arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        // Not implemented
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache arg0) {
        // Not implemented

    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getProperties().setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());

    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return new NoOrPasswordSecuritySupport(propertySpecService).getClientSecurityPropertySpec();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return new NoOrPasswordSecuritySupport(propertySpecService).getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return new NoOrPasswordSecuritySupport(propertySpecService).getEncryptionAccessLevels();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return new NoOrPasswordSecuritySupport(propertySpecService).getSecurityProperties();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> retVal = new ArrayList<>();
        retVal.add(new SioAtModemConnectionType(propertySpecService));
        return retVal;
    }

    @Override
    public String getProtocolDescription() {
        return "SEL 734/735";
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    public Logger getLogger() {
        return logger;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, offlineDevice);
        }
        return loadProfileBuilder;
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return collectedDataFactory.createCalendarCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        return collectedDataFactory.createCreditAmountCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return collectedDataFactory.createFirmwareVersionsCollectedData(offlineDevice.getDeviceIdentifier());
    }
}