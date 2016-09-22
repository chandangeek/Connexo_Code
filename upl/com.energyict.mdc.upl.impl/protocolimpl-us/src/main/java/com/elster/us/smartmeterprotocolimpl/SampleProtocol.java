package com.elster.us.smartmeterprotocolimpl;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SmartMeterProtocol;
import com.energyict.protocol.UnsupportedException;

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
 * @author sva
 * @since 13/01/2016 - 9:16
 */
public class SampleProtocol implements SmartMeterProtocol {

    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {

    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

    }

    public void connect() throws IOException {

    }

    public void disconnect() throws IOException {

    }

    public String getFirmwareVersion() throws IOException {
        return null;
    }

    public String getMeterSerialNumber() throws IOException {
        return null;
    }

    public Date getTime() throws IOException {
        return null;
    }

    public void setTime(Date newMeterTime) throws IOException {

    }

    public void initializeDevice() throws IOException, UnsupportedException {

    }

    public void release() throws IOException {

    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return null;
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return null;
    }

    public void setCache(Object cacheObject) {

    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {

    }

    public String getVersion() {
        return null;
    }

    @Override
    public void addProperties(TypedProperties properties) {

    }

    public void addProperties(Properties properties) {

    }

    public List<String> getRequiredKeys() {
        return null;
    }

    public List<String> getOptionalKeys() {
        return null;
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return null;
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return null;
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return null;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return null;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return null;
    }
}