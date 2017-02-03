/*
 * TransmitterStateMachine.java
 *
 * Created on 30 november 2006, 11:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TransmitterStateMachine {
    
    final int DEBUG=0;
    final long TIMEOUT = 30000;
    
    MiniDLMSConnection connection;
    
    final int STATE_UI_WAIT=0;
    final int STATE_ONLINE_WAIT=1;
    final int STATE_OFFLINE=2;
    final int STATE_ONLINE=3;
    final int STATE_IWAIT=4;
    
    int state = STATE_OFFLINE;
    
    
    
    
    final int EVENT_ONLINE_REQUEST=0;
    final int EVENT_TIMEOUT_AND_RETRIES=1;
    final int EVENT_SEND_UI_REQUEST=2;
    final int EVENT_SEND_SABM_REQUEST=3;
    final int EVENT_SEND_I_REQUEST=4;
    final int EVENT_TIMEOUT_AND_RETRIES_ERROR=5;
    final int EVENT_RECEIVE_UA=6;
    final int EVENT_RECEIVE_REJ=7;
    final int EVENT_RECEIVE_RR=8;
    
    
    
    /** Creates a new instance of TransmitterStateMachine */
    public TransmitterStateMachine(MiniDLMSConnection connection) {
        this.connection=connection;
        
    }
    
    public void onlineRequest() throws IOException {
        stateMachine(EVENT_ONLINE_REQUEST);
        connection.getReceiverStateMachine().initAsClient();
    }
    
    public byte[] sendRequestI(byte[] data) throws IOException {
        if (DEBUG>=1) System.out.println("KV_DEBUG> TransmitterStateMachine, sendRequestI, stateMachine");        
        stateMachine(EVENT_SEND_I_REQUEST, data);
        
        // This step should only be performed if the P bit in the I frame is set. Means that the clien expects a response from the transmitter.
        // Anyhow, we do not send chunked frames where the P bit should be set false for all but the last chunk
        if (DEBUG>=1) System.out.println("KV_DEBUG> TransmitterStateMachine, sendRequestI, receiverstatemachine->receive");        
        return connection.getReceiverStateMachine().receive().getData();
    }
    
    public byte[] sendRequestUI(byte[] data) throws IOException {
        return stateMachine(EVENT_SEND_UI_REQUEST, data);
    }
    
    private byte[] stateMachine(int event) throws IOException {
        return stateMachine(event,null);
    }
    private byte[] stateMachine(int event, byte[] data) throws IOException {
        long protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        int retry=0;
        String error="";
        Frame frame=null;
        while(true) {
            try {
                switch(state) {
                    
                    case STATE_UI_WAIT: {
                        
                        switch(event) {
                            case EVENT_TIMEOUT_AND_RETRIES: {
                                connection.sendUI(data);
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                            } break; // EVENT_ONLINE_REQUEST
                            
                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_OFFLINE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR
                            
                            case EVENT_RECEIVE_UA: {
                                state = STATE_ONLINE;
                                return null;
                            } // EVENT_RECEIVE_UA
                            
                            default: {
                                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default
                            
                        } // switch(event)
                        
                    } break; // STATE_UI_WAIT
                    
                    case STATE_OFFLINE: {
                        
                        switch(event) {
                            
                            case EVENT_ONLINE_REQUEST:
                            case EVENT_SEND_SABM_REQUEST: {
                                connection.sendSABM();
                                state = STATE_ONLINE_WAIT;
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                                
                            } break; // EVENT_ONLINE_REQUEST
                            
                            default: {
                                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default
                            
                        } // switch(event)
                        
                    } break; // STATE_OFFLINE
                    
                    case STATE_ONLINE_WAIT: {
                        
                        switch(event) {
                            case EVENT_RECEIVE_UA: {
                                state = STATE_ONLINE;
                                connection.setSendSequence(0);
                                connection.setReceiveSequence(7);
                                return null;
                            } // EVENT_RECEIVE_UA
                            
                            case EVENT_TIMEOUT_AND_RETRIES: {
                                connection.sendSABM();
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                            } break; // EVENT_ONLINE_REQUEST
                            
                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_OFFLINE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR
                            
                            default: {
                                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default
                            
                            
                        } // switch(event)
                    } break; // STATE_ONLINE_WAIT
                    
                    
                    case STATE_ONLINE: {
                        switch(event) {
                            case EVENT_ONLINE_REQUEST:
                            case EVENT_SEND_SABM_REQUEST: {
                                connection.sendSABM();
                                state = STATE_ONLINE_WAIT;
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                            } break; // EVENT_ONLINE_REQUEST || EVENT_SEND_SABM_REQUEST
                            
                            case EVENT_SEND_UI_REQUEST: {
                                connection.sendUI(data);
                                state = STATE_UI_WAIT;
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                            } break; // EVENT_SEND_UI_REQUEST
                            
                            case EVENT_SEND_I_REQUEST: {
                                sendPackets(data);
                                state = STATE_IWAIT;
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                            } break; // EVENT_SEND_I_REQUEST
                            
                            default: {
                                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default
                        } // switch(event)
                        
                    } break; // STATE_ONLINE
                    
                    case STATE_IWAIT: {
                        switch(event) {
                            
                            case EVENT_RECEIVE_RR: {
                                if (connection.getSendSequence() == frame.getReceiveSequencePlus1()) {
                                    state = STATE_ONLINE;
                                    return frame.getData();
                                } else {
                                    state = STATE_OFFLINE;
                                    throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine(), sequence error LN(s)="+connection.getSendSequence()+", PN(r)="+frame.getReceivedSequence());
                                }
                            } // EVENT_RECEIVE_RR
                            
                            case EVENT_RECEIVE_REJ: {
                                
                                if (frame.getReceivedSequence() > connection.getSendSequence())
                                    sendPackets(data,7-(frame.getReceivedSequence()-connection.getSendSequence()));
                                else
                                    sendPackets(data,connection.getSendSequence()-frame.getReceivedSequence());
                                
                                connection.setSendSequence(frame.getReceivedSequence());
                                
                                
                                sendPackets(data,frame.getReceivedSequence());
                                
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                                
                            } break; // EVENT_RECEIVE_REJ
                            
                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_OFFLINE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR
                            
                            case EVENT_TIMEOUT_AND_RETRIES: {
                                sendPackets(data, true);
                                frame = connection.receivePacketStateMachine();
                                event = getEvent(frame);
                                
                            } break; // EVENT_TIMEOUT_AND_RETRIES
                            
                            default: {
                                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default
                            
                        } // switch(event)
                        
                    } break; // STATE_IWAIT
                    
                } // switch(state)
                
            } catch(ProtocolConnectionException e) {
                if (DEBUG>=1) e.printStackTrace();
                if (e.getReason() == connection.getPROTOCOL_ERROR()) {
                    state = STATE_OFFLINE;
                    //throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine() error, "+e.getMessage());
                    error = "TransmitterStateMachine, stateMachine() error, "+e.getMessage();
                    event = EVENT_TIMEOUT_AND_RETRIES_ERROR;
                } else {
                    if (retry++>=connection.getMaxRetries()) {
                        //throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine() error maxRetries ("+connection.getMaxRetries()+"), "+e.getMessage());
                        error = "TransmitterStateMachine, stateMachine() error maxRetries ("+connection.getMaxRetries()+"), "+e.getMessage();
                        event = EVENT_TIMEOUT_AND_RETRIES_ERROR;
                    } else {
                        event = EVENT_TIMEOUT_AND_RETRIES;
                    }
                }
            } // catch(ProtocolConnectionException e)
            catch(ConnectionException e) {
                throw e;
            } // catch(ConnectionException e)
            
            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine() protocol timeout error",connection.getTIMEOUT_ERROR());
            } // if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0)
            
        } // while(true)
        
    } // private void stateMachine()

    private int requiredPackets(byte[] data) {
        int nrOfPackets = data.length / 60;
        nrOfPackets +=  ((data.length%60)==0?0:1);
        return nrOfPackets;
    }
    
    private void sendPackets(byte[] data) throws IOException {
        sendPackets(data, 0, false);
    }
    private void sendPackets(byte[] data, boolean retry) throws IOException {
        sendPackets(data, 0, retry);
    }
    
    private void sendPackets(byte[] data, int packets2resend) throws IOException {
        sendPackets(data, packets2resend, false);
    }
    
    int keepSendSequence;
    private void sendPackets(byte[] data, int packets2resend, boolean retry) throws IOException {
        
        int nrOfPackets = requiredPackets(data);
        int offset=0;
        int length=data.length;
        int from = nrOfPackets - packets2resend;
        
        if (retry)
            connection.setSendSequence(keepSendSequence);
        else
            keepSendSequence = connection.getSendSequence();
        
        // shift the offset
        for (int i=0; i<(from-1);i++) {
            offset+=60;
            length-=60;
        }
        
        
        for (int i=from; i<(nrOfPackets-1);i++) {
            byte[] chunk = ProtocolUtils.getSubArray2(data, offset, 60);
            offset+=60;
            length-=60;
            connection.send(MiniDLMSConnection.getPACKET_TYPE_I(), chunk);
            connection.incSendSequence();
        } // for (int i=0; i<nrOfPackets;i++)
        
        
        byte[] chunk = ProtocolUtils.getSubArray2(data, offset, length>60?60:length);
        connection.send(MiniDLMSConnection.getPACKET_TYPE_IP(), chunk);
        connection.incSendSequence();
        
    }
    
    private int getEvent(Frame frame) throws IOException {
        
        if (frame.isPacketRR())
            return EVENT_RECEIVE_RR;
        else if (frame.isPacketREJ())
            return EVENT_RECEIVE_REJ;
        else if (frame.isPacketUA())
            return EVENT_RECEIVE_UA;
        else throw new ProtocolConnectionException("TransmitterStateMachine, getEvent(), invalid packet: "+frame);
    }
    
    
} // public class TransmitterStateMachine
