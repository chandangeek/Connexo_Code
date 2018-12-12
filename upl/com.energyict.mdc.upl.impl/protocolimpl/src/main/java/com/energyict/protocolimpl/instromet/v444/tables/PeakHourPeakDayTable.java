package com.energyict.protocolimpl.instromet.v444.tables;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v444.CommandFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class PeakHourPeakDayTable extends AbstractTable {

	private BigDecimal peak;
	private Date peakTime;

	public PeakHourPeakDayTable(TableFactory tableFactory) {
		super(tableFactory);
	}

	protected void parse(byte[] data) throws ProtocolException {
		//System.out.println("parse peak");
		//System.out.println(ProtocolUtils.outputHexString(data));
		int peakValue = ProtocolUtils.getIntLE(data, 0, 4);
		int peakRemainder = ProtocolUtils.getIntLE(data, 4, 4);
		peak = new BigDecimal(peakValue).add(new BigDecimal(
			Float.intBitsToFloat(peakRemainder)));

		int year = ProtocolUtils.getInt(data, 14, 1);
		int month = ProtocolUtils.getInt(data, 13, 1);
		int day = ProtocolUtils.getInt(data, 12, 1);
		int weekday = ProtocolUtils.getInt(data, 11, 1);
		int hour = ProtocolUtils.getInt(data, 10, 1);
		int min = ProtocolUtils.getInt(data, 9, 1);
		int sec = ProtocolUtils.getInt(data, 8, 1);

		Calendar cal = Calendar.getInstance(
				getTableFactory().getInstromet444().getTimeZone());
		cal.set(Calendar.YEAR, (2000 + year));
		cal.set(Calendar.MONTH, (month - 1));
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		cal.set(Calendar.MILLISECOND, 0);
		peakTime = cal.getTime();
		//System.out.println("peakTime = " + peakTime);
	}

	public BigDecimal getPeak() {
		return peak;
	}

	public Date getPeakTime() {
		return peakTime;
	}

	protected void prepareBuild() throws IOException {
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.switchToPeakTable().invoke();
		if (response == null)
			throw new ProtocolException("Peak table switch: No answer from corrector");
		parseStatus(response);
    	readHeaders();
	}

	protected void doBuild() throws IOException {
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.readPeakCommand().invoke();
		parseStatus(response);
	    parseWrite(response);
	}

	public int getTableType() {
		return 24;
	}

	protected int getTableTypeReturned() {
    	return 20;
    }

}

