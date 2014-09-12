package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationRuleSetResourceTest extends JerseyTest {

    public static final long DEVICE_TYPE_ID = 564L;
    public static final long DEVICE_CONFIGURATION_ID = 211L;
    public static final long RULESET_ID_1 = 1L;
    public static final long RULESET_ID_2 = 2L;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private MasterDataService masterDataService;
    @Mock
    private ValidationService validationService;
    @Mock
    private Thesaurus thesaurus;
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

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ResourceHelper.class,
                DeviceTypeResource.class,
                DeviceConfigurationResource.class,
                ValidationRuleSetResource.class,
                RegisterConfigurationResource.class,
                ConnectionMethodResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(ResourceHelper.class).to(ResourceHelper.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(validationService).to(ValidationService.class);
                bind(masterDataService).to(MasterDataService.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(thesaurus).to(Thesaurus.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

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