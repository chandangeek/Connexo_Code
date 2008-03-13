package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.NestedIOException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMIOException;

/**
 * For information regarding Opus protocol, read manufacturer documentation:
 * Protocol for communication between an instation and outstation.
 * 
 * @author fbo
 */

public class OpusConnection extends Connection {

    /* protocol properties */
    private String nodeId = null;
    private String password = null;
    private int maxRetry = 0;
    private long forceDelay = 0;
    private long delayAfterFail = 0;
    private long timeout = 0;
    private TimeZone timeZone = null;
    private Logger logger = null;
   
    private int errorCount = 0; 
    private boolean canWakeUp = false;
    private boolean canInstruct = false;

    /**
     * Just a constructor, move along
     * 
     * @param inputStream
     * @param outputStream
     * @param ppm
     * @throws ConnectionException
     */
    public OpusConnection(InputStream inputStream, OutputStream outputStream,
            PPM ppm) throws ConnectionException {

        super(inputStream, outputStream, 0, 0);
        this.nodeId = ppm.getNodeId();
        this.password = ppm.getPassword();
        this.maxRetry = ppm.getMaxRetry();
        this.forceDelay = ppm.getForceDelay();
        this.delayAfterFail = ppm.getDelayAfterFail();
        this.timeout = ppm.getTimeout();
        this.timeZone = ppm.getTimeZone();
        this.logger = ppm.getLogger();

    }
    
    public int getErrorCount(){
        return errorCount;
    }

    /* ___________________ Communication Sequences _________________________ */

    public byte[] wakeUp() throws IOException {

        delay(forceDelay);
        copyEchoBuffer();
        flushEchoBuffer();

        byte[] identification = null;

        sendOut(createWakeUp(nodeId).toByteArray());
        identification = receive(CtrlChar.ACK);
        return identification;

    }

    public OpusResponse readRegister(String dataIdentity, int packetNr,
            int dayNr, int nrPackets, boolean isProfileData) throws NestedIOException,
            ConnectionException, IOException {

        ReadCommand command = 
            new ReadCommand(dataIdentity, packetNr, dayNr, nrPackets, 
            isProfileData);
        
        doCommand(command);
        return command.getOpusResponse();

    }

    public OpusResponse writeRegister(String dataIdentity, byte[] data)
            throws NestedIOException, ConnectionException, IOException {

        WriteCommand command = new WriteCommand(dataIdentity, data);
        doCommand(command);
        return command.getOpusResponse();

    }

    abstract class OpusCommand {
        
        abstract void execute() throws ConnectionException, IOException;
        abstract OpusResponse getOpusResponse();
        abstract void clearOpusResponse();
        
        /* Check Message/Packet for
         *  - outstation nr (=nodeId) matches configured outstation nr 
         *  - data identity matches requested data identity
         *  - packet nr matches expected packet nr  
         */ 
        void check(byte [] rsp, String dataId, int pNr) throws IOException {
            
            int offset = (rsp[0] == CtrlChar.SOH.byteValue) ? 1 : 0; 
            
            String rcvd     = toString(rsp, offset, 3);  
            String xpctd    = OpusConnection.this.nodeId;
            
            if( !rcvd.equals(xpctd) ) {
                String msg = "Received NodeId incorrect: " + rcvd;
                logger.severe(msg);
                throw new IOException(msg);
            }
            
            rcvd            = toString(rsp, (offset+3), 3);
            xpctd           = dataId;
            
            if( !rcvd.equals(xpctd) ) {
                String msg = "Received DataIdentity incorrect: " + rcvd;
                logger.severe(msg);
                throw new IOException(msg);
            }
            
            rcvd            = toString(rsp, (offset+6), 3);
            int rcvdPNr     = Integer.parseInt(rcvd);
            
            if( rcvdPNr != pNr ) {
                String msg = "Received PacketNr not matching: " + rcvd + " " + pNr;
                logger.severe(msg);
                throw new IOException(msg);
            }
            
        }
        
