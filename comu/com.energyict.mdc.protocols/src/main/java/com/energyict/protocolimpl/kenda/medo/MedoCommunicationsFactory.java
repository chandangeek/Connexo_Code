/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.kenda.medo;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MedoCommunicationsFactory {
	/**
 	 * ---------------------------------------------------------------------------------<p>
	 * Medo CommunicationsFactory<p>
	 * <p>
	 * Starts with the attributes and constructors.  After the constructors all methods dealing with
	 * serialization can be found: building the headers, generating and checking the checksum, packing
	 * and unpacking the data blocks.  After the processing methods general purpose communication
	 * methods and dedicated methods (trim RTC and data download).  Under the communication methods the
	 * backbone of these methods can be found to send and receive data.  They throw timeout exceptions
	 * if necessary.  The last method deals with the parsing of the streams in the right objects.
	 * <p>
	 * building profile data and events is done in the method: retrieveProfileData
	 * <p>
	 *  Initial version:<p>
	 *  ----------------<p>
	 *  Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
	 *  Version: 1.0 <p>
	 *  First edit date: 1/07/2008 PST<p>
	 *  Last edit date: 13/08/2008  PST<p>
	 *  Comments: Beta ready for testing<p>
	 *  Released for testing: 13/08/2008<p>
	 *  <p>
	 *  Revisions<p>
	 *  ----------------<p>
	 *  Author: <p>
	 *  Version:<p>
	 *  First edit date: <p>
	 *  Last edit date: <p>
	 *  Comments:<p>
	 *  released for testing:<p>
	 * ---------------------------------------------------------------------------------<p>
	 */
	// command descriptions from the datasheet
	// Header format, at the moment I consider only ident as a variable
	private byte   ident;					// see ident format listed below
	private final byte[] sourceCode;		// Defines central equipment of origin
	private final byte   sourceCodeExt;		// Defines peripheral equipment of origin
	private final byte[] destinationCode;	// Defines central equipment of final destination
	private final byte   destinationCodeExt;// Defines peripheral equipment of final destination
	private byte   unit;					// DIP routing ???
	private byte   port;					// DIP routing ???

	private static final byte   fullPersTableRead          	=0x01;	  // ram initialised, table defines modes
	private static final byte   fullPersTableWrite         	=0x11;    // ... page 6/16 medo communications protocol
	private static final byte   partPersTableRead    	  	=0x02;
	private static final byte   partPersTableWrite     		=0x12;
	private static final byte   readRTC						=0x03;
	private static final byte   setRTC						=0x13;
	private static final byte   trimRTC						=0x14;
	private static final byte   firmwareVersion				=0x04;
	private static final byte   status						=0x05;
	private static final byte   readRelay					=0x16;
	private static final byte   setRelay					=0x06;
	private static final byte   meterDemands				=0x07;
	private static final byte   writingTimes				=0x19;
	private static final byte   readdialReadingCurrent		=0x0A;
	private static final byte   writedialReadingCurrent		=0x1A;
	private static final byte   dialReadingPast				=0x0B;
	private static final byte   powerFailDetails			=0x0C;
	private static final byte   readCommissioningCounters	=0x0D;
	private static final byte   writeCommissioningCounters	=0x1D;
	private InputStream inputStream;
	private OutputStream outputStream;
	private int retries=5;
	private long timeOut=5000; // 5000 milliseconds
	private int numChan=48; // hardwired to 48 for this meter
	private int[] channelMultipliers;
	private ProtocolChannelMap channelMap;
	private ProtocolChannelMap meterChannelMap;
	// first three bits are to be set in BuildIdent method (later)
	// byte: 8 bit, word 16 bit signed integer, long 32 bit signed integer
	private TimeZone timezone;

	public MedoCommunicationsFactory(InputStream inputStream, OutputStream outputStream){// blank constructor for testing purposes only
		byte[] blank={0,0};
		ident=0;				// see ident format listed below
		sourceCode=blank;		// Defines central equipment of origin
		sourceCodeExt=0;		// Defines peripheral equipment of origin
		destinationCode=blank;	// Defines central equipment of final destination
		destinationCodeExt=0;	// Defines peripheral equipment of final destination
		unit=0;					// DIP routing ???
		port=0;					// DIP routing ???
		this.inputStream=inputStream;
		this.outputStream=outputStream;
	}
	public MedoCommunicationsFactory(  // real constructor, sets header correct.
			byte[] sourceCode,
			byte sourceCodeExt,
			byte[] destinationCode,
			byte destinationCodeExt,
			InputStream inputStream,
			OutputStream outputStream){
		ident=0;
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
		if(ack)   {command=(byte) (command | 0x80);}else{command=(byte) (command & 0x7F);} // set or reset ack (ack packet is embedded in the command structure and sent to confirm receive)
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
		for(int ii=0; ii<total.length; ii++){
			byte b=total[ii];
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
		// checksum is ok
// checksum is not ok => reject
		return checkSumFinal == dataToBeVerified[dataToBeVerified.length - 1];
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
		for (int ii=0; ii<block.length; ii++){
			byte[] bsm=block[ii];
			for(int i=10; i<bsm.length-1; i++){
				b[tel++]=bsm[i];
			}
		}
		// add header
		ident = block[0][0];
		ident = buildIdent((ident & 0x80)==0x80,true,true,(byte) (ident & 0x1F));
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
							// 	adapt first ident using BuildIdent
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
	public Parsers transmitData(byte command, Parsers p) throws IOException{
		byte[] bs=new byte[0];
		byte[][] br=new byte[0][0];
		Parsers pr=null;
		boolean ack=false;
						// build command
		int pog=retries;
		while(!ack && pog>0){
			pog--;
			if(p==null){ 	// request of data from the meter (11 byte command)
				bs=buildHeader(buildIdent(ack, true,true,command), 11);	// checksum added in blockprocessing
			}else{ 			// writing data to the meter
				bs=p.parseToByteArray();
			}
			sendData(bs);
							// timeout
			pr=buildCommand(addCheckSum(bs),p);
			long interFrameTimeout = System.currentTimeMillis() + this.timeOut*100;
			br=receiveData((byte) (bs[0]& 0x1F));
							// send ack
			if((br[br.length-1][0]&0x20)==0x20){
				ack=true;
			}
	        if (System.currentTimeMillis() - interFrameTimeout > 0) {
	            throw new ProtocolConnectionException("Interframe timeout error");
	        }
			bs=buildHeader(buildIdent(ack, true,true,command), 11);	// checksum added in blockprocessing
			sendData(bs);
		}
		if(!ack){
			throw new IOException("Data transmission did not succeed, thrown by communicationsFactory->transmitData");
		}
		return buildCommand(blockMerging(br),pr);
	}
	/*
	 * specific requests and sets for the meter
	 */
	public void trimRTC(byte b) throws IOException{
		byte[] bs=new byte[11];
		byte[][] br;
		byte[] bs2=buildHeader(buildIdent(false, true,true,(byte) 0x14), 12);	// checksum added in blockprocessing
		boolean ack=false;
		int pog=retries;

		for(int i=0; i<bs2.length; i++){
			bs[i]=bs2[i];
		}
		bs[10]=b;
		System.out.println(bs.length);
		while(!ack && pog>0){
			pog--;
			sendData(bs);
			// receive ack
			br=receiveData((byte) (bs[0]& 0x1F));
			if((br[0][0] & 0x80)==0x80){ack=true;} // get acknowledge
		}
		if(!ack){
			throw new IOException("Data transmission did not succeed, thrown by communicationsFactory->transmitData");
		}
	}
	private short[][] requestMeterDemands(byte command, Date d1, Date d2, int intervaltime) throws IOException{
		byte[] bs=new byte[16];
		byte[][] br;
		byte[] bs2=buildHeader(buildIdent(false, true,true,command), 17);	// checksum added in blockprocessing
		byte[] data;
		short[][] shortData=new short[0][0];
		byte[] byteData=new byte[0];
		boolean ack=false;
		int pog=this.retries;
		Calendar start=Calendar.getInstance(timezone);
		Calendar stop=Calendar.getInstance(timezone);
		start.setTime(d1);
		stop.setTime(d2);
		MedoRequestReadMeterDemands mrrd;

		mrrd = new MedoRequestReadMeterDemands(start,stop,intervaltime);
		// 	ready to send
		data=mrrd.parseToByteArray();
		for(int i=0; i<bs2.length; i++){
			bs[i]=bs2[i];
			if(i<6){bs[i+bs2.length]=data[i];}
		}
		while(!ack && pog>0){
			int tel=0, subcounter=0;
			int poscount=0;
			pog--;
			sendData(bs);
			// receive ack
			br=receiveData((byte) (bs[0]& 0x1F));
			if((br[br.length-1][0]&0x20)==0x20){
				ack=true;
			}
			// deal with the data, cut header and checksum
			for(int ii=0; ii<br.length; ii++){
				byte[] bt= br[ii];
				tel+=bt.length-11;
			}
			byteData=new byte[tel];
			for(int ii=0; ii<br.length; ii++){
				byte[] bt= br[ii];
				for(int i=10; i<bt.length-1;i++){
					byteData[poscount++]=bt[i];
				}
			}
			// parse the data
			shortData=new short[byteData.length/(numChan*2)][numChan];
			short[] tempshort=Parsers.parseBArraytoSArray(byteData);
			for(int i=0; i<tempshort.length/numChan; i++){
				for(int ii=0; ii<numChan; ii++){
					shortData[i][ii]=tempshort[subcounter];
					subcounter++;
				}
			}
			// send back ack
			bs=buildHeader(buildIdent(ack, true,true,command), 11);	// checksum added in blockprocessing
			sendData(bs);
		}
		if(!ack){
			throw new IOException("Data transmission did not succeed, thrown by communicationsFactory->transmitData");
		}
		return shortData;
	}
	/*
	 * commands to get profile data, contains also flagging
	 */
	public short[][] getTotalDemands(Date start, Date stop, int intervaltime) throws IOException{

		return requestMeterDemands((byte) 0x08,start,stop,intervaltime);

	}
	public int[] retrieveLastProfileData(int intervaltime) throws IOException{
		MedoReadDialReadings mrdr;
		mrdr=(MedoReadDialReadings) transmitData(readdialReadingCurrent, null);
		return mrdr.getCnt();
	}
	public ProfileData retrieveProfileData(Date start, Date stop, int intervaltime, boolean addevents) throws IOException{
		ProfileData pd = new ProfileData();
		IntervalData id = new IntervalData();		// current interval data
		MeterEvent meterEvent;
		ArrayList meterEventList = new ArrayList();
		ArrayList medoCLK= new ArrayList();// meter event flagging parallel matrix
		short[][] s=new short[0][0];
		boolean flag=false, powdownflag=false, prevIntervalPowdownflag=false, lastdata=false;
		long millis=0;
		int ids=0;
		// set timezone and calendar object
		Calendar cal1 = Calendar.getInstance(timezone);
		//Calendar cal2 = Calendar.getInstance(timezone);
		cal1.setTime(start);
		//cal2.setTime(stop);
		// retrieve powerFailDetails and add meter events
		MedoPowerFailDetails mpfd=(MedoPowerFailDetails) transmitData(powerFailDetails, null);
		MedoCLK[] pfhist=mpfd.getPfhist();
		for(int ii=0; ii<pfhist.length; ii++){
			MedoCLK mclk= pfhist[ii];
			System.out.println(mclk.toString());
			if(mclk.checkValidity() && !lastdata){
				cal1=mclk.getCalendar();
				if(ii%2==0){
					meterEvent=new MeterEvent(cal1.getTime(),MeterEvent.POWERDOWN);
				}else{
					meterEvent=new MeterEvent(cal1.getTime(),MeterEvent.POWERUP);
				}
				meterEventList.add(meterEvent);
				medoCLK.add(mclk); // parallel power down matrix
			}else{
				lastdata=true;
			}
		}
		// reset cal1 object
		cal1 = Calendar.getInstance(timezone); // for security reasons , should be not needed
		cal1.setTime(start);
        ParseUtils.roundDown2nearestInterval(cal1,intervaltime);
        // get meter data

        Calendar start1=Calendar.getInstance(timezone);
		Calendar stop1=Calendar.getInstance(timezone);
		Calendar strt=Calendar.getInstance(timezone);
		Calendar stp=Calendar.getInstance(timezone);

        stop1.set(strt.get(Calendar.YEAR), 11, 31, 23, 59, 59);
		stop1.set(Calendar.MILLISECOND,999);
		start1.set(stp.get(Calendar.YEAR),0,1,0,0,0);
		start1.set(Calendar.MILLISECOND,0);

		if(strt.get(Calendar.YEAR)==stp.get(Calendar.YEAR)){
        	 s=requestMeterDemands((byte) 0x07,cal1.getTime(),stop,intervaltime);
        }else{ // december-january
        	int counter=0;
        	short[][] s1=requestMeterDemands((byte) 0x07,cal1.getTime(),stop1.getTime(),intervaltime);
        	short[][] s2=requestMeterDemands((byte) 0x07,start1.getTime(),stop,intervaltime);

        	s=new short[s1.length+s2.length][];

        	for(int i=0; i<s1.length; i++){
        		s[counter]=s1[i];
        		counter++;
        	}
        	for(int i=0; i<s2.length; i++){
        		s[counter]=s2[i];
        		counter++;
        	}

        }
		// build channel map in profile data
		for(int i=0; i<s[0].length;i++ ){
			if(channelMap.isProtocolChannelEnabled(i) && meterChannelMap.isProtocolChannelEnabled(i)){
				pd.addChannel(new ChannelInfo(ids, "medo channel "+(i+1), Unit.get(BaseUnit.UNITLESS),0,ids, BigDecimal.valueOf(channelMultipliers[i])));
				ids++; // will run parallel with i but is needed in case of a channelmap
			}
		}
		// first block of data can be skipped
		for(int i=0; i<s.length; i++){
			// read first meter for flagging
			powdownflag=false;
			flag=false;
			id=new IntervalData(cal1.getTime());  // add time and date to the interval
			for(int ii=0; ii<medoCLK.size(); ii++){
				MedoCLK m=(MedoCLK) medoCLK.get(ii);
				long timeInterval = (cal1.getTimeInMillis()-m.getCalendar().getTimeInMillis());
				if(timeInterval<intervaltime*1000 && timeInterval>=0){
					powdownflag=true;
				}
			}
			for(int ii=0; ii<s[i].length; ii++){
				if(s[i][ii]<0){ // end or begin of power down (here real data is still available
					s[i][ii]=(short) (0x7FFF & s[i][ii]); // mask negative bit
					if(!flag){
						// check meter events
						if(powdownflag){
							id.addEiStatus(IntervalStateBits.POWERDOWN); // when power down from event register, change in status can only be power down
							meterEvent=new MeterEvent(cal1.getTime(),MeterEvent.POWERDOWN);
							meterEventList.add(meterEvent);
							prevIntervalPowdownflag=true;
						}else{
							id.addEiStatus(IntervalStateBits.POWERUP); // no power down in event register, so this must be a power up
							meterEvent=new MeterEvent(cal1.getTime(),MeterEvent.POWERUP);
							meterEventList.add(meterEvent);
							prevIntervalPowdownflag=false;
						}
					}
					flag=true;
				}else if(s[i][ii]==0x3FFF){ // power is down (on all lines)
					s[i][ii]=0;
					if(!flag){
						flag=true;
						prevIntervalPowdownflag=true;
						if(powdownflag){
							id.addEiStatus(IntervalStateBits.POWERDOWN); // when power down from event register, change in status can only be power down
							meterEvent=new MeterEvent(cal1.getTime(),MeterEvent.POWERDOWN);
							meterEventList.add(meterEvent);
							prevIntervalPowdownflag=true;
						}else{
							id.addEiStatus(IntervalStateBits.MISSING);
						}
					}
				}else{
					// boundary condition
					if(prevIntervalPowdownflag && !flag){ // previous interval was power down and no negative value has been detected (boundary condition)
						prevIntervalPowdownflag=false;
						flag=true;
						id.addEiStatus(IntervalStateBits.POWERUP);
						meterEvent=new MeterEvent(cal1.getTime(),MeterEvent.POWERUP);
						meterEventList.add(meterEvent);
					}
				}
				// add value to profile data
				if(channelMap.isProtocolChannelEnabled(ii) && meterChannelMap.isProtocolChannelEnabled(ii)){
					id.addValue(new Integer(s[i][ii])); // add data to the interval
				}
			}
			pd.addInterval(id);
			millis=cal1.getTimeInMillis()+1000*intervaltime; // increment for next interval
			cal1.setTimeInMillis(millis);
		}
		//add meter events
		if(addevents){
			for(int ii=0; ii<meterEventList.size(); ii++){
				MeterEvent m= (MeterEvent) meterEventList.get(ii);
				pd.addEvent(m);
			}
		}
		// return profileData
		return pd;
	}
	/*
	 * End of profile data commands
	 */

	/*
	 * Input readers
	 */
	private byte[][] receiveData(byte ident) throws IOException {
		int i=0,counter,length;
		byte[][] data;
		ArrayList recdat=new ArrayList();
		boolean go=true;
		String s;
		long interFrameTimeout;
		int errorCounter=0;

			// time out check
		while(go){
			interFrameTimeout = System.currentTimeMillis() + this.timeOut;
	        if (System.currentTimeMillis() - interFrameTimeout > 0) {
	            throw new ProtocolConnectionException("Interframe timeout error");
	        }
			counter=0;
			length=11;
			s="";
			while(counter<length){	// timeout!
				i=inputStream.read();
				s+=(char) i;
				if(i==-1){errorCounter++;}
				if(errorCounter>5){
		            throw new ProtocolConnectionException("InterCharacter timeout error");
				}
				if(counter==1){// block length byte
					length=i;
					if(i==0){length=256;} // modulus
				}
				counter++;
			}
			if((s.charAt(0) & 0x0020)==0x20){ // check ident on last block to transmit
				go=false;
			}
			recdat.add(s);
		}
		data=new byte[recdat.size()][];
		i=0;
		for(int ii=0; ii<recdat.size(); ii++){
			String str= (String) recdat.get(ii);
			data[i++]=Parsers.parseCArraytoBArray(str.toCharArray());
		}
		return data;
	}

	/*
	 * output writers
	 */
	private void sendData(byte[] bs) throws IOException {
		byte[][] b=blockProcessing(bs); // cuts up the bs into the frames requested by the meter
		for(int ii=0; ii<b.length; ii++){
			byte[] bp= b[ii];
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
		if (b.length<11) {
			throw new IOException("no data returned from the meter");
		}
		byte[] rawdata=new byte[b.length-11]; // strip header and checksum
		for (int i=10; i<b.length-1; i++){
			rawdata[i-10]=b[i];
		}
		if (checkSum) {
			// here comes the processing
			if (p==null){
				// here comes the send
				/** don't forget to break up sequences */
				switch((b[0]&0x1F)){ // mask ident byte
					case fullPersTableRead:
						//System.out.println("Get fullPersTable register");
						p = new MedoFullPersonalityTable(); // make correct parser
						break;
					case fullPersTableWrite:
						System.out.println("Set fullPersTable register");
						break;
					case partPersTableRead:
						//System.out.println("Get extendedPersTable register");
//						p = new MedoExtendedPersonalityTable(); // make correct parser
						break;
					case partPersTableWrite:
						System.out.println("Set partPersTable register");
						break;
					case readRTC:
						//System.out.println("Get RTC register");
						p = new MedoCLK();
						break;
					case setRTC:
						//System.out.println("Set RTC register");
						break;
					case trimRTC:
						// System.out.println("Trim RTC");
						p=null;
						break;
					case firmwareVersion:
						//System.out.println("Get Firmware Version");
						p=new MedoFirmwareVersion();
						break;
					case status:
						//System.out.println("Get Status register");
						p=new MedoStatus();
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
						p=new MedoReturnedReadMeterDemands();
						break;
					case writingTimes:
						System.out.println("Get writing times");
						break;
					case readdialReadingCurrent:
						System.out.println("Get dialReadingCurrent");
						p=new MedoReadDialReadings();
						break;
					case writedialReadingCurrent:
						System.out.println("Set dialReadingCurrent");
						break;
					case dialReadingPast:
						System.out.println("Get dialReadingPast");
						p=new MedoReadSavedDialReadings();
						break;
					case powerFailDetails:
						p= new MedoPowerFailDetails();
						break;
					case readCommissioningCounters:
						System.out.println("Get CommissioningCounters");
						p= new MedoCommissioningCounters();
						break;
					case writeCommissioningCounters:
						System.out.println("Set CommissioningCounters");
						break;
				}
				return p;
			}else{
			// here comes the reply...
				if(p instanceof MedoFullPersonalityTable){
					p=new MedoFullPersonalityTable(rawdata, timezone);
//				}else if(p instanceof MedoExtendedPersonalityTable){
//					p=new MedoExtendedPersonalityTable(rawdata);
				}else if(p instanceof MedoCLK){
					MedoCLK c=new MedoCLK(rawdata,timezone);
					p = c;
				}else if(p instanceof MedoFirmwareVersion){
					p=new MedoFirmwareVersion(rawdata);
				}else if(p instanceof MedoStatus){
					p=new MedoStatus(rawdata, timezone);
				}else if(p instanceof MedoReturnedReadMeterDemands){
					p=new MedoReturnedReadMeterDemands(rawdata);
				}else if(p instanceof MedoReadDialReadings){
					p=new MedoReadDialReadings(rawdata);
				}else if(p instanceof MedoReadSavedDialReadings){
				}else if(p instanceof MedoPowerFailDetails){
					p=new MedoPowerFailDetails(rawdata, timezone);
//				}else if(p instanceof medoCommissioningCounters){
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
	public int getRetries() {
		return retries;
	}
	public void setRetries(int retries) {
		this.retries = retries;
	}
	public long getTimeOut() {
		return timeOut;
	}
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}
	public void setMultipliers(char[] dialexp, char[] dialmlt) {
		channelMultipliers=new int[numChan];
		for(int i=0; i<numChan; i++){
			channelMultipliers[i]=(int) Math.pow(10,(long) (-1*dialexp[i])); //(int) Math.pow(10,(long) dialexp[i])*dialmlt[i];  // CHANGE HERE THE MULTIPLIERS
		}
	}
	public void setChannelMap(ProtocolChannelMap channelMap) {
		this.channelMap=channelMap;
	}
	public void setTimeZone(TimeZone timezone) {
		this.timezone=timezone;
	}
	public int getNumChan() {
		return numChan;
	}
	public void setNumChan(int numChan) {
		this.numChan = numChan;
	}
	public ProtocolChannelMap getMeterChannelMap() {
		return meterChannelMap;
	}
	public void setMeterChannelMap(ProtocolChannelMap meterChannelMap) {
		this.meterChannelMap = meterChannelMap;
	}
}
