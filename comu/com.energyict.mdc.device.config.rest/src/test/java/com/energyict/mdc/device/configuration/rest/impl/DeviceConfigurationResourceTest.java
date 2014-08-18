package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationResourceTest {

    public static final long DEVICE_TYPE_ID = 564L;
    public static final long DEVICE_CONFIGURATION_ID = 211L;
    public static final long RULESET_ID_1 = 1L;
    public static final long RULESET_ID_2 = 2L;

    private DeviceConfigurationResource deviceConfigurationResource;
    private MultivaluedMap<String, String> map = new MultivaluedHashMap<>();

    @Mock
    private ResourceHelper resourceHelper;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private Provider<RegisterConfigurationResource> registerConfigurationResourceProvider;
    @Mock
    private Provider<ConnectionMethodResource> conectionMethodResourceProvider;
    @Mock
    private Provider<ProtocolDialectResource> protocoolDialectResourceProvider;
    @Mock
    private Provider<LoadProfileConfigurationResource> loadProfileConfigurationResourceProvider;
    @Mock
    private Provider<SecurityPropertySetResource> securitySetResourceProvider;
    @Mock
    private Provider<ComTaskEnablementResource> comTaskEnablementResourceProvider;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private UriInfo uriInfo;
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

    @Before
    public void setUp() {
        deviceConfigurationResource = new DeviceConfigurationResource(resourceHelper, deviceConfigurationService, validationService,
                registerConfigurationResourceProvider, conectionMethodResourceProvider, protocoolDialectResourceProvider,
                loadProfileConfigurationResourceProvider, securitySetResourceProvider, comTaskEnablementResourceProvider,
                thesaurus);
        when(uriInfo.getQueryParameters()).thenReturn(map);
        when(validationService.getValidationRuleSet(RULESET_ID_1)).thenReturn(Optional.of(validationRuleSet1));
        when(validationService.getValidationRuleSet(RULESET_ID_2)).thenReturn(Optional.of(validationRuleSet2));
        when(resourceHelper.findDeviceTypeByIdOrThrowException(DEVICE_TYPE_ID)).thenReturn(deviceType);
        when(resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, DEVICE_CONFIGURATION_ID)).thenReturn(deviceConfiguration);
        when(propertyUtils.convertPropertySpecsToPropertyInfos(anyList(), anyMap())).thenReturn(Collections.<PropertyInfo>emptyList());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAddRuleSetsToDeviceConfiguration() throws Exception {
        deviceConfigurationResource.addRuleSetsToDeviceConfiguration(DEVICE_TYPE_ID, DEVICE_CONFIGURATION_ID, Arrays.asList(RULESET_ID_1, RULESET_ID_2), uriInfo);

        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testAddAllRuleSetsToDeviceConfiguration() throws Exception {
        map.add("all", Boolean.TRUE.toString());

        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(validationRuleSet1, validationRuleSet2));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType1, readingType2));
        when(validationRuleSet1.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Arrays.asList(rule1));
        when(validationRuleSet2.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Arrays.asList(rule2));

        deviceConfigurationResource.addRuleSetsToDeviceConfiguration(DEVICE_TYPE_ID, DEVICE_CONFIGURATION_ID, Collections.<Long>emptyList(), uriInfo);

        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testAddAllRuleSetsToDeviceConfigurationWithoutNonMatching() throws Exception {
        map.add("all", Boolean.TRUE.toString());

        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(validationRuleSet1, validationRuleSet2));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType1, readingType2));
        when(validationRuleSet1.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Collections.<ValidationRule>emptyList());
        when(validationRuleSet2.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Arrays.asList(rule2));

        deviceConfigurationResource.addRuleSetsToDeviceConfiguration(DEVICE_TYPE_ID, DEVICE_CONFIGURATION_ID, Collections.<Long>emptyList(), uriInfo);

        verify(deviceConfiguration, never()).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }


}