        /* cut a peace from a byte array and convert to a String */
        String toString(byte[] ba, int start, int length ) {
            byte [] rslt = new byte[length];
            System.arraycopy(ba, start,rslt, 0, length);
            return new String(rslt);
        }
        
    }

    class ReadCommand extends OpusCommand {

        OpusResponse opusResponse = null;
        String dataIdentity = null;
        String packetNumber = "001";
        String dayNumber = "000";
        int nrPackets = 0;
        boolean isProfileData = false;
        
        public ReadCommand(String dataIdentity) {
            this.dataIdentity = dataIdentity;
        }

        public ReadCommand(String dataIdentity, int packetNr, int dayNr,
                int nrPackets, boolean isProfileData) {
            this(dataIdentity);
            this.packetNumber = buildZeroLeadingString(packetNr, 3);
            this.dayNumber = buildZeroLeadingString(dayNr, 3);
            this.nrPackets = nrPackets;
            this.isProfileData = isProfileData;
        }

        /* Read sequence:
         * ->   Wake Up
         * <-   <AK>
         * ->   Read Instruction Message
         * <-   <AK>
         * ->   <STX>
         * <-   <SOH> Definition Message
         * ->   <AK>
         * <-   <SOH> Data Message x <O
         * ->   <AK>
         * ->   <EOT>
         *
         * The end of the message is the difficult part.  If the protocol knows
         * the end is reached, it sends an End of Transmission <EOT>
         * Else it sends and <ACK>.
         *
         * In case of the Profile data, the result has a dynamic lenght.  
         * If the end of the days has been reached, the meter sends an <ETX>
         */
        public void execute() throws ConnectionException, IOException {

            int packetCount = 1;
            boolean endOfRegister = false;
            
            MessageComposer iMessage = 
                createInstruction( dataIdentity, packetNumber, CtrlChar.READ, dayNumber);
            sendOut(iMessage);

            sendOut(CtrlChar.STX.byteValue);
            byte[] rsp = receiveMessage(null);
            checkDefinition(rsp);
            opusResponse.definitionMessage = rsp;
            
            if (opusResponse.isDefinitionMessageValid()) {
                
                sendOut(CtrlChar.ACK.byteValue);
                receiveCtrlChar();
                
                rsp = receiveMessage(CtrlChar.SOH);
                check(rsp, dataIdentity, 1);
                opusResponse.addDataMessage(rsp);
                
                while ( packetCount < nrPackets && !endOfRegister ) {    
                    
                    sendOut(CtrlChar.ACK.byteValue);
                    
                    if( receiveCtrlChar() == CtrlChar.SOH ) { 
                        
                        // if SOH, another part is comming 
                        
                        rsp = receiveMessage(CtrlChar.SOH);
                        check(rsp, dataIdentity, packetCount+1);
                        opusResponse.addDataMessage(rsp);
                        
                        packetCount = packetCount + 1;
                        
                    } else {                                  
                       // if something else (eg <ETX>) the transmission is done 
                       endOfRegister = true;
                    }
                }
            
                // in the end send an <EOT>, then start new command
                sendOut(CtrlChar.EOT.byteValue);
            
            }
            
        }
        
        /* Z field in definition message must be "R" */
        private void checkDefinition(byte [] rsp) throws IOException {
        
            String rcvd = toString(rsp, 11, 1); 
            if( ! rcvd.equals("R") ) {
                String msg = "Received Z field=" + rcvd + " (expected=R)";
                logger.severe(msg);
                throw new IOException(msg);
            }
            
            check(rsp, dataIdentity, 0);
            
        }

        public OpusResponse getOpusResponse() {
            return opusResponse;
        }

        public void clearOpusResponse() {
            opusResponse = new OpusResponse(timeZone, isProfileData);
        }

    }

    class WriteCommand extends OpusCommand {

