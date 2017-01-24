package com.energyict.protocolimpl.coronis.wavetalk.core;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ParameterFactoryImpl implements ParameterFactory {
	
	private AbstractWaveTalk waveFlow;

	// cached
	private ApplicationStatus applicationStatus=null;
	private OperatingMode operatingMode=null;
	
	public ParameterFactoryImpl(final AbstractWaveTalk waveFlow) {
		this.waveFlow = waveFlow;
	}
	
	
	final public int readApplicationStatus() throws IOException {
		if (applicationStatus == null) {
			applicationStatus = new ApplicationStatus(waveFlow);
			applicationStatus.read();
		}
		return applicationStatus.getStatus();
	}

	final public void writeApplicationStatus(final int status) throws IOException {
		applicationStatus = new ApplicationStatus(waveFlow);
		applicationStatus.setStatus(status);
		applicationStatus.write();
	}
	
	final public int readOperatingMode() throws IOException {
		if (operatingMode == null) {
			operatingMode = new OperatingMode(waveFlow);
			operatingMode.read();
		}
		return operatingMode.getOperatingMode();
	}

	
	
	final public void writeOperatingMode(final int operatingModeVal, final int mask) throws IOException {
		operatingMode = new OperatingMode(waveFlow);
		operatingMode.setOperatingMode(operatingModeVal);
		operatingMode.setMask(mask);
		operatingMode.write();
	}
	
	final public void writeOperatingMode(final int operatingModeVal) throws IOException {
		operatingMode = new OperatingMode(waveFlow);
		operatingMode.setOperatingMode(operatingModeVal);
		operatingMode.write();
	}
	
	final public Date readTimeDateRTC() throws IOException {
		TimeDateRTC o = new TimeDateRTC(waveFlow);
		o.invoke();
		return o.getCalendar().getTime();
	}
	
	final public void writeTimeDateRTC(final Date date) throws IOException {
		TimeDateRTC o = new TimeDateRTC(waveFlow);
		Calendar calendar = Calendar.getInstance(waveFlow.getTimeZone());
		calendar.setTime(date);
		o.setCalendar(calendar);
		o.set();
	}
	

	public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
		BatteryLifeDurationCounter o = new BatteryLifeDurationCounter(waveFlow);
		o.read();
		return o;
	}
	
	
	final public Date readBatteryLifeDateEnd() throws IOException {
		BatteryLifeDateEnd o = new BatteryLifeDateEnd(waveFlow);
		o.read();
		return (o.getCalendar()==null?null:o.getCalendar().getTime());
	}


}
