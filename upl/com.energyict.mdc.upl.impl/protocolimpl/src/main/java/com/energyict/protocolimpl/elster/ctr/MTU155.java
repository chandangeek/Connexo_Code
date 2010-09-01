package com.energyict.protocolimpl.elster.ctr;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5-aug-2010
 * Time: 11:12:17
 */
public class MTU155 extends AbstractMTU155 {

    private CTRConnection ctrConnection;
    private final ProtocolProperties protocolProperties = new MTU155Properties();
    private Logger logger;
    private TimeZone timeZone;
    
    public void connect() throws IOException {

    }

    public void disconnect() throws IOException {
        getCtrConnection().closeSession();
    }

    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        getProtocolProperties().initProperties(properties);
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.logger = logger;
        this.timeZone = timeZone;
        this.ctrConnection = new CTRConnection(inputStream, outputStream, getProtocolProperties(), getLogger());
    }

    public List getRequiredKeys() {
        return getProtocolProperties().getRequiredKeys();
    }

    public List getOptionalKeys() {
        return getProtocolProperties().getOptionalKeys();
    }

    public String getProtocolVersion() {
        return "$Revision$";
    }

    public Date getTime() throws IOException {
        return new Date();
    }

    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    public ProfileData getProfileData(Date fromDate, Date toDate, boolean includeEvents) throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("Unsupported");
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        throw new NoSuchRegisterException();
    }

    public CTRConnection getCtrConnection() {
        return ctrConnection;
    }

    public ProtocolProperties getProtocolProperties() {
        return protocolProperties;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Logger getLogger() {
        if (logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

}
