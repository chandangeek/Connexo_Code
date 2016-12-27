/*
 * SiemensSCTM.java
 *
 * Created on 24 januari 2003, 14:44
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the SCTM datalink layer protocol.
 * <B>Changes :</B><BR>
 *      KV 04022003 Initial version.<BR>
 *      KV 17032004 Add HalfDuplex support
 *      KV 24022005 Changed forceDelay(...) method
 *      KV 08032005 remove forceDelay(...) anf add delay(...) method
 */
public class SiemensSCTM {

    private static final byte DEBUG=0;

    ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
    ByteArrayInputStream echoByteArrayInputStream;

    private static final byte UNKNOWN_ERROR=-1;
    private static final byte TIMEOUT_ERROR=-2;
    private static final byte MAX_RETRIES=-3;

    private int iMaxRetries;

    // General attributes
    private OutputStream outputStream;
    private InputStream inputStream;
    private int iProtocolTimeout;
    private String strPass=null;
    private String nodeId=null;
    private int iEchoCancelling;
    private HalfDuplexController halfDuplexController=null;

    private int DBN;

    private static final byte SOH=0x01;
    private static final byte STX=0x02;
    private static final byte ETX=0x03;
    private static final byte ACK=0x06;
    private static final byte NAK=0x15;

    // SCTM frame offsets
    protected int SCTM_HEADER_HCC;
    protected int SCTM_HEADER_STATUS;
    protected int SCTM_DATABLOCK_OFFSET;
    protected int SCTM_HEADER_OFFSET;
    protected int SCTM_HEADER_LENGTH;
    protected int SCTM_HEADER_LENGTH_SIZE;
    protected int SCTM_HEADER_DBN;
    protected int SCTM_HEADER_AKN;
    protected int SCTM_HEADER_ID;
    protected int SCTM_HEADER_ID_SIZE;

    // SCTM frame sizes
    protected int SCTM_HEADER_SIZE;

    // SCTM header frame status byte
    protected final byte CENTRAL_STATION_TELEGRAMM=1;
    protected final byte REMOTE_STATION_TELEGRAMM=0;
    protected final byte CONTINUE_BIT=2;

    // specific IEC1107
    private boolean boolSCTMConnected;

    byte[] iec1107Dump;

    // Raw frames
    private static final int MAX_BUFFER_SIZE=256;

    SiemensSCTMFrameDecoder lastSCTMFrame=null;

    public static final byte[] TABENQ1={'E','1'};
    public static final byte[] TABENQ3={'E','3'};
    public static final byte[] BUFENQ1={'E','4'};
    public static final byte[] BUFENQ2={'E','6'};
    public static final byte[] NEXT={'E','5'};
    public static final byte[] SETTIME={'T','1'};
    public static final byte[] SSYNC={'T','2'};
    public static final byte[] MSYNC={'T','4'};

    public static final byte[] PASSWORD={'P','1'};

    public static final byte[] DATETIME={'0','1'};

    //public static final byte[] CLEARINGDATA={'1','1'}; //  KV changed to use multiple buffers...
    public static final byte[] PERIODICBUFFERS={'0','1'};
    public static final byte[] SPONTANEOUSBUFFERS={'5','1'};
    //public static final byte[] LOADPROFILEBUFFERSTRUCTURE={'2','1'};

