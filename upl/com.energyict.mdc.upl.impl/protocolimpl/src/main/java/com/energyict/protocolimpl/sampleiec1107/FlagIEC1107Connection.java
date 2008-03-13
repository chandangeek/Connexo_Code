package com.energyict.protocolimpl.sampleiec1107;

import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocol.*;
import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.HHUSignOn;
/**
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Flag IEC1107 datalink layer protocol.
 * <B>Changes :</B><BR>
 *      KV 04102002 Initial version.<BR>
 *      KV 06102003 changed delay after break 300ms -> DELAY_AFTER_BREAK macro 2000ms
 *                  bugfix in method doDataReadout(), set Z (baudrate) parameter
 *      KV 06072004 Add delayAndFlush(2000) to disconnect
 *                  Check for password != null if security level higher then 0 is requested
 *                  Add security level cause...
 */
public class FlagIEC1107Connection extends Connection implements ProtocolConnection {
    
    private static final byte DEBUG=0;
    private static final int DELAY_AFTER_BREAK=2000; // KV 06102003
    
    ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
    
    private static final int TIMEOUT=600000;
    
    private static final byte UNKNOWN_ERROR=-1;
    private static final byte TIMEOUT_ERROR=-2;
    private static final byte SECURITYLEVEL_ERROR=-3; // KV 06072004
    
    
    private int iMaxRetries;
    
    // General attributes
    private int iProtocolTimeout;
    private int iIEC1107Compatible;
    
    private static final byte SOH=0x01;
    private static final byte STX=0x02;
    private static final byte ETX=0x03;
    private static final byte EOT=0x04;
    private static final byte ACK=0x06;
    private static final byte NAK=0x15;
    
    // specific IEC1107
    private boolean boolFlagIEC1107Connected;
    
    
    // Raw frames
    // private static final int MAX_BUFFER_SIZE=256;
    // private byte[] txFrame = new byte[MAX_BUFFER_SIZE];
    // private byte[] rxFrame = new byte[MAX_BUFFER_SIZE];
    
    private long lForceDelay;
    
