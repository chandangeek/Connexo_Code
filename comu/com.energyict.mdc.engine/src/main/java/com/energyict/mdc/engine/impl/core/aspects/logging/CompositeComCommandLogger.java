package com.energyict.mdc.engine.impl.core.aspects.logging;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link ComCommandLogger} interface
 * that delegates all calls to a set of actual ComCommandLoggers.
 * All actual logging is done from the {@link AbstractComCommandLogging} aspect
 * but the current two concrete implementation have different log level requirements.
 * This implementation detail should not be exposed to the ExecutionContext
 * that holds the ComCommandLogger.
 * This class provides API to support multiple initialization calls
 * from the concrete aspects and to silently ignore
 * all but the first from each concrete aspect class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-01 (13:17)
 */
public class CompositeComCommandLogger implements ComCommandLogger {

    private Set<ComCommandLogger> actualLoggers = new HashSet<>();

    /**
     * Adds a new Logger and starts it, i.e. calls the started method
     * on the logger, iff the logger is the first instance of its kind.
     *
     * @param logger The ComCommandLogger
     */
    public void start (ComCommandLogger logger) {
        this.actualLoggers.add(logger);
        logger.started();
    }

    @Override
    public void started () {
        // Actual loggers are started when they are added
    }

    @Override
    public void startedBasicCheckCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedBasicCheckCommand();
        }
    }

    @Override
    public void startedToVerifySerialNumber () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedToVerifySerialNumber();
        }
    }

    @Override
    public void serialNumberMisMatch (String deviceSerialNumber, String eiserverSerialNumber) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.serialNumberMisMatch(deviceSerialNumber, eiserverSerialNumber);
        }
    }

    @Override
    public void startedTimeDifferenceCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedTimeDifferenceCommand();
        }
    }

    @Override
    public void startedVerifyTimeDifferenceCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedVerifyTimeDifferenceCommand();
        }
    }

    @Override
    public void timeDifferenceExceeded (long actualTimeDifference, long maximumTimeDifference) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.timeDifferenceExceeded(actualTimeDifference, maximumTimeDifference);
        }
    }

    @Override
    public void startedClockCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedClockCommand();
        }
    }

    @Override
    public void startedSetClock (String newTime) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedSetClock(newTime);
        }
    }

    @Override
    public void timeDifferenceBelowMinimum (long timeDifference) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.timeDifferenceBelowMinimum(timeDifference);
        }
    }

    @Override
    public void timeDifferenceAboveMaximum (long timeDifference) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.timeDifferenceAboveMaximum(timeDifference);
        }
    }

    @Override
    public void startedSynchronizeClockCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedSynchronizeClockCommand();
        }
    }

    @Override
    public void synchronizeClockWithTimeShift (long timeShift) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.synchronizeClockWithTimeShift(timeShift);
        }
    }

    @Override
    public void startedForceClockCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedForceClockCommand();
        }
    }

    @Override
    public void startedLoadProfileCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedLoadProfileCommand();
        }
    }

    @Override
    public void startedReadLoadProfileCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedReadLoadProfileCommand();
        }
    }

    @Override
    public void startedMarkIntervalsAsBadTimeCommand () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedMarkIntervalsAsBadTimeCommand();
        }
    }

    @Override
    public void startedCreateMeterEventsFromStatusBits () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedCreateMeterEventsFromStatusBits();
        }
    }

    @Override
    public void startedToVerifyLoadProfile () {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.startedToVerifyLoadProfile();
        }
    }

    @Override
    public void fetchedLoadProfileConfigurationsFromDevice (String loadProfileConfigurations) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.fetchedLoadProfileConfigurationsFromDevice(loadProfileConfigurations);
        }
    }

    @Override
    public void createdComCommand (String commands) {
        for (ComCommandLogger logger : this.actualLoggers) {
            logger.createdComCommand(commands);
        }
    }

}
