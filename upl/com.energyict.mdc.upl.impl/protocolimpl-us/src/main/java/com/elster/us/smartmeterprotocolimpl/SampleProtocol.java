package com.elster.us.smartmeterprotocolimpl;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 13/01/2016 - 9:16
 */
public class SampleProtocol implements SmartMeterProtocol {

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
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return null;
    }

    @Override
    public Serializable getCache() {
        return null;
    }

    @Override
    public void setCache(Serializable cacheObject) {

    }

    @Override
    public String getProtocolDescription() {
        return "Elster Sample (SMART)";
    }

    @Override
    public String getVersion() {
        return null;
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

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }
}