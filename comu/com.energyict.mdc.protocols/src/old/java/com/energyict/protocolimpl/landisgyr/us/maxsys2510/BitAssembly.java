/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.ByteArrayOutputStream;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * An assembly maintains a stream of language elements along with stack and target objects.
 * 
 * Parsers use assemblers to record progress at recognizing language elements from assembly's string.
 */
 
class BitAssembly {

    /* a place to keep track of consumption progress */
    private Stack stack = new Stack();

    /* another place to record progress; this is just an object */
    private Object target;

    int bitPosition;
    
    /* InputStream that will be parsed */
    private ByteArray byteArray = null;
    
    /* buffer of consumed elements, useful for debugging */
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * construct new Assembly with InputStream
     * 
     * @param inputStream
     *            to parse
     */
    public BitAssembly(ByteArray byteArray) {
        this.byteArray = byteArray;
        this.bitPosition = 0;
    }
    
    int nibbleIntValue( ){
        int bytePosition = bitPosition / 8;
        int mask = ( bitPosition % 8 > 0 ) ? 0x0F : 0xF0 ;
        int r = byteArray.get( bytePosition ) & mask;
        if( bitPosition % 8 == 0 )
            r = r >> 4;
        bitPosition = bitPosition + 4;
        return r;
    }

    /**
     * returns the elements of the assembly that have been consumed, separated 
     * by the specified delimiter.
     * 
     * @param String the mark to show between consumed elements
     * @return the elements of the assembly that have been consumed
     */
    public String consumed(String delimiter) {
        StringBuffer sb = new StringBuffer();
        byte[] array = buffer.toByteArray();
        for (int i = 0; i < array.length; i++)
            sb.append(array[i] + delimiter);
        return sb.toString();
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
        return buffer.size();
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
    
    public boolean notEndOfAssembly( ){
        return bitPosition/8 < byteArray.size();
    }
    
    int bitsLeft( ){
        return (byteArray.size() * 8) - bitPosition;
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