package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.util.*;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;

class ParameterFactory {
	
	private WaveFlow100mW waveFlow100mW;

	// cached
	private MeasurementPeriod measurementPeriod=null;
	private SamplingPeriod samplingPeriod=null;
	private SamplingActivationType samplingActivationType=null;
	private ApplicationStatus applicationStatus=null;
	private OperatingMode operatingMode=null;
	private EncoderModel[] encoderModels = new EncoderModel[2];
	private EncoderUnit[] encoderUnits = new EncoderUnit[2];
	
	ParameterFactory(final WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}
	
	
	final int readApplicationStatus() throws IOException {
		if (applicationStatus == null) {
			applicationStatus = new ApplicationStatus(waveFlow100mW);
			applicationStatus.read();
		}
		return applicationStatus.getStatus();
	}

	final void writeApplicationStatus(final int status) throws IOException {
		applicationStatus = new ApplicationStatus(waveFlow100mW);
		applicationStatus.setStatus(status);
		applicationStatus.write();
	}
	
	final int readOperatingMode() throws IOException {
		if (operatingMode == null) {
			operatingMode = new OperatingMode(waveFlow100mW);
			operatingMode.read();
		}
		return operatingMode.getOperatingMode();
	}

	final void writeOperatingMode(final int operatingModeVal, final int mask) throws IOException {
		operatingMode = new OperatingMode(waveFlow100mW);
		operatingMode.setOperatingMode(operatingModeVal);
		operatingMode.setMask(mask);
		operatingMode.write();
	}
	
	final void writeOperatingMode(final int operatingModeVal) throws IOException {
		operatingMode = new OperatingMode(waveFlow100mW);
		operatingMode.setOperatingMode(operatingModeVal);
		operatingMode.write();
	}
	
	final Date readTimeDateRTC() throws IOException {
		TimeDateRTC o = new TimeDateRTC(waveFlow100mW);
		o.read();
		return o.getCalendar().getTime();
	}
	
	final void writeTimeDateRTC(final Date date) throws IOException {
		TimeDateRTC o = new TimeDateRTC(waveFlow100mW);
		Calendar calendar = Calendar.getInstance(waveFlow100mW.getTimeZone());
		calendar.setTime(date);
		o.setCalendar(calendar);
		o.write();
	}
	
	final int readSamplingPeriod() throws IOException {
		if (samplingPeriod == null) {
			samplingPeriod = new SamplingPeriod(waveFlow100mW);
			samplingPeriod.read();
		}
		return samplingPeriod.getSamplingPeriodInSeconds();
	}

	final void writeSamplingPeriod(final int samplingPeriodInSeconds) throws IOException {
		samplingPeriod = new SamplingPeriod(waveFlow100mW);
		samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
		samplingPeriod.write();
	}

	final int readSamplingActivationType() throws IOException {
		if (samplingActivationType == null) {
			samplingActivationType = new SamplingActivationType(waveFlow100mW);
			samplingActivationType.read();
		}
		return samplingActivationType.getType();
	}
	
	final void writeSamplingActivationImmediate() throws IOException {
		writeSamplingActivationType(SamplingActivationType.START_IMMEDIATE);
	}
	
	final void writeSamplingActivationNextHour() throws IOException {
		writeSamplingActivationType(SamplingActivationType.START_NEXT_HOUR);
	}
	
	final void writeSamplingActivationType(final int type) throws IOException {
		samplingActivationType = new SamplingActivationType(waveFlow100mW);
		samplingActivationType.setType(type);
		samplingActivationType.write();
	}
	
	
	final int readMeasurementPeriod() throws IOException {
		if (measurementPeriod == null) {
			measurementPeriod = new MeasurementPeriod(waveFlow100mW);
			measurementPeriod.read();
		}
		return measurementPeriod.getMeasurementPeriod();
	}

	final void writeMeasurementPeriod(final int measurementPeriodVal) throws IOException {
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
	
	
	final int readNrOfLoggedRecords() throws IOException {
		NrOfLoggedRecords o = new NrOfLoggedRecords(waveFlow100mW);
		o.read();
		return o.getNrOfRecords();
	}
	
	/**
	 * Read the encodermodel for the port A (portId<=0) or B (portId>=1)
	 * @param portId
	 * @return the encoder model
	 * @throws IOException 
	 */
	final EncoderModel readEncoderModel(int portId) throws IOException {

		// validate the portId
		if (portId < 0) portId=0;
		if (portId > 1) portId=1;
		
		if (encoderModels[portId] == null ) {
			encoderModels[portId] = new EncoderModel(waveFlow100mW,portId);
			encoderModels[portId].read();
		}
		return encoderModels[portId];
	}
	
	/**
	 * Read the encoderunit for the port A (portId<=0) or B (portId>=1)
	 * @param portId
	 * @return the encoder unit
	 * @throws IOException 
	 */
	final EncoderUnit readEncoderUnit(int portId) throws IOException {
		// validate the portId
		if (portId < 0) portId=0;
		if (portId > 1) portId=1;
		
		if (encoderUnits[portId] == null ) {
			encoderUnits[portId] = new EncoderUnit(waveFlow100mW,portId);
			encoderUnits[portId].read();
		}
		return encoderUnits[portId];
	}
	
	final void writeEncoderUnit(int portId, EncoderUnitType encoderUnitType, int nrOfDigitsBeforeDecimalPoint) throws IOException {
		encoderUnits[portId] = new EncoderUnit(waveFlow100mW,portId);
		encoderUnits[portId].setEncoderUnitInfo(new EncoderUnitInfo(encoderUnitType,nrOfDigitsBeforeDecimalPoint));
		encoderUnits[portId].write();
	}
	

	final BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
		BatteryLifeDurationCounter o = new BatteryLifeDurationCounter(waveFlow100mW);
		o.read();
		return o;
	}
	
	final Date readBatteryLifeDateEnd() throws IOException {
		BatteryLifeDateEnd o = new BatteryLifeDateEnd(waveFlow100mW);
		o.read();
		return (o.getCalendar()==null?null:o.getCalendar().getTime());
	}

	final Date readBackflowDetectionDate(int portId) throws IOException {
		BackflowDetectionDate backflowDetectionDate = new BackflowDetectionDate(waveFlow100mW, portId);
		backflowDetectionDate.read();
		return backflowDetectionDate.getDate();
	}
	
	final int readBackflowDetectionFlags(int portId) throws IOException {
		BackflowDetectionFlags backflowDetectionflags = new BackflowDetectionFlags(waveFlow100mW, portId);
		backflowDetectionflags.read();
		return backflowDetectionflags.getFlags();
	}
	
	final Date readCommunicationErrorDetectionDate(int portId) throws IOException {
		CommunicationErrorDetectionDate communicationErrorDetectionDate = new CommunicationErrorDetectionDate(waveFlow100mW, portId);
		communicationErrorDetectionDate.read();
		return communicationErrorDetectionDate.getDate();
	}
	
	final Date readCommunicationErrorReadingDate(int portId) throws IOException {
		CommunicationErrorReadingDate communicationErrorReadingDate = new CommunicationErrorReadingDate(waveFlow100mW, portId);
		communicationErrorReadingDate.read();
		return communicationErrorReadingDate.getDate();
	}
	
}
