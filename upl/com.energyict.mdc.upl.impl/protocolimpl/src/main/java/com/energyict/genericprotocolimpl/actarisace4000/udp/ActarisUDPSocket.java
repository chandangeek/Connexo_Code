/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.udp;

import java.net.*;

/**
 * @author gna
 *
 */
public class ActarisUDPSocket {
	
	private DatagramSocket udpSocket;
	
	private String ipAddress;
	private String portNumber;

	/**
	 * 
	 */
	public ActarisUDPSocket() {
		// TODO Auto-generated constructor stub
	}

	public ActarisUDPSocket(String phoneNumber){
		setIpAddress(phoneNumber.split(":")[0]);
		setPortNumber(phoneNumber.split(":")[1]);
		
		try {
			udpSocket = new DatagramSocket(Integer.parseInt(portNumber));
			udpSocket.connect(InetAddress.getByName(ipAddress), Integer.parseInt(portNumber));
//			udpSocket = new DatagramSocket(Integer.parseInt(portNumber), InetAddress.getByName(ipAddress));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new NumberFormatException("Invalid portnumber");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getIpAddress() {
		return ipAddress;
	}

	private void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPortNumber() {
		return portNumber;
	}

	private void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}

	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}

}
