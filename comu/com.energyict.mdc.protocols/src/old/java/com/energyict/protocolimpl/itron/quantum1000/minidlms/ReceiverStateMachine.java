/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ReceiverStateMachine.java
 *
 * Created on 30 november 2006, 17:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ReceiverStateMachine {

    final int DEBUG=0;
    final long TIMEOUT = 30000;

    MiniDLMSConnection connection;

    final int STATE_OFFLINE=0;
    final int STATE_ONLINE=1;
    final int STATE_RECEIVING_MESSAGE=2;

    int state;




    /** Creates a new instance of ReceiverStateMachine */
    public ReceiverStateMachine(MiniDLMSConnection connection) {
        this.connection=connection;

    }

    public void initAsServer() throws IOException {
        state = STATE_OFFLINE;
        stateMachine();
    }

    public void initAsClient() throws IOException {
        state = STATE_ONLINE;
    }

    public Frame receive() throws IOException {
        return stateMachine();
    }


    private Frame stateMachine() throws IOException {

        long protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.reset();

        while(true) {
            switch(state) {
                case STATE_OFFLINE: {

                    Frame frame = connection.receivePacketStateMachine();
                    if (frame.isPacketSABM()) {
                        state = STATE_ONLINE;
                        connection.sendUA();
                        connection.setSendSequence(0);
                        connection.setReceiveSequence(7);
                        message.reset();
                    }
                } break; // STATE_OFFLINE

                case STATE_ONLINE: {
                    Frame frame = connection.receivePacketStateMachine();
                    if (frame.isPacketI()) {
                        if ((connection.getReceiveSequence() == frame.getSendSequence()) && frame.isLastFrame()) {
                            connection.sendRR();
                            return null;
                        } else if ((connection.getReceiveSequencePlus1() == frame.getSendSequence()) && frame.isLastFrame()) {
                            connection.incReceiveSequence();
                            connection.sendRR();
                            message.write(frame.getData());
                            frame.setData(message.toByteArray());
if (DEBUG>=1) System.out.println("STATE_ONLINE last frame received datasize = "+frame+", "+frame.getData().length+", "+ProtocolUtils.outputHexString(frame.getData()));
                            return frame;
                        } else if ((connection.getReceiveSequencePlus1() == frame.getSendSequence()) && !frame.isLastFrame()) {

                            connection.incReceiveSequence();
                            message.write(frame.getData());
if (DEBUG>=1) System.out.println("STATE_ONLINE chunk frame received datasize = "+frame+", "+frame.getData().length+", "+ProtocolUtils.outputHexString(frame.getData()));
                            state = STATE_RECEIVING_MESSAGE;
                        } else if (connection.getReceiveSequencePlus1() != frame.getSendSequence()) {
                            connection.sendREJ();
                        }
                    } else if (frame.isPacketSABM()) {
                        connection.sendUA();
                        connection.setSendSequence(0);
                        connection.setReceiveSequence(7);
                        message.reset();
                        return null;
                    } else if (frame.isPacketUI()) {
                        connection.sendUA();
                        frame.setData(message.toByteArray());
                        return frame;
                    }

                } break; // STATE_ONLINE

                case STATE_RECEIVING_MESSAGE: {
                    Frame frame = connection.receivePacketStateMachine();
                    if (frame.isPacketI()) {
                        if ((connection.getReceiveSequencePlus1() == frame.getSendSequence()) && frame.isLastFrame()) {
                            connection.incReceiveSequence();
                            connection.sendRR();
                            message.write(frame.getData());
                            state = STATE_ONLINE;
if (DEBUG>=1) System.out.println("STATE_RECEIVING_MESSAGE last frame received datasize = "+frame+", "+frame.getData().length+", "+ProtocolUtils.outputHexString(frame.getData()));
                            frame.setData(message.toByteArray());
                            return frame;
                        } else if ((connection.getReceiveSequencePlus1() == frame.getSendSequence()) && !frame.isLastFrame()) {
                            connection.incReceiveSequence();
                            message.write(frame.getData());
if (DEBUG>=1) System.out.println("STATE_RECEIVING_MESSAGE chunk frame received datasize = "+frame+", "+frame.getData().length+", "+ProtocolUtils.outputHexString(frame.getData()));
                        } else if (connection.getReceiveSequencePlus1() != frame.getSendSequence()) {
                            connection.sendREJ();

                        }
                    } else if (frame.isPacketSABM()) {
                        state = STATE_ONLINE;
                        connection.sendUA();
                        connection.setSendSequence(0);
                        connection.setReceiveSequence(7);
                        message.reset();
                        return null;
                    } else if (frame.isPacketUI()) {
                        connection.sendUA();
                        frame.setData(message.toByteArray());
                        return frame;
                    }

                } break; // STATE_RECEIVING_MESSAGE

            } // switch(state)

            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("ReceiverStateMachine, stateMachine() protocol timeout error",connection.getTIMEOUT_ERROR());
            }

        } // while(true)

    } // private byte[] stateMachine(int event) throws IOException

}
