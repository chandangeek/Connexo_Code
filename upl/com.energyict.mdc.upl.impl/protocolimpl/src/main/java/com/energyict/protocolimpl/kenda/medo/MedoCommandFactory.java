package com.energyict.protocolimpl.kenda.medo;

import java.io.IOException;

public class MedoCommandFactory {
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

	MedoCommandFactory(){};

	MedoCommandFactory(ComStruc s, Parsers command) throws IOException{
		process(s,command);
	}

	private Parsers process(ComStruc s, Parsers command) throws IOException{
		Medo medo=new Medo(propertySpecService);
		type=s.isType();
		if (type){ // SEND (is always in a vector)
			this.command=medo.getMcf().buildCommand(s.getByteArray(), null); // returns object for the receive side
			flag=true;
		}
		if (!type){ // RECEIVE (is probably in a matrix to be parsed into a vector
			// check first and last bits
			if((s.getByteArray()[0] & 0x60)==0x60){
				this.command=medo.getMcf().buildCommand(s.getByteArray(),command);  // vector case
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
				this.command=medo.getMcf().buildCommand(medo.getMcf().blockMerging(byteArray),command);
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
	public Parsers addCommand(ComStruc s, Parsers command) throws IOException{
		return process(s,command);
	}
	public boolean isReady(){
		return flag;
	}
	public boolean isType() {
	return type;
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
