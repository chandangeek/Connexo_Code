package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.Interval;
import com.energyict.comserver.commands.exceptions.RuntimeBusinessException;
import com.energyict.comserver.commands.exceptions.RuntimeSQLException;
import com.energyict.comserver.core.impl.online.ComServerDAOImpl;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.services.impl.Bus;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
    private final String channelObisCodeOne = "1.0.1.8.0.255";
    private final String channelObisCodeTwo = "1.0.2.8.0.255";
    private final String deltaChannelObisCodeOne = "1.0.1.6.0.255";
    private final String deltaChannelObisCodeTwo = "1.0.2.6.0.255";
    private final Unit kiloWattHours = Unit.get("kWh");

    private FrozenClock verificationTimeStamp = FrozenClock.frozenOn(2015, 0, 0, 0, 0, 0, 0);
    private FrozenClock currentTimeStamp = FrozenClock.frozenOn(2014, 0, 13, 10, 0, 0, 0);

    private FrozenClock fromClock = FrozenClock.frozenOn(2013, 0, 1, 0, 0, 0, 0);
    private FrozenClock intervalEndTime1 = FrozenClock.frozenOn(2014, 0, 1, 0, 0, 0, 0);
    private FrozenClock intervalEndTime2 = FrozenClock.frozenOn(2014, 0, 1, 0, 15, 0, 0);
    private FrozenClock intervalEndTime3 = FrozenClock.frozenOn(2014, 0, 1, 0, 30, 0, 0);
    private FrozenClock intervalEndTime4 = FrozenClock.frozenOn(2014, 0, 1, 0, 45, 0, 0);

    @Test
    public void successfulStoreWithDeltaDataTest() {
        int deviceId = 99875;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfileWithDeltaData(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();


        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

        Clocks.setAppServerClock(verificationTimeStamp);
        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        Assertions.assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel1).hasSize(4);

        Assertions.assertThat(intervalReadingsChannel1.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel2).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel2.get(0).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreTest() throws SQLException, BusinessException {
        int deviceId = 651;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();


        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

        Clocks.setAppServerClock(verificationTimeStamp);
        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        Assertions.assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel1).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        Assertions.assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel2).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        Assertions.assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulDoubleStoreTestWithSameData() {
        int deviceId = 9854651;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
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

        Clocks.setAppServerClock(verificationTimeStamp);
        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        Assertions.assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel1).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        Assertions.assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 2));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel2).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        Assertions.assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 2));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void successfulStoreWithUpdatedDataTest() {
        int deviceId = 4451;
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, createMockedLoadProfile());

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
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
        updatedCollectedIntervalData.add(new IntervalData(intervalEndTime3.now(), 0, 0, 0, updatedIntervalList));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(updatedCollectedIntervalData);

        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

        Clocks.setAppServerClock(verificationTimeStamp);
        Clocks.setDatabaseServerClock(verificationTimeStamp);

        List<Channel> channels = getChannels(deviceId);

        Assertions.assertThat(channels.size()).isEqualTo(2);
        List<IntervalReadingRecord> intervalReadingsChannel1 = channels.get(0).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel1).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel1.get(0).getValue()).isNull();
        Assertions.assertThat(intervalReadingsChannel1.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel1.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 1));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(7653));
        Assertions.assertThat(intervalReadingsChannel1.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelOne));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-7651));
        Assertions.assertThat(intervalReadingsChannel1.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueOne + 3));
        List<IntervalReadingRecord> intervalReadingsChannel2 = channels.get(1).getIntervalReadings(new Interval(fromClock.now(), verificationTimeStamp.now()));
        Assertions.assertThat(intervalReadingsChannel2).hasSize(4);
        Assertions.assertThat(intervalReadingsChannel2.get(0).getValue()).isNull();
        Assertions.assertThat(intervalReadingsChannel2.get(0).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(0).getValue()).isEqualTo(new BigDecimal(1));
        Assertions.assertThat(intervalReadingsChannel2.get(1).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 1));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(0).getValue()).isEqualTo(new BigDecimal(-6513184));
        Assertions.assertThat(intervalReadingsChannel2.get(2).getQuantity(1).getValue()).isEqualTo(new BigDecimal(updatedIntervalChannelTwo));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(0).getValue()).isEqualTo(new BigDecimal(6513186));
        Assertions.assertThat(intervalReadingsChannel2.get(3).getQuantity(1).getValue()).isEqualTo(new BigDecimal(intervalValueTwo + 3));
    }

    @Test
    public void updateLastReadingTest() throws SQLException, BusinessException {
        int deviceId = 45456;
        BaseLoadProfile mockedLoadProfile = createMockedLoadProfile();
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, mockedLoadProfile);

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();


        executeInTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                collectedLoadProfileDeviceCommand.execute(comServerDAO);
            }
        });

        Clocks.setAppServerClock(verificationTimeStamp);
        Clocks.setDatabaseServerClock(verificationTimeStamp);

        verify(mockedLoadProfile).updateLastReadingIfLater(intervalEndTime4.now());
    }

    @Test(expected = RuntimeSQLException.class)
    public void throwSqlExceptionWhenUpdatingLastReadingTest() throws SQLException, BusinessException {
        int deviceId = 45456;
        BaseLoadProfile mockedLoadProfile = createMockedLoadProfile();
        SQLException myCustomSqlException = new SQLException("Exception for testing purposes");
        doThrow(myCustomSqlException).when(mockedLoadProfile).updateLastReadingIfLater(any(Date.class));
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, mockedLoadProfile);

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        try {
            executeInTransaction(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    collectedLoadProfileDeviceCommand.execute(comServerDAO);
                }
            });
        } catch (RuntimeSQLException e) {
            if (!e.getCause().equals(myCustomSqlException)) {
                Assertions.fail("Should have gotten my SqlException, but something else went wrong : " + e.getCause().getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test(expected = RuntimeBusinessException.class)
    public void throwBusinessExceptionWhenUpdatingLastReadingTest() throws SQLException, BusinessException {
        int deviceId = 45456;
        BaseLoadProfile mockedLoadProfile = createMockedLoadProfile();
        BusinessException myCustomBusinessException = new BusinessException("Exception for testing purposes", "Exception pattern");
        Mockito.doThrow(myCustomBusinessException).when(mockedLoadProfile).updateLastReadingIfLater(any(Date.class));
        CollectedLoadProfile collectedLoadProfile = createCollectedLoadProfile(deviceId, mockedLoadProfile);

        final CollectedLoadProfileDeviceCommand collectedLoadProfileDeviceCommand = new CollectedLoadProfileDeviceCommand(collectedLoadProfile, issueService);
        final ComServerDAOImpl comServerDAO = mockComServerDAOButCallRealMethodForMeterReadingStoring();

        try {
            executeInTransaction(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    collectedLoadProfileDeviceCommand.execute(comServerDAO);
                }
            });
        } catch (RuntimeBusinessException e) {
            if (!e.getCause().equals(myCustomBusinessException)) {
                Assertions.fail("Should have gotten my BusinessException, but something else went wrong : " + e.getCause().getMessage());
            } else {
                throw e;
            }
        }
    }

    private CollectedLoadProfile createCollectedLoadProfileWithDeltaData(int deviceId, BaseLoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(deviceId, loadProfile, createMockLoadProfileWithTwoDeltaChannels());
    }

    private CollectedLoadProfile createCollectedLoadProfile(int deviceId, BaseLoadProfile loadProfile) {
        return enhanceCollectedLoadProfile(deviceId, loadProfile, createMockLoadProfileWithTwoChannels());
    }

    private CollectedLoadProfile enhanceCollectedLoadProfile(int deviceId, BaseLoadProfile loadProfile, CollectedLoadProfile collectedLoadProfile) {
        mockServiceLocator();
        mockDevice(deviceId);
        Clocks.setAppServerClock(currentTimeStamp);
        Clocks.setDatabaseServerClock(currentTimeStamp);
        when(loadProfile.getDeviceId()).thenReturn(deviceId);
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        when(loadProfileIdentifier.findLoadProfile()).thenReturn(loadProfile);
        when(collectedLoadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileIdentifier);
        return collectedLoadProfile;
    }

    private BaseLoadProfile createMockedLoadProfile() {
        BaseLoadProfile loadProfile = mock(BaseLoadProfile.class);
        when(loadProfile.getInterval()).thenReturn(new TimeDuration(15, TimeDuration.MINUTES));
        return loadProfile;
    }

    private List<Channel> getChannels(int deviceId) {
        Optional<AmrSystem> amrSystem = Bus.getMeteringService().findAmrSystem(1);
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
        intervalDatas.add(new IntervalData(intervalEndTime1.now(), 0, 0, 0, getIntervalValues(0)));
        intervalDatas.add(new IntervalData(intervalEndTime2.now(), 0, 0, 0, getIntervalValues(1)));
        intervalDatas.add(new IntervalData(intervalEndTime3.now(), 0, 0, 0, getIntervalValues(2)));
        intervalDatas.add(new IntervalData(intervalEndTime4.now(), 0, 0, 0, getIntervalValues(3)));
        return intervalDatas;
    }

    private List<ChannelInfo> createMockedDeltaChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        channelInfos.add(new ChannelInfo(1, deltaChannelObisCodeOne, kiloWattHours));
        channelInfos.add(new ChannelInfo(2, deltaChannelObisCodeTwo, kiloWattHours));
        return channelInfos;
    }

    private List<ChannelInfo> createMockedChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        channelInfos.add(new ChannelInfo(1, channelObisCodeOne, kiloWattHours));
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
