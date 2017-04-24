/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ByteArray {

    byte[] data;

    ByteArray() {
        data = new byte[0];
    }

    ByteArray(byte aByte) {
        data = new byte[] { aByte };
    }

    ByteArray(byte[] data) {
        this.data = data;
    }

    ByteArray add(byte aByte) {
        int newLength = data.length + 1;
        byte[] tmp = new byte[newLength];
        System.arraycopy(data, 0, tmp, 0, data.length);
        tmp[tmp.length - 1] = aByte;
        data = tmp;
        return this;
    }

    ByteArray add(byte[] byteArray) {
        int newLength = data.length + byteArray.length;
        byte[] tmp = new byte[newLength];
        System.arraycopy(data, 0, tmp, 0, data.length);
        System.arraycopy(byteArray, 0, tmp, data.length, byteArray.length);
        data = tmp;
        return this;
    }

    ByteArray sub(int start, int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(data, start, tmp, 0, length);
        return new ByteArray(tmp);
    }

    ByteArray sub(int start) {
        int length = data.length - start;
        return sub(start, length);
    }

    ByteArray add(ByteArray byteArray) {
        byte[] tmp = byteArray.getBytes();
        return add(tmp);
    }
    
    /** Serialize an int to a ByteStream (prepended with typefield)
     * 
     * @param anInt value to be serialized
     * @param length number of bytes used for value
     * @return this instance
     */
    ByteArray addInt( int anInt, int length ){

        if( length < 0 || length > 4 ) {
            String msg = "length must be >=1 and <= 4";
            throw new SerializeException( msg );
        }
        
        byte [] b = new byte[length+1];
        int bPos = 0;
        
        b[bPos++] = (byte)(0x60 | length);
        
        if( length > 3 ) b[bPos++] |= ((anInt & 0xff000000) >> 24);
        if( length > 2 ) b[bPos++] |= ((anInt & 0x00ff0000) >> 16);
        if( length > 1 ) b[bPos++] |= ((anInt & 0x0000ff00) >> 8 );
        b[bPos++] |= (anInt & 0x000000ff);
        
        this.add( b ); 
        return this;
        
    }

    /** Serialize an int to a ByteStream (without type field)
     * 
     * @param anInt value to be serialized
     * @param length number of bytes used for value
     * @return this instance
     */
    ByteArray addRawInt( int anInt, int length ){

        if( length < 0 || length > 4 ) {
            String msg = "length must be >=1 and <= 4";
            throw new SerializeException( msg );
        }
        
        byte [] b = new byte[length];
        int bPos = 0;
        
        if( length > 3 ) b[bPos++] |= ((anInt & 0xff000000) >> 24);
        if( length > 2 ) b[bPos++] |= ((anInt & 0x00ff0000) >> 16);
        if( length > 1 ) b[bPos++] |= ((anInt & 0x0000ff00) >> 8 );
        b[bPos++] |= (anInt & 0x000000ff);
        
        this.add( b ); 
        return this;
        
    }
    
    ByteArray add( IonHandle handle ){
        this.add( handle.toByteArray() );
        return this;
    }
    
    ByteArray add( IonMethod method ){
        this.add( method.toByteArray() );
        return this;
    }

    byte first() {
        return data[0];
    }

    byte last() {
        return data[data.length - 1];
    }

    byte get(int index) {
        return data[index];
    }

    int intValue(int start) {
        return data[start] & 0xFF;
    }

    byte[] getBytes() {
        return data;
    }

    int size() {
        return data.length;
    }
    
    ByteArray trim( ) {
        int i = data.length-1;
        for( ; i >= 0; i-- )
            if( data[i] != (byte)69 )
                break;
        byte [] temp = new byte[i+1];
        System.arraycopy( data, 0, temp, 0, i+1 );
        data = temp;
        return this;
    }
    
    boolean getBit( int index ) {
        return ( data[index/8] & ( 1 << index%8 ) ) > 0;
    }
    
    List split(int blockSize) {
        ArrayList result = new ArrayList();
        int index = 0;
        
        while( (index + blockSize) <= data.length ) {
            result.add( sub( index, blockSize) ); 
            index = index + blockSize;
        }
        if( index < data.length )
            result.add( sub( index, data.length - index ) );
        
        return result;
    }

    String toHexaString() {
        return toHexaString(data, 0, data.length, false);
    }

    String toHexaString(boolean display) {
        return toHexaString(data, 0, data.length, display);
    }
    
    String toHexaString(int from, boolean display){
        return toHexaString(data, from, data.length, display);
    }

    /** Special debug stuff */
    String toHexaString(byte[] b, int pos, int length, boolean display) {
        StringBuffer sb = new StringBuffer();
        if (b == null)
            sb.append(" <null> ");
        else
            for (int i = pos; i < pos + length && i < b.length; i++) {
                if (display)
                    sb.append("0x" + toHexaString(b[i]) + " ");
                else
                    sb.append(toHexaString(b[i]));
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

    String toBinaryString() {
        StringBuffer result = new StringBuffer();
        for (int bi = 0; bi < data.length; bi++) {
            result
                .append(((data[bi] & 0x80) > 0) ? "1" : "0")
                .append(((data[bi] & 0x40) > 0) ? "1" : "0")
                .append(((data[bi] & 0x20) > 0) ? "1" : "0")
                .append(((data[bi] & 0x10) > 0) ? "1" : "0")
                .append(((data[bi] & 0x08) > 0) ? "1" : "0")
                .append(((data[bi] & 0x04) > 0) ? "1" : "0")
                .append(((data[bi] & 0x02) > 0) ? "1" : "0")
                .append(((data[bi] & 0x01) > 0) ? "1" : "0");
        }
        return result.toString();
    }
    
    void dumpToFile( String fileName ) throws IOException {
        File f = new File( fileName );
        FileOutputStream fos = new FileOutputStream(f);
        fos.write( data );
        fos.close();
    }
    
    public String toString( ) {
        return toHexaString(true);
    }

    class SerializeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        SerializeException(String msg) {
            super( msg );
        }
    }
    
}
