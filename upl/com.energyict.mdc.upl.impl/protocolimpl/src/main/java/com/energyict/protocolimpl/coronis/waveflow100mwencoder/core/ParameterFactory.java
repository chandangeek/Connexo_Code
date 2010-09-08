package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.util.*;

class ParameterFactory {
	
	WaveFlow100mW waveFlow100mW;

	// cached
	MeasurementPeriod measurementPeriod=null;
	SamplingPeriod samplingPeriod=null;
	ApplicationStatus applicationStatus=null;
	OperatingMode operatingMode=null;
	
	
	ParameterFactory(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}
	
	
	final public int readApplicationStatus() throws IOException {
		if (applicationStatus == null) {
			applicationStatus = new ApplicationStatus(waveFlow100mW);
			applicationStatus.read();
		}
		return applicationStatus.getStatus();
	}

	final public void writeApplicationStatus(int status) throws IOException {
		applicationStatus = new ApplicationStatus(waveFlow100mW);
		applicationStatus.setStatus(status);
		applicationStatus.write();
	}
	
	final public int readOperatingMode() throws IOException {
		if (operatingMode == null) {
			operatingMode = new OperatingMode(waveFlow100mW);
			operatingMode.read();
		}
		return operatingMode.getOperatingMode();
	}
	
	final public void writeOperatingMode(int operatingModeVal) throws IOException {
		operatingMode = new OperatingMode(waveFlow100mW);
		operatingMode.setOperatingMode(operatingModeVal);
		operatingMode.write();
	}
	
	final public Date readTimeDateRTC() throws IOException {
		TimeDateRTC o = new TimeDateRTC(waveFlow100mW);
		o.read();
		return o.getCalendar().getTime();
	}
	
	final public void writeTimeDateRTC(Date date) throws IOException {
		TimeDateRTC o = new TimeDateRTC(waveFlow100mW);
		Calendar calendar = Calendar.getInstance(waveFlow100mW.getTimeZone());
		calendar.setTime(date);
		o.setCalendar(calendar);
		o.write();
	}
	
	final public int readSamplingPeriod() throws IOException {
		if (samplingPeriod == null) {
			samplingPeriod = new SamplingPeriod(waveFlow100mW);
			samplingPeriod.read();
		}
		return samplingPeriod.getSamplingPeriodInSeconds();
	}

	final public void writeSamplingPeriod(int samplingPeriodInSeconds) throws IOException {
		samplingPeriod = new SamplingPeriod(waveFlow100mW);
		samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
		samplingPeriod.write();
	}

	final public int readMeasurementPeriod() throws IOException {
		if (measurementPeriod == null) {
			measurementPeriod = new MeasurementPeriod(waveFlow100mW);
			measurementPeriod.read();
		}
		return measurementPeriod.getMeasurementPeriod();
	}

	final public void writeMeasurementPeriod(int measurementPeriodVal) throws IOException {
		measurementPeriod = new MeasurementPeriod(waveFlow100mW);
		measurementPeriod.setMeasurementPeriod(measurementPeriodVal);
		measurementPeriod.write();
	}
	
	/**
	 * This is the combination of SamplingPeriod and MeasurementPeriod
	 * @return the interval in seconds
	 */
	final int getProfileIntervalInSeconds() throws IOException {
		return readSamplingPeriod() * readMeasurementPeriod();
	}
	
	
	final public int readNrOfLoggedRecords() throws IOException {
		NrOfLoggedRecords o = new NrOfLoggedRecords(waveFlow100mW);
		o.read();
		return o.getNrOfRecords();
	}
	
}
