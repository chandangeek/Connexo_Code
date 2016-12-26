/*
 * PACTConnection.java
 *
 * Created on 24 maart 2004, 17:42
 * Changes:
 * KV 29112004 solved bug in block counting. Resulted in a -13 authentication error because non streaming mode retrieved 8 bytes more then necessary!
 * GN 25032008 added a main to check the dates
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 *
 * @author Koen
 */
public class PACTConnection extends Connection {

	public final int DEBUG = 0;

	private int maxRetries;
	private long forceDelay;
	private int protocolTimeout;

	// *************************************************************************
	// Timing values (in ms)
	private static final int TO1 = 1000; // maximum time to wait for meter to start
//	private static final int T02 = 5000; // maximum time meter waits for next communication
//	private static final int TM1 = 2; // minimum time before meter may reply to a message
//	private static final int ICD = 0; // minimum inter character delay

	// *************************************************************************
	// Control codes
	public static final int CFM = 0xFC; // 'Confirm' The action implicit in the previous exchange(s) was taken.
	public static final int TRM = 0xFF; // 'Terminate transaction' - sender has ended or abandoned the current
	// transaction.
	public static final int NXT = 0xF1; // 'Next block' - request next block in sequence of block transfer. This
	// also provides an implicit acknowledge that the previous block has been
	// accepted.
	public static final int PRE = 0xF0; // 'Repeat block' - request re-sending of previous block, either for
	// confirmation, or because of data corruption.
	public static final int MBH = 0x8A; // 'Meter block header' - Header prefix character to 8-byte data block sent
	// to HHU from meter.
	public static final int HBH = 0x9A; // 'HHU block header' - Header prefix character to 8-byte data block sent to
	// meter from HHU.
	public static final int CHK = 0x00; // 'Checksum' - Suffix to 8-byte data blocks consisting of exclusive-or of
	// the prefix byte and the 8 bytes of data.
	public static final int RDM = 0x80; // 'Read meter' - Transaction initiation code for reading a meter.
	public static final int RDL = 0xA0; // 'Read log' - Transaction initiation code for reading load survey
	// information from a meter.
	public static final int DLT = 0x90; // 'Download Tariff' - Transaction initiation code for downloading tariff or
	// other program information to a meter.
	public static final int MDR = 0xE0; // 'Reset Max Demand' - Transaction initiation code for Max Demand
	// resetting.
	public static final int MB6 = 0x87; // 'Meter block header 6' - Header prefix character to 6-byte data block
	// sent to HHU from meter.
	public static final int RTC = 0xA1; // 'Read time and code' - Request to read meter time and time access code.
	public static final int WTC = 0xA9; // 'Write time and code' - Prefix to 8-byte data block containing time,
	// date, access authenticator, and new time access block.
	public static final int RQ3 = 0xBC; // Request Baud rate change to 300 Baud.
	public static final int RQ1 = 0xBD; // Request Baud rate change to 1200 Baud.
	public static final int RQ2 = 0xBE; // Request Baud rate change to 2400 Baud.
	public static final int RQ4 = 0xBF; // Request Baud rate change to 4800 Baud.
	public static final int RQ9 = 0xBB; // Request Baud rate change to 9600 Baud.
	public static final int READLOG = 0xB2; // Request the logbook

	public static final int BUILDTYPE = 0xFE; // Request for the build type message, processor type codes and
	// identification messages

	public static final int REQUEST_PASSWORD_SEED = 0xC1; // Password seed request
	public static final int PASSWORD_SEED = 0xC5; // Password seed

	public static final int PAKNET_STREAMING = 0xC3; // streaming command

	public static final int PACTLAN_GLOBAL_ENABLE = 0xEC;
	public static final int PACTLAN_GLOBAL_DISABLE = 0xED;
	public static final int PACTLAN_SPECIFIC_ENABLE = 0xE9;

	private static final char END_MARKER_AUTHENTICATION = '#';

	private static final int PACT_FLUSH_TIMEOUT = 4000; // was 1000
	private static final int PACT_TIMEOUT = 1000;

	/** Creates a new instance of PACTConnection */
	public PACTConnection(InputStream inputStream, OutputStream outputStream, int protocolTimeout, int maxRetries,
			long forceDelay, int echoCancelling) throws ConnectionException {
		super(inputStream, outputStream, forceDelay, echoCancelling);
		this.maxRetries = maxRetries;
		this.forceDelay = forceDelay;
		this.protocolTimeout = protocolTimeout;
	} // public PACTConnection(...)

