package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdw.cpo.PropertySpecFactory;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
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
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Dummy MeterProtocol for PluggableClassTestUsages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (10:59)
 */
public class MockMeterProtocol implements MeterProtocol, DeviceSecuritySupport, DeviceMessageSupport {

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {

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
    public String getRegister(String name) throws IOException, NoSuchRegisterException {
        return null;
    }

    @Override
    public void setRegister(String name, String value) throws IOException, UnsupportedException {

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
    public String getProtocolDescription() {
        return "";
    }

    @Override
    public void setCache(Object cacheObject) {

    }

    @Override
    public Object getCache() {
        return null;
    }

    @Override
    public Object fetchCache(int deviceId) throws SQLException, BusinessException {
        return null;
    }

    @Override
    public void updateCache(int deviceId, Object cacheObject) throws SQLException, BusinessException {

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
        required.add(PropertySpecFactory.stringPropertySpec("RequiredProperty"));
        return required;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<>(1);
        optional.add(PropertySpecFactory.stringPropertySpec("OptionalProperty"));
        return optional;
    }

    @Override
    public List<com.elster.jupiter.properties.PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return "MockRelationType";
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
    public com.elster.jupiter.properties.PropertySpec getSecurityPropertySpec(String name) {
        return null;
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
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