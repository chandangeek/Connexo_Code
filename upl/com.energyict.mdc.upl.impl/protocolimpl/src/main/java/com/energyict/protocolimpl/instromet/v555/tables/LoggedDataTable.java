package com.energyict.protocolimpl.instromet.v555.tables;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v555.CommandFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
			int year = convertYear(b2);
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

	/**
	 * Convert the 4 bits year information to a regular year
	 * by padding with 2000/2016 <br/>
	 * <b>Warning: <br/>padding is either with 2000 or with 2016, which should cover years in range 2014-2029
	 * For other years, the padding will produce erroneous year!
	 *
	 * @param b2 the byte containing the year nibble
	 * @return the converted year
     */
	protected int convertYear(byte b2) {
		int yearLowNibble = ProtocolUtils.byte2int(b2) >> 4;
		if (yearLowNibble >= 14) {	// Consider 14 and 15 as 2014 and 2015
			return 2000 + yearLowNibble;
		} else {                    // Else consider as 2016, 2017, ...
			return 2016 + yearLowNibble;
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

}
