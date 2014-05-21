package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the store functionality of the {@link CollectedLoadProfileDeviceCommand}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/01/14
 * Time: 15:30
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedLoadProfileStoreDeviceCommandTest extends AbstractCollectedDataIntegrationTest {

    private final int intervalValueOne = 123;
    private final int intervalValueTwo = 6516516;
    private final Unit kiloWattHours = Unit.get("kWh");

    private Date verificationTimeStamp = new DateTime(2015, 0, 0, 0, 0, 0, 0).toDate();
    private Date currentTimeStamp = new DateTime(2014, 0, 13, 10, 0, 0, 0).toDate();

    private Date fromClock = new DateTime(2013, 0, 1, 0, 0, 0, 0).toDate();
    private Date intervalEndTime1 = new DateTime(2014, 0, 1, 0, 0, 0, 0).toDate();
    private Date intervalEndTime2 = new DateTime(2014, 0, 1, 0, 15, 0, 0).toDate();
    private Date intervalEndTime3 = new DateTime(2014, 0, 1, 0, 30, 0, 0).toDate();
    private Date intervalEndTime4 = new DateTime(2014, 0, 1, 0, 45, 0, 0).toDate();

    @Mock
    private MeteringService meteringService;

    @Test
    public void successfulStoreWithDeltaDataTest() {
        int deviceId = 99875;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfileWithDeltaData(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();


        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

//        Clocks.setAppServerClock(verificationTimeStamp);
//        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);

        assertThat(intervalReadingsChannel1.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreTest() throws SQLException, BusinessException {
        int deviceId = 651;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();


        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

//        Clocks.setAppServerClock(verificationTimeStamp);
//        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulDoubleStoreTestWithSameData() {
        int deviceId = 9854651;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });
        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

//        Clocks.setAppServerClock(verificationTimeStamp);
//        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreWithUpdatedDataTest() {
        int deviceId = 4451;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

        List<IntervalData> updatedCollectedIntervalData = new ArrayList<>();

        List<IntervalValue> updatedIntervalList = new ArrayList<>();
        int updatedIntervalChannelOne = 7777;
        int updatedIntervalChannelTwo = 3333;
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelOne, 0, 0));
        updatedIntervalList.add(new IntervalValue(updatedIntervalChannelTwo, 0, 0));
        updatedCollectedIntervalData.add(new IntervalData(intervalEndTime3, 0, 0, 0, updatedIntervalList));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(updatedCollectedIntervalData);

        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

//        Clocks.setAppServerClock(verificationTimeStamp);
//        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel1).hasSize(4);
        assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(7653));
        assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-7651));
        assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock, verificationTimeStamp));
        assertThat(intervalReadingsChannel2).hasSize(4);
        assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-6513184));
        assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(6513186));
        assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void updateLastReadingTest() throws SQLException, BusinessException {
        long deviceId = 45456;
        LoadProfile mockedLoadProfile = createMockedLoadProfile();
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(deviceId);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = mock(LoadProfile.LoadProfileUpdater.class);
        when(device.getLoadProfileUpdaterFor(mockedLoadProfile)).thenReturn(loadProfileUpdater);
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(device, mockedLoadProfile);

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        // Business method
        collectedLoadProfileDeviceCommand.execute(comServerDAO);

        // Asserts
        verify(device).getLoadProfileUpdaterFor(mockedLoadProfile);
        verify(loadProfileUpdater).setLastReadingIfLater(intervalEndTime4);
        verify(loadProfileUpdater).update();
    }

    private CollectedLoadProfile createCollectedLoadProfileWithDeltaData(int deviceId, LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(deviceId, loadProfile, createMockLoadProfileWithTwoDeltaChannels());
    }

    private CollectedLoadProfile createCollectedLoadProfile(long deviceId, LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(deviceId, loadProfile, createMockLoadProfileWithTwoChannels());
    }

    private CollectedLoadProfile enhanceCollectedLoadProfile(long deviceId, LoadProfile loadProfile, CollectedLoadProfile collectedLoadProfile) {
        Device device = mockDevice(deviceId);
        return enhanceCollectedLoadProfile(device, loadProfile, collectedLoadProfile);
    }

    private CollectedLoadProfile createCollectedLoadProfile(Device device, LoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(device, loadProfile, createMockLoadProfileWithTwoChannels());
    }

    private CollectedLoadProfile enhanceCollectedLoadProfile(Device device, LoadProfile loadProfile, CollectedLoadProfile collectedLoadProfile) {
        when(loadProfile.getDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.findLoadProfile()).thenReturn(loadProfile);
        when(collectedLoadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileIdentifier);
        return collectedLoadProfile;
    }

    private LoadProfile createMockedLoadProfile() {
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getInterval()).thenReturn(new TimeDuration(15, TimeDuration.MINUTES));
        return loadProfile;
    }

    private List<Channel> getChannels(int deviceId) {
        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(1);
        for (MeterActivation meterActivation : amrSystem.get().findMeter(String.valueOf(deviceId)).get().getMeterActivations()) {
            if (meterActivation.isCurrent()) {
                return meterActivation.getChannels();
            }
        }
        return Collections.emptyList();
    }

    private CollectedLoadProfile createMockLoadProfileWithTwoDeltaChannels() {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        List<ChannelInfo> mockedChannelInfos = createMockedDeltaChannelInfos();
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    private CollectedLoadProfile createMockLoadProfileWithTwoChannels() {
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        List<ChannelInfo> mockedChannelInfos = createMockedChannelInfos();
        when(collectedLoadProfile.getChannelInfo()).thenReturn(mockedChannelInfos);
        List<IntervalData> mockedCollectedIntervalData = createMockedIntervalData();
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(mockedCollectedIntervalData);
        return collectedLoadProfile;
    }

    private List<IntervalData> createMockedIntervalData() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        intervalDatas.add(new IntervalData(intervalEndTime1, 0, 0, 0, getIntervalValues(0)));
        intervalDatas.add(new IntervalData(intervalEndTime2, 0, 0, 0, getIntervalValues(1)));
        intervalDatas.add(new IntervalData(intervalEndTime3, 0, 0, 0, getIntervalValues(2)));
        intervalDatas.add(new IntervalData(intervalEndTime4, 0, 0, 0, getIntervalValues(3)));
        return intervalDatas;
    }

    private List<ChannelInfo> createMockedDeltaChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        String deltaChannelObisCodeOne = "1.0.1.6.0.255";
        channelInfos.add(new ChannelInfo(1, deltaChannelObisCodeOne, kiloWattHours));
        String deltaChannelObisCodeTwo = "1.0.2.6.0.255";
        channelInfos.add(new ChannelInfo(2, deltaChannelObisCodeTwo, kiloWattHours));
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        String channelObisCodeOne = "1.0.1.8.0.255";
        channelInfos.add(new ChannelInfo(1, channelObisCodeOne, kiloWattHours));
        String channelObisCodeTwo = "1.0.2.8.0.255";
        channelInfos.add(new ChannelInfo(2, channelObisCodeTwo, kiloWattHours));
        return channelInfos;
    }

    public List<IntervalValue> getIntervalValues(int addition) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(intervalValueOne + addition, 0, 0));
        intervalValues.add(new IntervalValue(intervalValueTwo + addition, 0, 0));
        return intervalValues;
    }
}
