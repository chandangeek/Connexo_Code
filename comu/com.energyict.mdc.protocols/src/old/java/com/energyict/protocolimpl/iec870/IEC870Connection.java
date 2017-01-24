/*
 * IEC870Connection.java
 *
 * Created on 19 juni 2003, 11:20
 */

package com.energyict.protocolimpl.iec870;

import com.energyict.dialer.connection.Connection;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class IEC870Connection extends Connection {

    final int DEBUG=0;

    // General attributes
    private InputStream inputStream;
    private int iProtocolTimeout;
    int iMaxRetries;
    TimeZone timeZone=null;

    // frame variables
    int rtuAddress=0xFFFF;
    boolean fcb=false;


    IEC870Frame currentFrameRx=null;
    IEC870Frame currentFrameTx=null;

    /** Creates a new instance of IEC870Connection */
    public IEC870Connection(InputStream inputStream,
                            OutputStream outputStream,
                            int iTimeout,
                            int iMaxRetries,
                            long lForceDelay,
                            int iEchoCancelling,
                            TimeZone timeZone) throws ConnectionException {
        super(inputStream,outputStream,lForceDelay,iEchoCancelling);
        this.inputStream = inputStream;
        this.iMaxRetries = iMaxRetries;
        this.timeZone = timeZone;

        iProtocolTimeout=iTimeout;

        currentFrameRx=null;
        currentFrameTx=null;

        rtuAddress = 0xFFFF;

    } // public FlagIEC1107Connection(...)

    private void printFrame(IEC870Frame frame) {
        System.out.println(frame.toString(timeZone));
    }

    public void connectLink() throws NestedIOException,IEC870ConnectionException {
        try {
            IEC870Frame frame;
            int retries=0;
            while(true) {
                try {
                    rtuAddress = 0xFFFF;
                    sendFrame(IEC870Frame.CONTROL_SEND_CONFIRM_RESET_REMOTE_LINK);
                    waitFor(IEC870Frame.CONTROL_CONFIRM_ACK);
                    rtuAddress = currentFrameRx.getAddress();
                    sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_STATUS_LINK);
                    waitFor(IEC870Frame.CONTROL_RESPOND_STATUS_LINK);
                    return;
                }
                catch(ConnectionException e) {
                    if (retries++ > (iMaxRetries-1)) {
                        throw new IEC870ConnectionException("IEC870Connection, connectLink, max retries"+MAX_RETRIES_ERROR);
                    }
                }
            } // while(true)
        }
        catch(ConnectionException e) {
            throw new IEC870ConnectionException("IEC870Connection, connectLink, ConnectionException, "+e.getMessage());
        }
    }

    public void disconnectLink() throws IEC870ConnectionException {

    }

    public int getRTUAddress() {
       return rtuAddress;
    }

    /*
     * Copy currentFrameTx to previousFrameTx frame.
     * Adjust FCB in currentFrameTx.
     * Send currentFrameTx.
     */
    private void sendFrame(int function) throws NestedIOException, IEC870ConnectionException {
        try {
            currentFrameTx = new IEC870Frame(function,rtuAddress);
            fcb = currentFrameTx.toggleFCB(fcb);
            if (DEBUG >= 1) printFrame(currentFrameTx);
            sendRawData(currentFrameTx.getData());
        }
        catch(ConnectionException e) {
            throw new IEC870ConnectionException("IEC870Connection, sendFrame, ConnectionException, "+e.getMessage());
        }
    }


    /*
     * Copy currentFrameTx to previousFrameTx frame.
     * Adjust FCB in currentFrameTx.
     * Send currentFrameTx.
     */
    private void sendFrame(int function, IEC870ASDU asdu) throws NestedIOException, IEC870ConnectionException {
        try {
            currentFrameTx = new IEC870Frame(function,rtuAddress,asdu);
            fcb=currentFrameTx.toggleFCB(fcb);
            if (DEBUG >= 1) printFrame(currentFrameTx);
            sendRawData(currentFrameTx.getData());
        }
        catch(ConnectionException e) {
            throw new IEC870ConnectionException("IEC870Connection, sendFrame, ConnectionException, "+e.getMessage());
        }
    }

    private void reSendCurrentFrameTx() throws NestedIOException, IEC870ConnectionException {
        if (DEBUG >= 1) printFrame(currentFrameTx);
        try {
            sendRawData(currentFrameTx.getData());
        }
        catch(ConnectionException e) {
            throw new IEC870ConnectionException("IEC870Connection, sendCurrentFrame, ConnectionException, "+e.getMessage());
        }
    }

    public List sendConfirm(IEC870ASDU asdu) throws NestedIOException,ConnectionException {
        List asdus=null;
        int retries=0;
        sendFrame(IEC870Frame.CONTROL_SEND_CONFIRM_USER_DATA,asdu);
        while(true) {
            try {
                return linkLayerStateMachine();
            }
            catch(IEC870ConnectionException e) {
                if (retries++ > (iMaxRetries-1)) {
                    throw new IEC870ConnectionException("IEC870Connection, connectLink, max retries"+MAX_RETRIES_ERROR);
                }
                else {
                    if (e.getReason() == TIMEOUT_ERROR) {
                        reSendCurrentFrameTx();
                    }
                }
            }
        }
    }

    /*
     * The linkLayerStateMachine collects APDU's using polling for data class1/2. The state machine continues polling for data class1/2 (with a timeout of 10 sec.) until the first secondary frame
     * RESPOND[user data] is received.
     */

    private static final int POLL_TIMEOUT = 10000;

    private static final int STATE_WAIT_FOR_FRAME=0;
    private static final int STATE_WAIT_FOR_CLASS1=1;
    private static final int STATE_WAIT_FOR_CLASS2=2;

    private List linkLayerStateMachine() throws NestedIOException,IEC870ConnectionException {
        long lMSTimeout;
        List asdus = new ArrayList();
        int state = STATE_WAIT_FOR_FRAME;
        boolean booleanUserDataReceived=false;

        lMSTimeout = System.currentTimeMillis() + POLL_TIMEOUT;

        while(true) {
            currentFrameRx = waitForFrame();
            if (DEBUG >= 1) printFrame(currentFrameRx);
            switch(state) {
                case STATE_WAIT_FOR_FRAME: {
                    if (currentFrameRx.isSingleCharAck()) {
                        sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2);
                        state = STATE_WAIT_FOR_CLASS2;
                    }
                    else if (currentFrameRx.isFunction(IEC870Frame.CONTROL_CONFIRM_ACK)) {
                        if (currentFrameRx.isACD()) {
                            sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS1);
                            state = STATE_WAIT_FOR_CLASS1;
                        }
                        else {
                            sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2);
                            state = STATE_WAIT_FOR_CLASS2;
                        }
                    }
                    else throw new IEC870ConnectionException("IEC870Connection, linkLayerStateMachine, frame error, control=0x"+Integer.toHexString(currentFrameRx.getControl()));
                } break;

                case STATE_WAIT_FOR_CLASS1: {
                    if (currentFrameRx.isFunction(IEC870Frame.CONTROL_RESPOND_NACK)) {
                        sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2);
                        state = STATE_WAIT_FOR_CLASS2;
                    }
                    else if (currentFrameRx.isFunction(IEC870Frame.CONTROL_RESPOND_USER_DATA)) {
                        booleanUserDataReceived=true;
                        asdus.add(currentFrameRx.getASDU());
                        lMSTimeout = System.currentTimeMillis() + 10000;
                        sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2);
                        state = STATE_WAIT_FOR_CLASS2;
                    }
                    else  throw new IEC870ConnectionException("IEC870Connection, linkLayerStateMachine, frame error, control=0x"+Integer.toHexString(currentFrameRx.getControl()));
                } break;

                case STATE_WAIT_FOR_CLASS2: {
                    if (currentFrameRx.isFunction(IEC870Frame.CONTROL_RESPOND_NACK)) {
                        if (currentFrameRx.isACD()) {
                            sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS1);
                            state = STATE_WAIT_FOR_CLASS1;
                        }
                        else {

                            if (booleanUserDataReceived) return asdus; // end of story...
                            else if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                                System.out.println("timeout...");
                                return asdus; // end of story...
                            }
                            else {
                                sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2);
                                state = STATE_WAIT_FOR_CLASS2;
                            }

                        }
                    }
                    else if (currentFrameRx.isFunction(IEC870Frame.CONTROL_RESPOND_USER_DATA)) {
                        booleanUserDataReceived=true;
                        asdus.add(currentFrameRx.getASDU());
                        lMSTimeout = System.currentTimeMillis() + 10000;
                        sendFrame(IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2);
                        state = STATE_WAIT_FOR_CLASS2;
                    }
                    else  throw new IEC870ConnectionException("IEC870Connection, linkLayerStateMachine, frame error, control=0x"+Integer.toHexString(currentFrameRx.getControl()));

                } break;

            } // switch(state)
        } // while(true)
    } // private List linkLayerStateMachine() throws IEC870ConnectionException {

    private void waitFor(int function) throws IEC870ConnectionException {
        currentFrameRx = waitForFrame();
        if (DEBUG >= 1) printFrame(currentFrameRx);
        if (!currentFrameRx.isFunction(function))
            throw new IEC870ConnectionException("IEC870Connection, waitFor",FRAME_ERROR);
    }

    private static final int STATE_WAIT_FOR_START=0;
    private static final int STATE_WAIT_FOR_END=1;
    private static final int STATE_WAIT_FOR_LENGTH=2;
    private static final int STATE_WAIT_FOR_CHECKSUM=3;
    private static final int STATE_WAIT_FOR_DATA=4;

    /*
     * Wait until timeout for a frame.
     * @return byte array with framedata.
     * @throws IEC870ConnectionException
     */
    private IEC870Frame waitForFrame() throws IEC870ConnectionException {
        long lMSTimeoutInterFrame;
        int state=STATE_WAIT_FOR_START;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int kar,count=0,length=0,checksumreceived,checksumcalculated;
        bos.reset();

        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;

        copyEchoBuffer();

        try {
            while(true) {
                if ((kar = readIn()) != -1) {
                    bos.write(kar);
                    switch(state) {
                        case STATE_WAIT_FOR_START:

                            if (kar == IEC870Frame.FRAME_VARIABLE_LENGTH) {
                                state = STATE_WAIT_FOR_LENGTH;
                                count=0;
                            }
                            else if (kar == IEC870Frame.FRAME_FIXED_LENGTH) {
                                length = 3;
                                state = STATE_WAIT_FOR_DATA;
                            }
                            else if (kar == IEC870Frame.FRAME_SINGLE_CHAR_A2) {
                                return new IEC870Frame(bos.toByteArray());
                            }
                            else if (kar == IEC870Frame.FRAME_SINGLE_CHAR_E5) {
                                return new IEC870Frame(bos.toByteArray());
                            }
                            else {
                                bos.reset();
                            }
                            break; // STATE_WAIT_FOR_START

                        case STATE_WAIT_FOR_LENGTH:
                            if (count == 0) {
                                length = kar;
                                count++;
                            }
                            else if (count == 1) {
                                count++;
                            }
                            else if (count == 2) { // 2x length + 0x68
                                state = STATE_WAIT_FOR_DATA;
                                count=0;
                            }

                            break; // STATE_WAIT_FOR_LENGTH

                        case STATE_WAIT_FOR_DATA:
                            if (count++ >= (length-1)) {
                                state = STATE_WAIT_FOR_CHECKSUM;
                                count=0;
                            }

                            break; // STATE_WAIT_FOR_DATA

                        case STATE_WAIT_FOR_CHECKSUM:
                            byte data[] = bos.toByteArray();
                            checksumcalculated=0;
                            for (int i = 0; i< length;i++) {
                                checksumcalculated+=((int)data[((data.length-1)-length)+i]&0xFF);
                            }
                            checksumcalculated &= 0xFF; // modulo 256
                            checksumreceived = (int)data[data.length-1]&0xFF;
                            if (checksumcalculated == checksumreceived) {
                                state = STATE_WAIT_FOR_END;
                            }
                            else {
                                throw new IEC870ConnectionException("IEC870Connection, waitForFrame, bad crc",CRC_ERROR);
                            }
                            break; // STATE_WAIT_FOR_CHECKSUM

                        case STATE_WAIT_FOR_END:
                            if (kar == 0x16) {
                                return new IEC870Frame(bos.toByteArray());
                            }
                            else {
                                throw new IEC870ConnectionException("IEC870Connection, waitForFrame, end character invalid (0x"+Integer.toHexString(kar)+")",FRAMING_ERROR);
                            }
                    } // switch(state)

                } // if ((iNewKar = readIn()) != -1)
                if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                    throw new IEC870ConnectionException("IEC870Connection, waitForFrame, interframe timeout error",TIMEOUT_ERROR);
                }
            } // while(true)
        }
        catch(IOException e) {
            throw new IEC870ConnectionException("IEC870Connection, waitForFrame, "+e.getMessage());
        }

    } // public IEC870Frame waitForFrame() throws IEC870ConnectionException

    /*
     * For testing purposes. This method gets bytes from the inputstream until no bytes left
     * and returns a list with byte array frames.
     * @return List with byte array frames.
     * @throws IEC870ConnectionException
     */
    public List parseFrames() throws IEC870ConnectionException {
        List frames = new ArrayList();
        int state=STATE_WAIT_FOR_START;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream bosdata = new ByteArrayOutputStream();
        int kar,count=0,length=0,checksumreceived,checksumcalculated;
        bos.reset();
        try {
            while(inputStream.available()>0) {
                kar = inputStream.read();
                bos.write(kar);
                switch(state) {
                    case STATE_WAIT_FOR_START:

                        if (kar == IEC870Frame.FRAME_VARIABLE_LENGTH) {
                            state = STATE_WAIT_FOR_LENGTH;
                            count=0;
                        }
                        else if (kar == IEC870Frame.FRAME_FIXED_LENGTH) {
                            length = 3;
                            state = STATE_WAIT_FOR_DATA;
                        }
                        else if (kar == IEC870Frame.FRAME_SINGLE_CHAR_A2) {
                            frames.add(bos.toByteArray());
                            bos.reset();
                        }
                        else if (kar == IEC870Frame.FRAME_SINGLE_CHAR_E5) {
                            frames.add(bos.toByteArray());
                            bos.reset();
                        }
                        else {
                            bos.reset();
                        }
                        bosdata.reset();
                        break; // STATE_WAIT_FOR_START
                    case STATE_WAIT_FOR_LENGTH:
                        if (count == 0) {
                            length = kar;
                            count++;
                        }
                        else if (count == 1) {
                            count++;
                        }
                        else if (count == 2) { // 2x length + 0x68
                            state = STATE_WAIT_FOR_DATA;
                            count=0;
                        }

                        break; // STATE_WAIT_FOR_LENGTH
                    case STATE_WAIT_FOR_DATA:
                        bosdata.write(kar);
                        if (count++ >= (length-1)) {
                            state = STATE_WAIT_FOR_CHECKSUM;
                            count=0;
                        }

                        break; // STATE_WAIT_FOR_DATA

                    case STATE_WAIT_FOR_CHECKSUM:
                        checksumreceived = kar;
                        byte data[] = bosdata.toByteArray();
                        checksumcalculated=0;
                        for (int i = 0; i< length;i++) {
                            checksumcalculated+=((int)data[i]&0xFF);
                        }
                        checksumcalculated &= 0xFF; // modulo 256
                        if (checksumcalculated == checksumreceived) {
                            state = STATE_WAIT_FOR_END;
                        }
                        else {
                            System.out.println("Error, invalid checksum");
                            state = STATE_WAIT_FOR_START;
                        }
                        break; // STATE_WAIT_FOR_CHECKSUM

                    case STATE_WAIT_FOR_END:
                        if (kar == 0x16) {
                            frames.add(bos.toByteArray());
                            bos.reset();
                        }
                        else System.out.println("Error, end character not valid");

                        state = STATE_WAIT_FOR_START;

                        break; // STATE_WAIT_FOR_END
                } // switch(state)
            } // while(inputStream.available()>0)
        }
        catch(IOException e) {
            throw new IEC870ConnectionException("IEC870Connection, parseFrames, "+e.getMessage());
        }

        return frames;
    } // public List parseFrames()

} // public class IEC870Connection