    public static void main(String[] args) {
    	try {
    		byte[] data = new byte[]{1,1,30,30,35,34,36,30,38,38,30,31,39,(byte)3F,2,20,20,20,20,20,20,20,20,20,20,20,20,20,20,33,30,3,0};
    		byte[] data2 = new byte[]{(byte)0x01,(byte)0x01,(byte)0x30,(byte)0x30,(byte)0x35,(byte)0x34,(byte)0x36,(byte)0x30,(byte)0x31,
    								  (byte)0x31,(byte)0x30,(byte)0x31,(byte)0x37,(byte)0x31,(byte)0x02,(byte)0x30,(byte)0x38,(byte)0x31,
    								  (byte)0x31,(byte)0x30,(byte)0x39,(byte)0x20,(byte)0x37,(byte)0x31,(byte)0x35,(byte)0x34,(byte)0x36,
    								  (byte)0x31,(byte)0x31,(byte)0x03,(byte)0x13};
			SiemensSCTM sSctm = new SiemensSCTM(null, null,20,5,"","",1,1);

			System.out.println(sSctm.isChecksumDump(data));
			System.out.println(sSctm.isChecksumDump(data2));
		} catch (SiemensSCTMException e) {
			e.printStackTrace();
		}

    }

    private int forcedDelay;
    /**
     * Class constructor.
     * @param inputStream InputStream for the active connection, e.g. established with ATDialer.
     * @param outputStream OutputStream for the active connection, e.g. established with ATDialer.
     * @param iTimeout Time in ms. for a request to wait for a response before returning an timeout error.
     * @exception SiemensSCTMException
     */
    public SiemensSCTM(InputStream inputStream,
                       OutputStream outputStream,
                       int iTimeout,
                       int iMaxRetries,
                       String strPass,
                       String nodeId,
                       int iEchoCancelling, int forcedDelay) throws SiemensSCTMException {
        this(inputStream, outputStream, iTimeout, iMaxRetries, strPass, nodeId, iEchoCancelling, null, forcedDelay);
    } // public SiemensSCTM(...)

    public SiemensSCTM(InputStream inputStream,
                       OutputStream outputStream,
                       int iTimeout,
                       int iMaxRetries,
                       String strPass,
                       String nodeId,
                       int iEchoCancelling,
                       HalfDuplexController halfDuplexController,
                       int forcedDelay) {
        this.strPass = strPass;
        this.nodeId = nodeId;
        //setOffsetandLengths(strPass);
        setOffsetandLengths(nodeId); // 12112007
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.iMaxRetries = iMaxRetries;
        this.iEchoCancelling = iEchoCancelling;
        iProtocolTimeout=iTimeout;
        boolSCTMConnected=false;
        initProtocolCounters();
        lastSCTMFrame = new SiemensSCTMFrameDecoder(this);
        this.halfDuplexController=halfDuplexController;
        this.forcedDelay = forcedDelay;
    }

    private void setOffsetandLengths(String str) {
         // SCTM frame offsets
         SCTM_HEADER_HCC=7+str.length();
         SCTM_HEADER_STATUS=1;
         SCTM_DATABLOCK_OFFSET=9+str.length();
         SCTM_HEADER_OFFSET=1;
         SCTM_HEADER_LENGTH=4+str.length();
         SCTM_HEADER_LENGTH_SIZE=3;
         SCTM_HEADER_DBN=2+str.length();
         SCTM_HEADER_AKN=3+str.length();
         SCTM_HEADER_ID=2;
         SCTM_HEADER_ID_SIZE=str.length();
         // SCTM frame sizes
         SCTM_HEADER_SIZE=9+str.length();
    }

    public byte[] getDumpData() throws IOException {
        return iec1107Dump;
    }

    private void incDBN() {
        DBN++;
        if (DBN == 10) DBN=0;
    }

    private byte getDBN() {
        return (byte)DBN;
    }

    private byte getAKN() {
        return (byte)lastSCTMFrame.getDBN();
    }

    private void initProtocolCounters() {
        DBN=0;
    }
    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     */
    public void disconnectMAC() throws SiemensSCTMException {
        if (boolSCTMConnected==true) {
            boolSCTMConnected=false;
        } // if (boolSCTMConnected==true)
    } // public void disconnectMAC() throws SiemensSCTMException

