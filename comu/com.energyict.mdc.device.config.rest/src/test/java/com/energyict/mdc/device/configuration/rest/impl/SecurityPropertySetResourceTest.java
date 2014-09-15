package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.assertj.core.data.MapEntry;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetResourceTest extends JerseyTest {

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
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                for (MessageSeeds messageSeeds : MessageSeeds.values()) {
                    if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                        return messageSeeds.getDefaultFormat();
                    }
                }
                return (String) invocationOnMock.getArguments()[1];
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
                RegisterConfigurationResource.class,
                SecurityPropertySetResource.class,
                ConnectionMethodResource.class,
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
    public void testGetSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        Group group1 = mockUserGroup(66L, "Z - user group 1");
        Group group2 = mockUserGroup(67L, "A - user group 2");
        Group group3 = mockUserGroup(68L, "O - user group 1");
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", 1, "Auth1", 1001, "Encrypt1", EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2));
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", 2, "Auth2", 1002, "Encrypt2", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        Map response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(Map.class);
        assertThat(response.containsKey("total"));
        assertThat(response.containsKey("data"));
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        Map<String, Object> map = data.get(0);
        assertThat(map).contains(MapEntry.entry("id", 101),
                MapEntry.entry("name", "Primary"),
                MapEntry.entry("authenticationLevelId", 1),
                MapEntry.entry("encryptionLevelId", 1001));
        List<Map<String, Object>> executionLevels = (List<Map<String, Object>>) map.get("executionLevels");
        assertThat(executionLevels.get(0)).contains(MapEntry.entry("id", "execute.com.task.level1"), MapEntry.entry("name","Execute com task (level 1)"));
        assertThat(executionLevels.get(1)).contains(MapEntry.entry("id", "execute.com.task.level2"), MapEntry.entry("name", "Execute com task (level 2)"));
        List<Map<String, Object>> userRoles = (List<Map<String, Object>>) executionLevels.get(0).get("userRoles");
        assertThat(userRoles.get(0)).contains(MapEntry.entry("id", 67), MapEntry.entry("name", "A - user group 2"));
        assertThat(userRoles.get(1)).contains(MapEntry.entry("id", 68), MapEntry.entry("name", "O - user group 1"));
        assertThat(userRoles.get(2)).contains(MapEntry.entry("id", 66), MapEntry.entry("name", "Z - user group 1"));
    }

    @Test
    public void testGetSecurityPropertySetFilteredByPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        Group group1 = mockUserGroup(66L, "Z - user group 1", Arrays.asList(Privileges.EXECUTE_COM_TASK_1));
        Group group2 = mockUserGroup(67L, "A - user group 2", Arrays.asList(Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4));
        Group group3 = mockUserGroup(68L, "O - user group 3", Collections.emptyList());
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", 1, "Auth1", 1001, "Encrypt1", EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2));
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", 2, "Auth2", 1002, "Encrypt2", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        Map response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        Map<String, Object> sps1map = data.get(0);
        List<Map<String, Object>> executionLevels = (List<Map<String, Object>>) sps1map.get("executionLevels");
        List<Map<String, Object>> userRoles1 = (List<Map<String, Object>>) executionLevels.get(0).get("userRoles");
        assertThat(userRoles1.get(0)).containsExactly(MapEntry.entry("id", 66), MapEntry.entry("name", "Z - user group 1"));
        List<Map<String, Object>> userRoles2 = (List<Map<String, Object>>) executionLevels.get(1).get("userRoles");
        assertThat(userRoles2).isEmpty();

        Map<String, Object> sps2map = data.get(1);
        List<Map<String, Object>> executionLevels2 = (List<Map<String, Object>>) sps2map.get("executionLevels");
        List<Map<String, Object>> userRoles1bis = (List<Map<String, Object>>) executionLevels2.get(0).get("userRoles");
        assertThat(userRoles1bis.get(0)).containsExactly(MapEntry.entry("id", 67), MapEntry.entry("name", "A - user group 2"));
        List<Map<String, Object>> userRoles2bis = (List<Map<String, Object>>) executionLevels2.get(1).get("userRoles");
        assertThat(userRoles2bis).isEmpty();

    }

    private Group mockUserGroup(long id, String name) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.<String>anyObject())).thenReturn(true);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private Group mockUserGroup(long id, String name, List<String> privileges) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.<String>anyObject())).then(invocationOnMock -> privileges.contains(invocationOnMock.getArguments()[0]));
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private SecurityPropertySet mockSecurityPropertySet(Long id, String name, Integer authenticationAccessLevelId, String authenticationAccessLevelName, Integer encryptionAccessLevelId, String encryptionAccessLevelName, Set<DeviceSecurityUserAction> userAction) {
        SecurityPropertySet mock = mock(SecurityPropertySet.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(authenticationAccessLevelId);
        when(authenticationAccessLevel.getTranslationKey()).thenReturn(authenticationAccessLevelName);
        when(mock.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationAccessLevel);
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(encryptionAccessLevelId);
        when(encryptionAccessLevel.getTranslationKey()).thenReturn(encryptionAccessLevelName);
        when(mock.getEncryptionDeviceAccessLevel()).thenReturn(encryptionAccessLevel);
        when(mock.getUserActions()).thenReturn(userAction);
        return mock;
    }
}