        OpusResponse opusResponse = null;

        String dataIdentity = null;

        String packetNumber = "000";
        /* ASK ROY !! */
        String dayNumber = "851";

        byte[] data;

        public WriteCommand(String dataIdentity, byte[] data) {
            this.dataIdentity = dataIdentity;
            this.data = data;
        }

        public void execute() throws ConnectionException, IOException {

            MessageComposer iMessage = createInstruction(dataIdentity,
                    packetNumber, CtrlChar.WRITE, dayNumber);
            sendOut(iMessage);

            sendOut(CtrlChar.STX.byteValue);
            byte[] receive = receiveMessage(null);
            checkDefinition(receive);
            opusResponse.definitionMessage = receive;

            sendOut(CtrlChar.ACK.byteValue);
            
            receive(CtrlChar.STX );
            
            MessageComposer message = createDataMessage(dataIdentity,
                    "001", data);
            sendOut(message);

            sendOut(CtrlChar.EOT.byteValue);

        }
        
        /* Z field in definition message must be "W" */
        private void checkDefinition(byte [] rsp) throws IOException {
        
            String rcvd = toString(rsp, 11, 1); 
            if( ! rcvd.equals("W") ) {
                String msg = "Received Z field=" + rcvd + " (expected=W)";
                logger.severe(msg);
                throw new IOException(msg);
            }
            
            check(rsp, dataIdentity, 0);
            
        }

        public OpusResponse getOpusResponse() {
            return opusResponse;
        }

        public void clearOpusResponse() {
            opusResponse = new OpusResponse(timeZone, false);

        }
    }

    private void doCommand(OpusCommand command) throws IOException {

        int tries = 0;
        boolean done = false;
        
        while ( tries < maxRetry && !done) {
            tries += 1;
            command.clearOpusResponse();
            try {
                // KV 22072005 changed to use setter & getter! 
                command.getOpusResponse().setIdentificationMessage(wakeUp());
                canWakeUp = true;
            } catch( NestedIOException nex ){
                throw nex;
            } catch (IOException ex) { // first time ignore
                logger.info( "IOException handle in command.getOpusResponse() , try nr " + tries );
                errorCount ++;
                if( tries == maxRetry ) {
                    String msg = "Error sending wake up: ";
                    if( canWakeUp ){
                        msg += "Connection broken.";
                    } else {
                        msg += "Probably node id is wrong ";
                        msg += "(or the connection could not be established ).";
                    }
                    throw new PPMIOException( msg );
                } else {
                    delay( delayAfterFail );
                    continue;
                }
            } catch( NumberFormatException nfex ){
                logger.info( "NumberFormatException handle in command.getOpusResponse() , try nr " + tries );
                errorCount ++;
                if( tries == maxRetry ) {
                    String msg = "Error sending wake up: NumberFormatException";
                    throw new PPMIOException( msg );
                } else {
                    delay( delayAfterFail );
                    continue;
                }
            }
            try {
                command.execute();
                canInstruct = true;
                done = true;
            } catch( NestedIOException nex ){
                throw nex;
            } catch (IOException ex) { // first time ignore
                logger.info( "IOException handle in command.execute() , try nr " + tries );
                errorCount ++;
                if( tries == maxRetry ) {
                    String msg = "Error sending instruction: ";
                    if( canInstruct ) {
                        msg += "Connection is broken.";
                    } else {
                        msg += "Password is wrong.";
                    }
                    throw new PPMIOException( msg );   
                } else {
                    delay( delayAfterFail );
                }
            } catch( NumberFormatException nfex ){
                logger.info( "NumberFormatException handle in command.execute() , try nr " + tries );
                errorCount ++;
                if( tries == maxRetry ) {
                    String msg = "Error sending instruction: NumberFormatException";
                    throw new PPMIOException( msg );
                } else {
                    delay( delayAfterFail );
                    continue;
                }
            }
        }

    }

