/*
 * MarkVConnection.java
 *
 * Created on 8 augustus 2005, 10:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.connection;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.Connection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.meteridentification.MeterTypeImpl;
import com.energyict.protocolimpl.transdata.markv.core.commands.CommandDescription;
import com.energyict.protocolimpl.transdata.markv.core.commands.CommandIdentification;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 *
 * @author koen
 */
public class MarkVConnection extends Connection  implements ProtocolConnection {

    private static final int DEBUG=0;
    private static final long TIMEOUT=600000;

    int timeout;
    int maxRetries;
    boolean loggedOn=false;
    SerialCommunicationChannel commChannel=null;
    int dtrBehaviour=2;

    /** Creates a new instance of MarkVConnection */
    public MarkVConnection(InputStream inputStream,
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

    public void setSerialCommunicationChannel(SerialCommunicationChannel commChannel) {
        this.commChannel=commChannel;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {

    }
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    public void disconnectMAC() {
    }

    public void setDtrBehaviour(int dtrBehaviour) {
        this.dtrBehaviour=dtrBehaviour;
    }

    public MeterType connectMAC(String strID,String password,int securityLevel,String nodeId) throws IOException {
        String id=null;

        if (commChannel != null) {
            commChannel.setBaudrate(9600);

            if (dtrBehaviour == 0) {
                commChannel.setDTR(false);
            } else if (dtrBehaviour == 1) {
                commChannel.setDTR(true);
            }

            commChannel.setRTS(false);
            id = tryAuthentication(password,securityLevel);
        }
        else {
            if ((nodeId!= null) && ("".compareTo(nodeId) != 0)) {
                unlockMeter(nodeId);
            }
            id = tryAuthentication(password,securityLevel);
        }

        if (securityLevel>0) {
            return new MeterTypeImpl(id);
        } else {
            return null;
        }
    }

    private void unlockMeter(String nodeId) throws IOException {
        long protocolTimeout;
        long interFrameTimeout;
        int retries=0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String id=null;
        int kar;
        // ***************************************************************************************************
        // send address
        retries=0;
        copyEchoBuffer();
        while(retries++ < getMaxRetries()) {
            interFrameTimeout = System.currentTimeMillis() + timeout;
            sendRawDataNoDelayTerminalMode(("HL\r\n"+nodeId+"\r\n").getBytes(), false);
            //sendOut(("HL\n\r"+nodeId).getBytes());
            while(true) {
                if ((kar = readIn()) != -1) {
                    // characters received
                    if (kar == '\n') {
                        return;
                    }
                }
                if (System.currentTimeMillis() - interFrameTimeout > 0) {
                    break;
                }
            } // while(true)
        } // while(retries++ < getMaxRetries())
        throw new ProtocolConnectionException("unlockMeter() nodeId acceptance max retries error!",MAX_RETRIES_ERROR);
    }

    private String tryAuthentication(String password,int securityLevel) throws IOException {
        String id = null;
        if (securityLevel>0) {
           id = authenticate(password);
           loggedOn=true;
        }
        return id;
    }

    public byte[] dataReadout(String strID,String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void sendCommand(CommandIdentification ci) throws IOException {
        sendCommand(ci, false);
    }

    public ResponseFrame sendCommandAndReceive(CommandIdentification ci) throws IOException {
        return sendCommand(ci, true);
    }

    private byte[] assembleAndSendFrame(String command) throws ConnectionException, NestedIOException {
        waitForEmptyBuffer(500);
        byte[] frame = (command + "\r\n").getBytes();
        sendRawDataNoDelayTerminalMode(frame,true);
        return frame;
    }

    public byte[] receiveWithTimeout(String command) throws IOException {

        long protocolTimeout;
        long interFrameTimeout;
        int retries=0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String id=null;
        int kar;

        // ***************************************************************************************************
        // request identification
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        copyEchoBuffer();
        while(retries++ < getMaxRetries()) {
            sendRawDataNoDelayTerminalMode((command+"\r\n").getBytes(),false);
            interFrameTimeout = System.currentTimeMillis() + 1000;
            while(true) {
                if ((kar = readIn()) != -1) {
                    // characters received
                    baos.write(kar);
                    interFrameTimeout = System.currentTimeMillis() + 500;
                }


                if (System.currentTimeMillis() - interFrameTimeout > 0) {
                    if (baos.toByteArray().length == 0) {
                        break;
                    }
                    else {
                        retries=getMaxRetries();
                        break;
                    }
                }

                if (System.currentTimeMillis() - protocolTimeout > 0) {
                    throw new ProtocolConnectionException("authenticate() response to 'II' timeout error!",TIMEOUT_ERROR);
                }
            } // while(true)
        } // while(retries++ < getMaxRetries())

        return baos.toByteArray();
    }

    private String authenticate(String password) throws IOException {

        long protocolTimeout;
        long interFrameTimeout;
        int retries=0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String id=null;
        int kar;


        // ***************************************************************************************************
        // request identification
        baos.write(receiveWithTimeout("II"));

        if (baos.toByteArray().length == 0) {
            throw new ProtocolConnectionException("authenticate() response to 'II' max retries error!", MAX_RETRIES_ERROR);
        }

        // ***************************************************************************************************
        // send password

        retries=0;
        copyEchoBuffer();
        while(retries++ < getMaxRetries()) {
            interFrameTimeout = System.currentTimeMillis() + timeout;
            sendRawDataNoDelayTerminalMode((password+"\r\n").getBytes(), false);
            while(true) {
                if ((kar = readIn()) != -1) {
                    // characters received
                    if (kar == '?') {
                        return baos.toString();
                    }
                }
                if (System.currentTimeMillis() - interFrameTimeout > 0) {
                    break;
                }
            } // while(true)
        } // while(retries++ < getMaxRetries())
        throw new ProtocolConnectionException("authenticate() password acceptance max retries error!",MAX_RETRIES_ERROR);
    } // private String authenticate(String password) throws IOException

    private ResponseFrame sendCommand(CommandIdentification ci, boolean response) throws IOException {
        int retry=0;
        while(true) {
            ResponseFrame rf=null;
            try {
                byte[] frame = assembleAndSendFrame(ci.getCommand());
                if (response) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    if (ci.getArguments() != null) {
                        // receive without prompt for the command
                        baos.write(receiveFrame(false,frame));
                        // check for prompt...
                        rf = new ResponseFrame(baos.toString());
                        if (rf.isRetry()||rf.isFailed()||rf.isDisabled()||rf.isDenied()) {
                            throw new ProtocolConnectionException("sendCommand(), send argument, prompt " + rf.getPrompt() + " received for command " + CommandDescription.getDescriptionFor(ci.getCommand()) + "!");
                        }
                        // send all arguments and wait for CR
                        for (int argument=0;(argument<ci.getArguments().length-1);argument++) {
                           frame = assembleAndSendFrame(ci.getArguments()[argument]);
                           baos.write(receiveFrame(false,frame));
                           // check for prompt...
                           rf = new ResponseFrame(baos.toString());
                           if (rf.isRetry()||rf.isFailed()||rf.isDisabled()||rf.isDenied()) {
                               throw new ProtocolConnectionException("sendCommand(), send argument, prompt " + rf.getPrompt() + " received for command " + CommandDescription.getDescriptionFor(ci.getCommand()) + "!");
                           }
                           }
                        frame = assembleAndSendFrame(ci.getArguments()[ci.getArguments().length-1]);
                    } // if (ci.getArguments() != null)

                    if (ci.isUseProtocol()) {

                        // wait for the 'start the protocol prompt
                        // KV_TO_DO we should implement a real match for that prompt with a timeout...
                        waitForEmptyBuffer(3000);
                        // reset the data collect stream
                        baos.reset();
                        // start xmodem transfer
                        byte[] data = getXmodemProtocolData();
                        if ((data == null) || (data.length==0)) {
                            waitForEmptyBuffer(3000);
                            throw new ProtocolConnectionException("sendCommand(), xmodem data length==0 error!");
                        }
//                        else {
//ProtocolUtils.printResponseData(data);
//                        }

                        baos.write(data);
                        frame=null;
                    }
                    if (!ci.isLogOff()) {
                       // receive with prompt
                       baos.write(receiveFrame(true,frame));
                    }
                    rf = new ResponseFrame(baos.toString());


                    if (loggedOn) {
                        if (rf.isOK()) {
                            return rf;
                        }
                        else {
                            throw new ProtocolConnectionException("sendCommand(), failed prompt "+rf.getPrompt()+" received for command "+CommandDescription.getDescriptionFor(ci.getCommand())+"!");
                        }
                    }
                    else {
                        return rf;
                    }
                } else {
                    return null;
                }
            } catch(ConnectionException e) {
                if ((rf!= null) && (rf.isDenied()||(rf.isDisabled()))) {
                    throw new ProtocolConnectionException("sendCommand(), failed prompt "+rf.getPrompt()+" received for command "+CommandDescription.getDescriptionFor(ci.getCommand())+"!");
                }
                if (retry++>=getMaxRetries()) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+getMaxRetries()+"), "+e.getMessage(), MAX_RETRIES_ERROR);
                }
            }
        } // while(true)
    } // sendCommand(...)



