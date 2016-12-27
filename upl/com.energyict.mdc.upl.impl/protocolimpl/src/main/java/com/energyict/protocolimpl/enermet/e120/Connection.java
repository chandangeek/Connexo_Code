package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/** Connection encapsulates all the protocol layers:
 *  - application layer (message)
 *  - network layer (packet)
 *  - datalink layer (frame)
 *
 * The methods that are available:
 * - connect(String userId, String password)
 * - send(MessageType mt)
 * - setTime(ByteArray time)
 * - registerValue(int registerIndex, int historicLevel)
 * - seriesOnCount(int registerIndex, Date from, int valueCount )
 *
 * The senquence nr of the Packet:
 * (1) positive integer
 * (2) unique on virtual machine level
 *
 * @author fbo
 */

class Connection implements ProtocolConnection {

    private boolean dbg = false;

    /** authentication level is always 3, don't ask why */
    private static byte AUTHENTICATION_LEVEL = 3;

    /** timeout property in millisec */
    private long timeOut;
    /** nr retries */
    private int maxRetries = 3;

    private E120 e120;
    private OutputStream outputStream;
    private InputStream inputStream;

    /** unique sequence  */
    private static int SEQUENCE = 0;

    Connection(E120 e120, InputStream inputStream, OutputStream outputStream ){
        this.e120 = e120;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timeOut = e120.getInfoTypeTimeout();
        this.maxRetries = e120.getRetries();
    }

    /** Connect by authenticating
     *
     * @param userId    meter userId
     * @param pwd       meter password
     * @return          Response object
     * @throws IOException
     */
    DefaultResponse connect(String userId, String pwd) throws IOException {

        Request request =
            new Request(
                    new ByteArray()
                        .add(userId).add((byte)'\0')
                        .add(pwd).add((byte)'\0')
                        .add(AUTHENTICATION_LEVEL));

        Message message = new Message( MessageType.AUTHENTICATION, request);
        return (DefaultResponse)send(new Packet(message,true)).getMessage().getBody();

    }

    /** General purpose method for sending all kinds of messages.  Only the
     * messages without arguments are needed.
     *
     * @param messageType
     * @return response Message
     */
    Message send(MessageType messageType) throws IOException {
        Message message = new Message(messageType);
        return send( new Packet(message) ).getMessage();
    }

    /** Set time on meter
     *
     * @param time  bytearray with encoded time
     * @return      Response
     * @throws IOException
     */
    Message setTime(ByteArray time)
        throws IOException {

        Request r = new Request( time );
        Message request = new Message(MessageType.SET_TIME_NORMAL,r);
        return send(request);

    }

    /** Read 1 single register value.
     * @param registerIndex     address of register
     * @param historicLevel     point in history
     * @return                  Response
     * @throws IOException
     */
    Message registerValue(int registerIndex, int historicLevel)
        throws IOException {
        Request r = new Request(
            new ByteArray( (byte)registerIndex ).add((byte)historicLevel) );

        Message request = new Message(MessageType.REGISTER_VALUE,r);
        return send(request);
    }

    /** Read addres using method 18: "Series on count".
     *
     * @param registerIndex     address
     * @param from              start date of requested interval
     * @param valueCount        number of values to return
     * @return                  response Message
     * @throws IOException
     */
    Message seriesOnCount(int registerIndex, Date from, int valueCount )
        throws IOException{
        Request r = new Request(
            new ByteArray( (byte)registerIndex )
                .add( e120.getDataType().getTime().construct(from) )
                .add( (short)0 )
                .add( (short)valueCount ) );
        Message request = new Message(MessageType.SERIES_ON_COUNT,r);
        return send(request);

    }

    private Message send(Message request) throws IOException {
        debug(request.toString());

        Message response = send( new Packet(request) ).getMessage();

        debug(response.toString());
        return response;
    }

    /** send packet and return packet back */
    private Packet send(Packet request) throws IOException {
        Packet response = send( new Frame(request) ).getPacket();

        if( !response.isOk() )
            throw new IOException( createPacketErrorMsg(request, response) );

        return response;
    }

