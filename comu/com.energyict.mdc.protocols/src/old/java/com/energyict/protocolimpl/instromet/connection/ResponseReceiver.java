package com.energyict.protocolimpl.instromet.connection;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseReceiver {

	private static final int DEBUG = 0;

	private final int WAIT_FOR_START = 0;
	private final int WAIT_FOR_FUNCTION = 1;
	private final int WAIT_FOR_DATA = 2;
	private final int WAIT_FOR_STATUS = 3;
	private final int WAIT_FOR_CRC = 4;

	private final byte COLON = 0x3A;

	private InstrometConnection instrometConnection;
	private int timeout;

	public ResponseReceiver(InstrometConnection instrometConnection, int timeout) {
		this.instrometConnection = instrometConnection;
		this.timeout = timeout;
	}

	protected void printState(int state) {
		if (state == 0) {
			System.out.println("WAIT_FOR_START");
		} else if (state == 1) {
			System.out.println("WAIT_FOR_FUNCTION");
		} else if (state == 2) {
			System.out.println("WAIT_FOR_DATA");
		} else if (state == 3) {
			System.out.println("WAIT_FOR_STATUS");
		} else if (state == 4) {
			System.out.println("WAIT_FOR_CRC");
		} else {
			System.out.println("status = " + state);
		}
	}

	protected Response receiveResponse(Command command, long timeoutEnq) throws IOException {
		long protocolTimeout = System.currentTimeMillis() + getTimeout();
		ByteArrayOutputStream dataArrayOutputStream = new ByteArrayOutputStream();
		dataArrayOutputStream.reset();
		instrometConnection.echoCancellation();

		int state = WAIT_FOR_START;
		int kar;
		int commIdByteCount = 0;
		int startAddressByteCount = 0;
		int datalengthByteCount = 0;
		int datalengthValue = 0;
		int dataBytesRead = 0;
		int crcByteCount = 0;
		int statusAddressCount = 0;

		boolean statusCodeFound = false;
		byte[] startAddress = new byte[4], datalength = new byte[2], crc = new byte[2], statusAddress = new byte[4];
		Command responseCommand = null;
		while (true) {
			if ((kar = instrometConnection.readNext()) != -1) {
				dataArrayOutputStream.write(kar);

				/*System.out.print(",0x");
                ProtocolUtils.outputHex( ((int)kar));
                System.out.println("");*/
				//printState(state);
				if (state == WAIT_FOR_START) {
					if ((byte)kar == COLON) {
						state = WAIT_FOR_FUNCTION;
					}
				}
				else if (state == WAIT_FOR_FUNCTION) {
					//System.out.println("commIdByteCount = " + commIdByteCount);
					if (commIdByteCount == 2) {
						char function = (char)kar;
						//System.out.println("function = " + function);
						responseCommand = new Command(function);
						if (responseCommand.isWriteCommand()) {
							state = WAIT_FOR_DATA;
						} else if (responseCommand.isStatusCommand()) {
							state = WAIT_FOR_STATUS;
						} else {
							throw new IOException(
									"invalid command returned: " + responseCommand.getCommand());
						}
					}
					commIdByteCount++;
				}
				else if (state == WAIT_FOR_STATUS) {
					if (!statusCodeFound) {
						int statusCode = kar;
						if (DEBUG>=1) {
							System.out.println("IG_DEBUG> status received: " + statusCode);
						}
						statusCodeFound = true;
					}
					else {
						if (statusAddressCount < 4) {
							statusAddress[statusAddressCount] = (byte) kar;
							statusAddressCount++;
							if (statusAddressCount == 4) {
								int statusAddressValue = this.getStatusAddressValue(statusAddress);
								if (DEBUG>=1) {
									System.out.println("IG_DEBUG> statusAddress received: " +
											statusAddressValue);
								}
								state = WAIT_FOR_CRC;
							}
						}
					}
				}
				else if (state == WAIT_FOR_DATA) {  // kan enkel bij Write vanuit connector
					if (startAddressByteCount < 4) {
						startAddress[startAddressByteCount] = (byte) kar;
						startAddressByteCount++;
						if (startAddressByteCount == 4) {
							if (DEBUG>=1) {
								System.out.println("IG_DEBUG> startAddress received: " +
										getStartAddress(startAddress));
							}
						}
					}
					else if ((startAddressByteCount == 4) && (datalengthByteCount < 2)) {
						datalength[datalengthByteCount] = (byte) kar;
						datalengthByteCount++;
						if (datalengthByteCount == 2) {
							datalengthValue = getDataLengthValue(datalength);
							if (DEBUG>=1) {
								System.out.println("IG_DEBUG> datalength received: " + datalengthValue);
							}
							state = WAIT_FOR_CRC;
						}
					}

				}
				else if (state == WAIT_FOR_CRC) {
					dataBytesRead++;
					//System.out.println("dataBytesRead = " + dataBytesRead);
					//System.out.println("datalengthValue = " + datalengthValue);
					if (dataBytesRead > datalengthValue) {
						crcByteCount++;
						//System.out.println("crcByteCount = " + crcByteCount);
						crc[crcByteCount - 1] = (byte) kar;
						if (crcByteCount == 2) {
							checkCrc(dataArrayOutputStream.toByteArray(), crc);
							byte[] data = dataArrayOutputStream.toByteArray();
							//System.out.println("data.length " + data.length);
							Response response = new Response(ProtocolUtils.getSubArray2(data, 3, data.length-5));

							if (DEBUG>=1) {
								System.out.println("IG_DEBUG> CRC OK, response="+response);
							}
							return response;
						}
					}
				}
			}
			if ((command != null) && (!command.isLogoffCommand())) {
				if (((System.currentTimeMillis() - protocolTimeout)) > 0) {
					throw new ProtocolConnectionException(
							"receiveResponse() response timeout error",
							instrometConnection.getTimeoutError());
				}
			}
		}
	}

	protected void checkCrc(byte[] data, byte[] crc) throws IOException {
		this.instrometConnection.checkCrc(data, crc);
	}

	protected int getStatusAddressValue(byte[] statusAddress) throws IOException {
		return ProtocolUtils.getInt(statusAddress, 0, 4);
	}

	protected int getStartAddress(byte[] startAddress) throws IOException {
		return ProtocolUtils.getInt(startAddress, 0, 4);
	}

	protected int getDataLengthValue(byte[] datalength) throws IOException  {
		return ProtocolUtils.getInt(datalength, 0, 2);
	}

	private int getTimeout() {
		return timeout;
	}

}