    /**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    public void connectMAC() {
        if (boolSCTMConnected==false) {
            initProtocolCounters();
            boolSCTMConnected=true;
        } // if (boolSCTMConnected==false)

    } // public void connectMAC()

    public void flag(String strIdent) throws NestedIOException,SiemensSCTMException {
        try {
            signOn(strIdent);
        }
        catch(SiemensSCTMException e) {
            throw new SiemensSCTMException("flag() error "+e.getMessage());
        }
    }

    public byte[] sendRequest(byte[] command, byte[] data) throws SiemensSCTMException {
        int iRetries=0;
        incDBN();
        while(true) {
            try {
                sendCommand(command,data);
                return (doReceive());
            }
            catch (SiemensSCTMException e) {
                if (e.getReason() == MAX_RETRIES) throw e;
                if (iRetries++ >=iMaxRetries) throw new SiemensSCTMException("SiemensSCTM, sendRequest, error iMaxRetries, "+e.getMessage(),MAX_RETRIES);
            }
            catch (IOException e) {
                throw new SiemensSCTMException("SiemensSCTM, sendRequest, IOException, "+e.getMessage());
            }
        }
    }

    private byte[] doReceive() throws SiemensSCTMException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int iRetries=0;
        boolean follow=false;
        while(true) {
            try {
                byte[] data = receiveResponse().getData();
                if (data!=null) baos.write(data);
                if (isFollow()) {
                    sendFollowFrame();
                    iRetries=0;
                    follow=true;
                }
                else {
                    if (baos.toByteArray().length == 0)
                        return null;
                    else
                        return baos.toByteArray();
                }
            }
            catch (SiemensSCTMException e) {
                if (follow) {
                    if (e.getReason() == MAX_RETRIES) throw e;
                    if (iRetries++ >=iMaxRetries) throw new SiemensSCTMException("SiemensSCTM, doReceive, error iMaxRetries, "+e.getMessage(),MAX_RETRIES);
                    sendFollowFrame();
                }
                else throw e;
            }
        }
    }

    private boolean isFollow() {
      return ((lastSCTMFrame.getStatus() & CONTINUE_BIT) != 0);
    }

    public void sendInit() throws SiemensSCTMException {
        int iRetries=0;
        while(true) {
            try {
                initProtocolCounters();
                sendINITCOM();
                if (!receiveResponse().isHeaderOnly())
                    throw new SiemensSCTMException("SiemensSCTM, sendInit, recieved frame not empty!");
                //initProtocolCounters();
                return;
            }
            catch (SiemensSCTMException e) {
                if (e.getReason() == MAX_RETRIES) throw e;
                if (iRetries++ >=iMaxRetries) throw new SiemensSCTMException("SiemensSCTM, sendInit, error iMaxRetries, "+e.getMessage(),MAX_RETRIES);
            }
        }

    }

//    private void sendOut(byte txbyte) throws SiemensSCTMException {
//        byte[] txbuffer = new byte[1];
//        txbuffer[0]=txbyte;
//        doSendOut(txbuffer);
//    }

    private void sendOut(byte[] txbuffer) throws SiemensSCTMException {
        doSendOut(txbuffer);
    }

    private void doSendOut(byte[] txbuffer)  throws SiemensSCTMException {

        try {
            if (iEchoCancelling!=0) echoByteArrayOutputStream.write(txbuffer);
            if (halfDuplexController != null)
                halfDuplexController.request2Send(txbuffer.length);
            outputStream.write(txbuffer);
            if (halfDuplexController != null)
                halfDuplexController.request2Receive(txbuffer.length);
        }
        catch (IOException e) {
            throw new SiemensSCTMException("doSendOut() error "+e.getMessage());
        }
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
    }

    private byte[] buildInitFrame() {
        return(calcChecksum(buildHeader((byte)0,(byte)((byte)'?' - 0x30),0)));
    }

    private byte[] buildACKrame() {
        return(calcChecksum(buildHeader(getDBN(),getAKN(),0)));
    }



    private byte[] buildCommandFrame(byte[] command, byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int length=command.length+3;
        if (data != null)  length += data.length;
        byteArrayOutputStream.write(buildHeader(getDBN(),getAKN(),length));
        byteArrayOutputStream.write(command);
        if (data != null) byteArrayOutputStream.write(data);
        byteArrayOutputStream.write(ETX);
        byteArrayOutputStream.write(0);
        return calcChecksum(byteArrayOutputStream.toByteArray());
    }

    private byte[] buildHeader(byte DBN, byte AKN, int length) {
        byte[] header = new byte[SCTM_HEADER_SIZE];
        int i;
        header[0] = SOH;
        if (SCTM_HEADER_ID_SIZE == 5)
            header[SCTM_HEADER_STATUS] = (byte)(CENTRAL_STATION_TELEGRAMM+0x30);
        else if (SCTM_HEADER_ID_SIZE == 8)
            header[SCTM_HEADER_STATUS] = (byte)(CENTRAL_STATION_TELEGRAMM+0x60);
        //for (i=0;i<SCTM_HEADER_ID_SIZE;i++) header[i+SCTM_HEADER_ID] = strPass.getBytes()[i];
        for (i=0;i<SCTM_HEADER_ID_SIZE;i++) header[i+SCTM_HEADER_ID] = nodeId.getBytes()[i]; // KV 12112007
        header[SCTM_HEADER_DBN] = (byte)(DBN+0x30);
        header[SCTM_HEADER_AKN] = (byte)(AKN+0x30);
        header[SCTM_HEADER_LENGTH] = String.valueOf(length/100).getBytes()[0];
        header[SCTM_HEADER_LENGTH+1] = String.valueOf((length/10)%10).getBytes()[0];
        header[SCTM_HEADER_LENGTH+2] = String.valueOf(length%10).getBytes()[0];

        header[SCTM_HEADER_HCC] = 0; // HCC
        if (length == 0) header[SCTM_HEADER_LENGTH+SCTM_HEADER_LENGTH_SIZE+1] = ETX;
        else header[SCTM_HEADER_LENGTH+SCTM_HEADER_LENGTH_SIZE+1] = STX;
        return header;
    }

    /**
     * Method to send an array of bytes via outputstream.
     */
    private void sendRawData(byte[] txbuffer) throws SiemensSCTMException {
        delay(forcedDelay);
        flushEchoBuffer();
        sendOut(txbuffer);

    } // private void sendRawData(byte[] byteBuffer)

