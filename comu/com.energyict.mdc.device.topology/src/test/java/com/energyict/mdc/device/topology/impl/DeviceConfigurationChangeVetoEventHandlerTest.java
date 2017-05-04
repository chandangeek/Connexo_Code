package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;

import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Copyrights EnergyICT
 * Date: 4/04/2017
 * Time: 15:05
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationChangeVetoEventHandlerTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerTopologyService topologyService;
    @Mock
    private Clock clock;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private Device device, someSlaveDevice;
    @Mock
    private Channel channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9, channel10, channel11, channel12,
                    channel13, channel14, channel15, channel16, channel17, channel18, channel19, channel20, channel21, channel22, channel23, channel24,
                    channel25, channel26, channel27, channel28, channel29, channel30, channel31, channel32;
    @Mock
    private Channel linkedChannel1, linkedChannel2, linkedChannel3, linkedChannel4, linkedChannel5, linkedChannel6;
    @Mock
    private ChannelSpec newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec, newConfigChannel5Spec, newConfigCchannel6Spec,
                   newConfigChannelSpec7, newConfigChannelSpec8, newConfigChannelSpec9, newConfigChannelSpec10, newConfigChannelSpec11, newConfigChannelSpec12;
    @Mock
    private DeviceType deviceType, dataLoggerSlaveDeviceType, multiElementSlaveDeviceType;
    @Mock
    private DeviceConfiguration currentDeviceConfig, newDeviceConfig;
    @Mock
    private Query<EndDevice> endDeviceQuery;

    @Before
    public void initializeMocks() {
        NlsMessageFormat nlsFormat = mock(NlsMessageFormat.class);

        when(nlsService.getThesaurus(any(), any())).thenReturn(thesaurus);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsFormat);

        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(currentDeviceConfig);
        when(device.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9, channel10, channel11, channel12,
                            channel13, channel14, channel15, channel16, channel17, channel18, channel19, channel20, channel21, channel22, channel23, channel24,
                            channel25, channel26, channel27, channel28, channel29, channel30, channel31, channel32));
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(deviceType.isMultiElementSlave()).thenReturn(false);

        when(linkedChannel1.getDevice()).thenReturn(someSlaveDevice);
        when(linkedChannel2.getDevice()).thenReturn(someSlaveDevice);
        when(linkedChannel3.getDevice()).thenReturn(someSlaveDevice);
        when(linkedChannel4.getDevice()).thenReturn(someSlaveDevice);
        when(linkedChannel5.getDevice()).thenReturn(someSlaveDevice);
        when(linkedChannel6.getDevice()).thenReturn(someSlaveDevice);

        when(currentDeviceConfig.getDeviceType()).thenReturn(deviceType);
        when(newDeviceConfig.getDeviceType()).thenReturn(deviceType);
        when(this.localEvent.getSource()).thenReturn(Pair.of(device, newDeviceConfig));
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(deviceType.isMultiElementSlave()).thenReturn(false);
        when(dataLoggerSlaveDeviceType.isDataloggerSlave()).thenReturn(true);
        when(dataLoggerSlaveDeviceType.isMultiElementSlave()).thenReturn(false);
        when(multiElementSlaveDeviceType.isDataloggerSlave()).thenReturn(false);
        when(multiElementSlaveDeviceType.isMultiElementSlave()).thenReturn(true);
    }

    @Test
    public void deviceConfigChangeForDataLoggerSlave(){
        when(deviceType.isDataloggerSlave()).thenReturn(true);
        when(deviceType.isMultiElementSlave()).thenReturn(false);
        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);

        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }
    @Test
    public void deviceConfigChangeForMultiElementSlave(){
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(deviceType.isMultiElementSlave()).thenReturn(true);
        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test
    public void deviceConfigChangeWithoutLinkedChannels(){
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test
    public void deviceConfigChangeWithoutLinkedChannelsWithMultiElementEnabledConfig(){
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test
    public void deviceConfigChangeWithLinkedChannelsAndEnoughAvialableChannels(){
        when(someSlaveDevice.getDeviceType()).thenReturn(dataLoggerSlaveDeviceType);
        when(newDeviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.of(linkedChannel1));
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.of(linkedChannel2));
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.of(linkedChannel3));
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.of(linkedChannel4));
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.of(linkedChannel5));
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.of(linkedChannel6));
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());
        when(newDeviceConfig.getChannelSpecs()).thenReturn(Arrays.asList(newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec, newConfigChannel5Spec, newConfigCchannel6Spec,
                           newConfigChannelSpec7, newConfigChannelSpec8, newConfigChannelSpec9, newConfigChannelSpec10, newConfigChannelSpec11, newConfigChannelSpec12));

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test
    public void deviceConfigChangeWithLinkedChannelsAndEnoughAvialableChannelsForMultiElement(){
        when(someSlaveDevice.getDeviceType()).thenReturn(multiElementSlaveDeviceType);
        when(newDeviceConfig.isMultiElementEnabled()).thenReturn(true);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.of(linkedChannel1));
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.of(linkedChannel2));
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.of(linkedChannel3));
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.of(linkedChannel4));
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.of(linkedChannel5));
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.of(linkedChannel6));
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());
        when(newDeviceConfig.getChannelSpecs()).thenReturn(Arrays.asList(newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec, newConfigChannel5Spec, newConfigCchannel6Spec,
                           newConfigChannelSpec7, newConfigChannelSpec8, newConfigChannelSpec9, newConfigChannelSpec10, newConfigChannelSpec11, newConfigChannelSpec12));

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test(expected=DeviceConfigurationChangeException.class)
    public void deviceConfigToLessChannels(){
        when(someSlaveDevice.getDeviceType()).thenReturn(dataLoggerSlaveDeviceType);
        when(newDeviceConfig.isDataloggerEnabled()).thenReturn(true);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.of(linkedChannel1));
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.of(linkedChannel2));
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.of(linkedChannel3));
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.of(linkedChannel4));
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.of(linkedChannel5));
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.of(linkedChannel6));
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());
        when(newDeviceConfig.getChannelSpecs()).thenReturn(Arrays.asList(newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec));

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test(expected=DeviceConfigurationChangeException.class)
    public void deviceConfigToLessChannelsMultiElement(){
        when(someSlaveDevice.getDeviceType()).thenReturn(multiElementSlaveDeviceType);
        when(newDeviceConfig.isMultiElementEnabled()).thenReturn(true);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.of(linkedChannel1));
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.of(linkedChannel2));
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.of(linkedChannel3));
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.of(linkedChannel4));
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.of(linkedChannel5));
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.of(linkedChannel6));
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());
        when(newDeviceConfig.getChannelSpecs()).thenReturn(Arrays.asList(newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec));

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test(expected=DeviceConfigurationChangeException.class)
    public void deviceConfigChangeButCurrentDeviceLinkedToDataLoggerSlaves(){
        when(someSlaveDevice.getDeviceType()).thenReturn(dataLoggerSlaveDeviceType);
        when(newDeviceConfig.isMultiElementEnabled()).thenReturn(true);
        when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.of(linkedChannel1));
        when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.of(linkedChannel2));
        when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.of(linkedChannel3));
        when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.of(linkedChannel4));
        when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.of(linkedChannel5));
        when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.of(linkedChannel6));
        when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());
        when(newDeviceConfig.getChannelSpecs()).thenReturn(Arrays.asList(newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec, newConfigChannel5Spec, newConfigCchannel6Spec,
                           newConfigChannelSpec7, newConfigChannelSpec8, newConfigChannelSpec9, newConfigChannelSpec10, newConfigChannelSpec11, newConfigChannelSpec12));

        DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

        // Business method
        handler.handle(this.localEvent);
        verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
    }

    @Test(expected=DeviceConfigurationChangeException.class)
      public void deviceConfigChangeButCurrentDeviceLinkedToMultiElementSlaves(){
          when(someSlaveDevice.getDeviceType()).thenReturn(multiElementSlaveDeviceType);
          when(newDeviceConfig.isDataloggerEnabled()).thenReturn(true);
          when(topologyService.getSlaveChannel(channel1)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel2)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel3)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel4)).thenReturn(Optional.of(linkedChannel1));
          when(topologyService.getSlaveChannel(channel5)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel6)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel7)).thenReturn(Optional.of(linkedChannel2));
          when(topologyService.getSlaveChannel(channel8)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel9)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel10)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel11)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel12)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel13)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel14)).thenReturn(Optional.of(linkedChannel3));
          when(topologyService.getSlaveChannel(channel15)).thenReturn(Optional.of(linkedChannel4));
          when(topologyService.getSlaveChannel(channel16)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel17)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel18)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel19)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel20)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel21)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel22)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel23)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel24)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel25)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel26)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel27)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel28)).thenReturn(Optional.of(linkedChannel5));
          when(topologyService.getSlaveChannel(channel29)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel30)).thenReturn(Optional.empty());
          when(topologyService.getSlaveChannel(channel31)).thenReturn(Optional.of(linkedChannel6));
          when(topologyService.getSlaveChannel(channel32)).thenReturn(Optional.empty());
          when(newDeviceConfig.getChannelSpecs()).thenReturn(Arrays.asList(newConfigChannel1Spec, newConfigChannel2Spec, newConfigChannel3Spec, newConfigChannel4Spec, newConfigChannel5Spec, newConfigCchannel6Spec,
                             newConfigChannelSpec7, newConfigChannelSpec8, newConfigChannelSpec9, newConfigChannelSpec10, newConfigChannelSpec11, newConfigChannelSpec12));

          DeviceConfigurationChangeVetoEventHandler handler = spy(getTestInstance());

          // Business method
          handler.handle(this.localEvent);
          verify(handler, times(1)).validateEnoughChannelsOnTargetConfiguration(device, newDeviceConfig);
      }




    private DeviceConfigurationChangeVetoEventHandler getTestInstance() {
        return new DeviceConfigurationChangeVetoEventHandler(this.topologyService, this.nlsService);
    }


}
