/*
 * Connection.java
 *
 * Created on 1 juli 2003, 17:03
 */

package com.energyict.protocolimpl.osiframework;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import serialio.xmodemapi.XGet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author  Koen
 */
public class PhysicalLayerConnection {

    private final byte DEBUG=0;

    protected final byte UNKNOWN_ERROR=-1;
    protected final byte TIMEOUT_ERROR=-2;
    protected final byte CRC_ERROR=-3;
    protected final byte FRAMING_ERROR=-4;
    protected final byte MAX_RETRIES_ERROR=-5;
    protected final byte FRAME_ERROR=-6;
    protected final byte PROTOCOL_ERROR=-7;
    protected final byte NAK_RECEIVED=-8;


    // First 32 control characters of the ASCII SET
    String[] controlCharacters={"NUL","SOH","STX","ETX","EOT","ENQ","ACK","BEL","BS" ,"HT","LF" ,"VT" ,"FF","CR","SO","SI",
                                "DLE","DC1","DC2","DC3","DC4","NAK","SYN","ETB","CAN","EM","SUB","ESC","FS","GS","RS","US"};
    static public final byte NUL=0x00;
    static public final byte SOH=0x01;
    static public final byte STX=0x02;
    static public final byte ETX=0x03;
    static public final byte EOT=0x04;
    static public final byte ENQ=0x05;
    static public final byte ACK=0x06;
    static public final byte BEL=0x07;
    static public final byte BS=0x08;
    static public final byte HT=0x09;
    static public final byte LF=0x0A;
    static public final byte VT=0x0B;
    static public final byte FF=0x0C;
    static public final byte CR=0x0D;
    static public final byte SO=0x0E;
    static public final byte SI=0x0F;
    static public final byte DLE=0x10;
    static public final byte DC1=0x11;
    static public final byte DC2=0x12;
    static public final byte DC3=0x13;
    static public final byte DC4=0x14;
    static public final byte NAK=0x15;
    static public final byte SYN=0x16;
    static public final byte ETB=0x17;
    static public final byte CAN=0x18;
    static public final byte EM=0x19;
    static public final byte SUB=0x1A;
    static public final byte ESC=0x1B;
    static public final byte FS=0x1C;
    static public final byte GS=0x1D;
    static public final byte RS=0x1E;
    static public final byte US=0x1F;


    private OutputStream outputStream;
    private InputStream inputStream;
    long lForceDelay;
    int iEchoCancelling;

    ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
    ByteArrayInputStream echoByteArrayInputStream;
    HalfDuplexController halfDuplexController=null;
    StateMachineCallBack stateMachineCallBack;

    protected PhysicalLayerConnection(InputStream inputStream,
                         OutputStream outputStream,
                         long lForceDelay,
                         int iEchoCancelling,
                         HalfDuplexController halfDuplexController,
                         StateMachineCallBack stateMachineCallBack) throws ConnectionException {

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.lForceDelay = lForceDelay;
        this.iEchoCancelling = iEchoCancelling;
        this.halfDuplexController = halfDuplexController;
        this.stateMachineCallBack = stateMachineCallBack;
    }



    public void sendOutTerminalMode(byte[] txbuffer, boolean waitForEcho) throws NestedIOException, ConnectionException {
        if (waitForEcho) {
            for (int i=0;i<txbuffer.length;i++) {
               doSendOut(txbuffer[i]);
               waitForEcho(txbuffer[i]);
            }
        }
        else {
            for (int i=0;i<txbuffer.length;i++) {
               delay(lForceDelay);
               doSendOut(txbuffer[i]);
            }
        }
    }

    private void waitForEcho(int echo) throws NestedIOException,ConnectionException {
        long echoTimeout = System.currentTimeMillis() + 5000;
        int kar=0;
        while(true) {
            if ((kar = readIn()) != -1) {
                if (kar != echo)
                    throw new ConnectionException("Connection, waitForEcho(), wrong character echo received! ("+(char)echo+"!="+(char)kar+")");
                else break;
            }

            if (System.currentTimeMillis() - echoTimeout > 0) {
                throw new ConnectionException("Connection, waitForEcho(), timeout waiting for character echo!",TIMEOUT_ERROR);
            }
        } // while(true)
    }

