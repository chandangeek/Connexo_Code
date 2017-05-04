/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;

import com.energyict.protocolimpl.base.ProtocolProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public abstract class AbstractSmartMeterProtocol implements SmartMeterProtocol {

    private final PropertySpecService propertySpecService;
    private InputStream inputStream;
    private OutputStream outputStream;
    private TimeZone timeZone;
    private Logger logger;

    protected AbstractSmartMeterProtocol(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

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

    public void initializeDevice() throws IOException {
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

    public Object fetchCache(int rtuid) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getProtocolProperties().getRequiredKeys(), this.propertySpecService);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getProtocolProperties().getOptionalKeys(), this.propertySpecService);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

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
