package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionMethodResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private PartialInboundConnectionTask partialConnectionTask;

    @Test
    public void testGetConnectionMethod() throws Exception {
        String response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
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
        verify(partialConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType("device", 11);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 12);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        partialConnectionTask = mockPartialInboundConnectionTask(13L, "partial inbound");
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask));

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
        when(protocolPluggableService.findConnectionTypePluggableClassByName("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.getPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
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
