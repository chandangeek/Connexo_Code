package com.energyict.protocolimpl.iec1107.unilog;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 7-dec-2010
 * Time: 16:51:30
 */
public abstract class AbstractUnilog extends PluggableMeterProtocol implements RegisterProtocol, ProtocolLink, MeterExceptionInfo {

    private Logger logger;
    private TimeZone timeZone;

    public AbstractUnilog(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /**
     * Validate the properties and copy the correct value to
     * the matching field for further use in the protocol
     *
     * @param properties
     */
    protected abstract void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;

    /**
     * Getter for the logger. If the logger is null, create a new one
     *
     * @return
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    /**
     * Setter for the logger field
     *
     * @param logger
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Getter for the timeZone field
     *
     * @return
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Getter for the timeZone field
     *
     * @param timeZone
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Get the protocol properties, and validate them
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
    }

    /**
     * Check if all the required properties are available. If not, throw an MissingPropertyException
     *
     * @param properties
     * @throws MissingPropertyException
     */
    protected void checkMissingProperties(Properties properties) throws MissingPropertyException {
        Iterator iterator = getRequiredKeys().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (properties.getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing");
            }
        }
    }

    protected abstract List<String> getRequiredKeys();

    protected abstract List<String> getOptionalKeys();

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /**
     * Read the profiledata from now - 10 days until now.
     *
     * @param includeEvents
     * @return
     * @throws java.io.IOException
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    /**
     * Het a description for a given register, identified by its obiscode
     *
     * @param obisCode
     * @return
     * @throws IOException
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    /**
     * Read the profiledata from the last reading until now.
     *
     * @param lastReading
     * @param includeEvents
     * @return
     * @throws IOException
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @param name
     * @return
     * @throws UnsupportedException
     * @throws IOException
     */
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException("Method 'getMeterReading(String name)' not supported in the Unigas300 protocol.");
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @param channelId
     * @return
     * @throws UnsupportedException
     * @throws IOException
     */
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException("Method 'getMeterReading(int channelId)' not supported in the Unigas300 protocol.");
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @param name <br>
     * @return the register value
     * @throws IOException             <br>
     * @throws UnsupportedException    <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        throw new UnsupportedException("Method 'getRegister(String name)' not supported in the Unigas300 protocol.");
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @param name  <br>
     * @param value <br>
     * @throws IOException             <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException    <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException("Method 'getRegister(String name)' not supported in the Unigas300 protocol.");
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException("Method 'initializeDevice()' not supported in the Unigas300 protocol.");
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @return
     */
    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @return
     */
    public ChannelMap getChannelMap() {
        return null;
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @throws IOException
     */
    public void release() throws IOException {
    }

    /**
     * Get a description for a given meter error.
     *
     * @param id
     * @return
     */
    public String getExceptionInfo(String id) {
        if (id != null && id.equals("ERROR")) {
            return "Request could not execute!";
        } else {
            return "No meter specific exception info for " + id;
        }
    }


}
