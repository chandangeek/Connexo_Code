/**
 * MK10Push.java
 *
 * Created on 8-jan-2009, 12:47:25 by jme
 *
 */
package com.energyict.genericprotocolimpl.edmi.mk10;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.Ignore;

import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.protocol.ProtocolUtils;
/**
 * @author jme
 *
 */
public class MK10PushUtil {

	private static final String HEARTBEAT =
		"$8F$50$FF$E5$0C$7B$BB$F7$45$30$39$30$39$34$33$39" +
		"$00$33$35$33$31$36$37$30$30$35$39$36$32$37$37$34" +
		"$00$38$39$34$34$31$32$32$32$35$32$31$37$31$37$37" +
		"$33$36$31$38$00$0E$FD";

	private static final String COMMISSIONING =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$00$33$35$36$31$38$37$30$33$30$30$30$32$35$31$38" +
		"$00$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$30$58$58$00$31$2E$33$36$20$00$04$02$6D$40$31$31" +
		"$61$66$2C$37$64$62$62$2C$38$2C$00$00$00$00$00$00" +
		"$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
		"$E4$37";


	@Ignore
	public static void main(String[] args) {

		byte[] bytes = getBytesFromHexString(COMMISSIONING);
		System.out.println(ProtocolUtils.getResponseData(bytes));

		//		try {
		//			saveUdpPacket(bytes, "c:\\mk10.hex");
		//		} catch (IOException e1) {
		//			e1.printStackTrace();
		//		}

		try {
			sendUdpPacket(bytes, "127.0.0.1", 11000);
		} catch (IOException e) {
			e.printStackTrace();
		}

		PushPacket packet = PushPacket.getPushPacket(bytes);
		System.out.println(packet);

	}

	public static void saveUdpPacket(byte[] packetData, String fileName) throws IOException {
		FileWriter fw = new FileWriter(new File(fileName));
		fw.write(new String(packetData));
		fw.flush();
		fw.close();
	}

	public static void sendUdpPacket(byte[] packetData, String host, int port) throws IOException {
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			DatagramSocket socket = new DatagramSocket();
			socket.connect(address, port);
			DatagramPacket dp = new DatagramPacket(packetData, packetData.length);
			socket.send(dp);
			socket.close();
		} catch (UnknownHostException e) {
			throw new IOException(e.getMessage());
		} catch (SocketException e) {
			throw new IOException(e.getMessage());
		}

	}

}
