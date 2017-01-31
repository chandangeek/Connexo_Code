/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.legacy.dynamic.Pluggable;
import com.energyict.mdc.protocol.api.tasks.support.DeviceDescriptionSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * SmartMeterProtocol is an extension to the standard {@link MeterProtocol} interface.
 * The basic idea is to do more bulk request and adjust our framework to the current smarter meter market.
 */
public interface SmartMeterProtocol extends Pluggable, MultipleLoadProfileSupport, BulkRegisterProtocol, MeterProtocolEventSupport, CachingProtocol, DeviceDescriptionSupport {

    /**
     * The string typically used for the device address property
     */
    final String ADDRESS = "DeviceId";

    /**
     * The string used for the protocol password property
     */
    final String PASSWORD = "Password";

    /**
     * The string used for the protocol ProfileInterval property
     */
    final String PROFILEINTERVAL = "ProfileInterval";

    /**
     * The string used for the serialNumber property
     */
    final String SERIALNUMBER = "SerialNumber";

    /**
     * The string used for the nodeId property
     */
    final String NODEID = "NodeAddress";

    /**
     * The string used for the roundtripCorrection property.
     * This property is used by the getTime() and setTime() method
     * to correct the communication roundtrip.
     */
    final String ROUNDTRIPCORR = "RoundtripCorrection";

    /**
     * The string used for the correctTime property.
     * The property is used only by the protocoltester software.
     */
    final String CORRECTTIME = "CorrectTime";

    /**
     * The string used for the protocol classname property
     */
    final String PROTOCOL = "ProtocolReader";

    /**
     * Defines the number of retries a certain communicationRequest may be retried
     */
    final String RETRIES = "Retries";

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
     * Gets the current status of the breaker <br/>
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

}
