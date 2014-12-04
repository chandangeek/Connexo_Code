package com.energyict.dlms;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *  <pre>
 * The Consereth protocol runs over TCP/IP and enables transparent sending of any sort of messages through TCP/IP networks.
 * In this particular case, Consereth is be used to transfer HDLC packets over TCP/IP.
  * </pre>
 *
 * Consereth messages consists of 3 byte header and the payload message:<BR>
 * -------------------------------------------------------------<BR>
 * |         Header                          |     Payload     |<BR>
 * | Message ID | Length (HI) | Length (LOW) |     Payload     |<BR>
 * -------------------------------------------------------------<BR>
 * <br>
 * Message ID (1 byte): Defines the message type (e.g.: request/response/data)<br>
 * Length (2 bytes): The length of the payload<br>
 * Payload: the wrapped HDLC frame<br>
 * <br>
 * <b>Remark:</b> Payload is limited to 200 bytes - if a larger HDLC frame has to be transmitted, the frame should be split up in multiple consereth frames,
 * each containing at max 200 bytes of data; but that is currently not implemented in this class, cause as information field size is limited to 128 bytes, the
 * total length of each HDLC frame is below 200 bytes - so segmentation is never needed.
 *
 * @author sva
 * @since 21/10/13 - 11:34
 */
public class ConserethHDLC2Connection extends HDLC2Connection {

    private static final int WAIT_FOR_CONSERETH_DATA_TYPE = 0x00;
    private static final int WAIT_FOR_CONSERETH_LENGTH_HIGH_BYTE = 0x01;
    private static final int WAIT_FOR_CONSERETH_LENGTH_LOW_BYTE = 0x02;
    private static final int WAIT_FOR_CONSERETH_PAYLOAD = 0x03;

    private static final int CONSERETH_DATA_TYPE_REQUEST = 0x01;
    private static final int CONSERETH_DATA_TYPE_RESPOND = 0x02;
    private static final int CONSERETH_DATA_TYPE_DATA = 0x03;

    private static final int  STATE_MACHINE_TIMEOUT=0x02;
    private static final int CONSERETH_MAXIMUM_PAYLOAD_SIZE = 200;

    public ConserethHDLC2Connection(InputStream inputStream, OutputStream outputStream, int iTimeout, long lForceDelay, int iMaxRetries, int iClientMacAddress, int iServerLowerMacAddress, int iServerUpperMacAddress, int addressingMode, int informationFieldSize, int hhuSignonBaudRateCode) throws DLMSConnectionException, ConnectionException {
        super(inputStream, outputStream, iTimeout, lForceDelay, iMaxRetries, iClientMacAddress, iServerLowerMacAddress, iServerUpperMacAddress, addressingMode, informationFieldSize > 128 ? 128 : informationFieldSize, hhuSignonBaudRateCode);
    }

    @Override
    protected void sendFrame(byte[] byteBuffer) throws IOException {
        DLMSUtils.delay(getlForceDelay());
        byte[] dataToSendOut = addConserethHeaders(addHDLCFrameFlags(byteBuffer));
        sendOut(dataToSendOut);
    }

    protected byte[] addConserethHeaders(byte[] byteBuffer) {
        byte[] data = new byte[byteBuffer.length + 3];
        data[0] = CONSERETH_DATA_TYPE_DATA;
        data[1] = (byte) ((byteBuffer.length & 0xFF00) >> 8);
        data[2] = (byte) (byteBuffer.length & 0x00FF);
        System.arraycopy(byteBuffer, 0, data, 3, byteBuffer.length);
        return data;
    }

    @Override
    protected byte waitForHDLCFrameStateMachine(long iTimeout, byte[] byteReceiveBuffer) throws DLMSConnectionException, IOException {
        long lMSTimeout = System.currentTimeMillis() + iTimeout;
        int sRXCount = 0;
        int sLength=0;
        byte[] bLength = new byte[2];
        int bCurrentState=WAIT_FOR_CONSERETH_DATA_TYPE;

        copyEchoBuffer();

        try {
        	while(true) {
        		int inewKar = readIn();
        		if (inewKar != -1) {
        			switch (bCurrentState) {
        				case WAIT_FOR_CONSERETH_DATA_TYPE:
    						switch((byte)inewKar) {
                                case CONSERETH_DATA_TYPE_DATA:
                                    sRXCount= 0;
    								bCurrentState = WAIT_FOR_CONSERETH_LENGTH_HIGH_BYTE;
    								break;
                                default:
                                    throw new DLMSConnectionException("ConserethHDLC2Connection, waitForHDLCFrameStateMachine, unsupported consereth message type (" + inewKar + ") - Only DATA (1) type is supported");
                            }
                            break;

                        case WAIT_FOR_CONSERETH_LENGTH_HIGH_BYTE:
                            bLength[0] = (byte) inewKar;
                            bCurrentState = WAIT_FOR_CONSERETH_LENGTH_LOW_BYTE;
                            break;

                        case WAIT_FOR_CONSERETH_LENGTH_LOW_BYTE:
                            bLength[1] = (byte) inewKar;
                            sLength = ((bLength[0] & 0x0FF) << 8) + (bLength[1] & 0x0FF);
                            bCurrentState = WAIT_FOR_CONSERETH_PAYLOAD;
                            break;

        				case WAIT_FOR_CONSERETH_PAYLOAD:
                            byteReceiveBuffer[sRXCount++] = (byte) inewKar;
                            if (sRXCount >= sLength) {  // ToDo: Implement fragmentation - cause large HDLC frames can be fragmented in multiple consereth frames (each containing 200 bytes).
                                                        // ToDo: But as we limited the 'InformationFieldSize' to maximum 128, this should be never the case
                                return super.runHDLCFrameStateMachine(byteReceiveBuffer);
                            }
                            break;
        			}
                }
                if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
					return STATE_MACHINE_TIMEOUT;
				}
            }
        } catch(ArrayIndexOutOfBoundsException e) {
           throw new DLMSConnectionException(e.getMessage());
        }
    }
}
