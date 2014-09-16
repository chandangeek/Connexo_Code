package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.jayway.jsonpath.JsonModel;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class ExecutionLevelResourceTest extends JerseyTest {

    private final String DUMMY_THESAURUS_STRING = "";
    @Mock
    private MasterDataService masterDataService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EngineModelService engineModelService;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private UserService userService;
    @Mock
    private MdcPropertyUtils mdcPropertyUtils;
    @Mock
    private PropertyUtils propertyUtils;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(masterDataService, protocolPluggableService, engineModelService, deviceDataService, validationService);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeeds messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            return (String) invocationOnMock.getArguments()[1];
        });
        NlsMessageFormat nlsFormat = mock(NlsMessageFormat.class);
        when(nlsFormat.format(anyObject())).thenReturn("xxx");
        when(thesaurus.getFormat(any(MessageSeeds.class))).thenAnswer(invocationOnMock -> new NlsMessageFormat() {
            @Override
            public String format(Object... args) {
                return new MessageFormat(((MessageSeeds)invocationOnMock.getArguments()[0]).getDefaultFormat()).format(args);
            }

            @Override
            public String format(Locale locale, Object... args) {
                return null;
            }
        });
    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ResourceHelper.class,
                DeviceTypeResource.class,
                DeviceConfigurationResource.class,
                SecurityPropertySetResource.class,
                ExecutionLevelResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(masterDataService).to(MasterDataService.class);
                bind(validationService).to(ValidationService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(nlsService).to(NlsService.class);
                bind(userService).to(UserService.class);
                bind(ResourceHelper.class).to(ResourceHelper.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
                bind(SecurityPropertySetInfoFactory.class).to(SecurityPropertySetInfoFactory.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(PropertyUtils.class).to(PropertyUtils.class);
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
    public void testAddPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        List<String> executionLevels = Arrays.asList(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.name(), DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4.name());
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels").request().post(Entity.json(executionLevels));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        ArgumentCaptor<DeviceSecurityUserAction> argumentCaptor = ArgumentCaptor.forClass(DeviceSecurityUserAction.class);
        verify(securityPropertySet, times(2)).addUserAction(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().get(0)).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        assertThat(argumentCaptor.getAllValues().get(1)).isEqualTo(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4);
    }

    @Test
    public void testAddUnknownPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        List<String> executionLevels = Arrays.asList("UNKOWN", DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4.name());
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels").request().post(Entity.json(executionLevels));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(securityPropertySet, never()).addUserAction(anyObject());
        JsonModel jsonModel = JsonModel.create(response.readEntity(String.class));
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(Boolean.FALSE);
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("No such execution levels: UNKOWN");
        assertThat(jsonModel.<String>get("$.error")).isEqualTo("NoSuchExecutionLevels");
    }

    @Test
    public void testDeletePrivilegeFromSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/ALLOWCOMTASKEXECUTION3").request().delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        ArgumentCaptor<DeviceSecurityUserAction> argumentCaptor = ArgumentCaptor.forClass(DeviceSecurityUserAction.class);
        verify(securityPropertySet, times(1)).removeUserAction(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION3);
    }

    @Test
    public void testGetAvailableExecutionLevelsForVirginSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").queryParam("available", true).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels")).hasSize(12);
    }

    @Test
    public void testGetAvailableExecutionLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));
        when(securityPropertySet.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION3, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").queryParam("available", true).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels[*].id"))
                .hasSize(8)
                .containsExactly(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.name(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2.name(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3.name(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4.name(),
                DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1.name(), DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2.name(), DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3.name(), DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4.name());
        assertThat(jsonModel.<List>get("$.executionLevels[*].name")).containsExactly("Edit device security properties (level 1)", "Edit device security properties (level 2)", "Edit device security properties (level 3)", "Edit device security properties (level 4)",
                "View device security properties (level 1)", "View device security properties (level 2)", "View device security properties (level 3)", "View device security properties (level 4)");
    }
    @Test
    public void testGetExecutionLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));
        when(securityPropertySet.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION3, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels[*].id"))
                .hasSize(4)
                .containsExactly(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1.name(), DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2.name(), DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION3.name(), DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION4.name());
        assertThat(jsonModel.<List>get("$.executionLevels[*].name")).containsExactly("Execute com task (level 1)", "Execute com task (level 2)", "Execute com task (level 3)", "Execute com task (level 4)");
    }


}
