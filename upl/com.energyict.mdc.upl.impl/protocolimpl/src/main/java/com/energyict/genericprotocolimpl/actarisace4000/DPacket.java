/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
	
	private int port = 4096;
	private int timeOut = 60000;
	private String ipAddress = "194.122.175.38";
	private byte[] buffer = new byte[160];
	
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
		
//			inetAddress = InetAddress.getByName(ipAddress);
//			socketAddress = new InetSocketAddress(inetAddress, port);
		
//			socket = new DatagramSocket(port);
//			socket.connect(socketAddress);
		
		socket = aace.getUDPListener().getDatagramSocket();
//			socket.bind(socketAddress);
		
		packet = new DatagramPacket(buffer, buffer.length);
	}

	public void sendMessage(String msg){
		System.arraycopy(msg.getBytes(), 0, buffer, 0, msg.getBytes().length);
		packet.setData(buffer);
		
		try {
			socket.send(packet);
			receive();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receive(){
		try {
			socket.setSoTimeout(timeOut);
			while(true){
				if(DEBUG >=1) System.out.println("Waiting for receive.");
				socket.receive(packet);
				if(DEBUG >=1) System.out.println(packet.toString());
				if(DEBUG >=1) System.out.println("Datalenght = " + packet.getData().length);
				if(DEBUG >=1) System.out.println(new String(packet.getData()));
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
