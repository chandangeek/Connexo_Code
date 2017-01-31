/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DemoUsagePointMeterActivationValidatorTest {

    @Mock
    private DemoUsagePointMeterActivationValidator demoUsagePointMeterActivationValidator;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private Device device;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointCustomPropertySetExtension usagePointCasExtension;
    @Mock
    private UsagePointPropertySet usagePointPropertySet;

    private CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
    @Mock
    private DeviceType deviceType;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    private CustomPropertySet customPropertySet;
    @Mock
    private CustomPropertySet usagePointCustomPropertySet;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private Meter meter;
    @Mock
    private MeterRole meterRole;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private UsagePointRequirement usagePointRequirement;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint;

    @Before
    public void initialize() {
        when(meter.getAmrId()).thenReturn("1");
        when(deviceService.findDeviceById(1)).thenReturn(Optional.of(device));
        when(usagePoint.forCustomProperties()).thenReturn(usagePointCasExtension);
        when(usagePointCasExtension.getAllPropertySets()).thenReturn(Collections.singletonList(usagePointPropertySet));
        when(usagePointPropertySet.getCustomPropertySet()).thenReturn(usagePointCustomPropertySet);

        when(usagePointCustomPropertySet.getId()).thenReturn("com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension");
        customPropertySetValues.setProperty("prepay", true);
        when(usagePointPropertySet.getValues()).thenReturn(customPropertySetValues);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfigurationOnUsagePoint));
        when(metrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyConfiguration.getUsagePointRequirements()).thenReturn(Collections.singletonList(usagePointRequirement));

        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = "prepay";
        valueBean.operator = SearchablePropertyOperator.EQUAL;
        valueBean.values = Collections.singletonList("true");
        when(usagePointRequirement.toValueBean()).thenReturn(valueBean);

        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySet.getName()).thenReturn("MeterSpecs");

        customPropertySetValues.setProperty("meterMechanism", "Credit");
        when(customPropertySetService.getUniqueValuesFor(anyObject(), anyObject())).thenReturn(customPropertySetValues);
    }

    @Test
    public void validateSuccessTest() {
        DemoUsagePointMeterActivationValidator validator = new DemoUsagePointMeterActivationValidator(deviceService, customPropertySetService, thesaurus);
        validator.validateActivation(meterRole, meter, usagePoint);
    }

    @Test(expected = CustomUsagePointMeterActivationValidationException.class)
    public void validateFailureTest() {
        DemoUsagePointMeterActivationValidator validator = new DemoUsagePointMeterActivationValidator(deviceService, customPropertySetService, thesaurus);
        customPropertySetValues.setProperty("meterMechanism", "Prepayment");
        validator.validateActivation(meterRole, meter, usagePoint);
    }

    @Test
    public void validateNoNPEIfThereIsNoActiveVersion() {
        when(usagePointPropertySet.getValues()).thenReturn(null);
        DemoUsagePointMeterActivationValidator validator = new DemoUsagePointMeterActivationValidator(deviceService, customPropertySetService, thesaurus);
        validator.validateActivation(meterRole, meter, usagePoint);
    }

    @Test
    public void validateNoNPEIfThereIsNoPrepayValue() {
        when(usagePointPropertySet.getValues()).thenReturn(CustomPropertySetValues.empty());
        DemoUsagePointMeterActivationValidator validator = new DemoUsagePointMeterActivationValidator(deviceService, customPropertySetService, thesaurus);
        validator.validateActivation(meterRole, meter, usagePoint);
    }
}