    private Encryptor encryptor;
    

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible) throws ConnectionException {
          this(inputStream,outputStream,iTimeout,iMaxRetries,lForceDelay,iEchoCancelling,iIEC1107Compatible,null);                           
    }
    
    /**
     * Class constructor.
     * @param inputStream InputStream for the active connection, e.g. established with ATDialer.
     * @param outputStream OutputStream for the active connection, e.g. established with ATDialer.
     * @param iTimeout Time in ms. for a request to wait for a response before returning an timeout error.
     * @param iMaxRetries nr of retries before fail in case of a timeout or recoverable failure
     * @param lForceDelay delay before send. Some protocols have troubles with fast send/receive
     * @param iEchoCancelling echo cancelling on/off
     * @param iIEC1107Compatible behave full compatible or use protocol special features
     * @param encryptor if the protocol has an encryptor to use for security level 2 password logon
     * @exception ProtocolConnectionException
     */
    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor) throws ConnectionException {
        super(inputStream, outputStream, lForceDelay, iEchoCancelling);
        this.iMaxRetries = iMaxRetries;
        this.lForceDelay = lForceDelay;
        this.iIEC1107Compatible = iIEC1107Compatible;
        this.encryptor=encryptor;
        iProtocolTimeout=iTimeout;
        boolFlagIEC1107Connected=false;
    } // public FlagIEC1107Connection(...)
    
    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     * @exception HDLCConnectionException
     */
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        if (boolFlagIEC1107Connected==true) {
            try {
                //byte[] buffer = {(byte)SOH,(byte)0x42,(byte)0x30,(byte)ETX,(byte)0x71};
                //sendRawData(buffer);
                sendBreak(); // KV 06072004
                delayAndFlush(DELAY_AFTER_BREAK); // KV 06072004
                boolFlagIEC1107Connected=false;
                return;
            }
            catch(ConnectionException e) {
                try {
                    flushInputStream();
                }
                catch(ConnectionException ex) {
                    throw new ProtocolConnectionException("disconnectMAC() error, ConnectionException, "+e.getMessage()+", ConnectionException, "+ex.getMessage());
                }
                throw new ProtocolConnectionException("disconnectMAC() error, ConnectionException, "+e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==true)
    } // public void disconnectMAC() throws ProtocolConnectionException
    
    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     * @exception HDLCConnectionException
     */
    public void sendBreak() throws NestedIOException,ProtocolConnectionException {
        try {
            byte[] buffer = {(byte)SOH,(byte)0x42,(byte)0x30,(byte)ETX,(byte)0x71};
            sendRawData(buffer);
            return;
        }
        catch(ConnectionException e) {
            try {
                flushInputStream();
            }
            catch(ConnectionException ex) {
                throw new ProtocolConnectionException("disconnectMAC() error, ConnectionException, "+e.getMessage()+", ConnectionException, "+ex.getMessage());
            }
            throw new ProtocolConnectionException("sendBreak() error, "+e.getMessage());
        }
    } // public void sendBreak() throws ProtocolConnectionException
    /**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     * @exception HDLCConnectionException
     */
    public MeterType connectMAC(String strIdentConfig, String strPass,int iSecurityLevel,String meterID) throws IOException,ProtocolConnectionException {
        if (boolFlagIEC1107Connected==false) {
            MeterType meterType;
            try {
                // KV 18092003
                if (hhuSignOn == null)
                    meterType = signOn(strIdentConfig,meterID);
                else
                    meterType = hhuSignOn.signOn(strIdentConfig,meterID);
                boolFlagIEC1107Connected=true;
                authenticate(strPass,iSecurityLevel);
                return meterType;
            }
            catch(ProtocolConnectionException e) {
                throw new ProtocolConnectionException("connectMAC(), ProtocolConnectionException "+e.getMessage());
            }
            catch(ConnectionException e) {
                throw new ProtocolConnectionException("connectMAC(), ConnectionException "+e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==false
        
        return null;
        
    } // public MeterType connectMAC() throws HDLCConnectionException
    
    private MeterType signOn(String strIdentConfig, String meterID) throws IOException,NestedIOException,ProtocolConnectionException {
        int retries=0;
        while(true) {
            try {
                String str="/?"+meterID+"!\r\n";
                sendRawData(str.getBytes());
                // KV 16122003
                String strIdent = receiveIdent(strIdentConfig);
                if (iIEC1107Compatible == 1) {
                    // protocol mode C, programming mode
                    byte[] ack={(byte)ACK,(byte)0x30,(byte)strIdent.charAt(4),(byte)0x31,(byte)0x0D,(byte)0x0A};
                    sendRawData(ack);
                }
                else  {
                    // special case for Elster A1700 meter
                    byte[] ack={(byte)ACK,(byte)0x30,(byte)strIdent.charAt(4),(byte)0x36,(byte)0x0D,(byte)0x0A};
                    sendRawData(ack);
                }
                
                return new MeterType(strIdent); 
            }
            catch (ConnectionException e) {
                if (retries++ >=iMaxRetries) throw new ProtocolConnectionException("signOn() error iMaxRetries, "+e.getMessage());
                else {
                    sendBreak();
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        }
        
    } // private MeterType signOn(String strIdentConfig, String meterID) throws NestedIOException,ProtocolConnectionException
    
    private void authenticate(String strPass,int iSecurityLevel) throws NestedIOException,ProtocolConnectionException {
        int i,t,iRetries=0;
        
        while(true) {
            try {
                // here, mac is connected for HHU or modem
                byte[] command=null;
                byte[] data=null;
                if (iSecurityLevel == 0) {
                    byte[] key = receiveData();
                    //command = LOGON_LEVEL_2;
                    //data = buildData(strPass);
                }
                else if (iSecurityLevel == 1) {
                    skipCommandMessage(); // P0
                    // KV 06072004
                    if (strPass == null)
                        throw new ProtocolConnectionException("FlagIEC1107Connection: invalid SecurityLevel 1 with a null password!",SECURITYLEVEL_ERROR);
                    command = LOGON_LEVEL_1;
                    data = buildData(strPass);
                }
                else if (iSecurityLevel == 2) {
                    byte[] key = receiveData();
                    command = LOGON_LEVEL_2;
                    if (encryptor != null)
                        data = buildData(encryptor.encrypt(strPass, new String(key)));
                    else
                        data = buildData(strPass);
                }
                else throw new ProtocolConnectionException("FlagIEC1107Connection: invalid SecurityLevel",SECURITYLEVEL_ERROR);
                
                if (iSecurityLevel != 0) {
                    while(true) {
                        echoByteArrayOutputStream.reset();
                        sendRawCommandFrame(command,data); // logon using securitylevel
                        String resp = receiveString();
                        if (resp.compareTo("ACK")==0) {
                            break;
                        }
                        else if (resp.compareTo("B0")==0) {
                            throw new IOException("FlagIEC1107Connection, error, "+resp+" received");
                        }
                        else if (resp.compareTo("(ERR1)")==0) {
                            throw new IOException("FlagIEC1107Connection, error, "+resp+" received");
                        }
                        else if (resp.indexOf("(ER")!=-1) { // IskraEmeco errors...
                            throw new IOException("FlagIEC1107Connection, error, "+resp+" received");
                        }
                        if (iRetries++ >=iMaxRetries) throw new ProtocolConnectionException("authenticate() error iRetries");
                    }
                }
                return;
                
            }
            catch (ProtocolConnectionException e) {
                if (e.getReason() == SECURITYLEVEL_ERROR) // KV 06072004
                    throw e;
                else if (iRetries++ >=iMaxRetries)
                    throw new ProtocolConnectionException("authenticate() error iMaxRetries, "+e.getMessage());
                else {
                    sendBreak();
                    delay(DELAY_AFTER_BREAK);
                }
            }
            catch (IOException e) {
                throw new NestedIOException(e,"FlagIEC1107Connection, authenticate(), "+e.getMessage());
            }
        }
    } // private authenticate() throws ProtocolConnectionException
    

    
    public byte[] dataReadout(String strIdent,String meterID) throws NestedIOException, ProtocolConnectionException {
        byte[] data = null;
        if (boolFlagIEC1107Connected==false) {
            try {
                data = doDataReadout(strIdent,meterID);
            }
            catch(ProtocolConnectionException e) {
                throw new ProtocolConnectionException("dataReadout() error "+e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==false)
        return data;
    } // public void connectMAC() throws HDLCConnectionException
    
    public byte[] doDataReadout(String strIdentConfig,String meterID) throws NestedIOException, ProtocolConnectionException {
        int iRetries=0;
        while(true) {
            try {
                String str="/?"+meterID+"!\r\n";
                sendRawData(str.getBytes());
                String strIdent = receiveIdent(strIdentConfig);
                byte[] ack={(byte)ACK,(byte)0x30,(byte)strIdent.charAt(4),(byte)0x30,(byte)0x0D,(byte)0x0A};
                sendRawData(ack);
                boolFlagIEC1107Connected=true;
                return (receiveRawData());
            }
            catch (ConnectionException e) {
                if (iRetries++ >=iMaxRetries) {
                    throw new ProtocolConnectionException("doDataReadout() error iMaxRetries, "+e.getMessage());
                }
                else {
                    sendBreak();
                    delay(DELAY_AFTER_BREAK);
                }
                
            }
        }
        
    } // private dataReadout() throws ProtocolConnectionException
    
    
    
    private byte[] buildData(String strPass) {
        byte[] data = new byte[strPass.getBytes().length+2];
        int i=0;
        data[i++] = '(';
        for (int t=0;t<strPass.getBytes().length;t++) data[i++] = strPass.getBytes()[t];
        data[i++] = ')';
        return data;
    }
    
    public byte[] parseDataBetweenBrackets(byte[] buffer) throws ProtocolConnectionException {
        
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int state=0;
        for(int i = 0;i<buffer.length ; i++) {
            if (state == 0) {
                if (buffer[i] == (byte)'(') state=1;
            }
            else if (state == 1) {
                if (buffer[i] == (byte)')') {
                    state = 2;
                    break;
                }
                data.write((int)buffer[i]);
            }
        }
        if (state==2) return data.toByteArray();
        else throw new ProtocolConnectionException("FlagIEC1107Connection, parseDataBetweenBrackets, error");
    }
    
    public static final byte[] WRITE5={'W','5'};
    public static final byte[] WRITE2={'W','2'};
    public static final byte[] READ5={'R','5'};
    public static final byte[] READ6={'R','6'};
    public static final byte[] READ3={'R','3'}; // KV 06072004
    public static final byte[] READ1={'R','1'};
    public static final byte[] READSTREAM={'R','D'};
    public static final byte[] WRITE1={'W','1'};
    public static final byte[] LOGON_LEVEL_1={'P','1'};
    public static final byte[] LOGON_LEVEL_2={'P','2'};
    
    public void sendCommandFrame(byte[] command,byte[] data) throws ProtocolConnectionException {
        try {
            ByteArrayOutputStream rawdata = new ByteArrayOutputStream();
            rawdata.write('(');
            rawdata.write(data);
            rawdata.write(')');
            doSendCommandFrame(command,rawdata.toByteArray(),false);
        }
        catch (IOException e) {
            throw new ProtocolConnectionException("FlagIEC1107Connection, sendCommandFrame, "+e.getMessage());
        }
    }
    
    public void sendRawCommandFrame(byte[] command,byte[] rawdata) throws NestedIOException, ConnectionException, ProtocolConnectionException {
        doSendCommandFrame(command,rawdata,false);
    }
    
    public String sendRawCommandFrameAndReturn(byte[] command,byte[] rawdata) throws NestedIOException, ConnectionException, ProtocolConnectionException {
        return doSendCommandFrame(command,rawdata,true);
    }
    
    private String doSendCommandFrame(byte[] command,byte[] data, boolean returnResult) throws NestedIOException, ConnectionException, ProtocolConnectionException {
        int iLength,iRetries=0;
        int t,i;
        byte[] txbuffer=new byte[command.length+data.length+3];
        byte bChecksum;
        String retVal=null;
        delay(lForceDelay);
        
        i=0;
        for (t = 0;t<command.length;t++) txbuffer[i++] = command[t];
        txbuffer[i++]=STX;
        for (t = 0;t<data.length;t++) txbuffer[i++] = data[t];
        txbuffer[i++]=ETX;
        bChecksum = calcChecksum(txbuffer);
        txbuffer[txbuffer.length-1]=bChecksum;
        
        flushEchoBuffer();
        
        
        if (command[0] == 'W') {
            while(true) {
                echoByteArrayOutputStream.reset();
                sendOut(SOH);
                sendOut(txbuffer);
                String resp = receiveString();
                if (resp.compareTo("ACK")==0) break;
                if (returnResult) {
                    if (resp.indexOf("ERR")!=-1) retVal=resp;  
                    break;
                }
                if (iRetries++ >=iMaxRetries) throw new ProtocolConnectionException("doSendCommandFrame() error iMaxRetries!");
            }
        }
        else if ((command[0] == 'R') || (command[0] == 'P')) {
            sendOut(SOH);
            sendOut(txbuffer);
        }
        else throw new ProtocolConnectionException("doSendCommandFrame() error unknown tag!");
        
        if (DEBUG==1) {
            ProtocolUtils.outputHex( ((int)SOH)  &0x000000FF);
            for (i=0;i<txbuffer.length;i++)
                ProtocolUtils.outputHex( ((int)txbuffer[i])  &0x000000FF);
            System.out.println();
        }
        
        return retVal;
        
    } // public void doSendCommandFrame(byte bCommand,byte[] data) throws ProtocolConnectionException
    
    public void skipCommandMessage() throws NestedIOException,ConnectionException, ProtocolConnectionException {
        long lMSTimeout;
        int iNewKar;
        byte bState=0;
        
        lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;
        
        copyEchoBuffer();
        
        while(true) {
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) ProtocolUtils.outputHex( ((int)iNewKar));
                
                if ((bState==0) && ((byte)iNewKar == SOH))
                    bState = 1;
                else if ((bState==1) && ((byte)iNewKar == ETX))
                    bState = 2;
                else if (bState==2)
                    return;
                
            } // if ((iNewKar = readIn()) != -1)
            
            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ProtocolConnectionException("skipCommandMessage() timeout error",TIMEOUT_ERROR);
            }
            
        } // while(true)
        
    } // public void skipCommandMessage() throws ProtocolConnectionException
    
    private static final byte STATE_WAIT_FOR_START=0;
    private static final byte STATE_WAIT_FOR_LENGTH=1;
    private static final byte STATE_WAIT_FOR_DATA=2;
    private static final byte STATE_WAIT_FOR_END=3;
    private static final byte STATE_WAIT_FOR_CHECKSUM=4;
    
    public String receiveString() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        return new String(receiveRawData());
    }
    
    public byte[] receiveData() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        return parseDataBetweenBrackets(doReceiveData());
    }
    
    public byte[] receiveRawData() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        return doReceiveData();
    }
    
    public byte[] doReceiveData() throws NestedIOException, ConnectionException, ProtocolConnectionException {
        long lMSTimeout,lMSTimeoutInterFrame;
        int iNewKar;
        int iState;
        int iLength=0;
        byte[] receiveBuffer;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream  = new ByteArrayOutputStream();
        byte calculatedChecksum;
        boolean end;
        
        // init
        iState=STATE_WAIT_FOR_START;
        end=false;
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
        resultArrayOutputStream.reset();
        byteArrayOutputStream.reset();
        
        if (DEBUG == 1) System.out.println("doReceiveData(...):");
        copyEchoBuffer();
        
        while(true) {
            
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) ProtocolUtils.outputHex( ((int)iNewKar));
                
                switch(iState) {
                    case STATE_WAIT_FOR_START: {
                        
                        if ((byte)iNewKar == SOH) iState = STATE_WAIT_FOR_END;
                        if ((byte)iNewKar == STX) iState = STATE_WAIT_FOR_END;
                        if ((byte)iNewKar == ACK) {
                            return ("ACK".getBytes());
                        }
//                        if ((byte)iNewKar == NAK) {
//                            System.out.println("NAK RECEIVED...");
//                        }
                        
                    } break; // STATE_WAIT_FOR_START
                    
                    case STATE_WAIT_FOR_END: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                        if ((byte)iNewKar == ETX) {
                            end = true;
                            iState = STATE_WAIT_FOR_CHECKSUM;
                        }
                        else if ((byte)iNewKar == EOT) {
                            end = false;
                            iState = STATE_WAIT_FOR_CHECKSUM;
                        }
                        byteArrayOutputStream.write(iNewKar);
                        
                    } break; // STATE_WAIT_FOR_END
                    
                    case STATE_WAIT_FOR_CHECKSUM: {
                        byteArrayOutputStream.write(iNewKar);
                        calculatedChecksum = calcChecksum(byteArrayOutputStream.toByteArray());
                        if (calculatedChecksum == byteArrayOutputStream.toByteArray()[byteArrayOutputStream.toByteArray().length-1]) {
                            // remove head and tail from byteArrayOutputStream.toByteArray()...
                            byte[] data = new byte[byteArrayOutputStream.toByteArray().length-2];
                            for (int i=0;i<(byteArrayOutputStream.toByteArray().length-2);i++)
                                data[i] = byteArrayOutputStream.toByteArray()[i];
                            try {
                                resultArrayOutputStream.write(data);
                            } catch ( IOException e) {
                                throw new ProtocolConnectionException("receiveStreamData(), IOException, "+e.getMessage());
                            }
                            
                            if (end) return resultArrayOutputStream.toByteArray();
                            
                            // init
                            iState=STATE_WAIT_FOR_START;
                            lMSTimeout = System.currentTimeMillis() + TIMEOUT;
                            lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                            byteArrayOutputStream.reset();
                            end=false;
                            
                            sendRawData(ACK);
                        }
                        else {
                            
                            // KV_DEBUG
                            // init
                            /*
                            iState=STATE_WAIT_FOR_START;
                            lMSTimeout = System.currentTimeMillis() + TIMEOUT;
                            lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                            byteArrayOutputStream.reset();
                            end=false;
                            sendRawData(NAK);
                            */
                            
                            throw new ProtocolConnectionException("doReceiveData() bad CRC error");
                        }
                        
                    } //break; // STATE_WAIT_FOR_CRC
                    
                } // switch(iState)
                
            } // if ((iNewKar = readIn()) != -1)
            
            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ProtocolConnectionException("doReceiveData() response timeout error",TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new ProtocolConnectionException("doReceiveData() interframe timeout error",TIMEOUT_ERROR);
            }
            
            
        } // while(true)
        
    } // public byte[] doReceiveData(String str) throws ProtocolConnectionException
    
    private static final byte STREAM_STATE_WAIT_FOR_START=0;
    private static final byte STREAM_STATE_WAIT_FOR_PACKET=1;
    private static final byte STREAM_STATE_WAIT_FOR_LENGTH=2;
    private static final byte STREAM_STATE_WAIT_FOR_DATA=3;
    private static final byte STREAM_STATE_WAIT_FOR_END=4;
    private static final byte STREAM_STATE_WAIT_FOR_CRC=5;
    
    public byte[] receiveStreamData() throws NestedIOException,ConnectionException, ProtocolConnectionException {
        long lMSTimeout,lMSTimeoutInterFrame;
        int iNewKar;
        int state=STREAM_STATE_WAIT_FOR_START;
        int length=0;
        int packetNR=0;
        int count=0;
        boolean end=false;
        ByteArrayOutputStream brutodata = new ByteArrayOutputStream();
        ByteArrayOutputStream nettodata = new ByteArrayOutputStream();
        ByteArrayOutputStream alldata = new ByteArrayOutputStream();
        byte calculatedChecksum;
        
        
        brutodata.reset();
        nettodata.reset();
        alldata.reset();
        
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
        
        if (DEBUG == 1) System.out.println("receiveStreamData(...):");
        
        copyEchoBuffer();
        
        while(true) {
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) ProtocolUtils.outputHex( ((int)iNewKar));
                brutodata.write(iNewKar);
                switch(state) {
                    case STREAM_STATE_WAIT_FOR_START: {
                        if ((byte)iNewKar == STX) {
                            state = STREAM_STATE_WAIT_FOR_PACKET;
                            count=0;
                            packetNR=0;
                            nettodata.reset();
                        }
                    } break; // STREAM_STATE_WAIT_FOR_START
                    
                    case STREAM_STATE_WAIT_FOR_PACKET: {
                        packetNR |= (((byte)iNewKar&0xFF)<<(8*count));
                        if (count++ >= 1) state = STREAM_STATE_WAIT_FOR_LENGTH;
                    } break; // STREAM_STATE_WAIT_FOR_PACKET
                    
                    case STREAM_STATE_WAIT_FOR_LENGTH: {
                        length = iNewKar&0xFF;
                        count = 0;
                        state = STREAM_STATE_WAIT_FOR_DATA;
                    } break; // STREAM_STATE_WAIT_FOR_LENGTH
                    
                    case STREAM_STATE_WAIT_FOR_DATA: {
                        nettodata.write(iNewKar);
                        if (count++ >= length) state = STREAM_STATE_WAIT_FOR_END;
                    } break; // STREAM_STATE_WAIT_FOR_DATA
                    
                    case STREAM_STATE_WAIT_FOR_END: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                        
                        if ((byte)iNewKar == ETX) {
                            end = false;
                        }
                        else if ((byte)iNewKar == EOT) {
                            end = true;
                        }
                        else throw new ProtocolConnectionException("receiveStreamData() invalid end flag");
                        
                        state = STREAM_STATE_WAIT_FOR_CRC;
                        count=0;
                    } break; // STREAM_STATE_WAIT_FOR_END
                    
                    case STREAM_STATE_WAIT_FOR_CRC: {
                        if (count++ >= 1) {
                            if (CRCGenerator.calcCRC(brutodata.toByteArray()) == 0) {
                                if (nettodata.toByteArray().length != (length+1))
                                    throw new ProtocolConnectionException("receiveStreamData() nettodata invalid length");
                                try {
                                    alldata.write(nettodata.toByteArray());
                                } catch ( IOException e) {
                                    throw new ProtocolConnectionException("receiveStreamData(), IOException, "+e.getMessage());
                                }
                                if (end) {
                                    alldata.write(255); // 0xFF end of data toevoegen!
                                    return alldata.toByteArray();
                                }
                                else state = STREAM_STATE_WAIT_FOR_START;
                            }
                            else throw new ProtocolConnectionException("receiveStreamData() bad crc");
                        }
                        
                    } break; // STREAM_STATE_WAIT_FOR_CRC
                    
                } // switch(iState)
                
            } // if ((iNewKar = readIn()) != -1)
            
            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveStreamData() response timeout error",TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new ProtocolConnectionException("receiveStreamData() interframe timeout error",TIMEOUT_ERROR);
            }
            
        } // while(true)
        
    } // public byte[] receiveStreamData(String str) throws ProtocolConnectionException
    
    public String receiveIdent(String str) throws NestedIOException, ConnectionException, ProtocolConnectionException {
        long lMSTimeout;
        int iNewKar;
        String strIdent= "";
        byte[] convert=new byte[1];
        
        lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;
        
        copyEchoBuffer();
        String convertstr;
        
        while(true) {
            
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) ProtocolUtils.outputHex( ((int)iNewKar));
                
                if ((byte)iNewKar==NAK) sendBreak();
                
                convert[0] = (byte)iNewKar;
                convertstr = new String(convert);
                strIdent += convertstr;
                if (convertstr.compareTo("\\") == 0)
                    strIdent += convertstr;

                // KV 15122003 if deviceid is different from null and not empty, use it to compare
                // with the received deviceid.
                if ((str != null) && ("".compareTo(str) != 0)) {
                    if (strIdent.compareTo(str) == 0) {
                        return strIdent; // KV 16122003
                    }
                    // KV 16122003
                    else if ((byte)iNewKar == 0x0A) {
                        throw new ProtocolConnectionException("receiveIdent() device id mismatch!");
                    }
                }
                else {
                    if ((byte)iNewKar == 0x0A) 
                        return strIdent; // KV 16122003
                }
            } // if ((iNewKar = readIn()) != -1)
            
            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveIdent() timeout error",TIMEOUT_ERROR);
            }
            
        } // while(true)
        
    } // public void receiveIdent(String str) throws ProtocolConnectionException
    
    
    // KV 18092003
    HHUSignOn hhuSignOn=null;
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        this.hhuSignOn=hhuSignOn;
    }
    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }
} // public class FlagIEC1107Connection
