package com.energyict.mdc.upl;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * MeterProtocol defines the interface between a data collection
 * system and the meter protocol implementation.
 * <p>
 * At configuration time, the getRequiredKeys, getOptionalKeys and setProperties methods
 * can be called in any sequence </p><p>
 * <p>
 * During normal operations the data collection system will call the
 * methods in the following sequence:
 * <ul>
 * <li>setProperties</li>
 * <li>init</li>
 * <li>connect</li>
 * <li>any of the get, set and initializeDevice methods in undefined sequence</li>
 * <li>release</li>
 * <li>disconnect</li>
 * </ul>
 * Failing to set a value for a required property will result in a NullPointerException
 * in one of the init, connect, get, set, initializeDevice, release or disconnect methods.
 * <p>
 * At configuration time, the getRequiredKeys, getOptionalKeys and setProperties methods
 * can be called in any sequence.
 * </p>
 *
 * @author Karel
 *         KV 15122003 serialnumber of the device
 */
@ConsumerType
public interface MeterProtocol extends HasDynamicProperties {

    /**
     * Models common properties that can be marked required or optional
     * by the actual MeterProtocol implementation classes.
     */
    enum Property {
        ADDRESS("DeviceId"),
        PASSWORD("Password"),
        RETRIES("Retries"),
        TIMEOUT("Timeout"),
        SECURITYLEVEL("SecurityLevel"),
        PROFILEINTERVAL("ProfileInterval"),
        SERIALNUMBER("SerialNumber"),
        NODEID("NodeAddress"),
        MAXTIMEDIFF("MaximumTimeDiff"),
        MINTIMEDIFF("MinimumTimeDiff"),

        /**
         * This property is used by the getTime() and setTime() method
         * to correct the communication roundtrip.
         */
        ROUNDTRIPCORRECTION("RoundtripCorrection"),

        /**
         * The string used for the correctTime property.
         * The property is used only by the protocoltester software.
         */
        CORRECTTIME("CorrectTime"),

        /**
         * This string used for the ExtraIntervals property.
         * The property is used to subtract nr of ExtraIntervals from last reading
         * so to request ExtraIntervals more profile data from a meter.
         * This is done to ensure that enough intervals are read
         * to calculate advances from cumulative values!
         */
        EXTRAINTERVALS("ExtraIntervals"),

        /**
         * The string used for the protocol classname property
         */
        PROTOCOL("ProtocolReader");

        private final String name;

        Property(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

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
    ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException;

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
    Quantity getMeterReading(int channelId) throws IOException;

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
    Quantity getMeterReading(String name) throws IOException;

    /**
     * <p></p>
     *
     * @return the device's number of logical channels
     *         </p>
     * @throws IOException          <br>
     * @throws UnsupportedException if the device does not support this operation
     */
    int getNumberOfChannels() throws IOException;

    /**
     * <p></p>
     *
     * @return the device's current profile interval in seconds
     *         </p>
     * @throws IOException          <br>
     * @throws UnsupportedException if the device does not support this operation
     */
    int getProfileInterval() throws IOException;

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
    String getRegister(String name) throws IOException;

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
    void setRegister(String name, String value) throws IOException;

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
    void initializeDevice() throws IOException;

    /**
     * This method is called by the collection software before the disconnect()
     * and can be used to free resources that cannot be freed in the disconnect()
     * method.
     *
     * @throws IOException Thrown in case of an exception
     */
    void release() throws IOException;

}