    public void sendOut(MessageComposer aMessage) throws ConnectionException,
            IOException {
                
        sendOut(aMessage.toByteArray());

        if( receiveCtrlChar() != CtrlChar.ACK )
            throw new IOException();
            
    }

    /**
     * low level receive function, stops at ETX
     * 
     * @throws IOException
     */
    public byte[] receive() throws IOException {
        return receive(CtrlChar.ETX);
    }

    /**
     * low level receive function, stops at endCtrlChar
     * 
     * @throws IOException
     */
    public byte[] receive(CtrlChar endCtrlChar) throws IOException {

        long startMilliseconds = System.currentTimeMillis();
        long currentMilliseconds = System.currentTimeMillis();

        long timediff = 0;
        int input;
        //String result = "";// KV 22072005 unused code
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        copyEchoBuffer();

        do {
            input = readIn();
            if (input != -1)
                bao.write(input);
            currentMilliseconds = System.currentTimeMillis();
            timediff = currentMilliseconds - startMilliseconds;
            if (timediff > timeout)
                throw new IOException("connection timeout");
        } while (input != endCtrlChar.byteValue);

        return bao.toByteArray();
    }

    /**
     * low level receive function, receives single CtrlChar
     * This can be <ACK>, <NAK>, <EOT>, <SOH>
     * 
     * @throws IOException
     */
    public CtrlChar receiveCtrlChar() throws IOException {

        int tries = 0, input = 0;
        CtrlChar result = null;
        copyEchoBuffer();

        do {
            input = readIn();

            if (input == CtrlChar.ACK.byteValue)
                result = CtrlChar.ACK;
            if (input == CtrlChar.NAK.byteValue)
                result = CtrlChar.NAK;
            if (input == CtrlChar.EOT.byteValue)
                result = CtrlChar.EOT;
            if (input == CtrlChar.SOH.byteValue)
                result = CtrlChar.SOH;
            tries++;
        } while (tries < 100 && result == null);
        if (tries == 100)
            throw new IOException("No meter response");
        return result;

    }

    /** receive function for messages, does a checksum-check */
    private byte[] receiveMessage(CtrlChar startChar) throws IOException {
        int retries = 0;
        byte[] message = null;
        boolean checksumOk = false;

        message = receive();
        if (startChar == null) {
            checksumOk = isCheckSumOk(message);
        } else {
            byte conMessage[] = new byte[message.length + 1];
            conMessage[0] = startChar.byteValue;
            System.arraycopy(message, 0, conMessage, 1, message.length);
            checksumOk = isCheckSumOk(conMessage);
        }
        if (checksumOk)
            return message;

        while (retries < maxRetry && !checksumOk) {
            retries += 1;
            if (retries == maxRetry)
                throw new IOException("receive failed, max retry exceeded");
            sendOut(CtrlChar.NAK.byteValue);
            message = receive();
            checksumOk = isCheckSumOk(message);
            if (checksumOk)
                return message;

        }
        throw new IOException("receive failed, max retry exceeded");
    }

    /** Opus checksum: add up all characters of a message, modulo 256 */
    private String calc0pusChecksum(MessageComposer m)
            throws ConnectionException {
        byte[] data = m.toByteArray();
        int checksum = calcOpusCheckSum(data, 0, data.length);
        char[] csa = Integer.toString(checksum).toCharArray();
        char[] ba = { '0', '0', '0' };
        System.arraycopy(csa, 0, ba, 3 - csa.length, csa.length);
        return new String(ba);
    }

    /** Opus checksum: add up all characters of a message, modulo 256 */
    private int calcOpusCheckSum(byte[] data, int offset, int length) {
        int checksum = 0;
        for (int i = offset; i < length; i++) {
            checksum += data[i] & 0xff;
            checksum = checksum & 0xff; // modulo
        }
        return checksum;
    }

