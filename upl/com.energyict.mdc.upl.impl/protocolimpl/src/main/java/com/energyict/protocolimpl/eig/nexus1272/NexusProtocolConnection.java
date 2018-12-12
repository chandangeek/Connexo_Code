package com.energyict.protocolimpl.eig.nexus1272;


import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.Connection;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 *
 * @author jsm
 */
public class NexusProtocolConnection extends Connection implements ProtocolConnection {

	InputStream inputStream;
	OutputStream outputStream;
	int iTimeout;
	int iMaxRetries;
	long lForceDelay;
	int iEchoCancelling;
	int iIEC1107Compatible;
	Encryptor encryptor;
	int iProtocolTimeout;
	boolean boolFlagIEC1107Connected;
	Logger logger;

	/** Creates a new instance of SDKSampleProtocolConnection */
	public NexusProtocolConnection(InputStream inputStream,
			OutputStream outputStream,
			int iTimeout,
			int iMaxRetries,
			long lForceDelay,
			int iEchoCancelling,
			int iIEC1107Compatible,
			Encryptor encryptor,Logger logger) {
		super(inputStream, outputStream, lForceDelay, iEchoCancelling);
		this.iMaxRetries = iMaxRetries;
		this.lForceDelay = lForceDelay;
		this.iIEC1107Compatible = iIEC1107Compatible;
		this.encryptor=encryptor;
		iProtocolTimeout=iTimeout;
		boolFlagIEC1107Connected=false;
		this.logger=logger;
		this.outputStream = outputStream;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {

	}
	public HHUSignOn getHhuSignOn() {
		return null;
	}
	public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
		logger.info("call connection class disconnectMAC(...)");
	}
	public MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) throws IOException {
		logger.info("call connection class connectMAC(...)");
		return null;
	}
	public byte[] dataReadout(String strID,String nodeId) {
		return null;
	}

	public void delayAndFlush(int delay) throws ConnectionException, NestedIOException {
		super.delayAndFlush(delay);
	}

	public ByteArrayOutputStream receiveResponse() throws IOException {
		return receiveResponse(-1);
	}

	private final long TIMEOUT = 8000;

	public enum RESPONSE_STATES {BUILD_TID, BUILD_PID, BUILD_LEN, BUILD_UID, BUILD_FC, HANDLE_READ_RESPONSE, HANDLE_WRITE_RESPONSE, CHECK_COMPLETE}

	public enum READ_RESPONSE_STATES {BUILD_BC, BUILD_DATA}

	public enum WRITE_RESPONSE_STATES{BUILD_SA, BUILD_DATA}

	public ByteArrayOutputStream receiveWriteResponse(Command c) throws ConnectionException, NestedIOException {
		byte[] transId = new byte[2];
		byte[] protocolId = new byte[2];
		byte[] len = new byte [2];
		byte functionCode;
		byte[] startAddress = new byte[2];
		byte[] data = new byte[2];
		int byteCount = 0;

		//Initial States
		RESPONSE_STATES state = RESPONSE_STATES.BUILD_TID;
		WRITE_RESPONSE_STATES writeState = WRITE_RESPONSE_STATES.BUILD_SA;
		READ_RESPONSE_STATES readState = READ_RESPONSE_STATES.BUILD_BC;
		byte kar = -2;

		ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
		long protocolTimeout = System.currentTimeMillis() + TIMEOUT;

		copyEchoBuffer();
		int count = 0;
		while(true) {

			int kar2;
			if ((kar2 = readIn()) != -1) {
				kar = (byte) kar2;
				protocolTimeout = System.currentTimeMillis() + TIMEOUT;
				switch(state) {

				case BUILD_TID: {
					transId[count] = kar;
					count++;
					if (count == 2) {
						int recTID = getSmallInt(transId);
						if ( recTID != (c.getTransactionID())) {
							throw new ProtocolConnectionException("Transaction ID mismatch " + recTID + " // " + c.getTransactionID() % 65536);
						}
						state = RESPONSE_STATES.BUILD_PID;
					}

				} break; // BUILD_TID
				case BUILD_PID: {
					protocolId[count-2] = kar;
					count++;
					if (count == 4) {
						if (ProtocolUtils.getShort(protocolId, 0) != c.getProtocolID()) {
							throw new ProtocolConnectionException("Protocol ID mismatch");
						}
						state = RESPONSE_STATES.BUILD_LEN;
					}

				} break; // BUILD_PID
				case BUILD_LEN: {
					len[count-4] = kar;
					count++;
					if (count == 6) {
						state = RESPONSE_STATES.BUILD_UID;
					}

				} break; // BUILD_LEN
				case BUILD_UID: {
					count++;
					state = RESPONSE_STATES.BUILD_FC;

				} break; // BUILD_UID
				case BUILD_FC: {
					functionCode = kar;
					count++;
					switch (functionCode) {
					case 6:
					case 16: {
						state = RESPONSE_STATES.HANDLE_WRITE_RESPONSE;
					} break;
					case 3: {
						state = RESPONSE_STATES.HANDLE_READ_RESPONSE;
					}break;
					default:
						throw new ProtocolConnectionException("Unacceptable Function Code " + functionCode);
					}
				} break; // BUILD_FC
				case HANDLE_WRITE_RESPONSE: {
					switch(writeState) {
					case BUILD_SA: {
						startAddress[count-8] = kar;
						count++;
						if (count == 10) {
							writeState = WRITE_RESPONSE_STATES.BUILD_DATA;
						}
					} break; // BUILD_SA
					case BUILD_DATA: {
						data[count-10] = kar;
						count++;
						if (count == 12) {
							state = RESPONSE_STATES.CHECK_COMPLETE;
						}
					} break; // BUILD_SP
					}
				}break; // HANDLE_WRITE_RESPONSE
				case HANDLE_READ_RESPONSE: {
					switch(readState) {
					case BUILD_BC: {
						if (kar <0) {
							byteCount = kar2;
						} else {
							byteCount = kar;
						}
						count++;
						readState = READ_RESPONSE_STATES.BUILD_DATA;
					} break; // BUILD_SA
					case BUILD_DATA: {
						resultArrayOutputStream.write(kar);
						count++;
						if ((count - 9) == Math.abs(byteCount)) {
							state = RESPONSE_STATES.CHECK_COMPLETE;
						}
					} break; // BUILD_SP
					}
				}break; // HANDLE_WRITE_RESPONSE
				case CHECK_COMPLETE: {
					logger.warning("Response is longer than expected, revieved " + ProtocolUtils.outputHexString(kar) + " when expecting nothing");
					delayAndFlush(500);
				} // CHECK_COMPLETE
				}

			} // if ((kar = readIn()) != -1)

			if (System.currentTimeMillis() - protocolTimeout > 0) {
				throw new ProtocolConnectionException("receiveWriteResponse() response timeout error",TIMEOUT_ERROR);
			}
			if (state == RESPONSE_STATES.CHECK_COMPLETE && kar2 == -1) {
				return resultArrayOutputStream;
			}

		} // while(true)
	}

	public void receiveReadResponse(Command c) throws IOException {

		RESPONSE_STATES state;
		byte[] transId = new byte[2];
		byte[] protocolId = new byte[2];
		byte[] len = new byte [2];
		byte kar = -2;
		state = RESPONSE_STATES.BUILD_TID;
		ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
		long protocolTimeout = System.currentTimeMillis() + TIMEOUT;


		copyEchoBuffer();
		int count = 0;
		while(true) {

			int kar2;
			if ((kar2 = readIn()) != -1) {
				kar = (byte) kar2;
				protocolTimeout = System.currentTimeMillis() + TIMEOUT;
				resultArrayOutputStream.write(kar);
				switch(state) {

				case BUILD_TID: {
					transId[count] = kar;
					count++;
					if (count == 2) {
						int recTID = getSmallInt(transId);
						if ( recTID != (c.getTransactionID())) {
							throw new ProtocolConnectionException("Transaction ID mismatch " + recTID + " // " + c.getTransactionID() % 65536);
						}
						state = RESPONSE_STATES.BUILD_PID;
					}

				} break; // BUILD_TID
				case BUILD_PID: {
					protocolId[count-2] = kar;
					count++;
					if (count == 4) {
						if (ProtocolUtils.getShort(protocolId, 0) != c.getProtocolID()) {
							throw new ProtocolConnectionException("Protocol ID mismatch");
						}
						state = RESPONSE_STATES.BUILD_LEN;
					}

				} break; // BUILD_PID
				case BUILD_LEN: {
					len[count-4] = kar;
					count++;
					if (count == 6) {
						state = RESPONSE_STATES.BUILD_UID;
					}

				} break; // BUILD_LEN
				case BUILD_UID: {
					count++;
					state = RESPONSE_STATES.BUILD_FC;

				} break; // BUILD_UID
				case CHECK_COMPLETE: {
					throw new IOException("Response is longer than expected, revieved " + ProtocolUtils.outputHexString(kar) + " when expecting nothing");
				}
				}

			} // if ((kar = readIn()) != -1)

			if (System.currentTimeMillis() - protocolTimeout > 0) {
				throw new ProtocolConnectionException("receiveWriteResponse() response timeout error",TIMEOUT_ERROR);
			}
			if (state == RESPONSE_STATES.CHECK_COMPLETE && kar2 == -1) {
				return;
			}

		} // while(true)
	}

	public ByteArrayOutputStream receiveResponse(long timeoutEnq) throws IOException {
		long protocolTimeout,interFrameTimeout;
		int kar;
		ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
		interFrameTimeout = System.currentTimeMillis() + (timeoutEnq==-1?iProtocolTimeout:timeoutEnq);
		protocolTimeout = System.currentTimeMillis() + TIMEOUT;



		resultArrayOutputStream.reset();
		copyEchoBuffer();
		while(true) {

			if ((kar = readIn()) != -1) {
				resultArrayOutputStream.write(kar);
				protocolTimeout = System.currentTimeMillis() + TIMEOUT;
			} // if ((kar = readIn()) != -1)

			if (System.currentTimeMillis() - protocolTimeout > 0) {
				return resultArrayOutputStream;
			}

			if (System.currentTimeMillis() - interFrameTimeout > 0) {
			}

		} // while(true)

	} // public Response receiveResponse()

	//Get 16 bit integer
	public static int getSmallInt(byte[] byteBuffer) {
		return((((int)byteBuffer[0]<<8)& 0x0000FF00) |
				(((int)byteBuffer[1])&    0x000000FF));
	}

}

