/*
 * AlphaConnection.java
 *
 * Created on 5 juli 2005, 11:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Koen
 */
public class AlphaConnection extends Connection implements ProtocolConnection {

    private static final int DEBUG=0;
    private static final long TIMEOUT=60*30*1000; // 30 minutes

    int timeout;
    int maxRetries;
    long forcedDelay;
    long whoAreYouTimeout;
    private boolean optical=false;

    public static final int FRAME_RESPONSE_TYPE_ACK_NAK=0;
    public static final int FRAME_RESPONSE_TYPE_WHO_ARE_YOU=1;
    public static final int FRAME_RESPONSE_TYPE_DATA_SINGLE=2;
    public static final int FRAME_RESPONSE_TYPE_DATA_MULTIPLE=3;
    public static final int FRAME_RESPONSE_TYPE_SHORT_FORMAT=4;


    /** Creates a new instance of AlphaConnection */
    public AlphaConnection(InputStream inputStream,
                         OutputStream outputStream,
                         int timeout,
                         int maxRetries,
                         long forcedDelay,
                         int echoCancelling,
                         HalfDuplexController halfDuplexController,long whoAreYouTimeout) {
          super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
          this.timeout = timeout;
          this.maxRetries=maxRetries;
          this.forcedDelay=forcedDelay;
          this.whoAreYouTimeout=whoAreYouTimeout;
    }



    public void delayAndFlush(long delay)  throws ConnectionException,NestedIOException {
        super.delayAndFlush(delay);
    }

    protected void waitForTakeControl() throws IOException {
        ResponseFrame responseFrame = receiveFrame(FRAME_RESPONSE_TYPE_SHORT_FORMAT); // receive take control...
    }

    protected void response2AreYouOK() throws IOException {
        int retry=0;
        while(true) {
            try {
                ResponseFrame responseFrame = receiveFrame(FRAME_RESPONSE_TYPE_SHORT_FORMAT);
                sendOut(assembleFrame(new byte[]{(byte)ShortFormatCommand.COMMANDBYTE_ARE_YOU_OK,0,0})); // ACK
                responseFrame = receiveFrame(FRAME_RESPONSE_TYPE_SHORT_FORMAT);
                // 20 is not stat but 20 x 8 ms delay before sending the take control!
                byte[] data = new byte[]{(byte)ShortFormatCommand.COMMANDBYTE_SET_BAUDRATE_9600,0,20}; // ACK
                sendOut(assembleFrame(data));
                // KV_TO_DO, remove the 200 extra!
                delayUsingBaudForDatalength(assembleFrame(data),1200,0); //, 200); // delay necessary to let the buffer flushed out!
                break;
            }
            catch(ConnectionException e) {
                if (retry++>=getMaxRetries()) {
                    throw new ProtocolConnectionException("response2AreYouOK() error maxRetries ("+getMaxRetries()+"), "+e.getMessage());
                }
            }
        }
    }

    protected void response2AreYouOKInSession() throws IOException {
        int retry=0;
        while(true) {
            try {
                sendOut(assembleFrame(new byte[]{(byte)ShortFormatCommand.COMMANDBYTE_ARE_YOU_OK,0,0})); // ACK
                ResponseFrame responseFrame = receiveFrame(FRAME_RESPONSE_TYPE_SHORT_FORMAT);
                // 20 is not stat but 20 x 8 ms delay before sending the take control!
                byte[] data = new byte[]{(byte)ShortFormatCommand.COMMANDBYTE_SET_BAUDRATE_9600,0,20}; // ACK
                sendOut(assembleFrame(data));
                // KV_TO_DO, remove the 200 extra!
                delayUsingBaudForDatalength(assembleFrame(data),1200,0); //, 200); // delay necessary to let the buffer flushed out!
                break;
            }
            catch(ConnectionException e) {
                if (retry++>=getMaxRetries()) {
                    throw new ProtocolConnectionException("response2AreYouOK() error maxRetries ("+getMaxRetries()+"), "+e.getMessage());
                }
            }
        }
    }

