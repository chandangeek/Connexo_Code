package com.energyict.protocolimpl.medo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

public class Medo extends AbstractProtocol{
	// command descriptions from the datasheet
	// Header format
	private byte   ident;				// see ident format listed below
	private byte   blockSize;			// character count of block modulo 256	
	private byte[] sourceCode;			// Defines central equipment of origin
	private byte   sourceCodeExt;		// Defines peripheral equipment of origin
	private byte[] destinationCode;	// Defines central equipment of final destination
	private byte   destinationCodeExt;	// Defines peripheral equipment of final destination
	private byte   unit;				// DIP routing ???
	private byte   port;				// DIP routing ???
	
	// ident byte
	// Ack bit, first block bit, last block bit, R/W, 4 bit operation select
	
	// operation select codes, lowest 5 bit of ident
	private static final byte   RESERVED                   	=0x00;		  // internal DIP/Central System function
	private static final byte   fullPersTableRead          	=0x01;	  // ram initialised, table defines modes
	private static final byte   fullPersTableWrite         	=0x11;    // ... page 6/16 medo communications protocol
	private static final byte   partPersTableRead          	=0x02;
	private static final byte   partPersTableWrite         	=0x12;
	private static final byte   readRTC						=0x03;
	private static final byte   setRTC						=0x13;
	private static final byte   trimRTC						=0x14;
	private static final byte   firmwareVersion				=0x04;
	private static final byte   status						=0x05;
	private static final byte   readRelay					=0x16;
	private static final byte   setRelay					=0x06;
	private static final byte   meterDemands				=0x07;
	private static final byte   totalDemands				=0x08;  // Not available (page 12/16)
	private static final byte	readingTimes				=0x09;
	private static final byte   writingTimes				=0x19;
	private static final byte   readdialReadingCurrent		=0x0A;
	private static final byte   writedialReadingCurrent		=0x1A;
	private static final byte   dialReadingPast				=0x0B;
	private static final byte   powerFailureDetails			=0x0C;
	private static final byte   readCommissioningCounters	=0x0D;
	private static final byte   writeCommissioningCounters	=0x0D;
	private static final byte   readMemoryDirect			=0x0E;
	private static final byte   writeMemoryDirect			=0x1E;
	private static final byte   priorityTelNo				=0x1F; // N/A 
	private static final byte   alarmChanTimes				=0x0F; // N/A
	// first three bits are to be set in BuildIdent method (later)
	// byte: 8 bit, word 16 bit signed integer, long 32 bit signed integer
	
	OutputStream outputStream;
	InputStream inputStream;
	int DEBUG=0;
	
	private byte BuildIdent(boolean ack, boolean first, boolean last, byte command){
		// Ack bit, first block bit, last block bit, R/W, 4 bit operation select (last five bits are set by command)
		if(ack)   {command=(byte) (command | 0x80);}		
		if(first) {command=(byte) (command | 0x40);}
		if(last)  {command=(byte) (command | 0x20);}
		return command;
	}
	private byte[] BuildHeader(){
		byte[] header= new byte[10];
		// all of the following globals should be built before calling BuildHeader
		header[0]=ident;
		header[1]=blockSize;
		header[2]=sourceCode[0];
		header[3]=sourceCode[1];
		header[4]=sourceCodeExt;
		header[5]=destinationCode[0];
		header[6]=destinationCode[1];
		header[7]=destinationCodeExt;
		header[8]=unit;
		header[9]=port;
		return header;
	}
	
	// checksum calculation, last step of serialization
	private byte[] CheckSum(byte[] total){
		int checksum=0;
		byte[] totalcheck= new byte[total.length+1];
		for(byte c:total){
			checksum=checksum+(int) c;
		}
		totalcheck[total.length]=(byte) (checksum%256);
		return totalcheck;
	}
	
	// press block array in the right frame sizes (10+245+1)=(header+data+checksum)
	private byte[][] BlockProcessing(byte[] block){
		//ArrayList<byte[]> blockProc = new ArrayList<byte[]>();
		byte[][]blockProc;
		int numOfBlocks;
		boolean ack=false;
		
		// calculate number of blocks
		numOfBlocks=(int) Math.ceil((double) ((block.length-11)/245)); // minus 11 to remove the checksum and the header
		// generate blockProck 2D matrix
		blockProc = new byte[numOfBlocks][];
		if (numOfBlocks==1){
			blockProc[0]=block;
		}else{
			// start building ident bits
			if((ident & 0x80) == 0x80){
				ack=true; 
			}
			// adapt first ident using BuildIdent
			BuildIdent(ack, true, false, (byte) (ident & 0x1F)); // global ident adapted
			byte[] header = BuildHeader(); // header matrix construction
			byte[] blockSection = new byte[255];
			for(int i=0; i<255;i++){
				if(i<10){blockProc[0][i]=header[i];} // add header
				if(i>10){blockProc[0][i]=block[i];}	 // add data			
			}
			byte[] finalBlockSection=CheckSum(blockSection); // checksum added to block
			blockProc[0]=finalBlockSection;
			// first block serialized
			
			//adapt ident for center frames
			if(numOfBlocks>2){
				for (int i=1; i<numOfBlocks; i++){
					BuildIdent(ack, false, false, (byte) (ident & 0x1F));
				}
			}
			// adapt last ident
			BuildIdent(ack, false, true, (byte) (ident & 0x1F));
		}

		return blockProc; 
	}
	
	@Override
	protected void doConnect() throws IOException {
	}

	@Override
	protected void doDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List doGetOptionalKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		
		this.inputStream=inputStream;
		this.outputStream=outputStream;
							
		return null;
	}
	
	// send and receive data from datalogger
    private void sendFrame(byte[] byteBuffer) throws IOException
    {
       outputStream.write(byteBuffer); // write a string to the datalogger

       if (DEBUG==1) // only for debugging purposes
       {
           for (int i=0;i<byteBuffer.length;i++){
               ProtocolUtils.outputHex( ((int)byteBuffer[i])  &0x000000FF);        	   
           }
           System.out.println();
       }
    } // private void sendMasterCommandBuffer() throws IOException
    
    private byte[] readFrame(byte[] byteBuffer) throws IOException{
    	inputStream.read();
    	return null;
    }
    // end send and receive data from datalogger
    
	@Override
	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return "MEDO Metering Equipment Digital Outstation - V1.3";
	}

	@Override
	public String getProtocolVersion() {
		return "$Date$";
	}

	@Override
	public Date getTime() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
