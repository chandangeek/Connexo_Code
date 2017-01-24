package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.protocolimpl.base.CRCGenerator;


/**
 * The class Frame is used for 2 purposes:
 * - to create new frames from the master, and set all required fields
 * - to parse frames comming from the slave (aka meter)
 * 
 * @author fbo
 */

class Frame {

    static final int SLAVE_TO_MASTER = 0x80;
    static final int ACK_NACK = 0x40;
    static final int ENABLE_ACK = 0x20;
    
    private int control;
    private int length;
    private int sourceAddress;
    private int destinationAddress;
    private int transactionCode;
    
    private ByteArray data;

    /** create a dataframe */
    Frame( ByteArray data ){
        this.data = data;
        this.length = 7;
        if( data != null )
            this.length = this.length + data.size();
        this.setIsData();
    }

    /** create an ack/nack frame */
    private Frame( ){}
    
    /** Parse an Assembly into a Frame */
    public static Frame parse( Assembly a ){
        Frame rslt = new Frame();
        a.byteValues( 4 ); // skip first 4

        rslt.control = a.unsignedIntValue(1);
        rslt.length = a.unsignedIntValue(1);
        
        byte []b = a.byteValues(2);
        rslt.sourceAddress = ((b[1]&0xff) << 8) + (b[0]&0xff);
        
        b = a.byteValues(2);
        rslt.destinationAddress = ((b[1]&0xff) << 8 ) + (b[0]&0xff);
        
        rslt.transactionCode = a.unsignedIntValue(1);
        
        a.byteValue();   // reserved 
        
        if( rslt.length > 7 ) {
            rslt.data = a.getBytes( rslt.length - 7 );
        }
        
        return rslt;
    }
    
    /** Frame Control, direction of frame is from master to slave 
     * @return this Frame
     */
    Frame setIsMasterToSlave( ){
        this.control = this.control & ~SLAVE_TO_MASTER;
        return this;
    }
    
    /** Frame Control, direction of the frame is from slave to master 
     * @return this Frame
     */
    Frame setIsSlaveToMaster( ){
        this.control = this.control | SLAVE_TO_MASTER;
        return this;
    }
    
    boolean isSlaveToMaster( ){
        return (control & SLAVE_TO_MASTER) > 0;
    }
    
    /** Frame Control, contains data 
     * @return this Frame
     */
    Frame setIsData( ){
        this.control = this.control &~ ACK_NACK;
        return this;
    }
    
    /** Frame Control, contains ack or nack 
     * @return this Frame
     */
    Frame setIsAckNak( ){
        this.control = this.control | ACK_NACK;
        return this;
    }
    
    boolean isAckNak( ){
        return (control & ACK_NACK) > 0;
    }

    boolean isData( ){
        return (control & ACK_NACK) == 0;
    }
    
    /** Frame Control, set to disable or nak 
     * @return this Frame
     */
    Frame setDisableNak( ){
        this.control = this.control &~ ENABLE_ACK;
        return this;
    }
    
    /** Frame Control, set to enable or ack 
     * @return this Frame
     */
    Frame setEnableAck( ){
        this.control = this.control | ENABLE_ACK;
        return this;
    }
    
    boolean isAckNakEnable( ) {
        return (this.control & ENABLE_ACK) > 0;
    }
    
    
    /** Address of the frame responder, 0 is reserved, 0xffff is for 
     * broadcasting to slave devices, slave devices have an address between
     * 1 and 9999
     * @return address of the frame responder
     */
    int getDestinationAddress() {
        return destinationAddress;
    }

    /** Address of the frame responder, 0 is reserved, 0xffff is for 
     * broadcasting to slave devices, slave devices have an address between
     * 1 and 9999
     * @param address of the frame responder
     */
    Frame setDestinationAddress(int destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    /** 
     * @return SourceAddress, address of frame initiator (1-9999)
     */
    int getSourceAddress() {
        return sourceAddress;
    }

    /** 
     * @param sourceAddress defines address of frame initiator (1-9999)
     */
    Frame setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }

    /** 
     * @return transactionCode
     */
    int getTransactionCode() {
        return transactionCode;
    }
    
    Frame setTransactionCode( int code ) {
        transactionCode = code;
        return this;
    }

    /**
     * @param isFirst when true set firstframe to 1, when false set to 0
     * @return this Frame
     */
    Frame setIsFirstFrame( boolean isFirst ){
        if( isFirst )
            transactionCode = transactionCode |  0x40;
        else
            transactionCode = transactionCode &~ 0x40;
        return this;
    }
    
    /**
     * @return true if firstframe bit is 1, zero otherwise
     */
    boolean isFirstFrame( ){
        return (transactionCode & 0x40) > 0;
    }
    
    /**
     * @param counter
     * @return this Frame
     */
    Frame setCounter( int counter ){
        transactionCode = transactionCode & 0xf0;
        transactionCode = transactionCode | counter;
        return this;
    }
    
    /**
     * @return the counter in the transactioncode field
     */
    int getCounter( ) {
        return transactionCode & 0x1F;
    }
    
    /**
     * @return 
     */
    ByteArray getData() {
        return data;
    }
    
    /**
     * @return the frame serialised into a ByteArray
     */
    ByteArray toByteArray( ){
        
        ByteArray rslt = new ByteArray();
        
        rslt.add( new byte[] { 0x00,0x00 } );

        rslt.add( new byte[] { 
                0x14,                       // frame sync 
                (byte)0xac,                 // frame format
                (byte)control });           // control
        
        if( data != null )                  // length
            rslt.add( new byte[] { (byte)(7+data.size() ) } );
        else
            rslt.add( new byte[] { (byte)7 } );
        
        rslt.add( new byte[] {
                (byte)(sourceAddress & 0x00FF),
                (byte)(sourceAddress >> 8 ),
                (byte)(destinationAddress & 0x00FF),
                (byte)(destinationAddress >> 8 ),
                (byte)transactionCode,
                0,                          // reserved 
            } );
        
        if( data != null )
            rslt.add(data);
        
        int crc = CRCGenerator.calcCRCModbus( rslt.sub(4).getBytes() );
        
        byte crcBa[] = new byte[2];
        crcBa[0] = (byte)(crc&0x00FF); 
        crcBa[1] = (byte)((crc>>8)&0xFF);

        return rslt.add( crcBa ); 
        
    }
    
    int getLength( ) {
        return length;
    }
    
    String toDebugString( ){
        StringBuffer rslt = new StringBuffer( );
        rslt.append( "Frame [" );
        rslt.append( "cntl 0x" + Integer.toHexString( control ) + ", " );
        rslt.append( "length " + length + ", " );
        rslt.append( "src " + Integer.toHexString( sourceAddress ) + ", " );
        rslt.append( "dest " + Integer.toHexString( destinationAddress ) );
        rslt.append( "isAck " + 
               ( isSlaveToMaster() && isAckNak() && isAckNakEnable() ) );
        rslt.append( "]" );
        return rslt.toString();
    }
    
    public String toString() {
        return toByteArray().toHexaString(true);
    }

}
