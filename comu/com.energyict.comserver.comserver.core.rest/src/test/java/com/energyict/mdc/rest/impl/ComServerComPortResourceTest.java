/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.rest.impl.comserver.ComPortInfo;
import com.energyict.mdc.rest.impl.comserver.TcpInboundComPortInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComServerComPortResourceTest extends ComserverCoreApplicationJerseyTest {

    public static final long COM_PORT_ID = 24L;
    public static final long COM_SERVER_ID = 17L;

    public static final long OK_VERSION = 58L;
    public static final long BAD_VERSION = 43L;

    private ComServer mockComServer() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COM_SERVER_ID);
        when(comServer.getObsoleteDate()).thenReturn(null);
        when(comServer.isObsolete()).thenReturn(false);
        when(comServer.getVersion()).thenReturn(OK_VERSION);
        List comPorts = new ArrayList();
        when(comServer.getComPorts()).thenReturn(comPorts);

        when(engineConfigurationService.findComServer(COM_SERVER_ID)).thenReturn(Optional.of(comServer));
        when(engineConfigurationService.findAndLockComServerByIdAndVersion(COM_SERVER_ID, OK_VERSION)).thenReturn(Optional.of(comServer));
        when(engineConfigurationService.findAndLockComServerByIdAndVersion(COM_SERVER_ID, BAD_VERSION)).thenReturn(Optional.empty());
        return comServer;
    }

    private TCPBasedInboundComPort mockComPort() {
        return mockComPort(mockComServer());
    }

    private TCPBasedInboundComPort mockComPort(ComServer comServer) {
        TCPBasedInboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getId()).thenReturn(COM_PORT_ID);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.getObsoleteDate()).thenReturn(null);
        when(comPort.isObsolete()).thenReturn(false);
        when(comPort.getVersion()).thenReturn(OK_VERSION);
        when(comPort.getComPortType()).thenReturn(ComPortType.TCP);
        comServer.getComPorts().add(comPort);

        doReturn(Optional.of(comPort)).when(engineConfigurationService).findComPort(COM_PORT_ID);
        doReturn(Optional.of(comPort)).when(engineConfigurationService).findAndLockComPortByIdAndVersion(COM_PORT_ID, OK_VERSION);
        doReturn(Optional.empty()).when(engineConfigurationService).findAndLockComPortByIdAndVersion(COM_PORT_ID, BAD_VERSION);
        return comPort;
    }

    @Test
    public void testUpdateComPortOkVersion() {
        TCPBasedInboundComPort comPort = mockComPort();
        ComPortInfo info = new TcpInboundComPortInfo(comPort);
        info.name = "new name";
        Response response = target("/comservers/" + COM_SERVER_ID + "/comports/" + COM_PORT_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comPort, times(1)).setName("new name");
    }

    @Test
    public void testUpdateComPortBadVersion() {
        TCPBasedInboundComPort comPort = mockComPort();
        ComPortInfo info = new TcpInboundComPortInfo(comPort);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/comservers/" + COM_SERVER_ID + "/comports/" + COM_PORT_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(comPort, never()).setName("new name");
    }

    @Test
    public void testUpdateComPortBadParentVersion() {
        TCPBasedInboundComPort comPort = mockComPort();
        ComPortInfo info = new TcpInboundComPortInfo(comPort);
        info.name = "new name";
        info.parent.version = BAD_VERSION;
        Response response = target("/comservers/" + COM_SERVER_ID + "/comports/" + COM_PORT_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(comPort, never()).setName("new name");
    }

    @Test
    public void testDeleteComPortOkVersion() {
        TCPBasedInboundComPort comPort = mockComPort();
        ComPortInfo info = new TcpInboundComPortInfo(comPort);
        Response response = target("/comservers/" + COM_SERVER_ID + "/comports/" + COM_PORT_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(comPort.getComServer(), times(1)).removeComPort(COM_PORT_ID);
    }

    @Test
    public void testDeleteComPortBadVersion() {
        TCPBasedInboundComPort comPort = mockComPort();
        ComPortInfo info = new TcpInboundComPortInfo(comPort);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/comservers/" + COM_SERVER_ID + "/comports/" + COM_PORT_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(comPort.getComServer(), never()).removeComPort(COM_PORT_ID);
    }

    @Test
    public void testDeleteComPortBadParentVersion() {
        TCPBasedInboundComPort comPort = mockComPort();
        ComPortInfo info = new TcpInboundComPortInfo(comPort);
        info.name = "new name";
        info.parent.version = BAD_VERSION;
        Response response = target("/comservers/" + COM_SERVER_ID + "/comports/" + COM_PORT_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(comPort.getComServer(), never()).removeComPort(COM_PORT_ID);
    }
}
