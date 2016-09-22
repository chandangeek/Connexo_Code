package com.energyict.protocolimpl.migration;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;
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

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        throw new UnsupportedOperationException();
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

    public String getRegister(String s) throws IOException, NoSuchRegisterException {
        throw new UnsupportedOperationException();
    }

    public void setRegister(String s, String s1) throws IOException, UnsupportedException {
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

    public Object getCache() {
        throw new UnsupportedOperationException();
    }

    public void setCache(Object o) {
        throw new UnsupportedOperationException();
    }

    public Object fetchCache(int i) throws SQLException, BusinessException {
        throw new UnsupportedOperationException();
    }

    public void updateCache(int i, Object o) throws SQLException, BusinessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        return getProtocolVersion();
    }

    @Override
    public void addProperties(TypedProperties properties) {
        throw new UnsupportedOperationException();
    }

    //No properties
    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    //No properties
    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }
}