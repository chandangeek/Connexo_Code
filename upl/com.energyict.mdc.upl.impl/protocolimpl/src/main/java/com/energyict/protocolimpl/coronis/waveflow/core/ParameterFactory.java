package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.util.*;

public class ParameterFactory {
	
	private WaveFlow waveFlow;

	// cached
	private SamplingPeriod samplingPeriod=null;
	private SamplingActivationType samplingActivationType=null;
	private ApplicationStatus applicationStatus=null;
	private OperatingMode operatingMode=null;
	
	ParameterFactory(final WaveFlow waveFlow) {
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

	final public void disableDataLogging() throws IOException {
		operatingMode = new OperatingMode(waveFlow);
		operatingMode.disableDataLogging();
		operatingMode.write();
	}
	
	final public void enableDataLoggingPeriodic() throws IOException {
		operatingMode = new OperatingMode(waveFlow);
		operatingMode.enableDataLoggingPeriodic();
		operatingMode.write();
	}

	final public void manageDataloggingInputs(int nrOfInputs2Enable) throws IOException {
		operatingMode = new OperatingMode(waveFlow);
		operatingMode.manageInputs(nrOfInputs2Enable);
		operatingMode.write();
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
	
	final public int readSamplingPeriod() throws IOException {
		if (samplingPeriod == null) {
			samplingPeriod = new SamplingPeriod(waveFlow);
			samplingPeriod.read();
		}
		return samplingPeriod.getSamplingPeriodInSeconds();
	}

	final public void writeSamplingPeriod(final int samplingPeriodInSeconds) throws IOException {
		samplingPeriod = new SamplingPeriod(waveFlow);
		samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
		samplingPeriod.write();
	}
	
	final public void writeSamplingActivationNextHour() throws IOException {
		Calendar cal = Calendar.getInstance(waveFlow.getTimeZone());
		writeSamplingActivationType(cal.get(Calendar.HOUR_OF_DAY)==23?0:cal.get(Calendar.HOUR_OF_DAY)+1);
	}
	
	final public void writeSamplingActivationType(final int startHour) throws IOException {
		samplingActivationType = new SamplingActivationType(waveFlow);
		samplingActivationType.setStartHour(startHour);
		samplingActivationType.write();
	}
	
	/**
	 * This is the combination of SamplingPeriod and MeasurementPeriod
	 * @return the interval in seconds
	 */
	final public int getProfileIntervalInSeconds() throws IOException {
		return readSamplingPeriod();
	}
	
	
//	final public int readNrOfLoggedRecords() throws IOException {
//		NrOfLoggedRecords o = new NrOfLoggedRecords(waveFlow);
//		o.read();
//		return o.getNrOfRecords();
//	}

	final public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
		BatteryLifeDurationCounter o = new BatteryLifeDurationCounter(waveFlow);
		o.read();
		return o;
	}
	
	final public Date readBatteryLifeDateEnd() throws IOException {
		BatteryLifeDateEnd o = new BatteryLifeDateEnd(waveFlow);
		o.read();
		return (o.getCalendar()==null?null:o.getCalendar().getTime());
	}

//	final public Date readBackflowDetectionDate(int portId) throws IOException {
//		BackflowDetectionDate backflowDetectionDate = new BackflowDetectionDate(waveFlow, portId);
//		backflowDetectionDate.read();
//		return backflowDetectionDate.getDate();
//	}
//	
//	final public int readBackflowDetectionFlags(int portId) throws IOException {
//		BackflowDetectionFlags backflowDetectionflags = new BackflowDetectionFlags(waveFlow, portId);
//		backflowDetectionflags.read();
//		return backflowDetectionflags.getFlags();
//	}
//	
//	final public Date readCommunicationErrorDetectionDate(int portId) throws IOException {
//		CommunicationErrorDetectionDate communicationErrorDetectionDate = new CommunicationErrorDetectionDate(waveFlow, portId);
//		communicationErrorDetectionDate.read();
//		return communicationErrorDetectionDate.getDate();
//	}
//	
//	final public Date readCommunicationErrorReadingDate(int portId) throws IOException {
//		CommunicationErrorReadingDate communicationErrorReadingDate = new CommunicationErrorReadingDate(waveFlow, portId);
//		communicationErrorReadingDate.read();
//		return communicationErrorReadingDate.getDate();
//	}
	
}
