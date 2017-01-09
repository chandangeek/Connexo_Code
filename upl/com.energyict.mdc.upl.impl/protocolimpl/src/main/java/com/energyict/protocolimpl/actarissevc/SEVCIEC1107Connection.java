package com.energyict.protocolimpl.actarissevc;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the IEC1107 datalink layer protocol.
 * <B>Changes :</B><BR>
 *      KV 27092002 Initial version.<BR>
 *      KV 02022004 changed for HHU
 */
public class SEVCIEC1107Connection {

    private static final byte DEBUG=0;

    private int iMaxRetries;

    // General attributes
    private OutputStream outputStream;
    private InputStream inputStream;


    private static final byte SOH=0x01;
    private static final byte STX=0x02;
    private static final byte ETX=0x03;
    private static final byte EOT=0x04;
    private static final byte ACK=0x06;
    private static final byte NAK=0x15;

    // specific IEC1107
    private boolean boolIEC1107Connected;

    private static final int LENGTH_OFFSET=1;
    // Raw frames
    private static final int MAX_BUFFER_SIZE=256;
    int forcedDelay;

    /**
     * Class constructor.
     * @param inputStream InputStream for the active connection
     * @param outputStream OutputStream for the active connection
     * @param iTimeout Time in ms. for a request to wait for a response before returning an timeout error.
     * @exception SEVCIEC1107ConnectionException
     */
    public SEVCIEC1107Connection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, int forcedDelay) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        boolIEC1107Connected=false;
        this.iMaxRetries = iMaxRetries;
        this.forcedDelay=forcedDelay;

    } // public IEC1107Connection(...)




    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     */
    public void disconnectMAC() throws SEVCIEC1107ConnectionException {
        if (boolIEC1107Connected) {
            sendBreak();
            boolIEC1107Connected=false;
        } // if (boolIEC1107Connected==true)
    } // public void disconnectMAC() throws SEVCIEC1107ConnectionException


    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     */
    private void sendBreak() throws SEVCIEC1107ConnectionException {
        try {
            byte[] buffer = {SOH,(byte)0x42,(byte)0x30, ETX,(byte)0x21,(byte)0x11};
            sendRawData(buffer);
            return;
        }
        catch(SEVCIEC1107ConnectionException e) {
            flushInputStream();
            throw new SEVCIEC1107ConnectionException("sendbreak() error, "+e.getMessage());
        }
    } // public void disconnectMAC() throws SEVCIEC1107ConnectionException



    /**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    public void connectMAC(String strIdent, String strPass, String meterID) throws NestedIOException,SEVCIEC1107ConnectionException { // KV 13082003
        if (!boolIEC1107Connected) {
            try {

                // KV 02022004
                if (hhuSignOn == null) {
                    wakeUp();
                    signOn(strIdent,strPass,meterID); // KV 13082003
                }
                else {
                    hhuSignOn(strIdent,strPass,meterID);
                }

                boolIEC1107Connected=true;
            }
            catch (IOException e) {
                throw new NestedIOException(e);
            }
            catch(SEVCIEC1107ConnectionException e) {
                throw new SEVCIEC1107ConnectionException("doConnectMAC() error "+e.getMessage());
            }
        }

    }

    private void doConnectMAC(String strIdent, String strPass) {

    }

    public void wakeUp()  throws NestedIOException,SEVCIEC1107ConnectionException {
        for (int i =0;i<3;i++) {
            try {
                sendWakeUpData();
                receiveWakeup();
                // KV 12022004
                try {
                    Thread.sleep(2000);
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
                return;
            }
            catch(SEVCIEC1107ConnectionException e) {
                flushInputStream();
                delay(5000);
            }

        } // for (int i =0;i<3;i++)

        throw new SEVCIEC1107ConnectionException("wakeUp() max retry error ");

    } // public void wakeUp()  throws SEVCIEC1107ConnectionException

    private void sendACKExt(char Z) throws SEVCIEC1107ConnectionException {
        byte[] ack={(byte)0x06,(byte)0x30,(byte)Z,(byte)0x37,(byte)0x0D,(byte)0x0A}; // KV 13082003
        sendRawData(ack);
    }
    private void sendACK() throws SEVCIEC1107ConnectionException {
        byte[] ack={(byte)0x06};
        sendRawData(ack);
    }

    private void hhuSignOn(String strIdentConfig, String strPass, String meterID) throws IOException, SEVCIEC1107ConnectionException { // KV 13082003
        int iRetries=0;
        while(true) {
            try {
                hhuSignOn.signOn(strIdentConfig,meterID,true,2); // 1200 = 2
                authenticate(strPass);
                return;
            }
            catch (SEVCIEC1107ConnectionException e) {
                if (e.isReasonTimeout()) {
                    if (iRetries++ >=iMaxRetries) {
                        throw new SEVCIEC1107ConnectionException("signOn() error, max retries");
                    }
                }
                else {
                    throw new SEVCIEC1107ConnectionException("signOn() error " + e.getMessage());
                }
            }

        } // while(true)

    } // private signOn() throws SEVCIEC1107ConnectionException


    private void signOn(String strIdentConfig, String strPass, String meterID) throws NestedIOException,SEVCIEC1107ConnectionException { // KV 13082003
        int iRetries=0;
        while(true) {
            try {
                String str="/?"+meterID+"!\r\n"; // KV 13082003
                sendRawData(str.getBytes());
                String strIdent = receiveIdent(strIdentConfig);
                sendACKExt(strIdent.charAt(4)); // pass Z! KV 13082003
                authenticate(strPass);
                return;
            }
            catch (SEVCIEC1107ConnectionException e) {
                if (e.isReasonTimeout()) {
                    if (iRetries++ >=iMaxRetries) {
                        throw new SEVCIEC1107ConnectionException("signOn() error, max retries");
                    }
                }
                else {
                    throw new SEVCIEC1107ConnectionException("signOn() error " + e.getMessage());
                }
            }

        } // while(true)

    } // private signOn() throws SEVCIEC1107ConnectionException

    private void authenticate(String strPass) throws NestedIOException,SEVCIEC1107ConnectionException {
        receivePassword(strPass);
        byte[] txbuffer = new byte[strPass.getBytes().length+2];
        for (int i=0;i<strPass.getBytes().length;i++) {
            txbuffer[i] = strPass.getBytes()[i];
        }
        byte[] crc=calcCRC(txbuffer);
        txbuffer[txbuffer.length-1] = crc[0];
        txbuffer[txbuffer.length-2] = crc[1];
        sendRawData(txbuffer);
        receiveACK();
        return;
    } // private void authenticate(String strPass)


    public void sendReadFrame(byte bIdentifier) throws SEVCIEC1107ConnectionException {
        int iLength;
        byte[] data=new byte[5];
        byte[] crc;

        // KV 27022006
        if (forcedDelay>0) {
           delay(forcedDelay);
           flushInputStream();
        }

        try {
            data[0] = SOH;
            data[1] = bIdentifier;
            data[2] = ETX;
            crc = calcCRC(data);
            data[data.length-1] = crc[0];
            data[data.length-2] = crc[1];
            outputStream.write(data,0,data.length);

            if (DEBUG==1) {
                int i;
                for (i=0;i<data.length;i++) {
                    ProtocolUtils.outputHex(((int) data[i]) & 0x000000FF);
                }
                System.out.println();
            }
        }
        catch (IOException e) {
            throw new SEVCIEC1107ConnectionException("sendReadFrame() error "+e.getMessage());
        }

    } // private void sendReadFrame(byte bIdentifier) throws SEVCIEC1107ConnectionException


    public void sendWriteFrame(byte bIdentifier,byte[] data) throws SEVCIEC1107ConnectionException {
        int iLength;
        int iRetries=0;
        byte[] txbuffer;

        // KV 27022006
        if (forcedDelay>0) {
           delay(forcedDelay);
           flushInputStream();
        }

        if (data != null) {
            txbuffer = new byte[5 + data.length + 1];
        } else {
            txbuffer = new byte[5];
        }

        byte[] crc;

        while(true) {
            try {
                txbuffer[0] = SOH;
                txbuffer[1] = bIdentifier;
                if (data != null) {
                    txbuffer[2] = (byte)data.length;
                    for (int i=0;i<data.length;i++) {
                        txbuffer[i + 3] = data[i];
                    }
                    txbuffer[3+data.length] = ETX;
                    crc = calcCRC(txbuffer);
                    txbuffer[txbuffer.length-1] = crc[0];
                    txbuffer[txbuffer.length-2] = crc[1];
                    outputStream.write(txbuffer,0,txbuffer.length);
                }
                else {
                    txbuffer[2] = ETX;
                    crc = calcCRC(txbuffer);
                    txbuffer[txbuffer.length-1] = crc[0];
                    txbuffer[txbuffer.length-2] = crc[1];
                    outputStream.write(txbuffer,0,txbuffer.length);
                }

                receiveACK();

                if (DEBUG==1) {
                    int i;
                    for (i=0;i<txbuffer.length;i++) {
                        ProtocolUtils.outputHex(((int) txbuffer[i]) & 0x000000FF);
                    }
                    System.out.println();
                }

                return;
            }
            catch (SEVCIEC1107ConnectionException e) {
                if (e.isReasonTimeout()) {
                    if (iRetries++ >= iMaxRetries) {
                        throw new SEVCIEC1107ConnectionException("sendWriteFrame() error, max retries, " + e.getMessage());
                    }
                }
                else {
                    throw new SEVCIEC1107ConnectionException("sendWriteFrame() error " + e.getMessage());
                }
            }
            catch (IOException e) {
                throw new SEVCIEC1107ConnectionException("sendWriteFrame() error "+e.getMessage());
            }

        } // while(true)

    } // public void sendWriteFrame(byte bIdentifier,byte[] data) throws SEVCIEC1107ConnectionException

    private byte[] calcCRC(byte[] data,int iLength) {
        return (doCalcCRC(data,iLength));
    }
    private byte[] calcCRC(byte[] data) {
        return (doCalcCRC(data,data.length));
    }
    private byte[] doCalcCRC(byte[] data,int iLength) {
        int CRC=0;
        byte[] crc = new byte[2];
        int a=0,b=0,Counter=0;
        for (int i=0;i<iLength-2;i++) {
            a=CRC/256;
            a ^= ((int)data[i]&0xff);
            a *=256;
            b=CRC & 0x000000FF;
            CRC = (a | b) & 0xFFFF;
            Counter=0;

            do {
                if ((CRC & 0x8000) != 0) {
                    CRC *= 2;
                    CRC ^= 0x8005;
                }
                else {
                    CRC *= 2;
                }
                Counter++;
            } while(Counter < 8);
        }

        crc[1] = (byte)(CRC);
        crc[0] = (byte)(CRC>>8);
        return crc;

    } // private byte[] doCalcCRC(byte[] data,int iLength)

    /**
     * Method to send an array of bytes via outputstream.
     * @param byteBuffer Byte array to send.
     */
    private void sendRawData(byte[] byteBuffer) throws SEVCIEC1107ConnectionException {
        try {
            // KV 27022006
            if (forcedDelay>0) {
               delay(forcedDelay);
               flushInputStream();
            }

            outputStream.write(byteBuffer);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("sendRawData() error "+e.getMessage());
        }

    } // public void sendRawData(byte[] byteBuffer)

    public void flushInputStream()  throws SEVCIEC1107ConnectionException {
        try {
            while(inputStream.available() != 0) {
                inputStream.read(); // flush inputbuffer
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("flushInputStream() error "+e.getMessage());
        }
    } // private void flushInputStream()  throws SEVCIEC1107ConnectionException


    public byte[] receiveSegmentedData(int size) throws NestedIOException,SEVCIEC1107ConnectionException {
        int count=0;
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        byte[] data;
        while(true) {
            data = receiveData();
            try {
                bytestream.write(data);
            }
            catch(IOException e) {
                throw new SEVCIEC1107ConnectionException("IEC1107Connection, receiveSegmentedData, IOException, "+e.getMessage());
            }
            count = count + data.length;
            sendACK();
            if (count>=size) {
                break;
            }
            //sendACK();
        }

        return (bytestream.toByteArray());
    }

    private static final byte STATE_WAIT_FOR_SOH=0;
    private static final byte STATE_WAIT_FOR_LENGTH=1;
    private static final byte STATE_WAIT_FOR_DATA=2;
    private static final byte STATE_WAIT_FOR_ETX=3;
    private static final byte STATE_WAIT_FOR_CRC=4;

    public byte[] receiveData() throws NestedIOException,SEVCIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar;
        int iState=STATE_WAIT_FOR_SOH;
        int iLength=0,iCount=0;
        byte[] receiveBuffer=null;
        byte[] calculatedCRC;

        iState=STATE_WAIT_FOR_SOH;
        lMSTimeout = System.currentTimeMillis() + 5000;
        try {
            while(true) {
                if (inputStream.available() != 0) {
                    iNewKar = inputStream.read();

                    switch(iState) {
                        case STATE_WAIT_FOR_SOH: {
                            if ((byte)iNewKar == SOH) {
                                iState = STATE_WAIT_FOR_LENGTH;
                            }
                        } break; // STATE_WAIT_FOR_SOH

                        case STATE_WAIT_FOR_LENGTH: {
                            iLength = iNewKar &0xff;
                            receiveBuffer= new byte[iLength+5];
                            receiveBuffer[0]=SOH;
                            receiveBuffer[1]=(byte)iLength;
                            iCount = 0;
                            iState = STATE_WAIT_FOR_DATA;

                        } break; // STATE_WAIT_FOR_LENGTH

                        case STATE_WAIT_FOR_DATA: {
                            receiveBuffer[iCount+2] = (byte)iNewKar;
                            if (iCount++ >= (iLength-1)) {
                                iState = STATE_WAIT_FOR_ETX;
                            }

                        } break; // STATE_WAIT_FOR_DATA

                        case STATE_WAIT_FOR_ETX: {
                            if ((byte)iNewKar == ETX) {
                                iState = STATE_WAIT_FOR_LENGTH;
                                receiveBuffer[iCount+2] = (byte)iNewKar;
                                if (iCount++ >= ((iLength-1)+1)) {
                                    iState = STATE_WAIT_FOR_CRC;
                                }

                            }
                            else {
                                throw new SEVCIEC1107ConnectionException("receiveData() should receive ETX!");
                            }

                        } break; // STATE_WAIT_FOR_ETX

                        case STATE_WAIT_FOR_CRC: {
                            receiveBuffer[iCount+2] = (byte)iNewKar;
                            if (iCount++ >= ((iLength-1)+3)) {
                                calculatedCRC = calcCRC(receiveBuffer);
                                if ((calculatedCRC[0] == receiveBuffer[iLength+4]) &&
                                (calculatedCRC[1] == receiveBuffer[iLength+3])) {
                                    // remove head and tail from receivebuffer...
                                    byte[] data = new byte[iLength];
                                    for (int i=0;i<iLength;i++) {
                                        data[i] = receiveBuffer[i + 2];
                                    }
                                    return data;
                                }
                                else {
                                    throw new SEVCIEC1107ConnectionException("receiveData() bad CRC error");
                                }
                            }

                        } break; // STATE_WAIT_FOR_CRC

                    } // switch(iState)

                } // if (inputStream.available() != 0)
                else {
                    Thread.sleep(100);
                }

                if (System.currentTimeMillis() - lMSTimeout > 0) {
                    SEVCIEC1107ConnectionException e = new SEVCIEC1107ConnectionException("receiveData() timeout error");
                    e.setReasonTimeout();
                    throw e;
                }

            } // while(true)

        } // try
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("receiveData() error "+e.getMessage());
        }

    } // public byte[] receiveData(String str) throws SEVCIEC1107ConnectionException


    private static final int WAIT_FOR_IDENT=0;
    private static final int WAIT_FOR_COMPLETION=1;


    public String receiveIdent(String str) throws NestedIOException,SEVCIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar;
        String strIdent= "";
        String convertstr;
        byte[] convert=new byte[1];
        lMSTimeout = System.currentTimeMillis() + 10000;
        try {
            while(true) {
                if (inputStream.available() != 0) {
                    iNewKar = inputStream.read();

                    if ((byte)iNewKar==NAK) {
                        sendBreak();
                    }

                    convert[0] = (byte)iNewKar;
                    convertstr = new String(convert);
                    if ((byte)iNewKar >= 0x20)  // no control characters...
                    {
                        strIdent += convertstr;
                    }

                    if (convertstr.compareTo("\\") == 0) {
                        strIdent += convertstr;
                    }

                    if ((byte)iNewKar == 0x0A) {
                        if ((str != null) && ("".compareTo(str) != 0)) {
                            if (strIdent.compareTo(str) == 0) {
                                return strIdent; // KV 16122003
                            }
                            throw new SEVCIEC1107ConnectionException("receiveIdent() device id mismatch! meter: "+strIdent+", configured: "+str);
                        }
                        return strIdent; // KV 16122003
                    }

                } // if (inputStream.available() != 0)
                else {
                    Thread.sleep(100);
                }

                if (System.currentTimeMillis() - lMSTimeout > 0) {
                    throw SEVCIEC1107ConnectionException.getSEVCIEC1107ConnectionExceptionTimeout("receiveIdent() timeout error");
                }

            } // while(true)

        } // try
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("receiveIdent() error "+e.getMessage());
        }

    } // public void receiveIdent(String str) throws SEVCIEC1107ConnectionException



    public void receiveACK() throws NestedIOException,SEVCIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar;
        lMSTimeout = System.currentTimeMillis() + 3000;
        try {
            while(true) {
                if (inputStream.available() != 0) {
                    iNewKar = inputStream.read();
                    if ((byte)iNewKar == 0x06) {
                        return;
                    }
                } // if (inputStream.available() != 0)
                else {
                    Thread.sleep(100);
                }

                if (System.currentTimeMillis() - lMSTimeout > 0) {
                    SEVCIEC1107ConnectionException e = new SEVCIEC1107ConnectionException("receiveACK() timeout error");
                    e.setReasonTimeout();
                    throw e;
                }

            } // while(true)

        } // try
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("receiveACK() error "+e.getMessage());
        }

    } // public void receiveACK() throws SEVCIEC1107ConnectionException

    public void receivePassword(String str) throws NestedIOException,SEVCIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar,iState,iCount;
        String strIdent= "";
        byte[] convert=new byte[1];
        byte[] receivedCRC=new byte[2];
        byte[] calculatedCRC;

        iState = 0;
        iCount=1;
        lMSTimeout = System.currentTimeMillis() + 5000;
        try {
            while(true) {
                if (inputStream.available() != 0) {
                    iNewKar = inputStream.read();
                    if (iState==0) {
                        convert[0] = (byte)iNewKar;
                        strIdent += new String(convert);
                        if (strIdent.compareTo(str) == 0) {
                            iState = 1;
                        }
                    }
                    else if (iState == 1) {
                        receivedCRC[iCount--] = (byte)iNewKar;
                        if (iCount < 0) {
                            strIdent += new String("");
                            strIdent += new String("");
                            calculatedCRC = calcCRC(strIdent.getBytes(),strIdent.getBytes().length+2);

                            if ((calculatedCRC[0] == receivedCRC[0]) &&
                            (calculatedCRC[1] == receivedCRC[1])) {
                                return;
                            } else {
                                throw new SEVCIEC1107ConnectionException("receivePassword() bad CRC error");
                            }
                        } // if (iCount < 0)
                    } // else if (iState == 1)
                } // if (inputStream.available() != 0)
                else {
                    Thread.sleep(100);
                }

                if (System.currentTimeMillis() - lMSTimeout > 0) {
                    throw SEVCIEC1107ConnectionException.getSEVCIEC1107ConnectionExceptionTimeout("receivePassword() timeout error");
                }

            } // while(true)

        } // try
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("receivePassword() error "+e.getMessage());
        }

    } // public void receivePassword(String str) throws SEVCIEC1107ConnectionException

    public void receiveWakeup() throws NestedIOException,SEVCIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar;
        short sRXCount=0;

        lMSTimeout = System.currentTimeMillis() + 5000; // KV 12022004
        sRXCount=0;
        try {
            while(true) {
                if (inputStream.available() != 0) {
                    iNewKar = inputStream.read();

                    if ((byte)iNewKar == 0) {
                        sRXCount++;
                        if (sRXCount == 3) {
                            return;
                        }
                    }
                    else {
                        throw new SEVCIEC1107ConnectionException("receiveWakeup() wrong kar error");
                    }

                } // if (inputStream.available() != 0)
                else {
                    Thread.sleep(100);
                }

                if (System.currentTimeMillis() - lMSTimeout > 0) {
                    throw SEVCIEC1107ConnectionException.getSEVCIEC1107ConnectionExceptionTimeout("receiveWakeup() timeout error");
                }

            } // while(true)

        } // try
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SEVCIEC1107ConnectionException("receiveWakeup() error "+e.getMessage());
        }
    } // public void receiveWakeup() throws SEVCIEC1107ConnectionException

    private void sendWakeUpData() throws NestedIOException {
        //        byte[] data = {(byte)0};//,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0};
        byte[] data = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0};
        try {
            for (int i=0;i<100;i++) {
                outputStream.write(data);
                delay(1);
            }
        }
        catch (IOException e) {
            throw new NestedIOException(e,"sendWakeUpData() error "+e.getMessage());
        }

    } // public void sendWakeUpData() throws SEVCIEC1107ConnectionException

    public void delay(long lDelay) {
        long lMSTimeout;
        lMSTimeout = System.currentTimeMillis() + lDelay;
        while(true) {
            if (System.currentTimeMillis() - lMSTimeout > 0) {
                return;
            }
        }
    }

    // KV 02022004
    HHUSignOn hhuSignOn=null;
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        this.hhuSignOn=hhuSignOn;
    }
    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }

} // public class IEC1107Connection {
