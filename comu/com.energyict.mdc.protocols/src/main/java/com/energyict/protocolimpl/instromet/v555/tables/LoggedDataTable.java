package com.energyict.protocolimpl.instromet.v555.tables;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v555.CommandFactory;
import com.energyict.protocolimpl.instromet.v555.Instromet555;

public class LoggedDataTable extends AbstractTable {
	
	private List intervalDatas = new ArrayList();
	private int numberOfItemsLogged;
	private Date lastReading;
	private int defaultSize; // number of bytes (data) to read with one command
	private boolean[] isFloatItem;
	
	public LoggedDataTable(TableFactory tableFactory) {
		super(tableFactory);
	}
	public LoggedDataTable(TableFactory tableFactory, Date lastReading) {
		this(tableFactory);
		this.lastReading = lastReading;
	}
	
	public int getTableType() {
		return 11;
	}
	
	public List getIntervalDatas() {
		return intervalDatas;
	}
	
	protected void init() throws IOException {
		isFloatItem = new boolean[numberOfItemsLogged];
		LoggingConfigurationTable config = 
			getTableFactory().getLoggingConfigurationTable();
		for (int i = 0; i < numberOfItemsLogged; i++) {
			isFloatItem[i] = config.isFloatingPoint((numberOfItemsLogged - 1) - i);
		}
	}
	
	public void setNumberOfItemsLogged(int numberOfItemsLogged) throws IOException {
		this.numberOfItemsLogged = numberOfItemsLogged;
		setDefaultSize();
	}
	
	protected void setDefaultSize() throws IOException {
		if (numberOfItemsLogged == 1)
			defaultSize = 1016;
		else if (numberOfItemsLogged == 2)
			defaultSize = 1020;
		else if (numberOfItemsLogged == 3)
			defaultSize = 1008;
		else if (numberOfItemsLogged == 4)
			defaultSize = 1020;
		else if (numberOfItemsLogged == 5)
			defaultSize = 1008;
		else
			throw new IOException("max number of items to be logged is 5");
	}
	
