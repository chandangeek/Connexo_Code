package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareUpgradeOptionsResourceTest extends BaseFirmwareTest {
    @Mock
    private DeviceType deviceType;

    @Mock
    private FirmwareUpgradeOptions options;

    private ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER;
    private Set<ProtocolSupportedFirmwareOptions> set = new HashSet<>();
    private String URI = "devicetypes/1/firmwareupgradeoptions/1";

    @Before
    public void setUpStubs() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));


        set.addAll(Arrays.asList(protocolSupportedFirmwareOptions));
        when(firmwareService.getSupportedFirmwareOptionsFor(deviceType)).thenReturn(set);
        when(firmwareService.getAllowedFirmwareUpgradeOptionsFor(deviceType)).thenReturn(set);

        when(firmwareService.getFirmwareUpgradeOptions(deviceType)).thenReturn(options);

    }

    @Test
    public void testGetFirmwareUpgradeOptions() {
        when(firmwareService.getAllowedFirmwareUpgradeOptionsFor(deviceType)).thenReturn(new HashSet<>());
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(0);
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(false);
    }

    @Test
    public void testGetFirmwareUpgradeOptionsAllowed() {
        when(firmwareService.getAllowedFirmwareUpgradeOptionsFor(deviceType)).thenReturn(set);
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(true);
    }

    @Test
    public void testPutFirmwareUpgradeOptions() {
        FirmwareUpgradeOptionsInfo firmwareUpgradeOptionsInfo = new FirmwareUpgradeOptionsInfo();
        firmwareUpgradeOptionsInfo.isAllowed = false;

        target(URI).request().put(Entity.json(firmwareUpgradeOptionsInfo));

        verify(firmwareService).saveFirmwareUpgradeOptions(options);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UPGRADE_OPTIONS_REQUIRED + "}", property = "translationKey", strict = false)
    public void testPutFirmwareUpgradeOptionsException() {
        FirmwareUpgradeOptionsInfo firmwareUpgradeOptionsInfo = new FirmwareUpgradeOptionsInfo();
        firmwareUpgradeOptionsInfo.isAllowed = true;

        target(URI).request().put(Entity.json(firmwareUpgradeOptionsInfo));
    }
}
