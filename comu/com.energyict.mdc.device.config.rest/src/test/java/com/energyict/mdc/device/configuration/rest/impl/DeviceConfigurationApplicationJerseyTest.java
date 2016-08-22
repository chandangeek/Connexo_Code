package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.calendar.rest.impl.CalendarInfoFactoryImpl;
import com.elster.jupiter.cbo.*;
import com.elster.jupiter.cps.*;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.mockito.Mock;

import javax.ws.rs.core.Application;

import java.util.Arrays;
import java.util.Currency;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceConfigurationApplicationJerseyTest extends FelixRestApplicationJerseyTest {
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
    ProtocolPluggableService protocolPluggableService;
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

    @Before
    public void setup() {
        when(thesaurus.getStringBeyondComponent(any(String.class), any(String.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
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
        return application;
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
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.<ReadingType>empty());
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
}