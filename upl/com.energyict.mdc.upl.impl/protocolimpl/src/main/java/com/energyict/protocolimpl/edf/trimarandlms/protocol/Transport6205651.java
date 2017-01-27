/*
 * Transport6205651.java
 *
 * Created on 14 februari 2007, 15:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 * Changes:
 * GNA |22012009| Made the safety timer customizable
 */
public class Transport6205651 {
    
    final int DEBUG=0;
    
    Connection62056 connection;
    private long TIMEOUT=300000; // safety timer
    
    
    // states
    final int STOPPED=0;
    final int IDLE=1;
    final int MSGT=2;
    String[] states=new String[]{"STOPPED","IDLE","MSGT"};
    
    // events
    final int TR_REQUEST=0;
    final int TR_ABORT=1;
    final int DL_INDICATE=2;
    final int DL_ABORT=3;
    String[] events=new String[]{"TR_REQUEST","TR_ABORT","DL_INDICATE","DL_ABORT"};
    
    int state;
    private boolean priority;
    private int stsap;
    private int dtsap;
    private TSDU tsdu;
    TSDU tsduReceived;
    private int maxPacketSize;
    private boolean strong;
    private DSDU dsduReceived;
    
    ByteArrayOutputStream baos;
    ByteArrayInputStream bais;
    
    
    /** Creates a new instance of Transport6205651 */
    public Transport6205651(Connection62056 connection) {
        this.connection=connection;
        this.TIMEOUT = this.connection.getSafetyTimeout();
    }
    
    public void initTransport() {
        state=STOPPED;
    }
    
    // from above, the applicationlayer
    public void request(boolean priority, int stsap, int dtsap, TSDU tsdu) throws IOException {
        tsduReceived = null;
        this.setPriority(priority);
        this.setTsdu(tsdu);
        this.setStsap(stsap);
        this.setDtsap(dtsap);
        stateMachine(TR_REQUEST);
    }

    public TSDU respond() throws IOException {
        long safetytimeout = System.currentTimeMillis() + TIMEOUT; // 5 min
        while(tsduReceived == null) {
            connection.getDatalink6205641().respond();
            if (System.currentTimeMillis() - safetytimeout > 0) {
                throw new ConnectionException("Transport6205651, respond() safetytimeout timeout error "+(TIMEOUT/1000)+" sec.",connection.getTIMEOUT_ERROR());
            }
        }
        return tsduReceived;
    }
    
    
    public void abort(boolean strong) throws IOException {
        setStrong(strong);
        stateMachine(TR_ABORT);
    }
    
    // from below, the datalinklayer
    public void indicate(DSDU dsduReceived) throws IOException {
        setDsduReceived(dsduReceived);
        stateMachine(DL_INDICATE);
    }
    
