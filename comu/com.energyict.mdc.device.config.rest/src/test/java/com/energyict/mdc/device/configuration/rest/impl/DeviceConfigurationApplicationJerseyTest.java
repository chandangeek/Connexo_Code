/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.impl.CalendarInfoFactoryImpl;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.AliasSearchFilterFactory;
import com.elster.jupiter.pki.rest.SecurityAccessorResourceHelper;
import com.elster.jupiter.pki.rest.impl.AliasSearchFilterFactoryImpl;
import com.elster.jupiter.pki.rest.impl.SecurityAccessorResourceHelperImpl;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.impl.PropertyValueInfoServiceImpl;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.services.ObisCodeDescriptor;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.configuration.rest.TrustStoreValuesProvider;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.impl.MdcPropertyUtilsImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLRequestSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLResponseSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLSecuritySuiteLevelAdapter;
import com.energyict.mdc.tasks.TaskService;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Sets;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceConfigurationApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    static ProtocolPluggableService protocolPluggableService;

    @Mock
    MeteringService meteringService;
    @Mock
    MasterDataService masterDataService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    KpiService kpiService;
    @Mock
    ValidationService validationService;
    @Mock
    EstimationService estimationService;
    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    DeviceService deviceService;
    @Mock
    UserService userService;
    @Mock
    JsonService jsonService;
    @Mock
    MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @Mock
    TaskService taskService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    FirmwareService firmwareService;
    @Mock
    DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    CalendarService calendarService;
    @Mock
    ObisCodeDescriptor obisCodeDescriptor;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    IssueService issueService;
    PropertyValueInfoService propertyValueInfoService;
    MdcPropertyUtils mdcPropertyUtils;
    SecurityAccessorResourceHelper securityAccessorResourceHelper;
    SecurityAccessorInfoFactory securityAccessorInfoFactory;
    TrustStoreValuesProvider trustStoreValuesProvider;
    AliasSearchFilterFactory aliasSearchFilterFactory;

    ReadingTypeInfoFactory readingTypeInfoFactory;
    RegisterConfigInfoFactory registerConfigInfoFactory;
    RegisterTypeInfoFactory registerTypeInfoFactory;
    RegisterGroupInfoFactory registerGroupInfoFactory;
    LoadProfileTypeOnDeviceTypeInfoFactory loadProfileTypeOnDeviceTypeInfoFactory;

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    @BeforeClass
    public static void before() {
        protocolPluggableService = mock(ProtocolPluggableService.class);
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLAuthenticationLevelAdapter.adaptTo((com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLEncryptionLevelAdapter.adaptTo((com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.SecuritySuite.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLSecuritySuiteLevelAdapter.adaptTo((com.energyict.mdc.upl.security.SecuritySuite) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.RequestSecurityLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLRequestSecurityLevelAdapter.adaptTo((com.energyict.mdc.upl.security.RequestSecurityLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.ResponseSecurityLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLResponseSecurityLevelAdapter.adaptTo((com.energyict.mdc.upl.security.ResponseSecurityLevel) args[0], null);
        });
    }

    @Before
    public void setup() {
        when(obisCodeDescriptor.describe(any(ObisCode.class))).thenReturn("obisCodeDescription");
        readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        registerConfigInfoFactory = new RegisterConfigInfoFactory(readingTypeInfoFactory, obisCodeDescriptor);
        registerTypeInfoFactory = new RegisterTypeInfoFactory(readingTypeInfoFactory);
        registerGroupInfoFactory = new RegisterGroupInfoFactory(registerTypeInfoFactory);
        loadProfileTypeOnDeviceTypeInfoFactory = new LoadProfileTypeOnDeviceTypeInfoFactory(registerTypeInfoFactory);
        this.setupThesaurus();
    }

    protected void setupThesaurus() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        doReturn(messageFormat).when(thesaurus).getFormat(any(MessageSeed.class));
        doReturn(messageFormat).when(thesaurus).getFormat(any(TranslationKey.class));
        doReturn(messageFormat).when(thesaurus).getSimpleFormat(any(MessageSeed.class));
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);
    }

    @Override
    protected Application getApplication() {
        DeviceConfigurationApplication application = new DeviceConfigurationApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setMeteringService(meteringService);
        application.setMasterDataService(masterDataService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setKpiService(kpiService);
        application.setValidationService(validationService);
        application.setEstimationService(estimationService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setEngineConfigurationService(engineConfigurationService);
        application.setDeviceService(deviceService);
        application.setUserService(userService);
        application.setJsonService(jsonService);
        application.setTaskService(taskService);
        application.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setFirmwareService(firmwareService);
        application.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setCalendarInfoFactory(new CalendarInfoFactoryImpl(thesaurus));
        application.setCalendarService(calendarService);
        propertyValueInfoService = createPropertyValueInfoService();
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setObisCodeDescriptor(obisCodeDescriptor);
        application.setSecurityManagementService(securityManagementService);
        mdcPropertyUtils = new MdcPropertyUtilsImpl(propertyValueInfoService, meteringGroupsService);
        application.setMdcPropertyUtils(mdcPropertyUtils);
        securityAccessorResourceHelper = new SecurityAccessorResourceHelperImpl(securityManagementService, propertyValueInfoService);
        application.setSecurityAccessorResourceHelper(securityAccessorResourceHelper);
        securityAccessorInfoFactory = new SecurityAccessorInfoFactoryImpl(mdcPropertyUtils, securityManagementService);
        application.setSecurityAccessorInfoFactory(securityAccessorInfoFactory);
        trustStoreValuesProvider = new TrustStoreValuesProviderImpl(securityManagementService);
        application.setTrustStoreValuesProvider(trustStoreValuesProvider);
        aliasSearchFilterFactory = new AliasSearchFilterFactoryImpl(securityManagementService);
        application.setIssueService(issueService);
        application.setAliasSearchFilterFactory(aliasSearchFilterFactory);
        return application;
    }

    static PropertyValueInfoService createPropertyValueInfoService() {
        PropertyValueInfoServiceImpl propertyValueInfoService = new PropertyValueInfoServiceImpl();
        propertyValueInfoService.activate();
        return propertyValueInfoService;
    }

    static ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getAliasName()).thenReturn("abcde");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        return readingType;
    }

    @SuppressWarnings("unchecked")
    PropertySpec mockPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(BigDecimalFactory.class);
        when(propertySpec.getName()).thenReturn("customAttribute");
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getValueFactory().getValueType()).thenReturn(BigDecimalFactory.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getDescription()).thenReturn("kw");
        return propertySpec;
    }

    PropertySpec mockPropertySpec(String name, ValueFactory valueFactory) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.getDisplayName()).thenReturn(name);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        return propertySpec;
    }

    @SuppressWarnings("unchecked")
    CustomPropertySet mockCustomPropertySet() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("domainExtensionName");
        when(customPropertySet.isRequired()).thenReturn(true);
        when(customPropertySet.isVersioned()).thenReturn(false);
        when(customPropertySet.defaultViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_3));
        when(customPropertySet.defaultEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_4));
        when(customPropertySet.getDomainClass()).thenReturn(BigDecimalFactory.class);
        return customPropertySet;
    }

    @SuppressWarnings("unchecked")
    protected RegisteredCustomPropertySet mockRegisteredCustomPropertySet() {
        PropertySpec propertySpec = mockPropertySpec();
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getId()).thenReturn(100500L);
        when(registeredCustomPropertySet.getViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_1));
        when(registeredCustomPropertySet.getEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_2));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        return registeredCustomPropertySet;
    }

    @SuppressWarnings("unchecked")
    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        when(deviceType.getRegisterTypeTypeCustomPropertySet(anyObject())).thenReturn(Optional.of(registeredCustomPropertySet));
        when(deviceType.getCustomPropertySets()).thenReturn(Arrays.asList(registeredCustomPropertySet));
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceLifeCycle deviceLifeCycle = mockStandardDeviceLifeCycle();
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceType.getVersion()).thenReturn(OK_VERSION);
        when(deviceType.getDeviceIcon()).thenReturn(new byte[0]);

        doReturn(Optional.of(deviceType)).when(deviceConfigurationService).findDeviceType(id);
        doReturn(Optional.of(deviceType)).when(deviceConfigurationService).findAndLockDeviceType(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService).findAndLockDeviceType(id, BAD_VERSION);

        return deviceType;
    }

    protected DeviceLifeCycle mockStandardDeviceLifeCycle() {
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle.getId()).thenReturn(1L);
        when(deviceLifeCycle.getName()).thenReturn("Default");
        return deviceLifeCycle;
    }

    protected <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).then(invocation -> list.stream());
        return finder;
    }

    DeviceConfiguration mockDeviceConfiguration(long id, DeviceType deviceType) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn("Device configuration " + id);
        when(deviceConfiguration.getId()).thenReturn(id);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        deviceType.getConfigurations().add(deviceConfiguration);
        List<RegisterSpec> registerSpec = new ArrayList<>();
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(registerSpec);
        List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(partialConnectionTasks);

        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService).findDeviceConfiguration(id);
        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION);

        return deviceConfiguration;
    }
}
