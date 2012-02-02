package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.csd;

import com.energyict.dialer.core.*;
import com.energyict.dialer.coreimpl.ModemConnection;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 31/01/12
 * Time: 16:30
 */


public class CSDCall extends ModemConnection{

	private SerialCommunicationChannel serialCommChannel;
	private Link link;

	private static String NOCARRIER = "NO CARRIER received";
	private static String BUSY = "BUSY received";

	/**
	 * Constructor
	 * @param link
	 * @throws IOException
	 */
	public CSDCall(Link link) throws IOException {
		super(link);
		this.link = link;
		this.serialCommChannel = link.getSerialCommunicationChannel();
	}

	/**
	 * Do a CSD ATDial with the given phone number.
	 * The CSD call fails when we do not get the expected response.
	 * @param phone
	 * @param postDialCommand
	 * @throws IOException
	 */
	public void doCall(String phone, String postDialCommand) throws IOException{

		try {
			if(this.serialCommChannel.sigCD()){
				hangupModem();
			} else {
				toggleDTR(1000);
			}
			initModem();
			dialMeter(phone, postDialCommand, 60000);

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not establish a new modemConnection. " + e.getMessage());
		} catch (LinkException e) {
			e.printStackTrace();
			throw new IOException("Could not hangup the modem. " + e.getMessage());
		} finally {
			try {
				hangupModem();
			} catch (LinkException e) {
				e.printStackTrace();
				throw new IOException("Could not hangup the modem. " + e.getMessage());
			}
		}
	}

	/**
	 * Make the dial to the meter
	 * @param phone
	 * @param postDialCommand
	 * @param timeOut
	 * @throws IOException
	 * @throws LinkException
	 */
	private void dialMeter(String phone, String postDialCommand, int timeOut) throws IOException, LinkException {

		try {
			if((postDialCommand != null) && (postDialCommand.trim().length() != 0)){
				write("ATD"+postDialCommand+phone+"\r\n", 500);
			} else {
				write("ATD"+phone+"\r\n",500);
			}

			if(expectCommPort("CONNECT", timeOut)){
				throw new IOException("Failed to do a wakeUp.");
			}

		} catch (DialerCarrierException e){
			if(e.getMessage().indexOf(NOCARRIER) != 0){
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
		} catch (DialerException e){
			if(e.getMessage().indexOf(BUSY) != 0){
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (LinkException e) {
			e.printStackTrace();
			throw new LinkException(e.getMessage());
		}
	}

	/**
	 * Write a specific command to the outputStream
	 * @param command
	 * @param timeOut
	 * @throws IOException
	 */
	private void write(String command, int timeOut) throws IOException{
		try {

			this.link.getStreamConnection().write(command, 500);

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

}