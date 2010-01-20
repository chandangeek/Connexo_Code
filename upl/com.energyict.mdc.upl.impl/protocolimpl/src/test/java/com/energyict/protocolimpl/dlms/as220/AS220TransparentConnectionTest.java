package com.energyict.protocolimpl.dlms.as220;

import static org.junit.Assert.*;

import org.junit.Test;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.SerialPortStreamConnection;
import com.energyict.protocolimpl.dlms.as220.AS220TransparentConnection;

public class AS220TransparentConnectionTest {

	@Test
	public void buildTransparentByteArrayTest(){
		try {
			byte[] transparentString = new byte[]{0x01, 0x57, 0x31, 0x02, 0x53, 0x30, 0x4E, 0x28, 0x30, 0x41, 0x35, 0x30, 0x29, 0x03, 0x3F};
			SerialCommunicationChannel scom = new SerialPortStreamConnection("TCP1");
			AS220TransparentConnection connection = new AS220TransparentConnection(scom, 10, 9600, 8, 1, 0, 1, "00000000");
			assertArrayEquals(transparentString, connection.buildTransparentByteArray());
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void getTransparentTimeByteArrayTest(){
		try {
			SerialCommunicationChannel scom = new SerialPortStreamConnection("TCP1");
			AS220TransparentConnection connection = new AS220TransparentConnection(scom, 10, 9600, 8, 1, 0, 1, "00000000");
			assertArrayEquals(new byte[]{0x30, 0x41}, connection.getTransparentTimeByteArray());
			
			connection = new AS220TransparentConnection(scom, 20, 9600, 8, 1, 0, 1, "00000000");
			assertArrayEquals(new byte[]{0x31, 0x34}, connection.getTransparentTimeByteArray());
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void getCommunicationParametersByteArray(){
		try {
			SerialCommunicationChannel scom = new SerialPortStreamConnection("TCP1");
			AS220TransparentConnection connection = new AS220TransparentConnection(scom, 10, 9600, 8, 1, 0, 1, "00000000");
			assertArrayEquals(new byte[]{0x35, 0x30}, connection.getCommunicationParametersByteArray());
			
			connection = new AS220TransparentConnection(scom, 10, 300, 7, 1, 2, 1, "00000000");
			assertArrayEquals(new byte[]{0x30, 0x37}, connection.getCommunicationParametersByteArray());
			
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
	}
}
