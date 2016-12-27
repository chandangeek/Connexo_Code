/*
 * EZ7Connection.java
 *
 * Created on 9 mei 2005, 9:57
 */

package com.energyict.protocolimpl.emon.ez7.core;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.base.SecurityLevelException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 *
 * @author  Koen
 */
public class EZ7Connection extends Connection  implements ProtocolConnection {

    private static final int DEBUG=0;
    private static final long TIMEOUT=600000;

    int timeout;
    int maxRetries;
    String nodeId;

    private static final char[] endFrame={'E','O','T','*',']','\r','\n'};
    private int endFrameCount;


    public EZ7Connection(InputStream inputStream,
                         OutputStream outputStream,
                         int timeout,
                         int maxRetries,
                         long forcedDelay,
                         int echoCancelling,
                         HalfDuplexController halfDuplexController) {
          super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
          this.timeout = timeout;
          this.maxRetries=maxRetries;


    } // EZ7Connection(...)

    public byte[] sendCommand(String cmd) throws ConnectionException,NestedIOException {
        return sendCommand(cmd,null);
    }

    public byte[] sendCommand(String cmd, String data) throws ConnectionException,NestedIOException {
        return sendCommand(cmd, data, true);
    }

    public byte[] sendCommand(String cmd, String data, boolean receive) throws ConnectionException,NestedIOException {
        int retry=0;
        while(true) {
            try {
                String command = nodeId+"*."+cmd+(data==null?"":(" "+data));
                String crc = ProtocolUtils.buildStringHex(CRCGenerator.calcCRCFull(("!"+command).getBytes()),4);
                sendOut(("!"+command+" "+crc+"\r").getBytes());
                if (receive) {
                    byte[] responseData = receiveResponse(command.getBytes());
                    if ((responseData.length == 5) && (responseData[2] == '?'))
                        throw new SecurityLevelException("EZ7Connection, sendCommand("+cmd+"), possibly wrong access level! Log onto the meter with the correct password!");
                    return responseData;
                }
                else return null;

            }
            catch(SecurityLevelException e) {
                throw e;
            }
            catch(ConnectionException e) {
                if (retry++>=maxRetries) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage(), MAX_RETRIES_ERROR);
                }
            }
        }
    } // public byte[] sendCommand(String command, String data) throws ConnectionException


    private static final byte STATE_WAIT_FOR_START=0;
    private static final byte STATE_WAIT_FOR_COMMAND_ECHO=1;
    private static final byte STATE_WAIT_FOR_DATA=2;

    private byte[] receiveResponse(byte[] command) throws NestedIOException, ConnectionException {
        long protocolTimeout,interFrameTimeout;
        int kar;
        int state;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

        int count=0;
        //byte[] crcBlock=new byte[10];
        // init
        state=STATE_WAIT_FOR_START;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        interFrameTimeout = System.currentTimeMillis() + timeout;
        resultArrayOutputStream.reset();
        allDataArrayOutputStream.reset();

        if (DEBUG == 1) {
            System.out.println("doReceiveData(...):");
        }
        copyEchoBuffer();

        while(true) {

            if ((kar = readIn()) != -1) {
                if (DEBUG == 1) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex(kar);
                }


                switch(state) {
                    case STATE_WAIT_FOR_START: {
                        if ((byte)kar == '@') {
                            allDataArrayOutputStream.write(kar);
                            state = STATE_WAIT_FOR_COMMAND_ECHO;
                            count=0;
                        }
                    } break; // STATE_WAIT_FOR_START

                    case STATE_WAIT_FOR_COMMAND_ECHO: {
                        allDataArrayOutputStream.write(kar);
                        if ((command[count]&0xFF) != kar) {
                            throw new ProtocolConnectionException("doReceiveData() response frame error", FRAME_ERROR);
                        }
                        if (count++>=(command.length-1)) {
                            endFrameCount=0;
                            state = STATE_WAIT_FOR_DATA;
                        }
                    } break; // STATE_WAIT_FOR_COMMAND_ECHO

                    case STATE_WAIT_FOR_DATA: {

                        // In case of high timeouts and defective meter, an outofmemory could happen. So, limit the receivebuffer!
                        if (allDataArrayOutputStream.size() > 100000) {
                            throw new ProtocolConnectionException("doReceiveData() response data > 100K!", PROTOCOL_ERROR);
                        }

                        resultArrayOutputStream.write(kar);
                        allDataArrayOutputStream.write(kar);
                        if (endFrameReceived(kar)) {
                            byte[] result = ProtocolUtils.getSubArray(resultArrayOutputStream.toByteArray(), 0, resultArrayOutputStream.toByteArray().length-(14+1));
                            byte[] all = ProtocolUtils.getSubArray(allDataArrayOutputStream.toByteArray(), 0, allDataArrayOutputStream.toByteArray().length-(14+1));
                            byte[] crcBlock = ProtocolUtils.getSubArray(resultArrayOutputStream.toByteArray(), resultArrayOutputStream.toByteArray().length-14);

                            String crcGen = ProtocolUtils.buildStringHex(CRCGenerator.calcCRCFull(all),4);
                            String crcRx = new String(ProtocolUtils.getSubArray(crcBlock,2,5));

                            if (DEBUG>=1) {
                               System.out.println();
                               System.out.println("GEN="+crcGen+", RX="+crcRx.toLowerCase());
                            }

                            if (crcGen.compareTo(crcRx.toLowerCase())!= 0) {
                                throw new ProtocolConnectionException("doReceiveData() response crc error", CRC_ERROR);
                            } else {
                                return result;
                            }
                        }

                    } // STATE_WAIT_FOR_DATA

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("doReceiveData() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("doReceiveData() interframe timeout error",TIMEOUT_ERROR);
            }
        } // while(true)
    } // private byte[] receiveResponse(byte[] command) throws NestedIOException, ConnectionException

    private boolean endFrameReceived(int kar) {
        if ((char)kar == endFrame[endFrameCount]) {
           endFrameCount++;
           if (endFrameCount == endFrame.length) {
               return true;
           }
        }
        else {
           endFrameCount=0;
        }
        return false;
    }

    public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
        this.nodeId=nodeId;
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

 // private byte[] receiveResponse(String request)

} // public class EZ7Connection extends Connection