    /** try the send-receive senquence "nrRetries" times
     * @throws IOException */
    private Frame send(Frame request) throws IOException{
        int failCount = 0;
        while( failCount <= maxRetries ){
            try {
                return doTry(request);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch(Exception e) {
                e.printStackTrace(); // ignore & try again
            }
            failCount = failCount + 1;
        }
        throw new IOException("nr retries exceeded");

    }

    /** try/attempt a send-receive sequence once */
    private Frame doTry(Frame request) throws Exception {

        request.setSeq(nextSequece());
        /* send message */

        debug( "->", request.toByteArray());
        outputStream.write(request.toByteArray().getBytes());
        outputStream.flush();

        /* receive */
        long start = System.currentTimeMillis();
        int messageLength[] = null;
        ByteArray buffer = new ByteArray();

        while(messageLength==null || buffer.size()<messageLength[0]) {

            int available = inputStream.available();

            if (available > 0) {

                byte [] ba = new byte[available];
                inputStream.read(ba);
                buffer.add(ba);

                if(buffer.size() >= 8){
                    int len = buffer.intValue(0);
                    int lenChk = buffer.intValue(4);

                    if( len+lenChk == 0 )
                        messageLength = new int[]{ len+8 };
                    else
                        throw new IOException("length - length Check mismatch");
                }

            } else {
                Thread.sleep(100);
                if( isTimeout(start) )
                    throw new IOException( "Timeout Exception" );
            }
            available = inputStream.available();

        }
        debug("<-", buffer);
        /* done */
        return Frame.parse(e120, buffer);

    }

    /** calculate next sequence nr and return */
    private int nextSequece( ){
        if( SEQUENCE == Integer.MAX_VALUE )
            return SEQUENCE = 0;
        else
            return SEQUENCE = SEQUENCE + 1;
    }

    /** return true if the timeOut period has passed */
    private boolean isTimeout(long start) {
        return start + timeOut < System.currentTimeMillis();
    }

    private String createPacketErrorMsg(Packet request, Packet response ){

        String rqB = request.toByteArray().toHexaString(true);
        String rsB = response.toString(); //.toByteArray().toHexaString(true);

        return
            "Request packet " + request + " " + rqB + "\n" +
            "Response " + response.getStatCode() + " " + rsB;

    }

    private void debug(String msg){
        if( dbg ) System.out.println( msg );
    }

    private void debug(String msg, ByteArray ba){
        if( dbg ) System.out.println( msg + " " + ba.toHexaString(true) );
    }

    /* not implemented
     * @see ProtocolConnection#setHHUSignOn(HHUSignOn)
     */
    public void setHHUSignOn(HHUSignOn hhuSignOn) { }

    /* not implemented
     * @see ProtocolConnection#getHhuSignOn()
     */
    public HHUSignOn getHhuSignOn() { return null;  }


    /* not implemented
     * @see ProtocolConnection#disconnectMAC()
     */
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException { }


    /* not implemented
     * @see ProtocolConnection#connectMAC(String, String, int, String)
     */
    public MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) {
        return null;
    }


    /* not implemented
     * @see ProtocolConnection#dataReadout(String, String)
     */
    public byte[] dataReadout(String strID,String nodeId)
        throws NestedIOException, ProtocolConnectionException {
        return null;
    }

}

class Message {

    MessageType idepId;
    MessageBody body;

    private Message(){ }

    Message(MessageType mType){
        this(mType, new Request());
    }

    Message(MessageType mType, MessageBody body){
        this.idepId = mType;
        this.body = body;
    }

    boolean isOk( ){
        return ((Response)body).isOk();
    }

    MessageBody getBody(){
        return body;
    }

    ByteArray toByteArray(){
        return
            new ByteArray()
                .add(idepId.shortValue())
                .add(((Request)body).toByteArray());
    }

    public String toString(){
        return
                "Message [" +
                        idepId +
                        ", " +
                        body.toString() + "]";
    }

    static Message parse(E120 e120, ByteArray data){

        MessageType messageType = MessageType.get( data.shortValue(0) );

        Message m = new Message();
        m.idepId = messageType;

        m.body = messageType.parse(e120, data.sub(0));

        return m;

    }

}

class Packet {

    static final int ver = 01;

    /** communication opening */
    static final byte ODEP_ADDRESS_MODEM = 0x00;
    /** communication with integrated meter */
    static final byte ODEP_ADDRESS_METER = 0x01;

    int stat;
    StatCode statCode;
    int seq;
    int odepAddress;
    ByteArray msgData;
    Message message;

    private Packet(){}

    Packet(Message message){
        this(message,false);
    }

    Packet(Message message, boolean opening){
        this.message = message;
        this.odepAddress = opening ? ODEP_ADDRESS_MODEM : ODEP_ADDRESS_METER;
    }

    Packet(ByteArray data){
        this.msgData = data;
    }

    Packet setSeq(int seq){
        this.seq = seq;
        return this;
    }

    Packet setOdepAddress(int address){
        this.odepAddress = address;
        return this;
    }

    StatCode getStatCode(){
        return statCode;
    }

    boolean isOk( ){
        return StatCode.OK.equals(statCode);
    }

    Message getMessage(){
        return message;
    }

