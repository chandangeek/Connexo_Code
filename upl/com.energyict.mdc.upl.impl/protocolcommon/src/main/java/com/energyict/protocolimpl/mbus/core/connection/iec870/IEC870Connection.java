/*
 * IEC870Connection.java
 *
 * Created on 19 juni 2003, 11:20
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.mbus.core.ApplicationData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private int timeout;
    int retries;
    TimeZone timeZone=null;

    // frame variables
    int rtuAddress=0xFE;
    boolean fcb=false;


    IEC870Frame currentFrameRx=null;
    IEC870Frame currentFrameTx=null;

    /** Creates a new instance of IEC870Connection */
    public IEC870Connection(InputStream inputStream,
                            OutputStream outputStream,
                            int timeout,
                            int retries,
                            long lForceDelay,
                            int iEchoCancelling,
                            TimeZone timeZone) {
        super(inputStream,outputStream,lForceDelay,iEchoCancelling);
        this.inputStream = inputStream;
        this.retries = retries;
        this.timeZone = timeZone;
        this.timeout=timeout;

        currentFrameRx=null;
        currentFrameTx=null;


    } // public FlagIEC1107Connection(...)

    private void printFrame(IEC870Frame frame) {
        System.out.println(frame.toString(timeZone));
    }


    public int getRTUAddress() {
       return rtuAddress;
    }
    public void setRTUAddress(int rtuAddress) {
       this.rtuAddress=rtuAddress;
    }

    protected IEC870Frame sendFrame(int type,int function,int address, int length, boolean response) throws NestedIOException, ConnectionException {
        return sendFrame(type,function,address,length,null,response);
    }
    protected IEC870Frame sendFrame(int type,int function,int address, ApplicationData asdu, boolean response) throws NestedIOException, ConnectionException {
        return sendFrame(type,function,address,-1, asdu,response);
    }
    protected IEC870Frame sendFrame(int type,int function,int address,int length, ApplicationData asdu, boolean response) throws NestedIOException, ConnectionException {
    	return sendFrame(type,function,address,length,asdu,response,false);
    }
    protected IEC870Frame sendFrame(int type,int function,int address,int length, ApplicationData asdu, boolean response, boolean discoverResponse) throws NestedIOException, ConnectionException {
        int retry=0;
        while(true) {
            try {
            	if (discoverResponse) {
                    flushInputStream();
                }

                List asdus=null;
                currentFrameTx = new IEC870Frame(type,function,address,length,asdu);
                fcb = currentFrameTx.toggleFCB(fcb);
                if (DEBUG >= 1) {
                    printFrame(currentFrameTx);
                }
                sendRawData(currentFrameTx.getData());
                if (response) {
                    return waitForFrame(discoverResponse);
                }
                else {
                    if (waitForFrame(discoverResponse).getType() != IEC870Frame.FRAME_SINGLE_CHAR_E5) {
                        throw new IEC870ConnectionException("IEC870Connection, sendFrame, no ACK (E5) received");
                    }
                    return null;
                }
            }
            catch(IEC870ConnectionException e) {

            	if (discoverResponse) {
            		throw e;
            	}

                if (retry++ > (retries-1)) {
                    throw new IEC870ConnectionException("IEC870Connection, connectLink, max retries "+MAX_RETRIES_ERROR+" "+e.toString());
                }
                else {
                    if (e.getReason() == TIMEOUT_ERROR) {
                    	reSendCurrentFrameTx();
                    }
                }
            }
        }
    }

    private void reSendCurrentFrameTx() throws NestedIOException, IEC870ConnectionException {
        if (DEBUG >= 1) {
            printFrame(currentFrameTx);
        }
        try {
            sendRawData(currentFrameTx.getData());
        }
        catch(ConnectionException e) {
            throw new IEC870ConnectionException("IEC870Connection, sendCurrentFrame, ConnectionException, "+e.getMessage());
        }
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
//    private IEC870Frame waitForFrame() throws IEC870ConnectionException {
//    	return waitForFrame(false);
//    }

    final int DISCOVERY_TIMEOUT = 600;
    private IEC870Frame waitForFrame(boolean discoverResponse) throws IEC870ConnectionException {
        long lMSTimeoutInterFrame,lMSTimeoutDiscovery=0;
        int state=STATE_WAIT_FOR_START;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int kar,count=0,length=0,checksumreceived,checksumcalculated;
        bos.reset();
        boolean received=false;

        lMSTimeoutInterFrame = System.currentTimeMillis() + timeout;

    	if (discoverResponse) {
            lMSTimeoutDiscovery = System.currentTimeMillis() + DISCOVERY_TIMEOUT;
        }

    	//System.out.println("KV_DEBUG> 1 "+System.currentTimeMillis());

        copyEchoBuffer();
        int nrOfDevices=0;

        try {
            while(true) {
                if ((kar = readIn()) != -1) {

                	if (discoverResponse) {
                		lMSTimeoutDiscovery = System.currentTimeMillis() + DISCOVERY_TIMEOUT;
                		received=true;
                	}



                    if (DEBUG >= 2) {
                        ProtocolUtils.outputHex(kar);
                    }
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
                if (System.currentTimeMillis() - lMSTimeoutInterFrame > 0) {
                    throw new IEC870ConnectionException("IEC870Connection, waitForFrame, interframe timeout error",TIMEOUT_ERROR);
                }
                if ((lMSTimeoutDiscovery>0) && (System.currentTimeMillis() - lMSTimeoutDiscovery > 0)) {
                	if (received) {
                        throw new IEC870ConnectionException("IEC870Connection, waitForFrame, discoverytimeout but received data...", FRAMING_ERROR);
                    } else {
                		//System.out.println("KV_DEBUG> 2 "+System.currentTimeMillis());
                		throw new IEC870ConnectionException("IEC870Connection, waitForFrame, discoverytimeout timeout error",TIMEOUT_ERROR);
                	}
                }
            } // while(true)
        }
        catch(IEC870ConnectionException e) {
        	throw e;
        }
        catch (IOException e) {
            throw new IEC870ConnectionException("IEC870Connection, waitForFrame, "+e.getMessage());
        }

    } // public IEC870Frame waitForFrame() throws IEC870ConnectionException

    public void setTimeout(int timeout) {
        this.timeout=timeout;
    }
    public void setRetries(int retries) {
        this.retries=retries;
    }


} // public class IEC870Connection