    private byte[] receiveFrame(boolean prompt, byte[] frame) throws ConnectionException, NestedIOException {
        int kar;
        long protocolTimeout,interFrameTimeout;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

        protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        interFrameTimeout = System.currentTimeMillis() + timeout;

        int frameKarPosition=0;
        int frameLength = frame==null?-1:frame.length;

        copyEchoBuffer();
        while(true) {
            if ((kar = readIn()) != -1) {
//                if (frameKarPosition<frameLength) {

                   if (DEBUG == 1) {
                       System.out.print(",0x");
                       ProtocolUtils.outputHex(kar);
                       System.out.print("=0x");
                       ProtocolUtils.outputHex(((int)frame[frameKarPosition]&0xFF));
                   }

//                   if (((int)frame[frameKarPosition++]&0xFF) != kar)
//                       throw new ProtocolConnectionException("receiveFrame() response command echo error",PROTOCOL_ERROR);
//                }
//                else {
                    allDataArrayOutputStream.write(kar);
                    if (loggedOn) {
                        if (prompt) {
                            if (kar == '?') { // password send , last karacter send is '?'
                                return allDataArrayOutputStream.toByteArray();
                            }
                        }
                        else {
                            if (kar == '\n') { // no prompt, last character must be a \n
                                return allDataArrayOutputStream.toByteArray();
                            }
                        }
                    } else {
                        if (kar == '\n') { // no password send yet, last character must be a \n
                            return allDataArrayOutputStream.toByteArray();
                        }
                    }
//                }
            } // if ((kar = readIn()) != -1)

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // private byte[] receive() throws IOException

    public int getMaxRetries() {
        return maxRetries;
    }

} // public class MarkVConnection extends Connection  implements ProtocolConnection
