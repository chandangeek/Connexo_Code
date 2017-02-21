/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceHelperTest extends DeviceDataRestApplicationJerseyTest {

    private final static String DATA_LOGGER_DEVICE_TYPE_NAME = "dataLogger.DeviceTypeName";
    private final static String DATA_LOGGER_DEVICE_CONFIGURATION_NAME = "dataLogger.ConfigurationName";
    private final static String SLAVE_DEVICE_TYPE_NAME = "slave.DeviceTypeName";
    private final static String SLAVE_DEVICE_CONFIGURATION_NAME = "slave.ConfigurationName";
    private final static String DATA_LOGGER_LOAD_PROFILE_TYPE_NAME = "dataLogger.LoadProfileName";
    private final static String SLAVE_DEVICE_LOAD_PROFILE_TYPE_NAME = "slave.LoadProfileName";

    private final static String READING_TYPE_1 = "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18";
    private final static String READING_TYPE_2 = "2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19";
    private final static String READING_TYPE_3 = "3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20";
    private final static String READING_TYPE_4 = "4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21";
    private final static String READING_TYPE_5 = "5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21.22";
    private final static String READING_TYPE_6 = "6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21.22.23";

    private DeviceConfiguration dataLoggerDeviceConfiguration, slaveDeviceConfiguration1, slaveDeviceConfiguration2;
    private DeviceType dataLoggerDeviceType, slaveDeviceType1, slaveDeviceType2;
    private Device dataLogger, slave1, slave2;

    public void setupMocks() {
        super.setupMocks();
        initDataLoggerTopology();
    }

    private void initDataLoggerTopology(){
        dataLoggerDeviceType = mock(DeviceType.class);
        when(dataLoggerDeviceType.getConfigurations()).thenReturn(Collections.singletonList(dataLoggerDeviceConfiguration));
        when(dataLoggerDeviceType.getName()).thenReturn(DATA_LOGGER_DEVICE_TYPE_NAME);
        when(dataLoggerDeviceType.isDataloggerSlave()).thenReturn(false);

        List<LoadProfileType> dataLoggerLPTypes = dataLoggerLoadProfileTypes();
        when(dataLoggerDeviceType.getLoadProfileTypes()).thenReturn(dataLoggerLPTypes);
        when(dataLoggerDeviceType.getRegisterTypes()).thenReturn(Collections.emptyList());

        dataLoggerDeviceConfiguration = mock(DeviceConfiguration.class);
        when(dataLoggerDeviceConfiguration.getDeviceType()).thenReturn(dataLoggerDeviceType);
        when(dataLoggerDeviceConfiguration.isDataloggerEnabled()).thenReturn(true);
        when(dataLoggerDeviceConfiguration.getName()).thenReturn(DATA_LOGGER_DEVICE_CONFIGURATION_NAME);

        slaveDeviceType1 = mock(DeviceType.class);
        when(slaveDeviceType1.getConfigurations()).thenReturn(Collections.singletonList(slaveDeviceConfiguration1));
        when(slaveDeviceType1.getName()).thenReturn(SLAVE_DEVICE_TYPE_NAME+".1");
        when(slaveDeviceType1.isDataloggerSlave()).thenReturn(true);

        slaveDeviceConfiguration1 = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration1.getDeviceType()).thenReturn(slaveDeviceType1);
        when(slaveDeviceConfiguration1.isDataloggerEnabled()).thenReturn(false);
        when(slaveDeviceConfiguration1.getName()).thenReturn(SLAVE_DEVICE_CONFIGURATION_NAME+".1");

        List<LoadProfileType> slave1LPTypes = dataLoggerSlave1ProfileTypes();
        when(slaveDeviceType1.getLoadProfileTypes()).thenReturn(slave1LPTypes);
        when(slaveDeviceType1.getRegisterTypes()).thenReturn(Collections.emptyList());

        slaveDeviceType2 = mock(DeviceType.class);
        when(slaveDeviceType2.getConfigurations()).thenReturn(Collections.singletonList(slaveDeviceConfiguration2));
        when(slaveDeviceType2.getName()).thenReturn(SLAVE_DEVICE_TYPE_NAME+".2");
        when(slaveDeviceType2.isDataloggerSlave()).thenReturn(true);

        slaveDeviceConfiguration2 = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration2.getDeviceType()).thenReturn(slaveDeviceType2);
        when(slaveDeviceConfiguration2.isDataloggerEnabled()).thenReturn(false);
        when(slaveDeviceConfiguration2.getName()).thenReturn(SLAVE_DEVICE_CONFIGURATION_NAME+".2");

        List<LoadProfileType> slave2LPTypes = dataLoggerSlave2ProfileTypes();
        when(slaveDeviceType2.getLoadProfileTypes()).thenReturn(slave2LPTypes);
        when(slaveDeviceType2.getRegisterTypes()).thenReturn(Collections.emptyList());

        Channel dataloggerC1 = mockChannel("1.0.1.8.0.255",READING_TYPE_1);
        Channel dataloggerC2 = mockChannel("1.0.2.8.0.255", READING_TYPE_2);
        Channel dataloggerC3 = mockChannel("1.0.1.8.1.255",READING_TYPE_3);
        Channel dataloggerC4 =mockChannel("1.0.1.8.2.255", READING_TYPE_4);
        Channel dataloggerC5 = mockChannel("1.0.2.8.1.255", READING_TYPE_5);
        Channel dataloggerC6 =  mockChannel("1.0.2.8.2.255", READING_TYPE_6);

        dataLogger = mock(Device.class);
        when(dataLogger.getDeviceConfiguration()).thenReturn(dataLoggerDeviceConfiguration);
        when(dataLogger.getName()).thenReturn("dataLogger.Name");
        when(dataLogger.getDeviceType()).thenReturn(dataLoggerDeviceType);
        when(dataLogger.getChannels()).thenReturn(Arrays.asList(dataloggerC1,dataloggerC2, dataloggerC3, dataloggerC4, dataloggerC5, dataloggerC6));

        Channel slave1C1 = mockChannel("1.0.1.8.0.255",READING_TYPE_1);
        Channel slave1C2 = mockChannel("1.0.1.8.1.255", READING_TYPE_3);
        Channel slave1C3 = mockChannel("1.0.1.8.2.255", READING_TYPE_4);

        slave1 = mock(Device.class);
        when(slave1.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration1);
        when(slave1.getName()).thenReturn("slave.Name1");
        when(slave1.getDeviceType()).thenReturn(slaveDeviceType1);
        when(slave1.getChannels()).thenReturn(Arrays.asList(slave1C1, slave1C2, slave1C3));

        Channel slave2C1 = mockChannel("1.0.2.8.0.255",READING_TYPE_4);
        Channel slave2C2 = mockChannel("1.0.1.8.1.255", READING_TYPE_5);
        Channel slave2C3 = mockChannel("1.0.1.8.2.255", READING_TYPE_6);

        slave2 = mock(Device.class);
        when(slave2.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration2);
        when(slave2.getName()).thenReturn("slave.Name2");
        when(slave2.getDeviceType()).thenReturn(slaveDeviceType2);
        when(slave1.getChannels()).thenReturn(Arrays.asList( slave2C1, slave2C2, slave2C3));

    }

    private List<LoadProfileType> dataLoggerLoadProfileTypes(){
        LoadProfileType lp1 = mock(LoadProfileType.class);
        when(lp1.getName()).thenReturn(DATA_LOGGER_LOAD_PROFILE_TYPE_NAME+".1");
        when(lp1.getObisCode()).thenReturn(ObisCode.fromString("1.0.99.1.0.255"));
        ChannelType ct1 = mockChannelType("1.0.1.8.0.255",READING_TYPE_1);
        ChannelType ct2 = mockChannelType("1.0.2.8.0.255", READING_TYPE_2);
        when(lp1.getChannelTypes()).thenReturn(Arrays.asList(ct1, ct2));

        ChannelType ct3= mockChannelType("1.0.1.8.1.255",READING_TYPE_3);
        ChannelType ct4 =  mockChannelType("1.0.1.8.2.255", READING_TYPE_4);
        ChannelType ct5 = mockChannelType("1.0.2.8.1.255", READING_TYPE_5);
        ChannelType ct6 = mockChannelType("1.0.2.8.2.255", READING_TYPE_6);
        LoadProfileType lp2 = mock(LoadProfileType.class);
        when(lp2.getName()).thenReturn(DATA_LOGGER_LOAD_PROFILE_TYPE_NAME+".2");
        when(lp2.getObisCode()).thenReturn(ObisCode.fromString("1.0.99.2.0.255"));
        when(lp2.getChannelTypes()).thenReturn(Arrays.asList(ct3, ct4,ct5,ct6));
        return Arrays.asList(lp1,lp2);
    }

    private List<LoadProfileType> dataLoggerSlave1ProfileTypes(){
        ChannelType ct1 = mockChannelType("1.0.1.8.0.255",READING_TYPE_1);
        ChannelType ct2 =mockChannelType("1.0.1.8.1.255", READING_TYPE_3);
        ChannelType ct3 = mockChannelType("1.0.1.8.2.255", READING_TYPE_4);

        LoadProfileType lp = mock(LoadProfileType.class);

        when(lp.getName()).thenReturn(SLAVE_DEVICE_LOAD_PROFILE_TYPE_NAME+".1");
        when(lp.getObisCode()).thenReturn(ObisCode.fromString("1.0.90.1.0.255"));
        when(lp.getChannelTypes()).thenReturn(Arrays.asList(ct1, ct2, ct3));
        return Collections.singletonList(lp);
    }

    private List<LoadProfileType> dataLoggerSlave2ProfileTypes(){
        LoadProfileType lp = mock(LoadProfileType.class);
        when(lp.getName()).thenReturn(SLAVE_DEVICE_LOAD_PROFILE_TYPE_NAME+".2");
        when(lp.getObisCode()).thenReturn(ObisCode.fromString("1.0.90.2.0.255"));
        ChannelType ct1 = mockChannelType("1.0.2.8.0.255",READING_TYPE_2);
        ChannelType ct2 = mockChannelType("1.0.2.8.1.255", READING_TYPE_5);
        ChannelType ct3 = mockChannelType("1.0.2.8.2.255", READING_TYPE_6);
        when(lp.getChannelTypes()).thenReturn(Arrays.asList(ct1, ct2, ct3));
        return Collections.singletonList(lp);
    }

    private ChannelType mockChannelType(String obisCode, String readingType){
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channelType.getObisCode()).thenReturn(ObisCode.fromString(obisCode));
        ReadingType rt = mockReadingType(readingType);
        when(channelType.getReadingType()).thenReturn(rt);
        return channelType;
    }

    private Channel mockChannel(String obisCode, String readingType){
        Channel channel = mock(Channel.class);
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channel.getObisCode()).thenReturn(ObisCode.fromString(obisCode));
        ReadingType rt = mockReadingType(readingType);
        when(channel.getReadingType()).thenReturn(rt);
        return channel;
    }


}
