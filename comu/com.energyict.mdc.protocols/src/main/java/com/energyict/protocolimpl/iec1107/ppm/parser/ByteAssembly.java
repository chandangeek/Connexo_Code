/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm.parser;

import com.energyict.protocolimpl.iec1107.ppm.PPMUtils;

import java.util.EmptyStackException;
import java.util.Stack;

/**This Assembly is used to parse an incoming byte stream.  The ByteAssembly
 * offers extra functionality during debugging.  It contains the read-index,
 * and with that it can display the progress of the parsing.
 *
 *  @author fbo */

public class ByteAssembly {

	/* a place to keep track of consumption progress */
	private Stack stack = new Stack();
	/* another place to record progress; this is just an object */
	private Object target;

	private int index = 0;
	private byte [] input = null;

	public int getIndex( ){
		return this.index;
	}

	public void setIndex( int index ){
		this.index = index;
	}

	public void setInput( byte [] input ){
		this.index = 0;
		this.input = input;
	}

	public byte get(){
		return this.input[this.index];
	}

	public int getSize( ){
		return this.input.length;
	}

	/**returns the object identified as this assembly's "target". Clients can
	 * set and retrieve a target, which can be a convenient supplement as a
	 * place to work, in addition to the assembly's stack.
	 *
	 * @return the target of this assembly
	 */
	public Object getTarget() {
		return this.target;
	}

	/**sets the target for this assembly. Targets must implement
	 * <code>clone()</code> as a public method.
	 *
	 * @param target a publicly cloneable object */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**removes the object at the top of this assembly's stack and returns it.
	 *
	 * @return the object at the top of this assembly's stack
	 * @exception EmptyStackException if this stack is empty */
	public Object pop() {
		return this.stack.pop();
	}

	/**pushes an object onto the top of this assembly's stack.
	 *
	 * @param object the object to be pushed */
	public void push(Object o) {
		this.stack.push(o);
	}

	public String toString( ){
		StringBuffer sb = new StringBuffer( );

		sb.append( "Assembly [size=" + this.input.length + "," );
		sb.append( "index=" + this.index + "]\n" );

		if( this.input.length > 256 ) {
			toShortString(sb, this.index);
		} else {
			toLongString(sb);
		}

		return sb.toString();
	}

	public String toString( int anIndex ){
		StringBuffer sb = new StringBuffer( );

		sb.append( "Assembly [size=" + this.input.length + "," );
		sb.append( "index=" + this.index + "]\n" );

		if( this.input.length > 256 ) {
			toShortString(sb, anIndex );
		} else {
			toLongString(sb);
		}

		return sb.toString();
	}

	private StringBuffer toShortString( StringBuffer sb, int anIndex ){
		int start = ( anIndex  - 16 ) - anIndex  % 16;
		int stop = ( anIndex  + 16 ) + anIndex  % 16;

		if( start < 0 ) {
			start = 0;
		}
		if( stop > this.input.length ) {
			stop = this.input.length;
		}

		for( int i = start; i < stop; i ++ ) {
			if( i == anIndex ) {
				sb.append( "^" );
			}
			sb.append( PPMUtils.toHexaString( this.input[i] ) + " " );
			if( ( (i+1) % 16 ) == 0 ) {
				sb.append( "\n" );
			}
		}

		return sb;
	}

	public StringBuffer toLongString( StringBuffer sb ){
		for( int i = 0; i < this.input.length; i ++ ) {
			if( i == this.index ) {
				sb.append( "^" );
			}
			sb.append( PPMUtils.toHexaString( this.input[i] ) + " " );
			if( ( (i+1) % 16 ) == 0 ) {
				sb.append( "\n" );
			}
		}
		return sb;
	}

	public byte[] getInput() {
		return this.input;
	}

	public Stack getStack() {
		return this.stack;
	}

	public int addToIndex(int value) {
		return this.index += value;
	}

}
