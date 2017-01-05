package com.energyict.protocolimpl.migration;

import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * https://jira.eict.vpdc/browse/EISERVERSG-4263
 * <p/>
 * A dummy protocol that does no actual communication, but serves as a placeholder to create the device type.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 30/03/2016 - 11:30
 */
public class DummyProtocol implements MeterProtocol {

    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void connect() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void disconnect() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getProtocolVersion() {
        return "$Date: 2016-03-30 11:49:58 +0200 (wo, 30 mrt 2016) $";
    }

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedOperationException();
    }

    public ProfileData getProfileData(boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    public ProfileData getProfileData(Date date, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    public ProfileData getProfileData(Date date, Date date1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Quantity getMeterReading(int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Quantity getMeterReading(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getNumberOfChannels() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getProfileInterval() throws IOException {
        throw new UnsupportedOperationException();
    }

    public Date getTime() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getRegister(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setRegister(String s, String s1) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setTime() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void initializeDevice() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void release() throws IOException {
        throw new UnsupportedOperationException();
    }

}