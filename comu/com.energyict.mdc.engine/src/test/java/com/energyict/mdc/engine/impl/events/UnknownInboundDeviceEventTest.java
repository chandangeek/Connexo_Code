/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UnknownInboundDeviceEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:33)
 */
@RunWith(MockitoJUnitRunner.class)
public class UnknownInboundDeviceEventTest {

    private static final long COMSERVER_ID = 97;
    private static final long COMPORT_ID = COMSERVER_ID + 1;
    private static final long DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID = COMPORT_ID + 1;

    private static final String COMSERVER_NAME = "UnknownInboundDeviceEventTest";
    private static final String COMPORT_NAME = "TCP";
    private static final String DEVICE_IDENTIFIER = "UnknownInboundDeviceEventTest";

    @Mock
    private ComServer comServer;
    @Mock
    private ComPort comPort;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass;

    @Before
    public void initializeMocks () {
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comServer.getName()).thenReturn(COMSERVER_NAME);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.comPort.getName()).thenReturn(COMPORT_NAME);
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        when(this.deviceIdentifier.getIdentifier()).thenReturn(DEVICE_IDENTIFIER);
        when(this.discoveryProtocolPluggableClass.getId()).thenReturn(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID);
    }

    @Test
    public void testConstructorExtractsInformation () {
        // Business method
        new UnknownInboundDeviceEvent(this.comPort, this.deviceIdentifier, this.discoveryProtocolPluggableClass);

        // Asserts
        verify(this.comPort).getName();
        verify(this.comServer).getName();
        verify(this.deviceIdentifier).getIdentifier();
        verify(this.discoveryProtocolPluggableClass).getId();
    }

    @Test
    public void testGetComPortName () {
        // Business method
        UnknownInboundDeviceEvent unknownInboundDeviceEvent = new UnknownInboundDeviceEvent(this.comPort, this.deviceIdentifier, this.discoveryProtocolPluggableClass);

        // Asserts
        assertThat(unknownInboundDeviceEvent.getComPortName()).isEqualTo(COMPORT_NAME);
    }

    @Test
    public void testGetComServerName () {
        // Business method
        UnknownInboundDeviceEvent unknownInboundDeviceEvent = new UnknownInboundDeviceEvent(this.comPort, this.deviceIdentifier, this.discoveryProtocolPluggableClass);

        // Asserts
        assertThat(unknownInboundDeviceEvent.getComServerName()).isEqualTo(COMSERVER_NAME);
    }

    @Test
    public void testGetDeviceIdentifier () {
        // Business method
        UnknownInboundDeviceEvent unknownInboundDeviceEvent = new UnknownInboundDeviceEvent(this.comPort, this.deviceIdentifier, this.discoveryProtocolPluggableClass);

        // Asserts
        assertThat(unknownInboundDeviceEvent.getDeviceIdentifier()).isEqualTo(DEVICE_IDENTIFIER);
    }

    @Test
    public void testGetDiscoveryProtocolId () {
        // Business method
        UnknownInboundDeviceEvent unknownInboundDeviceEvent = new UnknownInboundDeviceEvent(this.comPort, this.deviceIdentifier, this.discoveryProtocolPluggableClass);

        // Asserts
        assertThat(unknownInboundDeviceEvent.getDiscoveryProtocolId()).isEqualTo(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID);
    }

}