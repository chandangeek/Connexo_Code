package com.energyict.smartmeterprotocolimpl.common;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.SmartMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.TimeZone;
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

    public void addProperties(TypedProperties properties) {
        getProtocolProperties().addProperties(properties.toStringProperties());
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

    public Object fetchCache(int deviceId) throws SQLException, BusinessException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getProtocolProperties().getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getProtocolProperties().getOptionalKeys());
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

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
