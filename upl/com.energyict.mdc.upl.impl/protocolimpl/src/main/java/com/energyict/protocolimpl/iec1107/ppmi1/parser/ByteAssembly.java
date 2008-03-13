package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.util.EmptyStackException;
import java.util.Stack;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

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
	
	
	int index = 0;
	
	byte [] input = null;
	
	public int getIndex( ){
		return index;
	}
	
	public void setIndex( int index ){
		this.index = index;
	}
	
	public void setInput( byte [] input ){
		this.index = 0;
		this.input = input;
	}
	
	public byte get(){
		return input[index];
	}
	
	public int getSize( ){
		return input.length;
	}
	
	/**returns the object identified as this assembly's "target". Clients can
	 * set and retrieve a target, which can be a convenient supplement as a
	 * place to work, in addition to the assembly's stack. 
	 * 
	 * @return the target of this assembly
	 */
	public Object getTarget() {
		return target;
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
		return stack.pop();
	}
	
	/**pushes an object onto the top of this assembly's stack.
	 * 
	 * @param object the object to be pushed */
	public void push(Object o) {
		stack.push(o);
	}
	
	public String toString( ){
		StringBuffer sb = new StringBuffer( );
		
		sb.append( "Assembly [size=" + input.length + "," );
		sb.append( "index=" + index + "]\n" );
		
		if( input.length > 256 )
			toShortString(sb, index);
		else
			toLongString(sb);
		
		return sb.toString();
	}
	
	public String toString( int anIndex ){
		StringBuffer sb = new StringBuffer( );
		
		sb.append( "Assembly [size=" + input.length + "," );
		sb.append( "index=" + index + "]\n" );
		
		if( input.length > 256 )
			toShortString(sb, anIndex );
		else
			toLongString(sb);
		
		return sb.toString();
	}
	
	private StringBuffer toShortString( StringBuffer sb, int anIndex ){
		int start = ( anIndex  - 16 ) - anIndex  % 16;
		int stop = ( anIndex  + 16 ) + anIndex  % 16;
		
		if( start < 0 ) start = 0;
		if( stop > input.length ) stop = input.length;
		
		for( int i = start; i < stop; i ++ ) {
			if( i == anIndex ) sb.append( "^" );
			sb.append( PPMUtils.toHexaString( input[i] ) + " " );
			if( ( (i+1) % 16 ) == 0 ) sb.append( "\n" );
		}
		
		return sb;
	}
	
	public StringBuffer toLongString( StringBuffer sb ){
		for( int i = 0; i < input.length; i ++ ) {
			if( i == index ) sb.append( "^" );
			sb.append( PPMUtils.toHexaString( input[i] ) + " " );
			if( ( (i+1) % 16 ) == 0 ) sb.append( "\n" );
		}
		return sb;
	}
		
}