    private boolean isCheckSumOk(byte[] input) {
        byte[] content = new byte[input.length - 4];
        byte[] checksum = new byte[3];
        System.arraycopy(input, 0, content, 0, input.length - 4);
        System.arraycopy(input, input.length - 4, checksum, 0, 3);

        String bcd = PPMUtils.parseBCDString(checksum);
        int inputCheck = Integer.parseInt(bcd);

        int receiveCheck = calcOpusCheckSum(content, 0, content.length);

        if (receiveCheck == inputCheck)
            return true;
        else
            return false;
    }



    String buildZeroLeadingString(int packetID, int length) {
        String str = Integer.toString(packetID);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length())
            for (int i = 0; i < (length - str.length()); i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }

    /////////////////////

    MessageComposer createWakeUp(String nodeId) {
        MessageComposer m = new MessageComposer().add(CtrlChar.CR).add(
                CtrlChar.SOH);
        m.add(nodeId);
        return m;
    }

    MessageComposer createId(String outstationNumber) {
        MessageComposer m = new MessageComposer();
        return m;
    }

    protected MessageComposer createInstruction(String dataIdentity)
            throws ConnectionException {
        return createInstruction(dataIdentity, nodeId, CtrlChar.READ, "000");
    }

    protected MessageComposer createInstruction(String dataIdentity,
            String packetNr, CtrlChar Z, String dayOffset)
            throws ConnectionException {

        MessageComposer m = new MessageComposer();

        m.add(CtrlChar.SOH).add(nodeId).add(dataIdentity).add(packetNr);

        m.add(CtrlChar.SHARP).add(Z);

        m.add(CtrlChar.SHARP).add(dayOffset);
        m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
        m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
        m.add(CtrlChar.SHARP);

        m.add(password).add(CtrlChar.SHARP).add(password);

        m.add(CtrlChar.SHARP).add(CtrlChar.SHARP);
        //byte[] r = m.toByteArray();// KV 22072005 unused code
        m.add(calc0pusChecksum(m));

        m.add(CtrlChar.CR);
        return m;
    }

    protected MessageComposer createDataMessage(String dataIdentity,
            String packetNr, byte[] data) throws ConnectionException {
        MessageComposer m = new MessageComposer();

        m.add(CtrlChar.SOH).add(nodeId).add(dataIdentity).add(packetNr);

        m.add(CtrlChar.SHARP).add(data);

        m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
        m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
        m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
        m.add(CtrlChar.SHARP).add("0");

        m.add(CtrlChar.SHARP).add(CtrlChar.SHARP);
        //byte[] r = m.toByteArray(); // KV 22072005 unused code
        m.add(calc0pusChecksum(m));

        m.add(CtrlChar.CR);
        return m;
    }

    class MessageComposer {

        ByteArrayOutputStream content = new ByteArrayOutputStream();

        byte[] add(byte b) {
            content.write(b);
            return content.toByteArray();
        }

        MessageComposer add(CtrlChar ctrlChar) {
            content.write(ctrlChar.byteValue);
            return this;
        }

        MessageComposer add(byte[] b) {
            content.write(b, 0, b.length);
            return this;
        }

        MessageComposer add(int i) {
            byte[] data = new byte[2];
            ProtocolUtils.val2BCDascii(i, data, 0);
            content.write(data, 0, 1);
            return this;
        }

        MessageComposer add(String aString) {
            char[] c = aString.toCharArray();
            for (int i = 0; i < c.length; i++)
                content.write(c[i]);
            return this;
        }

        byte[] toByteArray() {
            return this.content.toByteArray();
        }

        public String toString() {
            return toHexaString();
        }

        public String toHexaString() {
            StringBuffer result = new StringBuffer();
            byte[] contentArray = content.toByteArray();
            for (int i = 0; i < contentArray.length; i++)
                result.append(PPMUtils.toHexaString(contentArray[i]) + " ");
            return result.toString();
        }

    }

}