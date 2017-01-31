/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.legacy.dynamic.Pluggable;
import com.energyict.mdc.protocol.api.tasks.support.DeviceDescriptionSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * <p>
 * MeterProtocol defines the interface between a data collection
 * system and the meter protocol implementation. The interface can both be
 * used at operational time and at configuration time
 * <p/>
 * During normal operations the data collection system will call the MeterProtocol
 * methods in the following sequence:
 * <ul>
 * <li> setProperties </li>
 * <li> init </li>
 * <li> connect </li>
 * <li> any of the get , set and initializeDevice methods in undefined sequence </li>
 * <li> release </li>
 * <li> disconnect </li>
 * </ul>
 * At configuration time, the getRequiredKeys, getOptionalKeys and setProperties methods
 * can be called in any sequence.
 *
 * @author Karel
 *         KV 15122003 serialnumber of the device
 */

public interface MeterProtocol extends Pluggable, CachingProtocol, DeviceDescriptionSupport {

    /**
     * The string typically used for the device address property
     */
    String ADDRESS = "DeviceId";

    /**
     * The string used for the protocol password property
     */
    String PASSWORD = "Password";

    /**
     * The string used for the protocol ProfileInterval property
     */
    String PROFILEINTERVAL = "ProfileInterval";

    /**
     * The string used for the serialNumber property
     */
    String SERIALNUMBER = "SerialNumber";

    /**
     * The string used for the nodeId property
     */
    String NODEID = "NodeAddress";

    /**
     * The string used for the roundtripCorrection property.
     * This property is used by the getTime() and setTime() method
     * to correct the communication roundtrip.
     */
    String ROUNDTRIPCORR = "RoundtripCorrection";

    /**
     * The string used for the correctTime property.
     * The property is used only by the protocoltester software.
     */
    String CORRECTTIME = "CorrectTime";

    /**
     * The string used for the protocol classname property
     */
    String PROTOCOL = "ProtocolReader";

    /**
     * <p>
     * Sets the protocol specific properties.
     * </p><p>
     * This method can also be called at device configuration time to check the validity of
     * the configured values </p><p>
     * The implementer has to specify which keys are mandatory,
     * and which are optional. Convention is to use lower case keys.</p><p>
     * Typical keys are: <br>
     * "address"  (MeterProtocol.ADDRESS) <br>
     * "password"  (MeterProtocol.PASSWORD) </p>
     *
     * @param properties contains a set of protocol specific key value pairs
     * @throws InvalidPropertyException if a property value is not compatible with the device type
     * @throws MissingPropertyException if a required property is not present
     */
    void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException;

    /**
     * <p>
     * Initializes the MeterProtocol.
     * </p><p>
     * Implementers should save the arguments for future use.
     * </p><p>
     * All times exchanged between the data collection system and a MeterProtocol are java.util.Date ,
     * expressed in milliseconds since 1/1/1970 in UTC. The implementer has
     * to convert the device times to UTC. </p><p>
     * The timeZone argument is the timezone that is configured in the collecting system
     * for the device. If the device knows its own timezone, this argument can be ignored </p><p>
     * Implementers can use the argument to convert from device
     * time format to java.util.Date, e.g.</p>
     * <PRE>
     * Calendar deviceCalendar = Calendar.getInstance(timeZone);
     * deviceCalendar.clear();
     * deviceCalendar.set(year, month - 1 , day , hour , minute , second);
     * java.util.Date deviceDate = deviceCalendar.getTime();
     * </PRE>
     * <p>
     * The last argument is used to inform the data collection system of problems and/or progress.
     * Messages with level INFO or above are logged to the collection system's
     * logbook. Messages with level below INFO are only displayed in diagnostic mode
     * </p>
     *
     * @param inputStream  byte stream to read data from the device
     * @param outputStream byte stream to send data to the device
     * @param timeZone     the device's timezone
     * @param logger       used to provide feedback to the collection system
     * @throws IOException Thrown when an exception happens
     */
    void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException;

    /**
     * <p>
     * Sets up the logical connection with the device.
     * </p><p>
     * As the physical connection has already been setup by the collection system,
     * it is up to the implementer to decide if any additional implementation is needed
     * </p>
     *
     * @throws IOException <br>
     */
    void connect() throws IOException;

    /**
     * Terminates the logical connection with the device.
     * The implementer should not close the inputStream and outputStream. This
     * is the responsibility of the collection system
     *
     * @throws IOException thrown in case of an exception
     */
    void disconnect() throws IOException;

    /**
     * @return the version of the specific protocol implementation
     */
    String getProtocolVersion();

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     *         </p>
     * @throws IOException          Thrown in case of an exception
     * @throws UnsupportedException Thrown if method is not supported
     */
    String getFirmwareVersion() throws IOException;

    /**
     * Gets the current status of the breaker<br/>
     * Note: if the {@link MeterProtocol} doesn't support breaker functionality (e.g. the device is
     * not equipped with a breaker), then {@link Optional#empty()} should be returned.
     *
     * @return the current status of the breaker
     * @throws IOException Thrown in case of an exception
     */
    default Optional<BreakerStatus> getBreakerStatus() throws IOException {
        return Optional.empty();
    }

    /**
     * Gets the name of the active calendar that is currently configured on the device.
     * Note: if the {@link MeterProtocol} doesn't support calendar functionality,
     * then {@link Optional#empty()} should be returned.
     *
     * @return The name of the active calendar
     * @throws IOException
     */
    default Optional<String> getActiveCalendarName() throws IOException {
        return Optional.empty();
    }

