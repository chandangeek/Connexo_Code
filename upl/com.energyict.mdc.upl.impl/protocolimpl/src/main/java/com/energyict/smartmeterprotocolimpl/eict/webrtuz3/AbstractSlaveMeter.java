package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 9/04/13
 * Time: 11:39
 */
public abstract class AbstractSlaveMeter implements SmartMeterProtocol {

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
}
