package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.SortOrder;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.core.DeviceType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Application;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceTypeResourceTest extends JerseyTest {

    private static DeviceConfigurationService deviceConfigurationService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deviceConfigurationService = mock(DeviceConfigurationService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(deviceConfigurationService);
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
        DeviceType deviceType = mock(DeviceType.class);
        String webRTUKP = "WebRTUKP";
        when(deviceType.getName()).thenReturn(webRTUKP);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.GATEWAY);
        when(deviceConfigurationService.findDeviceType(webRTUKP)).thenReturn(deviceType);

        Map<String, Object> map = target("/devicetypes/WebRTUKP").request().get(Map.class);
        assertThat(map.get("name")).isEqualTo(webRTUKP);
    }

    @Test
    public void testDeviceTypeInfoJavaScriptMappings() throws Exception {
        int NUMBER_OF_CONFIGS = 4;
        int NUMBER_OF_LOADPROFILES = 6;
        int NUMBER_OF_REGISTERS = 8;
        int NUMBER_OF_LOGBOOKS = 10;

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("unique name");
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
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.GATEWAY);
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
        assertThat(jsonDeviceType.get("logBookCount")).isEqualTo(NUMBER_OF_LOGBOOKS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("registerCount")).isEqualTo(NUMBER_OF_REGISTERS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceConfigurationCount")).isEqualTo(NUMBER_OF_CONFIGS).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("loadProfileCount")).isEqualTo(NUMBER_OF_LOADPROFILES).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("name")).isEqualTo("unique name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeGateway")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("isDirectlyAddressable")).isEqualTo(true).describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("communicationProtocolName")).isEqualTo("device protocol name").describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("serviceCategory")).isNotNull().describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceFunction")).isNotNull().describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(SortOrder.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }
}
