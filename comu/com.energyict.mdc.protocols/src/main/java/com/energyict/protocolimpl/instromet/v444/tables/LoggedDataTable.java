package com.energyict.protocolimpl.instromet.v444.tables;

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
import com.energyict.protocolimpl.instromet.v444.CommandFactory;
import com.energyict.protocolimpl.instromet.v444.Instromet444;


public class LoggedDataTable extends AbstractTable {
	
	private List intervalDatas = new ArrayList();
	private int numberOfItemsLogged;
	private Date lastReading;
	private int defaultSize = 528;
	
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
	
	public void setNumberOfItemsLogged(int numberOfItemsLogged) throws IOException {
		this.numberOfItemsLogged = numberOfItemsLogged;
	}
	
	
	protected void parse(byte[] data) throws IOException {
		Calendar cal = null;
		//System.out.println("pare logged data");
		//System.out.println(ProtocolUtils.outputHexString(data));
		int length = data.length;
		//System.out.println("length = " + length);
		int offset = 0;
		int numberOfRecords = length/48; // Vn, Vu, V, Ve, pressure, Temp, status, time (total 48 bytes)
		//System.out.println("numberOfRecords = " + numberOfRecords);
		for (int i = 0; i < numberOfRecords; i++) {
			int status = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			BigDecimal temp = 
				new BigDecimal(Float.intBitsToFloat(ProtocolUtils.getIntLE(data, offset, 4)));
			offset = offset + 4;
			BigDecimal pressure = new BigDecimal(Float.intBitsToFloat(ProtocolUtils.getIntLE(data, offset, 4)));
			offset = offset + 4;
			
			int veRemainder = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			long veValue = ProtocolUtils.getLongLE(data, offset, 4);
			offset = offset + 4;
			BigDecimal ve = new BigDecimal(veValue).add(new BigDecimal(
				Float.intBitsToFloat(veRemainder)));
			
			int vuRemainder = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			long vuValue = ProtocolUtils.getLongLE(data, offset, 4);
			offset = offset + 4;
			BigDecimal vu = new BigDecimal(vuValue).add(new BigDecimal(
				Float.intBitsToFloat(vuRemainder)));
			
			int vRemainder = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			long vValue = ProtocolUtils.getLongLE(data, offset, 4);
			offset = offset + 4;
			BigDecimal v = new BigDecimal(vValue).add(new BigDecimal(
				Float.intBitsToFloat(vRemainder)));
			
			int vnRemainder = ProtocolUtils.getIntLE(data, offset, 4);
			offset = offset + 4;
			long vnValue = ProtocolUtils.getLongLE(data, offset, 4);
			offset = offset + 4;
			BigDecimal vn = new BigDecimal(vnValue).add(new BigDecimal(
				Float.intBitsToFloat(vnRemainder)));
			
			
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
					getTableFactory().getInstromet444().getTimeZone());
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
			if ((lastReading == null) || (date.after(lastReading))) {
				IntervalData intervalData = new IntervalData(date);
				
				intervalData.addValue(new BigDecimal(status));
				intervalData.addValue(temp);
				intervalData.addValue(pressure);
				intervalData.addValue(ve);
				intervalData.addValue(vu);
				intervalData.addValue(v);
				intervalData.addValue(vn);
				
				/*System.out.println("status: " + status);
				System.out.println("temp: " + temp);
				System.out.println("pressure: " + pressure);
				System.out.println("ve: " + ve);
				System.out.println("vu: " + vu);
				System.out.println("v: " + v);
				System.out.println("vn: " + vn);*/
				
				intervalDatas.add(intervalData);
				//System.out.println(cal.getTime());
				System.out.println("");
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
		int bytesPerRecord = 8 + 8 + 8 + 8 + 4 + 4 + 4 + 4; // Vn, Vu, V, Ve, pressure, Temp, status, time
		return ((((tableLength - 6) / bytesPerRecord)) * bytesPerRecord) + 6;
		
	}
	
	static public void main(String[] argv) throws Exception {

		Dialer dialer =DialerFactory.getDefault().newDialer();
        dialer.init("COM1", "AT+CBST=71");
        dialer.getSerialCommunicationChannel().setParams(9600,
                                                       SerialCommunicationChannel.DATABITS_8,
                                                       SerialCommunicationChannel.PARITY_NONE,
                                                      SerialCommunicationChannel.STOPBITS_1);
        dialer.connect("0031651978414",60000);
        Instromet444 instromet = new Instromet444();
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
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        //instromet.getTableFactory().getLoggedDataTable(cal.getTime());
        System.out.println(instromet.getTime());
        System.out.println("peak: " + 
        		instromet.getTableFactory().getPeakHourPeakDayTable().getPeak());
        System.out.println("peak time: " + 
        		instromet.getTableFactory().getPeakHourPeakDayTable().getPeakTime());
        /*System.out.println("uncorrected: " + 
        		instromet.getTableFactory().getCountersTable().getUnCorrectedVolume());
    
        System.out.println("corrected: " + 
        		instromet.getTableFactory().getCountersTable().getCorrectedVolume());*/
    }

        

}
