package com.elster.us.smartmeterprotocolimpl;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.BusinessException;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
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

    public Serializable getCache() {
        return null;
    }

    public Object fetchCache(int deviceId) throws SQLException, BusinessException {
        return null;
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {

    }

    public String getVersion() {
        return null;
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
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }
}