/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.data.Device;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class MasterDataLinkageHandlerTest extends AbstractMasterDataLinkageTest {
    @Rule
    public TestRule frosty = Using.timeZoneOfMcMurdo();

    private MasterDataLinkageHandler linkageHandler;

    private static final String USAGE_POINT_NAME = "usagePointName";
    private static final String USAGE_POINT_MRID = "usagePointMRID";
    private static final String METER_NAME = "meterName";
    private static final String METER_MRID = "meterMRID";
    private static final String END_DEVICE_NAME = "endDeviceName";
    private static final String END_DEVICE_MRID = "endDeviceMRID";
    private static final String METER_ROLE = "meter.role.check";
    private static final String METER_SERIAL_NUMBER = "meterSerialNumber";
    private static final String END_DEVICE_SERIAL_NUMBER = "endDeviceSerialNumber";
    private static final String GATEWAY_SERIAL_NUMBER = "gatewaySerialNumber";
    private static final String GATEWAY_NAME = "gatewayName";
    private static final Long END_DEVICE_ID = 10l;

    private static final Instant CREATED_DATE_TIME = LocalDate.of(2017, Month.JULY, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant EFFECTIVE_DATE_TIME = LocalDate.of(2017, Month.JULY, 5).atStartOfDay().toInstant(ZoneOffset.UTC);

    @Mock
    private Meter meter;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Device meterDevice;
    @Mock
    private Device endDevice;
    @Mock
    private Device gateway;
    @Mock
    private MeterRole meterRole;
    @Mock
    private UsagePointMeterActivator usagePointMeterActivator;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private DeviceConfiguration deviceConfiguration;


    @Before
    public void setUp() throws Exception {
        linkageHandler = getInstance(MasterDataLinkageHandler.class);
    }

    @Test
    public void testForMessage() throws Exception {
        //Act
        linkageHandler.forMessage(getValidMessage().build());

        //Verify
        assertThat(linkageHandler.configurationEventNode.getCreatedDateTime()).isNotNull().isEqualTo(CREATED_DATE_TIME);
        assertThat(linkageHandler.configurationEventNode.getEffectiveDateTime()).isNotNull().isEqualTo(EFFECTIVE_DATE_TIME);
        assertThat(linkageHandler.meterNodes.get(0).getMRID()).isNotNull().isEqualTo(METER_MRID);
        assertThat(linkageHandler.meterNodes.get(0).getNames().get(0).getName()).isNotNull().isEqualTo(METER_NAME);
        assertThat(linkageHandler.meterNodes.get(0).getRole()).isNotNull().isEqualTo(METER_ROLE);
        assertThat(linkageHandler.usagePointNodes.get(0).getMRID()).isNotNull().isEqualTo(USAGE_POINT_MRID);
        assertThat(linkageHandler.usagePointNodes.get(0).getNames().get(0).getName()).isNotNull().isEqualTo(USAGE_POINT_NAME);
    }

    //<editor-fold desc="Create linkage">
    @Test
    public void testCreateLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .eraseEndDeviceList()
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator).complete();
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .withUsagePointMRID(null)
                .eraseEndDeviceList()
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator).complete();
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateEndDeviceLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .withEndDeviceName(END_DEVICE_NAME)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.empty());

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint, never()).linkMeters();
        verify(usagePointMeterActivator, never()).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator, never()).complete();
        verify(topologyService).setPhysicalGateway(endDevice, meterDevice);
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateEndDeviceLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(null)
                .withEndDeviceName(END_DEVICE_NAME)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.empty());

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint, never()).linkMeters();
        verify(usagePointMeterActivator, never()).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator, never()).complete();
        verify(topologyService).setPhysicalGateway(endDevice, meterDevice);
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateEndDeviceAndUsagePointLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .withEndDeviceName(END_DEVICE_NAME)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.empty());

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator).complete();
        verify(topologyService).setPhysicalGateway(endDevice, meterDevice);
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateEndDeviceAndUsagePointLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .withUsagePointMRID(null)
                .withEndDeviceMRID(null)
                .withEndDeviceName(END_DEVICE_NAME)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);
        when(deviceService.findDeviceByName(METER_NAME)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.empty());

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator).complete();
        verify(topologyService).setPhysicalGateway(endDevice, meterDevice);
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateLinkage_roleNotSpecified() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .eraseEndDeviceList()
                .withMeterRole(null)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT)).thenReturn(meterRole);
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator).complete();
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateLinkage_invalidRoleSpecified() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterRole("invalidRole")
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole("invalidRole")).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_METER_ROLE_WITH_KEY.getErrorCode(),
                    "No meter role is found by key 'invalidRole'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_bulkReceived() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .eraseEndDeviceList()
                .spawnLists()
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator).complete();
        verifyResponse(response,
                HeaderType.Verb.CREATED,
                ReplyType.Result.PARTIAL,
                MessageSeeds.UNSUPPORTED_BULK_OPERATION,
                MessageSeeds.UNSUPPORTED_BULK_OPERATION);
    }

    @Test
    public void testCreateLinkage_meterNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_METER_WITH_MRID.getErrorCode(),
                    "No meter or gateway is found by MRID 'meterMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_meterNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_METER_WITH_NAME.getErrorCode(),
                    "No meter or gateway is found by name 'meterName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_usagePointNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                    "No usage point is found by MRID 'usagePointMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_usagePointNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                    "No usage point is found by name 'usagePointName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_EndDeviceNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.empty());
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_END_DEVICE_WITH_MRID.getErrorCode(),
                    "No end device is found by MRID 'endDeviceMRID'.");
            verify(topologyService, never()).setPhysicalGateway(endDevice, meterDevice);
        }
    }

    @Test
    public void testCreateLinkage_EndDeviceNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(null)
                .withEndDeviceName(END_DEVICE_NAME)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.empty());
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_END_DEVICE_WITH_NAME.getErrorCode(),
                    "No end device is found by name 'endDeviceName'.");
            verify(topologyService, never()).setPhysicalGateway(endDevice, meterDevice);
        }
    }

    @Test
    public void testCreateLinkage_meterAndUsagePointAlreadyLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(meter.getName()).thenReturn(METER_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.SAME_USAGE_POINT_ALREADY_LINKED.getErrorCode(),
                    "Meter 'meterName' is already linked to usage point 'usagePointName' at the given time '2017-07-01T12:00:00+12:00'.");
            verifyZeroInteractions(usagePointMeterActivator);
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_MeterAndEndDeviceAlreadyLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.of(meterDevice));
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);
        when(endDevice.getSerialNumber()).thenReturn(END_DEVICE_SERIAL_NUMBER);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.METER_ALREADY_LINKED_TO_END_DEVICE.getErrorCode(),
                    "End device 'endDeviceName' (serial number 'endDeviceSerialNumber') already linked to gateway 'meterName' (serial number 'meterSerialNumber').");
            verify(topologyService, never()).setPhysicalGateway(endDevice, meterDevice);
        }
    }

    @Test
    public void testCreateLinkage_MeterAndEndDeviceAreTheSame() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(METER_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.CAN_NOT_BE_GATEWAY_TO_ITSELF.getErrorCode(),
                    "Device 'meterName' (serial number 'meterSerialNumber') can't be its own gateway.");
            verify(topologyService, never()).setPhysicalGateway(meterDevice, meterDevice);
        }
    }

    @Test
    public void testCreateLinkage_MeterAndEndDeviceNotSupportedMaster() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.NONE);
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NOT_SUPPORTED_MASTER.getErrorCode(),
                    "Device 'meterName' (serial number 'meterSerialNumber') isn't configured to act as gateway.");
            verify(topologyService, never()).setPhysicalGateway(meterDevice, meterDevice);
        }
    }

    @Test
    public void testCreateLinkage_MeterAndEndDeviceNotSupportedSlave() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(true);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.empty());
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);
        when(endDevice.getSerialNumber()).thenReturn(END_DEVICE_SERIAL_NUMBER);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NOT_SUPPORTED_SLAVE.getErrorCode(),
                    "Device 'endDeviceName' (serial number 'endDeviceSerialNumber') isn't configured to act as end device.");
            verify(topologyService, never()).setPhysicalGateway(endDevice, meterDevice);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Close linkage">
    @Test
    public void testCloseLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .eraseEndDeviceList()
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.clear(EFFECTIVE_DATE_TIME, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator).complete();
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .withMeterMRID(null)
                .eraseEndDeviceList()
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.clear(EFFECTIVE_DATE_TIME, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator).complete();
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseEndDeviceLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(deviceService.findAndLockDeviceById(END_DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.of(meterDevice));

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint, never()).linkMeters();
        verify(usagePointMeterActivator, never()).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator, never()).complete();
        verify(topologyService).clearPhysicalGateway(endDevice);
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseEndDeviceLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(null)
                .withEndDeviceName(END_DEVICE_NAME)
                .eraseUsagePointList()
                .build();
        when(endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.of(endDevice));
        when(deviceService.findAndLockDeviceById(END_DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.of(meterDevice));

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint, never()).linkMeters();
        verify(usagePointMeterActivator, never()).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator, never()).complete();
        verify(topologyService).clearPhysicalGateway(endDevice);
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseEndDeviceAndUsagePointLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .build();
        when(endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(deviceService.findAndLockDeviceById(END_DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.of(meterDevice));
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.clear(EFFECTIVE_DATE_TIME, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator).complete();
        verify(topologyService).clearPhysicalGateway(endDevice);
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseEndDeviceAndUsagePointLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .withMeterMRID(null)
                .withEndDeviceMRID(null)
                .withEndDeviceName(END_DEVICE_NAME)
                .build();
        when(endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(deviceService.findDeviceByName(METER_NAME)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.of(endDevice));
        when(deviceService.findAndLockDeviceById(END_DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(meterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.LOCAL_AREA_NETWORK);
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.of(meterDevice));
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.clear(EFFECTIVE_DATE_TIME, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint).linkMeters();
        verify(usagePointMeterActivator).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator).complete();
        verify(topologyService).clearPhysicalGateway(endDevice);
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseLinkage_meterNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_METER_WITH_MRID.getErrorCode(),
                    "No meter or gateway is found by MRID 'meterMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_meterNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_METER_WITH_NAME.getErrorCode(),
                    "No meter or gateway is found by name 'meterName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_usagePointNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                    "No usage point is found by MRID 'usagePointMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_usagePointNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                    "No usage point is found by name 'usagePointName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_EndDeviceNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.empty());
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_END_DEVICE_WITH_MRID.getErrorCode(),
                    "No end device is found by MRID 'endDeviceMRID'.");
            verify(topologyService, never()).setPhysicalGateway(endDevice, meterDevice);
        }
    }

    @Test
    public void testCloseLinkage_EndDeviceNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(null)
                .withEndDeviceName(END_DEVICE_NAME)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.empty());
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_END_DEVICE_WITH_NAME.getErrorCode(),
                    "No end device is found by name 'endDeviceName'.");
            verify(topologyService, never()).setPhysicalGateway(endDevice, meterDevice);
        }
    }

    @Test
    public void testCloseLinkage_meterAndUsagePointNotLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.emptyList());
        when(meter.getName()).thenReturn(METER_NAME);
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.METER_AND_USAGE_POINT_NOT_LINKED.getErrorCode(),
                    "Meter 'meterName' isn't linked to usage point 'usagePointName' at the given time '2017-07-05T12:00:00+12:00'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_meterAndEndDeviceNotLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.empty());
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);
        when(endDevice.getSerialNumber()).thenReturn(END_DEVICE_SERIAL_NUMBER);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.END_DEVICE_IS_NOT_LINKED.getErrorCode(),
                    "End device 'endDeviceName' (serial number 'endDeviceSerialNumber') isn't linked to gateway 'meterName' (serial number 'meterSerialNumber').");
            verify(topologyService, never()).clearPhysicalGateway(endDevice);
        }
    }

    @Test
    public void testCloseLinkage_meterAndEndDeviceAreTheSame() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(METER_MRID)
                .eraseUsagePointList()
                .build();
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.CAN_NOT_UNLINK_ITSELF.getErrorCode(),
                    "Device 'meterName' (serial number 'meterSerialNumber') can't be unlinked from itself.");
            verify(topologyService, never()).clearPhysicalGateway(endDevice);
        }
    }

    @Test
    public void testCloseLinkage_meterAndEndDeviceAlreadyLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withEndDeviceMRID(END_DEVICE_MRID)
                .eraseUsagePointList()
                .build();
        when(endDevice.getId()).thenReturn(END_DEVICE_ID);
        when(deviceService.findDeviceByMrid(METER_MRID)).thenReturn(Optional.of(meterDevice));
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(endDevice));
        when(deviceService.findAndLockDeviceById(END_DEVICE_ID)).thenReturn(Optional.of(endDevice));
        when(endDevice.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(topologyService.getPhysicalGateway(endDevice)).thenReturn(Optional.of(gateway));
        when(meterDevice.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(meterDevice.getName()).thenReturn(METER_NAME);
        when(endDevice.getSerialNumber()).thenReturn(END_DEVICE_SERIAL_NUMBER);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);
        when(gateway.getSerialNumber()).thenReturn(GATEWAY_SERIAL_NUMBER);
        when(gateway.getName()).thenReturn(GATEWAY_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.METER_ALREADY_LINKED_TO_END_DEVICE.getErrorCode(),
                    "End device 'endDeviceName' (serial number 'endDeviceSerialNumber') already linked to gateway 'gatewayName' (serial number 'gatewaySerialNumber').");
            verify(topologyService, never()).clearPhysicalGateway(endDevice);
        }
    }
    //</editor-fold>

    private void verifyResponse(MasterDataLinkageConfigResponseMessageType response,
                                HeaderType.Verb expectedVerb,
                                ReplyType.Result expectedReplyType,
                                MessageSeeds... expectedWarnings) {
        assertThat(response.getHeader().getNoun()).isEqualTo("MasterDataLinkageConfig");
        assertThat(response.getHeader().getVerb()).isEqualTo(expectedVerb);
        assertThat(response.getReply().getResult()).isEqualTo(expectedReplyType);
        assertThat(response.getReply().getError()).hasSize(expectedWarnings.length);

        List<String> expectedCodes = Stream.of(expectedWarnings).map(MessageSeeds::getErrorCode).collect(Collectors.toList());
        response.getReply().getError().forEach(
                errorType -> {
                    assertThat(errorType.getLevel()).isEqualTo(ErrorType.Level.WARNING);
                    assertThat(expectedCodes).contains(errorType.getCode());
                });
    }

    @Override
    protected MasterDataLinkageMessageBuilder getValidMessage() {
        return MasterDataLinkageMessageBuilder.createEmptyMessage()
                .withCreatedDateTime(CREATED_DATE_TIME)
                .withEffectiveDateTime(EFFECTIVE_DATE_TIME)
                .withMeterMRID(METER_MRID)
                .withMeterName(METER_NAME)
                .withMeterRole(METER_ROLE)
                .withUsagePointMRID(USAGE_POINT_MRID)
                .withUsagePointName(USAGE_POINT_NAME);
    }

}