	protected void parse(byte[] data) throws IOException {
		init();
		LoggingConfigurationTable config = 
			getTableFactory().getLoggingConfigurationTable();
		
		Calendar cal = null;
		System.out.println("parse logged data");
		System.out.println(ProtocolUtils.outputHexString(data));
		int length = data.length;
		System.out.println("length = " + length);
		int offset = 0;
		int numberOfRecords = length/(4 + (numberOfItemsLogged * 4));
		System.out.println("numberOfRecords = " + numberOfRecords);
		for (int i = 0; i < numberOfRecords; i++) {
			List values = new ArrayList();
			for (int j = 0; j < this.numberOfItemsLogged; j++) {
				boolean isFloat = isFloatItem[j];
				//System.out.println(j + " isFloat = " + isFloat);
				if (!isFloat) {
					long value = ProtocolUtils.getLongLE(data, offset, 4);
					//System.out.println(j + " value = " + value);
					values.add(new BigDecimal(value));
				}
				else {
					long value = ProtocolUtils.getLongLE(data, offset, 4);
					//System.out.println(j + " value = " + Float.intBitsToFloat((int) value));
					values.add(new BigDecimal(Float.intBitsToFloat((int) value)));
				}
				offset = offset + 4;
			}
			byte b4 = data[offset];
			byte b3 = data[offset + 1];
			byte b2 = data[offset + 2];
			byte b1 = data[offset + 3];
			offset = offset + 4;
			int year = 2000 + (ProtocolUtils.byte2int(b2) >> 4);
			int month = ProtocolUtils.byte2int(b2) & (byte)0x0F;
			int day = ProtocolUtils.byte2int(b3) >> 3;
			int hour = ((ProtocolUtils.byte2int(b3) & (byte)0x07) << 2)
						+ (ProtocolUtils.byte2int(b4) >> 6);
			
			if ((cal == null) || 
			    ((cal != null) && (!cal.getTimeZone().useDaylightTime()))) {
				cal = Calendar.getInstance(
					getTableFactory().getInstromet555().getTimeZone());
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, (month - 1));
				cal.set(Calendar.DATE, day);
				cal.set(Calendar.HOUR_OF_DAY, hour);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}
			else {
				//System.out.println("add");
				cal.add(Calendar.HOUR_OF_DAY, -1);
			}
			Date date = cal.getTime();
			System.out.println("date = " + date);
			if ((lastReading == null) || (date.after(lastReading))) {
				IntervalData intervalData = new IntervalData(date);
				for (int j = 0; j < numberOfItemsLogged; j++) 
					intervalData.addValue((BigDecimal) values.get(j));
				intervalDatas.add(intervalData);
				//System.out.println(cal.getTime());
				//System.out.println("");
			}
			else 
				throw new LastReadingReachedException();
		}
	}
	
	protected void prepareBuild() throws IOException {
		LoggingConfigurationTable config = 
			getTableFactory().getLoggingConfigurationTable();
		System.out.println(config);
		setNumberOfItemsLogged(config.getChannelInfos().size());
		CommandFactory commandFactory = 
			getTableFactory().getCommandFactory();
		Response response = 
			commandFactory.switchToLoggedDataCommand().invoke();
		parseStatus(response);
    	readHeaders();
	}
	
	protected void doBuild() throws IOException {
		try {
			int startAddress = 6;
			int endAddress = getEndAddress();
			int size = defaultSize;
			while (startAddress < endAddress) {
				if ((startAddress + size) >= endAddress) {
					size = endAddress - startAddress;
				}
				executeGetDataCommand(startAddress, size);
				startAddress = startAddress + size;
			}
		}
		catch (LastReadingReachedException e) {
			System.out.println("Last reading reached");
		}
	}
	
	protected void executeGetDataCommand(int startAddress, int size) throws IOException {
		System.out.println("startAddress = " + startAddress + ", " + size);
		CommandFactory commandFactory = 
			getTableFactory().getCommandFactory();
		Response response = 
			commandFactory.readLoggedDataCommand(startAddress, size).invoke();
		parseStatus(response);
		parseWrite(response);
	}
	
	protected int getEndAddress() {
		System.out.println("logged data table length: " + getTableLength());
		int tableLength = getTableLength();
		int bytesPerRecord = (numberOfItemsLogged * 4) + 4; // 4 extra bytes for time
		return ((((tableLength - 6) / bytesPerRecord)) * bytesPerRecord) + 6;
		
	}

	static public void main(String[] argv) throws Exception {
		System.out.println(
				Float.intBitsToFloat(
						1091344844));

		//System.out.println(ProtocolUtils.buildStringHex(150, 4));
		
		//55 51 00 06 02 04

		
		/*byte byteValue = (byte)0x06;
		int intValue = (int)byteValue;
		char charValue = (char) intValue;
		System.out.println(intValue);
		System.out.println(charValue);*/
		
		/*Dialer dialer =DialerFactory.getDirectDialer().newDialer();
        dialer.init("COM1");
        dialer.connect("",60000); 
        Instromet555 instromet = new Instromet555();
        dialer.getSerialCommunicationChannel().setParamsAndFlush(2400,
                SerialCommunicationChannel.DATABITS_8,
                SerialCommunicationChannel.PARITY_NONE,
                SerialCommunicationChannel.STOPBITS_1);
        instromet.init(
        		dialer.getInputStream(),
        		dialer.getOutputStream(),
        		TimeZone.getDefault(),
        		Logger.getLogger("name"));
        instromet.connect();
        //instromet.setTime();
        CorrectorInformationTable table = 
        	instromet.getTableFactory().getCorrectorInformationTable();*/
		
		/*Dialer dialer =DialerFactory.getDefault().newDialer();
        dialer.init("COM1", "AT+CBST=71");
        dialer.getSerialCommunicationChannel().setParams(9600,
                                                       SerialCommunicationChannel.DATABITS_8,
                                                       SerialCommunicationChannel.PARITY_NONE,
                                                      SerialCommunicationChannel.STOPBITS_1);
        dialer.connect("0031651978414",60000);
        Instromet555 instromet = new Instromet555();
        instromet.init(
        		dialer.getInputStream(),
        		dialer.getOutputStream(),
        		TimeZone.getDefault(),
        		Logger.getLogger("name"));
        instromet.connect();
        LoggingConfigurationTable table = 
        	instromet.getTableFactory().getLoggingConfigurationTable();
        System.out.println(table.getChannelInfos().size());
        System.out.println(table);
    
		
		
		/*Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR) - 2000;
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		int weekDay;
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY)
			weekDay = 1;
		else if (dayOfWeek == Calendar.MONDAY)
			weekDay = 2;
		else if (dayOfWeek == Calendar.TUESDAY)
			weekDay = 3;
		else if (dayOfWeek == Calendar.WEDNESDAY)
			weekDay = 4;
		else if (dayOfWeek == Calendar.THURSDAY)
			weekDay = 5;
		else if (dayOfWeek == Calendar.FRIDAY)
			weekDay = 6;
		else // saterday
			weekDay = 7;
		byte[] data = new byte[7];
		data[6] = ProtocolUtils.hex2BCD(year);
		data[5] = ProtocolUtils.hex2BCD(month);
		data[4] = ProtocolUtils.hex2BCD(day);
		data[3] = ProtocolUtils.hex2BCD(weekDay);
		data[2] = ProtocolUtils.hex2BCD(hour);
		data[1] = ProtocolUtils.hex2BCD(min);
		data[0] = ProtocolUtils.hex2BCD(sec);
		System.out.println(ProtocolUtils.outputHexString(data));*/
        
        
        /*byte[] data = {(byte)0x3A , (byte)0x00 , (byte)0x00 , (byte)0x57 , 
        		       (byte)0x00 , (byte)0x00 , (byte)0x00 , (byte)0x00 , 
        		       (byte)0x00 , (byte)0x01 , (byte)0x0B};
        
        int crc = CRCGenerator.calcCCITTCRCReverse(data);
        
        System.out.println(Integer.toHexString(crc));*/
        
        //dialer.getOutputStream().write(data, 0, 11);

		
		/*byte[] data= {(byte)0x3A, (byte)0x00, (byte)0x00, (byte)0x57, (byte)0x00   
		,(byte)0x00 ,(byte)0x00 ,(byte)0x06 ,(byte)0x03 ,(byte)0xFC ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0xC0 ,(byte)0xC1 ,(byte)0x7A   
		,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x80 ,(byte)0xC1 ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E   
		,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x40 ,(byte)0xC1 ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20   
		,(byte)0x00 ,(byte)0x00 ,(byte)0xC1 ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0xC0 ,(byte)0xC0 ,(byte)0x7A   
		,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x80 ,(byte)0xC0 ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E   
		,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x40 ,(byte)0xC0 ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20   
		,(byte)0x00 ,(byte)0x00 ,(byte)0xC0 ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0xC0 ,(byte)0xBD ,(byte)0x7A   
		,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x80 ,(byte)0xBD ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E   
		,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x40 ,(byte)0xBD ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20   
		,(byte)0x00 ,(byte)0x00 ,(byte)0xBD ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0xC0 ,(byte)0xBC ,(byte)0x7A   
		,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x80 ,(byte)0xBC ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E   
		,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20 ,(byte)0x00 ,(byte)0x40 ,(byte)0xBC ,(byte)0x7A ,(byte)0x00 ,(byte)0x30 ,(byte)0x9B ,(byte)0x1E ,(byte)0x00 ,(byte)0x43 ,(byte)0x23 ,(byte)0x20   
		,(byte)0x00 ,(byte)0x00 ,(byte)0xBC ,(byte)0x7A ,(byte)0x00};

		int offset = 4; //3A 00 00 57
		int start = ProtocolUtils.getInt(data, offset, 4);
		offset = offset + 4;
		int length = ProtocolUtils.getInt(data, offset, 2);
		offset = offset + 2;
		System.out.println(start);
		System.out.println(length);
		for (int i = 0; i < 10; i++) {
			int v = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			int vn = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			System.out.println("v  = " + v);
			System.out.println("vn = " + vn);
			byte b4 = data[offset];
			byte b3 = data[offset + 1];
			byte b2 = data[offset + 2];
			byte b1 = data[offset + 3];
			offset = offset + 4;
			int year = 2000 + (ProtocolUtils.byte2int(b2) >> 4);
			int month = ProtocolUtils.byte2int(b2) & (byte)0x0F;
			int day = ProtocolUtils.byte2int(b3) >> 3;
			int hour = ((ProtocolUtils.byte2int(b3) & (byte)0x07) << 2)
						+ (ProtocolUtils.byte2int(b4) >> 6);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DATE, day);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			//System.out.println("year = " + year);
			//System.out.println("month = " + month);
			//System.out.println("day = " + day);
			//System.out.println("hour = " + hour);
			System.out.println(cal.getTime());
			System.out.println("");
		}*/
		
		
		
    }

        

}
