package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.util.ArrayList;
import java.util.Iterator;
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
            result.append(((data[bi] & 0x80) > 0) ? "1" : "0").append(((data[bi] & 0x40) > 0) ? "1" : "0").append(
                    ((data[bi] & 0x20) > 0) ? "1" : "0").append(((data[bi] & 0x10) > 0) ? "1" : "0").append(
                    ((data[bi] & 0x08) > 0) ? "1" : "0").append(((data[bi] & 0x04) > 0) ? "1" : "0").append(
                    ((data[bi] & 0x02) > 0) ? "1" : "0").append(((data[bi] & 0x01) > 0) ? "1" : "0");
        }
        return result.toString();
    }
    
    public static void main( String [] args ){
        
        byte [] b = 
        new byte [] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };
        
        ByteArray ba = new ByteArray( b );
        
        System.out.println( ba.toHexaString( true) );
        
        Iterator i = ba.split( 2 ).iterator();
        while( i.hasNext() )  {
            ByteArray temp = (ByteArray)i.next();
            System.out.println(temp.toHexaString(true));
        
        }
        
    }

}