    ByteArray toByteArray( ){
        return new ByteArray( )
            .add((byte)ver)
            .add((byte)stat)
            .add(seq)
            .add((byte)odepAddress)
            .add(message.toByteArray());
    }

    static Packet parse(E120 e120, ByteArray byteArray){
        Packet packet = new Packet();

        packet.stat = byteArray.byteValue(1);
        packet.statCode = StatCode.get(byteArray.byteValue(1));
        packet.seq = byteArray.intValue(2);
        packet.odepAddress = byteArray.byteValue(6);
        packet.msgData = byteArray.sub(7);

        packet.message = Message.parse(e120, byteArray.sub(7));
        return packet;
    }

    public String toString(){
        return
                "Packet [" +
                        "ver 0x" + Integer.toHexString(ver) + ", " +
                        "stat 0x" + Integer.toHexString(stat) + ", " +
                        "seq 0x" + Integer.toHexString(seq) + ", " +
                        "addr 0x" + Integer.toHexString(odepAddress) + ", " +
                        message.toString() +
                        "]";
    }

}

class Frame {

    ByteArray data;
    Packet packet;

    Frame(Packet packet){
        this.packet = packet;
    }

    Frame(ByteArray data) {
        this.data = data;
    }

    ByteArray getData(){
        return data;
    }

    Packet getPacket( ){
        return packet;
    }

    Frame setPacket(Packet packet){
        this.packet = packet;
        return this;
    }

    Frame setSeq(int seq){
        packet.setSeq(seq);
        return this;
    }

    ByteArray toByteArray( ){
        ByteArray pba = packet.toByteArray();
        short crc = (short)CRCGenerator.calcCRCCCITTEnermet(pba.getBytes());
        int size = pba.add(crc).size();

        return
            new ByteArray( )
                .add(size)                      // LEN
                .add((int)(4294967296l - size)) // LEN_CHK
                .add(pba);                       // DATA
    }

    public String toString(){
        return
                "Frame [ " +
                        packet +
                        " ]";
    }

    static Frame parse(E120 e120, ByteArray byteArray) throws ParseException {

        int len = byteArray.sub(0,4).intValue(0);
        int lenChk = byteArray.sub(4,4).intValue(0);

        if( (len + lenChk) != 0) {
            throw new ParseException("LEN and LEN_CHK mismatch");
        }

        ByteArray fBody = byteArray.sub(8, len-2);
        ByteArray crc = byteArray.sub(len+6, 2);

        if( !crcOk( fBody, crc) ){
            throw new ParseException("Crc exception");
        }

        Frame f = new Frame( byteArray.sub(8, len-2) );
        f.packet = Packet.parse(e120, fBody);

        return f;
    }

    static boolean crcOk(ByteArray body, ByteArray crc) {
        return (crc.shortValue(0) & 0xffff) == CRCGenerator.calcCRCCCITTEnermet(body.getBytes());
    }

    /* binary test messages, only for testing */

    // authentication response
    static byte [] m1 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xF4,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x26, (byte)0x93,     // ODEP seq
        (byte)0x00,                                         // ODEP addr

        (byte)0x00, (byte)0x00,                             // IDEP id
        (byte)0x00,                                         // IDEP ack

