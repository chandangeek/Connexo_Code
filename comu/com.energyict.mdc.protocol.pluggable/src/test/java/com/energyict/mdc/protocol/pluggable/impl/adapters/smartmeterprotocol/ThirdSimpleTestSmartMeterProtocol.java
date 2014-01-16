package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * A third simple test protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 13:10
 */
public class ThirdSimpleTestSmartMeterProtocol implements SmartMeterProtocol {

    public ThirdSimpleTestSmartMeterProtocol() {
        super();
    }

    @Override
    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {
        // nothing to do
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        // nothing to do
    }

    @Override
    public void connect() throws IOException {
        // nothing to do
    }

    @Override
    public void disconnect() throws IOException {
        // nothing to do
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return null;  // nothing to do
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        return null;  // nothing to do
    }

    @Override
    public Date getTime() throws IOException {
        return null;  // nothing to do
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        // nothing to do
    }

    @Override
    public void initializeDevice() throws IOException {
        // nothing to do
    }

    @Override
    public void release() throws IOException {
        // nothing to do
    }

    @Override
    public RegisterInfo translateRegister(Register register) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public void setCache(Object cacheObject) {
        // nothing to do
    }

    @Override
    public Object getCache() {
        return null;  // nothing to do
    }

    @Override
    public Object fetchCache(int rtuId) throws SQLException, BusinessException {
        return null;  // nothing to do
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) throws SQLException, BusinessException {
        // nothing to do
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return null;  // nothing to do
    }

    @Override
    public String getVersion() {
        return null;  // nothing to do
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing to do
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return null;  // nothing to do
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return null;  // nothing to do
    }


    @Override
    public String getProtocolDescription() {
        return "";
    }
}
