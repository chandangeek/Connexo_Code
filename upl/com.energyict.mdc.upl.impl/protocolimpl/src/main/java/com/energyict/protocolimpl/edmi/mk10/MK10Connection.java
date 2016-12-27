package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.edmi.mk10.core.ResponseData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author jme
 */
public class MK10Connection extends Connection  implements ProtocolConnection {

	private static final int DEBUG=0;
	private static final long TIMEOUT=60000;

	int timeout;
	int maxRetries;
	ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();


	long sourceId;
	long destinationId=-1;
	int sequenceNr=0x7FFF; // initial sequencenumber
	long forcedDelay;

	/** Creates a new instance of AlphaConnection */
	public MK10Connection(InputStream inputStream,
			OutputStream outputStream,
			int timeout,
			int maxRetries,
			long forcedDelay,
			int echoCancelling,
			HalfDuplexController halfDuplexController,
			String serialNumber) {
		super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
		this.timeout = timeout;
		this.maxRetries=maxRetries;
		this.forcedDelay=forcedDelay;
		if ((serialNumber!=null) && ("".compareTo(serialNumber)!=0)) {
			destinationId=Long.parseLong(serialNumber);
		}
	} // EZ7Connection(...)

	public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException {
		sourceId = Long.parseLong(nodeId);
		return null;
	}

	public byte[] dataReadout(String strID, String nodeId) {
		return null;
	}

	public void disconnectMAC() {
	}

