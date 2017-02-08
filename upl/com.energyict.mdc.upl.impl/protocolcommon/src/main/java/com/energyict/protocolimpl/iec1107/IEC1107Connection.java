package com.energyict.protocolimpl.iec1107;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.Connection;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.meteridentification.MeterTypeImpl;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
 *      KV 28072004 Reengineered to implement ProtocolConnection interface
 *      KV 27102004 Make more robust against marginal comminication quality
 *      KV 10122004 Add IEC1107Compatible > 1 as special feature to control baudrate on a E600 meter...
 *      KV 21122004 Cearify exception messages and add state to control NAK
 *      JM 15042009 Generalized the Software7E1 for all IEC1107 protocols
 */
public class IEC1107Connection extends Connection implements ProtocolConnection {

    private static final byte DEBUG=0;
    protected static final int DELAY_AFTER_BREAK=2000; // KV 06102003

    ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
    //ByteArrayInputStream echoByteArrayInputStream;

    protected static final int TIMEOUT=600000;

    protected static final byte UNKNOWN_ERROR=-1;
    protected static final byte TIMEOUT_ERROR=-2;
    protected static final byte SECURITYLEVEL_ERROR=-3; // KV 06072004


    private int iMaxRetries;

    private byte[] authenticationCommand=null;
    private byte[] authenticationData=null;

    // General attributes
    private int iProtocolTimeout;
    private int iIEC1107Compatible;

    protected static final byte SOH=0x01;
    protected static final byte STX=0x02;
    protected static final byte ETX=0x03;
    protected static final byte EOT=0x04;
    protected static final byte ACK=0x06;
    protected static final byte NAK=0x15;

    // specific IEC1107
    private boolean boolFlagIEC1107Connected;


    // Raw frames
    // private static final int MAX_BUFFER_SIZE=256;
    // private byte[] txFrame = new byte[MAX_BUFFER_SIZE];
    // private byte[] rxFrame = new byte[MAX_BUFFER_SIZE];

    private long lForceDelay;
    private int iEchoCancelling;
    private int iSecurityLevel;
    private Encryptor encryptor;
    private String errorSignature=null;

    private byte[] txBuffer=null;

    private static final int STATE_SIGNON=0;
    private static final int STATE_PROGRAMMINGMODE=1;

    private int state=STATE_SIGNON;
    private int checksumMethod=0;
    private boolean readoutEnabled = false;
	private boolean software7E1 = false;
    private boolean noBreakRetry = false;