    private void sendINITCOM() throws SiemensSCTMException {
        byte[] frame = buildInitFrame();
        sendRawData(frame);
    }
    private void sendFollowFrame() throws SiemensSCTMException {
        byte[] frame = buildACKrame();
        sendRawData(frame);
    }

    private void sendCommand(byte[] command, byte[] data) throws SiemensSCTMException {
        try {
            byte[] frame = buildCommandFrame(command, data);
            sendRawData(frame);
        }
        catch(IOException e) {
            throw new SiemensSCTMException("SiemensSCTM, sendCommand, IOException, "+e.getMessage());
        }
    }

    private SiemensSCTMFrameDecoder receiveResponse()  throws SiemensSCTMException {
        try {
            byte[] frame = receiveFrame();
            lastSCTMFrame = new SiemensSCTMFrameDecoder(this,frame);
            if (lastSCTMFrame.getAKN() != getDBN()) {
                throw new SiemensSCTMException("SiemensSCTM, receiveResponse, wrong sequence nr");
            }
            return lastSCTMFrame;
        }
        catch(IOException e) {
            throw new SiemensSCTMException("SiemensSCTM, receiveResponse, IOException, "+e.getMessage());
        }
    }


    private byte[] calcChecksum(byte[] data) {
        calcHCC(data);
        calcBCC(data);
        return data;
    }

    private boolean isChecksum(byte[] data) {
        return (calcHCC(data) == 0) &&
                (calcBCC(data) == 0);
    }

