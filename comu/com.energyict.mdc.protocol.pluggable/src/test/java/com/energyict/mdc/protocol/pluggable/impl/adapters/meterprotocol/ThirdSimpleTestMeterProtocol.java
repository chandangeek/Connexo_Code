package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.cbo.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * A third SimpleTest protocol
 */
public class ThirdSimpleTestMeterProtocol implements MeterProtocol, LegacySecurityPropertyConverter {

    public ThirdSimpleTestMeterProtocol() {
        super();
    }

    @Override
    public String getProtocolDescription() {
        return this.getClass().getName();
    }

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        // nothing to set
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        // nothing to set
    }

    @Override
    public void connect() throws IOException {
        // nothing to set
    }

    @Override
    public void disconnect() throws IOException {
        // nothing to set
    }

    @Override
    public String getProtocolVersion() {
        return null;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 0;
    }

    @Override
    public Date getTime() throws IOException {
        return null;
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        // nothing to set
    }

    @Override
    public void setTime() throws IOException {
        // nothing to set
    }

    @Override
    public void initializeDevice() throws IOException {
        // nothing to set
    }

    @Override
    public void release() throws IOException {
        // nothing to set
    }

    @Override
    public Object getCache() {
        return null;
    }

    @Override
    public void setCache(Object cacheObject) {
        // nothing to set
    }

    @Override
    public Object fetchCache(int rtuId) throws SQLException {
        return null;
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) throws SQLException {
        // nothing to set
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing to set
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }


    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        return TypedProperties.empty();
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return 0;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return TypedProperties.empty();
            }
        };
    }
}
