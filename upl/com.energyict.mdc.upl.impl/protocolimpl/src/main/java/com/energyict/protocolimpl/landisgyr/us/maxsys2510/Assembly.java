package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * An assembly maintains a stream of language elements along with stack and target objects.
 * 
 * Parsers use assemblers to record progress at recognizing language elements from assembly's string.
 */

class Assembly {

    boolean dbg = false;

    MaxSys maxSys;

    /* a place to keep track of consumption progress */
    private Stack stack = new Stack();

    /* another place to record progress; this is just an object */
    private Object target;

    int bytePosition;

    /* InputStream that will be parsed */
    private ByteArray byteArray = null;

    /* buffer of consumed elements, useful for debugging */
    private ByteArray consumed = new ByteArray();

    /**
     * construct new Assembly with InputStream
     * 
     * @param inputStream
     *            to parse
     */
    public Assembly(MaxSys maxSys, ByteArray byteArray) {
        this.maxSys = maxSys;
        this.byteArray = byteArray;
        this.bytePosition = 0;
        
        this.byteArray.trim();
        
    }

    /** 1 byte */
    int rawByteValue( ){
        if( dbg ) 
            System.out.println(toString());
        return popByte();
    }
    
    /** 1 byte */
    int byteValue() {
        if( dbg ) 
            System.out.println(toString());
        byte b = popByte();
        return ( ( ( b & 0xf0 ) >> 4 ) * 10 ) + ( b & 0x0F );
    }

    /** 2 bytes, binary, lsb first, signed */
    int intValue() {
        if( dbg ) 
            System.out.println(toString());
        int b1 = popByte() & 0xFF;
        int b2 = popByte();
        return (b2 * 256) + b1;
    }

    /** 2 bytes, binary, lsb first, unsigned */
    int wordValue() {
        if( dbg ) 
            System.out.println(toString());
        int b1 = popByte() & 0xFF;
        int b2 = popByte() & 0xFF;
        return (b2 * 256) + b1;
    }

    /** 8 bytes, binary, lsb first */
    double doubleValue() {
        return ByteBuffer.wrap(popBytes(8)).order(ByteOrder.LITTLE_ENDIAN).getDouble();
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

    private byte[] popBytes(int lenght) {
        byte[] b = byteArray.sub(bytePosition, lenght).getBytes();
        bytePosition = bytePosition + lenght;
        if (dbg)
            consumed.add(b);
        return b;
    }

    private byte popByte() {
        return popBytes(1)[0];
    }

    /**
     * returns the elements of the assembly that have been consumed, separated by the specified delimiter.
     * 
     * @param String
     *            the mark to show between consumed elements
     * @return the elements of the assembly that have been consumed
     */
    public String consumed(String delimiter) {
        int from = ( consumed.size() > 10 ) ? consumed.size()-10 : consumed.size() ;
        return consumed.toHexaString( from, true);
    }

    boolean hasMoreElements() {
        return bytePosition < byteArray.size();
    }

    /**
     * returns the default string to show between elements.
     * 
     * @return the default string to show between elements
     */
    public String defaultDelimiter() {
        return " ";
    }

    /**
     * returns the number of elements that have been consumed.
     * 
     * @return the number of elements that have been consumed
     */
    public int elementsConsumed() {
        return consumed.size();
    }
    
    public int elementsLeft() {
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
        return stack.pop();
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

    MaxSys getMaxSys() {
        return maxSys;
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
        String delimiter = defaultDelimiter();
        return stack + consumed(delimiter) + "^" + "...";
    }

}