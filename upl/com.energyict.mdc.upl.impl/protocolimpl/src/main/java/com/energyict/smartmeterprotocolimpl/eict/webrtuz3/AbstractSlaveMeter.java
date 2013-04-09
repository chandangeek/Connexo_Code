package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.cbo.BusinessException;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 11:39
 */
public abstract class AbstractSlaveMeter implements SmartMeterProtocol{

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
    public void initializeDevice() throws IOException, UnsupportedException {
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
        return Collections.emptyList();
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
        return Collections.emptyList();
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return Collections.emptyList();
    }
}
