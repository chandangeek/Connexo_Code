package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.cbo.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/02/2017 - 16:08
 */
public class UPLMeterProtocolAdapter implements MeterProtocol, UPLProtocolAdapter {

    //TODO implement/adapt all methods
    private final com.energyict.mdc.upl.MeterProtocol uplMeterProtocol;

    public UPLMeterProtocolAdapter(com.energyict.mdc.upl.MeterProtocol uplMeterProtocol) {
        this.uplMeterProtocol = uplMeterProtocol;
    }

    @Override
    public Class getActualClass() {
        return uplMeterProtocol.getClass();
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
    public Object getCache() {
        return null;
    }

    @Override
    public void setCache(Object cacheObject) {

    }

    @Override
    public Object fetchCache(int rtuId) throws SQLException {
        return null;
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) throws SQLException {

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
        return new ArrayList<>(uplMeterProtocol.getUPLPropertySpecs()
                .stream()
                .filter(com.energyict.mdc.upl.properties.PropertySpec::isRequired)
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList()));
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>(uplMeterProtocol.getUPLPropertySpecs()
                .stream()
                .filter((propertySpec) -> !propertySpec.isRequired())
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList()));
    }

    @Override
    public String getProtocolDescription() {
        return null;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(uplMeterProtocol.getUPLPropertySpecs());
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }
}