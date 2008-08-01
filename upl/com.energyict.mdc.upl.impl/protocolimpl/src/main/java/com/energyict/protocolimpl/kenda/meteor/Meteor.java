package com.energyict.protocolimpl.kenda.meteor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

public class Meteor implements MeterProtocol{

	private OutputStream outputStream;
	private InputStream inputStream;
	private int DEBUG=0;
	private MeteorCommunicationsFactory mcf;
	private int outstationID;
	boolean ack=false;
	
	// command descriptions from the datasheet
	// Header format, at the moment I consider only ident as a variable
	private byte   ident;					// see ident format listed below
	private byte   blockSize;	     		// character count of block modulo 256	
	private byte[] sourceCode;		// Defines central equipment of origin
	private byte   sourceCodeExt;		// Defines peripheral equipment of origin
	private byte[] destinationCode;	// Defines central equipment of final destination
	private byte   destinationCodeExt;// Defines peripheral equipment of final destination
	private byte   unit;					// DIP routing ???
	private byte   port;					// DIP routing ???
	
	// ident byte
	// Ack bit, first block bit, last block bit, R/W, 4 bit operation select
	
	// operation select codes, lowest 5 bit of ident
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
	// first three bits are to be set in BuildIdent method (later)
	// byte: 8 bit, word 16 bit signed integer, long 32 bit signed integer
	
	/*
	 * constructors
	 */
	public Meteor(){// blank constructor for testing purposes only
		byte[] blank={0,0};
		ident=0;				// see ident format listed below
		blockSize=11;			// character count of block modulo 256	
		sourceCode=blank;		// Defines central equipment of origin
		sourceCodeExt=0;		// Defines peripheral equipment of origin
		destinationCode=blank;	// Defines central equipment of final destination
		destinationCodeExt=0;	// Defines peripheral equipment of final destination
		unit=0;					// DIP routing ???
		port=0;					// DIP routing ???		
	}
	public Meteor(  // real constructor, sets header correct.
			byte[] sourceCode, 
			byte sourceCodeExt, 
			byte[] destinationCode, 
			byte destinationCodeExt){
		ident=0;
		this.sourceCode=sourceCode;
		this.sourceCodeExt=sourceCodeExt;
		this.destinationCode=destinationCode;
		this.destinationCodeExt=destinationCodeExt;
		unit=0; // correct?
		port=0; // correct?
	}

//	protected ProtocolConnection doInit(InputStream inputStream,
//			OutputStream outputStream, int timeoutProperty,
//			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
//			int protocolCompatible, Encryptor encryptor,
//			HalfDuplexController halfDuplexController) throws IOException {
//		
//		this.inputStream=inputStream;
//		this.outputStream=outputStream;
//							
//		return null;
//	}
	
	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		MeteorFirmwareVersion mfv=(MeteorFirmwareVersion) mcf.transmitData(firmwareVersion, ack, null);
		return mfv.getVersion();
	}

	public String getProtocolVersion() {
		return "$Date: 2008-07-01 08:35:14 +0200 (di, 01 jul 2008) $";
	}

	public Date getTime() throws IOException {
		// TODO Auto-generated method stub
		return new Date(System.currentTimeMillis());
	}

	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}
	public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
			Logger arg3) throws IOException {
		// set streams
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        // build command factory
		this.mcf=new MeteorCommunicationsFactory(inputStream,outputStream);
	}
	public void connect() throws IOException {
		// TODO Auto-generated method stub
		
	}
	public void disconnect() throws IOException {
		// TODO Auto-generated method stub
		
	}
	public Object fetchCache(int arg0) throws SQLException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}
	public Object getCache() {
		// TODO Auto-generated method stub
		return null;
	}
	public Quantity getMeterReading(int arg0) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}
	public Quantity getMeterReading(String arg0) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}
	public int getNumberOfChannels() throws UnsupportedException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	public ProfileData getProfileData(boolean arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	public ProfileData getProfileData(Date arg0, boolean arg1)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	public ProfileData getProfileData(Date arg0, Date arg1, boolean arg2)
			throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}
	public int getProfileInterval() throws UnsupportedException, IOException {
		//TODO
		return 1800;
	}
	public String getRegister(String arg0) throws IOException,
			UnsupportedException, NoSuchRegisterException {
		// TODO Auto-generated method stub
		return null;
	}
	public void initializeDevice() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		
	}
	public void release() throws IOException {
		// TODO Auto-generated method stub
		
	}
	public void setCache(Object arg0) {
		// TODO Auto-generated method stub
		
	}
	public void setProperties(Properties properties) throws InvalidPropertyException,
			MissingPropertyException {
		this.outstationID = Integer.parseInt(properties.getProperty("NodeAddress", "000"));		
	}
	public void setRegister(String arg0, String arg1) throws IOException,
			NoSuchRegisterException, UnsupportedException {
		// TODO Auto-generated method stub
		
	}
	public void updateCache(int arg0, Object arg1) throws SQLException,
			BusinessException {
		// TODO Auto-generated method stub
		
	}
	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		return list;
	}
	public List getRequiredKeys() {
		ArrayList list = new ArrayList();
		return list;
	}
	public boolean isAck() {
		return ack;
	}
	public void setAck(boolean ack) {
		this.ack = ack;
	}
}
