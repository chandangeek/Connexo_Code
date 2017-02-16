/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataLoggerSlaveDeviceInfoFactoryTest extends DeviceDataRestApplicationJerseyTest {

    private final static long DATA_LOGGER_ID = 6666L;
    private final static long REGISTER_TYPE_ID = 121L;
    private final static String DATA_LOGGER_DEVICE_TYPE_NAME = "Data logger device type";
    private final static String collectedReadingTypeMrid = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private final static String calculatedReadingTypeMrid = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0";

    @Mock
    DeviceDataInfoFactory deviceDataInfoFactory;
    @Mock
    private RegisterType registerType;
    @Mock
    private NumericalRegisterSpec registerSpec;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileType loadProfileType;
    @Mock
    private LoadProfileSpec loadProfileSpec;
    @Mock
    private Device dataLogger = mock(Device.class);

    private NumericalRegister dataLoggerRegister1, dataLoggerRegister2, dataLoggerRegister3, dataLoggerRegister4;
    private Channel dataLoggerChannel1, dataLoggerChannel2, dataLoggerChannel3;

    @Test
    public void fromTest() {
        Device dataLogger = mockDataLogger();
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        when(topologyService.getSlaveChannel(eq(dataLoggerChannel1), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerChannel1))).thenReturn(Optional.of(Instant.EPOCH));
        when(topologyService.getSlaveChannel(eq(dataLoggerChannel2), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerChannel2))).thenReturn(Optional.of(now));
        when(topologyService.getSlaveChannel(eq(dataLoggerChannel3), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerChannel3))).thenReturn(Optional.empty());

        when(topologyService.getSlaveRegister(eq(dataLoggerRegister1), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerRegister1))).thenReturn(Optional.of(Instant.EPOCH));
        when(topologyService.getSlaveRegister(eq(dataLoggerRegister2), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerRegister2))).thenReturn(Optional.of(Instant.EPOCH));
        when(topologyService.getSlaveRegister(eq(dataLoggerRegister3), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerRegister3))).thenReturn(Optional.of(Instant.EPOCH));
        when(topologyService.getSlaveRegister(eq(dataLoggerRegister4), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(eq(dataLoggerRegister4))).thenReturn(Optional.of(Instant.EPOCH));

        List<DataLoggerSlaveDeviceInfo> infos = new DataLoggerSlaveDeviceInfoFactory(clock, topologyService, deviceDataInfoFactory, batchService, channelInfoFactory).from(dataLogger);

        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).dataLoggerSlaveChannelInfos).hasSize(3);
        assertThat(infos.get(0).dataLoggerSlaveRegisterInfos).hasSize(4);
        assertThat(infos.get(0).dataLoggerSlaveChannelInfos.stream().filter((x)-> x.slaveChannel == null).count()).isEqualTo(3); // all not linked yet
        assertThat(infos.get(0).dataLoggerSlaveChannelInfos.get(0).availabilityDate).isEqualTo(0L); // availability since Instant.Epoch
        assertThat(infos.get(0).dataLoggerSlaveChannelInfos.get(1).availabilityDate).isEqualTo(now.toEpochMilli()); // availability since now
        assertThat(infos.get(0).dataLoggerSlaveChannelInfos.get(2).availabilityDate).isNull(); // no availability

        assertThat(infos.get(0).dataLoggerSlaveRegisterInfos.stream().filter((x)-> x.slaveRegister == null).count()).isEqualTo(4); // all not linked yet
        assertThat(infos.get(0).dataLoggerSlaveRegisterInfos.stream().filter((x)-> x.availabilityDate == 0).count()).isEqualTo(4); // availability since Instant.Epoch
    }

    private Device mockDataLogger() {

        dataLoggerChannel1 = mockChannel(1L, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.empty());
        dataLoggerChannel2 = mockChannel(2L, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.empty());
        dataLoggerChannel3 = mockChannel(3L, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(BigDecimal.ONE));

        dataLoggerRegister1 = mockRegister(4L);
        dataLoggerRegister2 = mockRegister(5L);
        dataLoggerRegister3 = mockRegister(6L);
        dataLoggerRegister4 = mockRegister(7L);

        when(deviceType.getName()).thenReturn(DATA_LOGGER_DEVICE_TYPE_NAME);
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(dataLoggerChannel1, dataLoggerChannel2, dataLoggerChannel3));

        when(dataLogger.getId()).thenReturn(DATA_LOGGER_ID);
        when(dataLogger.getChannels()).thenReturn(Arrays.asList(dataLoggerChannel1, dataLoggerChannel2, dataLoggerChannel3));
        when(dataLogger.getRegisters()).thenReturn(Arrays.asList(dataLoggerRegister1, dataLoggerRegister2, dataLoggerRegister3, dataLoggerRegister4));
        when(dataLogger.getDeviceType()).thenReturn(deviceType);
        when(dataLogger.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(dataLogger.getVersion()).thenReturn(1L);

        when(deviceConfiguration.isDataloggerEnabled()).thenReturn(true);

        return dataLogger;
    }

    private Channel mockChannel(long channelId, String collectedReadingTypeMrid, String calculatedReadingTypeMrid, Optional<BigDecimal> multiplier) {
        when(loadProfile.getId()).thenReturn(1L);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileType.getName()).thenReturn("LoadProfileTypeName");

        ReadingType collectedReadingType = ReadingTypeMockBuilder.from(collectedReadingTypeMrid).getMock();
        ReadingType calculatedReadingType = ReadingTypeMockBuilder.from(calculatedReadingTypeMrid).getMock();
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getReadingType()).thenReturn(collectedReadingType);
        when(channelSpec.getOverflow()).thenReturn(Optional.empty());
        Channel channelWithBulkAndCalculatedDelta = mock(Channel.class);
        when(channelWithBulkAndCalculatedDelta.getId()).thenReturn(channelId);
        when(channelWithBulkAndCalculatedDelta.getChannelSpec()).thenReturn(channelSpec);
        when(channelWithBulkAndCalculatedDelta.getReadingType()).thenReturn(collectedReadingType);
        when(channelWithBulkAndCalculatedDelta.getCalculatedReadingType(any(Instant.class))).thenReturn(Optional.of(calculatedReadingType));
        when(channelWithBulkAndCalculatedDelta.getMultiplier(any(Instant.class))).thenReturn(multiplier);
        when(channelWithBulkAndCalculatedDelta.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channelWithBulkAndCalculatedDelta.getLastReading()).thenReturn(Optional.empty());
        when(channelWithBulkAndCalculatedDelta.getLastDateTime()).thenReturn(Optional.empty());
        when(channelWithBulkAndCalculatedDelta.getLoadProfile()).thenReturn(loadProfile);
        when(channelWithBulkAndCalculatedDelta.getOverflow()).thenReturn(Optional.empty());
        Unit collectedUnit = getUnit(collectedReadingType);
        when(channelWithBulkAndCalculatedDelta.getUnit()).thenReturn(collectedUnit);
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(channelWithBulkAndCalculatedDelta));
        when(channelWithBulkAndCalculatedDelta.getDevice()).thenReturn(dataLogger);
        return channelWithBulkAndCalculatedDelta;
    }

    private NumericalRegister mockRegister(long registerId){
        ReadingType collectedReadingType = ReadingTypeMockBuilder.from(collectedReadingTypeMrid).getMock();
        when(collectedReadingType.isCumulative()).thenReturn(false);

        when(registerSpec.getId()).thenReturn(registerId);
        when(registerSpec.getDeviceObisCode()).thenReturn(ObisCode.fromString("1.0.1.8.0.255"));
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.of(new BigDecimal(999999L)));
        when(registerSpec.getNumberOfFractionDigits()).thenReturn(0);
        when(registerSpec.isUseMultiplier()).thenReturn(false);

        when(registerType.getId()).thenReturn(REGISTER_TYPE_ID);
        when(registerType.getReadingType()).thenReturn(collectedReadingType);

        NumericalRegister register = mock(NumericalRegister.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getDevice()).thenReturn(dataLogger);
        when(register.getReadingType()).thenReturn(collectedReadingType);
        when(register.getLastReading()).thenReturn(Optional.empty());
        when(register.getDeviceObisCode()).thenReturn(ObisCode.fromString("1.0.1.8.0.255"));
        when(register.getOverflow()).thenReturn(Optional.empty());
        when(register.getLastReadingDate()).thenReturn(Optional.empty());
        when(register.getCalculatedReadingType(any(Instant.class))).thenReturn(Optional.empty());
        when(register.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());

        return register;
    }

    private Unit getUnit(ReadingType rt) {
        Unit unit = Unit.get(rt.getMultiplier().getSymbol() + rt.getUnit().getSymbol());
        if (unit == null) {
            unit = Unit.get(rt.getMultiplier().getSymbol() + rt.getUnit().getUnit().getAsciiSymbol());
        }
        return unit;
    }

}
