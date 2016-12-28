package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CachingProtocol;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;
import com.energyict.protocolimpl.dlms.common.ProfileCacheImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for A1800 protocol
 * <p/>
 * Created by heuckeg on 13.06.2014.
 */
@SuppressWarnings("unused")
public class A1800 extends AbstractDlmsSessionProtocol implements SerialNumberSupport, CachingProtocol {

    static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");

    private OutputStream outputStream;
    private A1800MeterInfo info;

    private A1800Profile loadProfile;
    private A1800EventLog eventLog;
    private ProfileCacheImpl cache = new ProfileCacheImpl();
    private RegisterReader registerReader = null;

    protected final A1800Properties properties;

    public A1800(PropertySpecService propertySpecService) {
        this.properties = new A1800Properties(propertySpecService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:39 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getSerialNumber() {
        try {
            return readSerialNumber();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, properties.getRetries() + 1);
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        super.init(inputStream, outputStream, timeZone, logger);
        this.outputStream = outputStream;
    }

    @Override
    protected void doInit() {
        this.info = new A1800MeterInfo(getSession());
        this.loadProfile = new A1800Profile(getSession(), getProperties().getLoadProfileObiscode(), cache);
        this.eventLog = new A1800EventLog(getSession(), A1800EventLog.EVENT_LOG);
    }

    @Override
    public void connect() throws IOException {
        if (getProperties().sendPrefix()) {
            outputStream.write(new byte[]{0x00, 0x00});
            try {
                Thread.sleep(3000);
            } catch (InterruptedException interrupt) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(interrupt);
            }
        }

        getSession().connect();
        if (getProperties().isReadSerialNumber()) {
            String eisSerial = getProperties().getSerialNumber().trim();
            String meterSerialNumber = readSerialNumber().trim();
            getLogger().info("Meter serial number [" + meterSerialNumber + "]");
            if (!eisSerial.isEmpty()) {
                if (!eisSerial.equalsIgnoreCase(meterSerialNumber)) {
                    String message = "Configured serial number [" + eisSerial + "] does not match with the meter serial number [" + meterSerialNumber + "]!";
                    getLogger().severe(message);
                    throw new IOException(message);
                }
            } else {
                getLogger().info("Skipping validation of meter serial number: No serial number found in EIServer.");
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        getSession().disconnect(false); // release of the application association is not supported, only disconnectMAC should be done
    }

    @Override
    protected A1800Properties getProperties() {
        return properties;
    }

    @Override
    protected String readSerialNumber() throws IOException {
        return info.getDeviceSerialNumber();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return info.getMeterFirmware();
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        final ProfileData profileData;

        profileData = loadProfile.getProfileData(from, to);
        if (includeEvents) {
            profileData.setMeterEvents(eventLog.getEvents(from, to));
        }

        profileData.sort();
        return profileData;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return loadProfile.getNumberOfChannels();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return loadProfile.getProfileInterval();
    }

    @Override
    public Date getTime() throws IOException {
        return getSession().getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).getDateTime();
    }

    @Override
    public void setTime() throws IOException {
        try {
            AXDRDateTime dateTime = new AXDRDateTime(getSession().getTimeZone());
            getSession().getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            getSession().getLogger().log(Level.FINEST, e.getMessage());
            throw new NestedIOException(e, "Could not write the clock!");
        }
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getRegisterReader().readRegister(obisCode);
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(getSession());
        }
        return registerReader;
    }

    @Override
    public Serializable getCache() {
        return this.cache;
    }

    @Override
    public void setCache(Serializable cache) {
        if ((cache != null) && (cache instanceof ProfileCacheImpl)) {
            this.cache = (ProfileCacheImpl) cache;
        }
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException, ProtocolCacheFetchException {
        return null;
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
    }

}