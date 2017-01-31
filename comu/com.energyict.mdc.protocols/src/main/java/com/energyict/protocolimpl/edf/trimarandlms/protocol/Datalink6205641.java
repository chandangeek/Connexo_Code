/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Datalink6205641.java
 *
 * Created on 13 februari 2007, 17:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Datalink6205641 {

    final int DEBUG=0;

    // KV_DEBUG was 10000
    final long TIMEOUT=15000; // safety timer
    private final int MAX_DSDU_SIZE=123;

    // states
    final int STOPPED=0;
    final int MREC=1;
    final int DFRAME=2;
    final int MSEND=3;



    String[] states = new String[]{"STOPPED","MREC","DFRAME","MSEND"};

    // events
    final int NO_EVENT=-1;
    final int DL_REQUEST=0;
    final int DL_ABORT=1;
    final int PHY_INDICATE=2;
    final int PHY_ABORT=3;
    final int T1_TIMEOUT=4;

    String[] events = new String[]{"DL_REQUEST","DL_ABORT","PHY_INDICATE","PHY_ABORT","T1_TIMEOUT"};

    int state;

    // state machine variables
    private boolean ackExpected;
    private int maxRetry;
    private boolean send;
    private boolean confirm;
    private DSDU dsdu;
    private Frame frameReceived;
    private Frame previousFrameReceived=null;
    private DSDU previousDsdu=null;
    DSDU dsduReceived;
    private boolean strong;
    private boolean checkTimer=false;
    private Connection62056 connection;


    private int index;
    long protocolTimeout;

    /** Creates a new instance of Datalink6205641 */
    public Datalink6205641(Connection62056 connection) {
        this.connection=connection;
    }

    public void initDatalink() {
        state=STOPPED;
    }

    // from above, the transportlayer
    public void request(DSDU dsdu) throws IOException {

        if (DEBUG>=2){
        	System.out.println("KV_DEBUG> "+dsdu);
        }
        setDsdu(dsdu);
        stateMachine(DL_REQUEST);
    }

    public void respond() throws IOException {
        connection.getPhysical6205641().respond();
    }

    public void abort(boolean strong) throws IOException {
        setStrong(strong);
        stateMachine(DL_ABORT);
    }

    // from below, the physicallayer
    public void indicate(Frame frameReceived) throws IOException {

        setFrameReceived(frameReceived);
        stateMachine(PHY_INDICATE);
        connection.getTransport6205651().indicate(dsduReceived);
    }


    private void initTimer() {
//        protocolTimeout = System.currentTimeMillis() + getConnection().getT1Timeout();
//        checkTimer=true;
    }
    private void stopTimer() {
//        checkTimer=false;
    }

    private void stateMachine(int event) throws IOException {

        long safetyTimeout = System.currentTimeMillis() + TIMEOUT;
        int retry=0;
        String errorDescription="";
        int errorNr=0;
        while(true) {
            try {
                switch(state) {

                    case STOPPED: {

                        switch(event) {

                            case DL_REQUEST: {

                                init();
                                setSend(false);
                                setConfirm(true);
                                state = MREC;
                                sendFrame(getDsdu());
                            } break; // DL_REQUEST

                            case PHY_INDICATE: {
                                if (getFrameReceived().isText()) {
                                    if (getFrameReceived().isCheckFrame()) {
                                        init();
                                        setSend(true);
                                        setConfirm(false);
                                        state = MSEND;
                                        dsduReceived = getFrameReceived().getDsdu();
                                        //return;
                                    } else {
                                        init();
                                        setSend(true);
                                        setConfirm(true);
                                        state = MSEND;
                                    }
                                } else {
                                    throw new IOException("Datalink6205641, stateMachine, invalid state="+states[state]+", event="+events[event]);
                                }
                            } break; // PHY_INDICATE

                            default:
                                throw new IOException("Datalink6205641, stateMachine, invalid state="+states[state]+", event="+events[event]);

                        } // switch(event)

                    } break; // STOPPED

                    case MREC: {
                        // here we must receive a frame!
                        //if (event != PHY_INDICATE) {
                        if (event == NO_EVENT) {
                            try {
                                byte[] data = connection.receiveData();

                                frameReceived = new Frame();
                                frameReceived.init(data);

                                event = PHY_INDICATE;
                            }
                            catch(ProtocolConnectionException e) {
                                throw e;
                            }
                        } // if (event != PHY_INDICATE)

                        switch(event) {

                            case PHY_ABORT: {
                                throw new DatalinkAbortException("Datalink6205641, stateMachine, Phy abort eror, "+errorDescription, errorNr, true);
                            } // PHY_ABORT

                            case T1_TIMEOUT: {
                                if (DEBUG>=1) {
									System.out.println("KV_DEBUG> T1_TIMEOUT in MREC at "+System.currentTimeMillis());
								}
                                state = STOPPED;
                                connection.getPhysical6205641().abort();
                                throw new DatalinkAbortException("Datalink6205641, stateMachine, EL-4F eror (Expiry of the period T1 without any frame being received)", 4);
                            } // T1_TIMEOUT

                            case DL_ABORT: {
                                state = STOPPED;
                                if (isStrong()) {
                                    connection.getPhysical6205641().abort();
                                    state = STOPPED;
                                }
                                else {
                                    if (!isAckExpected()) {
                                       connection.getPhysical6205641().abort();
                                       state = STOPPED;
                                    }
                                    else {
                                        throw new IOException("Datalink6205641, stateMachine, invalid state="+states[state]+", event="+events[event]);
                                    }
                                }

                            } break; // DL_ABORT

                            case PHY_INDICATE: {
                                stopTimer();
                                state = DFRAME;
                                if (DEBUG>=2) {
									System.out.println("KV_DEBUG> PHY_INDICATE "+System.currentTimeMillis());
								}
                            } break; // PHY_INDICATE

                            default:
                                throw new IOException("Datalink6205641, stateMachine, invalid state="+states[state]+", event="+events[event]);

                        } // switch(event)

                    } break; // MREC

                    case DFRAME: {
                        boolean ack = isAck();

                        if (DEBUG>=2) {
							System.out.println("KV_DEBUG> Datalink, DFRAME, getFrameReceived().isCheckFrame()="+getFrameReceived().isCheckFrame()+", isAck()="+ack+", getFrameReceived().isText()="+getFrameReceived().isText());
						}

                        // Response received
                        if (getFrameReceived().isCheckFrame() && ack && getFrameReceived().isText()) {
                            if (DEBUG>=2) {
								System.out.println("KV_DEBUG> Datalink, frame received at "+System.currentTimeMillis());
							}
                            setAckExpected(false);
                            toggleConfirm();
                            state = MSEND;
                            dsduReceived = getFrameReceived().getDsdu();

                            if ((previousFrameReceived != null) && (previousFrameReceived.isSend() == frameReceived.isSend()) && !dsduReceived.isEnd()) {
                                dsduReceived=null;
                                if (DEBUG>=1) {
									System.out.println("KV_DEBUG> Datalink, duplicate frame received!");
								}
                            }
                            previousFrameReceived = getFrameReceived();

                        }

                        // ACK received
                        if (getFrameReceived().isCheckFrame() && ack && !getFrameReceived().isText()) {
                            if (DEBUG>=2) {
								System.out.println("KV_DEBUG> Datalink, ack received at "+System.currentTimeMillis());
							}
                            setAckExpected(false);
                            state = MSEND;

                            previousFrameReceived = getFrameReceived();

                            if ((dsduReceived != null) && (dsduReceived.isEnd())) {
								return;
							}


                        }

                        // bad frame retry
                        if (!(getFrameReceived().isCheckFrame() && ack) && (index <= getMaxRetry())) {
                            previousFrameReceived=null;
                            if (DEBUG>=1) {
								System.out.println("KV_DEBUG> Datalink, error frame received at "+System.currentTimeMillis());
							}
                            initTimer();
                            reSendFrame(); //getDsdu());
                            state = MREC;
                        }

                        // bad frame max retries
                        if (!(getFrameReceived().isCheckFrame() && ack) && (index > getMaxRetry())) {
                            if (DEBUG>=1) {
								System.out.println("KV_DEBUG> Datalink, error frame max retries "+System.currentTimeMillis());
							}
                            previousFrameReceived=null;
                            connection.getPhysical6205641().abort();
                            state = STOPPED;
                            throw new DatalinkAbortException("Datalink6205641, stateMachine, EL-5F eror (MaxRetry repeated transmissions of the same frame without any acknowledgement frame being received)", 5);
                        }


                    } break; // DFRAME

                    case MSEND: {
                        switch(event) {

                            case DL_REQUEST: {
                                if (DEBUG>=2) {
									System.out.println("KV_DEBUG> MSEND, send frame "+System.currentTimeMillis());
								}
                                toggleSend();
                                state = MREC;
                                sendFrame(getDsdu());
                            } break; // DL_REQUEST

                            default: {
                                // send ACK
                                if (DEBUG>=2) {
									System.out.println("KV_DEBUG> MSEND, send ack "+System.currentTimeMillis());
								}
                                state = MREC;
                                sendFrame();

                                if (dsduReceived != null) {
									if (DEBUG>=2) {
										System.out.println("KV_DEBUG> MSEND, send ack, "+dsduReceived);
									}
								}

                                if ((dsduReceived==null) || ((dsduReceived!=null) && (!dsduReceived.isEnd()))) {
									return;
								}

                            } break;

                        } // switch(event)
                    } break; // MSEND

                } // switch(state);

                event = NO_EVENT; // reset event
            }
            catch(PhysicalAbortException e) {
                if (DEBUG>=1) {
					System.out.println("KV_DEBUG> Datalink, PhysicalAbortException, "+e.toString()+" at "+System.currentTimeMillis()+", generates PHY_ABORT event!");
				}
                errorNr = e.getErrorNr();
                errorDescription = e.toString();
                event = PHY_ABORT;
            }
            catch(ProtocolConnectionException e) {
                event = T1_TIMEOUT;
                if (DEBUG>=1) {
					System.out.println("KV_DEBUG> Datalink, ProtocolConnectionException, "+e.toString()+" at "+System.currentTimeMillis()+", generates T1_TIMEOUT event!");
				}
            } // catch(ProtocolConnectionException e)
            catch(ConnectionException e) {
                if (DEBUG>=1) {
					System.out.println("KV_DEBUG> Datalink, ConnectionException, "+e.toString()+" at "+System.currentTimeMillis());
				}
                throw e;
            } // catch(ConnectionException e)

//            if (checkTimer && (((long) (System.currentTimeMillis() - protocolTimeout)) > 0)) {
//                event = T1_TIMEOUT;
//            } // if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0)

            if (((long) (System.currentTimeMillis() - safetyTimeout)) > 0) {
                throw new ConnectionException("Datalink6205641, stateMachine, Safety timeout",connection.getTIMEOUT_ERROR());
            } // if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0)

        } // while(true)

        //throw new IOException("Datalink6205641, stateMachine, program flow error!");

    } // private void stateMachine(int event)


    // KV_DEBUG ***DEBUG***
//    int debugCount=0;

    private boolean isAck() {

//        // KV_DEBUG ***DEBUG***
//        debugCount++;
//        if ((debugCount%15)==0) {
//            System.out.print("Simulate BAD FRAME RECEPTION for "+ProtocolUtils.outputHexString(getFrameReceived().getData())+"\n");
//            return false;
//        }

        return getFrameReceived().isConfirm() == isSend();
    }

    private void sendFrame() throws IOException {
        sendFrame(null);
    }
    private void sendFrame(DSDU dsdu) throws IOException {
        Frame frame = new Frame();
        if (dsdu == null) {
			frame.init(isSend(),isConfirm());
		} else {
            setAckExpected(true);
            dsduReceived = null;
            frame.init(isSend(),isConfirm(), dsdu);
        }
        //if (DEBUG >=1) System.out.println("KV_DEBUG> "+frame);

        previousDsdu = dsdu;

        initTimer();
        setIndex(1);

        if (DEBUG>=2) {
			System.out.println("KV_DEBUG> sendFrame(), index="+index+", frame data: "+ProtocolUtils.outputHexString(frame.getData()));
		}
        getConnection().getPhysical6205641().request(frame);
    }
    private void reSendFrame() throws IOException {
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> Datalink, reSendFrame, "+ProtocolUtils.outputHexString(dsdu.getData()));
		}
        Frame frame = new Frame();
        if (previousDsdu == null) {
			frame.init(isSend(),isConfirm());
		} else {
            dsduReceived = null;
            frame.init(isSend(),isConfirm(), previousDsdu);
        }
        initTimer();
        index++;
        if (DEBUG>=2) {
			System.out.println("KV_DEBUG> reSendFrame(), index="+index+", frame data: "+ProtocolUtils.outputHexString(frame.getData()));
		}
        getConnection().getPhysical6205641().request(frame);
    }


