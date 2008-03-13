/*
 * SessionLayer.java
 *
 * Created on 29 juni 2006, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import com.energyict.protocol.*;
import java.io.*;

/**
 *
 * @author Koen
 */
public class SessionLayer {
    
    private final int STATE_INIT=0;
    private final int STATE_STOPPED=1;    
    private final int STATE_MUST_REC_DL_SEND_OK=2;
    private final int STATE_MUST_REC_DL_DATA_REC=3;
    private final int STATE_WAIT_COMM=4;
    private final int STATE_TEST_TYPE=5;
    private final int STATE_TEST_SIZE=6;
            
    private final int EVENT_CONNECT_REQUEST=0;
    private final int EVENT_READ_REQUEST=1;
    private final int EVENT_WRITE_REQUEST=2;
    private final int EVENT_DISCONNECT_REQUEST=3;
    private final int EVENT_TIMER_EXPIRED=4; // goes together with int timeoutTimerId;
    private final int EVENT_DL_DATA_SEND=5;
    private final int EVENT_DL_DATA_RECEIVED=6;
    private final int EVENT_DL_ABORT=7;
    
      
    
    private final int SPDU_TYPE_XID=0x0F;
    private final int SPDU_TYPE_EOS=0x01;
    private final int SPDU_TYPE_ENQ=0x09;
    private final int SPDU_TYPE_REC=0x06;
    private final int SPDU_TYPE_DAT=0x0C;
    private final int SPDU_TYPE_EOD=0x03;
    private final int SPDU_TYPE_WTM=0xA;
    
    
    // error codes errfat
    private final int ES_R3F=0x80; // TSE timeout
    private final int ES_R2F=0x40; // reception of a non existing spdu
    private final int ES_R1F=0x20; // reception of a bad master and/or slave
    
    // error codes errses
    private final int ES_R5F=0x02; // reception of a non expected number of data
    private final int ES_R4F=0x01; // reception of a write command in a read state
            
            
    LayerManager layerManager = null;
    int state=STATE_INIT;
    byte[] ssdu;
    int masterId,slaveId;
    int mstId,slId;
    int maxPktSize;
    boolean endTMA;
    int comm;
    int errses;
    int errfat;
    int errNb;
    int type;
    byte[] spdu;
    int timeoutTimerId;
    int lgSend;
    int lgReceive;
    int lgPacket;
    byte[] packet;
    byte[] rMsg;
    byte[] sMsg;
    int code;
    int lg;
    
    // Timer id's
    private final int TSE=0;
    private final int TSE_VAL=22000; // 22000 ms or 22 sec    
    private final int TPA=1;
    private final int TPA_VAL=6000; // 6000 ms or 6 sec    
    private final int TMA=2;
    private final int TMA_VAL=300000; // 300000 ms or 300 sec    
    
    
    /** Creates a new instance of SessionLayer */
    public SessionLayer(LayerManager layerManager) {
        this.layerManager=layerManager;
    }
    
    private void substr_pack(byte[] sMsg, int maxPktSize, byte[] packet) {
        packet = ProtocolUtils.getSubArray2(sMsg, 0, maxPktSize);
    }
    
    private void store_error(int errNb,int comm) {
        if (comm != -1) {
            errses &= errses & 0x0F;
            errses |= ((comm<<4)&0xF0);
        }
        if (errNb != -1)
            errfat |= errNb;       
    }
    
    private byte[] concat(byte[] rMsg, byte[] packet) throws IOException { 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(rMsg);
        baos.write(packet);
        return baos.toByteArray();
    }
    
    private byte[] concat(int type, int mstId, int slId) {
        byte[] data = new byte[5]; 
        data[0] = (byte)type;
        data[1] = (byte)(mstId);
        data[2] = (byte)(mstId>>8);
        data[3] = (byte)(slId);
        data[4] = (byte)(slId>>8);
        return data;           
    }
    
    private byte[] concat(int type, int code) {
        byte[] data = new byte[2]; 
        data[0] = (byte)type;
        data[1] = (byte)code;
        return data;        
    }
    
    private byte[] concat(int type, byte[] packet) {
        byte[] data = new byte[1+packet.length]; 
        data[0] = (byte)type;
        System.arraycopy(packet, 0, data, 1, packet.length);
        return data;             
    }
    
