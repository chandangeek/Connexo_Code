package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.tests.Matcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 2/05/2016
 * Time: 14:03
 */
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

    public void getDataLoggerSlavesTest(){
//        public void findPhysicalConnectedDevicesOnDataLoggerTest() {
//
//
//            // Business method
//            topologyService.setDataLogger(slave1, dataLogger, );
//            topologyService.setDataLogger(slave2, dataLogger, Collections.emptyMap());
//
//            // Business method
//            List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(dataLogger);
//
//            // Asserts
//            assertThat(downstreamDevices).hasSize(0);
//        }
    }

    private void initDataLoggerTopology(){
        dataLoggerDeviceType = mock(DeviceType.class);
        when(dataLoggerDeviceType.getConfigurations()).thenReturn(Collections.singletonList(dataLoggerDeviceConfiguration));
        when(dataLoggerDeviceType.getName()).thenReturn(DATA_LOGGER_DEVICE_TYPE_NAME);
        when(dataLoggerDeviceType.isDataloggerSlave()).thenReturn(false);

        when(dataLoggerDeviceType.getLoadProfileTypes()).thenReturn(dataLoggerLoadProfileTypes());
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

        when(slaveDeviceType1.getLoadProfileTypes()).thenReturn(dataLoggerSlave1ProfileTypes());
        when(slaveDeviceType1.getRegisterTypes()).thenReturn(Collections.emptyList());

        slaveDeviceType2 = mock(DeviceType.class);
        when(slaveDeviceType2.getConfigurations()).thenReturn(Collections.singletonList(slaveDeviceConfiguration2));
        when(slaveDeviceType2.getName()).thenReturn(SLAVE_DEVICE_TYPE_NAME+".2");
        when(slaveDeviceType2.isDataloggerSlave()).thenReturn(true);

        slaveDeviceConfiguration2 = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration2.getDeviceType()).thenReturn(slaveDeviceType2);
        when(slaveDeviceConfiguration2.isDataloggerEnabled()).thenReturn(false);
        when(slaveDeviceConfiguration2.getName()).thenReturn(SLAVE_DEVICE_CONFIGURATION_NAME+".2");

        when(slaveDeviceType2.getLoadProfileTypes()).thenReturn(dataLoggerSlave2ProfileTypes());
        when(slaveDeviceType2.getRegisterTypes()).thenReturn(Collections.emptyList());

        dataLogger = mock(Device.class);
        when(dataLogger.getDeviceConfiguration()).thenReturn(dataLoggerDeviceConfiguration);
        when(dataLogger.getName()).thenReturn("dataLogger.MRID");
        when(dataLogger.getDeviceType()).thenReturn(dataLoggerDeviceType);
        when(dataLogger.getChannels()).thenReturn(Arrays.asList(mockChannel("1.0.1.8.0.255",READING_TYPE_1),
                mockChannel("1.0.2.8.0.255", READING_TYPE_2),mockChannel("1.0.1.8.1.255",READING_TYPE_3),
                        mockChannel("1.0.1.8.2.255", READING_TYPE_4),mockChannel("1.0.2.8.1.255", READING_TYPE_5),
                        mockChannel("1.0.2.8.2.255", READING_TYPE_6)));

        slave1 = mock(Device.class);
        when(slave1.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration1);
        when(slave1.getName()).thenReturn("slave.MRID1");
        when(slave1.getDeviceType()).thenReturn(slaveDeviceType1);
        when(slave1.getChannels()).thenReturn(Arrays.asList(mockChannel("1.0.1.8.0.255",READING_TYPE_1),
                mockChannel("1.0.1.8.1.255", READING_TYPE_3), mockChannel("1.0.1.8.2.255", READING_TYPE_4)));

        slave2 = mock(Device.class);
        when(slave2.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration2);
        when(slave2.getName()).thenReturn("slave.MRID2");
        when(slave2.getDeviceType()).thenReturn(slaveDeviceType2);
        when(slave1.getChannels()).thenReturn(Arrays.asList(mockChannel("1.0.2.8.0.255",READING_TYPE_4),
                mockChannel("1.0.1.8.1.255", READING_TYPE_5), mockChannel("1.0.1.8.2.255", READING_TYPE_6)));

    }

    private List<LoadProfileType> dataLoggerLoadProfileTypes(){
        LoadProfileType lp1 = mock(LoadProfileType.class);
        when(lp1.getName()).thenReturn(DATA_LOGGER_LOAD_PROFILE_TYPE_NAME+".1");
        when(lp1.getObisCode()).thenReturn(ObisCode.fromString("1.0.99.1.0.255"));
        when(lp1.getChannelTypes()).thenReturn(Arrays.asList(mockChannelType("1.0.1.8.0.255",READING_TYPE_1),
                mockChannelType("1.0.2.8.0.255", READING_TYPE_2)));

        LoadProfileType lp2 = mock(LoadProfileType.class);
        when(lp2.getName()).thenReturn(DATA_LOGGER_LOAD_PROFILE_TYPE_NAME+".2");
        when(lp2.getObisCode()).thenReturn(ObisCode.fromString("1.0.99.2.0.255"));
        when(lp2.getChannelTypes()).thenReturn(Arrays.asList(mockChannelType("1.0.1.8.1.255",READING_TYPE_3),
                        mockChannelType("1.0.1.8.2.255", READING_TYPE_4),mockChannelType("1.0.2.8.1.255", READING_TYPE_5),
                        mockChannelType("1.0.2.8.2.255", READING_TYPE_6)));
        return Arrays.asList(lp1,lp2);
    }

    private List<LoadProfileType> dataLoggerSlave1ProfileTypes(){
        LoadProfileType lp = mock(LoadProfileType.class);
        when(lp.getName()).thenReturn(SLAVE_DEVICE_LOAD_PROFILE_TYPE_NAME+".1");
        when(lp.getObisCode()).thenReturn(ObisCode.fromString("1.0.90.1.0.255"));
        when(lp.getChannelTypes()).thenReturn(Arrays.asList(mockChannelType("1.0.1.8.0.255",READING_TYPE_1),
                mockChannelType("1.0.1.8.1.255", READING_TYPE_3), mockChannelType("1.0.1.8.2.255", READING_TYPE_4)));
        return Collections.singletonList(lp);
    }

    private List<LoadProfileType> dataLoggerSlave2ProfileTypes(){
        LoadProfileType lp = mock(LoadProfileType.class);
        when(lp.getName()).thenReturn(SLAVE_DEVICE_LOAD_PROFILE_TYPE_NAME+".2");
        when(lp.getObisCode()).thenReturn(ObisCode.fromString("1.0.90.2.0.255"));
        when(lp.getChannelTypes()).thenReturn(Arrays.asList(mockChannelType("1.0.2.8.0.255",READING_TYPE_2),
                mockChannelType("1.0.2.8.1.255", READING_TYPE_5), mockChannelType("1.0.2.8.2.255", READING_TYPE_6)));
        return Collections.singletonList(lp);
    }

    private ChannelType mockChannelType(String obisCode, String readingType){
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channelType.getObisCode()).thenReturn(ObisCode.fromString(obisCode));
        when(channelType.getReadingType()).thenReturn(mockReadingType(readingType));
        return channelType;
    }

    private Channel mockChannel(String obisCode, String readingType){
        Channel channel = mock(Channel.class);
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channel.getObisCode()).thenReturn(ObisCode.fromString(obisCode));
        when(channel.getReadingType()).thenReturn(mockReadingType(readingType));
        return channel;
    }


}
