package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import com.google.common.collect.Sets;

import javax.inject.Provider;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCategoryImplTest {

    private ServiceCategoryImpl serviceCategory;
    private Clock clock = Clock.systemUTC();

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EventService eventService;
    @Mock
    private Provider<MeterActivationImpl> meterActivationFactory;
    @Mock
    private Provider<UsagePointAccountabilityImpl> accountabilityFactory;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ServerDataAggregationService dataAggregationService;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator validator;
    @Mock
    private MeteringService meteringService;
    @Mock
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Before
    public void setUp() {
        serviceCategory = new ServiceCategoryImpl(dataModel, clock, thesaurus).init(ServiceKind.ELECTRICITY);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.emptySet());
    }

    @Test
    public void testCreatingRemembersKind() {
        assertThat(serviceCategory.getKind()).isEqualTo(ServiceKind.ELECTRICITY);
    }

    @Test
    public void testGetAliasName() {
        String alias = "alias";
        serviceCategory.setAliasName(alias);

        assertThat(serviceCategory.getAliasName()).isEqualTo(alias);
    }

    @Test
    public void testGetDescription() {
        String description = "description";
        serviceCategory.setDescription(description);

        assertThat(serviceCategory.getDescription()).isEqualTo(description);
    }

    @Test
    public void testGetId() {
        assertThat(serviceCategory.getId()).isEqualTo(ServiceKind.ELECTRICITY.ordinal() + 1);
    }

    @Test
    public void testPersist() {
        serviceCategory.persist();
        verify(dataModel).persist(serviceCategory);
    }

    @Test
    public void testNewUsagePoint() {
        when(dataModel.getInstance(UsagePointImpl.class)).thenReturn(new UsagePointImpl(clock, dataModel, eventService, thesaurus, () -> null, () -> null, customPropertySetService, metrologyConfigurationService, dataAggregationService, usagePointLifeCycleConfigurationService));
        when(dataModel.getInstance(UsagePointConnectionStateImpl.class)).thenAnswer(invocation -> new UsagePointConnectionStateImpl());
        UsagePointState usagePointState = mock(UsagePointState.class);
        when(usagePointState.isInitial()).thenReturn(true);
        UsagePointLifeCycle usagePointLifeCycle = mock(UsagePointLifeCycle.class);
        when(usagePointLifeCycle.getStates()).thenReturn(Collections.singletonList(usagePointState));
        UsagePointLifeCycleConfigurationService lifeCycleConfigurationService = mock(UsagePointLifeCycleConfigurationService.class);
        when(lifeCycleConfigurationService.getDefaultLifeCycle()).thenReturn(usagePointLifeCycle);
        when(dataModel.getInstance(UsagePointLifeCycleConfigurationService.class)).thenReturn(lifeCycleConfigurationService);
        when(dataModel.getInstance(UsagePointStateTemporalImpl.class)).thenReturn(new UsagePointStateTemporalImpl(dataModel));
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrId", Instant.EPOCH).create();
        assertThat(usagePoint).isInstanceOf(UsagePointImpl.class);
        assertThat(usagePoint.getName()).isEqualTo("mrId");
        assertThat(usagePoint.getInstallationTime()).isEqualTo(Instant.EPOCH);
    }

    @Test
    public void testCustomPropertySet() {
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();

        ServiceCategoryCustomPropertySetUsage usage = new ServiceCategoryCustomPropertySetUsage();

        when(dataModel.getInstance(ServiceCategoryCustomPropertySetUsage.class)).thenReturn(usage);

        serviceCategory.addCustomPropertySet(registeredCustomPropertySet);

        assertThat(serviceCategory.getCustomPropertySets()).isNotEmpty();
        assertThat(serviceCategory.getCustomPropertySets()).contains(registeredCustomPropertySet);
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
