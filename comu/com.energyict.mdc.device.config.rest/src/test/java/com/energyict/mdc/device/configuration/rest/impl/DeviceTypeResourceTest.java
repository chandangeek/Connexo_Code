package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.ShadowList;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.SortOrder;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.amr.RegisterMappingFactory;
import com.energyict.mdw.core.DeviceType;
import com.energyict.mdw.shadow.DeviceTypeShadow;
import com.energyict.mdw.shadow.amr.RegisterMappingShadow;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceTypeResourceTest extends JerseyTest {

    private static DeviceConfigurationService deviceConfigurationService;
    private static RegisterMappingFactory registerMappingFactory;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        registerMappingFactory = mock(RegisterMappingFactory.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(deviceConfigurationService, registerMappingFactory);
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
                bind(registerMappingFactory).to(RegisterMappingFactory.class);
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
        when(deviceConfigurationService.allDeviceTypes()).thenReturn(finder);

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
        when(deviceConfigurationService.allDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List)map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testGetAllDeviceTypesWithFullPage() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66),mockDeviceType("device type 2", 66),mockDeviceType("device type 3", 66),mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.allDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 0).queryParam("limit", 4).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(5);
        assertThat((List)map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testGetEmptyDeviceTypeListPaged() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.allDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 100).queryParam("limit", 20).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List)map.get("deviceTypes")).isEmpty();
        ArgumentCaptor<Integer> startArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> limitArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(finder).paged(startArgumentCaptor.capture(), limitArgumentCaptor.capture());
        assertThat(startArgumentCaptor.getValue()).isEqualTo(100);
        assertThat(limitArgumentCaptor.getValue()).isEqualTo(20);
    }

    @Test
    public void testGetDeviceTypeByName() throws Exception {
        String webRTUKP = "WebRTUKP";
        DeviceType deviceType = mockDeviceType(webRTUKP, 66);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(deviceType);

        Map<String, Object> map = target("/devicetypes/66").request().get(Map.class);
        assertThat(map.get("name")).isEqualTo(webRTUKP);
    }

    private DeviceType mockDeviceType(String name, int id) {
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
        when(deviceType.getId()).thenReturn(13);
        List configsList = mock(List.class);
        when(configsList.size()).thenReturn(NUMBER_OF_CONFIGS);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(NUMBER_OF_LOADPROFILES);
        List registerList = mock(List.class);
        when(registerList.size()).thenReturn(NUMBER_OF_REGISTERS);
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
        when(deviceConfigurationService.allDeviceTypes()).thenReturn(finder);

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

        DeviceType deviceType = mockDeviceType("updater", 31);
        DeviceTypeShadow deviceTypeShadow = mock(DeviceTypeShadow.class);
        RegisterMappingShadow registerMappingShadow1 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow1.getId()).thenReturn((int)RM_ID_1);
        RegisterMappingShadow registerMappingShadow2 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow2.getId()).thenReturn((int)RM_ID_2);
        ShadowList<RegisterMappingShadow> registerMappingShadows = new ShadowList<>();
        registerMappingShadows.basicAdd(registerMappingShadow1);
        registerMappingShadows.basicAdd(registerMappingShadow2);
        when(deviceTypeShadow.getRegisterMappingShadows()).thenReturn(registerMappingShadows);
        when(deviceType.getShadow()).thenReturn(deviceTypeShadow);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn((int)RM_ID_1);
        when(registerMapping101.getShadow()).thenReturn(registerMappingShadow1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn((int)RM_ID_2);
        when(registerMapping102.getShadow()).thenReturn(registerMappingShadow2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101, registerMapping102));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(registerMappingFactory.find((int)RM_ID_1)).thenReturn(registerMapping101);
        when(registerMappingFactory.find((int) RM_ID_2)).thenReturn(registerMapping102);

        Entity<List<RegisterMappingInfo>> json = Entity.json(Arrays.asList(registerMappingInfo1, registerMappingInfo2));
        Response response = target("/devicetypes/31/registers").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing updated
        assertThat(registerMappingShadows.getDeletedShadows()).isEmpty();
        assertThat(registerMappingShadows.getNewShadows()).isEmpty();
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
        DeviceTypeShadow deviceTypeShadow = mock(DeviceTypeShadow.class);
        RegisterMappingShadow registerMappingShadow1 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow1.getId()).thenReturn((int)RM_ID_1);
        RegisterMappingShadow registerMappingShadow2 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow2.getId()).thenReturn((int)RM_ID_2);
        ShadowList<RegisterMappingShadow> registerMappingShadows = new ShadowList<>();
        registerMappingShadows.basicAdd(registerMappingShadow1);
        when(deviceTypeShadow.getRegisterMappingShadows()).thenReturn(registerMappingShadows);
        when(deviceType.getShadow()).thenReturn(deviceTypeShadow);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn((int)RM_ID_1);
        when(registerMapping101.getShadow()).thenReturn(registerMappingShadow1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn((int)RM_ID_2);
        when(registerMapping102.getShadow()).thenReturn(registerMappingShadow2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(registerMappingFactory.find((int) RM_ID_1)).thenReturn(registerMapping101);
        when(registerMappingFactory.find((int)RM_ID_2)).thenReturn(registerMapping102);

        Entity<List<RegisterMappingInfo>> json = Entity.json(Arrays.asList(registerMappingInfo1, registerMappingInfo2));
        Response response = target("/devicetypes/31/registers").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        assertThat(registerMappingShadows.getDeletedShadows()).isEmpty();
        assertThat(registerMappingShadows.getNewShadows()).contains(registerMappingShadow2);
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
        DeviceTypeShadow deviceTypeShadow = mock(DeviceTypeShadow.class);
        RegisterMappingShadow registerMappingShadow1 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow1.getId()).thenReturn((int)RM_ID_1);
        ShadowList<RegisterMappingShadow> registerMappingShadows = new ShadowList<>();
        registerMappingShadows.basicAdd(registerMappingShadow1);
        when(deviceTypeShadow.getRegisterMappingShadows()).thenReturn(registerMappingShadows);
        when(deviceType.getShadow()).thenReturn(deviceTypeShadow);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn((int)RM_ID_1);
        when(registerMapping101.getShadow()).thenReturn(registerMappingShadow1);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(registerMappingFactory.find((int)RM_ID_1)).thenReturn(registerMapping101);
        when(registerMappingFactory.find((int) RM_ID_2)).thenReturn(null);

        Entity<List<RegisterMappingInfo>> json = Entity.json(Arrays.asList(registerMappingInfo1, registerMappingInfo2));
        Response response = target("/devicetypes/31/registers").request().put(json);
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
        DeviceTypeShadow deviceTypeShadow = mock(DeviceTypeShadow.class);
        RegisterMappingShadow registerMappingShadow1 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow1.getId()).thenReturn((int)RM_ID_1);
        RegisterMappingShadow registerMappingShadow2 = mock(RegisterMappingShadow.class);
        when(registerMappingShadow2.getId()).thenReturn((int) RM_ID_2);
        ShadowList<RegisterMappingShadow> registerMappingShadows = new ShadowList<>();
        registerMappingShadows.basicAdd(registerMappingShadow1);
        registerMappingShadows.basicAdd(registerMappingShadow2);
        when(deviceTypeShadow.getRegisterMappingShadows()).thenReturn(registerMappingShadows);
        when(deviceType.getShadow()).thenReturn(deviceTypeShadow);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn((int)RM_ID_1);
        when(registerMapping101.getShadow()).thenReturn(registerMappingShadow1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn((int)RM_ID_2);
        when(registerMapping102.getShadow()).thenReturn(registerMappingShadow2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101, registerMapping102));
        when(deviceConfigurationService.findDeviceType(31)).thenReturn(deviceType);
        when(registerMappingFactory.find((int)RM_ID_1)).thenReturn(registerMapping101);
        when(registerMappingFactory.find((int)RM_ID_2)).thenReturn(registerMapping102);

        Entity<List<RegisterMappingInfo>> json = Entity.json(Arrays.asList(registerMappingInfo1));
        Response response = target("/devicetypes/31/registers").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing updated
        assertThat(registerMappingShadows.getDeletedShadows()).hasSize(1).contains(registerMappingShadow2);
        assertThat(registerMappingShadows.getNewShadows()).isEmpty();
    }

    @Test
    public void testRegistersForDeviceTypeWithoutFilter() throws Exception {
        // Backend has RM 101 and 102 for device type 31
        long deviceType_id=31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn((int)RM_ID_1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn((int)RM_ID_2);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101, registerMapping102));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(deviceType);

        List response = target("/devicetypes/31/registers").request().get(List.class);
        assertThat(response).hasSize(2);
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_Filtered() throws Exception {
        // Backend has RM 101 and 102 for device type 31
        long deviceType_id=31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        long RM_ID_3 = 103L;


        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterMapping registerMapping101 = mock(RegisterMapping.class);
        when(registerMapping101.getId()).thenReturn((int)RM_ID_1);
        RegisterMapping registerMapping102 = mock(RegisterMapping.class);
        when(registerMapping102.getId()).thenReturn((int)RM_ID_2);
        RegisterMapping registerMapping103 = mock(RegisterMapping.class);
        when(registerMapping103.getId()).thenReturn((int)RM_ID_3);
        when(deviceType.getRegisterMappings()).thenReturn(Arrays.asList(registerMapping101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(deviceType);
        int[] existingIds = new int[] {(int) RM_ID_1};
        when(registerMappingFactory.findAllExcept(existingIds)).thenReturn(Arrays.asList(registerMapping102, registerMapping103));

        List response = target("/devicetypes/31/registers").queryParam("available","true").request().get(List.class);
        assertThat(response).hasSize(2);
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(SortOrder.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }
}