    public void waitForEmptyBuffer(long delay) throws NestedIOException,ConnectionException {
        long emptyBufferTimeout = System.currentTimeMillis() + delay;
        int kar=0;
        while(true) {
            if ((kar = readIn()) != -1) {
                emptyBufferTimeout = System.currentTimeMillis() + delay;
            }
            if (System.currentTimeMillis() - emptyBufferTimeout > 0) {
                break;
            }
        } // while(true)
    }

    public byte[] getXmodemProtocolData() throws IOException {
        XGet xget = new XGet(outputStream,inputStream);
//System.out.println("KV_DEBUG> changed xmodem settings... XMODEM should timeout after 10 seconds???? followingg the doc???") ;
        return xget.getBinaryData(10); //3);
        //return xget.get('b', true,true,false, 10, 10); //(10 sec timeout and 10 retries)
        //return xget.get('b',true,true,false,0, 10);
    }

    /* Send 1 byte
     * @param byte to send
     */
    public void sendOut(byte txbyte) throws ConnectionException {
        byte[] txbuffer = new byte[1];
        txbuffer[0]=txbyte;
        doSendOut(txbuffer,0,1);
    }

    /* Send byte array
     * @param byte array to send
     */
    public void sendOut(byte[] txbuffer) throws ConnectionException {
        doSendOut(txbuffer,0,txbuffer.length);
    }

    /* Send byte array
     * @param byte array to send
     */
    protected void sendOut(byte[] txbuffer, int offset, int length) throws ConnectionException {
        doSendOut(txbuffer,offset,length);
    }

    private void doSendOut(byte txbuffer)  throws ConnectionException {
        try {
            if (iEchoCancelling!=0) echoByteArrayOutputStream.write(txbuffer);
            if (getHalfDuplexController() != null)
                getHalfDuplexController().request2Send(1);
            outputStream.write(txbuffer);
            if (getHalfDuplexController() != null)
                getHalfDuplexController().request2Receive(1);
        }
        catch (IOException e) {
            throw new ConnectionException("Connection, doSendOut() error "+e.getMessage());
        }
    } // private void doSendOut(byte txbuffer)

    private void doSendOut(byte[] txbuffer, int offset, int length)  throws ConnectionException {
        try {
            if (iEchoCancelling!=0) echoByteArrayOutputStream.write(txbuffer);
            if (getHalfDuplexController() != null)
                getHalfDuplexController().request2Send(length);
            outputStream.write(txbuffer,offset,length);
            if (getHalfDuplexController() != null)
                getHalfDuplexController().request2Receive(length);
        }
        catch (IOException e) {
            throw new ConnectionException("Connection, doSendOut() error "+e.getMessage());
        }
    } // private void doSendOut(byte[] txbuffer)

