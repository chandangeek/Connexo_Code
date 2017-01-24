package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 5/04/2016
 * Time: 14:20
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceInFirmwareCampaignInfoTest {

    @Mock
    Thesaurus thesaurus;
    @Mock
    private DeviceInFirmwareCampaign deviceInFirmwareCampaign;
    @Mock
    private FirmwareCampaign firmwareCampaign;
    @Mock
    private Device device;

    private Instant startedOn = Instant.ofEpochMilli(459859514);
    private Instant finishedOn = Instant.ofEpochMilli(1462451511);

    @Before
    public void initMock(){
        when(thesaurus.getString(anyString(), anyString())).thenReturn("someTranslatedFirmwareManagementDeviceStatus");

        when(device.getName()).thenReturn("NameOfDevice");
        when(firmwareCampaign.getId()).thenReturn(6598L);
        when(deviceInFirmwareCampaign.getDevice()).thenReturn(device);
        when(deviceInFirmwareCampaign.getFirmwareCampaign()).thenReturn(firmwareCampaign);
        when(deviceInFirmwareCampaign.getStatus()).thenReturn(FirmwareManagementDeviceStatus.UPLOAD_PENDING);
        when(deviceInFirmwareCampaign.getStartedOn()).thenReturn(startedOn);
        when(deviceInFirmwareCampaign.getFinishedOn()).thenReturn(finishedOn);
    }
    @Test
    public void testDeviceInFirmwareCampaignInfoConstructor(){
        DeviceInFirmwareCampaignInfo info = new DeviceInFirmwareCampaignInfo(deviceInFirmwareCampaign, thesaurus);

        assertThat(info.campaignId).isEqualTo(6598L);
        assertThat(info.deviceName).isEqualTo("NameOfDevice");
        assertThat(info.status.id).isEqualTo(FirmwareManagementDeviceStatus.UPLOAD_PENDING.key());
        assertThat(info.status.name).isEqualTo("someTranslatedFirmwareManagementDeviceStatus");
        assertThat(info.startedOn).isEqualTo(startedOn);
        assertThat(info.finishedOn).isEqualTo(finishedOn);
    };

}
