package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
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
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        // nothing to set
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
        return null;          // nothing to set
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return null;          // nothing to set
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return null;          // nothing to set
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;          // nothing to set
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 0;          // nothing to set
    }

    @Override
    public Date getTime() throws IOException {
        return null;          // nothing to set
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;          // nothing to set
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
    public String getProtocolDescription() {
        return "";
    }

    @Override
    public void setCache(Object cacheObject) {
        // nothing to set
    }

    @Override
    public Object getCache() {
        return null;          // nothing to set
    }

    @Override
    public Object fetchCache(int rtuId) throws SQLException, BusinessException {
        return null;          // nothing to set
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) throws SQLException, BusinessException {
        // nothing to set
    }

    @Override
    public String getVersion() {
        return null;          // nothing to set
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing to set
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return null;          // nothing to set
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return null;          // nothing to set
    }


    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        return TypedProperties.empty();
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
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
