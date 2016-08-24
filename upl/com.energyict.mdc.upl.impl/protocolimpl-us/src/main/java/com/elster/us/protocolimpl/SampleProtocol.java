package com.elster.us.protocolimpl;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
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
 * @since 12/01/2016 - 17:23
 */
public class SampleProtocol implements MeterProtocol {

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {

    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

    }

    public void connect() throws IOException {

    }

    public void disconnect() throws IOException {

    }

    public String getProtocolVersion() {
        return null;
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return null;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return null;
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        return null;
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return null;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return 0;
    }

    public Date getTime() throws IOException {
        return null;
    }

    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return null;
    }

    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {

    }

    public void setTime() throws IOException {

    }

    public void initializeDevice() throws IOException, UnsupportedException {

    }

    public void release() throws IOException {

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

    public List<String> getRequiredKeys() {
        return null;
    }

    public List<String> getOptionalKeys() {
        return null;
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
        return null;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return null;
    }
}
