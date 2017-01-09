package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.upl.RuntimeEnvironment;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.SerialPortStreamConnection;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class AS220TransparentConnectionTest {

    @Mock
    private RuntimeEnvironment runtimeEnvironment;

	@Test
	public void buildTransparentByteArrayTest() throws ConnectionException {
        byte[] transparentString = new byte[]{0x01, 0x57, 0x31, 0x02, 0x53, 0x30, 0x4E, 0x28, 0x30, 0x41, 0x35, 0x30, 0x29, 0x03, 0x3F};
        SerialCommunicationChannel scom = new SerialPortStreamConnection("TCP1", this.runtimeEnvironment);
        AS220TransparentConnection connection = new AS220TransparentConnection(scom, 10, 9600, 8, 1, 0, 1, "00000000");
        assertArrayEquals(transparentString, connection.buildTransparentByteArray());
	}

	@Test
	public void getTransparentTimeByteArrayTest() throws ConnectionException {
        SerialCommunicationChannel scom = new SerialPortStreamConnection("TCP1", this.runtimeEnvironment);
        AS220TransparentConnection connection = new AS220TransparentConnection(scom, 10, 9600, 8, 1, 0, 1, "00000000");
        assertArrayEquals(new byte[]{0x30, 0x41}, connection.getTransparentTimeByteArray());

        connection = new AS220TransparentConnection(scom, 20, 9600, 8, 1, 0, 1, "00000000");
        assertArrayEquals(new byte[]{0x31, 0x34}, connection.getTransparentTimeByteArray());
	}

	@Test
	public void getCommunicationParametersByteArray() throws ConnectionException {
        SerialCommunicationChannel scom = new SerialPortStreamConnection("TCP1", this.runtimeEnvironment);
        AS220TransparentConnection connection = new AS220TransparentConnection(scom, 10, 9600, 8, 1, 0, 1, "00000000");
        assertArrayEquals(new byte[]{0x35, 0x30}, connection.getCommunicationParametersByteArray());

        connection = new AS220TransparentConnection(scom, 10, 300, 7, 1, 2, 1, "00000000");
        assertArrayEquals(new byte[]{0x30, 0x37}, connection.getCommunicationParametersByteArray());
	}
}