    private boolean isChecksumDump(byte[] data) {
        int sum=0;
        for (int i = 1;i<data.length; i++) {
            sum ^= data[i];
        }
        return (sum==0);

    }

    private byte calcHCC(byte[] data) {
        int sum=0;
        if (SCTM_DATABLOCK_OFFSET <= data.length) {
            for (int i=SCTM_HEADER_OFFSET;i<SCTM_DATABLOCK_OFFSET;i++) {
                if ((data[i] == STX) || (data[i] == ETX)) {
                    break;
                }
                sum^=data[i];
            }
            data[SCTM_HEADER_HCC] = (byte)(sum&0xff);
            return data[SCTM_HEADER_HCC];
        }
        else {
            return 0;
        }
    }

    private byte calcBCC(byte[] data) {
        int sum=0;
        if (SCTM_DATABLOCK_OFFSET < data.length) {
            for (int i=SCTM_DATABLOCK_OFFSET;i<data.length;i++) {
                sum ^= data[i];
            }
            data[data.length-1] = (byte)(sum&0xff);
            return data[data.length-1];
        }
        else {
            return 0;
        }
    }

    private void flushInputStream()  throws SiemensSCTMException {
        try {
            while(inputStream.available() != 0) {
                inputStream.read(); // flush inputbuffer
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SiemensSCTMException("flushInputStream() error "+e.getMessage());
        }
    } // private void flushInputStream()  throws SiemensSCTMException

    private byte[] receiveFrame() throws NestedIOException,SiemensSCTMException {
        return receiveFrame(false);
    }

    private byte[] receiveDump() throws NestedIOException,SiemensSCTMException {
        return receiveFrame(true);
    }

    private static final byte STATE_WAIT_FOR_SOH=0;
    private static final byte STATE_WAIT_FOR_STX=1;
    private static final byte STATE_WAIT_FOR_LENGTH=2;
    private static final byte STATE_WAIT_FOR_DATA=3;
    private static final byte STATE_WAIT_FOR_ETX=4;
    private static final byte STATE_WAIT_FOR_CHECKSUM=5;

    private byte[] receiveFrame(boolean dump) throws NestedIOException,SiemensSCTMException {
        long lMSTimeout,lMSTimeoutInterFrame;
        int iNewKar;
        int iState=STATE_WAIT_FOR_SOH;
        int iLength=0;
        byte[] receiveBuffer;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte calculatedChecksum;

        if (dump) {
            iState = STATE_WAIT_FOR_STX;
        } else {
            iState = STATE_WAIT_FOR_SOH;
        }

        lMSTimeout = System.currentTimeMillis() + 600000;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;

        if (DEBUG == 1) {
            System.out.println("receiveData(...):");
        }

        copyEchoBuffer();

        while(true) {
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) {
                    ProtocolUtils.outputHex(iNewKar);
                }

                switch(iState) {

                    case STATE_WAIT_FOR_SOH: {
                        if ((byte)iNewKar == SOH) {
                            iState = STATE_WAIT_FOR_ETX;
                            byteArrayOutputStream.reset();
                            byteArrayOutputStream.write(iNewKar);
                        }
                    } break; // STATE_WAIT_FOR_SOH

                    case STATE_WAIT_FOR_STX: {
                        if ((byte)iNewKar == STX) {
                            iState = STATE_WAIT_FOR_ETX;
                            byteArrayOutputStream.reset();
                            byteArrayOutputStream.write(iNewKar);
                        }
                    } break; // STATE_WAIT_FOR_STX

                    case STATE_WAIT_FOR_ETX: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                        byteArrayOutputStream.write(iNewKar);

                        if ((byte)iNewKar == ETX) {
                            iState = STATE_WAIT_FOR_CHECKSUM;
                            if (!dump) {
                                byte[] data=byteArrayOutputStream.toByteArray();
                                int length = ProtocolUtils.parseIntFromStr(data,SCTM_HEADER_LENGTH,SCTM_HEADER_LENGTH_SIZE);
                                if (length == 0) {
                                    if (!isChecksum(data)) {
                                        throw new SiemensSCTMException("receiveData() bad CRC error");
                                    }
                                    return(data);
                                }
                            }
                        }


                    } break; // STATE_WAIT_FOR_ETX

                    case STATE_WAIT_FOR_CHECKSUM: {
                        byteArrayOutputStream.write(iNewKar);
                        byte[] data=byteArrayOutputStream.toByteArray();
                        if (dump) {
                            if (!isChecksumDump(data)) {
                                throw new SiemensSCTMException("receiveData() bad CRC error");
                            }
                        }
                        else {
                            if (!isChecksum(data)) {
                                throw new SiemensSCTMException("receiveData() bad CRC error");
                            }
                        }
                        return(data);

                    } //break; // STATE_WAIT_FOR_CRC

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new SiemensSCTMException("receiveData() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - lMSTimeoutInterFrame > 0) {
                throw new SiemensSCTMException("receiveData() interframe timeout error",TIMEOUT_ERROR);
            }


        } // while(true)

    } // private byte[] receiveData(String str,boolean dump) throws SiemensSCTMException