    private void delayUsingBaudForDatalength(byte[] data,int baudrate,long extra) throws NestedIOException {
        // calc sleeptime using baudrate and length of data
        try {
            long val = (data.length*10*1000)/baudrate;
            Thread.sleep(val+extra);
        }
        catch(InterruptedException e) {
            throw new NestedIOException(e);
        }
    }

    /*******************************************************************************************
     * PROTECTED METHODS
     ******************************************************************************************/
    protected ResponseFrame sendCommand(byte[] data,int expectedFrameType, boolean response) throws IOException {
        int retry=0;
        while(true) {
            try {
                sendFrame(data);
                if (response) {
                    ResponseFrame responseFrame = receiveFrame(expectedFrameType);
                    if (!responseFrame.isAck()) {
                        throw new ProtocolConnectionException("sendCommand() NAK received, reason: " + responseFrame.getNakReason(), PROTOCOL_ERROR);
                    }
                    return responseFrame;
                }
                else {
                    return null;
                }
            }
            catch (ConnectionException e) {

                if (DEBUG >= 2) {
                    e.printStackTrace();
                }

                int mr=getMaxRetries();
                if (expectedFrameType == FRAME_RESPONSE_TYPE_WHO_ARE_YOU) {
                    mr = 30;
                }
                if (retry++>=mr) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+mr+"), "+e.getMessage());
                }
            }
        }

    }

    /*******************************************************************************************
     * PRIVATE METHODS
     ******************************************************************************************/
    private void sendFrame(byte[] data) throws IOException {

//System.out.print("TX FRAME: ");
//ProtocolUtils.printResponseDataFormatted2(assembleFrame(data));

        delay(forcedDelay);
        sendOut(assembleFrame(data));
    }

    final String[] NAKSTR = {"No error", // 0=ACK
                             "Bad CRC",  // 1..n NAK
                             "Communications Lockout against this Function",
                             "Illegal command, syntax, or length" ,
                             "Framing error",
                             "Timeout error (Internal System Error)",
                             "Invalid password",
                             "NAK received from computer",
                             "","","","",
                             "Request In Process, Try Again Later (This is a polling response)",
                             "Too Busy to Honor Request, Try again Later",
                             "",
                             "Rules Class NAK. Request not supported by current class"};

    private static final byte STATE_WAIT_FOR_STX=0;
    private static final byte STATE_WAIT_FOR_CB=1;
    private static final byte STATE_WAIT_FOR_LEN=2;
    private static final byte STATE_WAIT_FOR_STAT=3;
    private static final byte STATE_WAIT_FOR_ACK_NAK=4;
    private static final byte STATE_WAIT_FOR_DATA=5;
    private static final byte STATE_WAIT_FOR_CRC=6;
    private static final byte STATE_WAIT_FOR_CRC_ON_ARE_YOU_OK=7;

    private ResponseFrame receiveFrame(int expectedFrameType) throws IOException {
        long protocolTimeout,interFrameTimeout;
        int kar;
        int state;
        int len=0;
        boolean lastPacket=true;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
        ResponseFrame responseFrame=new ResponseFrame();
        responseFrame.setExpectedFrameType(expectedFrameType);
        int count=0;
        state=STATE_WAIT_FOR_STX;
        int areYouOk=0;

        // Should send the who are you frames very quickly!!
        if (expectedFrameType == FRAME_RESPONSE_TYPE_WHO_ARE_YOU) {
            interFrameTimeout = System.currentTimeMillis() + whoAreYouTimeout;
        }
        else {
            interFrameTimeout = System.currentTimeMillis() + timeout;
        }

        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        resultArrayOutputStream.reset();
        allDataArrayOutputStream.reset();

        if (DEBUG >= 2) {
            System.out.println("doReceiveData(...):");
        }
        copyEchoBuffer();
        while(true) {

            if ((kar = readIn()) != -1) {
                if (DEBUG >= 2) {
                    System.out.print(",0x");
                }
                allDataArrayOutputStream.write(kar);

                switch(state) {
                    case STATE_WAIT_FOR_STX: {
                        if (kar == STX) {
                            interFrameTimeout = System.currentTimeMillis() + timeout;
                            switch(expectedFrameType) {

                                case FRAME_RESPONSE_TYPE_WHO_ARE_YOU: {
                                    len = 12;
                                    count=0;
                                    //responseFrame.setLen(len);
                                    responseFrame.setAck(true);
                                    state = STATE_WAIT_FOR_DATA;
                                } break; // FRAME_RESPONSE_TYPE_DATA_MULTIPLE
                                default: {
                                    len=count=0;
                                    state = STATE_WAIT_FOR_CB;
                                } break; // default
                            } // switch(expectedFrameType)
                        }
                    } break; // STATE_WAIT_FOR_STX

                    case STATE_WAIT_FOR_CB: {

                        // KV 27062007 Some meters seem to send that ARE YOU OK MESSAGE in between...
                        if ((kar == ShortFormatCommand.COMMANDBYTE_ARE_YOU_OK) && (!isOptical())) {
                            count=1;
                            state = STATE_WAIT_FOR_CRC_ON_ARE_YOU_OK;
                            if (DEBUG >= 1) {
                                System.out.println("KV_DEBUG> STATE_WAIT_FOR_CRC_ON_ARE_YOU_OK receivd!");
                            }
                        }
                        else {
                            if (expectedFrameType == FRAME_RESPONSE_TYPE_SHORT_FORMAT) {
                                state = STATE_WAIT_FOR_CRC;
                                count=1;
                            }
                            else {
                                 state = STATE_WAIT_FOR_ACK_NAK;
                            }
                            responseFrame.setCommandByte(kar);
                        }
                    } break; // STATE_WAIT_FOR_CB

                    case STATE_WAIT_FOR_CRC_ON_ARE_YOU_OK: {

                        if (count-- <= 0) {
                            // send ACK
                            //sendOut(assembleFrame(new byte[]{(byte)ShortFormatCommand.COMMANDBYTE_ARE_YOU_OK,0,0})); // ACK
                            response2AreYouOKInSession();
                            state = STATE_WAIT_FOR_STX;
                            resultArrayOutputStream.reset();
                            allDataArrayOutputStream.reset();
                            count=0;
                            if (areYouOk++ >=2) {
                                throw new ProtocolConnectionException("receiveFrame(), are you ok sequence for dial in meter!", PROTOCOL_ERROR);
                            }
                        }

                    } break;

                    case STATE_WAIT_FOR_ACK_NAK: {
                        if (kar == 0) { // ACK received?
                            responseFrame.setAck(true);
                            switch(expectedFrameType) {
                                case FRAME_RESPONSE_TYPE_ACK_NAK : {
                                    state = STATE_WAIT_FOR_STAT;
                                } break; // FRAME_RESPONSE_TYPE_ACK_NAK
                                case FRAME_RESPONSE_TYPE_DATA_SINGLE : {
                                    state = STATE_WAIT_FOR_STAT;
                                    len=0;
                                    count=0;
                                } break; // FRAME_RESPONSE_TYPE_DATA_SINGLE
                                case FRAME_RESPONSE_TYPE_DATA_MULTIPLE : {
                                    state = STATE_WAIT_FOR_LEN;
                                    len=0;
                                    count=1;
                                } break; // FRAME_RESPONSE_TYPE_DATA_MULTIPLE
                                default: {
                                    throw new ProtocolConnectionException("receiveFrame() wrong frametype "+expectedFrameType+"!",PROTOCOL_ERROR);
                                }
                            } // switch(expectedFrameType)
                        }
                        else {
                            // NAK received, check
                            state = STATE_WAIT_FOR_STAT;
                            if (kar < NAKSTR.length) {
                                responseFrame.setNakReason(NAKSTR[kar]);
                            }
                            responseFrame.setAck(false);
                        }
                    } break; // STATE_WAIT_FOR_ACK_NAK

                    case STATE_WAIT_FOR_LEN: {
                        lastPacket=true;
                        len|=(count==1?kar<<8:kar);
                        if (count-- <= 0) {

                            if (DEBUG >= 2) {
                                System.out.println("KV_DEBUG> AlphaConnection, receiveData, STATE_WAIT_FOR_LEN, len = 0x" + Integer.toHexString(len) + ", expectedFrameType=0x" + Integer.toHexString(expectedFrameType));
                            }

                            switch(expectedFrameType) {
                                case FRAME_RESPONSE_TYPE_DATA_SINGLE : {
                                    lastPacket = (len&0x80)==0x80;
                                    len &= 0x7F;
                                } break; // FRAME_RESPONSE_TYPE_DATA_SINGLE
                                case FRAME_RESPONSE_TYPE_DATA_MULTIPLE : {
                                    lastPacket = (len&0x8000)==0x8000;
                                    len &= 0x0FFF;
                                } break; // FRAME_RESPONSE_TYPE_DATA_MULTIPLE
                                default: {
                                    throw new ProtocolConnectionException("receiveFrame() no len state allowed for frame type "+expectedFrameType+"!",PROTOCOL_ERROR);
                                }
                            } // switch(expectedFrameType)



                            //responseFrame.setLen(len);
                            state = STATE_WAIT_FOR_DATA;
                        } // if (count-- <= 0)

                    } break; // STATE_WAIT_FOR_LEN

                    case STATE_WAIT_FOR_STAT: {
                        responseFrame.setStat(kar);
                        switch(expectedFrameType) {
                            case FRAME_RESPONSE_TYPE_DATA_SINGLE : {
                                if (responseFrame.isAck()) {
                                    state = STATE_WAIT_FOR_LEN;
                                }
                                else {
                                    state = STATE_WAIT_FOR_CRC;
                                    count=1;
                                }
                            } break; // FRAME_RESPONSE_TYPE_DATA_SINGLE

                            case FRAME_RESPONSE_TYPE_ACK_NAK: {
                                state = STATE_WAIT_FOR_CRC;
                                count=1;
                            } break; // FRAME_RESPONSE_TYPE_ACK_NAK

                            default: {
                                throw new ProtocolConnectionException("receiveFrame() no stat state allowed for frame type "+expectedFrameType+"!",PROTOCOL_ERROR);
                            }
                        } // switch(expectedFrameType)

                    } break; // STATE_WAIT_FOR_STAT

                    case STATE_WAIT_FOR_DATA: {
                        // receive len bytes
                        resultArrayOutputStream.write(kar);

                        if (--len <= 0) {
                            state = STATE_WAIT_FOR_CRC;
                            count=1;
                        } // if (--len <= 0)
                    } break; // STATE_WAIT_FOR_DATA

                    case STATE_WAIT_FOR_CRC: {
                        if (count-- <= 0) {
                            // validate CRC
                            byte[] data = allDataArrayOutputStream.toByteArray();
                            if (CRCGenerator.isCRCAlphaValid(data)) {
                                if (lastPacket) {
                                    responseFrame.setData(resultArrayOutputStream.toByteArray());
                                    return responseFrame;
                                }
                                else {
                                    allDataArrayOutputStream.reset();
                                    state = STATE_WAIT_FOR_STX;
                                    lastPacket = true;
                                    sendFrame(new byte[]{(byte) ShortFormatCommand.COMMANDBYTE_CONTINUE_READ});
                                }
                            }
                            else {
                                throw new ProtocolConnectionException("receiveFrame() response crc error",CRC_ERROR);
                            }
                        }
                    } break; // STATE_WAIT_FOR_CRC

                }
            }

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }
        }
    }

    private byte[] assembleFrame(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(STX);
        baos.write(data,0, data.length);
        int crc = CRCGenerator.calcCRCAlpha(baos.toByteArray());
        baos.write((crc>>8)&0xFF);
        baos.write(crc&0xFF);
        return baos.toByteArray();
    }



    /*******************************************************************************************
     * Implementation of the abstract Connection class
     ******************************************************************************************/
    public void setHHUSignOn(HHUSignOn hhuSignOn) {

    }
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {

    }

    public MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) throws IOException {
        return null;
    }

    public byte[] dataReadout(String strID,String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean isOptical() {
        return optical;
    }

    public void setOptical(boolean optical) {
        this.optical = optical;
    }



}
