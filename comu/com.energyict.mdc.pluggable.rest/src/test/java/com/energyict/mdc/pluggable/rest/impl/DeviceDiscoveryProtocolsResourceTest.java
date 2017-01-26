package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceDiscoveryProtocolsResourceTest extends PluggableRestApplicationJerseyTest {

    @Test
    public void testDeleteProtocolOkVersion() {
        InboundDeviceProtocolPluggableClass protocolClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(protocolPluggableService.findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(1L, 1L)).thenReturn(Optional.of(protocolClass));

        DeviceDiscoveryProtocolInfo info = new DeviceDiscoveryProtocolInfo();
        info.id = 1L;
        info.version = 1L;

        Response response = target("/devicediscoveryprotocols/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(protocolClass).delete();
    }

    @Test
    public void testDeleteProtocolBadVersion() {
        InboundDeviceProtocolPluggableClass protocolClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(protocolPluggableService.findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(protocolPluggableService.findInboundDeviceProtocolPluggableClass(1L)).thenReturn(Optional.empty());

        DeviceDiscoveryProtocolInfo info = new DeviceDiscoveryProtocolInfo();
        info.id = 1L;
        info.version = 1L;

        Response response = target("/devicediscoveryprotocols/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(protocolClass, never()).delete();
    }

    @Test
    public void testUpdateProtocolOkVersion() {
        InboundDeviceProtocolPluggableClass protocolClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(protocolPluggableService.findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(1L, 1L)).thenReturn(Optional.of(protocolClass));
        InboundDeviceProtocol inboundProtocol = mock(InboundDeviceProtocol.class);
        when(protocolClass.getInboundDeviceProtocol()).thenReturn(inboundProtocol);
        when(inboundProtocol.getPropertySpecs()).thenReturn(Collections.emptyList());

        DeviceDiscoveryProtocolInfo info = new DeviceDiscoveryProtocolInfo();
        info.id = 1L;
        info.version = 1L;

        Response response = target("/devicediscoveryprotocols/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(protocolClass).setName(Matchers.anyString());
        verify(protocolClass).save();
    }

    @Test
    public void testUpdateProtocolBadVersion() {
        InboundDeviceProtocolPluggableClass protocolClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(protocolPluggableService.findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(protocolPluggableService.findInboundDeviceProtocolPluggableClass(1L)).thenReturn(Optional.empty());

        DeviceDiscoveryProtocolInfo info = new DeviceDiscoveryProtocolInfo();
        info.id = 1L;
        info.version = 1L;

        Response response = target("/devicediscoveryprotocols/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(protocolClass, never()).setName(Matchers.anyString());
        verify(protocolClass, never()).save();
    }
}
