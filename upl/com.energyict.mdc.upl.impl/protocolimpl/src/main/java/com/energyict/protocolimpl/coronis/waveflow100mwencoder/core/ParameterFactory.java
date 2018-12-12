package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW.MeterProtocolType;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ParameterFactory {

    private WaveFlow100mW waveFlow100mW;

    // cached
    private MeasurementPeriod measurementPeriod = null;
    private SamplingPeriod samplingPeriod = null;
    private DayOfWeek dayOfWeek = null;
    private HourOfMeasurement hourOfMeasurement = null;
    private SamplingActivationType samplingActivationType = null;
    private ApplicationStatus applicationStatus = null;
    private OperatingMode operatingMode = null;
    private EncoderModel[] encoderModels = new EncoderModel[2];
    private EncoderUnit[] encoderUnits = new EncoderUnit[2];

    ParameterFactory(final WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW = waveFlow100mW;
    }

    public final int readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            if (waveFlow100mW.getMeterProtocolType() == MeterProtocolType.SM150E) {
                applicationStatus = new ApplicationStatusSevernTrent(waveFlow100mW);
            } else if (waveFlow100mW.getMeterProtocolType() == MeterProtocolType.ECHODIS) {
                applicationStatus = new ApplicationStatusMbus(waveFlow100mW);
            }
            applicationStatus.read();
        }
        return applicationStatus.getStatus();
    }

    public final void writeApplicationStatus(final int status) throws IOException {
        if (waveFlow100mW.getMeterProtocolType() == MeterProtocolType.SM150E) {
            applicationStatus = new ApplicationStatusSevernTrent(waveFlow100mW);
        } else if (waveFlow100mW.getMeterProtocolType() == MeterProtocolType.ECHODIS) {
            applicationStatus = new ApplicationStatusMbus(waveFlow100mW);
        }
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    public final int readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(waveFlow100mW);
            operatingMode.read();
        }
        return operatingMode.getOperatingMode();
    }

    public final void writeOperatingMode(final int operatingModeVal, final int mask) throws IOException {
        operatingMode = new OperatingMode(waveFlow100mW);
        operatingMode.setOperatingMode(operatingModeVal);
        operatingMode.setMask(mask);
        operatingMode.write();
    }

    public final void writeOperatingMode(final int operatingModeVal) throws IOException {
        operatingMode = new OperatingMode(waveFlow100mW);
        operatingMode.setOperatingMode(operatingModeVal);
        operatingMode.write();
    }

    public final Date readTimeDateRTC() throws IOException {
        TimeDateRTC o = new TimeDateRTC(waveFlow100mW);
        o.read();
        return o.getCalendar().getTime();
    }

    public final void writeTimeDateRTC(final Date date) throws IOException {
        TimeDateRTC o = new TimeDateRTC(waveFlow100mW);
        Calendar calendar = Calendar.getInstance(waveFlow100mW.getTimeZone());
        calendar.setTime(date);
        o.setCalendar(calendar);
        o.write();
    }


    public final int readHourOfMeasurement() throws IOException {
        if (hourOfMeasurement == null) {
            hourOfMeasurement = new HourOfMeasurement(waveFlow100mW);
            hourOfMeasurement.read();
        }
        return hourOfMeasurement.getHourId();
    }

    public final void writeHourOfMeasurement(final int hourId) throws IOException {
        hourOfMeasurement = new HourOfMeasurement(waveFlow100mW);
        hourOfMeasurement.setHourId(hourId);
        hourOfMeasurement.write();
    }

    public final int readDayOfWeek() throws IOException {
        if (dayOfWeek == null) {
            dayOfWeek = new DayOfWeek(waveFlow100mW);
            dayOfWeek.read();
        }
        return dayOfWeek.getDayId();
    }

    public final void writeDayOfWeek(final int dayId) throws IOException {
        dayOfWeek = new DayOfWeek(waveFlow100mW);
        dayOfWeek.setDayId(dayId);
        dayOfWeek.write();
    }

    public final int readSamplingPeriod() throws IOException {
        if (samplingPeriod == null) {
            samplingPeriod = new SamplingPeriod(waveFlow100mW);
            samplingPeriod.read();
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    public final void writeSamplingPeriod(final int samplingPeriodInSeconds) throws IOException {
        samplingPeriod = new SamplingPeriod(waveFlow100mW);
        samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
        samplingPeriod.write();
    }


    public final int readSamplingActivationType() throws IOException {
        if (samplingActivationType == null) {
            samplingActivationType = new SamplingActivationType(waveFlow100mW);
            samplingActivationType.read();
        }
        return samplingActivationType.getType();
    }

    public final void writeSamplingActivationImmediate() throws IOException {
        writeSamplingActivationType(SamplingActivationType.START_IMMEDIATE);
    }

    public final void writeSamplingActivationNextHour() throws IOException {
        writeSamplingActivationType(SamplingActivationType.START_NEXT_HOUR);
    }

    public final void writeSamplingActivationType(final int type) throws IOException {
        samplingActivationType = new SamplingActivationType(waveFlow100mW);
        samplingActivationType.setType(type);
        samplingActivationType.write();
    }


    public final int readMeasurementPeriod() throws IOException {
        if (measurementPeriod == null) {
            measurementPeriod = new MeasurementPeriod(waveFlow100mW);
            measurementPeriod.read();
        }
        return measurementPeriod.getMeasurementPeriod();
    }

    public final void writeMeasurementPeriod(final int measurementPeriodVal) throws IOException {
        measurementPeriod = new MeasurementPeriod(waveFlow100mW);
        measurementPeriod.setMeasurementPeriod(measurementPeriodVal);
        measurementPeriod.write();
    }

    /**
     * This is the combination of SamplingPeriod and MeasurementPeriod
     *
     * @return the interval in seconds
     */
    public final int getProfileIntervalInSeconds() throws IOException {
        return readSamplingPeriod() * readMeasurementPeriod();
    }


    public final int readNrOfLoggedRecords() throws IOException {
        NrOfLoggedRecords o = new NrOfLoggedRecords(waveFlow100mW);
        o.read();
        return o.getNrOfRecords();
    }

    /**
     * Read the encodermodel for the port A (portId<=0) or B (portId>=1)
     *
     * @param portId
     * @return the encoder model
     * @throws IOException
     */
    public final EncoderModel readEncoderModel(int portId) throws IOException {

        // validate the portId
        if (portId < 0) {
            portId = 0;
        }
        if (portId > 1) {
            portId = 1;
        }

        if (encoderModels[portId] == null) {
            encoderModels[portId] = new EncoderModel(waveFlow100mW, portId);
            encoderModels[portId].read();
        }
        return encoderModels[portId];
    }

    /**
     * Read the encoderunit for the port A (portId<=0) or B (portId>=1)
     *
     * @param portId
     * @return the encoder unit
     * @throws IOException
     */
    public final EncoderUnit readEncoderUnit(int portId) throws IOException {
        // validate the portId
        if (portId < 0) {
            portId = 0;
        }
        if (portId > 1) {
            portId = 1;
        }

        if (encoderUnits[portId] == null) {
            encoderUnits[portId] = new EncoderUnit(waveFlow100mW, portId);
            encoderUnits[portId].read();
        }
        return encoderUnits[portId];
    }

    public final void writeEncoderUnit(int portId, EncoderUnitType encoderUnitType, int nrOfDigitsBeforeDecimalPoint) throws IOException {
        encoderUnits[portId] = new EncoderUnit(waveFlow100mW, portId);
        encoderUnits[portId].setEncoderUnitInfo(new EncoderUnitInfo(encoderUnitType, nrOfDigitsBeforeDecimalPoint));
        encoderUnits[portId].write();
    }


    public final BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
        BatteryLifeDurationCounter o = new BatteryLifeDurationCounter(waveFlow100mW);
        o.read();
        return o;
    }

    public final Date readBatteryLifeDateEnd() throws IOException {
        BatteryLifeDateEnd o = new BatteryLifeDateEnd(waveFlow100mW);
        o.read();
        return (o.getCalendar() == null ? null : o.getCalendar().getTime());
    }

    public final Date readBackflowDetectionDate(int portId) throws IOException {
        BackflowDetectionDate backflowDetectionDate = new BackflowDetectionDate(waveFlow100mW, portId);
        backflowDetectionDate.read();
        return backflowDetectionDate.getDate();
    }

    public final int readBackflowDetectionFlags(int portId) throws IOException {
        BackflowDetectionFlags backflowDetectionflags = new BackflowDetectionFlags(waveFlow100mW, portId);
        backflowDetectionflags.read();
        return backflowDetectionflags.getFlags();
    }

    public final Date readCommunicationErrorDetectionDate(int portId) throws IOException {
        CommunicationErrorDetectionDate communicationErrorDetectionDate = new CommunicationErrorDetectionDate(waveFlow100mW, portId);
        communicationErrorDetectionDate.read();
        return communicationErrorDetectionDate.getDate();
    }

    public final Date readCommunicationErrorReadingDate(int portId) throws IOException {
        CommunicationErrorReadingDate communicationErrorReadingDate = new CommunicationErrorReadingDate(waveFlow100mW, portId);
        communicationErrorReadingDate.read();
        return communicationErrorReadingDate.getDate();
    }

    public final void writeAlarmConfiguration(final int alarmConfigurationValue) throws IOException {
        AlarmConfiguration alarmConfiguration = new AlarmConfiguration(waveFlow100mW);
        alarmConfiguration.setAlarmConfiguration(alarmConfigurationValue);
        alarmConfiguration.write();
    }

    public final int readAlarmConfiguration() throws IOException {
        AlarmConfiguration alarmConfiguration = new AlarmConfiguration(waveFlow100mW);
        alarmConfiguration.read();
        return alarmConfiguration.getAlarmConfiguration();
    }
}
