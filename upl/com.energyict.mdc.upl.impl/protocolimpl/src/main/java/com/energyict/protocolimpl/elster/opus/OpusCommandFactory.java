package com.energyict.protocolimpl.elster.opus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

public class OpusCommandFactory {
	/*
	 * Commands are executed in 3 layers
	 * 1) command number is used to call the correct method
	 * 2) method generates the data sequence needed for the transmission
	 * 3) the correct state machine connected to the command is selected and the connection is made
	 * 
	 * -> the return is a data array (List)
	 */
	
	/*
	 * Static Final Attributes
	 */
	// ASCII commands used in command control and communication
	private static final char SOH =0x0001;  // start of heading
	private static final char STX =0x0002;  // start of text
	private static final char ETX =0x0003;  // end of text
	private static final char EOT =0x0004;  // end of transmission 
	private static final char ENQ =0x0005;  // enquiry
	private static final char ACK =0x0006;  // acknowledge
	private static final char CR  =0x000D;  // carriage return
	private static final char XON =0x0011;  // instruction packet control characters
	private static final char XOFF=0x0013;  // instruction packet control characters
	private static final char NAK =0x0021;  // negative acknowledge

	/*
	 * private Attributes
	 */
	private InputStream inputStream;
	private OutputStream outputStream;
	private String newPassword;
	private String oldPassword;
	private int outstationID;
	private int numChan=1;
	private int period=1;
	private int cap=0;
	private int dateOffset=0;
	private boolean infoOnly=false;
	private boolean com121=false;
	

	/*
	 * Constructor (no empty constructor allowed)
	 */
	OpusCommandFactory(int outstationID, 
			String oldPassword, 
			String newPassword,
			InputStream inputStream,
			OutputStream outputStream){
		this.outstationID=outstationID;
		this.oldPassword=oldPassword;
		this.newPassword=newPassword;
		this.inputStream=inputStream;
		this.outputStream=outputStream;
	}
	
	/*
	 * 1) Processing of the command (after making an instance of the class)
	 */
	public ArrayList<String[]> command(int command, int attempts, int timeOut, Calendar cal) throws IOException{
		// maybe good to catch some of the errors here
		ArrayList<String[]> s=new ArrayList<String[]>();
		if     (command==3) {s=currentMonthCumulativeReadings(attempts, timeOut, numChan);}
		else if(command==4) {s=previousMonthCumulativeReadings(attempts, timeOut, numChan);}
		else if(command==5) {s=previousDayCumulativeReadings(attempts, timeOut, numChan);}
		else if(command>9 && command<70){
			// state machine 2
			s=retrievalOfDailyPeriodData(command,attempts,timeOut,numChan,dateOffset,cap);
		}
		else if(command==81) {s=currentDayPeriodData(attempts, timeOut, numChan, period, cap);}
		else if(command==101){s=synchronizeOutstation(attempts, timeOut);}
		else if(command==102){s=fetchTimeDateFromOutstation(attempts,timeOut);}
		else if(command==111){s=retrievalDeltaMinAdvance(attempts, timeOut,cal,numChan);} // use command as deltamin
		else if(command==121){s=writeReadControlOutstation(attempts, timeOut);}
		//TODO command implementation
		else if(command==200){s=null;}
		else if(command==550){s=null;}// commands for PPM stations, so far not implemented
		else if(command==860){s=null;}
		else if(command==999){s=null;}
		// test commands
		//else if(command==1)  {s=identificationData(attempts,timeOut);}
		else{throw new IOException("command unknown");}
		return s;
	}


