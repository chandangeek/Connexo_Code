package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.Quantity;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Adapts a {@link com.energyict.mdc.upl.MeterProtocol} to the {@link MeterProtocol} interface.
 *
 * @author khe
 * @since 10/02/2017 - 16:08
 */
public class UPLMeterProtocolAdapter implements MeterProtocol, UPLProtocolAdapter {

    private final com.energyict.mdc.upl.MeterProtocol actual;
    private final CachingProtocol cachingProtocol;

    public UPLMeterProtocolAdapter(com.energyict.mdc.upl.MeterProtocol actual) {
        this.actual = actual;
        if (actual instanceof com.energyict.mdc.upl.cache.CachingProtocol) {
            this.cachingProtocol = new CachingDelegator((com.energyict.mdc.upl.cache.CachingProtocol) actual);
        } else {
            this.cachingProtocol = new NoCacheSupport();
        }
    }


    CachingProtocol getCachingProtocol() {
        return cachingProtocol;
    }

    @Override
    public Class getActualClass() {
        return actual.getClass();
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
    public String getProtocolVersion() {
        return this.actual.getProtocolVersion();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return this.actual.getFirmwareVersion();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return this.actual.getProfileData(includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return this.actual.getProfileData(lastReading, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return this.actual.getProfileData(from, to, includeEvents);
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return this.actual.getMeterReading(channelId);
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return this.actual.getMeterReading(name);
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return this.actual.getNumberOfChannels();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return this.actual.getProfileInterval();
    }

    @Override
    public Date getTime() throws IOException {
        return this.actual.getTime();
    }

    @Override
    public String getRegister(String name) throws IOException {
        return this.actual.getRegister(name);
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        this.actual.setRegister(name, value);
    }

    @Override
    public void setTime() throws IOException {
        this.actual.setTime();
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
    public Object getCache() {
        return this.getCachingProtocol().getCache();
    }

    @Override
    public void setCache(Object cacheObject) {
        this.getCachingProtocol().setCache((Serializable) cacheObject);
    }

    @Override
    public String getVersion() {
        return this.actual.getProtocolVersion();
    }

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        this.addProperties(TypedPropertiesValueAdapter.adaptToUPLValues(properties));
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
        this.actual.setUPLProperties(adaptedProperties);
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.addProperties(TypedPropertiesValueAdapter.adaptToUPLValues(properties));
    }

    private void addProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        try {
            this.actual.setUPLProperties(properties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return this.actual.getUPLPropertySpecs()
                .stream()
                .filter(com.energyict.mdc.upl.properties.PropertySpec::isRequired)
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return this.actual.getUPLPropertySpecs()
                .stream()
                .filter((propertySpec) -> !propertySpec.isRequired())
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return this.actual.getUPLPropertySpecs();
    }

    @Override
    public String getProtocolDescription() {
        return this.actual.getProtocolDescription();
    }

    /**
     * Provides an implementation for the {@link CachingProtocol} interface
     * when the actual MeterProtocol implements the {@link com.energyict.mdc.upl.cache.CachingProtocol}
     * interface and will delegate the appropriate methods to the actual protocol.
     */
    private static class CachingDelegator implements CachingProtocol {
        private final com.energyict.mdc.upl.cache.CachingProtocol meterProtocol;

        private CachingDelegator(com.energyict.mdc.upl.cache.CachingProtocol meterProtocol) {
            this.meterProtocol = meterProtocol;
        }

        @Override
        public void setCache(Serializable cacheObject) {
            this.meterProtocol.setCache(cacheObject);
        }

        @Override
        public Serializable getCache() {
            return this.meterProtocol.getCache();
        }
    }

    /**
     * Provides an implementation fo the {@link CachingProtocol} interface
     * when the actual MeterProtocol that is being adapted does not
     * implement the {@link com.energyict.mdc.upl.cache.CachingProtocol} interface.
     */
    private static class NoCacheSupport implements CachingProtocol {
        @Override
        public void setCache(Serializable cacheObject) {
            // Ignore
        }

        @Override
        public Serializable getCache() {
            return null;
        }
    }

}