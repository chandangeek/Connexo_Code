package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.*;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionMethodResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private PartialInboundConnectionTask partialInboundConnectionTask;
    private PartialScheduledConnectionTask partialScheduledConnectionTask;

    @Test
    public void testGetConnectionMethod() throws Exception {
        String response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
    }

    @Test
    public void testUpdateConnectionMethodSetComPortPoolToNull() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = "pluggableClass";
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        verify(partialInboundConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }

    @Test
    public void testUpdateConnectionMethodSetInvalidTimeout() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = "pluggableClass";
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = "connectionTimeOut";
        propertyInfo.name = "connectionTimeOut";
        propertyInfo.required = false;
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.timeUnit="1"; // INVALID
        propertyInfo.propertyValueInfo = new PropertyValueInfo<TimeDurationInfo>(timeDurationInfo,null,null,true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = SimplePropertyType.TIMEDURATION;
        info.properties.add(propertyInfo);
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).startsWith("properties.");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("Unknown time unit '" + timeDurationInfo.timeUnit + "'");
    }

    @Test
    public void testUpdateScheduledConnectionMethodSetComPortPoolToNull() throws Exception {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = "pluggableClass";
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/14").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<OutboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(OutboundComPortPool.class);
        verify(partialScheduledConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }

    @Before
    public void setup() {
        super.setup();
        DeviceType deviceType = mockDeviceType("device", 11);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 12);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        partialInboundConnectionTask = mockPartialInboundConnectionTask(13L, "partial inbound");
        partialScheduledConnectionTask = mockPartialScheduledConnectionTask(14L, "partial scheduled");
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialInboundConnectionTask, partialScheduledConnectionTask));

    }

    protected DeviceConfiguration mockDeviceConfiguration(String name, long id){
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        return deviceConfiguration;
    }

    private PartialInboundConnectionTask mockPartialInboundConnectionTask(long id, String name) {
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(id);
        when(partialConnectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(partialConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("pluggableClass");

        PropertySpec propertySpec = new BasicPropertySpec("connectionTimeOut",false, new TimeDurationValueFactory());
        when(pluggableClass.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(protocolPluggableService.findConnectionTypePluggableClassByName("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("com port pool");
        when(partialConnectionTask.getComPortPool()).thenReturn(comPortPool);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        return partialConnectionTask;
    }

    private PartialScheduledConnectionTask mockPartialScheduledConnectionTask(long id, String name) {
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(id);
        when(partialConnectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(partialConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("pluggableClass");
        when(protocolPluggableService.findConnectionTypePluggableClassByName("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("com port pool");
        when(partialConnectionTask.getComPortPool()).thenReturn(comPortPool);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        return partialConnectionTask;
    }


    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceConfigurationService.findDeviceType(id)).thenReturn(Optional.of(deviceType));
        return deviceType;
    }

}
