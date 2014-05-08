package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.energyict.mdc.engine.impl.core.aspects.logging.ComCommandLogger;
import com.energyict.mdc.engine.impl.core.aspects.logging.CompositeComCommandLogger;

import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link CompositeComCommandLogger} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-01 (13:44)
 */
public class CompositeComCommandLoggerTest {

    @Test
    public void testStartFirstInstance () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();

        // Business method
        compositeLogger.start(logger);

        // Asserts
        verify(logger).started();
    }

    @Test
    public void testStartedDoesNotDelegate () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.started();

        // Asserts
        verify(logger, never()).started();
    }

    @Test
    public void testStartedBasicCheckCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedBasicCheckCommand();

        // Asserts
        verify(logger).startedBasicCheckCommand();
    }

    @Test
    public void testStartedToVerifySerialNumberDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedToVerifySerialNumber();

        // Asserts
        verify(logger).startedToVerifySerialNumber();
    }

    @Test
    public void testSerialNumberMisMatchDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        String deviceSerialNumber = "deviceSerialNumber";
        String eiserverSerialNumber = "eiserverSerialNumber";
        compositeLogger.serialNumberMisMatch(deviceSerialNumber, eiserverSerialNumber);

        // Asserts
        verify(logger).serialNumberMisMatch(deviceSerialNumber, eiserverSerialNumber);
    }

    @Test
    public void testStartedTimeDifferenceCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedTimeDifferenceCommand();

        // Asserts
        verify(logger).startedTimeDifferenceCommand();
    }

    @Test
    public void testStartedVerifyTimeDifferenceCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedVerifyTimeDifferenceCommand();

        // Asserts
        verify(logger).startedVerifyTimeDifferenceCommand();
    }

    @Test
    public void testTimeDifferenceExceededDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        long actualTimeDifference = 97;
        long maximumTimeDifference = 31;
        compositeLogger.timeDifferenceExceeded(actualTimeDifference, maximumTimeDifference);

        // Asserts
        verify(logger).timeDifferenceExceeded(actualTimeDifference, maximumTimeDifference);
    }

    @Test
    public void testStartedClockCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedClockCommand();

        // Asserts
        verify(logger).startedClockCommand();
    }

    @Test
    public void testStartedSetClockDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        String newTime = "13:52:47";
        compositeLogger.startedSetClock(newTime);

        // Asserts
        verify(logger).startedSetClock(newTime);
    }

    @Test
    public void testTimeDifferenceBelowMinimumDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        int timeDifference = 31;
        compositeLogger.timeDifferenceBelowMinimum(timeDifference);

        // Asserts
        verify(logger).timeDifferenceBelowMinimum(timeDifference);
    }

    @Test
    public void testTimeDifferenceAboveMaximumDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        int timeDifference = 97;
        compositeLogger.timeDifferenceAboveMaximum(timeDifference);

        // Asserts
        verify(logger).timeDifferenceAboveMaximum(timeDifference);
    }

    @Test
    public void testStartedSynchronizeClockCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedSynchronizeClockCommand();

        // Asserts
        verify(logger).startedSynchronizeClockCommand();
    }

    @Test
    public void testSynchronizeClockWithTimeShiftDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        int timeShift = 31;
        compositeLogger.synchronizeClockWithTimeShift(timeShift);

        // Asserts
        verify(logger).synchronizeClockWithTimeShift(timeShift);
    }

    @Test
    public void testStartedForceClockCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedForceClockCommand();

        // Asserts
        verify(logger).startedForceClockCommand();
    }

    @Test
    public void testStartedLoadProfileCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedLoadProfileCommand();

        // Asserts
        verify(logger).startedLoadProfileCommand();
    }

    @Test
    public void testStartedReadLoadProfileCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedReadLoadProfileCommand();

        // Asserts
        verify(logger).startedReadLoadProfileCommand();
    }

    @Test
    public void testStartedMarkIntervalsAsBadTimeCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedMarkIntervalsAsBadTimeCommand();

        // Asserts
        verify(logger).startedMarkIntervalsAsBadTimeCommand();
    }

    @Test
    public void testStartedCreateMeterEventsFromStatusBitsDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedCreateMeterEventsFromStatusBits();

        // Asserts
        verify(logger).startedCreateMeterEventsFromStatusBits();
    }

    @Test
    public void testStartedToVerifyLoadProfileDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        compositeLogger.startedToVerifyLoadProfile();

        // Asserts
        verify(logger).startedToVerifyLoadProfile();
    }

    @Test
    public void testFetchedLoadProfileConfigurationsFromDeviceDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        String loadProfileConfigurations = "load profile configurations";
        compositeLogger.fetchedLoadProfileConfigurationsFromDevice(loadProfileConfigurations);

        // Asserts
        verify(logger).fetchedLoadProfileConfigurationsFromDevice(loadProfileConfigurations);
    }

    @Test
    public void testCreatedComCommandDelegatesToActualLogger () {
        ComCommandLogger logger = mock(ComCommandLogger.class);
        CompositeComCommandLogger compositeLogger = new CompositeComCommandLogger();
        compositeLogger.start(logger);
        reset(logger);

        // Business method
        String commands = "a set of command descriptions";
        compositeLogger.createdComCommand(commands);

        // Asserts
        verify(logger).createdComCommand(commands);
    }

}
