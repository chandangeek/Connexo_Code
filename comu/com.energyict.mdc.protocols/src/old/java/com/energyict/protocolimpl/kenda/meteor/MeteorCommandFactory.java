/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.meteor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MeteorCommandFactory{
	/**
	 * To Be removed from the project, was only for file reading
	 */
	/*
	 * The command factory takes in blocks of raw data received by the input stream
	 * The data is either in vector format or a sequence of a vector
	 * The factory merges all the data together and builds the correct object
	 * 
	 * Use instanceof for determination of the correct type
	 */
	private byte[][] byteArray=new byte[1][];
	private Parsers command;
	private boolean flag=false;
	private boolean type=true;
	private MeteorCommunicationsFactory mcf;
	
	MeteorCommandFactory(InputStream inputStream, OutputStream outputStream){
		mcf=new MeteorCommunicationsFactory(inputStream,outputStream);
	}
	
	public MeteorCommandFactory(  // real constructor, sets header correct.
			byte[] sourceCode, 
			byte sourceCodeExt, 
			byte[] destinationCode, 
			byte destinationCodeExt,
			InputStream inputStream, 
			OutputStream outputStream){
		mcf=new MeteorCommunicationsFactory(sourceCode,sourceCodeExt,destinationCode,destinationCodeExt,inputStream,outputStream);
	}
	
	private Parsers process(ComStruc s, Parsers command) throws IOException{
		type=s.isType();  // type of command
		if (type){ // SEND (is always in a vector)
			this.command=mcf.buildCommand(s.getByteArray(), null); // returns object for the receive side
			flag=true;
		}
		if (!type){ // RECEIVE (is probably in a matrix to be parsed into a vector
			// check first and last bits in ident
			if((s.getByteArray()[0] & 0x60)==0x60){
				this.command=mcf.buildCommand(s.getByteArray(),command);  // vector case
				flag=true;
			}
			// other cases
			else if((s.getByteArray()[0] & 0x20)==0x20){ // last block						
				byte[][] bArray=new byte[byteArray.length+1][];
				for(int i=0; i<byteArray.length; i++){
					bArray[i]=byteArray[i];
				}
				bArray[byteArray.length]=s.getByteArray();
				byteArray=bArray;
				this.command=mcf.buildCommand(mcf.blockMerging(byteArray),command);
				flag=true;
			}else if((s.getByteArray()[0] & 0x40)==0x40){ // first block
				flag=false;
				byteArray=new byte[1][];
				byteArray[0]=s.getByteArray();
			}else{// other in between
				flag=false;
				byte[][] bArray=new byte[byteArray.length+1][];
				for(int i=0; i<byteArray.length; i++){
					bArray[i]=byteArray[i];
				}
				bArray[byteArray.length]=s.getByteArray();
				byteArray=bArray;
			}
		}
		return this.command;
	}
//	public Parsers sendCommand(byte command, boolean ack, Parsers p) throws IOException{
//		return mcf.transmitData(command,ack,p);
//	}
	public boolean isReady(){
		return flag;
	}
	public boolean isType() {
	return type;
	}
	
	private String getStringArray(int length) throws IOException {
		int i=0;
		String s="";
		for(int ii=0; ii<length; ii++){
//			i=inputStream.read();
			s+=(char) i;
		}
		return s;
	}

	/**
	 * @return the command
	 */
	public Parsers getCommand() {
		return command;
	}

	/**
	 * @return the byteArray
	 */
	public byte[][] getByteArray() {
		return byteArray;
	}

	/**
	 * @param byteArray the byteArray to set
	 */
	public void setByteArray(byte[][] byteArray) {
		this.byteArray = byteArray;
	}

}
