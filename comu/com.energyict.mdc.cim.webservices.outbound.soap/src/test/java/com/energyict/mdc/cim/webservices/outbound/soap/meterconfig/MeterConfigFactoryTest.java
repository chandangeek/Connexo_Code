/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import ch.iec.tc57._2011.meterconfig.MeterConfig;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterConfigFactoryTest {

    private static final String DEVICE_MRID = UUID.randomUUID().toString();
    private static final String DEVICE_NAME = "SPE0000001";
    private static final String SERIAL_NUMBER = "00000001";
    private static final Instant RECEIVED_DATE = Instant.now();
    private static final String DEVICE_TYPE_NAME = "Actaris SL7000";
    private static final String BATCH = "batch";
    private static final String MANUFACTURER = "Honeywell";
    private static final String MODEL_NUMBER = "001";
    private static final String MODEL_VERSION = "1.0.0";
    private static final String DEVICE_CONFIG_ID = "123";
    private static final String DEVICE_CONFIGURATION_NAME = "Default";
    private static final float MULTIPLIER = 1.23456789f;
    private static final Instant START_DATE = Instant.from(ZonedDateTime.of(2020, 3, 1, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final Instant FIRMWARE_SENT_DATE = Instant.from(ZonedDateTime.of(2020, 3, 2, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final String FIRMWARE_VERSION = "v1.0";
    private static final String FIRMWARE_IMAGE_IDENTIFIER = "firmware image";
    private static final Instant FIRMWARE_UPLOAD_DATE = Instant.from(ZonedDateTime.of(2020, 3, 3, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final Instant FIRMWARE_ACTIVATION_DATE = Instant.from(ZonedDateTime.of(2020, 3, 4, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final String CALENDAR_NAME = "Calendar name";
    private static final Instant CALENDAR_UPLOAD_DATE = Instant.from(ZonedDateTime.of(2020, 3, 5, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final Date CALENDAR_ACTIVATION_DATE = Date.from(Instant.from(ZonedDateTime.of(2020, 3, 6, 1, 0, 0, 0, ZoneId.systemDefault())));
    private static final Instant CALENDAR_ACTIVATION_DATE_INSTANT = Instant.from(ZonedDateTime.of(2020, 3, 6, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final Instant CONTACTOR_STATUS_SENT_DATE = Instant.from(ZonedDateTime.of(2020, 3, 6, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final Instant CONTACTOR_STATUS_VERIFIED_SENT_DATE = Instant.from(ZonedDateTime.of(2020, 3, 5, 1, 0, 0, 0, ZoneId.systemDefault()));
    private static final Date CONTACTOR_ACTIVATION_DATE = Date.from(Instant.from(ZonedDateTime.of(2020, 3, 7, 1, 0, 0, 0, ZoneId.systemDefault())));
    private static final Date CONTACTOR_ACTIVATION_DATE_VERIFIED = Date.from(Instant.from(ZonedDateTime.of(2020, 3, 7, 0, 0, 0, 0, ZoneId.systemDefault())));
    private static final Instant CONTACTOR_ACTIVATION_DATE_VERIFIED_INSTANT = Instant.from(ZonedDateTime.of(2020, 3, 7, 0, 0, 0, 0, ZoneId.systemDefault()));

    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private FirmwareService firmwareService;
    @Mock
    private DeviceMessageService deviceMessageService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private Clock clock;
    @Mock
    private NlsService nlsService;
    @Mock
    private Device device;
    @Mock
    private Batch batch;
    @Mock
    private State state;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private CIMLifecycleDates lifecycleDates;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private ComTask comTask;
    @Mock
    private StatusInformationTask statusInformationTask;
    @Mock
    private ComTaskExecutionSession session;
    @Mock
    private ActivatedFirmwareVersion activatedFirmwareVersion;
    @Mock
    private FirmwareManagementDeviceUtils versionUtils;
    @Mock
    private DeviceMessage calendarMessage, contactorMessage, verifiedContactorMessage;
    @Mock
    private FirmwareVersion firmwareVersion;
    @Mock
    private Device.CalendarSupport calendarSupport;
    @Mock
    private ActiveEffectiveCalendar activeEffectiveCalendar;
    @Mock
    private AllowedCalendar allowedCalendar;
    @Mock
    private Calendar calendar;
    @Mock
    private DeviceMessageCategory calendarCategory, contactorCategory;
    @Mock
    private Finder<DeviceMessage> calendarFinder, contactorFinder;
    @Mock
    private DeviceMessageAttribute deviceMessageAttribute, activationDateAttr, contactorActivationDateAttr, contactorActivationDateAttrVerified;
    @Mock
    private ActivatedBreakerStatus breakerStatus;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private MeterConfigFactory meterConfigFactory;

    @Before
    public void setUp() {
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        mockDevice();
        meterConfigFactory = new MeterConfigFactoryImpl(customPropertySetService, deviceService, firmwareService, deviceMessageService, deviceMessageSpecificationService, clock, nlsService);
    }

    @Test
    public void withoutMeterConfigTest() {
        MeterConfig meterConfig = meterConfigFactory.asGetMeterConfig(device, PingResult.NOT_NEEDED, false);
        assertThat(meterConfig.getMeter().get(0).getMRID()).isEqualTo(DEVICE_MRID);
        assertThat(meterConfig.getMeter().get(0).getSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(meterConfig.getMeter().get(0).getNames().get(0).getName()).isEqualTo(DEVICE_NAME);
        assertThat(meterConfig.getMeter().get(0).getLotNumber()).isEqualTo(BATCH);
        assertThat(meterConfig.getMeter().get(0).getEndDeviceInfo().getAssetModel().getModelNumber()).isEqualTo(MODEL_NUMBER);
        assertThat(meterConfig.getMeter().get(0).getEndDeviceInfo().getAssetModel().getModelVersion()).isEqualTo(MODEL_VERSION);
        assertThat(meterConfig.getMeter().get(0).getType()).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(meterConfig.getMeter().get(0).getMeterMultipliers().get(0).getValue()).isEqualTo(MULTIPLIER);
        assertThat(meterConfig.getMeter().get(0).getStatus().getValue()).isEqualTo(DefaultState.IN_STOCK.getDefaultFormat());
        assertThat(meterConfig.getMeter().get(0).getMeterStatus()).isNull();
    }

    @Test
    public void withMeterConfigTest() {
        mockComTask();
        mockFirmware();
        mockCalendar();
        mockContactorStatus();
        MeterConfig meterConfig = meterConfigFactory.asGetMeterConfig(device, PingResult.NOT_NEEDED, true);
        assertThat(meterConfig.getMeter().get(0).getMRID()).isEqualTo(DEVICE_MRID);
        assertThat(meterConfig.getMeter().get(0).getSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(meterConfig.getMeter().get(0).getNames().get(0).getName()).isEqualTo(DEVICE_NAME);
        assertThat(meterConfig.getMeter().get(0).getLotNumber()).isEqualTo(BATCH);
        assertThat(meterConfig.getMeter().get(0).getEndDeviceInfo().getAssetModel().getModelNumber()).isEqualTo(MODEL_NUMBER);
        assertThat(meterConfig.getMeter().get(0).getEndDeviceInfo().getAssetModel().getModelVersion()).isEqualTo(MODEL_VERSION);
        assertThat(meterConfig.getMeter().get(0).getType()).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(meterConfig.getMeter().get(0).getMeterMultipliers().get(0).getValue()).isEqualTo(MULTIPLIER);
        assertThat(meterConfig.getMeter().get(0).getStatus().getValue()).isEqualTo(DefaultState.IN_STOCK.getDefaultFormat());
        assertThat(meterConfig.getMeter().get(0).getMeterStatus()).isNotNull();
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getStatusInfoComTaskExecutionStatus()).isEqualTo(ComTaskExecutionSession.SuccessIndicator.Success.toString());
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getStatusInfoComTaskExecutionDate()).isEqualTo(START_DATE);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getFirmwareStatus().getMeterFirmware().getVersion()).isEqualTo(FIRMWARE_VERSION);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getFirmwareStatus().getMeterFirmware().getImageIdentifier()).isEqualTo(FIRMWARE_IMAGE_IDENTIFIER);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getFirmwareStatus().getMeterFirmware().getUploadDate()).isEqualTo(FIRMWARE_UPLOAD_DATE);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getFirmwareStatus().getMeterFirmware().getActivationDate()).isEqualTo(FIRMWARE_ACTIVATION_DATE);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getCalendarStatus().getCalendar()).isEqualTo(CALENDAR_NAME);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getCalendarStatus().getUploadDate()).isEqualTo(CALENDAR_UPLOAD_DATE);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getCalendarStatus().getActivationDate()).isEqualTo(CALENDAR_ACTIVATION_DATE_INSTANT);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getContactorStatus().getStatus()).isEqualTo(BreakerStatus.CONNECTED.toString());
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getContactorStatus().getSentDate()).isEqualTo(CONTACTOR_STATUS_VERIFIED_SENT_DATE);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getContactorStatus().getActivationDate()).isEqualTo(CONTACTOR_ACTIVATION_DATE_VERIFIED_INSTANT);
    }

    @Test
    public void withMeterConfigEmptyFirmwareAndCalendarAndContactorTest() {
        mockComTask();
        mockEmptyFirmware();
        mockEmptyCalendar();
        mockEmptyContactorStatus();
        MeterConfig meterConfig = meterConfigFactory.asGetMeterConfig(device, PingResult.NOT_NEEDED,true);
        assertThat(meterConfig.getMeter().get(0).getMRID()).isEqualTo(DEVICE_MRID);
        assertThat(meterConfig.getMeter().get(0).getSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(meterConfig.getMeter().get(0).getNames().get(0).getName()).isEqualTo(DEVICE_NAME);
        assertThat(meterConfig.getMeter().get(0).getLotNumber()).isEqualTo(BATCH);
        assertThat(meterConfig.getMeter().get(0).getEndDeviceInfo().getAssetModel().getModelNumber()).isEqualTo(MODEL_NUMBER);
        assertThat(meterConfig.getMeter().get(0).getEndDeviceInfo().getAssetModel().getModelVersion()).isEqualTo(MODEL_VERSION);
        assertThat(meterConfig.getMeter().get(0).getType()).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(meterConfig.getMeter().get(0).getMeterMultipliers().get(0).getValue()).isEqualTo(MULTIPLIER);
        assertThat(meterConfig.getMeter().get(0).getStatus().getValue()).isEqualTo(DefaultState.IN_STOCK.getDefaultFormat());
        assertThat(meterConfig.getMeter().get(0).getMeterStatus()).isNotNull();
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getStatusInfoComTaskExecutionStatus()).isEqualTo(ComTaskExecutionSession.SuccessIndicator.Success.toString());
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getStatusInfoComTaskExecutionDate()).isEqualTo(START_DATE);
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getFirmwareStatus()).isNull();
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getCalendarStatus()).isNull();
        assertThat(meterConfig.getMeter().get(0).getMeterStatus().getContactorStatus()).isNull();
    }

    private void mockDevice() {
        when(device.getCreateTime()).thenReturn(RECEIVED_DATE);
        when(device.getmRID()).thenReturn(DEVICE_MRID);
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(device.getManufacturer()).thenReturn(MANUFACTURER);
        when(device.getModelNumber()).thenReturn(MODEL_NUMBER);
        when(device.getModelVersion()).thenReturn(MODEL_VERSION);
        when(device.getBatch()).thenReturn(Optional.of(batch));
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        when(batch.getName()).thenReturn(BATCH);
        when(device.getMultiplier()).thenReturn(BigDecimal.valueOf(MULTIPLIER));
        when(device.getState()).thenReturn(state);
        when(state.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        mockDeviceConfiguration();
        mockLifeCycleDates();
    }

    private void mockDeviceConfiguration() {
        when(deviceConfigurationService.findDeviceTypeByName(any())).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceTypeByName(DEVICE_TYPE_NAME)).thenReturn(Optional.of(deviceType));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getId()).thenReturn(Long.valueOf(DEVICE_CONFIG_ID));
        when(deviceConfiguration.getName()).thenReturn(DEVICE_CONFIGURATION_NAME);
        mockDeviceType();
    }

    private void mockLifeCycleDates() {
        when(lifecycleDates.getReceivedDate()).thenReturn(Optional.of(RECEIVED_DATE));
        when(device.getLifecycleDates()).thenReturn(lifecycleDates);
    }

    private void mockDeviceType() {
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getName()).thenReturn(DEVICE_TYPE_NAME);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
    }

    private void mockComTask() {
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.isSuspended()).thenReturn(false);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.isOnHold()).thenReturn(false);
        when(comTaskExecution.getLastSession()).thenReturn(Optional.of(session));
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(statusInformationTask));
        when(comTask.isManualSystemTask()).thenReturn(true);
        when(session.getSuccessIndicator()).thenReturn(ComTaskExecutionSession.SuccessIndicator.Success);
        when(session.getStartDate()).thenReturn(START_DATE);
    }

    private void mockFirmware() {
        when(firmwareService.getSupportedFirmwareTypes(deviceType)).thenReturn(EnumSet.of(FirmwareType.METER));
        when(firmwareService.getActiveFirmwareVersion(device, FirmwareType.METER)).thenReturn(Optional.of(activatedFirmwareVersion));
        when(firmwareService.getFirmwareManagementDeviceUtilsFor(device, true)).thenReturn(versionUtils);
        when(versionUtils.getFirmwareMessages()).thenReturn(Collections.singletonList(calendarMessage));
        when(activatedFirmwareVersion.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(versionUtils.getFirmwareVersionFromMessage(calendarMessage)).thenReturn(Optional.of(firmwareVersion));
        when(calendarMessage.getSentDate()).thenReturn(Optional.of(FIRMWARE_SENT_DATE));
        when(firmwareVersion.getFirmwareVersion()).thenReturn(FIRMWARE_VERSION);
        when(firmwareVersion.getImageIdentifier()).thenReturn(FIRMWARE_IMAGE_IDENTIFIER);
        when(calendarMessage.getReleaseDate()).thenReturn(FIRMWARE_UPLOAD_DATE);
        when(versionUtils.getActivationDateFromMessage(calendarMessage)).thenReturn(Optional.of(FIRMWARE_ACTIVATION_DATE));
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
    }

    private void mockEmptyFirmware() {
        when(firmwareService.getSupportedFirmwareTypes(deviceType)).thenReturn(EnumSet.noneOf(FirmwareType.class));
    }

    private void mockCalendar() {
        when(device.calendars()).thenReturn(calendarSupport);
        when(calendarSupport.getActive()).thenReturn(Optional.of(activeEffectiveCalendar));
        when(activeEffectiveCalendar.getAllowedCalendar()).thenReturn(allowedCalendar);
        when(allowedCalendar.isObsolete()).thenReturn(false);
        when(allowedCalendar.getName()).thenReturn(CALENDAR_NAME);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(deviceMessageSpecificationService.allCategories()).thenReturn(Arrays.asList(calendarCategory, contactorCategory));
        when(deviceMessageService.findDeviceMessagesByFilter(any(DeviceMessageQueryFilter.class))).thenReturn(calendarFinder,contactorFinder);
        when(calendarCategory.getName()).thenReturn("Activity calendar");
        when(calendarFinder.stream()).thenReturn(Stream.of(calendarMessage));
        doReturn(Arrays.asList(deviceMessageAttribute, activationDateAttr)).when(calendarMessage).getAttributes();
        when(deviceMessageAttribute.getName()).thenReturn(activityCalendarNameAttributeName);
        when(deviceMessageAttribute.getValue()).thenReturn(CALENDAR_NAME);
        when(activationDateAttr.getName()).thenReturn(activityCalendarActivationDateAttributeName);
        when(activationDateAttr.getValue()).thenReturn(CALENDAR_ACTIVATION_DATE);
        when(calendarMessage.getSentDate()).thenReturn(Optional.of(CALENDAR_UPLOAD_DATE));
    }

    private void mockEmptyCalendar() {
        when(device.calendars()).thenReturn(calendarSupport);
        when(calendarSupport.getActive()).thenReturn(Optional.empty());
    }

    private void mockContactorStatus() {
        when(deviceService.getActiveBreakerStatus(device)).thenReturn(Optional.of(breakerStatus));
        when(breakerStatus.getBreakerStatus()).thenReturn(BreakerStatus.CONNECTED);
        when(breakerStatus.getLastChecked()).thenReturn(CONTACTOR_STATUS_VERIFIED_SENT_DATE);
        when(contactorCategory.getName()).thenReturn("Contactor");
        when(contactorFinder.stream()).thenReturn(Stream.of(verifiedContactorMessage, contactorMessage));
        when(verifiedContactorMessage.getSentDate()).thenReturn(Optional.of(CONTACTOR_STATUS_VERIFIED_SENT_DATE));
        when(contactorMessage.getSentDate()).thenReturn(Optional.of(CONTACTOR_STATUS_SENT_DATE));
        doReturn(Arrays.asList(contactorActivationDateAttrVerified)).when(verifiedContactorMessage).getAttributes();
        doReturn(Arrays.asList(contactorActivationDateAttr)).when(contactorMessage).getAttributes();
        when(contactorActivationDateAttrVerified.getName()).thenReturn(contactorActivationDateAttributeName);
        when(contactorActivationDateAttrVerified.getValue()).thenReturn(CONTACTOR_ACTIVATION_DATE_VERIFIED);
        when(contactorActivationDateAttr.getName()).thenReturn(contactorActivationDateAttributeName);
        when(contactorActivationDateAttr.getValue()).thenReturn(CONTACTOR_ACTIVATION_DATE);
    }

    private void mockEmptyContactorStatus() {
        when(deviceService.getActiveBreakerStatus(device)).thenReturn(Optional.empty());
    }
}
