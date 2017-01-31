/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;

/**
 * An assembly maintains a stream of language elements along with stack and target objects.
 * 
 * Parsers use assemblers to record progress at recognizing language elements from assembly's string.
 */

class Assembly {

    boolean dbg = true;

    /* a place to keep track of consumption progress */
    private Stack stack = new Stack();

    /* another place to record progress; this is just an object */
    private Object target;

    int bytePosition;
    int nibblePosition;

    /* InputStream that will be parsed */
    private ByteArray byteArray = null;

    /**
     * construct new Assembly with InputStream
     * 
     * @param inputStream
     *            to parse
     */
    public Assembly( Object target, ByteArray byteArray) {
        this.target = target;
        this.byteArray = byteArray;
        this.bytePosition = 0;
        
    }

    String stringValue(int length) {
        return new String(popBytes(length));
    }

    ByteArray getBytes(int length) {
        return new ByteArray(popBytes(length));
    }
    
    ByteArray getBytes() {
        return byteArray;
    }

    byte byteValue( ) {
        return popBytes(1)[0];
    }
   
    byte nibbleValue( ) {
        byte result;
        if( nibblePosition == 0 ) {
            nibblePosition = 1;
            result = (byte)(( byteArray.get( bytePosition ) & 0xf0 ) >> 4);
        } else {
            nibblePosition = 0;
            result = (byte)(popBytes(1)[0] & 0x0f);
        }
        return result;
    }
    
    byte[] byteValues( int lenght ) {
        return popBytes(lenght);
    }
    
    int unsignedIntValue( int length ) {
        int rslt = 0; 
        for( int i = length; i > 0; i -- ) {
            rslt = rslt << 8;
            rslt += (popBytes(1)[0]&0xff);
        }
        return rslt;
    }
    
    int intValue( int length ) {
        return unsignedIntValue(length);
    }
    
    long longValue( ) {
        long rslt = 0; 
        for( int i = 8; i > 0; i -- ) {
            rslt = rslt << 8;
            rslt += (popBytes(1)[0]&0xff);
        }
        return rslt;        
    }
        
    private byte[] popBytes(int length) {
        byte[] b = byteArray.sub(bytePosition, length).getBytes();
        bytePosition = bytePosition + length;
        return b;
    }

    boolean hasMoreBytes() {
        return bytePosition < byteArray.size();
    }
    
    /** Take a peek at the next byte in line. */
    byte peekByte() {
        return byteArray.get(bytePosition);
    }

    /**
     * returns the default string to show between elements.
     * 
     * @return the default string to show between elements
     */
    public String defaultDelimiter() {
        return " ";
    }

    public int bytesLeft() {
        return byteArray.size() - bytePosition;
    }

    /**
     * returns the object identified as this assembly's "target". Clients can set and retrieve a target, which can be a
     * convenient supplement as a place to work, in addition to the assembly's stack.
     * 
     * @return the target of this assembly
     */
    public Object getTarget() {
        return target;
    }

    /**
     * sets the target for this assembly. Targets must implement <code>clone()</code> as a public method.
     * 
     * @param target
     *            a publicly cloneable object
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * removes the object at the top of this assembly's stack and returns it.
     * 
     * @return the object at the top of this assembly's stack
     * @exception EmptyStackException
     *                if this stack is empty
     */
    public Object pop() {
        try {
            return stack.pop();
        } catch( EmptyStackException ese ) {
            ese.printStackTrace();
            return null;
        }
    }

    /**
     * pushes an object onto the top of this assembly's stack.
     * 
     * @param object
     *            the object to be pushed
     */
    public void push(Object o) {
        stack.push(o);
    }

    /**
     * returns true if this assembly's stack is empty.
     * 
     * @return true, if this assembly's stack is empty
     */
    public boolean stackIsEmpty() {
        return stack.isEmpty();
    }

    public byte [] toBytes( ){
        return byteArray.getBytes();
    }
    
    int getBytePosition() {
        return bytePosition;
    }
    
    /**
     * returns a textual description of this assembly.
     * 
     * @return a textual description of this assembly
     */
    public String toString() {

        final int displayLength = 7;
        
        StringBuffer sb = new StringBuffer();
        StringBuffer stackBuffer = new StringBuffer();
        
        stackBuffer.append( "\n");
        int si = 0;
        Iterator sIt = stack.listIterator();
        while( sIt.hasNext() ) {
            String s = sIt.next().toString();
            s = s.replaceAll( "\n", " " );
            if( s.length() > 100 )
                s = s.substring( 0, 100 );
        
            stackBuffer.append( " " + si++ + ": " + s + "\n" );
        }
        
        stackBuffer.append( "\n" );
        
        sb.append( "^" );
        
        for( int i = 0; i < displayLength; i++ ) {
            int dp = bytePosition - i - 1;
            if( dp >= 0 ) {
                sb.insert( 0, toHexaString( byteArray.get( dp ) & 0xff ) + " " );
            } else {
                sb.insert( 0, "     " );
            }
        }
        for( int i = 0; i < displayLength; i++ ) {
            int dp = bytePosition + i;
            if( dp < byteArray.size() ) {
                sb.append( toHexaString( byteArray.get( dp ) & 0xff ) + " " );
            } else {
                sb.append( "     " );
            }
        }
        
        return sb.insert(0, stackBuffer ).toString();
        
    }

    private String toHexaString(int b) {
        String r = Integer.toHexString(b & 0xFF);
        if (r.length() < 2)
            r = "0" + r;
        return "0x" + r;
    }
    
}