    private static final int WAIT_FOR_IDENT=0;
    private static final int WAIT_FOR_COMPLETION=1;

    private void receiveIdent(String str) throws NestedIOException,SiemensSCTMException {
        long lMSTimeout;
        int iNewKar;
        String strIdent= "";
        byte[] convert=new byte[1];
        int state=WAIT_FOR_IDENT;

        lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

        copyEchoBuffer();
        String convertstr;

        while(true) {

            if ((iNewKar = readIn()) != -1) {
                convert[0] = (byte)iNewKar;
                if (state==WAIT_FOR_IDENT) {
                    strIdent += new String(convert);

                    if ((str != null) && ("".compareTo(str) != 0)) {
                        if (strIdent.compareTo(str) == 0) {
                            state = WAIT_FOR_COMPLETION;
                        }
                    }
                    else {
                        state=WAIT_FOR_COMPLETION;
                    }
                }
                else if (state==WAIT_FOR_COMPLETION) {
                    if (convert[0] == 0x0A) {
                        return;
                    }
                }
            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new SiemensSCTMException("receiveIdent() timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // private void receiveIdent(String str) throws SiemensSCTMException


    private int readIn() throws SiemensSCTMException {
        try {
            int iNewKar;

            if (inputStream.available() != 0) {
                iNewKar = inputStream.read();
                if (iNewKar != echoByteArrayInputStream.read()) {
                    return iNewKar;
                }
            } // if (inputStream.available() != 0)
            else {
                Thread.sleep(100);
            }

        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new SiemensSCTMException("readIn() error "+e.getMessage());
        }

        return(-1);

    } // private int readIn() throws SiemensSCTMException


    private void delay(long lDelay) {
        try {
            Thread.sleep(lDelay);
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    private void flushEchoBuffer() {
        echoByteArrayOutputStream.reset();
    }
    private void copyEchoBuffer() {
        echoByteArrayInputStream = new ByteArrayInputStream(echoByteArrayOutputStream.toByteArray());
    }

    private void signOn(String strIdent) throws NestedIOException,SiemensSCTMException {
        int i,t,iRetries=0;

        while(true) {
            try {
                String str="/?"+nodeId+"!\r\n";
                sendRawData(str.getBytes());
                receiveIdent(strIdent);
                iec1107Dump = receiveDump();
                return;
            }
            catch (SiemensSCTMException e) {
                if (e.getReason() == MAX_RETRIES) {
                    throw e;
                }
                if (iRetries++ >=iMaxRetries) {
                    throw new SiemensSCTMException("signOn() error iMaxRetries, " + e.getMessage(), MAX_RETRIES);
                }
            }
        }
    }

}