//    private boolean checkFrame(Frame frame) throws IOException {
//
//        // KV_TO_DO implement
//        return false;
//    }



    private void init() {
        ackExpected = false;
        maxRetry = 5;
    }

    private boolean isAckExpected() {
        return ackExpected;
    }

    private void setAckExpected(boolean ackExpected) {
        this.ackExpected = ackExpected;
    }

    private int getMaxRetry() {
        return maxRetry;
    }

    private void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    private boolean isSend() {
        return send;
    }

    private void setSend(boolean send) {
        this.send = send;
    }

    private boolean toggleSend() {
        if (isSend()) {
			setSend(false);
		} else {
			setSend(true);
		}
        return isSend();
    }

    private boolean toggleConfirm() {
        if (isConfirm()) {
			setConfirm(false);
		} else {
			setConfirm(true);
		}
        return isConfirm();
    }

    private boolean isConfirm() {
        return confirm;
    }

    private void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }



    private DSDU getDsdu() {
        return dsdu;
    }

    private void setDsdu(DSDU dsdu) {
        this.dsdu = dsdu;
    }

    private Connection62056 getConnection() {
        return connection;
    }

    private void setConnection(Connection62056 connection) {
        this.connection = connection;
    }

    private int getIndex() {
        return index;
    }

    private void setIndex(int index) {
        this.index = index;
    }

    private void incIndex() {
        this.index++;
    }



    public boolean isStrong() {
        return strong;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }

    public int getMAX_DSDU_SIZE() {
        return MAX_DSDU_SIZE;
    }

    public Frame getFrameReceived() {
        return frameReceived;
    }

    public void setFrameReceived(Frame frameReceived) {
        this.frameReceived = frameReceived;
    }

} // public class Datalink6205641
