/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.DEFAULT_METERING_SYSTEM_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterEventCreateRequestFactoryTest {
    private final static String EVENT_CODE = "1.2.3.4";
    private final static String DEVICE_EVENT_TYPE = "1";
    private final static String MRID = "1234567890";
    private final static String DEVICE_TYPE_NAME = "deviceTypeName";
    private final static String DEVICE_GROUP_NAME = "deviceGroupName";
    private final static String SAP_DEVICE_ID = "sapDeviceID";
    private final static String END_DEVICE_NAME = "endDeviceName";
    private final static String AMR_ID = "1";
    private final static String EVENT_TYPE_CODE = "3";
    private final static String EVENT_TYPE_MRID = "122333";
    private final static Set<String> listOfDeviceTypes = new HashSet<>();
    private final static Set<String> listOfDeviceGroups = new HashSet<>();

    @Mock
    private DeviceService deviceService;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private EndDeviceEventRecord eventRecord;
    @Mock
    private SAPDeviceEventType eventType;
    @Mock
    EndDeviceEventType endDeviceEventType;
    @Mock
    private EndDevice endDevice;
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private EndDeviceGroup endDeviceGroup;

    private LocalDate date = LocalDate.of(2022, 7, 29);
    private Instant currentTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    private ForwardedDeviceEventTypesFormatter formatter;
    private Function<EndDeviceEventRecord, Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt>> eventFormatter;
    private MeterEventCreateRequestFactory meterEventCreateRequestFactory;

    @Before
    public void initMocks() {
        formatter = new ForwardedDeviceEventTypesFormatter(sapCustomPropertySets, deviceService, meteringGroupsService, Clock.fixed(currentTime, ZoneId.systemDefault()));
        eventFormatter = formatter::filterAndFormat;
        meterEventCreateRequestFactory = new MeterEventCreateRequestFactory(formatter);

        when(deviceService.findDeviceByMrid(MRID)).thenReturn(Optional.of(device));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getName()).thenReturn(DEVICE_TYPE_NAME);
        when(sapCustomPropertySets.getRegisteredSapDeviceId(endDevice)).thenReturn(Optional.of(SAP_DEVICE_ID));
        when(sapCustomPropertySets.isAnyLrnPresentForDate(Long.parseLong(AMR_ID), currentTime)).thenReturn(true);
        when(meteringGroupsService.findEndDeviceGroupByName(DEVICE_GROUP_NAME)).thenReturn(Optional.of(endDeviceGroup));
        when(endDeviceGroup.isMember(endDevice, currentTime)).thenReturn(true);
        when(endDevice.getAmrId()).thenReturn(AMR_ID);
        when(endDevice.getMRID()).thenReturn(MRID);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);

        mockEventType(eventType);
        mockEventRecord(eventRecord);

        listOfDeviceTypes.add(DEVICE_TYPE_NAME);
        listOfDeviceGroups.add(DEVICE_GROUP_NAME);
    }

    @Test
    public void noEventCodeInMappingTest() {
        Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> msg = meterEventCreateRequestFactory.getMeterEventBulkMessage(currentTime, DEFAULT_METERING_SYSTEM_ID, eventRecord);
        AssertionsForClassTypes.assertThat(msg).isEqualTo(Optional.empty());
    }

    @Test
    public void successfulTest() {
        formatter.add(eventType);
        Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> msg = meterEventCreateRequestFactory.getMeterEventBulkMessage(currentTime, DEFAULT_METERING_SYSTEM_ID, eventRecord);
        assertThat(msg).isPresent();
        assertThat(msg.get().getUtilitiesSmartMeterEventERPCreateRequestMessage()).hasSize(1);
        assertThat(msg.get().getUtilitiesSmartMeterEventERPCreateRequestMessage().get(0).getUtilitiesSmartMeterEvent().getTypeCode().getValue()).isEqualTo(EVENT_TYPE_CODE);
    }

    @Test
    public void failureDeviceTypeTest() {
        formatter.add(eventType);
        when(deviceType.getName()).thenReturn("not " + DEVICE_TYPE_NAME);
        Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> msg = meterEventCreateRequestFactory.getMeterEventBulkMessage(currentTime, DEFAULT_METERING_SYSTEM_ID, eventRecord);
        assertThat(msg).isEqualTo(Optional.empty());
    }

    @Test
    public void failureDeviceGroupTest() {
        formatter.add(eventType);
        when(endDeviceGroup.isMember(endDevice, currentTime)).thenReturn(false);
        Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> msg = meterEventCreateRequestFactory.getMeterEventBulkMessage(currentTime, DEFAULT_METERING_SYSTEM_ID, eventRecord);
        assertThat(msg).isEqualTo(Optional.empty());
    }

    @Test
    public void exceptionTest() {
        MeterEventCreateRequestFactory meterEventCreateRequestFactory1 = new MeterEventCreateRequestFactory(null);
        assertThatThrownBy(() -> meterEventCreateRequestFactory1.getMeterEventBulkMessage(currentTime, DEFAULT_METERING_SYSTEM_ID, eventRecord)).isInstanceOf(RuntimeException.class).hasMessage("Failed to send notification about event " + EVENT_TYPE_MRID + " (" + DEVICE_EVENT_TYPE + ") on device " + END_DEVICE_NAME + ": mapping csv hadn't been loaded properly.");
    }

    private void mockEventRecord(EndDeviceEventRecord eventRecord) {
        when(eventRecord.getEventTypeCode()).thenReturn(EVENT_CODE);
        when(eventRecord.getDeviceEventType()).thenReturn(DEVICE_EVENT_TYPE);
        when(eventRecord.getEndDevice()).thenReturn(endDevice);
        when(eventRecord.getCreatedDateTime()).thenReturn(currentTime);
        when(eventRecord.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEventType.getMRID()).thenReturn(EVENT_TYPE_MRID);
    }

    private void mockEventType(SAPDeviceEventType eventType) {
        when(eventType.getEventCode()).thenReturn(Optional.of(EVENT_CODE));
        when(eventType.getTypeCode()).thenReturn(Integer.parseInt(EVENT_TYPE_CODE));
        when(eventType.getDeviceTypes()).thenReturn(listOfDeviceTypes);
        when(eventType.getDeviceGroups()).thenReturn(listOfDeviceGroups);
    }
}
