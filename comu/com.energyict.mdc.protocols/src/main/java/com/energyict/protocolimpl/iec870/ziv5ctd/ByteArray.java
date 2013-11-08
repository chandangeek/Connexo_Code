package com.energyict.protocolimpl.iec870.ziv5ctd;


/** */

public class ByteArray {
    
    byte [] data;
    
    ByteArray(){
        data = new byte[0];
    }
    
    ByteArray(byte aByte){
        data = new byte[]{ aByte };
    }
    
    ByteArray( byte [] data ){
        this.data = data;
    }
    
    ByteArray add( byte aByte ){
        int newLength = data.length+1;
        byte [] tmp = new byte[newLength];
        System.arraycopy( data, 0, tmp, 0, data.length );
        tmp[tmp.length-1] = aByte;
        data = tmp;
        return this;
    }
    
    ByteArray add( byte [] byteArray ) {
        int newLength = data.length+byteArray.length;
        byte [] tmp = new byte[newLength];
        System.arraycopy( data, 0, tmp, 0, data.length );
        System.arraycopy( byteArray, 0, tmp, data.length, byteArray.length );
        data = tmp;
        return this;
    }
    
    ByteArray sub( int start, int length ){
        byte [] tmp = new byte [length];
        System.arraycopy( data, start, tmp, 0, length );
        return new ByteArray(tmp);
    }
    
    ByteArray sub( int start ) {
        int length = data.length - start;
        return sub(start, length);
    }
    
    ByteArray add( ByteArray byteArray ) {
        byte[] tmp = byteArray.toByteArray();
        return add( tmp );
    }
    
    ByteArray add( Marshalable marshalable ){
        return add( marshalable.toByteArray() );
    }
    
    byte first( ){
        return data[0];
    }
    
    byte last( ) {
        return data[data.length-1];
    }
    
    byte get( int index ) {
        return data[index];
    }
    
    int intValue( int start ){
        return data[start] & 0xFF;
    }
    
    /*
     * @arg index start of int
     * @arg length in byte
     */
    int intValue( int start, int length ){
        int r = ((int)data[start])& 0x000000FF;
        
        if( length > 1 )
            r |= ((int)data[start+1]) * 256& 0x0000FF00;
        if( length > 2 )
            r |= ((int)data[start+2]) * 512& 0x00FF0000;
        if( length > 3 )
            r |= ((int)data[start+3]) * 1024& 0xFF000000;
        
        return r;
    }
    
    boolean getBit( int index ) {
        return ( data[index/8] & ( 1 << index%8 ) ) > 0;
    }
    
    int bitValue( int start, int stop ) {
        String sbString = bitString(start, stop);
        return Integer.parseInt(sbString, 2);
    }
    
    int length( ){
        return data.length;
    }
    
    byte checksum( ){
        byte checksum = 0;
        
        for( int i = 0; i < data.length; i ++ ) {
            checksum += (int)data[i]&0xFF;
        }
        
        return checksum;
    }
    
    String toHexaString( ){
        return toHexaString( data, 0, data.length, false );
    }
    
    String toHexaString( boolean display ) {
        return toHexaString( data, 0, data.length, display );
    }
    
    /** Special debug stuff */
    private String toHexaString( byte [] b, int pos, int length, boolean display ){
        StringBuffer sb = new StringBuffer();
        if( b == null )
            sb.append( " <null> " );
        else
            for( int i = pos; i < pos + length; i ++  ){
                if( display )
                    sb.append( "0x" + toHexaString( b[i] ) + " " );
                else
                    sb.append(toHexaString( b[i] ));
            }
        return sb.toString();
    }
    
    /** Special debug stuff */
    private String toHexaString(int b) {
        String r = Integer.toHexString(b & 0xFF);
        if (r.length() < 2)
            r = "0" + r;
        return r;
    }
    
    String toBinaryString( ){
        StringBuffer result = new StringBuffer();
        for( int bi = 0; bi < data.length; bi ++ ) {
            result
                    .append( ( ( data[bi] & 0x80 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x40 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x20 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x10 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x08 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x04 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x02 ) > 0 ) ? "1" : "0" )
                    .append( ( ( data[bi] & 0x01 ) > 0 ) ? "1" : "0" );
        }
        return result.toString();
    }
    
    byte [] toByteArray( ) {
        return data;
    }
    
    public String toString( ){
        return toHexaString(data, 0, data.length, true);
    }
    
    void setBit( int pos, boolean b ){
        int bitnr = pos%8;
        if( b )
            data[pos/8] = (byte) (data[pos/8] | (1 << bitnr));
        else
            data[pos/8] = (byte) (data[pos/8] & ~(1 << bitnr));
    }
    
    String bitString( int start, int stop ){
        String rslt = "";
        for( int i = start; i < stop+1; i++ )
            rslt = ( getBit(i) ? "1" : "0" ) + rslt;
        
        return rslt;
    }
   
}
