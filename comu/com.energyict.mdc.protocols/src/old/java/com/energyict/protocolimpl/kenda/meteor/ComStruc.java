/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;

public class ComStruc {
	private boolean type;
	private char[] sequence={};
	
	public ComStruc(){}
	
	ComStruc(char c, char[] cs){
		type=false;
		if(c=='s'){
			type=true; // SEND=TRUE
		}
		sequence=cs;
	}
	public void addByte(char c){
		char[] cext=new char[sequence.length+1];
		for(int i=0; i<sequence.length; i++){
			cext[i]=sequence[i];
		}
		cext[sequence.length]=c;
		sequence=cext;
	}
	public byte[] getByteArray(){
		byte[] b=new byte[sequence.length];
		int i=0;
		for(int ii=0; ii<sequence.length; ii++){
        	char c=sequence[ii];
			b[i++]=(byte) c;
		}
		return b;
	}
	
	// string writers
	public String toString(){
		String s="";
		if(type){s="-S- ";}
		else{s="-R- ";}
		s+=new String(sequence);
		return s;
	}
	public String toByteString(){
		String s="";
		s+=new String(sequence);
		return s;
	}
	public String getHexVals(){
		String s="";
		for(int ii=0; ii<sequence.length; ii++){
			char c=sequence[ii];
			Integer integer;
			integer=new Integer(((int) c));
//			s+=Integer.toHexString(integer).toUpperCase()+" "; //FIXME: Commented out to prevent build errors (1.4 <-> 1.5) Only debug code
		}
		return s;
	}
	/**
	 * @return the type
	 */
	public boolean isType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(boolean type) {
		this.type = type;
	}
	/**
	 * @return the sequence
	 */
	public char[] getSequence() {
		return sequence;
	}
	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(char[] sequence) {
		this.sequence = sequence;
	}

	
}
