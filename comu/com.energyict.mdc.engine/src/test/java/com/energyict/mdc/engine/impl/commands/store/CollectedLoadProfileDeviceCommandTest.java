package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.impl.meterdata.identifiers.LoadProfileDataIdentifier;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
* Tests the {@link CollectedLoadProfileDeviceCommand} component.
* Primary focus is the toString method for now.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-12-12 (11:51)
*/
@RunWith(MockitoJUnitRunner.class)
public class CollectedLoadProfileDeviceCommandTest {

    private static final long DEVICE_ID = 1;
    private static final long CHANNEL1_ID = DEVICE_ID + 1;
    private static final long CHANNEL2_ID = CHANNEL1_ID + 1;
    private static final long CHANNEL_INFO_ID = CHANNEL2_ID + 1;
    private static final String OBIS_CODE = "1.33.1.8.0.255";

    @Mock
    private DeviceDataService deviceDataService;

    @Test
    public void testToJournalMessageDescriptionWithoutCollectedData () {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=null, end=null}; channels: }");
    }

    @Test
    public void testToJournalMessageDescriptionWithOneInterval () {
        Date now = new DateTime(2012, 12, 12, 12, 53, 5, 0).toDate();
        Clock frozenClock = new ProgrammableClock().frozenAt(now);
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
        List<IntervalData> intervalData = Arrays.asList(new IntervalData(now));
        List<ChannelInfo> channelInfo = Arrays.asList(new ChannelInfo(CHANNEL_INFO_ID, CHANNEL1_ID, "testToStringWithOneInterval", Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Wed Dec 12 13:53:05 CET 2012, end=Wed Dec 12 13:53:05 CET 2012}; channels: 2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithMultipleIntervalsFromOneChannel () {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
        List<IntervalData> intervalData =
                Arrays.asList(
                        new IntervalData(new DateTime(2012, 12, 12, 12, 45, 0, 0).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 0, 0, 0).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 15, 0, 0).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 30, 0, 0).toDate()));
        List<ChannelInfo> channelInfo =
                Arrays.asList(
                        new ChannelInfo(
                                CHANNEL_INFO_ID,
                                CHANNEL1_ID,
                                "testToStringWithMultipleIntervalsFromOneChannel",
                                Unit.get("kWh")));
        deviceLoadProfile.setCollectedData(intervalData, channelInfo);
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName()
                + " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Wed Dec 12 13:45:00 CET 2012, end=Wed Dec 12 14:30:00 CET 2012}; channels: 2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithIntervalsFromMultipleChannels () {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new LoadProfileDataIdentifier(ObisCode.fromString(OBIS_CODE), new DeviceIdentifierById(DEVICE_ID, deviceDataService)));
        List<IntervalData> intervalData =
                Arrays.asList(
                        new IntervalData(new DateTime(2012, 12, 12, 12, 45, 0, 0).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 0, 0, 0).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 12, 45, 0, 0).toDate()),
                        new IntervalData(new DateTime(2012, 12, 12, 13, 0, 0, 0).toDate()));
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
        CollectedLoadProfileDeviceCommand command = new CollectedLoadProfileDeviceCommand(deviceLoadProfile);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedLoadProfileDeviceCommand.class.getSimpleName() +
                " {load profile: 1.33.1.8.0.255; interval data period: Interval{start=Wed Dec 12 13:45:00 CET 2012, end=Wed Dec 12 14:00:00 CET 2012}; channels: 2, 3}");
    }
}