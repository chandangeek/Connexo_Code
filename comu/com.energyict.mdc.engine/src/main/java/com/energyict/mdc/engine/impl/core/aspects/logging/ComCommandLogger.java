package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * Defines all log messages for the {@link com.energyict.mdc.engine.impl.commands.collect.ComCommand} components.
 *
 * Copyrights EnergyICT
 * Date: 27/08/12
 * Time: 10:22
 */
public interface ComCommandLogger {

    /**
     * Logs that the {@link DeviceProtocol} execution has started.
     */
    @Configuration(format = "Started execution of the commands", logLevel = LogLevel.DEBUG)
    public void started();

    /**
     * Logs that the ComServer has started executing the BasicCheckCommand
     */
    @Configuration(format = "Started the command to perform basic checks", logLevel = LogLevel.DEBUG)
    public void startedBasicCheckCommand();

    /**
     * Logs that the ComServer has started executing the VerifySerialNumber command
     */
    @Configuration(format = "Started to verify the serial number of the device", logLevel = LogLevel.DEBUG)
    public void startedToVerifySerialNumber();

    /**
     * Logs that the verification of the SerialNumber did not match
     *
     * @param deviceSerialNumber   the serialNumber of the device
     * @param eiserverSerialNumber the serialNumber configured in EIServer
     */
    @Configuration(format = "Serialnumber mismatch. Meter: {0} - EIServer: {1}", logLevel = LogLevel.ERROR)
    public void serialNumberMisMatch(String deviceSerialNumber, String eiserverSerialNumber);

    /**
     * Logs that the ComServer has started executing the TimeDifference command
     */
    @Configuration(format = "Started to calculate the timedifference between the ComServer and the device.", logLevel = LogLevel.DEBUG)
    public void startedTimeDifferenceCommand();

    /**
     * Logs that the ComServer has started executing the VerifyTimeDifference command
     */
    @Configuration(format = "Started to verify the timedifference between the ComServer and the device.", logLevel = LogLevel.DEBUG)
    public void startedVerifyTimeDifferenceCommand();

    /**
      * Logs that the timeDifference exceeds the configured maximum timeDifference
      *
      * @param actualTimeDifference the timeDifference between the ComServer and the Device
      * @param maximumTimeDifference the maximum allowed timeDifference
      */
     @Configuration(format = "Timedifference exceeds the configured maximum: The timedifference ({0} ms) is larger than the configured allowed maximum ({1} ms)", logLevel = LogLevel.WARN)
     public void timeDifferenceExceeded(long actualTimeDifference, long maximumTimeDifference);

    /**
     * Logs that the ComServer has started executing the Clock command
     */
    @Configuration(format = "Started the command to handle the clock", logLevel = LogLevel.DEBUG)
    public void startedClockCommand();

    /**
     * Logs that the ComServer will set the clock of the device to the given time
     *
     * @param newTime the given time to set in the device
     */
    @Configuration(format = "Setting the time of the device to {0}", logLevel = LogLevel.WARN)
    public void startedSetClock(String newTime);

    /**
     * Logs that the ComServer will not set the time due to the fact that the timeDifference is below the minimum configured timeDifference
     *
     * @param timeDifference the timeDifference between the ComServer and the Device
     */
    @Configuration(format = "Time difference [{0}ms] is below the minimum, time will not be set", logLevel = LogLevel.WARN)
    public void timeDifferenceBelowMinimum(long timeDifference);

    /**
     * Logs that the ComServer will not set the time due to the fact that the timeDifference is above the maximum configured timeDifference
     *
     * @param timeDifference the timeDifference between the ComServer and the Device
     */
    @Configuration(format = "Time difference [{0}ms] is above the maximum, time will not be set", logLevel = LogLevel.WARN)
    public void timeDifferenceAboveMaximum(long timeDifference);

    /**
     * Logs that the ComServer started the execution of the SynchronizeClockCommand
     */
    @Configuration(format = "Starting to synchronize the clock", logLevel = LogLevel.DEBUG)
    public void startedSynchronizeClockCommand();

    /**
     * Logs that the ComServer will synchronize the Device time with the given time Shift
     *
     * @param timeShift the amount of milliseconds to shift the device time
     */
    @Configuration(format = "Device time will be shifted by {0}ms", logLevel = LogLevel.WARN)
    public void synchronizeClockWithTimeShift(long timeShift);

    /**
     * Logs that the ComServer started the execution of the ForceClock command
     */
    @Configuration(format = "Forcing the device time to the current system time", logLevel = LogLevel.WARN)
    public void startedForceClockCommand();

    /**
     * Logs that the ComServer started the execution of the LoadProfile command
     */
    @Configuration(format = "Starting to handle the load profiles", logLevel = LogLevel.DEBUG)
    public void startedLoadProfileCommand();

    /**
     * Logs that the ComServer started the execution of the ReadLoadProfileCommand
     */
    @Configuration(format = "Starting to read the load profiles", logLevel = LogLevel.DEBUG)
    public void startedReadLoadProfileCommand();

    /**
     * Logs that the ComServer started the execution of the MarkIntervalsAsBadTime command
     */
    @Configuration(format = "Marking intervals as bad time", logLevel = LogLevel.DEBUG)
    public void startedMarkIntervalsAsBadTimeCommand();

    /**
     * Logs that the ComServer started the execution of the CreateMeterEventsFromStatusBits command
     */
    @Configuration(format = "Starting to create meter events from interval status bits", logLevel = LogLevel.DEBUG)
    public void startedCreateMeterEventsFromStatusBits();

    /**
     * Logs that the ComServer started the execution of the VerifyLoadProfileCommand
     */
    @Configuration(format = "Starting to verify the load profile", logLevel = LogLevel.DEBUG)
    public void startedToVerifyLoadProfile();

    /**
     * Logs the configurations which are read from the device
     */
    @Configuration(format = "Load profile configurations read from the device: \\r\\n {0}", logLevel = LogLevel.DEBUG)
    public void fetchedLoadProfileConfigurationsFromDevice(String loadProfileConfigurations);

    /**
     * Logs that the ComServer created the given commands
     *
     * @param commands the commands which were created
     */
    @Configuration(format = "Created the following command : {0}", logLevel = LogLevel.DEBUG)
    public void createdComCommand (String commands);

}