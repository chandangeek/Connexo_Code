/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import com.energyict.protocol.ProtocolUtils;

/**
 * @author kvds
 * 
 *
 */
public class UDPListener extends Thread {
	
	private int DEBUG = 1;
	private int port = 4096;
	private String ipAddress = "194.122.175.38";
	
	private ActarisACE4000 aace = null;
	private DatagramSocket datagramSocket = null;
	
	private static int maxChars = 160;
	private static int maxSize = 8192;	// theoretical maximum UDP size
	
	/**
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 * 
	 */
	public UDPListener() throws SocketException, UnknownHostException {
		this(null);
	}
	
	public UDPListener(ActarisACE4000 actarisACE4000) throws SocketException, UnknownHostException {
		super("UDP Listener");
		this.aace = actarisACE4000;
		datagramSocket = new DatagramSocket(port);
		datagramSocket.connect(InetAddress.getByName(ipAddress), port);
		start();
	}

	public void run() {
		if(DEBUG >= 1) System.out.println("In the run method.");
		try{
			byte[] buf = new byte[maxChars];
			while(true){
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				datagramSocket.receive(packet);
				if(DEBUG >= 1) System.out.println(new String(packet.getData()));
				aace.getBuffer().addMessage(new String(packet.getData()));
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(datagramSocket != null){
				datagramSocket.close();
				datagramSocket = null;
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UDPListener udpListener = new UDPListener();
			udpListener.allInterfacesInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    private void allInterfacesInfo() throws IOException {
        Enumeration<NetworkInterface> enumNetworkInterface = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface networkInterface : Collections.list(enumNetworkInterface)) {
            if (DEBUG>=1) {
                System.out.println("*********************************************************************************************************");
                System.out.println("Name: "+networkInterface.getName());
                System.out.println("Displayname: "+networkInterface.getDisplayName());
                System.out.println("HardwareAddress: " + getInterfaceMACAddress(networkInterface));
            }
            int count=0;
        }
    }
    
    private String getInterfaceMACAddress(NetworkInterface ni) throws SocketException{
    	byte[] b = ni.getHardwareAddress();
    	if (b != null){
    		StringBuilder mac = new StringBuilder();
    		for(int i = 0; i < b.length; i++){
    			mac.append(ProtocolUtils.buildStringHex(b[i]&0xFF, 2));
    			if(i == b.length-1)
    				return mac.toString();
    			mac.append(":");
    		}
    	}
    	return null;
    }

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public void setDatagramSocket(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
	}
}
