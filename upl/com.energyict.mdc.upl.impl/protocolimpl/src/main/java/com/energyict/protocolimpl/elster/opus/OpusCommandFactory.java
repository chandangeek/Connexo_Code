package com.energyict.protocolimpl.elster.opus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

public class OpusCommandFactory {
	
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

	private InputStream inputStream;
	private OutputStream outputStream;
	private String newPassword;
	private String oldPassword;
	private int outstationID;
	private int numChan;
	private int period;
	private int cap;
	private int dateOffset;


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
	
	public ArrayList<String[]> command(int command, int attempts, int timeOut) throws IOException{
		// maybe good to catch some of the errors here
		ArrayList<String[]> s=new ArrayList<String[]>();
		if     (command==3) {s=currentMonthCumulativeReadings(attempts, timeOut, numChan);}
		else if(command==4) {s=previousMonthCumulativeReadings(attempts, timeOut, numChan);}
		else if(command==5) {s=previousDayCumulativeReadings(attempts, timeOut, numChan);}
		else if(command>=10 && command<=69){
			// state machine 2
			retrievalOfDailyPeriodData(command,attempts,timeOut,numChan,dateOffset,cap);
		}
		else if(command==81) {s=currentDayPeriodData(attempts, timeOut, numChan, period, cap);}		
		else if(command==101){}
		else if(command==102){}
		else if(command==111){}
		else if(command==121){}
		else if(command==200){}
		else if(command==201){}
		else if(command==550){}
		else if(command==860){}
		else if(command==999){}
		else{throw new IOException("command unknown");}
		return s;
	}


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
		d=d.substring(d.length()-3); // parse with zeros or cut if necessary
		String c=""+cap;
		String[] data=dataArrayBuilder("0",d,c,"0","0","0",oldPassword,newPassword); // build data packet
		return stateMachine2(commandnr,attempts,timeOut,numChan,data);
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
	
	/*
	 * State Machines
	 */	
	private ArrayList<String[]> stateMachine1(int commandnr,int attempts, int timeOut, int numChan, String[] data) throws IOException{	
		int attempts1= attempts,attempts2=attempts, attempts3=attempts,attempts4=attempts;
		ArrayList<String[]> returnedData=new ArrayList<String[]>();
		boolean temp=true,loop=true; // to pass true or false flags from state to state
		OpusBuildPacket sendPacket,receivePacket;
		String s="";
        long interFrameTimeout;
		interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states
		int i=0; // returned data;
		int state=1; // start state;	
		int packetnr=0;
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
					state=acknak(3,1);
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

	private ArrayList<String[]> stateMachine2(int commandnr,int attempts, int timeOut, int numChan, String[] data) throws IOException{	
		int attempts1= attempts,attempts2=attempts, attempts3=attempts,attempts4=attempts;
		ArrayList<String[]> returnedData=new ArrayList<String[]>();
		boolean temp=true,loop=true; // to pass true or false flags from state to state
		OpusBuildPacket sendPacket,receivePacket;
		String s="";
        long interFrameTimeout;
		interFrameTimeout = System.currentTimeMillis() + timeOut; // timeout between states
		int i=0; // returned data;
		int state=1; // start state;	
		int packetnr=0;
		int channr=1;
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
					state=acknak(3,1);
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
				case 6:
					state=detectUnstable(data,temp,7,4);
					break;
				case 7:
					// channel header
					returnedData.add(data);  // data passed ack frame
					s=getStringArray();
					state=8;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					channr=Integer.valueOf(data[0].substring(5));
					break;
				case 8:
					// ENQ not implemented
					state=acknack(temp,9,7);
					break;
				case 9:
					returnedData.add(data);  // data passed ack frame
					s=getStringArray();
					state=8;
					receivePacket=new OpusBuildPacket(s.toCharArray());
					temp=receivePacket.verifyCheckSum();
					data=receivePacket.getData();
					break;
				case 10:
					state=acknack(temp,11,9);
					break;
				case 11:
					// last packet of 1 channel, go to 12 or 7 if not last channel
					state=7;
					if(channr==numChan){// last channel move to next state
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
	


	/*
	 * State Machine support functions
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

	// readers
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
		}else{
			outputStream.write(NAK);
			state=NAKstate;
		}
		return state;
	}
	private int acknak(int ACKstate, int NAKstate) throws IOException{
		int state=17; // will timeout
		int i = inputStream.read();
		if(i==ACK){
			state=ACKstate;
		}else{
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
	
}
