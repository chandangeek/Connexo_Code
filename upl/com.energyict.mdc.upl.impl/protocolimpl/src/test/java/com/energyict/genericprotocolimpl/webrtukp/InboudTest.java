package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.junit.Test;


public class InboudTest {

	@Test
	public void inboundTest(){
		try {
			Socket socket = new Socket("10.0.0.61", 4059);
				
			socket.getOutputStream().write("<REQUEST>serialId=3000-0000FF-0922</REQUEST>".getBytes());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