    public void stateMachine(int event) throws IOException {
        long safetyTimeout = System.currentTimeMillis() + TIMEOUT;
        String errorDescription="";
        int errorNr=0;
        while(true) {
            try {
                switch(state) {
                    case STOPPED: {
                        init();
                        state = IDLE;
                    } break; // STOPPED
                    
                    case IDLE: {
                        
                        switch(event) {
                            
                            case TR_REQUEST: {
                                bais = new ByteArrayInputStream(getTsdu().getData());
                                state=MSGT;
                                tsduReceived = null;
                                // If no bufferPool size available, throw new TransportAbortException ET_2F and abort the Datalink
                                // Not implemented. See 62056-51 page 17 for  more info
                                // return to STOPPED state
                                
                            } break; // TR_REQUEST
                            
                            case DL_INDICATE: {
                                if (getDsduReceived() == null) {
                                    //return;
                                    if (DEBUG>=1) {
										System.out.println("KV_DEBUG> Transport6205651, getDsduReceived() == null !!!");
									}
                                    throw new ConnectionException("Transport6205641, stateMachine, no response from meter!",connection.getPROTOCOL_ERROR());
                                }
                                
                                if (getDsduReceived().checkSgt() && !getDsduReceived().lastSgt()) {
                                    if (DEBUG>=1) {
										System.out.println("KV_DEBUG> Transport6205651, end false segment "+ProtocolUtils.outputHexString(getDsduReceived().getSegmentData()));
									}
                                    baos.write(getDsduReceived().getSegmentData());
                                    return;
                                    //connection.getDatalink6205641().respond();
                                }
                                else if (getDsduReceived().checkSgt() && getDsduReceived().lastSgt()) {
                                    if (DEBUG>=1){
                                    	System.out.println("KV_DEBUG> Transport6205651, end true segment "+ProtocolUtils.outputHexString(getDsduReceived().getSegmentData()));
                                    }
                                    
                                    baos.write(getDsduReceived().getSegmentData());
                                    tsduReceived = new TSDU();
                                    tsduReceived.setData(baos.toByteArray());
                                    baos.reset();
                                    
//                                    if (tsduReceived.getData().length > 1000) {
//                                        System.out.println("GOTCHA!");
//                                    }
                                    
                                    return; // tsduReceived;
                                }
                                else if (!getDsduReceived().checkSgt()) {
                                    connection.getDatalink6205641().abort(true);                                    
                                    state = STOPPED;
                                    throw new TransportAbortException("Transport6205651, stateMachine, ET-1F eror (TPDU incorrect. The only possible cause of this error is a TPDU type different from DT+)", 1);
                                }
                                
                                // If no bufferPool size available, throw new TransportAbortException ET_2F and abort the Datalink
                                // Not implemented. See 62056-51 page 17 for  more info
                                // return to STOPPED state
                                
                            } break; // DL_INDICATE
                            
                            case TR_ABORT: {
                                connection.getDatalink6205641().abort(true);                                    
                                state = STOPPED;
                                
                            } break; // TR_ABORT
                            
                            case DL_ABORT: {
                                throw new TransportAbortException("Transport6205651, stateMachine, Datalink abort eror, "+errorDescription, errorNr, true);
                            } // DL_ABORT
                            
                        } // switch(event) {
                        
                    } break; // IDLE
                    
                    case MSGT: {
                        if (bais.available() > getMaxPacketSize()) {
                            DSDU dsdu = new DSDU();
                            byte[] data = new byte[getMaxPacketSize()];
                            bais.read(data);
                            dsdu.init(isPriority(), false, getStsap(),getDtsap(), data);
                            connection.getDatalink6205641().request(dsdu);
                        }
                        else {
                            DSDU dsdu = new DSDU();
                            byte[] data = new byte[bais.available()];
                            bais.read(data);
                            dsdu.init(isPriority(), true, getStsap(),getDtsap(), data);
                            connection.getDatalink6205641().request(dsdu);
                            state = IDLE;
                            return;
                        }
                        
                    } break; // MSGT
                    
                } // switch(state)
            }
            catch(DatalinkAbortException e) {
                event = DL_ABORT;
                errorDescription = e.toString();
                errorNr = e.getErrorNr();
            }
            
            if (System.currentTimeMillis() - safetyTimeout > 0) {
                throw new ConnectionException("Transport6205641, stateMachine, Safety timeout",connection.getTIMEOUT_ERROR());
            } // if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0)
            
        } // while(true)       
    } // public TSDU stateMachine(int event) throws IOException {
    
    private void init(){
        setMaxPacketSize(connection.getDatalink6205641().getMAX_DSDU_SIZE()-2);
        baos = new ByteArrayOutputStream();
    }



    public int getStsap() {
        return stsap;
    }

    public void setStsap(int stsap) {
        this.stsap = stsap;
    }

    public int getDtsap() {
        return dtsap;
    }

    public void setDtsap(int dtsap) {
        this.dtsap = dtsap;
    }

    public TSDU getTsdu() {
        return tsdu;
    }

    public void setTsdu(TSDU tsdu) {
        this.tsdu = tsdu;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public boolean isStrong() {
        return strong;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }

    public DSDU getDsduReceived() {
        return dsduReceived;
    }

    public void setDsduReceived(DSDU dsduReceived) {
        this.dsduReceived = dsduReceived;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }


}
