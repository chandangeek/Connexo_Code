package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author khe
 * @since 10/02/2017 - 16:08
 */
public class UPLSmartMeterProtocolAdapter implements SmartMeterProtocol, UPLProtocolAdapter<com.energyict.mdc.upl.SmartMeterProtocol> {

    private final com.energyict.mdc.upl.SmartMeterProtocol actual;

    public UPLSmartMeterProtocolAdapter(com.energyict.mdc.upl.SmartMeterProtocol actual) {
        this.actual = actual;
    }

    @Override
    public Class getActualClass() {
        return actual.getClass();
    }

    @Override
    public com.energyict.mdc.upl.SmartMeterProtocol getActual() {
        return actual;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return this.actual.getUPLPropertySpecs()
                .stream()
                .filter(com.energyict.mdc.upl.properties.PropertySpec::isRequired)
                .map(UPLToConnexoPropertySpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return this.actual.getUPLPropertySpecs()
                .stream()
                .filter((propertySpec) -> !propertySpec.isRequired())
                .map(UPLToConnexoPropertySpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.actual.init(inputStream, outputStream, timeZone, logger);
    }

    @Override
    public void connect() throws IOException {
        this.actual.connect();
    }

    @Override
    public void disconnect() throws IOException {
        this.actual.disconnect();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return this.actual.getFirmwareVersion();
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        return this.actual.getMeterSerialNumber();
    }

    @Override
    public Date getTime() throws IOException {
        return this.actual.getTime();
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        this.actual.setTime(newMeterTime);
    }

    @Override
    public void initializeDevice() throws IOException {
        this.actual.initializeDevice();
    }

    @Override
    public void release() throws IOException {
        this.actual.release();
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return this.actual.readRegisters(registers);
    }

    @Override
    public Serializable getCache() {
        return this.actual.getCache();
    }

    @Override
    public void setCache(Serializable cacheObject) {
        this.actual.setCache(cacheObject);
    }

    @Override
    public String getProtocolDescription() {
        return this.actual.getProtocolDescription();
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return this.actual.getMeterEvents(lastLogbookDate);
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return this.actual.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return this.actual.getLoadProfileData(loadProfiles);
    }

    @Override
    public String getVersion() {
        return this.actual.getVersion();
    }

    @Override
    public void addProperties(TypedProperties properties) {
        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
        try {
            actual.setUPLProperties(adaptedProperties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return this.actual.getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.actual.setUPLProperties(properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLSmartMeterProtocolAdapter) {
            return actual.equals(((UPLSmartMeterProtocolAdapter) obj).actual);
        } else {
            return actual.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}