	public HHUSignOn getHhuSignOn() {
		return null;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {
	}

	private void sendByte(byte txbyte) {
		switch(txbyte) {
		case STX:
		case ETX:
		case DLE:
		case DC1: //XON:
		case DC3: //XOFF:
			assembleFrame(DLE);
		assembleFrame((byte)(txbyte|0x40));
		break;
		default:
			assembleFrame(txbyte);
		}
	} // void sendByte(byte txbyte) throws ConnectionException

	public ResponseData sendCommand(byte[] cmdData) throws ConnectionException {
		int retry=0;
		doSendCommand(cmdData);
		while(true) {
			try {
				delayAndFlush(forcedDelay); // KV_DEBUG
				sendFrame();
				return receiveFrame();
			}
			catch(IOException e) {
				if (retry++>=maxRetries) {
					throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
				}
			}
		} // while(true)
	} // public void sendCommand(byte[] cmdData) throws ConnectionException

	private void genSequenceNr() {
		if ((sequenceNr==0) || (sequenceNr>=0x7FFF)) {
			sequenceNr=1;
		} else {
			sequenceNr++;
		}
	}

	private byte[] getExtendedCommandHeader() {
		byte[] cmdData = new byte[11];
		cmdData[0]='E';
		cmdData[1]=(byte)(destinationId>>24);
		cmdData[2]=(byte)(destinationId>>16);
		cmdData[3]=(byte)(destinationId>>8);
		cmdData[4]=(byte)(destinationId);
		cmdData[5]=(byte)(sourceId>>24);
		cmdData[6]=(byte)(sourceId>>16);
		cmdData[7]=(byte)(sourceId>>8);
		cmdData[8]=(byte)(sourceId);
		genSequenceNr();
		cmdData[9]=(byte)(sequenceNr>>8);
		cmdData[10]=(byte)(sequenceNr);
		return cmdData;
	}

	// multidrop?
	private boolean isExtendedCommunication() {
		return (destinationId != -1);
	}

	private void assembleFrame(byte txbyte) {
		txOutputStream.write(txbyte);
	}

	private void sendFrame() throws ConnectionException {
		sendOut(txOutputStream.toByteArray());
	}


	private void doSendCommand(byte[] rawData) {
		txOutputStream.reset();
		assembleFrame(STX);
		byte[] cmdData=rawData;
		byte[] txFrame=null;
		if (isExtendedCommunication()) {  // multidrop...
			// rawData is null when sending the EnterCommandline command
			// The EnterCommandline command has no data so it's impossible to use a extended command.
			// An extended command without actual data will return a <CAN><WRONG_LENGTH> message.
			if (rawData==null){
				//cmdData = getExtendedCommandHeader();
				cmdData = null;
			}
			else {
				cmdData = ProtocolUtils.concatByteArrays(getExtendedCommandHeader(),rawData);
			}
		}
		if ((cmdData!=null) && (cmdData.length>0)) {
			txFrame = new byte[cmdData.length+1+2]; // [STX][cmdData array bytes][CRC 16 bit]
			System.arraycopy(cmdData,0,txFrame,1,cmdData.length);
			txFrame[0]=STX;
			int crc = CRCGenerator.ccittCRC(txFrame, txFrame.length-2);
			txFrame[txFrame.length-2]=(byte)(crc>>8);
			txFrame[txFrame.length-1]=(byte)(crc);
			for (int i=1; i<(txFrame.length); i++) {
				sendByte(txFrame[i]);
			}
		}

		assembleFrame(ETX); // [ETX]
	} // void sendData(byte[] cmdData) throws ConnectionException


	private static final int STATE_WAIT_FOR_STX=0;
	private static final int STATE_WAIT_FOR_DATA=1;

	public ResponseData receiveFrame() throws IOException {

		long protocolTimeout,interFrameTimeout;
		int kar;
		int state = STATE_WAIT_FOR_STX;
		boolean dleKar=false;
		ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

		interFrameTimeout = System.currentTimeMillis() + timeout;
		protocolTimeout = System.currentTimeMillis() + TIMEOUT;

		resultArrayOutputStream.reset();
		allDataArrayOutputStream.reset();

		copyEchoBuffer();
		while(true) {

			if ((kar = readIn()) != -1) {
				if (DEBUG == 1) {
					System.out.print(",0x");
					ProtocolUtils.outputHex( (kar));
				}

				switch(state) {
				case STATE_WAIT_FOR_STX:
					interFrameTimeout = System.currentTimeMillis() + timeout;

					if (kar==STX) {
						allDataArrayOutputStream.write(kar);
						state = STATE_WAIT_FOR_DATA;
					}

					break; // STATE_WAIT_FOR_STX

				case STATE_WAIT_FOR_DATA:

					if (kar==DLE) {
						dleKar=true;
					}
					else if (kar==ETX) {
						// Calc CRC
						byte[] rxFrame = allDataArrayOutputStream.toByteArray();
						if (CRCGenerator.ccittCRC(rxFrame)==0) {

							// The EnterCommandline command doesn't support extended packages.
							// The response will be a normal package even if isExtendedCommunication() returns true
							// so we have to check the length of the package to see if it's a real extended package.
							// A extended package always contains the following fields and length
							//		<Destination_ID> 	4 bytes
							//		<Source_ID> 		4 bytes
							//		<Sequencenumber> 	2 bytes
							// So an extended package is always bigger than 10 bytes

							if (isExtendedCommunication() & (rxFrame.length > 10)) {
								int rxSequenceNr = ((rxFrame[10]&0xFF)<<8) | (rxFrame[11]&0xFF);
								if (rxSequenceNr != sequenceNr) {
									throw new ProtocolConnectionException("receiveFrame() rxSequenceNr("+rxSequenceNr+") != sequenceNr("+sequenceNr+")",PROTOCOL_ERROR);
								} else {
									return new ResponseData(ProtocolUtils.getSubArray(rxFrame,12, rxFrame.length-3));
								}
							} else {
								return new ResponseData(ProtocolUtils.getSubArray(rxFrame,1, rxFrame.length-3));
							}
						}
						else {
							// ERROR, CRC error
							throw new ProtocolConnectionException("receiveFrame() response crc error",CRC_ERROR);
						}
					}
					else {
						if (dleKar) {
							//System.out.println("dle");
							allDataArrayOutputStream.write(kar&0xBF);
						}
						else {
							allDataArrayOutputStream.write(kar);
						}
						dleKar=false;
					}

					break; // STATE_WAIT_FOR_DATA

				} // switch(state)

			} // if ((kar = readIn()) != -1)

			if (((System.currentTimeMillis() - protocolTimeout)) > 0) {
				throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
			}
			if (((System.currentTimeMillis() - interFrameTimeout)) > 0) {
				throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
			}

		} // while(true)


	} // public void receiveFrame() throws ConnectionException

} // public class MK10Connection extends Connection  implements ProtocolConnection