    private boolean check_type(int type, int comm) {
        // master
        if ((type == SPDU_TYPE_XID) && (comm == SPDU_TYPE_XID)) return true;
        if ((type == SPDU_TYPE_DAT) && ((comm == SPDU_TYPE_ENQ) || (comm == SPDU_TYPE_DAT))) return true;
        if ((type == SPDU_TYPE_EOD) && (comm == SPDU_TYPE_DAT)) return true;
        if ((type == SPDU_TYPE_WTM) && (comm == SPDU_TYPE_WTM)) return true;
        
        return false;
    }
    
    private boolean checkId(byte[] spdu) throws IOException {
        int temp = ProtocolUtils.getIntLE(spdu,3,2);
        if (temp != slId) return false;
        temp = ProtocolUtils.getIntLE(spdu,1,2);
        if (temp != mstId) return false;
        
        return false;
        
    }
    
    private int extract_code(byte[] spdu) {
        code = (int)spdu[1]&0xFF;
        return code;
    }
    
    private int extract_Mst(byte[] spdu) throws IOException {
        mstId = ProtocolUtils.getIntLE(spdu,1,2);
        return mstId;
    }
    
    private int extract_Sl(byte[] spdu) throws IOException {
        slId = ProtocolUtils.getIntLE(spdu,3,2);
        return slId;
    }
    
    private byte[] extract_pkt(byte[] spdu) throws IOException {
        packet = ProtocolUtils.getSubArray(spdu, 1);
        return packet;
    }
    
    private int extract_type(byte[] spdu) throws IOException {
        type = (int)spdu[0]&0xFF;
        return type;
    }
    
   // KV_TO_DO
    private void init_Timer(int timerId) { // what do we have to implement here?
        // implement a timer mechanisme that can respond with events if expired!
        switch(timerId) {
            case TSE: {
                
            } break; // TSE
            
            case TPA: {
                
            } break; // TPA
            
            case TMA: {
                
            } break; // TMA
        } // switch(timerId)
    } // private void initTimer(int timerId)
    
    // KV_TO_DO
    private void stop_timer(int timerId) { // what do we have to implement here?
        // stop the timer so not to expire and send events anymore!
        switch(timerId) {
            case TSE: {
                
            } break; // TSE
            
            case TPA: {
                
            } break; // TPA
            
            case TMA: {
                
            } break; // TMA
        } // switch(timerId)
    } // private void stopTimer(int timerId)
    
    public void stateMachine(int event) throws IOException {
        
        switch(state) {
            
            case STATE_INIT: {
                maxPktSize=121;
                state = STATE_STOPPED;
            } break; // STATE_INIT
            
            case STATE_STOPPED: {
                
                switch(event) {
                    case EVENT_CONNECT_REQUEST: {
                        endTMA=false;
                        slId = slaveId;
                        mstId = masterId;
                        comm = SPDU_TYPE_XID;
                        store_error(errNb,comm);
                        layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_XID,mstId,slaveId));
                        state = STATE_MUST_REC_DL_SEND_OK;
                    } break; // EVENT_CONNECT_REQUEST
                    
                } // switch(event)
                
            } break; // STATE_STOPPED   
            
            case STATE_MUST_REC_DL_SEND_OK: {
                switch(event) {
                    case EVENT_DL_DATA_SEND: {
                        if ((comm==SPDU_TYPE_XID) || (comm==SPDU_TYPE_ENQ) || (comm==SPDU_TYPE_WTM)) {
                            init_Timer(TSE);
                            state = STATE_MUST_REC_DL_DATA_REC;
                        }
                        else if (comm==SPDU_TYPE_EOD) {
                            writeConfirmation(0);
                            init_Timer(TMA);
                            state = STATE_WAIT_COMM;
                        }
                        else if ((comm==SPDU_TYPE_EOS) && (endTMA)) {
                            layerManager.getDatalinkLayer().dlAbort();
                            abortIndication();
                            state = STATE_STOPPED;
                        }
                        else if ((comm==SPDU_TYPE_EOS) && (!endTMA)) {
                            stop_timer(TMA);
                            layerManager.getDatalinkLayer().dlAbort();
                            disconnectConfirmation();
                            state = STATE_STOPPED;
                        }
                        else if ((comm==SPDU_TYPE_DAT) && (lgSend>0)) {
                            store_error(-1,comm);
                            substr_pack(sMsg,maxPktSize,packet);
                            lgSend -= packet.length;
                            layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_DAT,packet));
                        }
                        else if ((comm==SPDU_TYPE_DAT) && (lgSend==0)) {
                            comm = SPDU_TYPE_EOD;
                            store_error(-1,comm);
                        }
                        
                        
                    } break; // EVENT_DL_DATA_SEND
                    