    private void delayAndCallBack() throws IOException {
        int count=0;
        while(true) {
            int retval = stateMachineCallBack.receiving();
            try {
               Thread.sleep(10);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
            if (count++ >= 9) break;
        }
    }

    /*
     * Get next charakter from stream if exist.
     * @return next karakter or -1 if none
     */
    protected int readIn() throws NestedIOException, ConnectionException {
        try {
            int iNewKar;

            if (inputStream.available() != 0) {
                iNewKar = inputStream.read();
                if (iNewKar != echoByteArrayInputStream.read()) {
                    return iNewKar;
                }
            } // if (inputStream.available() != 0)
            else {
                delayAndCallBack();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionException("Connection, readIn() error "+e.getMessage());
        }
        return(-1);
    } // private int readIn() throws ConnectionException

    protected byte[] readInArray() throws NestedIOException, ConnectionException {
        try {
            byte[] data;
            int len;
            if ((len = inputStream.available()) != 0) {
                data = new byte[len];
                inputStream.read(data,0,len);
                for (int i=0;i<len;i++) {
                   if (data[i] != (byte)echoByteArrayInputStream.read()) {
                      return ProtocolUtils.getSubArray(data,i);
                   }
                }
            } // if (inputStream.available() != 0)
            else {
                delayAndCallBack();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionException("Connection, readIn() error "+e.getMessage());
        }
        return(null);
    } // private int readInArray() throws ConnectionException

    protected void delay(long lDelay) throws NestedIOException {
        try {
            Thread.sleep(lDelay);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }
    /*
     *  flush the echo output buffer. This must be done before a frame send.
     */
    protected void flushEchoBuffer() {
        echoByteArrayOutputStream.reset();
    }
    /*
     * Copy echou output buffer to input. This is used to compare with the received characters
     * on the input stream. Invoke this method before receiving a frame after a frame send.
     */
    protected void copyEchoBuffer() {
        echoByteArrayInputStream = new ByteArrayInputStream(echoByteArrayOutputStream.toByteArray());
    }


    protected void delayAndFlush(long delay)  throws ConnectionException,NestedIOException {
        delay(delay);
        flushInputStream();
    }
    /*
     * Flush all waiting characters ikn the inputstream.
     */
    protected void flushInputStream()  throws ConnectionException {
        try {
            while(inputStream.available() != 0) inputStream.read(); // flush inputbuffer
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionException("Connection, flushInputStream() error "+e.getMessage());
        }
    } // private void flushInputStream()  throws ConnectionException

    /**
     * Method to send an array of bytes via outputstream. It flushes the echo output buffer before send.
     * @param byteBuffer Byte array to send.
     */
    protected void sendRawDataNoDelay(byte[] txbuffer) throws ConnectionException {
        flushEchoBuffer();
        sendOut(txbuffer);
    } // public void sendRawData(byte[] byteBuffer)

    protected void sendRawDataNoDelayTerminalMode(byte[] txbuffer, boolean waitForEcho) throws NestedIOException, ConnectionException {
        flushEchoBuffer();
        sendOutTerminalMode(txbuffer,waitForEcho);
    } // public void sendRawDataNoDelayTerminalMode(byte[] byteBuffer)

    /**
     * Method to send an array of bytes via outputstream. It flushes the echo output buffer before send.
     * @param byteBuffer Byte array to send.
     */
    protected void sendRawData(byte[] txbuffer) throws NestedIOException,ConnectionException {
        delay(lForceDelay);
        flushEchoBuffer();
        sendOut(txbuffer);
    } // public void sendRawData(byte[] byteBuffer)

    /**
     * Method to send a byte via outputstream. It flushes the echo output buffer before send.
     * @param byte to send.
     */
    protected void sendRawData(byte txbuffer) throws NestedIOException,ConnectionException {
        delay(lForceDelay);
        flushEchoBuffer();
        sendOut(txbuffer);
    } // public void sendRawData(byte[] byteBuffer)

    /*
     *  Calculate modulo 256 checksum.
     *  @param data byte array to calculate checksum on
     *  @param length nr of bytes of data to calculate checksum
     *  @param offset offset in byte array to calculate checksum
     */
    protected byte calcChecksum(byte[] data,int length, int offset) throws ConnectionException {
        return (doCalcChecksum(data,length,offset));
    }
    /*
     *  Calculate modulo 256 checksum.
     *  @param data byte array to calculate checksum on
     *  @param length nr of bytes of data to calculate checksum
     */
    protected byte calcChecksum(byte[] data,int length) throws ConnectionException {
        return (doCalcChecksum(data,length,0));
    }
    /*
     *  Calculate modulo 256 checksum.
     *  @param data byte array to calculate checksum on
     */
    protected byte calcChecksum(byte[] data) throws ConnectionException {
        return (doCalcChecksum(data,data.length,0));
    }
    private byte doCalcChecksum(byte[] data, int length, int offset) throws ConnectionException {
        int checksum=0;
        int a=0,b=0,Counter=0;
        if (length > (data.length - offset)) throw new ConnectionException("Connection, doCalcChecksum, datalength="+data.length+", length="+length+", offset="+offset);
        for (int i=0;i<length-1;i++) {
            checksum ^= ((int)data[offset+i]&0xff);
        }
        return (byte)checksum;
    }

    /**
     * Getter for property halfDuplexController.
     * @return Value of property halfDuplexController.
     */
    public com.energyict.dialer.core.HalfDuplexController getHalfDuplexController() {
        return halfDuplexController;
    }

    /**
     * Setter for property halfDuplexController.
     * @param halfDuplexController New value of property halfDuplexController.
     */
    public void setHalfDuplexController(com.energyict.dialer.core.HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
    }

 // private byte doCalcChecksum(byte[] data,int length)

} // abstract public class Connection
