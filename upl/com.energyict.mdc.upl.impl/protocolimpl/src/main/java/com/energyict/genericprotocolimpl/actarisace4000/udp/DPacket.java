/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author gna
 *
 */
public class DPacket {

	private int DEBUG = 1;
	
	private ActarisACE4000 aace = null;
	private DatagramSocket socket = null;
	private DatagramPacket packet = null;
	private SocketAddress socketAddress = null;
	private InetAddress inetAddress = null;
	
//	private int port = 4096;
//	private String ipAddress = "194.122.175.38";
	private int timeOut = 60000;
	private byte[] buffer;
	
	private static int maxChars = 160;
	private static int maxSize = 8192;	// theoretical maximum UDP size
	/**
	 * @throws SocketException 
	 * 
	 */
	public DPacket() throws SocketException {
		this(null);
	}

	public DPacket(ActarisACE4000 aace) throws SocketException {
		this.aace = aace;
		socket = aace.getUdpSocket().getUdpSocket();
	}

	public void sendMessage(String msg){
		buffer = new byte[msg.getBytes().length];
		packet = new DatagramPacket(buffer, buffer.length);
		System.arraycopy(msg.getBytes(), 0, buffer, 0, msg.getBytes().length);
		packet.setData(buffer);
		
		try {
			if(DEBUG >=1) System.out.println("Message sent: " + new String(packet.getData()));
			socket.send(packet);
			receive();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receive(){
		
		byte packetBuff[];
		
		buffer = new byte[maxSize];
		packet = new DatagramPacket(buffer, buffer.length);
		
		try {
			socket.setSoTimeout(timeOut);
			while(true){
				if(DEBUG >=1) System.out.println("Waiting for responce.");
				socket.receive(packet);
				packetBuff = new byte[packet.getLength()];
				System.arraycopy(packet.getData(), 0, packetBuff, 0, packet.getLength());
				if(DEBUG >=1) System.out.println("Received: " + new String(packetBuff));
				aace.getObjectFactory().parseXML(new String(packetBuff));
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
