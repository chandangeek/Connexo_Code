/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFirmwareCheckTest {
    private final FirmwareCheckManagementOption checkOption;
    private final Class<? extends FirmwareCheck> checkClass;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected FirmwareCheck firmwareCheck;
    protected Thesaurus thesaurus = NlsModule.SimpleThesaurus.from(new FirmwareServiceImpl().getKeys());
    @Mock
    protected FirmwareServiceImpl firmwareService;
    @Mock
    protected TopologyService topologyService;
    @Mock
    protected FirmwareManagementDeviceUtils firmwareManagementDeviceUtils;
    @Mock
    protected Device device;
    @Mock
    protected DeviceType deviceType;
    @Mock
    protected FirmwareCampaignManagementOptions firmwareCampaignManagementOptions;
    @Mock
    protected FirmwareManagementOptions firmwareManagementOptions;
    @Mock
    protected FirmwareVersion uploadedFirmware, activeMeterFirmware, activeCommunicationFirmware, activeAuxiliaryFirmware;
    @Mock
    protected DataModel dataModel;
    @Mock
    protected ActivatedFirmwareVersion activatedMeterFirmware, activatedCommunicationFirmware, activatedAuxiliaryFirmware;

    protected AbstractFirmwareCheckTest(FirmwareCheckManagementOption checkOption, Class<? extends FirmwareCheck> checkClass) {
        this.checkOption = checkOption;
        this.checkClass = checkClass;
    }

    @Before
    public void setUp() {
        when(firmwareManagementDeviceUtils.getDevice()).thenReturn(device);
        when(firmwareManagementDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(true);
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.METER)).thenReturn(Optional.of(activatedMeterFirmware));
        when(activatedMeterFirmware.getFirmwareVersion()).thenReturn(activeMeterFirmware);
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.COMMUNICATION)).thenReturn(Optional.of(activatedCommunicationFirmware));
        when(activatedCommunicationFirmware.getFirmwareVersion()).thenReturn(activeCommunicationFirmware);
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.AUXILIARY)).thenReturn(Optional.of(activatedAuxiliaryFirmware));
        when(activatedAuxiliaryFirmware.getFirmwareVersion()).thenReturn(activeAuxiliaryFirmware);
        when(uploadedFirmware.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(uploadedFirmware.getMeterFirmwareDependency()).thenReturn(Optional.empty());
        when(uploadedFirmware.getCommunicationFirmwareDependency()).thenReturn(Optional.empty());
        when(uploadedFirmware.getAuxiliaryFirmwareDependency()).thenReturn(Optional.empty());
        when(uploadedFirmware.getRank()).thenReturn(10);
        when(activeMeterFirmware.getRank()).thenReturn(1);
        when(activeCommunicationFirmware.getRank()).thenReturn(2);
        when(activeAuxiliaryFirmware.getRank()).thenReturn(3);
        when(uploadedFirmware.getDeviceType()).thenReturn(deviceType);
        when(uploadedFirmware.compareTo(any(FirmwareVersion.class))).thenAnswer(invocation -> FirmwareVersion.compare(uploadedFirmware, invocation.getArgumentAt(0, FirmwareVersion.class)));
        when(activeMeterFirmware.getDeviceType()).thenReturn(deviceType);
        when(activeMeterFirmware.compareTo(any(FirmwareVersion.class))).thenAnswer(invocation -> FirmwareVersion.compare(activeMeterFirmware, invocation.getArgumentAt(0, FirmwareVersion.class)));
        when(activeCommunicationFirmware.getDeviceType()).thenReturn(deviceType);
        when(activeCommunicationFirmware.compareTo(any(FirmwareVersion.class))).thenAnswer(invocation -> FirmwareVersion.compare(activeCommunicationFirmware, invocation.getArgumentAt(0, FirmwareVersion.class)));
        when(activeAuxiliaryFirmware.getDeviceType()).thenReturn(deviceType);
        when(activeAuxiliaryFirmware.compareTo(any(FirmwareVersion.class))).thenAnswer(invocation -> FirmwareVersion.compare(activeAuxiliaryFirmware, invocation.getArgumentAt(0, FirmwareVersion.class)));

        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(device.getDeviceType()).thenReturn(deviceType);
        when(firmwareService.isFirmwareCheckActivated(deviceType, checkOption)).thenReturn(true);
        when(firmwareService.findFirmwareManagementOptions(deviceType)).thenReturn(Optional.of(firmwareManagementOptions));
        when(firmwareManagementOptions.isActivated(checkOption)).thenReturn(true);
        when(firmwareManagementOptions.getStatuses(checkOption)).thenReturn(EnumSet.of(FirmwareStatus.TEST, FirmwareStatus.FINAL));
        when(firmwareCampaignManagementOptions.isActivated(checkOption)).thenReturn(true);
        when(firmwareCampaignManagementOptions.getStatuses(checkOption)).thenReturn(EnumSet.of(FirmwareStatus.TEST, FirmwareStatus.FINAL));
        Injector injector = Guice.createInjector(getModule());
        firmwareCheck = injector.getInstance(checkClass);
    }

    protected void expectError(String message) {
        expectedException.expect(FirmwareCheck.FirmwareCheckException.class);
        expectedException.expectMessage(message);
        firmwareCheck.execute(firmwareCampaignManagementOptions, firmwareManagementDeviceUtils, uploadedFirmware);
    }

    protected void expectSuccess() {
        try {
            firmwareCheck.execute(firmwareCampaignManagementOptions, firmwareManagementDeviceUtils, uploadedFirmware);
        } catch (Throwable throwable) {
            throw new AssertionError("Unexpected exception during call of firmwareCheck.execute(firmwareManagementDeviceUtils, uploadedFirmware) : "
                    + System.lineSeparator()
                    + throwable.toString(), throwable);
        }
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(FirmwareServiceImpl.class).toInstance(firmwareService);
                bind(FirmwareService.class).toInstance(firmwareService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(TopologyService.class).toInstance(topologyService);
            }
        };
    }
}
