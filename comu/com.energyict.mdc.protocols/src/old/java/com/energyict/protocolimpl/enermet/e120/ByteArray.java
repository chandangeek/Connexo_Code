/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

import java.util.ArrayList;
import java.util.List;

/** 
 * Big Endian Byte Array - for contemporary byte[] manipulation.
 *  
 * @author fbo
 */

class ByteArray {

    byte[] data;
    

    /** create new empty ByteArray object */
    ByteArray() {
        data = new byte[0];
    }

    /** create new single ByteArray object containing a single byte */
    ByteArray(byte aByte) {
        data = new byte[] { aByte };
    }

    /** create new single ByteArray with a byte[] */
    ByteArray(byte[] data) {
        this.data = data;
    }
    
    /** create new single ByteArray with a String */
    ByteArray(String data){
        this.data = data.getBytes();
    }

    /** 
     * Add a byte[] object.
     * @param byteArray Object to add
     * @return this
     */
    ByteArray add(byte[] byteArray) {
        int newLength = data.length + byteArray.length;
        byte[] tmp = new byte[newLength];
        System.arraycopy(data, 0, tmp, 0, data.length);
        System.arraycopy(byteArray, 0, tmp, data.length, byteArray.length);
        data = tmp;
        return this;
    }
    
    /** 
     * Add a ByteArray object.
     * @param byteArray Object to add
     * @return this
     */
    ByteArray add(ByteArray byteArray) {
        if( byteArray != null ) {
            byte[] tmp = byteArray.getBytes();
            add(tmp);
        }
        return this;
    }
    
    /** 
     * Add a String object as ascii encoded bytes.
     * @param string Object to add
     * @return this
     */
    ByteArray add(String string){
        if( string != null )
            add(string.getBytes());
        return this;
    }
    
    /** 
     * Add a byte primitive.
     * @param aByte primitive to add
     * @return this
     */
    ByteArray add(byte aByte) {
        int newLength = data.length + 1;
        byte[] tmp = new byte[newLength];
        System.arraycopy(data, 0, tmp, 0, data.length);
        tmp[tmp.length - 1] = aByte;
        data = tmp;
        return this;
    }

    /** 
     * Add a short primitive (2 bytes).
     * @param aShort primitive to add
     * @return this
     */
    ByteArray add(short aShort){
        byte [] ba = new byte[2];
        ba[1] = (byte)(aShort & 0xff);
        ba[0] = (byte)(aShort >> 8 & 0xff);
        return add(ba);
    }
    
    /** 
     * Add an int primitive (4 bytes).
     * @param anInt primitive to add
     * @return this
     */
    ByteArray add(int anInt){
        byte [] ba = new byte[4];
        ba[3] = (byte)(anInt & 0xff);
        ba[2] = (byte)((anInt >> 8) & 0xff);
        ba[1] = (byte)((anInt >> 16) & 0xff);
        ba[0] = (byte)((anInt >> 24) & 0xff);
        return add(ba);
    }

    ByteArray addInt(int anInt){
        return add(anInt);
    }
    
    ByteArray addShort(int anInt){
        return add((short)anInt);
    }
    
    ByteArray addByte(int anInt){
        return add((byte)anInt);
    }
    
    /** 
     * @param start
     * @param length
     * @return
     */
    ByteArray sub(int start, int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(data, start, tmp, 0, length);
        return new ByteArray(tmp);
    }

    /**
     * @param start
     * @return
     */
    ByteArray sub(int start) {
        int length = data.length - start;
        return sub(start, length);
    }

    
    /** 
     * get first byte
     * @return first byte
     */
    byte first() {
        return data[0];
    }

    /** 
     * get last byte
     * @return last byte 
     */
    byte last() {
        return data[data.length - 1];
    }

    /** 
     * return 4 byte integral
     * @param start
     * @return
     */
    int intValue(int start) {
        int result = 0;
        result |= (((int)data[start])   << 24 ) & 0xFF000000;
        result |= (((int)data[start+1]) << 16 ) & 0x00FF0000;
        result |= (((int)data[start+2]) << 8  ) & 0x0000FF00;
        result |= (((int)data[start+3])       ) & 0x000000FF;
        return result;
    }
    
    /** 
     * 2 byte integral
     * @param start
     * @return
     */
    short shortValue(int start){
        int result = 0;
        result |= (((int)data[start])   << 8) & 0x0000FF00;
        result |= (((int)data[start+1])     ) & 0x000000FF;
        return (short)result;
    }
    
    /** get _unsigned_ byte from position 
     * @param start position of byte
     * @return int 
     */
    int byteValue(int start){
        return data[start] & 0xFF;
    }
    
    /** 
     * get primitive byte[] 
     * @return ByteArray as primitive byte[]
     */
    byte[] getBytes() {
        return data;
    }

    /**
     * nr of bytes in ByteArray
     * @return nr of bytes
     */
    int size() {
        return data.length;
    }
    

    /**
     * Returns the index within this ByteArray of the first occurrence of the
     * specified byte. If no such byte occurs in this string,
     * then <code>-1</code> is returned.
     *
     * @param   aByte   a byte primitive.
     * @return  the index of the first occurrence of the byte, or
     *          <code>-1</code> if the byte does not occur.
     */
    int indexOf(byte aByte){
        for(int i=0; i<data.length; i++ )
            if(data[i]==aByte)return i;
        return -1; 
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

    /** Special debug stuff */
    String toHexaString() {
        return toHexaString(data, 0, data.length, false);
    }

    /** Special debug stuff */
    String toHexaString(boolean display) {
        return toHexaString(data, 0, data.length, display);
    }
    
    /** Special debug stuff */
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

    /** Special debug stuff */
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
    
}