	public void connect() throws NestedIOException, ConnectionException {
		int retries = 0;
		while (true) {
			try {
				flushBuffer();
				return;
			} catch (PACTConnectionException e) {
				if ((e.getReason() == TIMEOUT_ERROR) || (e.getReason() == PROTOCOL_ERROR)) {
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
				} else {
					throw new NestedIOException(e);
				}
			}
		} // while(true)
	} // public void connect()

	public String getIntantaneousValue(String name) throws NestedIOException, ConnectionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(name);
		strBuff.append("\r");
		sendRawData(strBuff.toString().getBytes());
		long timeout;
		int val;
		timeout = System.currentTimeMillis() + PACT_TIMEOUT;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				if (DEBUG >= 2) {
					ProtocolUtils.outputHex(((int) val));
				}
				baos.write(val);
				if (val == '\r') {
					return new String(baos.toByteArray());
				}
			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				throw new PACTConnectionException("getIntantaneousValue() timeout error", TIMEOUT_ERROR);
			}
		} // while(true)
	} // public String getIntantaneousValue(String name)

	public void globalEnableMeter() throws NestedIOException, ConnectionException {
		sendRawData((byte) PACTLAN_GLOBAL_ENABLE);
		long timeout;
		int val;
		timeout = System.currentTimeMillis() + PACT_TIMEOUT;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				if (DEBUG >= 2) {
					ProtocolUtils.outputHex(((int) val));
				}
				if ((val == CFM) || (val == PACTLAN_GLOBAL_ENABLE)) {
					return;
				}
			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				return;
			}
		} // while(true)
	} // public void globalEnableMeter() throws NestedIOException, ConnectionException

	public void globalDisableMeter() throws NestedIOException, ConnectionException {
		sendRawData((byte) PACTLAN_GLOBAL_DISABLE);
		long timeout;
		int val;
		timeout = System.currentTimeMillis() + PACT_TIMEOUT;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				// absorb
			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				return;
			}
		} // while(true)
	} // public void globalDisableMeter() throws NestedIOException, ConnectionException

	public void specificEnableMeter(String serialNumber) throws NestedIOException, ConnectionException {

		if (!((serialNumber != null) && ("".compareTo(serialNumber) != 0))) {
			throw new ConnectionException(
					"PACTConnection, specificEnableMeter, serialNumber = NULL, cannot use PACTLAN specific meter enable!");
		}

		byte[] data = new byte[9]; // pactlan command + 8 bytes
		data[0] = (byte) PACTLAN_SPECIFIC_ENABLE;
		byte[] deviceIdArray = serialNumber.getBytes();
		data[1] = deviceIdArray[3];
		data[2] = deviceIdArray[2];
		data[3] = deviceIdArray[1];
		data[4] = deviceIdArray[0];
		data[5] = deviceIdArray[7];
		data[6] = deviceIdArray[6];
		data[7] = deviceIdArray[5];
		data[8] = deviceIdArray[4];
		try {
			if (isConfirmed(data)) {
				return;
			} else {
				throw new ConnectionException(
						"PACTConnection, specificEnableMeter, meter returned TERMINATE instead of CONFIRM, cannot continue!");
			}
		} catch (PACTConnectionException e) {
			throw new NestedIOException(e);
		}
	} // public void specificEnableMeter(String deviceId) throws NestedIOException, ConnectionException

	public boolean writeRequest(byte[] data) throws NestedIOException, ConnectionException {
		return doWriteRequest(data);
	}

	public boolean writeRequest(int code, byte[] data) throws NestedIOException, ConnectionException {
		byte[] frame = new byte[data.length + 1];
		try {
			ProtocolUtils.arrayCopy(data, frame, 1);
			frame[0] = (byte) code;
		} catch (IOException e) {
			e.printStackTrace(); // shouyld never happen;
		}
		return doWriteRequest(frame);
	} // public void writeRequest(int code, byte[] data)

	private boolean doWriteRequest(byte[] data) throws NestedIOException, ConnectionException {
		int retries = 0;
		boolean retVal = false;
		try {
			retVal = isConfirmed(data);
		} catch (PACTConnectionException e) {
			if (e.getReason() == TIMEOUT_ERROR) {
				if (retries++ >= maxRetries) {
					throw new NestedIOException(e);
				}
			} else {
				throw new NestedIOException(e);
			}
		}
		return retVal;
	}

	public byte[] sendRequest(int code) throws NestedIOException, ConnectionException {
		int retries = 0;
		while (true) {
			try {
				byte[] data = doSendControlCode(code, false);
				// do validation for getTime...
				if ((code == PACTConnection.RTC) && ((data == null) || (((int) data[0] & 0xFF) != 0x87))) {
					if (retries++ >= maxRetries) {
						throw new NestedIOException(new IOException(),
								"PACTConnection, sendRequest(PACTConnection.RTC), max retry (data=="
										+ (data == null ? "null" : "0x" + Integer.toHexString((int) data[0])) + ")");
					}
					delayAndFlush(2000);
					// retry...
				} else {
					return ProtocolUtils.getSubArray2(data, 1, data.length - 1);
				}
			} catch (PACTConnectionException e) {
				if (e.getReason() == TIMEOUT_ERROR) {
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
					delayAndFlush(2000);
				} else {
					throw new NestedIOException(e);
				}
			}
		} // while(true)
	} // public byte[] sendRequest(int code)

	public byte[] getPasswordClearanceSeed() throws NestedIOException {
		try {
			return doGetPasswordClearanceSeed();
		} catch (IOException e) {
			throw new NestedIOException(e);
		}
	}

	public void sendPasswordClearance(byte[] data) throws NestedIOException {
		try {
			if (!(isConfirmed(data))) {
				throw new IOException("PACTConnection, sendPasswordClearance, probably wrong password!");
			}
		} catch (IOException e) {
			throw new NestedIOException(e);
		}
	}

	public byte[] getLogData() throws NestedIOException, ConnectionException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (isConfirmed(new byte[] { (byte) READLOG, 0 })) {
				while (true) {
					byte[] dataBlock = sendControlCode(NXT);
					if (dataBlock == null) {
						break;
					}
					baos.write(dataBlock);
				}
			}
		} catch (IOException e) {
			throw new NestedIOException(e);
		}
		return baos.toByteArray();
	} // public byte[] getLogData()

	public byte[] getLoadSurveyDataStream(int nrOfBlocks, int nrOfDays) throws NestedIOException, ConnectionException {
		int totalAmountOfRetries = 0;
		int retries = 0;
		ByteArrayOutputStream allData = new ByteArrayOutputStream();
		int dayCount = 0;
		int tempDayCount = 0;
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> getLoadSurveyDataStream(), nrOfBlocks=" + nrOfBlocks + ", nrOfDays="
					+ nrOfDays);
		}
		while (true) {
			try {

				if (dayCount > tempDayCount) {

					tempDayCount = dayCount;

					// We clear the retries so more days can be read again
					retries = 0;
				}

				if (DEBUG >= 1) {
					System.out.println("KV_DEBUG> Try reading nrOfDays=" + (nrOfDays - dayCount));
				}
				byte[] code = new byte[] { (byte) PAKNET_STREAMING, (byte) RDL, (byte) (nrOfDays - dayCount) };
				sendRawData(code);

				long timeout;
				int val;
				int count = 0;
				int notCollectingDataPackets = 0;
				int nrOfBlocksPerDay = nrOfBlocks / nrOfDays;
				int successblockCount = 0;
				int checkSum = 0;
				byte[] blockData = new byte[8];
				ByteArrayOutputStream dayData = new ByteArrayOutputStream();
				timeout = System.currentTimeMillis() + 7000; // protocolTimeout;
				copyEchoBuffer();
				boolean collectingData = true;
				while (true) {

					if ((val = readIn()) != -1) {
						timeout = System.currentTimeMillis() + 7000; // protocolTimeout; // rearm timeout

						if (collectingData) {

							if (!((count == 0) && (val == CFM))) {

								if (count == 0) {
									checkSum ^= val;
								}

								if (!((count == 0) || (count == 9))) {
									blockData[count - 1] = (byte) val;
									checkSum ^= val;
								}

								if (count++ >= 9) {
									count = 0;
									if (checkSum != val) { // Checksum ERROR
										collectingData = false;
										if (DEBUG >= 1) {
											System.out.println("KV_DEBUG> checksum error in block " + successblockCount
													+ ", waiting until all data has been send, timeout and retry!");
										}
									} else {
										successblockCount++;
										checkSum = 0;
										dayData.write(blockData);
										if ((successblockCount % nrOfBlocksPerDay) == 0) {
											allData.write(dayData.toByteArray());
											dayData.reset();
											dayCount++;
											if (DEBUG >= 1) {
												System.out.println("KV_DEBUG> day " + dayCount + " read successfull!");
											}
										}
									}

									if (dayCount >= nrOfDays) {
										// if(dayCount == 1){
										if (DEBUG >= 1) {
											System.out.println("KV_DEBUG> END! bufferstreamsize=" + allData.size()
													+ ", blockCount=" + successblockCount);
										}
										if (DEBUG >= 1) {
											System.out.println("KV_DEBUG> Successfull read " + dayCount + " days of "
													+ nrOfDays);
										}

										// It might be that some CLEM programs send FF as the last byte. Therefor
										// we wait 2000 ms to be sure that everything is received before continue and we
										// flush the
										// input buffer.
										// See PAKNET doc!
										delayAndFlush(TO1);

										return allData.toByteArray();
									} // if (successblockCount >= nrOfBlocks)

								} // if (count++ >= 9)

							} // if (!((count == 0) && (val == CFM)))

						} else {
							notCollectingDataPackets++;
							if (notCollectingDataPackets % 50 == 0) {
								sendRawData((byte) TRM);
								delayAndFlush(TO1);
								globalDisableMeter();
								globalEnableMeter();
							}
						}

					} // if ((val = readIn()) != -1)
					if (((long) (System.currentTimeMillis() - timeout)) > 0) {

						if (totalAmountOfRetries++ == 100) { // we will stop after 100 retries, I mean you can get the
							// data faster if you drive to the meter and read if
							// from the display ...
							throw new PACTConnectionException("getLoadSurveyDataStream() max TotalRetries to get "
									+ nrOfDays + " of data", PROTOCOL_ERROR);
						}

						// retry!
						// throw new PACTConnectionException("getLoadSurveyDataStream() timeout error",TIMEOUT_ERROR);
						if (retries++ >= maxRetries) {
							throw new PACTConnectionException("getLoadSurveyDataStream() max retries to get "
									+ nrOfDays + " of data", PROTOCOL_ERROR);
						} else {
							if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> Timeout! bufferstreamsize=" + allData.size()
										+ ", blockCount=" + successblockCount);
							}
							if (DEBUG >= 1) {
								System.out.println("KV_DEBUG> Successfull read " + dayCount + " days of " + nrOfDays);
							}
							break;
						}
					}
				} // while(true)
			} catch (PACTConnectionException e) {
				throw new NestedIOException(e);
			} catch (IOException e) {
				throw new NestedIOException(e);
			}
		} // while(true)
	} // public byte[] getLoadSurveyDataStream()

	public byte[] getLoadSurveyData(int nrOfBlocks) throws NestedIOException, ConnectionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count = 0;
		try {
			if (isConfirmed(RDL)) {
				while (true) {
					byte[] dataBlock = sendControlCode(NXT);
					if (dataBlock != null) {
						baos.write(dataBlock);
						if (DEBUG >= 1) {
							System.out.println("Count : " + count + " - nrOfBlocks : " + nrOfBlocks);
						}
						if (count++ >= (nrOfBlocks - 1)) {
							if (isConfirmed(TRM)) {
								break;
							}
						}
					} else {
                        throw new IOException("getLoadSurveyData() -> An NULL dataBlock was returned, which causes the communication to stop. (received "+count+" blocks)");
                    }
				}
			}
		} catch (IOException e) {
			throw new NestedIOException(e);
		}
		return baos.toByteArray();
	} // public byte[] getLoadSurveyData()

	public byte[] getMeterReadingData() throws NestedIOException, ConnectionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (isConfirmed(RDM)) {
				while (true) {
					byte[] dataBlock = sendControlCode(NXT);
					if (dataBlock != null) {
						baos.write(dataBlock);
						if (dataBlock[0] == END_MARKER_AUTHENTICATION) {
							if (isConfirmed(CFM)) {
								break;
							}
						}
					} else {
                        throw new IOException("getMeterReadingData() -> An NULL dataBlock was returned, which causes the communication to stop.");
                    }
				}
			}
		} catch (IOException e) {
			throw new NestedIOException(e);
		}
		return baos.toByteArray();
	} // public byte[] getMeterReadingData()

	private static final int DUMMIES = 5; // KV 25062004 allow 5 dummies to receive...
	byte[] timeFrame = new byte[7];

	public byte[] getMeterReadingDataStream() throws NestedIOException, ConnectionException {
		int retries = 0;
		boolean first = true;
		while (true) {
			try {
				byte[] code = new byte[] { (byte) PAKNET_STREAMING, (byte) RDM, (byte) 0 };
				sendRawData(code);
				long timeout;
				int val;
				int length = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int count = 0;
				int timeCount = 0;
				int lastBlockCount = 0;
				int nrOfDummyBytes = 0;
				int checkSum = 0;

				timeout = System.currentTimeMillis() + protocolTimeout;
				copyEchoBuffer();
				while (true) {
					if ((val = readIn()) != -1) {
						timeout = System.currentTimeMillis() + protocolTimeout; // rearm timeout
						if (DEBUG >= 2) {
							ProtocolUtils.outputHex(((int) val));
						}

						// check first byte if different from 0x87 and 0x8A
						if (first) {
							if ((val != 0x87) && (val != 0x8A)) { // if there was a first non (0x87 and 0x8A) byte
								// received, wait for the second byte...
								if (nrOfDummyBytes++ >= DUMMIES) {
									throw new PACTConnectionException(
											"getLoadSurveyDataStream() too many dummy bytes received while waiting for 0x87 or 0x8A...",
											PROTOCOL_ERROR);
								}
								continue;
							}
						}

						// if there was a first non (0x87 and 0x8A) byte received, check the second byte
						if (first) {
							first = false;
							if (val != 0x87) { // if no timeframe, skip it! We suppose
								timeCount = 7;
							}
						}

						if (timeCount < 7) {
							timeFrame[timeCount] = (byte) val;
							timeCount++;
						} else {

							if (count == 0) {
								checkSum ^= val;
							}

							if (!((count == 0) || (count == 9))) {
								baos.write(val);
								checkSum ^= val;
							}

							if (count == 1) {
								if (val == END_MARKER_AUTHENTICATION) {
									lastBlockCount += 2;
								}
							}

							if (count++ >= 9) {
								count = 0;
								if (checkSum != val) {
									throw new PACTConnectionException("getMeterReadingDataStream() CRC error",
											CRC_ERROR);
								}
								checkSum = 0;
							}

							if (lastBlockCount > 0) {
								if (lastBlockCount++ >= 9) {
									// It might be that some CLEM programs send FF as the last byte. Therefor
									// we wait 1000 ms to be sure that everything is received before continue and we
									// flush the
									// input buffer.
									// See PAKNET doc!
									try {
										Thread.sleep(TO1);
										flushInputStream();
									} catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        throw ConnectionCommunicationException.communicationInterruptedException(e);
                                    }
									return baos.toByteArray();
								}
							} // if (lastBlockCount > 0)
						} // else timecount >= 7
					} // if ((val = readIn()) != -1)

					if (((long) (System.currentTimeMillis() - timeout)) > 0) {
						throw new PACTConnectionException("getLoadSurveyDataStream() timeout error", TIMEOUT_ERROR);
					}
				} // while(true)
			} catch (PACTConnectionException e) {
				if ((e.getReason() == TIMEOUT_ERROR) || (e.getReason() == PROTOCOL_ERROR)
						|| (e.getReason() == CRC_ERROR)) {
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
				} else {
					throw new NestedIOException(e);
				}
				delayAndFlush(TO1);
			}
		} // while(true)
	} // public byte[] getMeterReadingDataStream()

	private byte[] sendControlCode(int code) throws NestedIOException, ConnectionException {
		int retries = 0;
		while (true) {
			try {
				return doSendControlCode(code, true);
			} catch (PACTConnectionException e) {
				if ((e.getReason() == TIMEOUT_ERROR) || (e.getReason() == PROTOCOL_ERROR)) {
					if (DEBUG >= 1) {
						System.out.println("Exception caused by timeout/protocolError.");
						System.out.println("Retries currently : " + retries);
					}
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
					code = NXT;
//					code = PRE;
				} else if (e.getReason() == CRC_ERROR) {
					if (DEBUG >= 1) {
						System.out.println("Exception caused by CRCError.");
						System.out.println("Retries currently : " + retries);
					}
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
					code = PRE;
				} else {
					throw new NestedIOException(e);
				}
//				delayAndFlush(TO1);
			}
		} // while(true)
	} // private int sendControlCode(int code)

	public boolean isConfirmed(int code) throws NestedIOException, ConnectionException {
		return isConfirmed(new byte[] { (byte) code });
	}

	public boolean isConfirmed(byte[] code) throws NestedIOException, ConnectionException {
		int retries = 0;
		while (true) {
			try {
				return doConfirm(code);
			} catch (PACTConnectionException e) {
				if ((e.getReason() == TIMEOUT_ERROR) || (e.getReason() == PROTOCOL_ERROR)) {
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
				} else {
					throw new NestedIOException(e);
				}
			}
		} // while(true)
	} // public boolean isConfirmed(int code) throws NestedIOException, ConnectionException

	private byte[] doGetPasswordClearanceSeed() throws NestedIOException, ConnectionException, PACTConnectionException {
		sendRawData(new byte[] { (byte) REQUEST_PASSWORD_SEED });
		long timeout;
		int val;
		int count = 0;
		byte[] frame = new byte[15]; // max frame langth = 15 bytes
		timeout = System.currentTimeMillis() + protocolTimeout;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				if (DEBUG >= 2) {
					ProtocolUtils.outputHex(((int) val));
				}

				frame[count] = (byte) val;

				// if first byte is REQUEST_PASSWORD_SEED, password is not supported
				if ((count == 0) && (val == REQUEST_PASSWORD_SEED)) {
					return null;
				}

				else if ((count == 0) && (val != PASSWORD_SEED)) {
					return null;
				}

				if (count++ >= 4) {
					return frame;
				}

			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				throw new PACTConnectionException("doConfirm() timeout error", TIMEOUT_ERROR);
			}
		} // while(true)
	}

	private boolean doConfirm(byte[] code) throws NestedIOException, ConnectionException, PACTConnectionException {
		sendRawData(code);
		long timeout;
		int val;
		int count = 0;
		byte[] frame = new byte[15]; // max frame langth = 15 bytes
		int echoCount = 0;
		timeout = System.currentTimeMillis() + protocolTimeout;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				if (DEBUG >= 2) {
					ProtocolUtils.outputHex(((int) val));
				}

				frame[count] = (byte) val;

				// if first byte is CFM or TRM
				if (count == 0) {
					if ((val == CFM) || ((val == TRM) && (code[0] == (byte) TRM))) {
						return true;
					} else if (val == TRM) {
						return false;
					}
				}

				// as long as count == echocount, continue checking for duplication
				if (count == echoCount) {
					if ((count < code.length) && (frame[count] == code[count])) {
						if (echoCount++ >= (code.length - 1)) {
							// It might be that some CLEM programs send some bytes that should be absorbed!
							// we wait 2000 ms to be sure that everything is received before continue and we flush the
							// input buffer.
							// this behaviour was recognised with the IMServ field production meters...
							delayAndFlush(2000);
							throw new PACTConnectionException("doConfirm() protocol error", PROTOCOL_ERROR);
						}
					} else {
						// It might be that some CLEM programs send some bytes that should be absorbed!
						// we wait 2000 ms to be sure that everything is received before continue and we flush the
						// input buffer.
						// this behaviour was recognised with the IMServ field production meters...
						delayAndFlush(2000);
						throw new PACTConnectionException("doConfirm() protocol error", PROTOCOL_ERROR);
					}
				}

				count++;

			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				throw new PACTConnectionException("doConfirm() timeout error", TIMEOUT_ERROR);
			}
		} // while(true)
	} // private int doConfirm(byte[] code)

	private byte[] doSendControlCode(int code, boolean blockTransaction) throws NestedIOException, ConnectionException,
			PACTConnectionException {
		sendRawData((byte) code);
		long timeout;
		int val;
		int length = 0;
		byte[] frame = new byte[15]; // max frame langth = 15 bytes
		int count = 0;
		// int echoCount=0;
		timeout = System.currentTimeMillis() + protocolTimeout;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				if (DEBUG >= 2) {
					ProtocolUtils.outputHex(((int) val));
				}
				frame[count] = (byte) val;

				if (count == 0) {
					length = val & 0x0F;
					if (val == TRM) {
						return null;
					} else if (val == CFM) {
						return null;
					} else if (length == 0) {
						return null;
					}
				} else if (blockTransaction && (count >= (length - 1))) {
					if (calcChecksum(frame, length) == frame[length - 1]) {
						return ProtocolUtils.getSubArray2(frame, 1, length - 2);
					} else {
						throw new PACTConnectionException("doSendControlCode() CRC error", CRC_ERROR);
					}
				} else if (!blockTransaction && (count >= (length - 1))) {
					return frame; // ProtocolUtils.getSubArray2(frame,1,length-1);
					// return ProtocolUtils.getSubArray2(frame, 0, length);
				}
				count++;

			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				throw new PACTConnectionException("doSendControlCode() timeout error", TIMEOUT_ERROR);
			}
		} // while(true)
	} // doSendControlCode()

	public byte[] sendStringRequest(String str) throws NestedIOException, ConnectionException {
		int retries = 0;
		while (true) {
			try {
				return doSendStringRequest(str);
			} catch (PACTConnectionException e) {
				if (e.getReason() == TIMEOUT_ERROR) {
					if (retries++ >= maxRetries) {
						throw new NestedIOException(e);
					}
					delayAndFlush(2000);
				} else {
					throw new NestedIOException(e);
				}
			}
		} // while(true)
	}

	private byte[] doSendStringRequest(String str) throws NestedIOException, ConnectionException,
			PACTConnectionException {
		String sendStr = str + "\r";
		sendRawData(sendStr.getBytes());
		long timeout;
		int val;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		timeout = System.currentTimeMillis() + protocolTimeout;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				if (DEBUG >= 2) {
					ProtocolUtils.outputHex(((int) val));
				}

				if (val == 0x0D) {
					return baos.toByteArray();
				}
				baos.write(val);

			} // if ((val = readIn()) != -1)

			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				throw new PACTConnectionException("sendStringRequest() timeout error", TIMEOUT_ERROR);
			}
		} // while(true)

	} // public byte[] sendStringRequest(String str) throws NestedIOException, ConnectionException,

	// PACTConnectionException

	/*
	 * flushBuffer() sends 10 characters, closed by a 0x0D to the meter in order to be sure that the meter's internal
	 * buffer is empty. We should ignore all received characters and wait for 1000 ms (see doc 'introduction to
	 * metercommunication') after the last received character before returning.
	 */
	private void flushBuffer() throws NestedIOException, ConnectionException, PACTConnectionException {
		int val;
		long flushTimeout, timeout;
		byte[] data = new byte[] { 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x0D };
		sendRawData(data);
		flushTimeout = System.currentTimeMillis() + PACT_FLUSH_TIMEOUT;
		timeout = System.currentTimeMillis() + protocolTimeout;
		copyEchoBuffer();
		while (true) {
			if ((val = readIn()) != -1) {
				flushTimeout = System.currentTimeMillis() + PACT_FLUSH_TIMEOUT;
				// if (val == 0x0D) return;
			} // if ((val = readIn()) != -1)
			if (((long) (System.currentTimeMillis() - flushTimeout)) > 0) {
				return;
			}
			if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				throw new PACTConnectionException("flushBuffer() timeout error", TIMEOUT_ERROR);
			}
		} // while(true)
	}

	public long getForceDelay() {
		return forceDelay;
	}

	public void setForceDelay(long forceDelay) {
		this.forceDelay = forceDelay;
	}

	public static void main(String[] args) throws IOException {

		Calendar cal = Calendar.getInstance();

		// byte[] data = new byte[]{(byte)0x00,(byte)0x00,(byte)0x47,(byte)0x11,(byte)0x44,(byte)0x28};
		// byte[] data = new byte[]{(byte)0x00,(byte)0x40,(byte)0x00,(byte)0x00,(byte)0x53,(byte)0x11};
		// byte[] data = new byte[]{(byte)0x00,(byte)0x00,(byte)0x68,(byte)0x00,(byte)0x45,(byte)0x28};
		// byte[] data = new byte[]{(byte)0x00,(byte)0x87,(byte)0x00,(byte)0x00,(byte)0x70,(byte)0x40};
		// byte[] data = new byte[]{(byte)0x00,(byte)0x00,(byte)0x3D,(byte)0x00,(byte)0x43,(byte)0x28};
		byte[] data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x2A, (byte) 0xF9, (byte) 0x87 };

		// 87 0 0 49 0 0 0

		DateTime dateTime = new DateTime(data, cal.getTimeZone());
		System.out.println(dateTime.getDate());

	}

} // public class PACTConnection extends Connection
