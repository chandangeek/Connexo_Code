/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SessionLayer {

	private static final int STATE_INIT=0;
	private static final int STATE_STOPPED=1;
	private static final int STATE_MUST_REC_DL_SEND_OK=2;
	private static final int STATE_MUST_REC_DL_DATA_REC=3;
	private static final int STATE_WAIT_COMM=4;
	private static final int STATE_TEST_TYPE=5;
	private static final int STATE_TEST_SIZE=6;

	private static final int EVENT_CONNECT_REQUEST=0;
	private static final int EVENT_READ_REQUEST=1;
	private static final int EVENT_WRITE_REQUEST=2;
	private static final int EVENT_DISCONNECT_REQUEST=3;
	private static final int EVENT_TIMER_EXPIRED=4; // goes together with int timeoutTimerId;
	private static final int EVENT_DL_DATA_SEND=5;
	private static final int EVENT_DL_DATA_RECEIVED=6;
	private static final int EVENT_DL_ABORT=7;



	private static final int SPDU_TYPE_XID=0x0F;
	private static final int SPDU_TYPE_EOS=0x01;
	private static final int SPDU_TYPE_ENQ=0x09;
	private static final int SPDU_TYPE_REC=0x06;
	private static final int SPDU_TYPE_DAT=0x0C;
	private static final int SPDU_TYPE_EOD=0x03;
	private static final int SPDU_TYPE_WTM=0xA;


	// error codes errfat
	private static final int ES_R3F=0x80; // TSE timeout
	private static final int ES_R2F=0x40; // reception of a non existing spdu
	private static final int ES_R1F=0x20; // reception of a bad master and/or slave

	// error codes errses
	private static final int ES_R5F=0x02; // reception of a non expected number of data
	private static final int ES_R4F=0x01; // reception of a write command in a read state


	private LayerManager layerManager = null;
	private int state=STATE_INIT;
	private byte[] ssdu;
	private int masterId,slaveId;
	private int mstId,slId;
	private int maxPktSize;
	private boolean endTMA;
	private int comm;
	private int errses;
	private int errfat;
	private int errNb;
	private int type;
	private byte[] spdu;
	private int timeoutTimerId;
	private int lgSend;
	private int lgReceive;
	private int lgPacket;
	private byte[] packet;
	private byte[] rMsg;
	private byte[] sMsg;
	private int code;
	private int lg;

	// Timer id's
	private static final int TSE=0;
	private static final int TPA=1;
	private static final int TMA=2;

	/** Creates a new instance of SessionLayer */
	public SessionLayer(LayerManager layerManager) {
		this.layerManager=layerManager;
	}

	private void substr_pack(byte[] sMsg, int maxPktSize, byte[] packet) {
		packet = ProtocolUtils.getSubArray2(sMsg, 0, maxPktSize);
	}

	private void store_error(int errNb,int comm) {
		if (comm != -1) {
			this.errses &= this.errses & 0x0F;
			this.errses |= ((comm<<4)&0xF0);
		}
		if (errNb != -1) {
			this.errfat |= errNb;
		}
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
		if ((type == SPDU_TYPE_XID) && (comm == SPDU_TYPE_XID)) {
			return true;
		}
		if ((type == SPDU_TYPE_DAT) && ((comm == SPDU_TYPE_ENQ) || (comm == SPDU_TYPE_DAT))) {
			return true;
		}
		if ((type == SPDU_TYPE_EOD) && (comm == SPDU_TYPE_DAT)) {
			return true;
		}
		if ((type == SPDU_TYPE_WTM) && (comm == SPDU_TYPE_WTM)) {
			return true;
		}

		return false;
	}

	private boolean checkId(byte[] spdu) throws IOException {
		int temp = ProtocolUtils.getIntLE(spdu,3,2);
		if (temp != this.slId) {
			return false;
		}
		temp = ProtocolUtils.getIntLE(spdu,1,2);
		if (temp != this.mstId) {
			return false;
		}

		return false;

	}

	private byte[] extract_pkt(byte[] spdu) {
		this.packet = ProtocolUtils.getSubArray(spdu, 1);
		return this.packet;
	}

	private int extract_type(byte[] spdu) {
		this.type = spdu[0]&0xFF;
		return this.type;
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

		switch(this.state) {

		case STATE_INIT: {
			this.maxPktSize=121;
			this.state = STATE_STOPPED;
		} break; // STATE_INIT

		case STATE_STOPPED: {

			switch(event) {
			case EVENT_CONNECT_REQUEST: {
				this.endTMA=false;
				this.slId = this.slaveId;
				this.mstId = this.masterId;
				this.comm = SPDU_TYPE_XID;
				store_error(this.errNb,this.comm);
				this.layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_XID,this.mstId,this.slaveId));
				this.state = STATE_MUST_REC_DL_SEND_OK;
			} break; // EVENT_CONNECT_REQUEST

			} // switch(event)

		} break; // STATE_STOPPED

		case STATE_MUST_REC_DL_SEND_OK: {
			switch(event) {
			case EVENT_DL_DATA_SEND: {
				if ((this.comm==SPDU_TYPE_XID) || (this.comm==SPDU_TYPE_ENQ) || (this.comm==SPDU_TYPE_WTM)) {
					init_Timer(TSE);
					this.state = STATE_MUST_REC_DL_DATA_REC;
				}
				else if (this.comm==SPDU_TYPE_EOD) {
					writeConfirmation(0);
					init_Timer(TMA);
					this.state = STATE_WAIT_COMM;
				}
				else if ((this.comm==SPDU_TYPE_EOS) && (this.endTMA)) {
					this.layerManager.getDatalinkLayer().dlAbort();
					abortIndication();
					this.state = STATE_STOPPED;
				}
				else if ((this.comm==SPDU_TYPE_EOS) && (!this.endTMA)) {
					stop_timer(TMA);
					this.layerManager.getDatalinkLayer().dlAbort();
					disconnectConfirmation();
					this.state = STATE_STOPPED;
				}
				else if ((this.comm==SPDU_TYPE_DAT) && (this.lgSend>0)) {
					store_error(-1,this.comm);
					substr_pack(this.sMsg,this.maxPktSize,this.packet);
					this.lgSend -= this.packet.length;
					this.layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_DAT,this.packet));
				}
				else if ((this.comm==SPDU_TYPE_DAT) && (this.lgSend==0)) {
					this.comm = SPDU_TYPE_EOD;
					store_error(-1,this.comm);
				}


			} break; // EVENT_DL_DATA_SEND

			case EVENT_TIMER_EXPIRED: {
				if (this.timeoutTimerId == TMA) {
					this.endTMA=true;
				}
			} break; // EVENT_TIMER_EXPIRED

			case EVENT_DL_ABORT: {
				stop_timer(TMA);
				store_error(this.errNb, -1);
				abortIndication();
				this.state = STATE_STOPPED;
			} break; // EVENT_DL_ABORT
			}
		} break; // STATE_MUST_REC_DL_SEND_OK

		case STATE_MUST_REC_DL_DATA_REC: {
			switch(event) {
			case EVENT_DL_DATA_RECEIVED: {
				if (check_type(extract_type(this.spdu), this.comm)) {
					stop_timer(TSE);
					store_error(-1, this.type);
					this.state = STATE_TEST_TYPE;
				}
				else if (!check_type(extract_type(this.spdu), this.comm)) {
					store_error(ES_R2F, -1);
					stop_timer(TMA);
					this.layerManager.getDatalinkLayer().dlAbort();
					abortIndication();
					this.state = STATE_STOPPED;
				}

			} break; // EVENT_DL_DATA_RECEIVED

			case EVENT_TIMER_EXPIRED: {
				if (this.timeoutTimerId == TSE) {
					store_error(ES_R3F, -1);
					stop_timer(TMA);
					this.layerManager.getDatalinkLayer().dlAbort();
					abortIndication();
					this.state = STATE_STOPPED;
				}
				else if (this.timeoutTimerId == TMA) {
					this.endTMA = true;
				}
			} break; // EVENT_TIMER_EXPIRED

			case EVENT_DL_ABORT: {
				store_error(this.errNb, -1);
				stop_timer(TSE);
				stop_timer(TMA);
				abortIndication();
				this.state = STATE_STOPPED;
			} break; // EVENT_DL_ABORT
			}
		} break; // STATE_MUST_REC_DL_DATA_REC

		case STATE_TEST_TYPE: {
			if ((this.type == SPDU_TYPE_XID) && checkId(this.spdu)) {
				connectConfirmation();
				init_Timer(this.TPA);
				this.state = STATE_WAIT_COMM;
			}
			else if ((this.type == SPDU_TYPE_XID) && !checkId(this.spdu)) {
				store_error(ES_R1F,-1);
				stop_timer(TMA);
				this.layerManager.getDatalinkLayer().dlAbort();
				abortIndication();
				this.state = STATE_STOPPED;
			}
			else if (this.type == SPDU_TYPE_DAT) {
				this.packet = extract_pkt(this.spdu);
				this.lgPacket = this.packet.length;
				this.comm = SPDU_TYPE_DAT;
				store_error(-1,this.comm);
				this.state = STATE_TEST_SIZE;
			}
			else if (this.type == SPDU_TYPE_EOD) {
				this.comm=SPDU_TYPE_EOD;
				store_error(-1,this.comm);
				this.state = STATE_TEST_SIZE;
			}
			else if ((this.type == SPDU_TYPE_WTM)  && !this.endTMA) {
				init_Timer(this.TPA);
				this.state = STATE_WAIT_COMM;
			}
			else if ((this.type == SPDU_TYPE_WTM)  && this.endTMA) {
				this.comm = SPDU_TYPE_EOS;
				store_error(-1,this.comm);
				this.layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_EOS});
				this.state = STATE_MUST_REC_DL_SEND_OK;
			}
		} break; // STATE_TEST_TYPE

		case STATE_TEST_SIZE: {
			if ((this.type == SPDU_TYPE_DAT) && (this.lgPacket <= this.lgReceive)) {
				this.rMsg = concat(this.rMsg,this.packet);
				this.lgReceive-=this.lgPacket;
				this.state = STATE_MUST_REC_DL_DATA_REC;

			}
			else if (((this.type == SPDU_TYPE_DAT) && (this.lgPacket > this.lgReceive)) || ((this.type == SPDU_TYPE_EOD) && (this.lgReceive!=0))) {
				store_error(ES_R5F, -1);
				stop_timer(this.TMA);
				this.layerManager.getDatalinkLayer().dlAbort();
				abortIndication();
				this.state = STATE_STOPPED;
			}
			else if ((this.type == SPDU_TYPE_EOD) && (this.lgReceive==0)) {
				readConfirmation(this.spdu);
				init_Timer(this.TPA);
				this.state = STATE_WAIT_COMM;
			}
		} break; // STATE_TEST_TYPE

		case STATE_WAIT_COMM: {

			switch(event) {

			case EVENT_TIMER_EXPIRED: {

				if ((this.timeoutTimerId == this.TPA) && (this.type != SPDU_TYPE_WTM)) {
					this.comm = SPDU_TYPE_WTM;
					store_error(-1,this.comm);
					this.layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_WTM});
					init_Timer(this.TMA);
					this.state = STATE_MUST_REC_DL_SEND_OK;
				}
				else if ((this.timeoutTimerId == this.TPA) && (this.type == SPDU_TYPE_WTM)) {
					this.layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_WTM});
					this.state = STATE_MUST_REC_DL_SEND_OK;
				}
				else if (this.timeoutTimerId == this.TMA) {
					this.endTMA = true;
				}
			} break; // EVENT_TIMER_EXPIRED

			case EVENT_READ_REQUEST: {
				stop_timer(this.TPA);
				this.endTMA=false;
				stop_timer(this.TMA);
				this.rMsg = this.ssdu;
				this.lgReceive=this.lg;
				this.comm=SPDU_TYPE_ENQ;
				store_error(-1,this.comm);
				this.layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_ENQ,this.code));
				this.state = STATE_MUST_REC_DL_SEND_OK;
			} break; // EVENT_READ_REQUEST

			case EVENT_WRITE_REQUEST: {
				if (this.mstId != 0) {
					stop_timer(this.TPA);
					this.endTMA=false;
					stop_timer(this.TMA);
					this.sMsg = this.ssdu;
					this.lgSend=this.sMsg.length;
					store_error(-1,SPDU_TYPE_REC);
					this.layerManager.getDatalinkLayer().dlRequestSendData(concat(SPDU_TYPE_REC,this.code));
					this.comm=SPDU_TYPE_DAT;
					this.state = STATE_MUST_REC_DL_SEND_OK;
				}
				else if (this.mstId == 0) {
					writeConfirmation(-1);
				}
			} break; // EVENT_WRITE_REQUEST

			case EVENT_DISCONNECT_REQUEST: {
				stop_timer(TPA);
				this.endTMA=false;
				stop_timer(TMA);
				this.comm=SPDU_TYPE_EOS;
				store_error(-1,this.comm);
				this.layerManager.getDatalinkLayer().dlRequestSendData(new byte[]{SPDU_TYPE_EOS});
				this.state = STATE_MUST_REC_DL_SEND_OK;
			} break; // EVENT_DISCONNECT_REQUEST

			case EVENT_DL_ABORT: {
				store_error(this.errNb,-1);
				stop_timer(TPA);
				stop_timer(TMA);
				abortIndication();
				this.state = STATE_STOPPED;
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
		this.spdu=data;
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