        (byte)0x61, 0x10                                    // ODEP crc

    };

    // device id response
    static byte [] m2 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1C,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xE4,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x83,     // ODEP seq
        (byte)0x00,                                         // ODEP addr

        (byte)0x00, (byte)0x05,                             // IDEP id
        (byte)0x00,                                         // IDEP ack
        (byte)0x33, (byte)0x35, (byte)0x31, (byte)0x32,
        (byte)0x37, (byte)0x37, (byte)0x30, (byte)0x30,
        (byte)0x30, (byte)0x32, (byte)0x36, (byte)0x31,
        (byte)0x38, (byte)0x32, (byte)0x35, (byte)0x00,     // IDEP string

        (byte)0xFF, (byte)0x48                              // ODEP crc

    };

    // get time response
    static byte [] m3 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x13,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xED,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x8D,     // ODEP seq
        (byte)0x01,      // ODEP addr (1 for communicate with integrated meter)

        (byte)0x00, (byte)0x07,                             // IDEP id
        (byte)0x00,                                         // IDEP ack
        (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x0A,
        (byte)0x0B, (byte)0x0C, (byte)0x37,

        // IDEP Time (07 D5 = year, 06 = month, 0A = day, 0B = hour, 0C = minutes, 37 = seconds)

        (byte)0xDD, (byte)0x3E                              // ODEP crc

    };

    // set time response
    static byte [] m4 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xF4,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x92,     // ODEP seq
        (byte)0x01,                                         // ODEP addr

        (byte)0x00, (byte)0x08,                             // IDEP id
        (byte)0x00,                                         // IDEP ack

        (byte)0x76, (byte)0xA8                              // ODEP crc

    };

    // register value response
    static byte [] m5 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1B,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xE5,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9A,     // ODEP seq
        (byte)0x01,                                         // ODEP addr

        (byte)0x00, (byte)0x14,                             // IDEP id
        (byte)0x00,                                         // IDEP ack
        (byte)0x01,                                         // IDEP register index
        (byte)0x00,                                         // IDEP Historic value
        (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xE1,
        (byte)0x0A, (byte)0x53, (byte)0x07, (byte)0xD5,
        (byte)0x06, (byte)0x0A, (byte)0x0B, (byte)0x1F,
        (byte)0x19,                                         // IDEP Register value

        (byte)0x69, (byte)0x65                              // ODEP crc
    };

    //second example (already ended measruing period)
    static byte[] m6 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1B,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xE5,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA3,     // ODEP seq
        (byte)0x01,                                         // ODEP addr

        (byte)0x00, (byte)0x14,                             // IDEP id
        (byte)0x00,                                         // IDEP ack
        (byte)0x01,                                         // IDEP register index
        (byte)0x04,                                         // IDEP Historic value
        (byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xD1,
        (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5,
        (byte)0x06, (byte)0x0A, (byte)0x07, (byte)0x00,
        (byte)0x00,
                                        // IDEP Register value 2005 10th june 7:0:0

        (byte)0x83, (byte)0xCB       // ODEP crc
    };



    static byte[] m7 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x47,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFE, (byte)0xB9,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x52,     // ODEP seq
        (byte)0x01,                                         // ODEP addr
        (byte)0x00, (byte)0x18,                             // IDEP id
        (byte)0x00,                                         // IDEP ack
        (byte)0x01,                                         // IDEP Register index
        (byte)0x00, (byte)0x18,                             // IDEP Value count
        (byte)0x00, (byte)0x00, (byte)0xF4, (byte)0x4E, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x17, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0xFA, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x16, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0xD3, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x15, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0xB9, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x14, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x90, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x13, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x56, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x12, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x38, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x11, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x1F, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x10, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x10, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0F, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x06, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0E, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF2, (byte)0xD8, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0D, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF2, (byte)0x90, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0C, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF2, (byte)0x90, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0B, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0xF8, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0A, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0x67, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x09, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0x67, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x08, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0x28, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x07, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0xE6, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0xBE, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x05, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x04, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x03, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x02, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x01, (byte)0x00, (byte)0x00,  // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00,  // IDEP Register value

        (byte)0xAE, (byte)0x0F       // ODEP crc

    };


    static byte[] m8 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x47,     // ODEP len
        (byte)0xFF, (byte)0xFF, (byte)0xFE, (byte)0xB9,     // ODEP len_chk
        (byte)0x01,                                         // ODEP ver
        (byte)0x00,                                         // ODEP stat
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x4E,     // ODEP seq
        (byte)0x01,                                         // ODEP addr

        (byte)0x00, (byte)0x18,                             // IDEP id
        (byte)0x00,                                         // IDEP ack
        (byte)0x01,                                         // IDEP Register index
        (byte)0x00, 0x18,                                   // IDEP Value count

        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x01, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x02, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x03, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0x8D, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x04, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0xBE, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x05, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF0, (byte)0xE6, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x06, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0x28, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x07, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0x67, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x08, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0x67, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x09, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF1, (byte)0xF8, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0A, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF2, (byte)0x90, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0B, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF2, (byte)0x90, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0C, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF2, (byte)0xD8, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0D, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x06, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0E, (byte)0x00, (byte)0x00, // IDEP R, (byte)0xgister value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x10, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x0F, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x1F, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x10, (byte)0x00, (byte)0x00, // IDEP, (byte)0xRegister value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x38, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x11, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x56, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x12, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0x90, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x13, (byte)0x00, (byte)0x00, // IDEP, (byte)0xRegister value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0xB9, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x14, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0xD3, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x15, (byte)0x00, (byte)0x00, // IDEP , (byte)0xegister value
        (byte)0x00, (byte)0x00, (byte)0xF3, (byte)0xFA, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x16, (byte)0x00, (byte)0x00, // IDEP Register value
        (byte)0x00, (byte)0x00, (byte)0xF4, (byte)0x4E, (byte)0x0A, (byte)0x41, (byte)0x07, (byte)0xD5, (byte)0x06, (byte)0x06, (byte)0x17, (byte)0x00, (byte)0x00,  // IDEP Register value

        (byte)0x62, (byte)0xE9       // ODEP crc

    };

}