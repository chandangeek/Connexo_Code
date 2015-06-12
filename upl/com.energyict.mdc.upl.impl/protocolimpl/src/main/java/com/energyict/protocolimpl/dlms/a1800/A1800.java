package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;
import com.energyict.protocolimpl.dlms.common.ProfileCacheImpl;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class A1800 extends AbstractDlmsSessionProtocol {

    public static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");

    protected A1800Properties properties = new A1800Properties();
    //
    private OutputStream outputStream;

    private A1800MeterInfo info;
    private A1800Profile loadProfile;
    private A1800EventLog eventLog;
    private ProfileCacheImpl cache = new ProfileCacheImpl();
    private RegisterReader registerReader = null;

    /**
     * The protocol version
     */
    public String getProtocolVersion() {
        return "$Date: 2014-06-27 13:00:00$";
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        super.init(inputStream, outputStream, timeZone, logger);
        this.outputStream = outputStream;
    }

    protected void doInit() {
        this.info = new A1800MeterInfo(getSession());
        this.loadProfile = new A1800Profile(getSession(), getProperties().getLoadProfileObiscode(), cache);
        this.eventLog = new A1800EventLog(getSession(), A1800EventLog.EVENT_LOG);
    }

    public void connect() throws IOException {
        if (getProperties().sendPrefix()) {
            outputStream.write(new byte[]{0x00, 0x00});
            try {
                Thread.sleep(3000);
            } catch (InterruptedException interrupt) {
                Thread.currentThread().interrupt();
                throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(interrupt);
            }
        }

        getSession().connect();
        if (getProperties().isReadSerialNumber()) {
            String eisSerial = getProperties().getSerialNumber().trim();
            String meterSerialNumber = readSerialNumber().trim();
            getLogger().info("Meter serial number [" + meterSerialNumber + "]");
            if (eisSerial.length() != 0) {
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

    protected A1800Properties getProperties() {
        return properties;
    }

    protected String readSerialNumber() throws IOException {
        return info.getDeviceSerialNumber();
    }

    public String getFirmwareVersion() throws IOException {
        return info.getMeterFirmware();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        final ProfileData profileData;

        profileData = loadProfile.getProfileData(from, to);
        if (includeEvents) {
            profileData.setMeterEvents(eventLog.getEvents(from, to));
        }

        profileData.sort();
        return profileData;
    }

    public int getNumberOfChannels() throws IOException {
        return loadProfile.getNumberOfChannels();
    }

    public int getProfileInterval() throws IOException {
        return loadProfile.getProfileInterval();
    }

    public Date getTime() throws IOException {
        Date dateTime = getSession().getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).getDateTime();
        return dateTime;
    }

    public void setTime() throws IOException {
        try {
            AXDRDateTime dateTime = new AXDRDateTime(getSession().getTimeZone());
            getSession().getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            getSession().getLogger().log(Level.FINEST, e.getMessage());
            throw new NestedIOException(e, "Could not write the clock!");
        }
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getRegisterReader().readRegister(obisCode);
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(getSession());
        }
        return registerReader;
    }

    public Object getCache() {
        return this.cache;
    }

    public void setCache(Object cache) {
        if ((cache != null) && (cache instanceof ProfileCacheImpl)) {
            this.cache = (ProfileCacheImpl) cache;
        }
    }
}
