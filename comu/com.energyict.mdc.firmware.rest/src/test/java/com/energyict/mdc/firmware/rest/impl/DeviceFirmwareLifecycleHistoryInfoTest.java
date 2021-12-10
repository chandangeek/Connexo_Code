/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceFirmwareLifecycleHistoryInfoTest extends BaseFirmwareTest {
    @Mock
    private DeviceMessage deviceMessage;
    @Mock
    private FirmwareManagementDeviceUtils firmwareManagementDeviceUtils;
    @Mock
    private FirmwareVersion firmwareVersionFromDevMessage;
    @Mock
    private ComTask firmwareComTask;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testDeviceFirmwareLifecycleHistoryInfoIsCreated() {
        String userName = "root";
        String firmwareVersion = "1.0";
        Instant currentTimestamp = Instant.now();
        Instant plannedTimestamp = currentTimestamp.minusSeconds(60);
        Instant activationTimestamp = currentTimestamp.plusSeconds(300);
        Long firmwareComTaskId = 1L;
        String resultMessage = "Sent";

        when(thesaurus.getFormat(any(DeviceMessageStatusTranslationKeys.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenReturn(((DeviceMessageStatusTranslationKeys) invocation.getArguments()[0]).getDefaultFormat());
            return messageFormat;
        });

        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.SENT);
        when(firmwareComTask.getId()).thenReturn(firmwareComTaskId);
        when(firmwareVersionFromDevMessage.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(deviceMessage.getReleaseDate()).thenReturn(plannedTimestamp);
        when(deviceMessage.getModTime()).thenReturn(currentTimestamp);
        when(deviceMessage.getUser()).thenReturn(userName);
        when(firmwareManagementDeviceUtils.getFirmwareVersionFromMessage(deviceMessage)).thenReturn(Optional.of(firmwareVersionFromDevMessage));
        when(firmwareManagementDeviceUtils.getActivationDateFromMessage(deviceMessage)).thenReturn(Optional.of(activationTimestamp));
        when(firmwareManagementDeviceUtils.getFirmwareTask()).thenReturn(Optional.of(firmwareComTask));

        DeviceFirmwareLifecycleHistoryInfo deviceFirmwareLifecycleHistoryInfo = new DeviceFirmwareLifecycleHistoryInfo(deviceMessage, firmwareManagementDeviceUtils, thesaurus);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getTriggeredBy()).isEqualTo(userName);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getFirmwareVersion()).isEqualTo(firmwareVersion);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getPlannedDate()).isEqualTo(plannedTimestamp);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getUploadedDate()).isEqualTo(currentTimestamp);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getActivationDate()).isEqualTo(activationTimestamp);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getFirmwareTaskId()).isEqualTo(firmwareComTaskId);
        assertThat(deviceFirmwareLifecycleHistoryInfo.getResult()).isEqualTo(resultMessage);
    }
}
