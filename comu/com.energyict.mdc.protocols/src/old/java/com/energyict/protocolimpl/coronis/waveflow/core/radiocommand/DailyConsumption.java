/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.DataLoggingDayOfWeek;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.DataLoggingMinuteOfMeasurement;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.DataLoggingTimeOfMeasurement;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.NumberOfLoggedRecords;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.SamplingActivationType;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.SamplingPeriod;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class DailyConsumption extends AbstractRadioCommand {

    private int genericHeaderLength = 23;

    public DailyConsumption(WaveFlow waveFlow) {
        super(waveFlow);
    }


    private NumberOfLoggedRecords numberOfRecords;
    private int numberOfInputs = 0;
    private IndexZone indexZone;
    private Date lastLoggedReading;
    private SamplingPeriod samplingPeriod;
    private SamplingActivationType startHour;
    private DataLoggingDayOfWeek dayOfWeek;
    private DataLoggingTimeOfMeasurement timeOfMeasurement;
    private DataLoggingMinuteOfMeasurement startMinuteOfMeasurement;
    private long[][] receivedValues;

    public IndexZone getIndexZone() {
        return indexZone;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.DailyConsumption;
    }

    public DataLoggingDayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public Date getLastLoggedReading() {
        return lastLoggedReading;
    }

    public NumberOfLoggedRecords getNumberOfRecords() {
        return numberOfRecords;
    }

    public long[][] getReceivedValues() {
        return receivedValues;
    }

    public SamplingPeriod getSamplingPeriod() {
        return samplingPeriod;
    }

    public SamplingActivationType getStartHour() {
        return startHour;
    }

    public DataLoggingMinuteOfMeasurement getStartMinuteOfMeasurement() {
        return startMinuteOfMeasurement;
    }

    public DataLoggingTimeOfMeasurement getTimeOfMeasurement() {
        return timeOfMeasurement;
    }

    public void setGenericHeaderLength(int length) {
        this.genericHeaderLength = length;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        int offset = 0;

        GenericHeader genericHeader = new GenericHeader(getWaveFlow());
        genericHeader.parse(data);  //Parses all metadata, caches it in the factory.

        numberOfInputs = getWaveFlow().getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        offset += genericHeaderLength;

        indexZone = new IndexZone(getWaveFlow());
        indexZone.parse(data, offset);
        offset += 39;

        TimeZone timeZone = getWaveFlow().getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        lastLoggedReading = TimeDateRTCParser.parse(data, offset, 7, timeZone).getTime();
        offset += 7;

        samplingPeriod = new SamplingPeriod(getWaveFlow());
        samplingPeriod.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        offset++;

        startHour = new SamplingActivationType(getWaveFlow());
        startHour.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        offset++;

        dayOfWeek = new DataLoggingDayOfWeek(getWaveFlow());
        dayOfWeek.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        offset++;

        timeOfMeasurement = new DataLoggingTimeOfMeasurement(getWaveFlow());
        timeOfMeasurement.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        offset++;

        startMinuteOfMeasurement = new DataLoggingMinuteOfMeasurement(getWaveFlow());
        startMinuteOfMeasurement.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        offset++;

        numberOfRecords = new NumberOfLoggedRecords(getWaveFlow());
        numberOfRecords.parse(ProtocolTools.getSubArray(data, offset, offset + 2));
        offset += 2;

        int numberOfReceivedValues = 24 / getNumberOfInputs();
        receivedValues = new long[getNumberOfInputs()][numberOfReceivedValues];
        for (int i = 0; i < getNumberOfInputs(); i++) {
            for (int j = 0; j < numberOfReceivedValues; j++) {
                receivedValues[i][j] = ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
        }
    }

    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    public int getNumberOfInputs() throws IOException {
        if (numberOfInputs == 0) {
            numberOfInputs = getWaveFlow().getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        }
        return numberOfInputs;
    }
}