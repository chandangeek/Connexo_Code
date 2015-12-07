package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Dummy MeterProtocol for PluggableClassTestUsages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (10:59)
 */
public class MockMeterProtocol implements MeterProtocol, DeviceSecuritySupport, DeviceMessageSupport {

    private final PropertySpecService propertySpecService;

    public MockMeterProtocol(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {

    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public String getProtocolVersion() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "1.0.0-SNAPSHOT";
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

    }

    @Override
    public void setTime() throws IOException {

    }

    @Override
    public void initializeDevice() throws IOException {

    }

    @Override
    public void release() throws IOException {

    }

    @Override
    public void setCache(Object cacheObject) {

    }

    @Override
    public Object getCache() {
        return null;
    }

    @Override
    public Object fetchCache(int deviceId) throws SQLException {
        return null;
    }

    @Override
    public void updateCache(int deviceId, Object cacheObject) throws SQLException {
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void addProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> required = new ArrayList<>(1);
        required.add(PropertySpecFactory.stringPropertySpec("RequiredProperty", this.propertySpecService));
        return required;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<>(1);
        optional.add(PropertySpecFactory.stringPropertySpec("OptionalProperty", this.propertySpecService));
        return optional;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(com.elster.jupiter.properties.PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

}