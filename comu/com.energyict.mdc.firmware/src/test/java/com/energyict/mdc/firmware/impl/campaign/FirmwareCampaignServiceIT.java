/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallStateChangeTopicHandler;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.google.common.collect.ImmutableMap;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FirmwareCampaignServiceIT {
    private static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();
    private static ServiceCallService serviceCallService;
    private static MeteringGroupsService meteringGroupsService;
    private static TransactionService transactionService;
    private static FirmwareCampaignServiceImpl firmwareCampaignService;
    private static FirmwareServiceImpl firmwareService;
    private static ServiceCallHandler serviceCallHandler;
    private static CalendarService calendarService;
    private static DeviceConfigurationService deviceConfigurationService;
    private static DeviceMessageSpecificationService deviceMessageSpecificationService;
    private Clock clock = inMemoryPersistence.get(Clock.class);
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
    private static DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
    private static DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
    private static ProtocolPluggableService protocolPluggableService;
    private static CustomPropertySet<ServiceCall, FirmwareCampaignDomainExtension> serviceCallCPS;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.get(TransactionService.class));

    @BeforeClass
    public static void setUp() {
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.firmware", false);
        serviceCallService = inMemoryPersistence.get(ServiceCallService.class);
        firmwareService = inMemoryPersistence.get(FirmwareServiceImpl.class);
        firmwareCampaignService = inMemoryPersistence.get(FirmwareCampaignServiceImpl.class);
        meteringGroupsService = inMemoryPersistence.get(MeteringGroupsService.class);
        transactionService = inMemoryPersistence.get(TransactionService.class);
        calendarService = inMemoryPersistence.get(CalendarService.class);
        deviceConfigurationService = inMemoryPersistence.get(DeviceConfigurationService.class);
        deviceMessageSpecificationService = inMemoryPersistence.get(DeviceMessageSpecificationService.class);
        protocolPluggableService = inMemoryPersistence.get(ProtocolPluggableService.class);
        List<DeviceMessageSpec> deviceMessageIds;
        deviceMessageIds = new ArrayList<>();
        DeviceMessageSpec deviceMessageSpec1 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        DeviceMessageSpec deviceMessageSpec2 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);
        DeviceMessageSpec deviceMessageSpec3 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec3.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec3);
        DeviceMessageSpec deviceMessageSpec4 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec4.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.dbValue());
        deviceMessageIds.add(deviceMessageSpec4);
        DeviceMessageSpec deviceMessageSpec5 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec5.getId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec5);
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.empty());
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(deviceProtocolDialect));
        when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolDialect.getDeviceProtocolDialectName()).thenReturn("name");
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(protocolPluggableService.adapt(any(AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> UPLAuthenticationLevelAdapter.adaptTo(invocation.getArgumentAt(0, DeviceAccessLevel.class), thesaurus));
        when(protocolPluggableService.adapt(any(EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> UPLEncryptionLevelAdapter.adaptTo(invocation.getArgumentAt(0, DeviceAccessLevel.class), thesaurus));
        transactionService.execute(() -> {
            EventServiceImpl eventService = inMemoryPersistence.get(EventServiceImpl.class);
            eventService.addTopicHandler(inMemoryPersistence.get(StateTransitionTriggerEventTopicHandler.class));
            eventService.addTopicHandler(inMemoryPersistence.get(ServiceCallStateChangeTopicHandler.class));
            PropertySpecService propertySpecService = inMemoryPersistence.get(PropertySpecService.class);
            serviceCallCPS = new FirmwareCampaignCustomPropertySet(thesaurus, propertySpecService, firmwareService);
            serviceCallHandler = new FirmwareCampaignServiceCallHandler(firmwareService);
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", FirmwareCampaignServiceCallHandler.NAME));
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", FirmwareCampaignItemServiceCallHandler.NAME));
            return null;
        });
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    @Transactional
    public void createCampaignTest() {
        makeDefaultCampaign();
        assertTrue(serviceCallService.getServiceCallFinder().stream()
                .anyMatch(serviceCall -> serviceCall.getExtension(FirmwareCampaignDomainExtension.class).isPresent()));
    }

    @Test
    @Transactional
    public void getAllCampaignsTest() {
        assertThat(firmwareCampaignService.streamAllCampaigns().collect(Collectors.toList())).isEmpty();
        makeDefaultCampaign();
        assertThat(firmwareCampaignService.streamAllCampaigns().collect(Collectors.toList())).isNotEmpty();
    }

    @Test
    @Transactional
    public void campaignBuilderTest() {
        FirmwareType firmwareType = FirmwareType.METER;
        DeviceType deviceType1 = deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass);
        String name = "camp-01";
        String deviceGroup = "Electro";
        Instant activationStart = Instant.ofEpochSecond(70000);
        Instant activationEnd = Instant.ofEpochSecond(75000);
        ProtocolSupportedFirmwareOptions activationOption = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE;
        TimeDuration timeValidation = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
        FirmwareCampaign firmwareCampaign1 = firmwareCampaignService.newFirmwareCampaign(name)
                .withFirmwareType(firmwareType)
                .withUploadTimeBoundaries(activationStart, activationEnd)
                .withDeviceGroup(deviceGroup)
                .withManagementOption(activationOption)
                .withValidationTimeout(timeValidation)
                .withDeviceType(deviceType1)
                .withUniqueFirmwareVersion(false)
                .create();
        assertThat(firmwareCampaign1.getName()).isEqualTo(name);
        assertThat(firmwareCampaign1.getDeviceGroup()).isEqualTo(deviceGroup);
        assertThat(firmwareCampaign1.getDeviceType()).isEqualTo(deviceType1);
        assertThat(firmwareCampaign1.getUploadPeriodStart()).isEqualTo(activationStart);
        assertThat(firmwareCampaign1.getUploadPeriodEnd()).isEqualTo(activationEnd);
        assertThat(firmwareCampaign1.getFirmwareType()).isEqualTo(firmwareType);
        assertThat(firmwareCampaign1.getFirmwareManagementOption()).isEqualTo(activationOption);
        assertThat(firmwareCampaign1.getValidationTimeout()).isEqualTo(timeValidation);
    }

    @Test
    @Transactional
    public void editCampaignTest() {
        FirmwareCampaignDomainExtension firmwareCampaign1 = makeDefaultCampaign();
        firmwareCampaign1.setName("c-2");
        firmwareCampaign1.setUploadPeriodStart(Instant.ofEpochSecond(1544450000));
        firmwareCampaign1.setUploadPeriodEnd(Instant.ofEpochSecond(1544460000));
        firmwareCampaign1.update();
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(FirmwareCampaignDomainExtension.class).get().getName()).isEqualTo(firmwareCampaign1.getName());
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(FirmwareCampaignDomainExtension.class).get().getUploadPeriodStart()).isEqualTo(firmwareCampaign1.getUploadPeriodStart());
        assertThat(serviceCallService.getServiceCallFinder().stream().findAny().get()
                .getExtension(FirmwareCampaignDomainExtension.class).get().getUploadPeriodEnd()).isEqualTo(firmwareCampaign1.getUploadPeriodEnd());
    }

    @Test
    @Transactional
    public void getCampaignByNameTest() {
        makeDefaultCampaign();
        assertThat(firmwareCampaignService.getCampaignByName("c-1").get()).isInstanceOf(FirmwareCampaign.class);
    }

    @Test
    @Transactional
    public void cancelCampaignTest() {
        FirmwareCampaign firmwareCampaign = makeDefaultCampaign();
        firmwareCampaign.cancel();
        assertThat(serviceCallService.getServiceCallFinder().find().get(0).getLogs().find().contains("campaign cancelled by user"));
    }

    private FirmwareCampaignDomainExtension makeDefaultCampaign() {
        return makeCampaign(null, 0, 0, null, null, null, null, null, null, 0);
    }

    private FirmwareCampaignDomainExtension makeCampaign(String name, long activationStart, long activationEnd, FirmwareType firmwareType,
                                                         String group, DeviceType deviceType, String updateType,
                                                         ProtocolSupportedFirmwareOptions activationOption, Instant activationDate, long timeValidation) {
        FirmwareCampaignDomainExtension firmwareCampaign = new FirmwareCampaignDomainExtension(thesaurus, firmwareService);
        firmwareCampaign.setName(name == null ? "c-1" : name);
        firmwareCampaign.setUploadPeriodStart(Instant.ofEpochSecond(activationStart == 0 ? 1544400000 : activationStart));
        firmwareCampaign.setUploadPeriodEnd(Instant.ofEpochSecond(activationEnd == 0 ? 1544410000 : activationEnd));
        firmwareCampaign.setFirmwareType(firmwareType == null ? FirmwareType.METER : firmwareType);
        firmwareCampaign.setDeviceGroup(group == null ? "group1" : group);
        firmwareCampaign.setDeviceType(deviceType == null ? deviceConfigurationService.newDeviceType("Elster AS1440", deviceProtocolPluggableClass) : deviceType);
        firmwareCampaign.setManagementOption(activationOption == null ? ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE : activationOption);
        firmwareCampaign.setActivationDate(activationDate == null ? clock.instant() : activationDate);
        firmwareCampaign.setValidationTimeout(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES));
        firmwareCampaignService.createServiceCallAndTransition(firmwareCampaign);
        return firmwareCampaign;
    }
}
