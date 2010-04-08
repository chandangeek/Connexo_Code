package com.energyict.protocolimpl.eig.nexus1272;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.cbo.Unit;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.eig.nexus1272.command.AbstractCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.eig.nexus1272.parse.NexusDataParser;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySetting;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySettingFactory;

public class Nexus1272 extends AbstractProtocol  {

	private InputStream inputStream;
	private NexusProtocolConnection connection;
	private OutputStream outputStream;

	List <LinePoint> masterlpMap = new ArrayList<LinePoint>();;
	List <LinePoint> mtrlpMap = null;
	List<LinePoint> chnlpMap = new ArrayList<LinePoint>();
	private long start;
	private int numChannels;
	private ScaledEnergySettingFactory sesf;
	private String channelMapping;


	public Nexus1272() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doConnect() throws IOException {
		// TODO Auto-generated method stub
		start = System.currentTimeMillis();
		authenticate();
	}

	@Override
	protected void doDisConnect() throws IOException {
		long duration = System.currentTimeMillis() - start;
		System.out.println("Took " + duration + " ms");

	}

	@Override
	protected List doGetOptionalKeys() {
		ArrayList al = new ArrayList();
		al.add("NexusChannelMapping");
		return al;
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		// TODO Auto-generated method stub
		connection = new NexusProtocolConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,getLogger());
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		return connection;
	}

	@Override
	protected void doValidateProperties(Properties properties)
	throws MissingPropertyException, InvalidPropertyException {
		channelMapping = properties.getProperty("NexusChannelMapping","");
		
		if (!channelMapping.equals(""))
			chnlpMap = processChannelMapping(channelMapping);
		
		
		for (LinePoint mylp : chnlpMap) {
			System.out.println(mylp.toString());
		}
		System.out.println(channelMapping);
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		String fwVersion = "";
		Command command = NexusCommandFactory.getFactory().getCommBootVersionCommand();
		outputStream.write(command.build());
		baos.write(connection.receiveWriteResponse(command).toByteArray());
		
		command = NexusCommandFactory.getFactory().getCommRunVersionCommand();
		outputStream.write(command.build());
		baos.write(connection.receiveWriteResponse(command).toByteArray());
		
		command = NexusCommandFactory.getFactory().getDSPBootVersionCommand();
		outputStream.write(command.build());
		baos.write(connection.receiveWriteResponse(command).toByteArray());
		
		command = NexusCommandFactory.getFactory().getDSPRunVersionCommand();
		outputStream.write(command.build());
		baos.write(connection.receiveWriteResponse(command).toByteArray());

		NexusDataParser ndp = new NexusDataParser(baos.toByteArray());
		fwVersion += ndp.parseF2() + ".";
		fwVersion += ndp.parseF2() + ".";
		fwVersion += ndp.parseF2() + ".";
		fwVersion += ndp.parseF2();
		
//		byte[] send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x48,0x00,0x02};
//		outputStream.write(send);
//		//		outputStream.write(calcCheckSum(send));
//
//		byte[] byteArray = connection.receiveResponse().toByteArray();
//		fwVersion += parseF2(byteArray) + ".";
//
//
//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x00,0x00,0x02};
//		outputStream.write(send);
//		byteArray = connection.receiveResponse().toByteArray();
//
//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x00,0x00,0x02};
//		outputStream.write(send);
//		outputStream.write(new byte[]{0x0b, (byte) 0xc4});
//		byteArray = connection.receiveResponse().toByteArray();
//
//
//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x4a,0x00,0x02};
//		outputStream.write(send);
//		byteArray = connection.receiveResponse().toByteArray();
//		fwVersion += parseF2(byteArray) + ".";
//
//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x4c,0x00,0x02};
//		outputStream.write(send);
//		byteArray = connection.receiveResponse().toByteArray();
//		fwVersion += parseF2(byteArray) + ".";
//
//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x4e,0x00,0x02};
//		outputStream.write(send);
//		byteArray = connection.receiveResponse().toByteArray();
//		fwVersion += parseF2(byteArray) + ".";

		return fwVersion;
	}

	@Override
	public String getProtocolVersion() {
		return "$Revision: 1.0 $";
	}

	@Override
	public Date getTime() throws IOException {
//		byte[] send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x54,0x00,0x04};
//		outputStream.write(send);
//		byte[] byteArray = connection.receiveResponse().toByteArray();
//		//		System.out.println(parseF18(new ByteArrayInputStream(byteArray), byteArray.length));
//
//		System.out.println(parseF3(byteArray));
//		
		Command command = NexusCommandFactory.getFactory().getGetTimeCommand();
		outputStream.write(command.build());
		NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(command).toByteArray());
		return ndp.parseF3();
	}

	@Override
	public void setTime() throws IOException {
		authenticate();
		Command command = NexusCommandFactory.getFactory().getSetTimeCommand();
		outputStream.write(command.build());
		connection.receiveWriteResponse(command);
	}

	@Override
	public int getNumberOfChannels() throws IOException {
		if (mtrlpMap == null) {
			Command command = NexusCommandFactory.getFactory().getDataPointersCommand();
			outputStream.write(command.build());
			mtrlpMap = processPointers(connection.receiveWriteResponse(command).toByteArray());
		}
		numChannels = mtrlpMap.size();
		return numChannels;

	}

	public static void main(String[] args) throws IOException {
		//		byte[] startingAddress = new byte[] {(byte) 0xFF, 0x20};
		//		int ii = 0;
		//		int i = ProtocolUtils.getShort(startingAddress, 0);
		//		try {
		//			ii = ProtocolUtils.getShort(new ByteArrayInputStream (startingAddress));
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		byte[] send = new byte[]{0x01,0x03,0x00,0x00,0x00,0x02};
		//		Nexus1272 clazz = new Nexus1272(); 
		//		clazz.bais = new ByteArrayInputStream(send);
		//		int x = clazz.xxx(clazz.bais);
		//		int y = clazz.xxx(clazz.bais);
		//		int z = clazz.xxx(clazz.bais);
		Calendar cal = Calendar.getInstance();
//		int century = cal.get(Calendar.YEAR)/100;
//		int year = cal.get(Calendar.YEAR)%100;
//		int month = cal.get(Calendar.MONTH+1);
//		int day = cal.get(Calendar.DAY_OF_MONTH);
//		int hour = cal.get(Calendar.HOUR_OF_DAY);
//		int minute = cal.get(Calendar.MINUTE);
//		int second = cal.get(Calendar.SECOND);
//		int tenMilli = cal.get(Calendar.MILLISECOND)/10;
//		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
//		
//		
//		byte century2 = (byte) (cal.get(Calendar.YEAR)/100);
//		byte year2 = (byte) (cal.get(Calendar.YEAR)%100);
//		byte month2 = (byte) cal.get(Calendar.MONTH+1);
//		byte day2 = (byte) cal.get(Calendar.DAY_OF_MONTH);
//		byte hour2 = (byte) cal.get(Calendar.HOUR_OF_DAY);
//		byte minute2 = (byte) cal.get(Calendar.MINUTE);
//		byte second2 = (byte) cal.get(Calendar.SECOND);
//		byte tenMilli2 = (byte) (cal.get(Calendar.MILLISECOND)/10);
//		byte dayOfWeek2 = (byte) cal.get(Calendar.DAY_OF_WEEK);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte []{(byte) 0x0f, (byte) 0x9f});
		System.out.println((int)ProtocolUtils.getLong(bais, 2));
		
		"".toString();
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
//		authenticate();
//		RegisterValue rv;
//		rv = readRegister(new ObisCode(1,1,1,8,1,255));
//		rv = readRegister(new ObisCode(1,1,2,8,1,255));
//		rv = readRegister(new ObisCode(1,1,1,8,2,255));
//		rv = readRegister(new ObisCode(1,1,2,8,2,255));
//		rv = readRegister(new ObisCode(1,1,1,8,3,255));
//		rv = readRegister(new ObisCode(1,1,2,8,3,255));
//		rv = readRegister(new ObisCode(1,1,1,8,0,255));
//		rv = readRegister(new ObisCode(1,1,2,8,0,255));
//		rv = readRegister(new ObisCode(1,1,3,8,1,255));
//		rv = readRegister(new ObisCode(1,1,4,8,1,255));
//		rv = readRegister(new ObisCode(1,1,3,8,2,255));
//		rv = readRegister(new ObisCode(1,1,4,8,2,255));
//		rv = readRegister(new ObisCode(1,1,3,8,3,255));
//		rv = readRegister(new ObisCode(1,1,4,8,3,255));
//		rv = readRegister(new ObisCode(1,1,1,2,1,255));
//		rv = readRegister(new ObisCode(1,1,2,2,1,255));
//		rv = readRegister(new ObisCode(1,1,1,2,2,255));
//		rv = readRegister(new ObisCode(1,1,2,2,2,255));
//		rv = readRegister(new ObisCode(1,1,1,2,3,255));
//		rv = readRegister(new ObisCode(1,1,2,2,3,255));
//		rv = readRegister(new ObisCode(1,1,1,6,1,255));
//		rv = readRegister(new ObisCode(1,1,2,6,1,255));
//		rv = readRegister(new ObisCode(1,1,1,6,2,255));
//		rv = readRegister(new ObisCode(1,1,2,6,2,255));
//		rv = readRegister(new ObisCode(1,1,1,6,3,255));
//		rv = readRegister(new ObisCode(1,1,2,6,3,255));
//		rv = readRegister(new ObisCode(1,1,13,4,0,255));
//		rv = readRegister(new ObisCode(1,1,32,7,124,255));
//		rv = readRegister(new ObisCode(1,1,52,7,124,255));
//		rv = readRegister(new ObisCode(1,1,72,7,124,255));
//		rv = readRegister(new ObisCode(1,1,31,7,124,255));
//		rv = readRegister(new ObisCode(1,1,51,7,124,255));
//		rv = readRegister(new ObisCode(1,1,71,7,124,255));
//		rv = readRegister(new ObisCode(1,1,83,8,50,255));
//		rv = readRegister(new ObisCode(1,1,83,8,70,255));
//		rv = readRegister(new ObisCode(1,1,83,8,90,255));
//		rv = readRegister(new ObisCode(1,1,83,8,49,255));
//		rv = readRegister(new ObisCode(1,1,83,8,69,255));
//		rv = readRegister(new ObisCode(1,1,83,8,89,255));
//		rv = readRegister(new ObisCode(1,1,32,7,0,255));
//		rv = readRegister(new ObisCode(1,1,52,7,0,255));
//		rv = readRegister(new ObisCode(1,1,72,7,0,255));
//		rv = readRegister(new ObisCode(1,1,81,7,10,255));
//		rv = readRegister(new ObisCode(1,1,81,7,21,255));
//		rv = readRegister(new ObisCode(1,1,81,7,2,255));
//		rv = readRegister(new ObisCode(1,1,31,7,0,255));
//		rv = readRegister(new ObisCode(1,1,51,7,0,255));
//		rv = readRegister(new ObisCode(1,1,71,7,0,255));
//		rv = readRegister(new ObisCode(1,1,81,7,4,255));
//		rv = readRegister(new ObisCode(1,1,81,7,15,255));
//		rv = readRegister(new ObisCode(1,1,81,7,26,255));

		
		if (channelMapping.equals(""))
			throw new IOException("NexusChannelMapping custom property must be set to read profile data");

		sesf = new ScaledEnergySettingFactory(outputStream, connection);
		
		ProfileData profileData = new ProfileData();
		buildChannelInfo(profileData);
	    buildIntervalData(profileData,from, to);
	    if (includeEvents) {
	    	buildEventLog(profileData,from,to);
	        profileData.applyEvents(getProfileInterval()/60);
	    }
	        
	    profileData.sort();
	    return profileData;
		
		
//		authenticate();
//		LogReader 
//		lr = new SystemLogReader(outputStream, connection);
//		lr.readLog(from);
		
//		
//		
		//getLogger().info("call overrided method getProfileData("+from+","+includeEvents+")");  
//		getLogger().info("--> here we read the profiledata from the meter and construct a profiledata object");  

		//		pd = new ProfileData();



		

		//		 
		//		
		//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x11, 0x01,0x10,0x00,0x54,0x00,0x05, 0x0a, 0x14, 0x0a, 0x03, 0x03, 0x0f, 27, 0x25, 0x00, 0x00, 0x04};
		////		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06, 0x01,0x06, (byte) 0xE0, 0x01, 0x00, 0x01};
		//		outputStream.write(send);
		//		byteArray = connection.receiveResponse().toByteArray();
		//
		//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,0x00,0x54,0x00,0x04};
		//		outputStream.write(send);
		//		byteArray = connection.receiveResponse().toByteArray();
		//		System.out.println(parseF3(byteArray));

//		final int sysLogWindowIndexAddress = 38154;
//		final int sysLogHeaderStartAddress = 37504;
//		final int sysLogHeaderEndAddress = 37522;
//		final int sysLogWindowStartAddress = 38912;
//		final int sysLogWindowEndAddress = 38976;
//		final int sysLogWindowModeAddress = 38218;
//
//		byte[] logData = downloadLog(sysLogWindowIndexAddress, sysLogWindowModeAddress, sysLogHeaderStartAddress, sysLogHeaderEndAddress, sysLogWindowStartAddress, sysLogWindowEndAddress);
//		parseSystemLog(logData);

		//		final int historical2LogWindowIndexAddress = 38145;
		//		final int historical2LogHeaderStartAddress = 36928;
		//		final int historical2LogHeaderEndAddress = 36946;
		//		final int historical2LogWindowStartAddress = 38336;
		//		final int historical2LogWindowEndAddress = 38400;
		//		final int historical2LogWindowModeAddress = 38209;
		//		logData = downloadLog(historical2LogWindowIndexAddress, historical2LogWindowModeAddress, historical2LogHeaderStartAddress, historical2LogHeaderEndAddress, historical2LogWindowStartAddress, historical2LogWindowEndAddress);
		//		parseHistorical2Log(logData);
		//		
		//		final int limitTriggerLogWindowIndexAddress = 38146;
		//		final int limitTriggerLogHeaderStartAddress = 36992;
		//		final int limitTriggerLogHeaderEndAddress = 37010;
		//		final int limitTriggerLogWindowStartAddress = 38400;
		//		final int limitTriggerLogWindowEndAddress = 38464;
		//		final int limitTriggerLogWindowModeAddress = 38210;
		//		byte[] limitTriggerLogData = downloadLog(limitTriggerLogWindowIndexAddress, limitTriggerLogWindowModeAddress, limitTriggerLogHeaderStartAddress, limitTriggerLogHeaderEndAddress, limitTriggerLogWindowStartAddress, limitTriggerLogWindowEndAddress);
		//		
		//		final int limitSnapshotLogWindowIndexAddress = 38147;
		//		final int limitSnapshotLogHeaderStartAddress = 37056;
		//		final int limitSnapshotLogHeaderEndAddress = 37074;
		//		final int limitSnapshotLogWindowStartAddress = 38464;
		//		final int limitSnapshotLogWindowEndAddress = 38528;
		//		final int limitSnapshotLogWindowModeAddress = 38211;
		//		byte[] limitSnapshotLogData = downloadLog(limitSnapshotLogWindowIndexAddress, limitSnapshotLogWindowModeAddress, limitSnapshotLogHeaderStartAddress, limitSnapshotLogHeaderEndAddress, limitSnapshotLogWindowStartAddress, limitSnapshotLogWindowEndAddress);
		//		
		//		parseLimitLog(limitTriggerLogData, limitSnapshotLogData);

//		return null;
	}

	 /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
   public RegisterValue readRegister(ObisCode obisCode) throws IOException {
       ObisCodeMapper ocm = new ObisCodeMapper(NexusCommandFactory.getFactory(), connection, outputStream);
       return ocm.getRegisterValue(obisCode);
   }
   
   public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
       return ObisCodeMapper.getRegisterInfo(obisCode);
   }
	

	private void buildEventLog(ProfileData profileData, Date from, Date to) throws IOException {
		LogReader 
		lr = new SystemLogReader(outputStream, connection);
		byte[] byteArray = lr.readLog(from);
		lr.parseLog(byteArray, profileData);
		List<MeterEvent> meterEvents = ((SystemLogReader)lr).getMeterEvents();
		profileData.setMeterEvents(meterEvents);
		//FIXME Read Event registers (low batt, etc)
		
		lr = new LimitTriggerLogReader(outputStream, connection);
		lr.readLog(from);
		lr = new LimitSnapshotLogReader(outputStream, connection);
		lr.readLog(from);
	}

	private void buildIntervalData(ProfileData profileData, Date from, Date to) throws IOException {
		LogReader lr = new Historical2LogReader(outputStream, connection, mtrlpMap, masterlpMap, sesf);
		byte[] ba = lr.readLog(from);
		lr.parseLog(ba, profileData);
		
	}

	private void buildChannelInfo(ProfileData profileData) throws IOException {
//		get Scaled Energy Setting
//		Command getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
//		((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, 0x00});//Q1234 VAh/ Q12 VARh
//		outputStream.write(getSES.build());
//		byte[] resp = connection.receiveWriteResponse(getSES).toByteArray();
//		System.out.println("1 ***************** " + ProtocolUtils.byte2int(resp[0]));
//		System.out.println("1 ***************** " + ProtocolUtils.byte2int(resp[1]));
//		System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int(resp[0]), 8));
//		System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int((byte) (resp[0]&0x07)), 8));
//		System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int((byte) ((byte) (resp[0]&0x18)>>3)), 8));
//		System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07))), 3));
//		System.out.println(ProtocolUtils.byte2int((byte) (resp[0]&0x07)));
//		System.out.println(ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03))));
//		System.out.println(ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07))));
//		
//		((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x01});//Q34 VARh/ Q14 Wh
//		outputStream.write(getSES.build());
//		 resp = connection.receiveWriteResponse(getSES).toByteArray();
//		System.out.println("11 ***************** " + ProtocolUtils.byte2int(resp[0]));
//		System.out.println("11 ***************** " + ProtocolUtils.byte2int(resp[1]));
//		
//		((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x04});//Q23 Wh/ Q2 VAh
//		outputStream.write(getSES.build());
//		 resp = connection.receiveWriteResponse(getSES).toByteArray();
//		System.out.println("111 ***************** " + ProtocolUtils.byte2int(resp[0]));
//		System.out.println("111 ***************** " + ProtocolUtils.byte2int(resp[1]));
		if (mtrlpMap == null) {
			Command command = NexusCommandFactory.getFactory().getDataPointersCommand();
			outputStream.write(command.build());
			mtrlpMap = processPointers(connection.receiveWriteResponse(command).toByteArray());
		}
		
		buildMasterLPMap();
		
		
//		for (LinePoint lp : mtrlpMap) {
//			if (lp.isScaled()) {
//				ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
//				System.out.println(ses.toString());
//			}			
//		}
		
		
		for (LinePoint lp : masterlpMap) {
//		for (int channel=0;channel<numChannels;channel++) {
			ScaledEnergySetting ses = null;
			Unit unit = Unit.getUndefined();
			if (lp.isScaled()) {
				ses = sesf.getScaledEnergySetting(lp);
				unit = ses.getUnit();
			}
			int channel = lp.getChannel()-1;
			ChannelInfo ci = new ChannelInfo(channel, "Nexus1272_channel_"+channel, unit);
			try {
				profileData.addChannel(ci);
			} catch (IndexOutOfBoundsException ioe) {
				throw new IndexOutOfBoundsException("Channel mapping custom property must start at 1 and not skip channels");
			}
		}
		
	}
	
	private void buildMasterLPMap() throws IOException {
		for (LinePoint lp : chnlpMap) {
			boolean added = false;
			for (LinePoint lp2 : mtrlpMap) {
				if (lp.getLine() == lp2.getLine() && lp.getPoint() == lp2.getPoint()) {
					added = true;
					masterlpMap.add(lp);
					break;
				}
			}
			if (!added)
			throw new IOException("Line " + lp.getLine() + " Point " + lp.getPoint() + " not found in the meter's log");
		}
		
	}

	private List <LinePoint> processChannelMapping(String channelMapping2) throws InvalidPropertyException {
		List <LinePoint> ret = new ArrayList <LinePoint> ();
		String[] lpStrs = channelMapping2.split(",");
		for (int i = 0; i<lpStrs.length; i++) {
			String[] chnLPStr = lpStrs[i].split("=");
			if (chnLPStr.length != 2)
				throw new InvalidPropertyException("Malformed Nexus Channel Mapping custom property");
			int chan = Integer.parseInt(chnLPStr[0]);
			String toSplit = chnLPStr[1];
			String[] lpStr = toSplit.split("\\.");
			if (lpStr.length != 2)
				throw new InvalidPropertyException("Malformed Nexus Channel Mapping custom property");
			LinePoint lp = new LinePoint(Integer.parseInt(lpStr[0]), Integer.parseInt(lpStr[1]), chan);
			ret.add(lp);
		}
		return ret;
	}

	private List<LinePoint> processPointers(byte[] ba) throws IOException {
		//TODO This is in Historical2LogReader as well
		
		int offset = 0;
		List <LinePoint> lpMap = new ArrayList <LinePoint> ();
		//		for (int i = 0; i<4; i++){
		while (offset <= ba.length-4) {	
			if (ba[offset] == -1 && ba[offset+1] == -1) {
				offset += 4;
				continue;
			}
			int line = ProtocolUtils.getInt(ba, offset, 2);
			offset+=2;
			int point = ProtocolUtils.getInt(ba, offset, 1);
			offset+=2;
			lpMap.add(new LinePoint(line, point));
		}
		return lpMap;
	}

	@Override
	protected void validateSerialNumber() throws IOException {
		if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
		Command command = NexusCommandFactory.getFactory().getSerialNumberCommand();
		outputStream.write(command.build());
		byte[] data = connection.receiveWriteResponse(command).toByteArray();

		NexusDataParser ndp = new NexusDataParser(data);
		
		String sn = ndp.parseSN();
		if (sn.compareTo(getInfoTypeSerialNumber()) != 0) 
			throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
	}

	

	private boolean authenticate() throws IOException {

		Command command = NexusCommandFactory.getFactory().getAuthenticationCommand();
		outputStream.write(command.build());
		connection.receiveWriteResponse(command);

		command = NexusCommandFactory.getFactory().getVerifyAuthenticationCommand();
		outputStream.write(command.build());
		byte[] data = connection.receiveWriteResponse(command).toByteArray();
		//		NexusDataParser ndp = new NexusDataParser(payload);

		byte[] send;
		byte[] byteArray;

		//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,(byte) 0xff,0x28,0x00,0x01};
		//		outputStream.write(send);
		//		byteArray = connection.receiveResponse().toByteArray();

		//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x17, 0x01,0x10,(byte) 0xFF,0x20,0x00,0x08, 0x10, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x32, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
		//		outputStream.write(send);
		//		byteArray = connection.receiveResponse().toByteArray();
		//
		//		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,(byte) 0xff,0x28,0x00,0x01};
		//		outputStream.write(send);
		//		byteArray = connection.receiveResponse().toByteArray();
		//
		//		if (byteArray[byteArray.length-1] != 0x04 )
		//			return false;

		//FIXME confirm authentication
		return true;
	}



	int recSize;

	private byte[] downloadLog(int windowIndexAddress, int windowModeAddress, int headerStartAddress, int headerEndAddress, int windowStartAddress, int windowEndAddress) throws IOException {

		byte[] test;
		byte addrHigh;
		byte addrLow;
		byte[] send;
		int len;
		byte[] byteArray;
		int address;
		int toRead;
		byte dataHigh;
		byte dataLow;
		byte[] data;

		//	1. Read the Nexus® meter's Programmable Settings Block (Registers 45057–53248). This information will
		//	   be used to interpret the data retrieved from the log.
		//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//		toRead = 53248-45056;
		//		while (toRead > 0) {
		//			address = 45056;
		//			test = intToByteArray(address);
		//			addrHigh = test[0];
		//			addrLow = test[1];
		//			len = (byte) (toRead > 125 ? 0x7d : toRead);
		//			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		//			outputStream.write(send);
		//			byteArray = connection.receiveResponse().toByteArray();
		//			byte [] ba2 = new byte[byteArray.length - 9]; 
		//			System.arraycopy(byteArray, 8, ba2, 0, ba2.length);
		//			baos.write(ba2);
		//			toRead -= 125;
		//			address+= 125;
		//
		//		}


		//	2. Pause the log by writing an initial, non-FFFFH value to the Log Window Index Register.
		if (authenticate()) {
			test = AbstractCommand.intToByteArray(windowIndexAddress);
			addrHigh = test[0];
			addrLow = test[1];
			data = AbstractCommand.intToByteArray(0);
			dataHigh = data[0];
			dataLow = data[1];
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
		}



		//	3. Read and store the Log Header information.
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		toRead = headerEndAddress-headerStartAddress;
		address = headerStartAddress;
		while (toRead > 0) {
			test = AbstractCommand.intToByteArray(address);
			addrHigh = test[0];
			addrLow = test[1];
			len = (byte) (toRead > 125 ? 0x7d : toRead);
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
			byte [] ba2 = new byte[byteArray.length - 9]; 
			System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
			baos2.write(ba2);
			toRead -= 125;
			address+= 125;
		}
		byteArray = baos2.toByteArray();
		int offset = 0;
		int length = 4;
		long memsize = parseF18(byteArray, offset, length);
		offset += length;
		length = 2;
		int recordSize = parseF51(byteArray, offset, length);
		//TODO fix this global needed for historical parsing
		recSize = recordSize;
		offset += length;
		int firstIndex = parseF51(byteArray, offset, length);
		offset += length;
		int lastIndex = parseF51(byteArray, offset, length);
		offset += length;
		length = 8;
		Date firstTimeStamp = parseF3(byteArray, offset);
		offset += length;
		Date lastTimeStamp = parseF3(byteArray, offset);
		offset += length;
		length = 8;
		long validBitmap = parseF18(byteArray, offset, length);
		offset += length;
		length = 2;
		int maxRecords = parseF51(byteArray, offset, length);

		//	4. Determine the starting Window Index and Window offset.
		int startWindowIndex = (recordSize * firstIndex) / 128;
		int startWindowOffset = (recordSize * firstIndex) % 128;

		//	5. Determine the largest Window Index and Window offset.
		int largestWindowIndex = (recordSize * maxRecords) / 128;
		int largestWindowOffset = (recordSize * maxRecords) % 128;

		//	6. Determine the ending Window Index and Window offset.
		int endWindowIndex = ( recordSize * (lastIndex + 1) ) / 128;
		int endWindowOffset = ( recordSize * (lastIndex + 1) ) % 128;

		System.out.println("Reading from start window index " + startWindowIndex + " offset " + startWindowOffset + " to end window index" +
				endWindowIndex + " offset " + endWindowOffset);


		//	7. Set the Window Mode to Download Mode.
		if (authenticate()) {
			test = AbstractCommand.intToByteArray(windowModeAddress);
			addrHigh = test[0];
			addrLow = test[1];
			data = AbstractCommand.intToByteArray(0);
			dataHigh = data[0];
			dataLow = data[1];
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
		}

		//	8. Set the Log Window Index to the starting Window Index.
		int windowIndex = startWindowIndex;
		if (authenticate()) {
			test = AbstractCommand.intToByteArray(windowIndexAddress);
			addrHigh = test[0];
			addrLow = test[1];
			data = AbstractCommand.intToByteArray(windowIndex);
			dataHigh = data[0];
			dataLow = data[1];
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
		}
		//	9. Read the Window from starting offset to the end of the Window.
		ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
		address = windowStartAddress + startWindowOffset/2;
		test = AbstractCommand.intToByteArray(address);
		addrHigh = test[0];
		addrLow = test[1];
		len = windowEndAddress - address;
		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		outputStream.write(send);
		byteArray = connection.receiveResponse().toByteArray();
		byte [] ba2 = new byte[byteArray.length - 9]; 
		System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
		baos3.write(ba2);

		//		10. Increment the Window Index.
		windowIndex++;

		//	12. Repeat steps 10 and 11 until the largest or ending Window Index is reached.
		//	     —If the largest is reached, go to step 13.
		//	     —If the ending is reached, go to step 15.
		while (windowIndex != endWindowIndex) {


			if (windowIndex == largestWindowIndex) {
				//	13. Read window from beginning up to (but not including) the largest offset.
				if (authenticate()) {
					test = AbstractCommand.intToByteArray(windowIndexAddress);
					addrHigh = test[0];
					addrLow = test[1];
					data = AbstractCommand.intToByteArray(windowIndex);
					dataHigh = data[0];
					dataLow = data[1];
					send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
					outputStream.write(send);
					byteArray = connection.receiveResponse().toByteArray();
				}

				test = AbstractCommand.intToByteArray(windowStartAddress);
				addrHigh = test[0];
				addrLow = test[1];
				len = largestWindowOffset/2;
				send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
				outputStream.write(send);
				byteArray = connection.receiveResponse().toByteArray();
				ba2 = new byte[byteArray.length - 9]; 
				System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
				baos3.write(ba2);
				//	14. Set Window Index to 0. Go to step 12.
				//set to -1 because we increment it to 0 next time around...
				windowIndex = -1;
				continue;
			}

			//	11. Read the Window from beginning to end.
			if (authenticate()) {
				test = AbstractCommand.intToByteArray(windowIndexAddress);
				addrHigh = test[0];
				addrLow = test[1];
				data = AbstractCommand.intToByteArray(windowIndex);
				dataHigh = data[0];
				dataLow = data[1];
				send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
				outputStream.write(send);
				byteArray = connection.receiveResponse().toByteArray();
			}

			test = AbstractCommand.intToByteArray(windowStartAddress);
			addrHigh = test[0];
			addrLow = test[1];
			len = windowEndAddress - windowStartAddress;
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
			ba2 = new byte[byteArray.length - 9]; 
			System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
			baos3.write(ba2);

			windowIndex++;

		} 

		//	15. Read Window from the beginning up to (but not including) the ending offset.
		if (authenticate()) {
			test = AbstractCommand.intToByteArray(windowIndexAddress);
			addrHigh = test[0];
			addrLow = test[1];
			data = AbstractCommand.intToByteArray(windowIndex);
			dataHigh = data[0];
			dataLow = data[1];
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
		}

		test = AbstractCommand.intToByteArray(windowStartAddress);
		addrHigh = test[0];
		addrLow = test[1];
		len = endWindowOffset/2;
		send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x03,addrHigh,addrLow, 0x00, (byte) len};
		outputStream.write(send);
		byteArray = connection.receiveResponse().toByteArray();
		ba2 = new byte[byteArray.length - 9]; 
		System.arraycopy(byteArray, 9, ba2, 0, ba2.length);
		baos3.write(ba2);


		//	16. Un-pause the log by writing FFFFH to the Log Window Index Register.
		if (authenticate()) {
			test = AbstractCommand.intToByteArray(windowIndexAddress);
			addrHigh = test[0];
			addrLow = test[1];
			dataHigh = (byte)0xFF;
			dataLow = (byte)0xFF;
			send = new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x06,addrHigh,addrLow,dataHigh,dataLow};
			outputStream.write(send);
			byteArray = connection.receiveResponse().toByteArray();
		}

		//		int i=0;
		//		while (i<5) {
		//			parseSystemLog(baos3.toByteArray());
		//			i++;
		//		}
		return baos3.toByteArray();

		//		while (true) {
		//			testParse(baos2.toByteArray());
		//		}
	}

	private long parseF18(InputStream dataInStream, int len) throws IOException {
		dataInStream.skip(9);
		long val = 	ProtocolUtils.getLong((ByteArrayInputStream)dataInStream, len-9);
		return val;

	}

	private long parseF18(byte[] bArray, int offset, int len) throws IOException {
		long val = ProtocolUtils.getLong(bArray, offset, len);
		return val;

	}

	private int parseF51(byte[] bArray, int offset, int len) throws IOException {
		int val = ProtocolUtils.getInt(bArray, offset, len);
		return val;

	}

	private int parseF64(byte[] bArray, int offset) throws IOException {
		return parseF64(bArray, offset, 4);
	}
	private int parseF64(byte[] bArray, int offset, int len) throws IOException {
		int val = ProtocolUtils.getInt(bArray, offset, len);
		return val;

	}
	private Date parseF3(byte[] byteArray, int offset) throws IOException {
		int century = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int year = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int month = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int day = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int hour = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int minute = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int second = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();
		int tenMilli = ParseUtils.getBigInteger(byteArray, offset++, 1).intValue();

		//TODO Use TZ from RMR tab?
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, century*100+year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, tenMilli*10);

		return cal.getTime();
	}

	public static final byte POWER = 0x000;
	public static final byte PASSWORD = 0x001;
	public static final byte CHANGE_PROGRAMMABLE_SETTINGS = 0x002;
	public static final byte CHANGE_FIRMWARE= 0x003;
	public static final byte CHANGE_TIME = 0x004;
	public static final byte TEST_MODE = 0x005;
	public static final byte LOG_DOWNLOAD = 0x006;
	public static final byte FEATURE_RESET = 0x007;
	private void parseSystemLog(byte[] ba) throws IOException {

		int offset = 0;
		int length = 8;
		int recNum = 0;
		int recSize = 16;

		while (offset < ba.length) {
			Date recDate = parseF3(ba, offset);
			String event = recDate + "";
			offset+= length;
			byte code = ba[offset++];
			byte subcode1;
			byte subcode2;
			byte subcode3;
			switch (code) {
			case POWER:
				event += " => POWER";
				subcode1 = ba[offset++];
				switch (subcode1) {
				case 0x00:
					event += " : Power was lost";
					break;
				case 0x01:
					event += " : Normal operation was restored";
					break;
				default:
					event += " : Undefined";
					break;
				}
				break;

			case PASSWORD:
				event += " => PASSWORD";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x00:
					event += " : Password Protection was Enabled";
					break;
				case 0x01:
					event += " : Password Protection was Disabled";
					break;
				case 0x002:
					event += " : The Level 1 Password was changed";
					break;
				case 0x003:
					event += " : The Level 2 Password was changed";
					break;
				case 0x004:
					event += " : Level 1 access was granted";
					break;
				case 0x005:
					event += " : Level 2 access was granted";
					break;
				case 0x006:
					event += " : An invalid password was supplied";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			case CHANGE_PROGRAMMABLE_SETTINGS:
				event += " => CHANGE PROGRAMMABLE SETTINGS";
				break;
			case CHANGE_FIRMWARE:
				event += " => CHANGE FIRMWARE";
				subcode1 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : Comm Run Time";
					break;
				case 0x001:
					event += " : DSP Run Time";
					break;
				default:
					event += " : Undefined";
					break;
				}
				//String version = parseF2(ba, offset, 4);
				event += " : Old version - ";// + version;
				break;
			case CHANGE_TIME:
				event += " => CHANGE TIME";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : Old Time - The time stamp is the old time of the meter";
					break;
				case 0x001:
					event += " : New Time - The time stamp is the new time of the meter";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			case TEST_MODE:
				event += " => TEST MODE";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x001:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC";
					break;
				case 0x002:
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = TLC";
					break;
				case 0x003:
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = TLC";
					break;
				case 0x004:
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = TLC";
					break;
				case 0x005:
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = TLC";
					break;
				case 0x006:
					event += " : Action = Block Average Test  Test Mode = TLC";
					break;
				case 0x007:
					event += " : Action = Rolling Average Test  Test Mode = TLC";
					break;
				case 0x008:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC";
					break;
				case 0x009:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT";
					break;
				case 0x00A:
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = TLC & CTPT";
					break;
				case 0x00B:
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = TLC & CTPT";
					break;
				case 0x00C:
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = TLC & CTPT";
					break;
				case 0x00D:
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = TLC & CTPT";
					break;
				case 0x00E:
					event += " : Action = Block Average Test  Test Mode = TLC & CTPT";
					break;
				case 0x00F:
					event += " : Action = Rolling Average Test  Test Mode = TLC & CTPT";
					break;
				case 0x010:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = TLC & CTPT";
					break;
				case 0x011:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated";
					break;
				case 0x012:
					event += " : Action = VARh Test (Q1 & Q2)  Test Mode = Uncompensated";
					break;
				case 0x013:
					event += " : Action = VARh Test (Q3 & Q4)  Test Mode = Uncompensated";
					break;
				case 0x014:
					event += " : Action = VAh Test (Q1 & Q4)  Test Mode = Uncompensated";
					break;
				case 0x015:
					event += " : Action = VAh Test (Q2 & Q3)  Test Mode = Uncompensated";
					break;
				case 0x016:
					event += " : Action = Block Average Test  Test Mode = Uncompensated";
					break;
				case 0x017:
					event += " : Action = Rolling Average Test  Test Mode = Uncompensated";
					break;
				case 0x018:
					event += " : Action = Wh Test (Del & Rcv)  Test Mode = Uncompensated";
					break;
				case 0x019:
					event += " : Action = Wh Test (Del & Rcv)   Test Mode = CTPT";
					break;
				case 0x01A:
					event += " : Action = VARh Test (Q1 & Q2)   Test Mode = CTPT";
					break;
				case 0x01B:
					event += " : Action = VARh Test (Q3 & Q4)   Test Mode = CTPT";
					break;
				case 0x01C:
					event += " : Action = VAh Test (Q1 & Q4)   Test Mode = CTPT";
					break;
				case 0x01D:
					event += " : Action = VAh Test (Q2 & Q3)   Test Mode = CTPT";
					break;
				case 0x01E:
					event += " : Action = Block Average Test   Test Mode = CTPT";
					break;
				case 0x01F:
					event += " : Action = Rolling Average Test   Test Mode = CTPT";
					break;
				case 0x020:
					event += " : Action = Wh Test (Del & Rcv)   Test Mode = CTPT";
					break;
				default:
					event += " : Undefined";
					break;
				}

			case LOG_DOWNLOAD:
				event += " => LOG DOWNLOAD";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				subcode3 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : Download Started, Log records while downloading";
					break;
				case 0x001:
					event += " : Download Started, Log Paused while downloading";
					break;
				case 0x002:
					event += " : Download Ended";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Historical Log 1";
					break;
				case 0x001:
					event += " : Historical Log 2";
					break;
				case 0x002:
					event += " : Sequence of Events State Log";
					break;
				case 0x003:
					event += " : Sequence of Events Snapshot Log";
					break;
				case 0x004:
					event += " : Digital Input State Log";
					break;
				case 0x005:
					event += " : Digital Input Snapshot Log";
					break;
				case 0x006:
					event += " : Digital Output State Log";
					break;
				case 0x007:
					event += " : Digital Output Snapshot Log";
					break;
				case 0x008:
					event += " : Flicker Log";
					break;
				case 0x009:
					event += " : Waveform Trigger Log";
					break;
				case 0x00A:
					event += " : System Event Log";
					break;
				case 0x00B:
					event += " : Waveform Sample Log";
					break;
				case 0x00C:
					event += " : PQ Log";
					break;
				case 0x00D:
					event += " : Reset Log";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode3) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			case FEATURE_RESET:
				event += " => FEATURE RESET";
				subcode1 = ba[offset++];
				subcode2 = ba[offset++];
				switch (subcode1) {
				case 0x000:
					event += " : All Logs Reset";
					break;
				case 0x001:
					event += " : Maximum Reset";
					break;
				case 0x002:
					event += " : Minimum Reset";
					break;
				case 0x003:
					event += " : Energy Reset";
					break;
				case 0x004:
					event += " : Time of Use Current Month";
					break;
				case 0x005:
					event += " : Internal Input Accumulations and Aggregations";
					break;
				case 0x006:
					event += " : KYZ Output Accumulations";
					break;
				case 0x007:
					event += " : Cumulative Demand";
					break;
				case 0x008:
					event += " : Historical Log 1 Reset";
					break;
				case 0x009:
					event += " : Historical Log 2 Reset";
					break;
				case 0x00A:
					event += " : Sequence of Events Log Reset";
					break;
				case 0x00B:
					event += " : Digital Input Log Reset";
					break;
				case 0x00C:
					event += " : Digital Output Log Reset";
					break;
				case 0x00D:
					event += " : Flicker Log Reset";
					break;
				case 0x00E:
					event += " : Waveform Log Reset";
					break;
				case 0x00F:
					event += " : PQ Log Reset";
					break;
				case 0x010:
					event += " : System Event Log Reset";
					break;
				case 0x011:
					event += " : Total Average Power Factor Reset";
					break;
				case 0x012:
					event += " : Time of Use Active Registers";
					break;
				default:
					event += " : Undefined";
					break;
				}
				switch (subcode2) {
				case 0x000:
					event += " : Port 4";
					break;
				case 0x001:
					event += " : Port 3 (10/100 Base T Ethernet)";
					break;
				case 0x002:
					event += " : Port 2";
					break;
				case 0x003:
					event += " : Port 1";
					break;
				default:
					event += " : Port Undefined";
					break;
				}
				break;
			default:
				event += " => UNDEFINED";
				break;
			}

			System.out.println(event);
			recNum++;
			offset = recNum * recSize;
		}

	}

	private void testParse(byte[] byteArray) throws IOException {
		int offset = 0;
		int length = 4;
		long memsize = parseF18(byteArray, offset, length);
		offset += length;
		length = 2;
		int recSize = parseF51(byteArray, offset, length);
		offset += length;
		int firstIndex = parseF51(byteArray, offset, length);
		offset += length;
		int lastIndex = parseF51(byteArray, offset, length);
		offset += length;
		length = 8;
		Date firstTimeStamp = parseF3(byteArray, offset);
		offset += length;
		Date lastTimeStamp = parseF3(byteArray, offset);
		offset += length;
		length = 8;
		long validBitmap = parseF18(byteArray, offset, length);
		offset += length;
		length = 2;
		int maxRecords = parseF51(byteArray, offset, length);
		"".toCharArray();
	}

}
