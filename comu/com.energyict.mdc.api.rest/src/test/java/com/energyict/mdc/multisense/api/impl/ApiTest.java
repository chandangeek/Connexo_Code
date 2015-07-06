package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/29/15.
 */
public class ApiTest extends DeviceDataPublicApiJerseyTest {

    private Device deviceXas;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        DeviceType water = mockDeviceType(10, "water");
        DeviceType gas = mockDeviceType(11, "gas");
        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        DeviceType elec2 = mockDeviceType(101, "Electricity 2");
        DeviceType elec3 = mockDeviceType(101, "Electricity 3");
        DeviceType elec4 = mockDeviceType(101, "Electricity 4");
        DeviceType elec5 = mockDeviceType(101, "Electricity 5");
        Finder<DeviceType> deviceTypeFinder = mockFinder(Arrays.asList(water, gas, elec1, elec2, elec3, elec4, elec5));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);

        Device device = mockDevice("DAV", "65749846514");
        deviceXas = mockDevice("XAS", "5544657642");
        Device device3 = mockDevice("PIO", "54687651356");
        when(this.deviceService.findByUniqueMrid("DAV")).thenReturn(Optional.of(device));
        when(this.deviceService.findByUniqueMrid("XAS")).thenReturn(Optional.of(deviceXas));
        Finder<Device> deviceFinder = mockFinder(Arrays.asList(device, deviceXas, device3));
        when(this.deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);
    }

    private Device mockDevice(String mrid, String serial) {
        Device mock = mock(Device.class);
        when(mock.getmRID()).thenReturn(mrid);
        when(mock.getName()).thenReturn(mrid);
        long deviceId = (long) mrid.hashCode();
        when(mock.getId()).thenReturn(deviceId);
        when(mock.getSerialNumber()).thenReturn(serial);
        DeviceType deviceType = mockDeviceType(31L, "X1");
        when(mock.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(deviceConfig.getName()).thenReturn("Default configuration");
        when(deviceConfig.getId()).thenReturn(34L);
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfig);
        Register register = mock(Register.class);
        when(register.getRegisterSpecId()).thenReturn(666L);
        when(mock.getRegisters()).thenReturn(Collections.singletonList(register));
        Batch batch = mock(Batch.class);
        when(batch.getName()).thenReturn("BATCH A");
        when(deviceImportService.findBatch(deviceId)).thenReturn(Optional.of(batch));
        when(topologyService.getPhysicalGateway(mock)).thenReturn(Optional.empty());
        return mock;
    }

    private DeviceType mockDeviceType(long id, String name) {
        DeviceType mock = mock(DeviceType.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1000 + id, "Default");
        when(mock.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getId()).thenReturn(id*id);
        when(mock.getDeviceProtocolPluggableClass()).thenReturn(pluggableClass);
        when(deviceConfigurationService.findDeviceType(id)).thenReturn(Optional.of(mock));
        return mock;
    }

    private DeviceConfiguration mockDeviceConfiguration(long id, String name) {
        DeviceConfiguration mock = mock(DeviceConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);

        return mock;
    }

    @Test
    public void testJsonCallSinglePage() throws Exception {

        Response response = target("/devicetypes").queryParam("start",0).queryParam("limit",10).request("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.data")).hasSize(7);
        assertThat(model.<List>get("$.link")).hasSize(1);

    }

    @Test
    public void testJsonCallMultiPage() throws Exception {

        Response response = target("/devicetypes").queryParam("start",2).queryParam("limit", 2).request("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.data")).hasSize(2);
        assertThat(model.<List>get("$.link")).hasSize(3);

    }

    @Test
    public void testJsonCallSingle() throws Exception {
        Response response = target("/devicetypes/10").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
    }

    @Test
    public void testHypermediaLinkJsonCallSingle() throws Exception {
        Response response = target("/devices/XAS").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
    }

    @Test
    public void testHypermediaLinkWithFieldsCallSingle() throws Exception {
        Response response = target("/devices/XAS").queryParam("fields", "id,serialNumber").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
    }

    @Test
    public void testGetDeviceExecutableActionsWithStringProperties() throws Exception {
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, stringPropertySpec);
        ExecutableAction executableAction2 = mockExecutableAction(2L, "action.name.2", MicroAction.ENABLE_VALIDATION, stringPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Arrays.asList(executableAction1, executableAction2));
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/XAS/actions?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(1);
        assertThat(model.<String>get("data[0].name")).isEqualTo("action.name.1");
        assertThat(model.<Boolean>get("data[0].transitionNow")).isEqualTo(true);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo("self");
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/actions/1");
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(2);
        assertThat(model.<String>get("data[1].name")).isEqualTo("action.name.2");
        assertThat(model.<Boolean>get("data[1].transitionNow")).isEqualTo(true);
        assertThat(model.<String>get("data[1].link.params.rel")).isEqualTo("self");
        assertThat(model.<String>get("data[1].link.href")).isEqualTo("http://localhost:9998/devices/XAS/actions/2");
        assertThat(model.<List>get("data[1].properties")).hasSize(1);
        assertThat(model.<String>get("data[1].properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("data[1].properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("data[1].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[1].properties[0].required")).isEqualTo(true);
    }

    private PropertySpec mockStringPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("string.property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn("default");
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);

        return propertySpec;
    }

    @Test
    public void testGetDeviceExecutableActionsWithBigDecimalProperties() throws Exception {
        PropertySpec bigDecimalPropertySpec = mockBigDecimalPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, bigDecimalPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("decimal.property");
        assertThat(model.<Integer>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(1);
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    private PropertySpec mockBigDecimalPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("decimal.property");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(BigDecimal.ONE);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    @Test
    public void testGetDeviceExecutableActionsWithListProperties() throws Exception {
        PropertySpec listPropertySpec = mockExhaustiveListPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, listPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("list.property");
        assertThat(model.<String>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo("Value1");
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    private PropertySpec mockExhaustiveListPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("list.property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.isExhaustive()).thenReturn(true);
        when(possibleValues.getAllValues()).thenReturn(Arrays.asList("Value1", "Value2", "Value3"));
        when(possibleValues.getDefault()).thenReturn("Value1");
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    @Test
    public void testGetDeviceExecutableActionsWithDateProperties() throws Exception {
        Date date = new Date();
        PropertySpec listPropertySpec = mockDatePropertySpec(date);
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, listPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("date.property");
        assertThat(model.<Long>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(date.getTime());
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("DATE");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    private PropertySpec mockDatePropertySpec(Date date) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("date.property");
        when(propertySpec.getValueFactory()).thenReturn(new DateFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(date);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    @Test
    public void testGetDeviceExecutableActionsWithDateTimeProperties() throws Exception {
        Date date = new Date();
        PropertySpec listPropertySpec = mockDateTimePropertySpec(date);
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, listPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("datetime.property");
        assertThat(model.<Long>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(date.getTime());
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("CLOCK");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    private PropertySpec mockDateTimePropertySpec(Date date) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("datetime.property");
        when(propertySpec.getValueFactory()).thenReturn(new DateAndTimeFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(date);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    private ExecutableAction mockExecutableAction(Long id, String name, MicroAction microAction, PropertySpec... propertySpecs) {
        ExecutableAction mock = mock(ExecutableAction.class);
        AuthorizedTransitionAction transitionAction = mock(AuthorizedTransitionAction.class);
        when(transitionAction.getId()).thenReturn(id);
        when(transitionAction.getName()).thenReturn(name);
        for (PropertySpec propertySpec : propertySpecs) {
            when(deviceLifeCycleService.getPropertySpecsFor(microAction)).thenReturn(Collections.singletonList(propertySpec));
        }
        when(transitionAction.getActions()).thenReturn(Collections.singleton(microAction));
        when(mock.getAction()).thenReturn(transitionAction);

        return mock;
    }

    @Test
    public void testDeviceTypeWithConfig() throws Exception {
        DeviceType serial = mockDeviceType(4, "Serial");
        DeviceType serial2 = mockDeviceType(6, "Serial 2");
        Finder<DeviceType> finder = mockFinder(Arrays.asList(serial, serial2));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        target("/devicetypes").queryParam("fields", "deviceConfigurations").request("application/json").get();

    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}
