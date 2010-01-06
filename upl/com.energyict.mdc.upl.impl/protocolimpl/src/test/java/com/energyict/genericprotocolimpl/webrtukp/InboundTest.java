package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

public class InboundTest {

	private static final String	LOCALHOST	= "127.0.0.1";

	@Test
	public void inboundTest() {
		try {
			Socket socket = new Socket(LOCALHOST, 4059);
			socket.getOutputStream().write("<REQUEST>serialId=SomeSerialNumber</REQUEST>".getBytes());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
