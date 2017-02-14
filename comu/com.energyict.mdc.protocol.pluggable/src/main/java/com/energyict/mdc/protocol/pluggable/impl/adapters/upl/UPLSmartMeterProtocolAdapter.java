package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/02/2017 - 16:08
 */
public class UPLSmartMeterProtocolAdapter implements SmartMeterProtocol, UPLProtocolAdapter {

    //TODO implement/adapt all methods
    private final com.energyict.mdc.upl.SmartMeterProtocol uplSmartMeterProtocol;

    public UPLSmartMeterProtocolAdapter(com.energyict.mdc.upl.SmartMeterProtocol uplSmartMeterProtocol) {
        this.uplSmartMeterProtocol = uplSmartMeterProtocol;
    }

    @Override
    public Class getActualClass() {
        return uplSmartMeterProtocol.getClass();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>(uplSmartMeterProtocol.getUPLPropertySpecs()
                .stream()
                .filter(com.energyict.mdc.upl.properties.PropertySpec::isRequired)
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList()));
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>(uplSmartMeterProtocol.getUPLPropertySpecs()
                .stream()
                .filter((propertySpec) -> !propertySpec.isRequired())
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList()));
    }

    @Override
    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {

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
    public String getFirmwareVersion() throws IOException {
        return null;
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        return null;
    }

    @Override
    public Date getTime() throws IOException {
        return null;
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {

    }

    @Override
    public void initializeDevice() throws IOException {

    }

    @Override
    public void release() throws IOException {

    }

    @Override
    public RegisterInfo translateRegister(Register register) throws IOException {
        return null;
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return null;
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
    public String getProtocolDescription() {
        return null;
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return null;
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return null;
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void addProperties(TypedProperties properties) {

    }
}