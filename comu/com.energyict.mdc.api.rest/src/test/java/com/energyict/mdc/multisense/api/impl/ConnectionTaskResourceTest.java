package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testGetAllConnectionTasksOfDevice() throws Exception {
        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        Device deviceXas = mockDevice("XAS", "5544657642", elec1);

        Response response = target("devices/XAS/connectionmethods").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    }

    @Test
    public void testGetSingleConnectionTask() throws Exception {
        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        Device deviceXas = mockDevice("XAS", "5544657642", elec1);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(65L);

        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(41L);
        when(connectionTask.getName()).thenReturn("connTask");
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(connectionTask.getDevice()).thenReturn(deviceXas);
        when(connectionTask.isSimultaneousConnectionsAllowed()).thenReturn(true);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        PartialScheduledConnectionTask partial = mock(PartialScheduledConnectionTask.class);
        ConnectionTypePluggableClass connectionTaskPluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(partial.getPluggableClass()).thenReturn(connectionTaskPluggeableClass);
        when(connectionTaskPluggeableClass.getName()).thenReturn("pluggeable class");
        when(connectionTask.getPartialConnectionTask()).thenReturn(partial);
        ConnectionType connectionType = mock(ConnectionType.class);
        PropertySpec propertySpec = mockStringPropertySpec();
        when(connectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(connectionTask.getRescheduleDelay()).thenReturn(TimeDuration.minutes(60));
        when(connectionTaskService.findConnectionTask(41L)).thenReturn(Optional.of(connectionTask));

        Response response = target("devices/XAS/connectionmethods/41").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(41);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("connTask");
        assertThat(jsonModel.<String>get("$.status")).isEqualTo("Active");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.allowSimultaneousConnections")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("pluggeable class");
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/XAS/connectionmethods/41");
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/65");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
    }
}
