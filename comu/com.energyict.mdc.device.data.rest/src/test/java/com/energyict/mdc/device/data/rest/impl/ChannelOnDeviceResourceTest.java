package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelOnDeviceResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String DEIVICE_MRID = "mrid1";
    private static final Instant NOW = Instant.ofEpochMilli(1410786205000L);

    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;

    @Before
    public void setupStubs(){
        when(deviceService.findByUniqueMrid(DEIVICE_MRID)).thenReturn(device);
        when(clock.instant()).thenReturn(NOW);
        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfile loadProfile2 = mock(LoadProfile.class);
        when(loadProfile1.getId()).thenReturn(1L);
        when(loadProfile2.getId()).thenReturn(2L);
        LoadProfileSpec lpSpec1 = mock(LoadProfileSpec.class);
        LoadProfileSpec lpSpec2 = mock(LoadProfileSpec.class);
        LoadProfileType lpType1 = mock(LoadProfileType.class);
        LoadProfileType lpType2 = mock(LoadProfileType.class);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(lpSpec1);
        when(loadProfile2.getLoadProfileSpec()).thenReturn(lpSpec2);
        when(lpSpec1.getLoadProfileType()).thenReturn(lpType1);
        when(lpSpec2.getLoadProfileType()).thenReturn(lpType2);
        when(lpType1.getName()).thenReturn("Load Profile Name 1");
        when(lpType2.getName()).thenReturn("Active Energy Import");
        Channel channel1 = mockChannel(1);
        Channel channel2 = mockChannel(2);
        Channel channel3 = mockChannel(3);
        Channel channel4 = mockChannel(4, "A special name");
        when(loadProfile1.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(loadProfile2.getChannels()).thenReturn(Arrays.asList(channel3, channel4));
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(device.getChannels()).thenReturn(Arrays.asList(channel1,channel2,channel3,channel4));
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(device.forValidation()).thenReturn(deviceValidation);
        doReturn(false).when(deviceValidation).isValidationActive(any(Channel.class), eq(NOW));
        when(deviceValidation.getLastChecked()).thenReturn(Optional.<Instant>empty());
    }

    private Channel mockChannel(long id){
        return this.mockChannel(id, "Channel " + id);
    }

    private Channel mockChannel(long id, String name){
        Channel channel = mock(Channel.class, RETURNS_DEEP_STUBS);
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channel.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(channel.getName()).thenReturn(name);
        when(channel.getId()).thenReturn(id);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getReadingType()).thenReturn(DeviceResourceTest.READING_TYPE);
        return channel;
    }

    @Test
    public void testGetAllChannelsOnDevice(){
        String json = target("devices/" + DEIVICE_MRID + "/channels")
                .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);
    }
}
