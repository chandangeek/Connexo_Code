/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
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
    private FirmwareVersion firmwareVersionFromDevmessage;
    @Mock
    private ComTask firmwareComTask;
    @Mock
    private Thesaurus thesaurus;


    @Test
    public void testDeviceFirmwareLifecycleHistoryInfoIsCreated() {
        String userName = "root";
        String firmwareVersion = "1.0";
        Instant currentTimestamp = Instant.now();
        Long firmwareComTaskId = 1L;
        String resultMessage = "Revoked";

        when(thesaurus.getFormat(any(DeviceMessageStatusTranslationKeys.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenReturn(((DeviceMessageStatusTranslationKeys) invocation.getArguments()[0]).getDefaultFormat());
            return messageFormat;
        });

        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CANCELED);
        when(firmwareComTask.getId()).thenReturn(firmwareComTaskId);
        when(firmwareVersionFromDevmessage.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(deviceMessage.getReleaseDate()).thenReturn(currentTimestamp);
        when(deviceMessage.getUser()).thenReturn(userName);
        when(firmwareManagementDeviceUtils.getFirmwareVersionFromMessage(deviceMessage)).thenReturn(Optional.of(firmwareVersionFromDevmessage));
        when(firmwareManagementDeviceUtils.getActivationDateFromMessage(deviceMessage)).thenReturn(Optional.of(currentTimestamp));
        when(firmwareManagementDeviceUtils.getFirmwareTask()).thenReturn(Optional.of(firmwareComTask));

        DeviceFirmwareLifecycleHistoryInfo deviceFirmwareLifecycleHistoryInfo = new DeviceFirmwareLifecycleHistoryInfo(deviceMessage, firmwareManagementDeviceUtils, thesaurus);
        assertEquals(userName, deviceFirmwareLifecycleHistoryInfo.getTriggerdBy());
        assertEquals(firmwareVersion, deviceFirmwareLifecycleHistoryInfo.getFirmwareVersion());
        assertEquals(currentTimestamp, deviceFirmwareLifecycleHistoryInfo.getActivationDate());
        assertEquals(currentTimestamp, deviceFirmwareLifecycleHistoryInfo.getUploadedOn());
        assertEquals(firmwareComTaskId, deviceFirmwareLifecycleHistoryInfo.getFirmwareTaskId());
        assertEquals(resultMessage, deviceFirmwareLifecycleHistoryInfo.getResult());
    }
}
