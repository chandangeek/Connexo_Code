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
/*
        byte[] packet = new byte[] {
                (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xD1, (byte) 0x40, (byte) 0xC0,
                (byte) 0x26, (byte) 0x9F, (byte) 0x69, (byte) 0x58, (byte) 0xEC, (byte) 0x35, (byte) 0x02, (byte) 0x86,
                (byte) 0x1D, (byte) 0x91, (byte) 0x44, (byte) 0xB9, (byte) 0x67, (byte) 0xC6, (byte) 0xEA, (byte) 0x07,
                (byte) 0x40, (byte) 0x5B, (byte) 0x9E, (byte) 0xC8, (byte) 0x59, (byte) 0x54, (byte) 0xAB, (byte) 0x13,
                (byte) 0x10, (byte) 0x9E, (byte) 0x13, (byte) 0xEF, (byte) 0x0D, (byte) 0xBB, (byte) 0x7D, (byte) 0xAE,
                (byte) 0x64, (byte) 0x6D, (byte) 0x63, (byte) 0x7B, (byte) 0x16, (byte) 0xF9, (byte) 0xFE, (byte) 0x77,
                (byte) 0xA7, (byte) 0x5C, (byte) 0x34, (byte) 0xFA, (byte) 0x21, (byte) 0x03, (byte) 0xD4, (byte) 0xDC,
                (byte) 0x1D, (byte) 0xDF, (byte) 0x76, (byte) 0xF7, (byte) 0x42, (byte) 0xCB, (byte) 0x48, (byte) 0xAA,
                (byte) 0x92, (byte) 0x4E, (byte) 0xC7, (byte) 0x35, (byte) 0x17, (byte) 0xD5, (byte) 0x97, (byte) 0x6E,
                (byte) 0x2E, (byte) 0x82, (byte) 0xEE, (byte) 0x9B, (byte) 0xCE, (byte) 0x4B, (byte) 0x1A, (byte) 0x6B,
                (byte) 0x3A, (byte) 0x2A, (byte) 0x2C, (byte) 0x5B, (byte) 0x78, (byte) 0xF5, (byte) 0x02, (byte) 0xB9,
                (byte) 0x9E, (byte) 0xE4, (byte) 0xB7, (byte) 0x2E, (byte) 0xD9, (byte) 0x4F, (byte) 0x36, (byte) 0x24,
                (byte) 0x01, (byte) 0x12, (byte) 0x97, (byte) 0x3D, (byte) 0x1E, (byte) 0x12, (byte) 0x97, (byte) 0xC8,
                (byte) 0xCE, (byte) 0xFE, (byte) 0x79, (byte) 0x12, (byte) 0x67, (byte) 0xBE, (byte) 0x2C, (byte) 0x15,
                (byte) 0x0C, (byte) 0xF2, (byte) 0x66, (byte) 0x54, (byte) 0xF0, (byte) 0x23, (byte) 0xE5, (byte) 0xCD,
                (byte) 0x48, (byte) 0x77, (byte) 0x95, (byte) 0x4F, (byte) 0x43, (byte) 0x0F, (byte) 0x02, (byte) 0xA6,
                (byte) 0x95, (byte) 0xDA, (byte) 0x57, (byte) 0x13, (byte) 0x37, (byte) 0xD1, (byte) 0x7E, (byte) 0x7F,
                (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x97, (byte) 0x82, (byte) 0x0D
        };
*/

        getCtrConnection().sendRequestGetResonse(null);
    }

    public void disconnect() throws IOException {

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
