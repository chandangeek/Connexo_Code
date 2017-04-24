/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DatalinkLayer.java
 *
 * Created on 29 juni 2006, 10:05
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.CRCGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DatalinkLayer {

    private final int STATE_INIT=0;
    private final int STATE_STOPPED=1;
    private final int STATE_MUST_REC_PHY_SEND_OK=2;
    private final int STATE_MUST_REC_FRAME_CONTROL=3;
    private final int STATE_IDLE=4;
    private final int STATE_MUST_SEND_FRAME_CONTROL=5;
    private final int STATE_TEST_SEQ_NR_OF_FRAME_CONTROL=6;
    private final int STATE_TEST_SEQ_NR_OF_DATA_FRAME=7;

    private final int EVENT_DL_REQUEST=0;
    private final int EVENT_PHY_INFORM_FRAME_RECEIVED=1;
    private final int EVENT_PHY_INFORM_FRAME_SEND=2;
    private final int EVENT_DL_ABORT=3;
    private final int EVENT_PHY_INFORM_ABORT=4;
    private final int EVENT_TIMER_EXPIRED=5; //  goes together with int timeoutTimerId;

    private final int TYPE_DATA=0;
    private final int TYPE_ACK=0x6;
    private final int TYPE_NACK=0xB;

    // errpli error bits
    private final int EL_R1=0x08; // error in frame crc, length or type
    private final int EL_R2=0x10; // error in frame sequence
    private final int EL_E1=0x20; // TL timeout waiting for Ack/Nack frame
    private final int EL_E2=0x40; // data frame received instead of ack/nack frame with seq = seq expected + 1
    private final int EL_E3=0x80; // data frame received instead of ack/nack frame with seq = seq expected - 1

    // ERRFAT error bits
    private final int EL_E4F=0x08; // maxerrl
    private final int EL_R3F=0x04; // reception of a data frame with seq != seq expected -1 OR  reception of a control frame without a previously send data frame
    private final int EL_E5F=0x10; // reception of a frame control with wrong sequence number OR reception of a data frame where a control frame was expected with a sequence number != from seq expected +/- 1

    // Timer id's
    private final int TL=0;
    private final int TL_VAL=3000; // 3000 ms or 3 sec


    int state=STATE_INIT;

    LayerManager layerManager=null;

    // testing modes, not implemented!
    boolean testS=false;
    boolean testM=false;
    int code_test;

    int maxErrl;
    int errl;
    int vseq,nseq;
    boolean ackExpected;
    int errpli;
    int codetest;
    long timeoutTL;
    int type;
    int size;
    byte[] text;

    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();
    byte[] dsdu; // dsdu request to send NL->DL
    byte[] fr; // assembled frame
    byte[] frame; // received PL->DL
    int errpliP;
    int errorNb;

    int timeoutTimerId;

    /** Creates a new instance of DatalinkLayer */
    public DatalinkLayer(LayerManager layerManager) {
        this.layerManager=layerManager;
    }

    private byte[] concat(int size, int type, int nseq) throws IOException {
        return concat(size, type, nseq, null);
    }
    private byte[] concat(int size, int type, int nseq, byte[] dsdu) throws IOException {
        txOutputStream.reset();
        int len = dsdu.length+2; // LEN (1 byte) ,type+nseq (1 byte), dsdu
        txOutputStream.write(len);
        txOutputStream.write(nseq| (type<<4));
        if (dsdu != null)
            txOutputStream.write(dsdu);
        return txOutputStream.toByteArray();
    } // void sendData(byte[] cmdData) throws ConnectionException

    private int crc(byte[] frame) {
        return CRCGenerator.calcCRCFull(frame);
    }

    private byte[] concat_frame(byte[] frame, int crc) throws IOException {
        txOutputStream.reset();
        txOutputStream.write(frame);
        txOutputStream.write(crc&0xFF);
        txOutputStream.write((crc>>8)&0xFF);
        return txOutputStream.toByteArray();
    }

    private boolean check_frame(byte[] frame, int errpliP) {

        if (errpliP != 0) return false;
        if (CRCGenerator.calcCRC(frame)!=0) return false;
        if (frame[0] != frame.length) return false;
        int temp = (frame[1]>>4) & 0xF;
        if ((temp != TYPE_DATA) && (temp != TYPE_ACK) && (temp != TYPE_NACK)) return false;
        return true;
    }

    private boolean is_ack(byte[] frame) {
        int temp = (frame[1]>>4) & 0xF;
        return (temp & TYPE_ACK) == TYPE_ACK;
    }
    private boolean is_nack(byte[] frame) {
        int temp = (frame[1]>>4) & 0xF;
        return (temp & TYPE_NACK) == TYPE_NACK;
    }

    // KV_TO_DO
    private boolean isText(byte[] frame) { // what do we have to implement here?
        return true;
    }
    private void extract_test(byte[] frame, boolean testM, int code_test) { // what do we have to implement here?

    }


    // KV_TO_DO
    private void init_Timer(int timerId) { // what do we have to implement here?
        // implement a timer mechanisme that can respond with events if expired!
        switch(timerId) {
            case TL: {

            } break; // TL

        } // switch(timerId)
    } // private void initTimer(int timerId)

    // KV_TO_DO
    private void stop_timer(int timerId) { // what do we have to implement here?
        // stop the timer so not to expire and send events anymore!
        switch(timerId) {
            case TL: {

            } break; // TL

        } // switch(timerId)
    } // private void stopTimer(int timerId)

    private boolean check_ELR2(int errpli) {
        return (errpli & EL_R2) == EL_R2;
    }

    private byte[] extract_text(byte[] frame) {
        return ProtocolUtils.getSubArray2(frame, 2, frame.length-4);
    }

    public void stateMachine(int event) throws IOException {

        switch(state) {

            case STATE_INIT: {
                maxErrl=8;
                errl=1;
                vseq=1;
                ackExpected=false;
                errpli=0;
                codetest=0;
                timeoutTL=3300; // 3.3 sec
                state=STATE_STOPPED;
            } break; // STATE_INIT

            case STATE_STOPPED: {
                switch(event) {
                    case EVENT_DL_REQUEST: {
                        nseq=vseq;
                        type=TYPE_DATA;
                        text = dsdu;
                        size = text.length+4;
                        fr = concat(size, type, nseq, text);
                        layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                        ackExpected=true;
                        errl = errl+1;
                        state = STATE_MUST_REC_PHY_SEND_OK;
                    } break; // EVENT_DL_REQUEST

                    case EVENT_PHY_INFORM_FRAME_RECEIVED: {
                        if (!check_frame(frame, errpliP)) {
                            errpli = errpli | errpliP | EL_R1;
                            state = STATE_MUST_SEND_FRAME_CONTROL;
                        } else if ((check_frame(frame, errpliP)) && ((nseq==vseq) || (nseq==(vseq-1))) && isText(frame)) {
                            state = STATE_TEST_SEQ_NR_OF_DATA_FRAME;
                        }


                    } break; // EVENT_PHY_INFORM_DATA
                } // switch(event)
            } break; // STATE_STOPPED

            case STATE_MUST_REC_PHY_SEND_OK: {
                switch(event) {
                    case EVENT_PHY_INFORM_FRAME_SEND: {
                        if (ackExpected) {
                            errpli=0;
                            init_Timer(TL);
                            state = STATE_MUST_REC_FRAME_CONTROL;
                        }
                        else if ((!ackExpected) && (type == TYPE_ACK) && (!check_ELR2(errpli))) {
                            dlInformDataReceived(extract_text(frame));
                            errpli=0;
                            vseq++;
                            state = STATE_IDLE;
                        }
                        else if ((!ackExpected) && (type == TYPE_ACK) && (check_ELR2(errpli))) {
                            errpli=0;
                            vseq++;
                            state = STATE_IDLE;
                        }
                        else if ((!ackExpected) && (type == TYPE_NACK)) {
                            errpli=0;
                            state = STATE_IDLE;
                        }
                    } break; // EVENT_PHY_INFORM_FRAME_SEND

                    case EVENT_DL_ABORT: {
                        errl=1;
                        vseq=1;
                        ackExpected=false;
                        layerManager.getPhysicalLayer().phyAbort();
                        state = STATE_STOPPED;
                    } break; // EVENT_DL_ABORT

                    case EVENT_PHY_INFORM_ABORT: {
                        errl=1;
                        vseq=1;
                        ackExpected=false;
                        dlAbortInform(errorNb);
                        state = STATE_STOPPED;
                    } break; // EVENT_PHY_INFORM_ABORT

                } // switch(event)

            } break; // STATE_MUST_REC_PHY_SEND_OK

            case STATE_MUST_REC_FRAME_CONTROL: {
                switch(event) {
                    case EVENT_PHY_INFORM_FRAME_RECEIVED: {

                        if (check_frame(frame, errpliP) && (!is_nack(frame))) {
                            stop_timer(TL);
                            extract_test(frame,testM,code_test);
                            state = STATE_TEST_SEQ_NR_OF_FRAME_CONTROL;
                        }
                        else if (check_frame(frame, errpliP) && is_nack(frame) && (errl < maxErrl)) {
                            stop_timer(TL);
                            extract_test(frame,testM,code_test);
                            layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                            errl++;
                            state = STATE_MUST_REC_PHY_SEND_OK;
                        }
                        else if (check_frame(frame, errpliP) && is_nack(frame) && (errl >= maxErrl)) {
                            errl=1;
                            vseq=1;
                            ackExpected=false;
                            stop_timer(TL);
                            extract_test(frame,testM,code_test);
                            dlAbortInform(EL_E4F);
                            layerManager.getPhysicalLayer().phyAbort();
                            state = STATE_STOPPED;
                        }
                        else if (!check_frame(frame, errpliP) && (errl < maxErrl)) {
                            errpli |= EL_R1;
                            stop_timer(TL);
                            layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                            errl++;
                            state = STATE_MUST_REC_PHY_SEND_OK;
                        }
                        else if (!check_frame(frame, errpliP) && (errl >= maxErrl)) {
                            errl=1;
                            vseq=1;
                            ackExpected=false;
                            stop_timer(TL);
                            dlAbortInform(EL_E4F);
                            layerManager.getPhysicalLayer().phyAbort();
                            state = STATE_STOPPED;
                        }
                    } break; // EVENT_PHY_INFORM_FRAME_RECEIVED

                    case EVENT_TIMER_EXPIRED: {
                        if ((timeoutTimerId == TL) &&  (errl < maxErrl)) {
                            errpli |= EL_E1;
                            layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                            errl++;
                            state = STATE_MUST_REC_PHY_SEND_OK;
                        }
                        else if ((timeoutTimerId == TL) &&  (errl >= maxErrl)) {
                            errl=1;
                            vseq=1;
                            ackExpected=false;
                            dlAbortInform(EL_E4F);
                            layerManager.getPhysicalLayer().phyAbort();
                            state = STATE_STOPPED;
                        }
                    } break; // EVENT_TIMER_EXPIRED

                    case EVENT_DL_ABORT: {
                            errl=1;
                            vseq=1;
                            ackExpected=false;
                            stop_timer(TL);
                            layerManager.getPhysicalLayer().phyAbort();
                            state = STATE_STOPPED;
                    } break; // EVENT_DL_ABORT

                    case EVENT_PHY_INFORM_ABORT: {
                            errl=1;
                            vseq=1;
                            ackExpected=false;
                            stop_timer(TL);
                            dlAbortInform(errorNb);
                            state = STATE_STOPPED;
                    } break; // EVENT_PHY_INFORM_ABORT

                } // switch(event)
            } break; // STATE_MUST_REC_FRAME_CONTROL

            case STATE_IDLE: {
                switch(event) {
                    case EVENT_DL_REQUEST: {
                        if (!testS) {
                            nseq=vseq;
                            type=TYPE_DATA;
                            text = dsdu;
                            size = text.length+4;
                            fr = concat(size, type, nseq, text);
                            layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                            ackExpected=true;
                            errl = errl+1;
                            state = STATE_MUST_REC_PHY_SEND_OK;
                        }
                        else if (testS) {
                            nseq=vseq;
                            type=TYPE_DATA;
                            text = dsdu;
                            size = text.length+5;
                            fr = concat(size, type, nseq, text);
                            layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                            ackExpected=true;
                            errl = errl+1;
                            state = STATE_MUST_REC_PHY_SEND_OK;
                        }
                    } break; // EVENT_DL_REQUEST

                    case EVENT_PHY_INFORM_FRAME_RECEIVED: {
                        if (!check_frame(frame, errpliP)) {
                            errpli = errpli | errpliP | EL_R1;
                            type = TYPE_NACK;
                            state = STATE_MUST_SEND_FRAME_CONTROL;
                        }
                        else if ((check_frame(frame, errpliP)) && ((nseq==vseq) || (nseq==(vseq-1))) && isText(frame)) {
                            extract_test(frame, testM, code_test);
                            state = STATE_TEST_SEQ_NR_OF_DATA_FRAME;
                        }
                        else if ((check_frame(frame, errpliP)) && ((!isText(frame)) || (isText(frame) && (nseq != vseq) && (nseq !=(vseq-1))))) {
                            errl=1;
                            vseq=1;
                            ackExpected=false;
                            extract_test(frame, testM, code_test);
                            dlAbortInform(EL_R3F);
                            layerManager.getPhysicalLayer().phyAbort();
                            state = STATE_STOPPED;
                        }

                    } break; // EVENT_PHY_INFORM_FRAME_RECEIVED

                    case EVENT_DL_ABORT: {
                        errl=1;
                        vseq=1;
                        ackExpected=false;
                        layerManager.getPhysicalLayer().phyAbort();
                        state = STATE_STOPPED;
                    } break; // EVENT_DL_ABORT

                    case EVENT_PHY_INFORM_ABORT: {
                        errl=1;
                        vseq=1;
                        ackExpected=false;
                        dlAbortInform(errorNb);
                        state = STATE_STOPPED;
                    } break; // EVENT_PHY_INFORM_ABORT

                } // switch(event)
            } break; // STATE_IDLE

            case STATE_MUST_SEND_FRAME_CONTROL: {
                if (!testS) {
                    nseq=vseq;
                    type = TYPE_ACK;
                    size=4;
                    fr=concat(size, type, nseq);
                    layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                    state = STATE_MUST_REC_PHY_SEND_OK;
                }
                else if (testS) {
                    nseq=vseq;
                    type = TYPE_ACK;
                    size=5;
                    fr=concat(size, type, nseq);
                    layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                    state = STATE_MUST_REC_PHY_SEND_OK;
                }
            } break; // STATE_MUST_SEND_FRAME_CONTROL

            case STATE_TEST_SEQ_NR_OF_FRAME_CONTROL: {
                if (is_ack(frame) && (nseq==vseq)) {
                    errl=1;
                    vseq++;
                    dlInformSendDataOK();
                    ackExpected=false;
                    state = STATE_IDLE;
                }
                else if (is_ack(frame) && (nseq!=vseq)) {
                    errl=1;
                    vseq=1;
                    ackExpected=false;
                    dlAbortInform(EL_E5F);
                    layerManager.getPhysicalLayer().phyAbort();
                    state = STATE_STOPPED;
                }
                else if ((isText(frame)) && (nseq == (vseq-1)) && (errl < maxErrl)) {
                    layerManager.getPhysicalLayer().phyRequestSendData(concat_frame(fr,crc(fr)));
                    errl++;
                    errpli |= EL_E3;
                    state = STATE_MUST_REC_PHY_SEND_OK;
                }
                else if ((isText(frame)) && (nseq == (vseq-1)) && (errl == maxErrl)) {
                    errl=1;
                    vseq=1;
                    ackExpected=false;
                    dlAbortInform(EL_E4F);
                    layerManager.getPhysicalLayer().phyAbort();
                    state = STATE_STOPPED;
                }
                else if ((isText(frame)) && (nseq == (vseq+1))) {
                    errpli |= EL_E2;
                    errl=1;
                    vseq++;
                    dlInformSendDataOK();
                    ackExpected=false;
                    state = STATE_IDLE;
                }
                else if ((isText(frame)) && (nseq != (vseq+1)) && (nseq != (vseq-1))) {
                    errl=1;
                    vseq=1;
                    ackExpected=false;
                    dlAbortInform(EL_E5F);
                    layerManager.getPhysicalLayer().phyAbort();
                    state = STATE_STOPPED;
                }

            } break; // STATE_TEST_SEQ_NR_OF_FRAME_CONTROL

            case STATE_TEST_SEQ_NR_OF_DATA_FRAME: {
                if (nseq==vseq) {
                    state = STATE_MUST_SEND_FRAME_CONTROL;
                }
                else if (nseq == (vseq+1)) {
                    errpli |= EL_E2;
                    vseq--;
                    state = STATE_MUST_SEND_FRAME_CONTROL;
                }
            } break; // STATE__TEST_SEQ_NR_OF_DATA_FRAME

        } // switch(state)

    } // public void stateMachine()

    public void phyInformFrameReceived(byte[] frame, int errpliP) throws IOException {
        this.frame=frame;
        this.errpliP=errpliP;
        stateMachine(EVENT_PHY_INFORM_FRAME_RECEIVED);
    }

    public void phyInformFrameSend() throws IOException {
        stateMachine(EVENT_PHY_INFORM_FRAME_SEND);
    }

    public void phyAbortInform(int errorNb) throws IOException {
        this.errorNb=errorNb;
        stateMachine(EVENT_PHY_INFORM_ABORT);
    }

    public void dlRequestSendData(byte[] dsdu) throws IOException {
        // request from session layer to send data
        this.dsdu=dsdu;
        stateMachine(EVENT_DL_REQUEST);
    }

    public void dlInformSendDataOK() throws IOException {
        layerManager.getSessionLayer().dlInformDataSend();
    }

    public void dlInformDataReceived(byte[] data) throws IOException {
        layerManager.getSessionLayer().dlInformDataReceived(data);
    }

    public void dlAbort() throws IOException {
        // request from session layer to finish datalink layer activities
        stateMachine(EVENT_DL_ABORT);
    }

    public void dlAbortInform(int errorNb) throws IOException {
        this.errorNb=errorNb;
        layerManager.getSessionLayer().dlAbortInform(errorNb);
    }

}
