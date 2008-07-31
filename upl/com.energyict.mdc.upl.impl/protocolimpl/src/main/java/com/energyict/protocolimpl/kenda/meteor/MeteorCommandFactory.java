package com.energyict.protocolimpl.kenda.meteor;
import java.io.IOException;

public class MeteorCommandFactory {
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
	
	MeteorCommandFactory(){
		mcf=new MeteorCommunicationsFactory();
	}
	
	public MeteorCommandFactory(byte blockSize,  // real constructor, sets header correct.
			byte[] sourceCode, 
			byte sourceCodeExt, 
			byte[] destinationCode, 
			byte destinationCodeExt){
		mcf=new MeteorCommunicationsFactory(blockSize,sourceCode,sourceCodeExt,destinationCode,destinationCodeExt);
	}
	
	private Parsers process(ComStruc s, Parsers command){
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
	public Parsers sendCommand(ComStruc s, Parsers command){
		return process(s,command);
	}
	public Parsers sendSmallCommand(byte b, Object object) {
		// build comstruc
		mcf.buildIdent(false,true,true,b);
		byte[] proc=mcf.buildHeader();
		proc=mcf.addCheckSum(proc);
		return(mcf.buildCommand(proc, null));
	}
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