    //public IEC1107Connection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible) throws ConnectionException {
    //      this(inputStream,outputStream,iTimeout,iMaxRetries,lForceDelay,iEchoCancelling,iIEC1107Compatible,null,null);
    //}
    public IEC1107Connection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, String errorSignature, boolean software7E1) throws ConnectionException {
        this(inputStream,outputStream,iTimeout,iMaxRetries,lForceDelay,iEchoCancelling,iIEC1107Compatible,null,errorSignature, software7E1);
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
    public IEC1107Connection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, Encryptor encryptor, String errorSignature, boolean software7E1) {
        super((software7E1?new Software7E1InputStream(inputStream):inputStream),
    			(software7E1?new Software7E1OutputStream(outputStream):outputStream), lForceDelay, iEchoCancelling);
        this.iMaxRetries = iMaxRetries;
        this.lForceDelay = lForceDelay;
        this.iEchoCancelling = iEchoCancelling;
        this.iIEC1107Compatible = iIEC1107Compatible;
        this.encryptor=encryptor;
        iProtocolTimeout=iTimeout;
        boolFlagIEC1107Connected=false;
        this.errorSignature=errorSignature;
        this.software7E1  = software7E1;
    } // public FlagIEC1107Connection(...)

    /**
     * Requests a MAC disconnect for the IEC1107 layer.
     */
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        if (boolFlagIEC1107Connected) {
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
                    throw new ProtocolConnectionException("disconnectMAC() error, ConnectionException, "+e.getMessage()+", ConnectionException, "+ex.getMessage(), e.getReason());
                }
                throw new ProtocolConnectionException("disconnectMAC() error, ConnectionException, "+e.getMessage(), e.getReason());
            }
        }
    }

    /**
     * Requests a MAC disconnect for the IEC1107 layer.
     */
    public void sendBreak() throws NestedIOException,ProtocolConnectionException {
        try {
            byte[] buffer = {SOH,(byte)0x42,(byte)0x30, ETX,(byte)0x71};
            sendRawData(buffer);
            return;
        }
        catch(ConnectionException e) {
            try {
                flushInputStream();
            }
            catch(ConnectionException ex) {
                throw new ProtocolConnectionException("disconnectMAC() error, ConnectionException, "+e.getMessage()+", ConnectionException, "+ex.getMessage(), e.getReason());
            }
            throw new ProtocolConnectionException("sendBreak() error, "+e.getMessage(), e.getReason());
        }
    }

    /**
     * Requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    public MeterType connectMAC(String strIdentConfig, String strPass,int iSecurityLevel,String meterID) throws IOException {
        if (!boolFlagIEC1107Connected) {

            MeterType meterType;
            this.iSecurityLevel=iSecurityLevel;
            try {

            	if ( getReadoutEnabled() ) {
                    hhuSignOn.enableDataReadout(true);
                }

                // KV 18092003
                if (hhuSignOn == null) {
                    meterType = signOn(strIdentConfig, meterID);
                } else {
                    meterType = hhuSignOn.signOn(strIdentConfig, meterID);
                }
                boolFlagIEC1107Connected=true;
                prepareAuthentication(strPass);
                return meterType;
            }
            catch(ProtocolConnectionException e) {
                throw new ProtocolConnectionException("connectMAC(), ProtocolConnectionException "+e.getMessage(), e.getReason());
            }
            catch(ConnectionException e) {
                throw new ProtocolConnectionException("connectMAC(), ConnectionException "+e.getMessage(), e.getReason());
            }
        }

        return null;

    }

    private MeterType signOn(String strIdentConfig, String meterID) throws IOException {
        int retries=0;
        if (isNoBreakRetry()) {
            retries = iMaxRetries - 1;
        }

        while(true) {
            try {
                String str="/?"+meterID+"!\r\n";
                sendRawData(str.getBytes());
                // KV 16122003
                String strIdentRaw = receiveIdent(strIdentConfig);
                // KV 28072004 remove rubbish at the beginning...
                if (strIdentRaw.indexOf('/') == -1) {
                    throw new ProtocolConnectionException("signOn() invalid response received '/' missing! (" + strIdentRaw + ")");
                }
                String strIdent = new String(ProtocolUtils.getSubArray(strIdentRaw.getBytes(),strIdentRaw.indexOf('/')));
                if (iIEC1107Compatible == 1) {
                    // protocol mode C, programming mode
                    byte[] ack={ACK,(byte)0x30,(byte)strIdent.charAt(4),(byte)0x31,(byte)0x0D,(byte)0x0A};
                    sendRawData(ack);
                }
                else  if (iIEC1107Compatible == 0) {
                    // special case for Elster A1700 meter
                    byte[] ack={ACK,(byte)0x30,(byte)strIdent.charAt(4),(byte)0x36,(byte)0x0D,(byte)0x0A};
                    sendRawData(ack);
                }
                else  {
                    // special case for Enermet E600 meter. Force baudrate ACK first time...
                    // iIEC1107Compatible = 2 = 300 baud
                    // iIEC1107Compatible = 3 = 1200 baud
                    // iIEC1107Compatible = 4 = 2400 baud
                    // iIEC1107Compatible = 5 = 4800 baud
                    // iIEC1107Compatible = 6 = 9600 baud

                    byte[] ack={ACK,(byte)0x30,(byte)(0x31+(iIEC1107Compatible-2)),(byte)0x31,(byte)0x0D,(byte)0x0A};
                    sendRawData(ack);
                    iIEC1107Compatible=1; // set back!
                }

                return new MeterTypeImpl(strIdent);
            }
            catch (StringIndexOutOfBoundsException e) {
                throw new ProtocolConnectionException("signOn() error, "+e.getMessage());
            }
            catch (ConnectionException e) {
                if (retries++ >=iMaxRetries) {
                    throw new ProtocolConnectionException("signOn() error iMaxRetries, possibly meter not responding or wrong nodeaddress, " + e.getMessage(), MAX_RETRIES_ERROR);
                } else {
                    sendBreak();
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        }

    }

    private void prepareAuthentication(String strPass) throws NestedIOException,ProtocolConnectionException {
        int iRetries=0;

        if (isNoBreakRetry()) {
            iRetries = iMaxRetries - 1;
        }

        while(true) {
            String pwd = (strPass!=null) ? strPass : "";
            try {
                // here, mac is connected for HHU or modem
                if (iSecurityLevel == 0) {
                    byte[] key = receiveData();
                    //command = LOGON_LEVEL_2;
                    //data = buildData(strPass);
                }
                else if (iSecurityLevel == 1) {
                    skipCommandMessage(); // P0
                    // KV 06072004
                    authenticationCommand = LOGON_LEVEL_1;
                    authenticationData = buildData(pwd);

                }
                else if (iSecurityLevel == 2) {
                    byte[] key = receiveData();
                    authenticationCommand = LOGON_LEVEL_2;
                    if (encryptor != null) {
                        authenticationData = buildData(encryptor.encrypt(pwd, new String(key)));
                    } else {
                        authenticationData = buildData(pwd);
                    }
                }
                else {
                    throw new ProtocolConnectionException("FlagIEC1107Connection: invalid SecurityLevel", SECURITYLEVEL_ERROR);
                }

                authenticate();
                return;

            }
            catch (ProtocolConnectionException e) {
                if (e.getReason() == SECURITYLEVEL_ERROR) // KV 06072004
                {
                    throw e;
                } else if (iRetries++ >=iMaxRetries) {
                    throw new ProtocolConnectionException("Authentication error! Possibly wrong password! (error iMaxRetries), " + e.getMessage(), MAX_RETRIES_ERROR);
                } else {
                	if (!isNoBreakRetry()) {
                		sendBreak();
                		delay(DELAY_AFTER_BREAK);
                	}
                }
            }
            catch (IOException e) {
                throw new NestedIOException(e,"Authentication error! Possibly wrong password!");
            }
        }
    } // private prepareAuthentication() throws ProtocolConnectionException

    public void authenticate() throws NestedIOException,ProtocolConnectionException {

        int iRetries=0;
        try {
            if (iSecurityLevel != 0) {
                while(true) {
                    echoByteArrayOutputStream.reset();
                    sendRawCommandFrame(authenticationCommand,authenticationData); // logon using securitylevel
                    String resp = receiveString();
                    if (resp.compareTo("ACK")==0) {
                        break;
                    }
                    else if (resp.compareTo("B0")==0) {
                        throw new IOException(resp+" received");
                    }
                    else if (resp.compareTo("(ERR1)")==0) {
                        throw new IOException(resp+" received");
                    }
                    else if (resp.contains("(ER")) { // IskraEmeco errors...
                        throw new IOException(resp+" received");
                    }
                    if (iRetries++ >=iMaxRetries) {
                        throw new ProtocolConnectionException("Authentication error! Possibly wrong password! (error iRetries)");
                    }
                }
            }
        }
        catch (ProtocolConnectionException e) {
            if (e.getReason() == SECURITYLEVEL_ERROR) // KV 06072004
            {
                throw e;
            } else if (iRetries++ >=iMaxRetries) {
                throw new ProtocolConnectionException("Authentication error! Possibly wrong password! (error iMaxRetries), " + e.getMessage());
            } else {
                sendBreak();
                delay(DELAY_AFTER_BREAK);
            }
        }
        catch (IOException e) {
            throw new NestedIOException(e,"Authentication error! Possibly wrong password!");
        }

    } // public void authenticate(int iSecurityLevel)



    public byte[] dataReadout(String strIdent,String meterID) throws NestedIOException, ProtocolConnectionException, ProtocolException {
        byte[] data = null;
        if (!boolFlagIEC1107Connected) {
            try {
                data = doDataReadout(strIdent,meterID);
            }
            catch(ProtocolConnectionException e) {
                throw new ProtocolConnectionException("dataReadout() error "+e.getMessage(), e.getReason());
            }
        } // if (boolFlagIEC1107Connected==false)
        return data;
    } // public void connectMAC() throws HDLCConnectionException

    public byte[] doDataReadout(String strIdentConfig,String meterID) throws NestedIOException, ProtocolConnectionException, ProtocolException {
        int iRetries=0;
        while(true) {
            try {
                String str="/?"+meterID+"!\r\n";
                sendRawData(str.getBytes());
                String strIdentRaw = receiveIdent(strIdentConfig);
                // KV 28072004 remove rubbish at the beginning...
                String strIdent = new String(ProtocolUtils.getSubArray(strIdentRaw.getBytes(),strIdentRaw.indexOf('/')));
                byte[] ack={ACK,(byte)0x30,(byte)strIdent.charAt(4),(byte)0x30,(byte)0x0D,(byte)0x0A};
                sendRawData(ack);

                boolFlagIEC1107Connected=true;
                return (receiveRawData());
            }
            catch (StringIndexOutOfBoundsException e) {
                throw new ProtocolConnectionException("doDataReadout() error, "+e.getMessage());
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



    protected byte[] buildData(String strPass) {
        byte[] data = new byte[strPass.getBytes().length+2];
        int i=0;
        data[i++] = '(';
        for (int t=0;t<strPass.getBytes().length;t++) {
            data[i++] = strPass.getBytes()[t];
        }
        data[i++] = ')';
        return data;
    }

    public byte[] parseDataBetweenBrackets(byte[] buffer) throws ProtocolConnectionException {

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int state=0;
        for(int i = 0;i<buffer.length ; i++) {
            if (state == 0) {
                if (buffer[i] == (byte)'(') {
                    state = 1;
                }
            }
            else if (state == 1) {
                if (buffer[i] == (byte)')') {
                    state = 2;
                    break;
                }
                data.write((int)buffer[i]);
            }
        }
        if (state==2) {
            return data.toByteArray();
        } else {
            throw new ProtocolConnectionException("FlagIEC1107Connection, parseDataBetweenBrackets, error");
        }
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

    public void sendRawCommandFrame(byte[] command,byte[] rawdata) throws NestedIOException, ConnectionException, ProtocolException {
        doSendCommandFrame(command,rawdata,false);
    }

    public String sendRawCommandFrameAndReturn(byte[] command,byte[] rawdata) throws NestedIOException, ConnectionException, ProtocolException {
        return doSendCommandFrame(command,rawdata,true);
    }

    private String doSendCommandFrame(byte[] command,byte[] data, boolean returnResult) throws NestedIOException, ConnectionException, ProtocolException {
        int iLength,iRetries=0;
        int t,i;
        initTxBuffer(command.length+data.length+3); // KV 27102004
        byte bChecksum=0;
        String retVal=null;
        delay(lForceDelay);

        i=0;
        for (t = 0;t<command.length;t++) {
            getTxBuffer()[i++] = command[t];
        }
        getTxBuffer()[i++]=STX;
        for (t = 0;t<data.length;t++) {
            getTxBuffer()[i++] = data[t];
        }
        getTxBuffer()[i++]=ETX;
        if (getChecksumMethod()==0) {
            bChecksum = calcChecksum(getTxBuffer());
        } else if (getChecksumMethod()==1) {
            bChecksum = calcChecksumSDC(getTxBuffer());
        }

        getTxBuffer()[getTxBuffer().length-1]=bChecksum;

        flushEchoBuffer();


        if (command[0] == 'W') {
            while (true) {
                echoByteArrayOutputStream.reset();
                sendTxBuffer(); // KV 27102004
                resetTxBuffer(); // KV 27102004
                String resp = receiveString();
                if (resp.compareTo("ACK") == 0) {
                    break;
                }
                if (returnResult) {
                    if ((errorSignature != null) && (resp.contains(errorSignature))) {
                        retVal = resp;
                    }
                    break;
                }
                if (iRetries++ >= iMaxRetries) {
                    throw new ProtocolConnectionException("doSendCommandFrame() error iMaxRetries!", MAX_RETRIES_ERROR);
                }
            }
        }
        else if ((command[0] == 'R') || (command[0] == 'P')) {
            sendTxBuffer(); // KV 27102004
        }
        else {
            throw new ProtocolConnectionException("doSendCommandFrame() error unknown tag!");
        }

        if (DEBUG==1) {
            ProtocolUtils.outputHex( ((int)SOH)  &0x000000FF);
            for (i=0;i<getTxBuffer().length;i++) {
                ProtocolUtils.outputHex(((int) getTxBuffer()[i]) & 0x000000FF);
            }
            System.out.println();
        }

        return retVal;

    } // public void doSendCommandFrame(byte bCommand,byte[] data) throws ProtocolConnectionException

    public void skipCommandMessage() throws NestedIOException, ConnectionException {
        long lMSTimeout;
        int iNewKar;
        byte bState=0;

        if (isNoBreakRetry()) {
            lMSTimeout = System.currentTimeMillis() + 5000;
        } else {
            lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;
        }

        copyEchoBuffer();

        while(true) {
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) {
                    ProtocolUtils.outputHex(iNewKar);
                }

                if ((bState==0) && ((byte)iNewKar == SOH)) {
                    bState = 1;
                } else if ((bState==1) && ((byte)iNewKar == ETX)) {
                    bState = 2;
                } else if (bState==2) {
                    return;
                }

            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new ProtocolConnectionException("skipCommandMessage() timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // public void skipCommandMessage() throws ProtocolConnectionException

    private static final byte STATE_WAIT_FOR_START=0;
    private static final byte STATE_WAIT_FOR_LENGTH=1;
    private static final byte STATE_WAIT_FOR_DATA=2;
    private static final byte STATE_WAIT_FOR_END=3;
    private static final byte STATE_WAIT_FOR_CHECKSUM=4;

    public String receiveString() throws NestedIOException, ConnectionException, ProtocolException {
        return new String(receiveRawData());
    }

    public byte[] receiveData() throws NestedIOException, ConnectionException, ProtocolException {
        return parseDataBetweenBrackets(doReceiveDataRetry());
    }

    public byte[] receiveRawData() throws NestedIOException, ConnectionException, ProtocolException {
        return doReceiveDataRetry();
    }

    // KV 27102004
    private byte[] doReceiveDataRetry() throws NestedIOException, ConnectionException, ProtocolException {
        int retries=0;
        while(true) {
            try {
                return doReceiveData();
            }
            catch(FlagIEC1107ConnectionException e) {
                if ((retries++ < iMaxRetries) && (getTxBuffer() != null) && ((e.getReason() == CRC_ERROR) || (e.getReason() == NAK_RECEIVED) ||(e.getReason() == TIMEOUT_ERROR))) {
                    //System.out.println("KV_DEBUG> RETRY "+e.getReason());
                    try {
                        delayAndFlush(1000);
                        sendTxBuffer();
                    } catch (ConnectionException connEx) {
                        throw new ProtocolConnectionException(e.getMessage(), connEx.getReason());
                    }

                }
                else {
                    throw new ProtocolConnectionException(e.getMessage(), e.getReason());
                }
            }
        }
    }

    public byte[] doReceiveData() throws NestedIOException, ConnectionException, ProtocolException {
        long lMSTimeout,lMSTimeoutInterFrame;
        int iNewKar;
        int iState;
        int iLength=0;
        byte[] receiveBuffer;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream  = new ByteArrayOutputStream();
        byte calculatedChecksum=0;
        boolean end;

        // init
        iState=STATE_WAIT_FOR_START;
        end=false;
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
        resultArrayOutputStream.reset();
        byteArrayOutputStream.reset();

        if (DEBUG == 1) {
            System.out.println("doReceiveData(...):");
        }
        copyEchoBuffer();

        while(true) {
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) {
                    ProtocolUtils.outputHex(iNewKar);
                }

                switch(iState) {
                    case STATE_WAIT_FOR_START: {

                        if ((byte)iNewKar == SOH) {
                            iState = STATE_WAIT_FOR_END;
                        }
                        if ((byte)iNewKar == STX) {
                            iState = STATE_WAIT_FOR_END;
                        }
                        if ((byte)iNewKar == ACK) {
                            return ("ACK".getBytes());
                        }
                        // KV 27102004
                        if ((byte)iNewKar == NAK) {
                            if (state != STATE_SIGNON) {
                                throw new ProtocolConnectionException("doReceiveData() NAK received" + NAK_RECEIVED);
                            } else {
                                throw new ProtocolConnectionException("Probably wrong password! (NAK received)");
                            }
                        }

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
                        if (getChecksumMethod()==0) {
                            calculatedChecksum = calcChecksum(byteArrayOutputStream.toByteArray());
                        } else if (getChecksumMethod()==1) {
                            calculatedChecksum = calcChecksumSDC(byteArrayOutputStream.toByteArray());
                        }

                        if (calculatedChecksum == byteArrayOutputStream.toByteArray()[byteArrayOutputStream.toByteArray().length-1]) {
                            // remove head and tail from byteArrayOutputStream.toByteArray()...
                            byte[] data = new byte[byteArrayOutputStream.toByteArray().length-2];
                            for (int i=0;i<(byteArrayOutputStream.toByteArray().length-2);i++) {
                                data[i] = byteArrayOutputStream.toByteArray()[i];
                            }
                            try {
                                resultArrayOutputStream.write(data);
                            } catch ( IOException e) {
                                throw new ProtocolConnectionException("receiveStreamData(), IOException, "+e.getMessage());
                            }

                            if (end) {
                                return resultArrayOutputStream.toByteArray();
                            }

                            // init
                            iState=STATE_WAIT_FOR_START;
                            lMSTimeout = System.currentTimeMillis() + TIMEOUT;
                            lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                            byteArrayOutputStream.reset();
                            end=false;

                            sendRawData(ACK);
                        }
                        else {
                            // KV 27102004
                            throw new FlagIEC1107ConnectionException("doReceiveData() bad CRC error",CRC_ERROR);
                        }

                    } //break; // STATE_WAIT_FOR_CRC

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new ProtocolConnectionException("doReceiveData() response timeout error", TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - lMSTimeoutInterFrame > 0) {
                throw new ProtocolConnectionException("doReceiveData() interframe timeout error", TIMEOUT_ERROR);
            }
        } // while(true)

    } // public byte[] doReceiveData(String str) throws ProtocolConnectionException

    private static final byte STREAM_STATE_WAIT_FOR_START=0;
    private static final byte STREAM_STATE_WAIT_FOR_PACKET=1;
    private static final byte STREAM_STATE_WAIT_FOR_LENGTH=2;
    private static final byte STREAM_STATE_WAIT_FOR_DATA=3;
    private static final byte STREAM_STATE_WAIT_FOR_END=4;
    private static final byte STREAM_STATE_WAIT_FOR_CRC=5;


    // KV 27102004
    public void breakStreamingMode() throws NestedIOException, ConnectionException {
        sendRawData((byte)0x1B);
        delayAndFlush(3000);
    }

    public byte[] receiveStreamData() throws NestedIOException, ConnectionException {
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

        if (DEBUG == 1) {
            System.out.println("receiveStreamData(...):");
        }

        copyEchoBuffer();

        while(true) {
            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) {
                    ProtocolUtils.outputHex(iNewKar);
                }
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
                        if (count++ >= 1) {
                            state = STREAM_STATE_WAIT_FOR_LENGTH;
                        }
                    } break; // STREAM_STATE_WAIT_FOR_PACKET

                    case STREAM_STATE_WAIT_FOR_LENGTH: {
                        length = iNewKar&0xFF;
                        count = 0;
                        state = STREAM_STATE_WAIT_FOR_DATA;
                    } break; // STREAM_STATE_WAIT_FOR_LENGTH

                    case STREAM_STATE_WAIT_FOR_DATA: {
                        nettodata.write(iNewKar);
                        if (count++ >= length) {
                            state = STREAM_STATE_WAIT_FOR_END;
                        }
                    } break; // STREAM_STATE_WAIT_FOR_DATA

                    case STREAM_STATE_WAIT_FOR_END: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;

                        if ((byte)iNewKar == ETX) {
                            end = false;
                        }
                        else if ((byte)iNewKar == EOT) {
                            end = true;
                        }
                        else {
                            throw new ProtocolConnectionException("receiveStreamData() invalid end flag", PROTOCOL_ERROR);
                        }

                        state = STREAM_STATE_WAIT_FOR_CRC;
                        count=0;
                    } break; // STREAM_STATE_WAIT_FOR_END

                    case STREAM_STATE_WAIT_FOR_CRC: {
                        if (count++ >= 1) {
                            if (CRCGenerator.calcCRC(brutodata.toByteArray()) == 0) {
                                if (nettodata.toByteArray().length != (length+1)) {
                                    throw new ProtocolConnectionException("receiveStreamData() nettodata invalid length", PROTOCOL_ERROR);
                                }
                                try {
                                    alldata.write(nettodata.toByteArray());
                                } catch ( IOException e) {
                                    throw new ProtocolConnectionException("receiveStreamData(), IOException, "+e.getMessage(),PROTOCOL_ERROR);
                                }
                                if (end) {
                                    alldata.write(255); // 0xFF end of data toevoegen!
                                    return alldata.toByteArray();
                                }
                                else {
                                    state = STREAM_STATE_WAIT_FOR_START;
                                }
                            }
                            else {
                                throw new ProtocolConnectionException("receiveStreamData() bad crc", CRC_ERROR);
                            }
                        }

                    } break; // STREAM_STATE_WAIT_FOR_CRC

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new ProtocolConnectionException("receiveStreamData() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - lMSTimeoutInterFrame > 0) {
                throw new ProtocolConnectionException("receiveStreamData() interframe timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // public byte[] receiveStreamData(String str) throws ProtocolConnectionException

    public String receiveIdent(String str) throws NestedIOException, ConnectionException {
        long lMSTimeout;
        int iNewKar;
        String strIdent= "";
        byte[] convert=new byte[1];

        lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

        copyEchoBuffer();
        String convertstr;

        while(true) {

            if ((iNewKar = readIn()) != -1) {
                if (DEBUG == 1) {
                    ProtocolUtils.outputHex(iNewKar);
                }

                if ((byte)iNewKar==NAK) {
                    sendBreak();
                }

                convert[0] = (byte)iNewKar;
                convertstr = new String(convert);
                strIdent += convertstr;
                if (convertstr.compareTo("\\") == 0) {
                    strIdent += convertstr;
                }

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
                    if ((byte)iNewKar == 0x0A) {
                        return strIdent; // KV 16122003
                    }
                }
            } // if ((iNewKar = readIn()) != -1)

            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new ProtocolConnectionException("receiveIdent() timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // public void receiveIdent(String str) throws ProtocolConnectionException

    //***********************************************************************************
    // KV 27102004
    // TX buffer management
    private byte[] getTxBuffer() {
        return txBuffer;
    }

    private void resetTxBuffer() {
        txBuffer=null;
    }

    private void initTxBuffer(int length) {
        txBuffer=new byte[length];
    }

    private void sendTxBuffer() throws ConnectionException {
        sendOut(SOH);
        sendOut(txBuffer);
    }

    // KV 18092003
    HHUSignOn hhuSignOn=null;
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        this.hhuSignOn=hhuSignOn;
    }
    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }
    public int getIEchoCancelling() {
        return iEchoCancelling;
    }

    public int getChecksumMethod() {
        return checksumMethod;
    }

    public void setChecksumMethod(int checksumMethod) {
        this.checksumMethod = checksumMethod;
    }

    public boolean getReadoutEnabled(){
    	return readoutEnabled;
    }

    public void setReadoutenabled(boolean state){
    	readoutEnabled = state;
    }

    /**
     * Calculate modulo 256 checksum.
     * @param data byte array to calculate checksum on
     * @param length nr of bytes of data to calculate checksum
     * @param offset offset in byte array to calculate checksum
     * @throws com.energyict.dialer.connection.ConnectionException Thrown for communication related exceptions
     * @return byte checksum
     */
    protected byte calcChecksumSDC(byte[] data,int length, int offset) throws ConnectionException {
        return (doCalcChecksumSDC(data,length,offset));
    }

    /**
     * Calculate modulo 256 checksum.
     * @param data byte array to calculate checksum on
     * @param length nr of bytes of data to calculate checksum
     * @throws com.energyict.dialer.connection.ConnectionException Thrown for communication related exceptions
     * @return byte checksum
     */
    protected byte calcChecksumSDC(byte[] data,int length) throws ConnectionException {
        return (doCalcChecksumSDC(data,length,0));
    }

    /**
     * Calculate modulo 256 checksum.
     * @param data byte array to calculate checksum on
     * @throws com.energyict.dialer.connection.ConnectionException Thrown for communication related exceptions
     * @return byte checksum
     */
    protected byte calcChecksumSDC(byte[] data) throws ConnectionException {
        return (doCalcChecksumSDC(data,data.length,0));
    }

    private byte doCalcChecksumSDC(byte[] data, int length, int offset) throws ConnectionException {
        int checksum=0;
        if (length > (data.length - offset)) {
            throw new ConnectionException("Connection, doCalcChecksum, datalength=" + data.length + ", length=" + length + ", offset=" + offset);
        }
        for (int i=0;i<length-1;i++) {
            checksum = checksum + data[offset+i]&0x7f;
        }
        return (byte)checksum;
    }

    public void setNoBreakRetry(boolean noBreakRetry) {
		this.noBreakRetry = noBreakRetry;
	}
    public boolean isNoBreakRetry() {
		return noBreakRetry;
	}

    public int getMaxRetries() {
        return iMaxRetries;
    }

    public int getIEC1107Compatible() {
        return iIEC1107Compatible;
    }

    public void setIEC1107Compatible(int iIEC1107Compatible) {
        this.iIEC1107Compatible = iIEC1107Compatible;
    }

    public boolean isBoolFlagIEC1107Connected() {
        return boolFlagIEC1107Connected;
    }

    public void setBoolFlagIEC1107Connected(boolean boolFlagIEC1107Connected) {
		this.boolFlagIEC1107Connected = boolFlagIEC1107Connected;
	}

    public int getSecurityLevel() {
        return iSecurityLevel;
    }

    public void setSecurityLevel(int iSecurityLevel) {
        this.iSecurityLevel = iSecurityLevel;
    }

    @Override
    public ByteArrayOutputStream getEchoByteArrayOutputStream() {
        return echoByteArrayOutputStream;
    }

    public byte[] getAuthenticationCommand() {
        return authenticationCommand;
    }

    public void setAuthenticationCommand(byte[] authenticationCommand) {
        this.authenticationCommand = authenticationCommand;
    }

    public byte[] getAuthenticationData() {
        return authenticationData;
    }

    public void setAuthenticationData(byte[] authenticationData) {
        this.authenticationData = authenticationData;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }
} // public class FlagIEC1107Connection
