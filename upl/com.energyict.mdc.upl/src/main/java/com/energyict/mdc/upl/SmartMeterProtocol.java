package com.energyict.mdc.upl;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * SmartMeterProtocol is an extension to the standard {@link MeterProtocol} interface.
 * The basic idea is to do more bulk request and adjust our framework to the current smarter meter market.
 */
@ConsumerType
public interface SmartMeterProtocol extends HasDynamicProperties {

    enum Property {

        ADDRESS("DeviceId"),
        PASSWORD("Password"),
        PROFILEINTERVAL("ProfileInterval"),
        SERIALNUMBER("SerialNumber"),
        NODEID("NodeAddress"),
        MAXTIMEDIFF("MaximumTimeDiff"),
        MINTIMEDIFF("MinimumTimeDiff"),

        /**
         * The string used for the roundtripCorrection property.
         * This property is used by the getTime() and setTime() method
         * to correct the communication roundtrip.
         */
        ROUNDTRIPCORR("RoundtripCorrection"),

        /**
         * The string used for the correctTime property.
         * The property is used only by the protocoltester software.
         */
        CORRECTTIME("CorrectTime"),

        /**
         * This string used for the ExtraIntervals property.
         * The property is used to subtract nr of ExtraIntervals from last reading so to request ExtraIntervals more profile data
         * from a meter. This is don to ensure that enough intervals are read to calculate advances from cumulative values!
         */
        EXTRAINTERVALS("ExtraIntervals"),

        /**
         * The string used for the protocol classname property
         */
        PROTOCOL("ProtocolReader"),

        /**
         * Defines the number of retries a certain communicationRequest may be retried
         */
        RETRIES("Retries");

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
     * Validates the protocol specific properties.
     * The implementer has to specify which keys are mandatory,
     * </p><p>
     *
     * @throws InvalidPropertyException if a property value is not compatible with the device type
     * @throws MissingPropertyException if a required property is not present
     */
    void validateProperties() throws InvalidPropertyException, MissingPropertyException;

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
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     * @throws IOException Thrown in case of an exception
     */
    String getFirmwareVersion() throws IOException;

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws IOException thrown in case of an exception
     */
    String getMeterSerialNumber() throws IOException;

    /**
     * @return the current device time
     * @throws IOException <br>
     */
    Date getTime() throws IOException;

    /**
     * <p>
     * sets the device time to the current system time.
     * </p>
     *
     * @param newMeterTime the time to set in the meter
     * @throws IOException Thrown in case of an exception
     */
    void setTime(Date newMeterTime) throws IOException;

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

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the meter.
     * Build up a list of <CODE>LoadProfileConfiguration</CODE> objects and return them so the
     * framework can validate them to the configuration in EIServer
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>LoadProfileConfiguration</CODE> objects corresponding with the meter
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException;

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException;
}
