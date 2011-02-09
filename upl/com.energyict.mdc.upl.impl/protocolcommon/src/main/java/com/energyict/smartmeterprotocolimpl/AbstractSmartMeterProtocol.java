package com.energyict.smartmeterprotocolimpl;

import com.energyict.cbo.BusinessException;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ProtocolProperties;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 10:15:35
 */
public abstract class AbstractSmartMeterProtocol implements SmartMeterProtocol {

    private InputStream inputStream;
    private OutputStream outputStream;
    private TimeZone timeZone;
    private Logger logger;

    protected abstract ProtocolProperties getProtocolProperties();

    public void addProperties(Properties properties) {
        getProtocolProperties().addProperties(properties);
    }

    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {
        getProtocolProperties().validateProperties();
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timeZone = timeZone;
        this.logger = logger;
    }

    public void connect() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disconnect() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException("getFirmwareVersion not supported.");
    }

    public void initializeDevice() throws IOException, UnsupportedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void release() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCache(Object cacheObject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getRequiredKeys() {
        return getProtocolProperties().getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return getProtocolProperties().getOptionalKeys();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Get the protocol logger, or create a temporary one if not initialized yet.
     *
     * @return
     */
    public Logger getLogger() {
        if (logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
