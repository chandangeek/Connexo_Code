/*
 * DataWatt.java
 *
 * Created on 18 juni 2003, 13:56
 */

package com.energyict.protocolimpl.iec870.datawatt;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec870.IEC870Connection;
import com.energyict.protocolimpl.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.iec870.IEC870ProtocolLink;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author Koen
 * @beginchanges KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
public class DataWatt extends PluggableMeterProtocol implements IEC870ProtocolLink {

    static final int MAX_COUNTER = 100000000; // = max counter + 1

    private static final int DEBUG = 0;

    // properties
    private String strID;
    private String strPassword;
    private int iIEC870TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iProfileInterval;
    private int iMeterType;
    private ChannelMap channelMap = null;

    private IEC870Connection iec870Connection = null;
    private ApplicationFunction applicationFunction = null;

    private TimeZone timeZone = null;
    private Logger logger = null;
    private DatawattRegistry datawattRegistry = null;
    private DatawattProfile datawattProfile = null;

    @Override
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

    @Override
    public void disconnect() {
        try {
            iec870Connection.disconnectLink();
        } catch (IEC870ConnectionException e) {
            logger.severe("DataWatt, disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException("DataWatt, getFirmwareVersion");
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return new Quantity(getDatawattRegistry().getRegister(Channel.parseChannel(name)), Unit.get(""));
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        if (channelId >= getChannelMap().getNrOfChannels()) {
            throw new IOException("DataWatt, getMeterReading, invalid channelId, " + channelId);
        }
        return new Quantity(getDatawattRegistry().getRegister(getChannelMap().getChannel(channelId)), Unit.get(""));
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfChannels();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.clear();
        return datawattProfile.getProfileData(fromCalendar, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return datawattProfile.getProfileData(fromCalendar, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    @Override
    public int getProfileInterval() {
        return iProfileInterval;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public String getRegister(String name) throws IOException {
        return getDatawattRegistry().getRegister(Channel.parseChannel(name)).toString();
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

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                "MeterType");
    }

    public List<String> getRequiredKeys() {
        return Collections.singletonList("ChannelMap");
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.string(ADDRESS.getName(), false),
                UPLPropertySpecFactory.string(PASSWORD.getName(), false),
                UPLPropertySpecFactory.integer(TIMEOUT.getName(), false),
                UPLPropertySpecFactory.integer(RETRIES.getName(), false),
                UPLPropertySpecFactory.integer(ROUNDTRIPCORRECTION.getName(), false),
                UPLPropertySpecFactory.string(PROFILEINTERVAL.getName(), false),
                UPLPropertySpecFactory.string("ChannelMap", false),
                UPLPropertySpecFactory.integer("MeterType", false),

                UPLPropertySpecFactory.string(SERIALNUMBER.getName(), false),
                UPLPropertySpecFactory.string(PASSWORD.getName(), false),
                UPLPropertySpecFactory.integer(CORRECTTIME.getName(), false),
                );
    }

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            strID = properties.getProperty(ADDRESS.getName());
            strPassword = properties.getProperty(PASSWORD.getName());
            iIEC870TimeoutProperty = Integer.parseInt(properties.getProperty(TIMEOUT.getName(), "25000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty(RETRIES.getName(), "3").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty(PROFILEINTERVAL.getName(), "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", ""));
            iMeterType = Integer.parseInt(properties.getProperty("MeterType", "0").trim());
        } catch (NumberFormatException | IOException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException("DataWatt, setRegister");
    }

    @Override
    public void setTime() throws ProtocolException {
        try {
            Calendar calendar = ProtocolUtils.getCalendar(timeZone);
            calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
            getApplicationFunction().clockSynchronizationASDU(calendar);
        } catch (IOException e) {
            throw new ProtocolException("DataWatt, setTime, " + e.getMessage());
        }
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public void release() throws IOException {
    }

}