    /**
     * Gets the name of the passive calendar that is currently configured on the device.
     * Note: if the {@link MeterProtocol} doesn't support calendar functionality,
     * then {@link Optional#empty()} should be returned.
     *
     * @return The name of the passive calendar
     * @throws IOException
     */
    default Optional<String> getPassiveCalendarName() throws IOException {
        return Optional.empty();
    }

    /**
     * <p>
     * Fetches profile data from the device.
     * </p><p>
     * The includeEvents flag indicates whether the data collection
     * system will process the MeterEvents in the returned data. This
     * is only provided as a hint. An implementation is free to ignore
     * this value based on the protocol capabilities</p>
     *
     * @param includeEvents indicates whether events need to be included
     * @return profile data containing interval records and optional meter events
     *         </p>
     * @throws IOException <br>
     */
    ProfileData getProfileData(boolean includeEvents) throws IOException;

    /**
     * <p>
     * Fetches profile data from the device.
     * </p><p>
     * The includeEvents flag indicates whether the data collection
     * system will process the MeterEvents in the returned data. This
     * is only provided as a hint. An implementation is free to ignore
     * this value based on the protocol capabilities</p>
     * Implementors should throw an exception if all data since lastReading
     * can not be fetched, as the collecting system will update its lastReading
     * setting based on the returned ProfileData
     * </p><p>
     *
     * @param includeEvents indicates whether events need to be included
     * @param lastReading   retrieve all data younger than lastReading
     *                      </p><p>
     * @return profile data containing interval records and optional meter events
     *         </p>
     * @throws IOException <br>
     */
    ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException;

    /**
     * <p>
     * Fetches profile data from the device.
     * </p><p>
     * The includeEvents flag indicates whether the data collection
     * system will process the MeterEvents in the returned data. This
     * is only provided as a hint. An implementation is free to ignore
     * this value based on the protocol capabilities</p>
     * Implementors should throw an exception if data between from and to
     * can not be fetched, as the collecting system will update its lastReading
     * setting based on the returned ProfileData
     * </p><p>
     *
     * @param includeEvents indicates whether events need to be included
     * @param from          retrieve all data starting with from date
     * @param to            retrieve all data until to date
     *                      </p><p>
     * @return profile data containing interval records and optional meter events between from and to
     *         </p>
     * @throws IOException          <br>
     * @throws UnsupportedException if meter does not support a to date to request the profile data
     */
    ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws UnsupportedException, IOException;

    /**
     * Fetches the meter reading for the specified logical device channel.
     *
     * @param channelId index of the channel. Indexes start with 1
     *                  </p><p>
     * @return meter register value as Quantity
     * @throws IOException          <br>
     * @throws UnsupportedException if the device does not support this operation
     * @deprecated Replaced by the RegisterProtocol interface method readRegister(...)
     */
    @Deprecated
    Quantity getMeterReading(int channelId) throws UnsupportedException, IOException;

    /**
     * Fetches the meterreading for the specified register, represented as a String
     *
     * @param name register name
     * @return meter register value as Quantity
     * @throws UnsupportedException Thrown if the method is not supported by the protocol
     * @throws IOException          Thrown in case of an exception
     * @deprecated Replaced by the RegisterProtocol interface method readRegister(...)
     */
    @Deprecated
    Quantity getMeterReading(String name) throws UnsupportedException, IOException;

    /**
     * <p></p>
     *
     * @return the device's number of logical channels
     *         </p>
     * @throws IOException          <br>
     * @throws UnsupportedException if the device does not support this operation
     */
    int getNumberOfChannels() throws UnsupportedException, IOException;

    /**
     * <p></p>
     *
     * @return the device's current profile interval in seconds
     *         </p>
     * @throws IOException          <br>
     * @throws UnsupportedException if the device does not support this operation
     */
    int getProfileInterval() throws UnsupportedException, IOException;

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws IOException <br>
     */
    Date getTime() throws IOException;

    /**
     * <p></p>
     *
     * @param name Register name. Devices supporting OBIS codes,
     *             should use the OBIS code as register name
     *             </p>
     * @return the value for the specified register
     *         </p><p>
     * @throws IOException             <br>
     * @throws UnsupportedException    if the device does not support this operation
     * @throws NoSuchRegisterException if the device does not support the specified register
     */
    String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException;

    /**
     * <p>
     * sets the specified register to value
     * </p>
     *
     * @param name  Register name. Devices supporting OBIS codes,
     *              should use the OBIS code as register name
     * @param value to set the register.
     *              </p>
     * @throws IOException             <br>
     * @throws UnsupportedException    if the device does not support this operation
     * @throws NoSuchRegisterException if the device does not support the specified register
     */
    void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException;

    /**
     * <p>
     * sets the device time to the current system time.
     * </p><p>
     *
     * @throws IOException Thrown in case of an exception
     */
    void setTime() throws IOException;

    /**
     * <p>
     * Initializes the device, typically clearing all profile data
     * </p>
     *
     * @throws IOException          <br>
     * @throws UnsupportedException if the device does not support this operation
     */
    void initializeDevice() throws IOException, UnsupportedException;

    /**
     * This method is called by the collection software before the disconnect()
     * and can be used to free resources that cannot be freed in the disconnect()
     * method.
     *
     * @throws IOException Thrown in case of an exception
     */
    void release() throws IOException;

}