	/*
	 * 2) array and data builders + State Machine selection
	 */
	private ArrayList<String[]> currentMonthCumulativeReadings(int attempts, int timeOut, int numChan) throws IOException {
		String[] data=dataArrayBuilder("0","0","0","0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine1(3,attempts,timeOut,numChan,data);
	}
	private ArrayList<String[]> previousMonthCumulativeReadings(int attempts, int timeOut, int numChan) throws IOException {
		String[] data=dataArrayBuilder("0","0","0","0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine1(4,attempts,timeOut,numChan,data);
	}
	private ArrayList<String[]> previousDayCumulativeReadings(int attempts, int timeOut, int numChan) throws IOException {
		String[] data=dataArrayBuilder("0","0","0","0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine1(5,attempts,timeOut,numChan,data);
	}
	private ArrayList<String[]> retrievalOfDailyPeriodData(int commandnr,int attempts, int timeOut, int numChan, int offset, int cap) throws IOException {
		String d=""+offset;
		String c=""+cap;
		String[] data=dataArrayBuilder("0",d,c,"0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine2(commandnr,attempts,timeOut,data);
	}
	private ArrayList<String[]> currentDayPeriodData(int attempts,int timeOut, int numChan,int period, int cap) throws IOException {
		// check 001 thing, comes from log-files
		String s="000"+period;
		s=s.substring(s.length()-3); // parse with zeros or cut if necessary
		String c=""+cap;
		s=c.substring(s.length()-3); // parse with zeros or cut if necessary
		String[] data=dataArrayBuilder(s,"0",c,"0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine1(81,attempts,timeOut,numChan,data);
	}
	private ArrayList<String[]> synchronizeOutstation(int attempts, int timeOut) throws IOException{
		// build calendar object
		Calendar cal;
		cal=Calendar.getInstance();
		String[] data=dataArrayBuilder(cal,oldPassword,newPassword); // build data packet
		return stateMachine3(101,attempts,timeOut,data);
	}
	private ArrayList<String[]> fetchTimeDateFromOutstation(int attempts, int timeOut) throws IOException{
		// build calendar object
		String[] data=dataArrayBuilder("0","0","0","0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine3(102,attempts,timeOut,data);
	}
	private ArrayList<String[]> retrievalDeltaMinAdvance(int attempts,int timeOut, Calendar cal, int numChan) throws IOException {
		// change calendar to 3 min interval number
		int deltamin=generateDeltamin(cal);
		String[] data=dataArrayBuilder(""+deltamin,"0","0","0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine4(111,attempts,timeOut,data,numChan);
	}
	private ArrayList<String[]> writeReadControlOutstation(int attempts,int timeOut) throws IOException {
		// only read cycles implemented for security reasons
		String[] data=dataArrayBuilder("48","0","0","0","0","1",oldPassword,newPassword); // build data packet
		// 48 at position 0 must be there, 1 at position 5 indicates that all values are to be ignored + read operation
		com121=true;
		ArrayList<String[]> s=stateMachine3(121,attempts,timeOut,data);
		com121=false;
		return s;
	}
//	private ArrayList<String[]> identificationData(int attempts, int timeOut) throws IOException {
//		String[] data=dataArrayBuilder("0","0","0","0","0","0",oldPassword,newPassword); // build data packet
//		return stateMachine3(1,attempts,timeOut,data);
//	}

	
	/*
	 * 3) State Machines, can be dumped in objects!! work with overriding on each state, here it is implemented as methods
	 */	
	private ArrayList<String[]> stateMachine1(int commandnr,int attempts, int timeOut, int numChan, String[] data) throws IOException{	
		int attempts1= attempts,attempts2=attempts, attempts3=attempts,attempts4=attempts;  // number of retries
		ArrayList<String[]> returnedData=new ArrayList<String[]>();		// data stack
		boolean temp=true,loop=true; 									// to pass true or false flags from state to state and end the loop
		OpusBuildPacket sendPacket,receivePacket;  						// packet frameworks for send and receive packets
		String s="";													// string stack for data reception
        long interFrameTimeout;											// timout
		int i=0; 														// returned data, is stacked on s
		int state=1; 													// start state;	
		int packetnr=0;													// automatic packet numbering
		interFrameTimeout = System.currentTimeMillis() + timeOut; 		// timeout between states
		while(loop){			
			// time out check
	        if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {	        	
	            throw new ProtocolConnectionException("Interframe timeout error");
	        }
	        // last loop attempt
	        if(attempts1==0){
	        	loop=false; // counter
	        }
			interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states reset
			switch(state){
				case 1:
					// build INSTRUCTION packet
					sendPacket=new OpusBuildPacket(this.outstationID,commandnr,packetnr,data,true);
					sendPacket.setInstructionPacket(); // add XON
					// send packet
					outputStream.write(sendPacket.getByteArray());
					// next state
					state=2;
					// minus one attempt
					packetnr++;
					attempts1--;
					break;
				case 2:	
					state=acknak(3,1,2);
					break;
				case 3:
					outputStream.write(STX);
					state=4;
					break;
				case 4:
					s=getStringArray();
					state=5;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					attempts2--;
					break;
				case 5: // following states are done in state 10
				case 6:
				case 7:
				case 8:
				case 9:
				case 10:
					state=detectUnstable(data,temp,11,4);
					break;
				case 11:					
					returnedData.add(data);
					s=getStringArray();
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					state=12;
					attempts3--;					
					break;
				case 12:
					state=acknack(temp,13,11);
					break;
				case 13:
					returnedData.add(data);
					s=getStringArray();
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					state=14;
					attempts4--;					
					break;
				case 14:
					if(temp){
						outputStream.write(ACK);
						returnedData.add(data);
						numChan-=8;// decrement number of channels
						state=15;
						if(numChan>0){// loop not yet passed
							state=11;
							attempts3++;
							attempts4++;
						}
					}else{
						outputStream.write(NAK);
						state=13;
					}
					break;
				case 15:
					// all data transferred
				case 16:
					i=inputStream.read();
					if(i==EOT){
						state=17;
					}
					break;
				case 17:
					outputStream.write(CR);
					loop=false;
					// in fact not necessary
					break;			
			}	
		}
		return returnedData;
	}
	// second state machine, commands 10-69
	private ArrayList<String[]> stateMachine2(int commandnr,int attempts, int timeOut, String[] data) throws IOException{
		int attempts1= attempts,attempts2=attempts, attempts3=attempts,attempts4=attempts;
		ArrayList<String[]> returnedData=new ArrayList<String[]>();
		boolean temp=true,loop=true; // to pass true or false flags from state to state
		OpusBuildPacket sendPacket,receivePacket;
		String s="";
        long interFrameTimeout;
		int i=0; // returned data;
		int state=1; // start state;	
		int packetnr=0;
		int channr=0;
		int numChan=1;
		int datanr=0;
		int numData=8; 
		interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states
		while(loop){			
			// time out check
	        if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {	        	
	            throw new ProtocolConnectionException("Interframe timeout error");
	        }
	        // last loop attempt
	        if(attempts1==0){
	        	loop=false; // counter
	        }
			interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states reset
			switch(state){
				case 1:
					// build INSTRUCTION packet
					sendPacket=new OpusBuildPacket(this.outstationID,commandnr,packetnr,data,true);
					sendPacket.setInstructionPacket(); // add XON
					// send packet
					outputStream.write(sendPacket.getByteArray());
					// next state
					state=2;
					// minus one attempt
					packetnr++;
					attempts1--;
					break;
				case 2:	
					state=acknak(3,1,2);
					break;
				case 3:
					outputStream.write(STX);
					state=4;
					break;
				case 4:
					s=getStringArray();
					state=5;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					// set number of channels
					if(temp){
						numChan=Integer.parseInt(data[6]);
						// 	set number of data samples per day
						numData=Integer.parseInt(data[5]);
					}else{
						loop=false;
						throw new IOException("connection properties are probably wrong or noise on connection");
					}
					attempts2--;
					break;
				case 5:
				case 6:
					if(infoOnly){ // make unstable
						if(!(data[2].equals("00") && data[3].equals("00") && data[4].equals("00"))){
							data[2]="00";
							data[3]="00";
							data[4]="00";
							returnedData.add(data);
							loop=false;
						}
					}
					state=detectUnstable(data,temp,7,4);
					if(state==7){returnedData.add(data);}  // data passed ack frame
					break;
				case 7:
					// channel header
					datanr=0;
					s=getStringArray();
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					if(!temp){attempts3--;}
					data=receivePacket.getData();
					channr=Integer.parseInt(data[0].substring(5));
					state=8;
					break;
				case 8:
					// ENQ not implemented
					state=acknack(temp,9,7);
					if(state==9){returnedData.add(data);}  // data passed ack frame, retries not included
					break;
				case 9:
					s=getStringArray();
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					if(!temp){attempts4--;}
					data=receivePacket.getData();
					datanr+=8;
					state=10;
					break;
				case 10:
					state=acknack(temp,11,9);
					if(state==11){returnedData.add(data);}
					break;
				case 11:
					// last packet of 1 channel, go to 12 or 7 if not last channel
					state=7;
					if(datanr<numData){
						state=9;
					}
					if(channr==numChan && datanr>=numData){// last channel move to next state
						state=12;
					}
					break;
				case 12:
					i=inputStream.read();
					if(i==EOT){
						state=13;
					}
					break;
				case 13:
					outputStream.write(CR);
					loop=false;
					// in fact not necessary
					break;
			}
		}
		return returnedData;
	}
	
	// state machine 3 commands 101 and 102
	private ArrayList<String[]> stateMachine3(int commandnr,int attempts, int timeOut, String[] data) throws IOException{
		ArrayList<String[]> returnedData=new ArrayList<String[]>();
		int attempts1= attempts, attempts2=attempts;
		boolean temp=true,loop=true; // to pass true or false flags from state (checksum) to state
		OpusBuildPacket sendPacket,receivePacket;
		String s="";
        long interFrameTimeout;
		int state=1; // start state;	
		int packetnr=0;
		// build time and date data package
		interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states
		
		
		while(loop){			
			// time out check
	        if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {	        	
	            throw new ProtocolConnectionException("Interframe timeout error");
	        }
	        // last loop attempt
	        if(attempts1==0 || attempts2==0){
	        	loop=false; // counter
	        }
			interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states reset
			switch(state){
				case 1:
					// build INSTRUCTION packet
					sendPacket=new OpusBuildPacket(this.outstationID,commandnr,packetnr,data,true);
					sendPacket.setInstructionPacket(); // add XON
					// 	send packet
					outputStream.write(sendPacket.getByteArray());
					// next state
					state=2;
					// minus one attempt
					packetnr++;
					attempts1--;
					break;
				case 2:	
					state=acknak(3,1,2);
					break;
				case 3:
					outputStream.write(STX);
					state=4;
					break;
				case 4:
				case 5: // 5-7-9 implement the same data structures, 4 is different but could not be serparted from this unit
				case 7:
				case 9: 
					s=getStringArray();
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum(); // checksum test (apply before parsing)
					data=receivePacket.getData();
					if(s.charAt(0)==EOT){
						state=11;
					}else if(temp){
						state=10;						
					}else{
						state=1;
					}
					attempts2--;
					break;
				case 10:
					if(temp){
						if(this.com121){outputStream.write(ACK);};
						outputStream.write(EOT);
						state=11;
					}else{
						outputStream.write(NAK);
						state=9;
					}
					break;
				case 11:
					returnedData.add(data);
					outputStream.write(CR);
					loop=false;
					break;
			}
		}
		return returnedData;
	}
	// state machine 4, connected to command 111
	private ArrayList<String[]> stateMachine4(int commandnr,int attempts, int timeOut, String[] data,int numChan) throws IOException{
		ArrayList<String[]> returnedData=new ArrayList<String[]>();
		int attempts1= attempts, attempts2=attempts, attempts3=attempts, attempts4=attempts*3;
		boolean temp=true,loop=true; // to pass true or false flags from state (checksum) to state
		OpusBuildPacket sendPacket,receivePacket;
		String s="";
        long interFrameTimeout;
		int state=1; // start state;	
		int packetnr=0;
		int channr=0;
		int i=0;
		// build time and date data package
		interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states
		while(loop){			
			// time out check
	        if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {	        	
	            throw new ProtocolConnectionException("Interframe timeout error");
	        }
	        // last loop attempt
	        if(attempts1==0 || attempts2==0 || attempts3==0 || attempts4==0){
	        	loop=false; // counter
	        }
			interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states reset
			switch(state){
				case 1:
					// build INSTRUCTION packet
					sendPacket=new OpusBuildPacket(this.outstationID,commandnr,packetnr,data,true);
					sendPacket.setInstructionPacket(); // add XON
					// 	send packet
					outputStream.write(sendPacket.getByteArray());
					// 	next state
					state=2;
					// 	minus one attempt
					packetnr++;
					attempts1--;
					break;
				case 2:	
					state=acknak(3,1,2);
					break;
				case 3:
					outputStream.write(STX);
					state=4;
					break;
				case 4:
					s=getStringArray();
					state=5;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					attempts2--;
					break;
				case 5:
					if(temp){// check temp in order to have no baud rate problems
						// deltamin conversion to time
						if(Integer.valueOf(data[0])==000 || Integer.valueOf(data[0])==999 ){
							outputStream.write(EOT);
							loop=false;							
						}else{
							outputStream.write(ACK);
							state=6;
						}
						data=generateTimefromDeltamin(Integer.valueOf(data[0]));
						returnedData.add(data);
					}else{
						outputStream.write(NAK);
						state=4;
					}
					break;
				case 6:
					// read channel number
					s=getStringArray();
					state=7;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					channr=Integer.parseInt(data[0].substring(5));					
					attempts3--;
					break;
				case 7:
					// ENQ not implemented
					state=acknack(temp,8,6);
					if(state==8){returnedData.add(data);attempts3++;}
					break;
				case 8:
					// read channel number
					s=getStringArray();
					state=9;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					attempts4--;
					break;
				case 9:					
					state=acknack(temp,10,8);
					if(state==10){
						returnedData.add(data);
						attempts4++;
						if(data[7].charAt(0)=='?'){ // end of channel data
							state=6;
							if(channr==numChan){state=14;} // jump to next state when all channels are read
						}
						if(data[0].charAt(0)=='S'){ // in case 8-16 measurements are taken, then no '?' is detected!!!!
							state=8; 
						}
					}
					break;
				case 10:
					s=getStringArray();
					state=11;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					attempts4--;
					break;					
				case 11:
					state=acknack(temp,12,10);
					if(state==12){
						returnedData.add(data);
						attempts4++;
						if(data[7].charAt(0)=='?'){ // end of channel data
							state=6;
							if(channr==numChan){state=14;} // jump to next state when all channels are read
						}
						if(data[0].charAt(0)=='S'){// in case 8-16 measurements are taken, then no '?' is detected!!!!
							state=8;
						}
					}
					break;					
				case 12:
					s=getStringArray();
					state=13;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					attempts4--;
					break;					
				case 13:
					state=acknack(temp,6,12);
					if(state==6){returnedData.add(data);attempts4++;}
					if(data[0].charAt(0)=='S'){
						state=8;
					}
					if(channr==numChan){state=14;} // jump to next state when all channels are read
					break;					
				case 14:
					i=inputStream.read();
					if(i==EOT){state=15;}
					break;
				case 15:
					outputStream.write(CR);
					loop=false;
					// in fact not necessary
					break;			}		
		}
		return returnedData;
	}
	/*
	 * ---------------------------------------------------------------------------------
	 * Support functions
	 * ---------------------------------------------------------------------------------
	 */
	
	/*
	 * State Machine support and data build support functions
	 */
	private int generateDeltamin(Calendar cal) {
		return (int) (20*cal.get(Calendar.HOUR_OF_DAY)+Math.floor(cal.get(Calendar.MINUTE)/3));
	}
	private String[] generateTimefromDeltamin(int deltamin){
		String[] data = new String[8];
		deltamin*=3;
		data[0]=""+Calendar.getInstance().get(Calendar.YEAR);
		data[1]=""+Calendar.getInstance().get(Calendar.MONTH);
		data[2]=""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		data[3]=""+(int) Math.floor(deltamin/12);
		data[4]=""+((deltamin%3)-3);
		data[5]=""+0;
		data[6]=""+0;
		data[7]=""+0;
		return data;
	}
	private String[] dataArrayBuilder(String string1, String string2,
			String string3, String string4, String string5, String string6,
			String string7, String string8) {
		String data[]=new String[8];
		data[0]=string1;
		data[1]=string2;
		data[2]=string3;
		data[3]=string4;
		data[4]=string5;
    	data[5]=string6;
		data[6]=string7;
		data[7]=string8;
		return data;
	}
	private String[] dataArrayBuilder(Calendar cal, String oldPassword,
			String newPassword) {
		String[] data=new String[8];
		data[0]=""+cal.get(Calendar.HOUR_OF_DAY);
		data[1]=""+cal.get(Calendar.MINUTE);
		data[2]=""+cal.get(Calendar.SECOND);
		data[3]=""+cal.get(Calendar.DAY_OF_MONTH);
		data[4]=""+cal.get(Calendar.MONTH);
		data[5]=""+cal.get(Calendar.YEAR);
		data[6]=oldPassword;
		data[7]=newPassword;
		return data;
	}
	
	/*
	 * State machine read and write functions
	 */
	private int detectUnstable(String[] data, boolean temp, int i, int j) throws IOException {
		int state=1;
		if((data[2].equals("00") && data[3].equals("00") && data[4].equals("00") || (data[0]=="000"))){
			// data unstable
			outputStream.write(EOT);
			// introduce timeout pause
			state=1;
		}else{ 
			state=acknack(temp,i,j);
		}			
		return state;
	}	
	private String getStringArray() throws IOException {
		int i=0;
		String s="";					
		while(i!=ETX){	// timeout!
			i=inputStream.read();
			s+=(char) i;
		}
		return s;
	}
	private int acknack(boolean temp,int ACKstate, int NAKstate) throws IOException {
		int state;
		if(temp){
			outputStream.write(ACK);
			state=ACKstate;
		}else {
			outputStream.write(NAK);
			state=NAKstate;
		}
		return state;
	}
	private int acknak(int ACKstate, int NAKstate,int curstate) throws IOException{
		int state=curstate; // will timeout
		int i = inputStream.read();
		if(i==ACK){
			state=ACKstate;
		}else if(i==NAK){
			// checksum error
			state=NAKstate;
		}	
		return state;
	}
	
	/*
	 * Setters and Getters
	 */	
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public int getOutstationID() {
		return outstationID;
	}

	public void setOutstationID(int outstationID) {
		this.outstationID = outstationID;
	}

	public int getNumChan() {
		return numChan;
	}

	public void setNumChan(int numChan) {
		this.numChan = numChan;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}
	public int getCap() {
		return cap;
	}

	public void setCap(int cap) {
		this.cap = cap;
	}

	public int getDateOffset() {
		return dateOffset;
	}

	public void setDateOffset(int dateOffset) {
		this.dateOffset = dateOffset;
	}
	public boolean isInfoOnly() {
		return infoOnly;
	}

	public void setInfoOnly(boolean infoOnly) {
		this.infoOnly = infoOnly;
	}
	
}