                    case EVENT_TIMER_EXPIRED: {
                        if (timeoutTimerId == TMA) {
                            endTMA=true;
                        }
                    } break; // EVENT_TIMER_EXPIRED
                    
                    case EVENT_DL_ABORT: {
                        stop_timer(TMA);
                        store_error(errNb, -1);
                        abortIndication();
                        state = STATE_STOPPED;
                    } break; // EVENT_DL_ABORT
                }
            } break; // STATE_MUST_REC_DL_SEND_OK
            
            case STATE_MUST_REC_DL_DATA_REC: {
                switch(event) {
                    case EVENT_DL_DATA_RECEIVED: {
                        if (check_type(extract_type(spdu), comm)) {
                            stop_timer(TSE);
                            store_error(-1, type);
                            state = STATE_TEST_TYPE;
                        }
                        else if (!check_type(extract_type(spdu), comm)) {
                            store_error(ES_R2F, -1);
                            stop_timer(TMA);
                            layerManager.getDatalinkLayer().dlAbort();
                            abortIndication();
                            state = STATE_STOPPED;
                        }
                        
                    } break; // EVENT_DL_DATA_RECEIVED
                    
                    case EVENT_TIMER_EXPIRED: {
                        if (timeoutTimerId == TSE) {
                            store_error(ES_R3F, -1);
                            stop_timer(TMA);
                            layerManager.getDatalinkLayer().dlAbort();
                            abortIndication();
                            state = STATE_STOPPED;
                        }
                        else if (timeoutTimerId == TMA) {
                            endTMA = true;
                        }
                    } break; // EVENT_TIMER_EXPIRED
                    
                    case EVENT_DL_ABORT: {
                        store_error(errNb, -1);
                        stop_timer(TSE);
                        stop_timer(TMA);
                        abortIndication();
                        state = STATE_STOPPED;
                    } break; // EVENT_DL_ABORT
                }
            } break; // STATE_MUST_REC_DL_DATA_REC
            
            case STATE_TEST_TYPE: {
                if ((type == SPDU_TYPE_XID) && checkId(spdu)) {
                    connectConfirmation();
                    init_Timer(TPA);
                    state = STATE_WAIT_COMM;
                }
                else if ((type == SPDU_TYPE_XID) && !checkId(spdu)) {
                    store_error(ES_R1F,-1);
                    stop_timer(TMA);
                    layerManager.getDatalinkLayer().dlAbort();
                    abortIndication();
                    state = STATE_STOPPED;
                }
                else if (type == SPDU_TYPE_DAT) {
                    packet = extract_pkt(spdu);
                    lgPacket = packet.length;
                    comm = SPDU_TYPE_DAT;
                    store_error(-1,comm);
                    state = STATE_TEST_SIZE;
                }
                else if (type == SPDU_TYPE_EOD) {
                    comm=SPDU_TYPE_EOD;
                    store_error(-1,comm);
                    state = STATE_TEST_SIZE;
                }
                else if ((type == SPDU_TYPE_WTM)  && !endTMA) {
                    init_Timer(TPA);
                    state = STATE_WAIT_COMM;
                }
                else if ((type == SPDU_TYPE_WTM)  && endTMA) {
                    comm = SPDU_TYPE_EOS;
                    store_error(-1,comm);
                    layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_EOS});
                    state = STATE_MUST_REC_DL_SEND_OK;
                }                
            } break; // STATE_TEST_TYPE
            
            case STATE_TEST_SIZE: {
                if ((type == SPDU_TYPE_DAT) && (lgPacket <= lgReceive)) {
                    rMsg = concat(rMsg,packet);
                    lgReceive-=lgPacket;
                    state = STATE_MUST_REC_DL_DATA_REC;
                    
                }
                else if ((type == SPDU_TYPE_DAT) && (lgPacket > lgReceive) || ((type == SPDU_TYPE_EOD) && (lgReceive!=0))) {
                    store_error(ES_R5F, -1);
                    stop_timer(TMA);
                    layerManager.getDatalinkLayer().dlAbort();
                    abortIndication();
                    state = STATE_STOPPED;
                }
                else if ((type == SPDU_TYPE_EOD) && (lgReceive==0)) {
                    readConfirmation(spdu);
                    init_Timer(TPA);
                    state = STATE_WAIT_COMM;
                }
            } break; // STATE_TEST_TYPE
            
            case STATE_WAIT_COMM: {
                
                switch(event) {
                    
                    case EVENT_TIMER_EXPIRED: {
                        
                        if ((timeoutTimerId == TPA) && (type != SPDU_TYPE_WTM)) {
                            comm = SPDU_TYPE_WTM;
                            store_error(-1,comm);
                            layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_WTM});
                            init_Timer(TMA);
                            state = STATE_MUST_REC_DL_SEND_OK;
                        }
                        else if ((timeoutTimerId == TPA) && (type == SPDU_TYPE_WTM)) {
                            layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_WTM});
                            state = STATE_MUST_REC_DL_SEND_OK;
                        }
                        else if (timeoutTimerId == TMA) {
                            endTMA = true;
                        }
                    } break; // EVENT_TIMER_EXPIRED
                    
                    case EVENT_READ_REQUEST: {
                        stop_timer(TPA);
                        endTMA=false;
                        stop_timer(TMA);
                        rMsg = ssdu;
                        lgReceive=lg;
                        comm=SPDU_TYPE_ENQ;
                        store_error(-1,comm);
                        layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_ENQ,code));
                        state = STATE_MUST_REC_DL_SEND_OK;
                    } break; // EVENT_READ_REQUEST
                    
                    case EVENT_WRITE_REQUEST: {
                        if (mstId != 0) {
                            stop_timer(TPA);
                            endTMA=false;
                            stop_timer(TMA);
                            sMsg = ssdu;
                            lgSend=sMsg.length;
                            store_error(-1,SPDU_TYPE_REC);
                            layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_REC,code));
                            comm=SPDU_TYPE_DAT;
                            state = STATE_MUST_REC_DL_SEND_OK;
                        }
                        else if (mstId == 0) {
                            writeConfirmation(-1);
                        }
                    } break; // EVENT_WRITE_REQUEST
                    
                    case EVENT_DISCONNECT_REQUEST: {
                        stop_timer(TPA);
                        endTMA=false;
                        stop_timer(TMA);
                        comm=SPDU_TYPE_EOS;
                        store_error(-1,comm);
                        layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_EOS});
                        state = STATE_MUST_REC_DL_SEND_OK;
                    } break; // EVENT_DISCONNECT_REQUEST
                    
                    case EVENT_DL_ABORT: {
                        store_error(errNb,-1);
                        stop_timer(TPA);
                        stop_timer(TMA);
                        abortIndication();
                        state = STATE_STOPPED;
                    } break; // EVENT_DL_ABORT 
                    
                } // switch(event)
                
            } break; // STATE_WAIT_COMM
            
        } // switch(state)
    } // public void stateMachine(int event) throws IOException
    
    // *********************************************************************************
    // DATALINK LAYER METHODS
    
    public void dlInformDataSend() throws IOException {
        stateMachine(EVENT_DL_DATA_SEND);
    }
    
    public void dlInformDataReceived(byte[] data) throws IOException {
        spdu=data;
        stateMachine(EVENT_DL_DATA_RECEIVED);
    }
    
    public void dlAbortInform(int errNb) throws IOException {
        this.errNb=errNb;
        stateMachine(EVENT_DL_ABORT);
    }
    
    // *********************************************************************************
    // SESSION LAYER METHODS
    
    public void abortIndication() {
        // KV_TO_DO?
    }
    
    public void readConfirmation(byte[] spdu) {
        // KV_TO_DO?
    }
    public void writeConfirmation(int err) {
        // KV_TO_DO?
    }
    public void connectConfirmation() {
        // KV_TO_DO?
    }
    public void disconnectConfirmation() {
        // KV_TO_DO?
    }        
    
    public void connectRequest(String masterId,String slaveId) throws IOException {
        this.masterId=Integer.parseInt(masterId);
        this.slaveId=Integer.parseInt(slaveId);
        stateMachine(EVENT_CONNECT_REQUEST);
    }
    
    public void disconnectRequest() throws IOException {
        stateMachine(EVENT_DISCONNECT_REQUEST);
    }    
    
    public void readRequest(int code, int lg, byte[] spdu) throws IOException {
        this.code=code;
        this.lg=lg;
        this.spdu=spdu;
        stateMachine(EVENT_READ_REQUEST);
    }
    
    public void writeRequest(int code, byte[] spdu) throws IOException {
        this.code=code;
        this.spdu=spdu;
        stateMachine(EVENT_WRITE_REQUEST);
    }
    
} // public class SessionLayer
