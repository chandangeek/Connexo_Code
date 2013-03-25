package com.energyict.protocolimpl.coronis.waveflow.hydreka;

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
 * Copyrights EnergyICT
 * Date: 12/04/12
 * Time: 17:19
 */
public class Hydreka implements MeterProtocol {

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void connect() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disconnect() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getProtocolVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getProfileInterval() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date getTime() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTime() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initializeDevice() throws IOException, UnsupportedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void release() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCache(Object cacheObject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object fetchCache(int rtuId) throws SQLException, BusinessException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) throws SQLException, BusinessException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}