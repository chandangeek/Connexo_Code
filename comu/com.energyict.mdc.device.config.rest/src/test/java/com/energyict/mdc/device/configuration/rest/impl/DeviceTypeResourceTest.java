package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceTypeResourceTest extends JerseyTest {

    private static DeviceConfigurationService deviceConfigurationService;
    private static ProtocolPluggableService protocolPluggableService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        protocolPluggableService = mock(ProtocolPluggableService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(deviceConfigurationService, protocolPluggableService);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(DeviceTypeResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
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
    public void testGetEmptyDeviceTypeList() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List)map.get("deviceTypes")).isEmpty();
    }

    @Test
    public void testGetNonExistingDeviceType() throws Exception {
        Response response = target("/devicetypes/12345").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllDeviceTypesWithoutPaging() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66), mockDeviceType("device type 2", 66), mockDeviceType("device type 3", 66), mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List)map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testGetAllDeviceTypesWithFullPage() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66), mockDeviceType("device type 2", 66), mockDeviceType("device type 3", 66), mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 0).queryParam("limit", 4).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List)map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testGetEmptyDeviceTypeListPaged() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 100).queryParam("limit", 20).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(100);
        assertThat((List)map.get("deviceTypes")).isEmpty();
        ArgumentCaptor<QueryParameters> queryParametersArgumentCaptor = ArgumentCaptor.forClass(QueryParameters.class);
        verify(finder).from(queryParametersArgumentCaptor.capture());
        assertThat(queryParametersArgumentCaptor.getValue().getStart()).isEqualTo(100);
        assertThat(queryParametersArgumentCaptor.getValue().getLimit()).isEqualTo(20);
    }

    @Test
    public void testGetDeviceTypeByName() throws Exception {
        String webRTUKP = "WebRTUKP";
        DeviceType deviceType = mockDeviceType(webRTUKP, 66);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(deviceType);

        Map<String, Object> map = target("/devicetypes/66").request().get(Map.class);
        assertThat(map.get("name")).isEqualTo(webRTUKP);
    }

    private DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        return deviceType;
    }

    @Test
    public void testDeviceTypeInfoJavaScriptMappings() throws Exception {
        int NUMBER_OF_CONFIGS = 4;
        int NUMBER_OF_LOADPROFILES = 6;
        int NUMBER_OF_REGISTERS = 8;
        int NUMBER_OF_LOGBOOKS = 10;

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("unique name");
        when(deviceType.getId()).thenReturn(13L);
        List configsList = mock(List.class);
        when(configsList.size()).thenReturn(NUMBER_OF_CONFIGS);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(NUMBER_OF_LOADPROFILES);
        List registerList = Arrays.asList(new RegisterMappingInfo(), new RegisterMappingInfo(), new RegisterMappingInfo(), new RegisterMappingInfo(), new RegisterMappingInfo(), new RegisterMappingInfo(), new RegisterMappingInfo(), new RegisterMappingInfo());
        List logBooksList = mock(List.class);
        when(logBooksList.size()).thenReturn(NUMBER_OF_LOGBOOKS);

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getConfigurations()).thenReturn(configsList);
        when(deviceType.getLoadProfileTypes()).thenReturn(loadProfileList);
        when(deviceType.getLogBookTypes()).thenReturn(logBooksList);
        when(deviceType.getRegisterMappings()).thenReturn(registerList);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);

        Finder<DeviceType> finder = mockFinder(Arrays.asList(deviceType));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List)map.get("deviceTypes")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceType = (Map) ((List) map.get("deviceTypes")).get(0);
        assertThat(jsonDeviceType.get("id")).isEqualTo(13).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("logBookCount")).isEqualTo(NUMBER_OF_LOGBOOKS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("registerCount")).isEqualTo(NUMBER_OF_REGISTERS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceConfigurationCount")).isEqualTo(NUMBER_OF_CONFIGS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("loadProfileCount")).isEqualTo(NUMBER_OF_LOADPROFILES).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("name")).isEqualTo("unique name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeGateway")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeDirectlyAddressed")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("communicationProtocolName")).isEqualTo("device protocol name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.containsKey("registerTypes")).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testDeviceConfigurationInfoJavaScriptMappings() throws Exception {

        DeviceType deviceType = mock(DeviceType.class);
        List registerList = mock(List.class);
        when(registerList.size()).thenReturn(2);
        List logBookList = mock(List.class);
        when(logBookList.size()).thenReturn(3);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(4);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(113L);
        when(deviceConfiguration.getName()).thenReturn("defcon");
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(deviceConfiguration.getDescription()).thenReturn("describe me");
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(registerList);
        when(deviceConfiguration.getLoadProfileSpecs()).thenReturn(loadProfileList);
        when(deviceConfiguration.getLogBookSpecs()).thenReturn(logBookList);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.METER);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);

        Finder<DeviceConfiguration> finder = mockFinder(deviceType.getConfigurations());
        when(deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(any(DeviceType.class))).thenReturn(finder);
        when(deviceConfigurationService.findDeviceType(6)).thenReturn(deviceType);

        Map<String, Object> map = target("/devicetypes/6/deviceconfigurations").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List)map.get("deviceConfigurations")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceConfiguration = (Map) ((List) map.get("deviceConfigurations")).get(0);
        assertThat(jsonDeviceConfiguration).hasSize(9);
        assertThat(jsonDeviceConfiguration.get("id")).isEqualTo(113).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("name")).isEqualTo("defcon").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("active")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("description")).isEqualTo("describe me").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("communicationProtocolName")).isEqualTo("device protocol name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("deviceFunction")).isEqualTo("Meter").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("registerCount")).isEqualTo(2).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("logBookCount")).isEqualTo(3).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("loadProfileCount")).isEqualTo(4).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testRegisterTypesInfoJavaScriptMappings() throws Exception {

        DeviceType deviceType = mock(DeviceType.class);
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.METER);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        ReadingType readingType = mock(ReadingType.class);
        when(registerMapping.getReadingType()).thenReturn(readingType);

        List<RegisterSpec> registerSpecs = mock(List.class);
        when(registerSpecs.size()).thenReturn(1);
        when(deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterMapping(deviceType, registerMapping)).thenReturn(registerSpecs);
        when(deviceConfigurationService.findDeviceType(6)).thenReturn(deviceType);

        Map<String, Object> map = target("/devicetypes/6/registertypes").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed").isEqualTo(1);
        assertThat((List)map.get("registerTypes")).hasSize(1).describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceConfiguration = (Map) ((List) map.get("registerTypes")).get(0);
        assertThat(jsonDeviceConfiguration).containsKey("isLinkedByActiveRegisterConfig").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration).containsKey("isLinkedByInactiveRegisterConfig").describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testUnlinkSingleNonExistingRegisterMappingFromDeviceType() throws Exception {
        // Backend has RM 101, UI wants to remove 102
        long RM_ID_1 = 101L;

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(deviceConfigurationService.findRegisterMapping(RM_ID_1)).thenReturn(registerMapping101);

        Response response = target("/devicetypes/31/registertypes/102").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testUpdateRegistersWithoutChanges() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101 and 102: no changes
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterMappingInfo registerMappingInfo1 = new RegisterMappingInfo();
        registerMappingInfo1.id=RM_ID_1;
        registerMappingInfo1.name="mapping 1";
        registerMappingInfo1.obisCode=new ObisCode(1,11,2,12,3,13);
        RegisterMappingInfo registerMappingInfo2 = new RegisterMappingInfo();
        registerMappingInfo2.id=RM_ID_2;
        registerMappingInfo2.name="mapping 2";
        registerMappingInfo2.obisCode=new ObisCode(11,111,12,112,13,113);

        DeviceType deviceType = mockDeviceType("updater", 31L);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101, registerMapping102));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(Collections.<DeviceProtocolPluggableClass>emptyList());

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerMappings=Arrays.asList(registerMappingInfo1, registerMappingInfo2);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing added
        verify(deviceType, never()).removeRegisterMapping(any(RegisterMapping.class));
        verify(deviceType, never()).addRegisterMapping(any(RegisterMapping.class));
    }

    @Test
    public void testUpdateRegistersAddOneRegister() throws Exception {
        // Backend has RM 101, UI sets for 101 and 102: 102 should be added
        RegisterMappingInfo registerMappingInfo1 = new RegisterMappingInfo();
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        registerMappingInfo1.id= RM_ID_1;
        registerMappingInfo1.name="mapping 1";
        registerMappingInfo1.obisCode=new ObisCode(1,11,2,12,3,13);
        RegisterMappingInfo registerMappingInfo2 = new RegisterMappingInfo();
        registerMappingInfo2.id=RM_ID_2;
        registerMappingInfo2.name="mapping 2";
        registerMappingInfo2.obisCode=new ObisCode(11,111,12,112,13,113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(deviceConfigurationService.findRegisterMapping(RM_ID_1)).thenReturn(registerMapping101);
        when(deviceConfigurationService.findRegisterMapping(RM_ID_2)).thenReturn(registerMapping102);
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(Collections.<DeviceProtocolPluggableClass>emptyList());

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerMappings=Arrays.asList(registerMappingInfo1, registerMappingInfo2);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceType, never()).removeRegisterMapping(any(RegisterMapping.class));
        verify(deviceType).addRegisterMapping(registerMapping102);
    }

    @Test
    public void testUpdateDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(deviceType);

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name="new name";
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration101).setName("new name");
    }

    @Test
    public void testDeleteDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(deviceType);

        Response response = target("/devicetypes/31/deviceconfigurations/101").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceType).removeConfiguration(deviceConfiguration101);
    }

    @Test
    public void testDeleteNonExistingDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        when(deviceType.getConfigurations()).thenReturn(new ArrayList<DeviceConfiguration>());
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(deviceType);

        Response response = target("/devicetypes/31/deviceconfigurations/101").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testActivateDeviceConfigurationIgnoresOtherProperties() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.isActive()).thenReturn(false);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);
        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.isActive()).thenReturn(false);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(deviceType);

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name="new name";
        deviceConfigurationInfo.active=true;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration101, never()).setName(anyString());
        verify(deviceConfiguration101, times(1)).activate();
    }

    @Test
    public void testUpdateRegistersAddNoneExistingRegisterMapping() throws Exception {
        // Backend has RM 101, UI sets for 101 and 102: 102 should be added but does not exist
        RegisterMappingInfo registerMappingInfo1 = new RegisterMappingInfo();
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        registerMappingInfo1.id= RM_ID_1;
        registerMappingInfo1.name="mapping 1";
        registerMappingInfo1.obisCode=new ObisCode(1,11,2,12,3,13);
        RegisterMappingInfo registerMappingInfo2 = new RegisterMappingInfo();
        registerMappingInfo2.id=RM_ID_2;
        registerMappingInfo2.name="mapping 2";
        registerMappingInfo2.obisCode=new ObisCode(11,111,12,112,13,113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(deviceConfigurationService.findRegisterMapping(RM_ID_1)).thenReturn(registerMapping101);
        when(deviceConfigurationService.findRegisterMapping(RM_ID_2)).thenReturn(null);
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(Collections.<DeviceProtocolPluggableClass>emptyList());

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerMappings=Arrays.asList(registerMappingInfo1, registerMappingInfo2);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testUpdateRegistersDeleteOneMapping() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101: delete 102
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterMappingInfo registerMappingInfo1 = new RegisterMappingInfo();
        registerMappingInfo1.id=RM_ID_1;
        registerMappingInfo1.name="mapping 1";
        registerMappingInfo1.obisCode=new ObisCode(1,11,2,12,3,13);
        RegisterMappingInfo registerMappingInfo2 = new RegisterMappingInfo();
        registerMappingInfo2.id=RM_ID_2;
        registerMappingInfo2.name="mapping 2";
        registerMappingInfo2.obisCode=new ObisCode(11,111,12,112,13,113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101, registerMapping102));
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(deviceConfigurationService.findRegisterMapping(RM_ID_1)).thenReturn(registerMapping101);
        when(deviceConfigurationService.findRegisterMapping(RM_ID_2)).thenReturn(registerMapping102);

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerMappings=Arrays.asList(registerMappingInfo1);
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing updated
        verify(deviceType).removeRegisterMapping(registerMapping102);
        verify(deviceType, never()).addRegisterMapping(any(RegisterMapping.class));
    }

    @Test
    public void testRegistersForDeviceTypeWithoutFilterAreSorted() throws Exception {
        // Backend has RM 101 and 102 for device type 31
        long deviceType_id=31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        ReadingType readingType = mock(ReadingType.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        when(registerMapping101.getReadingType()).thenReturn(readingType);
        when(registerMapping101.getName()).thenReturn("zzz");
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn(RM_ID_2);
        when(registerMapping102.getReadingType()).thenReturn(readingType);
        when(registerMapping102.getName()).thenReturn("aaa");
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101, registerMapping102));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(deviceType);

        Map response = target("/devicetypes/31/registertypes").request().get(Map.class);
        assertThat(response).hasSize(2);
        List<Map> registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
        assertThat(registerTypes.get(0).get("name")).isEqualTo("aaa");
        assertThat(registerTypes.get(1).get("name")).isEqualTo("zzz");
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_Filtered() throws Exception {
        // Backend has RM 101 for device type 31, while 101, 102 and 103 exist in the server.
        long deviceType_id=31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        long RM_ID_3 = 103L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = mock(ReadingType.class);
        when(registerMapping101.getReadingType()).thenReturn(readingType);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn(RM_ID_2);
        when(registerMapping102.getReadingType()).thenReturn(readingType);
        RegisterMapping registerMapping103 = mock(RegisterMapping.class);
        when(registerMapping103.getId()).thenReturn(RM_ID_3);
        when(registerMapping103.getReadingType()).thenReturn(readingType);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(deviceType);
        Finder<RegisterMapping> registerMappingFinder = mockFinder(Arrays.asList(registerMapping101, registerMapping102, registerMapping103));
        when(deviceConfigurationService.findAllRegisterMappings()).thenReturn(registerMappingFinder);

        Map response = target("/devicetypes/31/registertypes").queryParam("available","true").request().get(Map.class);
        assertThat(response).hasSize(2);
        List registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
    }

    @Test
    public void testGetDeviceCommunicationById() throws Exception {
        long deviceType_id=41;
        long deviceConfiguration_id=14;
        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        DeviceConfiguration mock1 = mock(DeviceConfiguration.class);
        when(mock1.getId()).thenReturn(deviceConfiguration_id+1);
        DeviceConfiguration mock2 = mock(DeviceConfiguration.class);
        when(mock2.getId()).thenReturn(deviceConfiguration_id+2);
        DeviceConfiguration mock3 = mock(DeviceConfiguration.class);
        when(mock3.getDeviceType()).thenReturn(deviceType);
        when(mock3.getId()).thenReturn(deviceConfiguration_id);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(mock1, mock2, mock3));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(deviceType);
        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetDeviceCommunicationByIdButNoSuchConfigOnTheDevice() throws Exception {
        long deviceType_id=41;
        long deviceConfiguration_id=14;
        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        DeviceConfiguration mock1 = mock(DeviceConfiguration.class);
        when(mock1.getId()).thenReturn(deviceConfiguration_id+1);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(mock1));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(deviceType);
        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetDeviceCommunicationByIdWithNonExistingDeviceType() throws Exception {
        long deviceType_id=41;
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(null);
        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }

}
