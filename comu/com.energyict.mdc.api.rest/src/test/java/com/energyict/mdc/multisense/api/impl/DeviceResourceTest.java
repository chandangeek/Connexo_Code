package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.rest.PropertyType;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ExecutableActionPropertyImpl;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/29/15.
 */
public class DeviceResourceTest extends MultisensePublicApiJerseyTest {

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

        Device device = mockDevice("DAV", "65749846514", elec1);
        deviceXas = mockDevice("XAS", "5544657642", elec1);
        Device device3 = mockDevice("PIO", "54687651356", elec2);
        Finder<Device> deviceFinder = mockFinder(Arrays.asList(device, deviceXas, device3));
        when(this.deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);
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
    public void testDeviceFields() throws Exception {
        Response response = target("/devices/fields").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(21);
        assertThat(model.<List<String>>get("$")).contains("actions","batch","connectionMethods","deviceConfiguration",
                "deviceProtocolPluggeableClassId","gatewayType","id","isDirectlyAddressable","isGateway","lifecycleState",
                "link","loadProfiles","logBooks","mRID","name","nbrOfDataCollectionIssues","physicalGateway","serialNumber",
                "slaveDevices","version","yearOfCertification");
    }

    @Test
    public void testHypermediaLinkWithConnectionMethods() throws Exception {
        ConnectionTask<?, ?> connectionTask13 = mock(ConnectionTask.class);
        when(connectionTask13.getId()).thenReturn(13L);
        ConnectionTask<?, ?> connectionTask14 = mock(ConnectionTask.class);
        when(connectionTask14.getId()).thenReturn(14L);
        when(deviceXas.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask13, connectionTask14));
        Response response = target("/devices/XAS").queryParam("fields","connectionMethods").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$.connectionMethods")).hasSize(2);
        assertThat(model.<Integer>get("$.connectionMethods[0].id")).isEqualTo(13);
        assertThat(model.<String>get("$.connectionMethods[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.connectionMethods[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/connectionmethods/13");
    }

    @Test
    public void testHypermediaLinkWithLogBooks() throws Exception {
        LogBook logBook1 = mock(LogBook.class);
        when(logBook1.getId()).thenReturn(13L);
        when(deviceXas.getLogBooks()).thenReturn(Arrays.asList(logBook1));
        Response response = target("/devices/XAS").queryParam("fields","logBooks").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$.logBooks")).hasSize(1);
        assertThat(model.<Integer>get("$.logBooks[0].id")).isEqualTo(13);
        assertThat(model.<String>get("$.logBooks[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.logBooks[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/logbooks/13");
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
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo("self");
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/actions/1");
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(2);
        assertThat(model.<String>get("data[1].name")).isEqualTo("action.name.2");
        assertThat(model.<String>get("data[1].link.params.rel")).isEqualTo("self");
        assertThat(model.<String>get("data[1].link.href")).isEqualTo("http://localhost:9998/devices/XAS/actions/2");
        assertThat(model.<List>get("data[1].properties")).hasSize(1);
        assertThat(model.<String>get("data[1].properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("data[1].properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("data[1].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[1].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testTriggerDeviceExecutableActionsWithStringProperties() throws Exception {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, stringPropertySpec);
        ExecutableAction executableAction2 = mockExecutableAction(2L, "action.name.2", MicroAction.ENABLE_VALIDATION, stringPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Arrays.asList(executableAction1, executableAction2));
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        info.deviceVersion = 333L;
        info.name = "action.name.1";
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        info.properties.add(propertyInfo);
        propertyInfo.key = "string.property";
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = PropertyType.TEXT;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("abcdefg", null);
        when(deviceLifeCycleService.toExecutableActionProperty("abcdefg", stringPropertySpec)).
                thenAnswer(invocationOnMock -> new ExecutableActionPropertyImpl((PropertySpec) invocationOnMock.getArguments()[1], invocationOnMock.getArguments()[0]));

        Response response = target("/devices/XAS/actions/1").request("application/json").put(Entity.json(info));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executableAction1).execute(instantArgumentCaptor.capture(), listArgumentCaptor.capture());
        List<ExecutableActionProperty> value = listArgumentCaptor.getValue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).getValue()).isEqualTo("abcdefg");
        assertThat(value.get(0).getPropertySpec().getName()).isEqualTo("string.property");
        assertThat(value.get(0).getPropertySpec().getValueFactory().getClass()).isEqualTo(StringFactory.class);
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(now);
    }

    @Test
    public void testTriggerDeviceExecutableActionsWithEffectiveTime() throws Exception {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now.minusSeconds(60));
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, stringPropertySpec);
        ExecutableAction executableAction2 = mockExecutableAction(2L, "action.name.2", MicroAction.ENABLE_VALIDATION, stringPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Arrays.asList(executableAction1, executableAction2));
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        info.deviceVersion = 333L;
        info.name = "action.name.1";
        info.properties = new ArrayList<>();
        info.effectiveTimestamp = now;
        PropertyInfo propertyInfo = new PropertyInfo();
        info.properties.add(propertyInfo);
        propertyInfo.key = "string.property";
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = PropertyType.TEXT;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("abcdefg", null);
        when(deviceLifeCycleService.toExecutableActionProperty("abcdefg", stringPropertySpec)).
                thenAnswer(invocationOnMock -> new ExecutableActionPropertyImpl((PropertySpec) invocationOnMock.getArguments()[1], invocationOnMock.getArguments()[0]));

        Response response = target("/devices/XAS/actions/1").request("application/json").put(Entity.json(info));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executableAction1).execute(instantArgumentCaptor.capture(), listArgumentCaptor.capture());
        List<ExecutableActionProperty> value = listArgumentCaptor.getValue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).getValue()).isEqualTo("abcdefg");
        assertThat(value.get(0).getPropertySpec().getName()).isEqualTo("string.property");
        assertThat(value.get(0).getPropertySpec().getValueFactory().getClass()).isEqualTo(StringFactory.class);
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(now);
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

    @Test
    public void testTriggerDeviceExecutableActionsWithDateProperty() throws Exception {
        Date now = new Date(1436306400000L);
        PropertySpec datePropertySpec = mockDatePropertySpec(now);
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, datePropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        info.deviceVersion = 333L;
        info.name = "action.name.1";
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        info.properties.add(propertyInfo);
        propertyInfo.key = "date.property";
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("2015-07-08", null);
        when(deviceLifeCycleService.toExecutableActionProperty(now, datePropertySpec)).
                thenAnswer(invocationOnMock -> new ExecutableActionPropertyImpl((PropertySpec) invocationOnMock.getArguments()[1], invocationOnMock.getArguments()[0]));

        Response response = target("/devices/XAS/actions/1").request("application/json").put(Entity.json(info));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executableAction1).execute(instantArgumentCaptor.capture(), listArgumentCaptor.capture());
        List<ExecutableActionProperty> value = listArgumentCaptor.getValue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).getValue()).isEqualTo(now);
        assertThat(value.get(0).getPropertySpec().getName()).isEqualTo("date.property");
        assertThat(value.get(0).getPropertySpec().getValueFactory().getClass()).isEqualTo(DateFactory.class);
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

}
