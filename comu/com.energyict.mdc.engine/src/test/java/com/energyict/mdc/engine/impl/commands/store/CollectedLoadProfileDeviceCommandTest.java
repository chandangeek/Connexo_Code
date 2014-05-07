package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.Unit;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.comserver.time.PredefinedTickingClock;
import com.energyict.mdc.meterdata.DeviceLoadProfile;
import com.energyict.mdc.meterdata.identifiers.LoadProfileDataIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import org.junit.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
* Tests the {@link CollectedLoadProfileDeviceCommand} component.
* Primary focus is the toString method for now.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-12-12 (11:51)
*/
public class CollectedLoadProfileDeviceCommandTest {

    private static final int DEVICE_ID = 1;
    private static final int CHANNEL1_ID = DEVICE_ID + 1;
    private static final int CHANNEL2_ID = CHANNEL1_ID + 1;
    private static final int CHANNEL_INFO_ID = CHANNEL2_ID + 1;
    private static final String OBIS_CODE = "1.33.1.8.0.255";

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    @Test
    public void testToJournalMessageDescriptionWithoutCollectedData () {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID)));
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, issueService, clock);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=null, end=null}; channels: }");
    }

    @Test
    public void testToJournalMessageDescriptionWithOneInterval () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 12, 53, 5, 0);
        Clocks.setAppServerClock(frozenClock);
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID)));
        List<IntervalData> intervalData = Arrays.asList(new IntervalData(Clocks.getAppServerClock().now()));
        List<ChannelInfo> channelInfo = Arrays.asList(new ChannelInfo(CHANNEL_INFO_ID, CHANNEL1_ID, "testToStringWithOneInterval", Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, issueService, clock);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Wed Dec 12 13:53:05 CET 2012, end=Wed Dec 12 13:53:05 CET 2012}; channels: 2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithMultipleIntervalsFromOneChannel () {
        FrozenClock firstTick = FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 12, 45, 0, 0);
        FrozenClock lastTick = FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 13, 30, 0, 0);
        PredefinedTickingClock clock = new PredefinedTickingClock(
                firstTick,
                FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 13, 0, 0, 0),
                FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 13, 15, 0, 0),
                lastTick);
        Clocks.setAppServerClock(clock);
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID)));
        List<IntervalData> intervalData =
                Arrays.asList(
                        new IntervalData(Clocks.getAppServerClock().now()),
                        new IntervalData(Clocks.getAppServerClock().now()),
                        new IntervalData(Clocks.getAppServerClock().now()),
                        new IntervalData(Clocks.getAppServerClock().now()));
        List<ChannelInfo> channelInfo =
                Arrays.asList(
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL1_ID,
                                "testToStringWithMultipleIntervalsFromOneChannel",
                                Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, issueService, clock);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Wed Dec 12 13:45:00 CET 2012, end=Wed Dec 12 14:30:00 CET 2012}; channels: 2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithIntervalsFromMultipleChannels () {
        FrozenClock firstTick = FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 12, 45, 0, 0);
        FrozenClock lastTick = FrozenClock.frozenOn(2012, Calendar.DECEMBER, 12, 13, 0, 0, 0);
        PredefinedTickingClock clock = new PredefinedTickingClock(firstTick, lastTick);
        Clocks.setAppServerClock(clock);
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID)));
        List<IntervalData> intervalData =
                Arrays.asList(
                        new IntervalData(Clocks.getAppServerClock().now()),
                        new IntervalData(Clocks.getAppServerClock().now()),
                        new IntervalData(Clocks.getAppServerClock().now()),
                        new IntervalData(Clocks.getAppServerClock().now()));
        List<ChannelInfo> channelInfo =
                Arrays.asList(
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL1_ID,
                                "Channel-1",
                                Unit.get("kWh")),
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL2_ID,
                                "Channel-2",
                                Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile, issueService, clock);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName() +
                " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Wed Dec 12 13:45:00 CET 2012, end=Wed Dec 12 14:00:00 CET 2012}; channels: 2, 3}");
    }
}