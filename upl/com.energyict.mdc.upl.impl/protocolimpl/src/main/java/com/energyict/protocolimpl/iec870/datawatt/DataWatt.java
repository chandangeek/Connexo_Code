/*
 * DataWatt.java
 *
 * Created on 18 juni 2003, 13:56
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec870.IEC870Connection;
import com.energyict.protocolimpl.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.iec870.IEC870ProtocolLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author Koen
 * @beginchanges KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
public class DataWatt extends PluggableMeterProtocol implements IEC870ProtocolLink {

    public static final int MAX_COUNTER = 100000000; // = max counter + 1

    private static final int DEBUG = 0;

    // properties
    String strID;
    String strPassword;
    int iIEC870TimeoutProperty;
    int iProtocolRetriesProperty;
    int iRoundtripCorrection;
    int iProfileInterval;
    int iMeterType;
    ChannelMap channelMap = null;

    IEC870Connection iec870Connection = null;
    ApplicationFunction applicationFunction = null;

    TimeZone timeZone = null;
    Logger logger = null;
    DatawattRegistry datawattRegistry = null;
    DatawattProfile datawattProfile = null;

    /**
     * Creates a new instance of DataWatt
     */
    public DataWatt() {
    }

    public void connect() throws IOException {
        try {
            iec870Connection.connectLink();
            if (strID.compareTo(String.valueOf(iec870Connection.getRTUAddress())) != 0) {
                throw new IOException("DataWatt, connect, invalid meter address, config=" + strID + ", meter=" + String.valueOf(iec870Connection.getRTUAddress()));
            }
        } catch (IEC870ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            iec870Connection.disconnectLink();
        } catch (IEC870ConnectionException e) {
            logger.severe("DataWatt, disconnect() error, " + e.getMessage());
        }
    }

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException("DataWatt, getFirmwareVersion");
    }

    public Quantity getMeterReading(String name) throws IOException {
        return new Quantity(getDatawattRegistry().getRegister(Channel.parseChannel(name)), Unit.get(""));
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        if (channelId >= getChannelMap().getNrOfChannels()) {
            throw new IOException("DataWatt, getMeterReading, invalid channelId, " + channelId);
        }
        return new Quantity(getDatawattRegistry().getRegister(getChannelMap().getChannel(channelId)), Unit.get(""));
    }

    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfChannels();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.clear();
        return datawattProfile.getProfileData(fromCalendar, includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return datawattProfile.getProfileData(fromCalendar, includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }


    public int getProfileInterval() throws IOException {
        return iProfileInterval;
    }

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    public String getRegister(String name) throws IOException {
        return getDatawattRegistry().getRegister(Channel.parseChannel(name)).toString();
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "MeterType");
    }

    public List<String> getRequiredKeys() {
        return Collections.singletonList("ChannelMap");
    }

    public Date getTime() throws IOException {
        return getApplicationFunction().dsapGetClockASDU().getTime();
    }

    public DatawattRegistry getDatawattRegistry() {
        return datawattRegistry;
    }

    public DatawattProfile getDatawattProfile() {
        return datawattProfile;
    }

    public IEC870Connection getIEC870Connection() {
        return iec870Connection;
    }

    public ApplicationFunction getApplicationFunction() {
        return applicationFunction;
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public int getMeterType() {
        return iMeterType;
    }

    public Logger getLogger() {
        return logger;
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, java.util.logging.Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            iec870Connection = new IEC870Connection(inputStream, outputStream, iIEC870TimeoutProperty, iProtocolRetriesProperty, (long) 300, 0, getTimeZone());
            applicationFunction = new ApplicationFunction(timeZone, iec870Connection, logger);
            datawattRegistry = new DatawattRegistry(this);
            datawattProfile = new DatawattProfile(this);
        } catch (IEC870ConnectionException e) {
            logger.severe("DataWatt: init(...), " + e.getMessage());
        }
    }

    public void initializeDevice() throws IOException {
        throw new UnsupportedException("DataWatt, initializeDevice");
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }

    /**
     * <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     *
     * @param properties <br>
     * @throws InvalidPropertyException <br>
     */
    private void validateProperties(Properties properties) throws InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            iIEC870TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "25000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "3").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", ""));
            iMeterType = Integer.parseInt(properties.getProperty("MeterType", "0").trim());

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DataWatt, validateProperties, NumberFormatException, " + e.getMessage());
        } catch (IOException e) {
            throw new InvalidPropertyException("DataWatt, validateProperties, IOException, " + e.getMessage());
        }
    }

    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException("DataWatt, setRegister");
    }

    public void setTime() throws ProtocolException {
        try {
            Calendar calendar = ProtocolUtils.getCalendar(timeZone);
            calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
            getApplicationFunction().clockSynchronizationASDU(calendar);
        } catch (IOException e) {
            throw new ProtocolException("DataWatt, setTime, " + e.getMessage());
        }
    }

    // implementation of IEC870ProtocolLink
    public String getPassword() {
        return strPassword;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    public void release() throws IOException {
    }

} // public class DataWatt implements MeterProtocol
