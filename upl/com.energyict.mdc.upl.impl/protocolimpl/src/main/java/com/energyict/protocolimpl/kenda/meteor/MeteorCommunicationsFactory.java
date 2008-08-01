package com.energyict.protocolimpl.kenda.meteor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MeteorCommunicationsFactory{
	
	// command descriptions from the datasheet
	// Header format, at the moment I consider only ident as a variable
	private byte   ident;					// see ident format listed below
	private byte   blockSize;			// character count of block modulo 256	
	private final byte[] sourceCode;		// Defines central equipment of origin
	private final byte   sourceCodeExt;		// Defines peripheral equipment of origin
	private final byte[] destinationCode;	// Defines central equipment of final destination
	private final byte   destinationCodeExt;// Defines peripheral equipment of final destination
	private byte   unit;					// DIP routing ???
	private byte   port;					// DIP routing ???
	
	// ident byte
	// Ack bit, first block bit, last block bit, R/W, 4 bit operation select
	private static final byte   RESERVED                   	=0x00;		  // internal DIP/Central System function
	private static final byte   fullPersTableRead          	=0x01;	  // ram initialised, table defines modes
	private static final byte   fullPersTableWrite         	=0x11;    // ... page 6/16 Meteor communications protocol
	private static final byte   extendedPersTableRead      	=0x02;
	private static final byte   extendedPersTableWrite     	=0x12;
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
	private static final byte   writeCommissioningCounters	=0x1D;
	private static final byte   readMemoryDirect			=0x0E;
	private static final byte   writeMemoryDirect			=0x1E;
	private static final byte   priorityTelNo				=0x1F; // N/A 
	private static final byte   alarmChanTimes				=0x0F; // N/A

	private InputStream inputStream;
	private OutputStream outputStream;
	// first three bits are to be set in BuildIdent method (later)
	// byte: 8 bit, word 16 bit signed integer, long 32 bit signed integer
	
	public MeteorCommunicationsFactory(InputStream inputStream, OutputStream outputStream){// blank constructor for testing purposes only
		byte[] blank={0,0};
		ident=0;				// see ident format listed below
		blockSize=11;			// character count of block modulo 256	
		sourceCode=blank;		// Defines central equipment of origin
		sourceCodeExt=0;		// Defines peripheral equipment of origin
		destinationCode=blank;	// Defines central equipment of final destination
		destinationCodeExt=0;	// Defines peripheral equipment of final destination
		unit=0;					// DIP routing ???
		port=0;					// DIP routing ???
		this.inputStream=inputStream;
		this.outputStream=outputStream;
	}
	public MeteorCommunicationsFactory(  // real constructor, sets header correct.
			byte[] sourceCode, 
			byte sourceCodeExt, 
			byte[] destinationCode, 
			byte destinationCodeExt,
			InputStream inputStream, 
			OutputStream outputStream){
		ident=0;
		blockSize=11;
		this.sourceCode=sourceCode;
		this.sourceCodeExt=sourceCodeExt;
		this.destinationCode=destinationCode;
		this.destinationCodeExt=destinationCodeExt;
		unit=0; // correct?
		port=0; // correct?
		this.inputStream=inputStream;
		this.outputStream=outputStream;
	}
	
	/*
	 * START
	 * 1)GENERAL 
	 * work classes, build the framework to transmit the 245 data segments (header, checksum,...)
	 */
	public byte buildIdent(boolean ack, boolean first, boolean last, byte command){
		// Ack bit, first block bit, last block bit, R/W, 4 bit operation select (last five bits are set by command)
		if(ack)   {command=(byte) (command | 0x80);}else{command=(byte) (command & 0x7F);} // set or reset ack		
		if(first) {command=(byte) (command | 0x40);}else{command=(byte) (command & 0xBF);} // set or reset F
		if(last)  {command=(byte) (command | 0x20);}else{command=(byte) (command & 0xDF);} // set or reset L
		return command;
	}
	// constructs header
	public byte[] buildHeader(byte ident, int blocksize){
		byte[] header= new byte[10];
		// all of the following globals should be built before calling BuildHeader
		header[0]=ident;
		header[1]=(byte) (blocksize & 0x000000FF);
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
	public byte[] addCheckSum(byte[] total){
		int checkSum=0;
		byte[] totalcheck= new byte[total.length+1];
		for(byte b:total){
			checkSum=checkSum+(int) b;
		}
		totalcheck[total.length]=(byte) (256-(checkSum%256));
		for(int i=0; i<total.length;i++){ // deep copy of total
			totalcheck[i]=total[i];
		}
		return totalcheck;
	}
	
	// verify checksum, to be used by unit tests and data reception
	private boolean verifyCheckSum(byte[] dataToBeVerified){
		int checkSum=0;
		byte checkSumFinal;
		
		for (int i=0; i<(dataToBeVerified.length-1); i++){
			checkSum=checkSum+(int) dataToBeVerified[i];
		}
		checkSumFinal=(byte) (256-(checkSum%256));
		if (checkSumFinal==dataToBeVerified[dataToBeVerified.length-1]){
			return true; // checksum is ok
		}else return false; // checksum is not ok => reject
	}
	
	// blocks received are to be merged in one byte array block
	public byte[] blockMerging(byte[][] block){
		int totalLength=0;
		byte [] b;
		byte[] header;
		
		for (int i=0; i<block.length; i++){
			totalLength+=(block[i].length-11); // minus the header and checksum
		}
		b=new byte[totalLength+10]; //+1 header
		// put result in a vector
		int tel=10;
		for (byte[] bsm: block){
			for(int i=10; i<bsm.length-1; i++){
				b[tel++]=bsm[i];
			}
		}
		// add header
		ident = block[0][0];
		ident = buildIdent((ident & 0x80)==0x80,true,true,(byte) ((byte) ident & 0x1F));
		header= buildHeader(ident,(1+b.length)%256);
		for(int i=0; i<10; i++){
			b[i]=header[i];
		}
		// recalculate checksum
		b=addCheckSum(b);
		return b;
	}
	
	// press block array in the right frame sizes (10+245+1)=(header+data+checksum)
	// the block array should contain a header, because the header contains the ident
	public byte[][] blockProcessing(byte[] block){
		// cut block in the correct pieces
		//ArrayList<byte[]> blockProc = new ArrayList<byte[]>();
		byte[][]blockProc;
		int numOfBlocks;
		boolean ack=false;
		byte[] header;            // header of the frame
		byte[] blockSection;      // frame and header
		byte[] finalBlockSection; // header frame checksum
		int mod;
		
		// calculate number of blocks
		numOfBlocks=(int) Math.ceil(((double) (block.length-10))/245); // minus 10 to remove the checksum and the header
		// generate blockProck 2D matrix
		if(numOfBlocks==0){numOfBlocks=1;}
		blockProc = new byte[numOfBlocks][];
		if (numOfBlocks==1){
			blockProc[0]=addCheckSum(block); // generate and add checksum
		}else{
			// start building ident bits
			ident=block[0];
			if((ident & 0x80) == 0x80){
				ack=true; 
			}
			// adapt first ident using BuildIdent
			ident=buildIdent(ack, true, false, (byte) (ident & 0x1F)); // global ident adapted
			header = buildHeader(ident, 0); // header matrix construction
			blockSection = new byte[255];
			for(int i=0; i<255;i++){
				if(i<10)  {blockSection[i]=header[i];}     // add header
				if(i>=10) {blockSection[i]=block[i];}	   // add data			
			}
			finalBlockSection=addCheckSum(blockSection);   // checksum added to block
			blockProc[0]=finalBlockSection;				   // add frame segment to matrix
			// first block serialized
			
			//adapt ident for center frames
			if(numOfBlocks>2){
				ident=buildIdent(ack, false, false, (byte) (ident & 0x1F)); // change ident (introduce F and L bits)
				header = buildHeader(ident, 0);										// rebuild header
				blockSection = new byte[255];								// rebuild the subframe
				for (int i=1; i<(numOfBlocks-1); i++){
					for(int ii=0; ii<255;ii++){
						if(i<10)  {blockSection[i]=header[i];}     // add header
						if(i>=10) {blockSection[i]=block[i];}	   // add data			
					}
					finalBlockSection=addCheckSum(blockSection); // checksum added to block
					blockProc[i]=finalBlockSection;
				}
			}
			// adapt last ident
			mod=(block.length-10)-245*(numOfBlocks-1); // last data left over
			ident=buildIdent(ack, false, true, (byte) (ident & 0x1F));
			header = buildHeader(ident, (mod+11)%256);
			blockSection = new byte[mod+10]; // data + header
			for(int i=0; i<mod;i++){
				if(i<10)  {blockSection[i]=header[i];}     // add header
				if(i>=10) {blockSection[i]=block[i];}	   // add data			
			}
			finalBlockSection=addCheckSum(blockSection); // checksum added to block
			blockProc[numOfBlocks-1]=finalBlockSection; // final block added to array
		}
		return blockProc; 
	}
	/*
	 * END 
	 * 1)GENERAL chapter, pg 4 to 6, description of how the communication frames
	 *  should look like
	*/
	
	/*
	 * START data transmission 
	 */
	public Parsers transmitData(byte command, boolean ack, Parsers p) throws IOException{
		byte[] bs=new byte[0];
		byte[][] br;
		Parsers pr;
		// build command
		if(p==null){ // request of data from the meter (11 byte command)
			bs=buildHeader(buildIdent(ack, true,true,command), 11);	// checksum added in blockprocessing		
		}else{ // writing data to the meter
			bs=p.parseToByteArray();
		}
		sendData(bs);
		System.out.println();
		pr=buildCommand(addCheckSum(bs),p);
		br=receiveData(bs[0]);
		return buildCommand(blockMerging(br),pr);
	}

	/*
	 * Input readers 
	 */
	private byte[][] receiveData(byte ident) throws IOException {
		int i=0,counter,length;
		byte[][] data;
		ArrayList <String> recdat=new ArrayList<String>();
		boolean go=true, correct=false;
		String s;
		while(go){
			counter=0;
			length=11;
			s="";
			while(counter<length){	// timeout!
				i=inputStream.read();
				if(counter==0 && ((i & 0x1F)==ident)){
					correct=true;					
				}
				if(correct){
					s+=(char) i;
					if(counter==1){// block length byte
						length=i;
						if(i==0){length=256;} // modulus
					}
				}
				counter++;
			}
			if((s.charAt(0) & 0x0020)==0x20){ // check ident on last block to transmit
				go=false;
			}
			recdat.add(s);
		}
		//for(int ii=0; ii<11; ii++){
		//	i=inputStream.read(); // check not implemented.
		//}
		data=new byte[recdat.size()][];
		i=0;
		for(String str:recdat){
			data[i++]=Parsers.parseCArraytoBArray(str.toCharArray());
		}		
		return data;
	}

	/*
	 * output writers
	 */
	private void sendData(byte[] bs) throws IOException {
		byte[][] b=blockProcessing(bs); // cuts up the bs into the frames requested by the meter
		for(byte[] bp: b){
			outputStream.write(bp); // send all frames
		}
	}
	/*
	 * END Data transmission
	 */
		
	/*
	 * The following method buildCommand takes in a byte array and if needed a parser object
	 * when the parser object is a null, the byte array is considered a command to write to the meter
	 * when the parser object is a parser then the byte array is considered a request
	 * the request is parsed into the right object and that object is then returned
	 */
	public Parsers buildCommand(byte [] b, Parsers p) throws IOException { // feed with a command from the stream and objects are generated automatically
		// build command using the objects and parse it into a byte array
		// checksum error check
		// set p=null for transmission
		boolean checkSum=verifyCheckSum(b);
		if(b.length<11){throw new IOException("no data returned from the meter");}
		byte[] rawdata=new byte[b.length-11]; // strip header and checksum
		for (int i=10; i<b.length-1; i++){
			rawdata[i-10]=b[i];
		}
		if (checkSum==true){
			// here comes the processing
			if (p==null){
				// here comes the send
				/** don't forget to break up sequences */
				switch((b[0]&0x1F)){ // mask ident byte
					case fullPersTableRead:
						System.out.println("Get fullPersTable register");
						p = new MeteorFullPersonalityTable(); // make correct parser
						break;
					case fullPersTableWrite:
						System.out.println("Set fullPersTable register");
						break;
					case extendedPersTableRead:
						System.out.println("Get extendedPersTable register");
						p = new MeteorExtendedPersonalityTable(); // make correct parser
						break;
					case extendedPersTableWrite:
						System.out.println("Set extendedPersTable register");
						break;
					case readRTC:
						System.out.println("Get RTC register");
						p = new MeteorCLK();
						break;
					case setRTC:
						System.out.println("Set RTC register");
						break;
					case trimRTC:
						System.out.println("Trim RTC");
						// send data, no return requested
						break;
					case firmwareVersion:
						//System.out.println("Get Firmware Version");
						p=new MeteorFirmwareVersion();
						break;
					case status:
						System.out.println("Get Status register");
						p=new MeteorStatus();
						break;
					case readRelay:
						System.out.println("Get Relay register // command not available in this meter");
						break;
					case setRelay:
						System.out.println("Set Relay register // command not available in this meter");
						break;
					case meterDemands:
						System.out.println("Get meter demands");
						// first send command + instructions
						p=new MeteorReturnedReadMeterDemands();
						break;
					case writingTimes:
						System.out.println("Get writing times");
						break;
					case readdialReadingCurrent:
						System.out.println("Get dialReadingCurrent");
						p=new MeteorReadDialReadings();
						break;
					case writedialReadingCurrent:
						System.out.println("Set dialReadingCurrent");
						break;
					case dialReadingPast:
						System.out.println("Get dialReadingPast");
						p=new MeteorReadSavedDialReadings();
						break;
					case powerFailureDetails:
						System.out.println("Get powerFailureDetails");
						p= new MeteorPowerFailDetails();
						break;
					case readCommissioningCounters:
						System.out.println("Get CommissioningCounters");
						p= new MeteorCommissioningCounters();
						break;
					case writeCommissioningCounters:
						System.out.println("Set CommissioningCounters");
						break;
				}
				return p;
			}else{
			// here comes the reply...
				if(p instanceof MeteorFullPersonalityTable){	
					p=new MeteorFullPersonalityTable(rawdata);
				}else if(p instanceof MeteorExtendedPersonalityTable){	
					p=new MeteorExtendedPersonalityTable(rawdata);
				}else if(p instanceof MeteorCLK){
				}else if(p instanceof MeteorFirmwareVersion){
					p=new MeteorFirmwareVersion(rawdata);
				}else if(p instanceof MeteorStatus){
					p=new MeteorStatus(rawdata);
				}else if(p instanceof MeteorReturnedReadMeterDemands){
					p=new MeteorReturnedReadMeterDemands(rawdata);
				}else if(p instanceof MeteorReadDialReadings){
					p=new MeteorReadDialReadings(rawdata);
				}else if(p instanceof MeteorReadSavedDialReadings){
				}else if(p instanceof MeteorPowerFailDetails){
					p=new MeteorPowerFailDetails(rawdata);
				}else if(p instanceof MeteorCommissioningCounters){
				}
			return p;
			}
		}else{
			return null;
		}	
	}
	public byte getIdent() {
		return ident;
	}
	public void setIdent(byte ident) {
		this.ident = ident;
	}
}
