/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ApplicationStateMachine.java
 *
 * Created on 4 december 2006, 16:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ApplicationStateMachine {

    final long DEBUG = 0;

    final long TIMEOUT = 30000;
    final int MAX_RETRIES = 5;


    private final int STATE_BASE_STATE=0;
    private final int STATE_IDENTIFY_WAIT=1;
    private final int STATE_ID=2;
    private final int STATE_INITIATE_WAIT=3;
    private final int STATE_READ_WAIT=4;
    private final int STATE_SESSION=5;
    private final int STATE_WRITE_WAIT=6;
    private final int STATE_INITIATE_UPLOAD_WAIT=7;
    private final int STATE_TERMINATE_UPLOAD_WAIT=8;
    private final int STATE_UPLOAD=9;
    private final int STATE_UPLOAD_SEGMENT_WAIT=10;

    private int state = STATE_BASE_STATE;

    private final int EVENT_IDENTIFY=0;
    private final int EVENT_TIMEOUT_AND_RETRIES=1;
    private final int EVENT_TIMEOUT_AND_RETRIES_ERROR=2;
    private final int EVENT_IDENTIFY_RESPONSE=3;
    private final int EVENT_INITIATE=4;
    private final int EVENT_INITIATE_RESPONSE=5;
    private final int EVENT_ABORT=6;
    private final int EVENT_EXCEPTION=7;
    private final int EVENT_SEND_READ=8;
    private final int EVENT_SEND_WRITE=9;
    private final int EVENT_RECEIVE_READ_RESPONSE=10;
    private final int EVENT_RECEIVE_WRITE_RESPONSE=11;
    private final int EVENT_SEND_INITIATE_UPLOAD=12;
    private final int EVENT_RECEIVE_INITIATE_UPLOAD_RESPONSE=13;
    private final int EVENT_SEND_TERMINATE_UPLOAD=14;
    private final int EVENT_RECEIVE_TERMINATE_UPLOAD_RESPONSE=15;
    private final int EVENT_SEND_UPLOAD_SEGMENT=16;
    private final int EVENT_RECEIVE_UPLOAD_SEGMENT_RESPONSE=17;

    private ProtocolLink protocolLink;
    private CommandFactory commandFactory=null;
    private AbstractCommand abstractCommand;

    /** Creates a new instance of ApplicationStateMachine */
    public ApplicationStateMachine(ProtocolLink protocolLink) {
        this.protocolLink=protocolLink;
        setCommandFactory(new CommandFactory(protocolLink));
    }


    public void abort() throws IOException {
        stateMachine(EVENT_ABORT);
    }

    public String identify() throws IOException {
        return (String)stateMachine(EVENT_IDENTIFY);
    }

    public InitiateResponse initiate() throws IOException {
        return (InitiateResponse)stateMachine(EVENT_INITIATE);
    }

    public ReadReply read(int variableName) throws IOException {
        abstractCommand = getCommandFactory().read(variableName);
        return (ReadReply)stateMachine(EVENT_SEND_READ);
    }

    public ReadReply read(int variableName,int index) throws IOException {
        abstractCommand = getCommandFactory().readIndexed(variableName,index, 0x04);
        return (ReadReply)stateMachine(EVENT_SEND_READ);
    }

    public ReadReply read(int variableName,int index, int range) throws IOException {
        abstractCommand = getCommandFactory().readIndexedWithRange(variableName, index, range, 0x05);
        return (ReadReply)stateMachine(EVENT_SEND_READ);
    }

    public WriteReply write(int variableName,byte[] data) throws IOException {
        if (DEBUG>=1) System.out.println("KV_DEBUG> ApplicationStateMachine, write, getCommandFactory().write");
        abstractCommand = getCommandFactory().write(variableName, data);
        if (DEBUG>=1) System.out.println("KV_DEBUG> ApplicationStateMachine, write, stateMachine");
        return (WriteReply)stateMachine(EVENT_SEND_WRITE);
    }

    public WriteReply write(int variableName, int index, byte[] data) throws IOException {
        abstractCommand = getCommandFactory().writeIndexed(variableName, index, data);
        return (WriteReply)stateMachine(EVENT_SEND_WRITE);
    }

    public WriteReply write(int variableName, int index, int range, byte[] data) throws IOException {
        abstractCommand = getCommandFactory().writeIndexedWithRange(variableName, index, range, data);
        return (WriteReply)stateMachine(EVENT_SEND_WRITE);
    }

    public InitiateUploadResponse initiateUpload(int dataSetId)  throws IOException {
       abstractCommand = getCommandFactory().initiateUpload(dataSetId);
       return (InitiateUploadResponse)stateMachine(EVENT_SEND_INITIATE_UPLOAD);
    }

    public UploadSegmentResponse uploadSegment(int segmentNumber)  throws IOException {
       abstractCommand = getCommandFactory().uploadSegment(segmentNumber);
       return (UploadSegmentResponse)stateMachine(EVENT_SEND_UPLOAD_SEGMENT);
    }

    public TerminateLoadResponse terminateLoad()  throws IOException {
       abstractCommand = getCommandFactory().terminateLoad();
       return (TerminateLoadResponse)stateMachine(EVENT_SEND_TERMINATE_UPLOAD);
    }


    private Object stateMachine(int event) throws IOException {
        long protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        int retry=0;
        String error="";
        Frame frame=null;
        Object returnObject=null;

        while(true) {
            try {
                switch(state) {

                    case STATE_BASE_STATE: {

                        switch(event) {

                            case EVENT_IDENTIFY: {



                                protocolLink.getMiniDLMSConnection().identify();
                                state = STATE_IDENTIFY_WAIT;
                                returnObject = protocolLink.getMiniDLMSConnection().receiveIdentifyResponse();
                                event = EVENT_IDENTIFY_RESPONSE;

                            } break; // EVENT_IDENTIFY

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default


                        } // switch(event)

                    } break; //STATE_BASE_STATE

                    case STATE_IDENTIFY_WAIT: {

                        switch(event) {

                            case EVENT_IDENTIFY_RESPONSE: {
                                state = STATE_ID;

                                // SABM
                                protocolLink.getMiniDLMSConnection().getTransmitterStateMachine().onlineRequest();

                                return returnObject;
                            } // EVENT_IDENTIFY_RESPONSE

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                protocolLink.getMiniDLMSConnection().identify();
                                returnObject = protocolLink.getMiniDLMSConnection().receiveIdentifyResponse();
                                event = EVENT_IDENTIFY_RESPONSE;
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)
                    } break; //STATE_IDENTIFY_WAIT

                    case STATE_ID: {
                        switch(event) {

                            case EVENT_INITIATE: {
                                state = STATE_INITIATE_WAIT;
                                returnObject = getCommandFactory().initiate();
                                event = EVENT_INITIATE_RESPONSE;
                            } break; // EVENT_INITIATE



                            case EVENT_ABORT: {
                                getCommandFactory().abort();
                                state = STATE_BASE_STATE;
                                return null;
                            } // EVENT_ABORT


                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)

                    } break; //STATE_ID

                    case STATE_INITIATE_WAIT: {
                        switch(event) {

                            case EVENT_INITIATE_RESPONSE: {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> "+returnObject);
                                state = STATE_SESSION;
                                return returnObject;
                            } // EVENT_INITIATE_RESPONSE

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                returnObject = getCommandFactory().initiate();
                                event = EVENT_INITIATE_RESPONSE;
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            case EVENT_EXCEPTION: {
                                state = STATE_ID;
                                throw new IOException(error);
                            } // EVENT_EXCEPTION

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)

                    } break; //STATE_INITIATE_WAIT

                    case STATE_SESSION: {

                        switch(event) {
                            case EVENT_ABORT: {
                                getCommandFactory().abort();
                                state = STATE_BASE_STATE;
                                return null;

                            } // EVENT_ABORT

                            case EVENT_INITIATE: {
                                state = STATE_INITIATE_WAIT;
                                returnObject = getCommandFactory().initiate();
                                event = EVENT_INITIATE_RESPONSE;
                            } break; // EVENT_INITIATE

                            case EVENT_SEND_READ: {
                                state = STATE_READ_WAIT;
                                abstractCommand.invoke();
                                event = EVENT_RECEIVE_READ_RESPONSE;
                                returnObject = abstractCommand.getResponse();
                            } break; // EVENT_SEND_READ

                            case EVENT_SEND_WRITE: {
                                state = STATE_WRITE_WAIT;
                                abstractCommand.invoke();
                                event = EVENT_RECEIVE_WRITE_RESPONSE;
                                returnObject = abstractCommand.getResponse();
                            } break; // EVENT_SEND_WRITE

                            case EVENT_SEND_INITIATE_UPLOAD: {
                                state = STATE_INITIATE_UPLOAD_WAIT;
                                abstractCommand.invoke();
                                event = EVENT_RECEIVE_INITIATE_UPLOAD_RESPONSE;
                                returnObject = abstractCommand.getResponse();
                            } break; // EVENT_SEND_INITIATE_UPLOAD

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default
                        } // switch(event)


                    } break; //STATE_SESSION

                    case STATE_READ_WAIT: {

                        switch(event) {

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                                event = EVENT_RECEIVE_READ_RESPONSE;
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            case EVENT_EXCEPTION: {
                                state = STATE_SESSION;
                                throw new IOException(error);
                            } // EVENT_EXCEPTION

                            case EVENT_RECEIVE_READ_RESPONSE: {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> "+returnObject);
                                state = STATE_SESSION;
                                return returnObject;
                            } // EVENT_RECEIVE_READ_RESPONSE

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)

                    } break; //STATE_READ_WAIT

                    case STATE_WRITE_WAIT: {

                        switch(event) {

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                                event = EVENT_RECEIVE_WRITE_RESPONSE;
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            case EVENT_EXCEPTION: {
                                state = STATE_SESSION;
                                throw new IOException(error);
                            }  // EVENT_EXCEPTION

                            case EVENT_RECEIVE_WRITE_RESPONSE: {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> "+returnObject);
                                state = STATE_SESSION;
                                return returnObject;
                            } // EVENT_RECEIVE_WRITE_RESPONSE

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)

                    } break; //STATE_WRITE_WAIT

                    case STATE_INITIATE_UPLOAD_WAIT: {

                        switch(event) {

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                                event = EVENT_RECEIVE_INITIATE_UPLOAD_RESPONSE;
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            case EVENT_EXCEPTION: {
                                state = STATE_SESSION;
                                throw new IOException(error);
                            } // EVENT_EXCEPTION

                            case EVENT_RECEIVE_INITIATE_UPLOAD_RESPONSE: {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> "+returnObject);
                                state = STATE_UPLOAD;
                                return returnObject;
                            } // EVENT_RECEIVE_INITIATE_UPLOAD_RESPONSE

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)
                    } break; //STATE_INITIATE_UPLOAD_WAIT

                    case STATE_TERMINATE_UPLOAD_WAIT: {

                        switch(event) {

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                                event = EVENT_RECEIVE_TERMINATE_UPLOAD_RESPONSE;
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            case EVENT_EXCEPTION: {
                                state = STATE_SESSION;
                                throw new IOException(error);
                            } // EVENT_EXCEPTION

                            case EVENT_RECEIVE_TERMINATE_UPLOAD_RESPONSE: {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> "+returnObject);
                                state = STATE_SESSION;
                                return returnObject;
                            } // EVENT_RECEIVE_TERMINATE_UPLOAD_RESPONSE

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)
                    } break; //STATE_TERMINATE_UPLOAD_WAIT

                    case STATE_UPLOAD: {

                        switch(event) {

                            case EVENT_ABORT: {
                                getCommandFactory().abort();
                                state = STATE_BASE_STATE;
                                return null;
                            } // EVENT_ABORT

                            case EVENT_SEND_TERMINATE_UPLOAD: {
                                state = STATE_TERMINATE_UPLOAD_WAIT;
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                                event = EVENT_RECEIVE_TERMINATE_UPLOAD_RESPONSE;
                            } break; // EVENT_SEND_TERMINATE_UPLOAD

                            case EVENT_SEND_UPLOAD_SEGMENT: {
                                state = STATE_UPLOAD_SEGMENT_WAIT;
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                                event = EVENT_RECEIVE_UPLOAD_SEGMENT_RESPONSE;
                            } break; // EVENT_SEND_UPLOAD_SEGMENT

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)

                    } break; //STATE_UPLOAD

                    case STATE_UPLOAD_SEGMENT_WAIT: {

                        switch(event) {

                            case EVENT_RECEIVE_UPLOAD_SEGMENT_RESPONSE: {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> "+returnObject);
                                state = STATE_UPLOAD;
                                return returnObject;
                            } // EVENT_RECEIVE_UPLOAD_SEGMENT_RESPONSE

                            case EVENT_EXCEPTION: {
                                state = STATE_UPLOAD;
                                throw new IOException(error);
                            } // EVENT_EXCEPTION

                            case EVENT_TIMEOUT_AND_RETRIES: {
                                abstractCommand.invoke();
                                returnObject = abstractCommand.getResponse();
                            } break; // EVENT_TIMEOUT_AND_RETRIES

                            case EVENT_TIMEOUT_AND_RETRIES_ERROR: {
                                state = STATE_BASE_STATE;
                                throw new ProtocolConnectionException(error);
                            } // EVENT_TIMEOUT_AND_RETRIES_ERROR

                            default: {
                                throw new ProtocolConnectionException("ApplicationStateMachine, stateMachine(), error state = "+state+", event = "+event);
                            } // default

                        } // switch(event)

                    } break; //STATE_UPLOAD_SEGMENT_WAIT

                    default:
                        throw new IOException("ApplicationStateMachine, stateMachine(), state="+state+", event="+event);
                }

            }
            catch(ProtocolConnectionException e) {
                if (DEBUG>=1) e.printStackTrace();
                if (e.getReason() == protocolLink.getMiniDLMSConnection().getPROTOCOL_ERROR()) {
                    state = STATE_BASE_STATE;
                    //throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine() error, "+e.getMessage());
                    error = "TransmitterStateMachine, stateMachine() error, "+e.getMessage();
                    event = EVENT_TIMEOUT_AND_RETRIES_ERROR;
                } else {
                    if (retry++>=MAX_RETRIES) {
                        //throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine() error maxRetries ("+connection.getMaxRetries()+"), "+e.getMessage());
                        error = "TransmitterStateMachine, stateMachine() error maxRetries ("+MAX_RETRIES+"), "+e.getMessage();
                        event = EVENT_TIMEOUT_AND_RETRIES_ERROR;
                    } else {
                        event = EVENT_TIMEOUT_AND_RETRIES;
                    }
                }
            } // catch(ProtocolConnectionException e)
            catch(ConnectionException e) {
               throw e;
            } // catch(ConnectionException e)
            catch(ReplyException e) {

                switch (state) {
                    case STATE_BASE_STATE:
                    case STATE_IDENTIFY_WAIT:
                    case STATE_ID:
                        break;
                    case STATE_INITIATE_WAIT:
                        state = STATE_ID;
                        break;
                    case STATE_SESSION:
                        break;
                    case STATE_READ_WAIT:
                    case STATE_WRITE_WAIT:
                    case STATE_INITIATE_UPLOAD_WAIT:
                    case STATE_TERMINATE_UPLOAD_WAIT:
                        state = STATE_SESSION;
                        break;
                    case STATE_UPLOAD:
                        break;
                    case STATE_UPLOAD_SEGMENT_WAIT:
                        state = STATE_UPLOAD;
                        break;
                } // switch (state)

                throw e;
            } // catch(ReplyException e)
            catch(IOException e) {
                // all other exceptions
                error = e.toString();
                event = EVENT_TIMEOUT_AND_RETRIES_ERROR;
            }

            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("TransmitterStateMachine, stateMachine() protocol timeout error",protocolLink.getMiniDLMSConnection().getTIMEOUT_ERROR());
            } // if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0)

        } // while(true)

    } // private void stateMachine(int event) throws IOException

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

} // public class ApplicationStateMachine
