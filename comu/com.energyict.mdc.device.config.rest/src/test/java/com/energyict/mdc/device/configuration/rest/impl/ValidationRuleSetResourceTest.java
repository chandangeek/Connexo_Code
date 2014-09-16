package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationRuleSetResourceTest extends DeviceConfigurationJerseyTest {

    public static final long DEVICE_TYPE_ID = 564L;
    public static final long DEVICE_CONFIGURATION_ID = 211L;
    public static final long RULESET_ID_1 = 1L;
    public static final long RULESET_ID_2 = 2L;
    @Mock
    private ValidationRuleSet validationRuleSet1, validationRuleSet2;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private ValidationRule rule1, rule2;
    @Mock
    private PropertyUtils propertyUtils;

    @Test
    public void testAddRuleSetsToDeviceConfiguration() throws Exception {
        when(validationService.getValidationRuleSet(RULESET_ID_1)).thenReturn(Optional.of(validationRuleSet1));
        when(validationService.getValidationRuleSet(RULESET_ID_2)).thenReturn(Optional.of(validationRuleSet2));
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID)).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(propertyUtils.convertPropertySpecsToPropertyInfos(anyList(), anyMap())).thenReturn(Collections.<PropertyInfo>emptyList());

        Invocation.Builder all = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/").queryParam("all", Boolean.FALSE).request();
        Response response = all.post(Entity.json(Arrays.asList(RULESET_ID_1, RULESET_ID_2)));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testAddAllRuleSetsToDeviceConfiguration() throws Exception {
        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(validationRuleSet1, validationRuleSet2));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType1, readingType2));
        when(validationRuleSet1.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Arrays.asList(rule1));
        when(validationRuleSet2.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Arrays.asList(rule2));
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID)).thenReturn(deviceType);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        Invocation.Builder all = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/").queryParam("all", Boolean.TRUE).request();
        Response response = all.post(Entity.json(Collections.emptyList()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testAddAllRuleSetsToDeviceConfigurationWithoutNonMatching() throws Exception {
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID)).thenReturn(deviceType);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(validationRuleSet1, validationRuleSet2));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType1, readingType2));
        when(validationRuleSet1.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Collections.<ValidationRule>emptyList());
        when(validationRuleSet2.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Arrays.asList(rule2));

        Invocation.Builder all = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/").queryParam("all", Boolean.TRUE).request();
        Response response = all.post(Entity.json(Collections.emptyList()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceConfiguration, never()).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }


}