/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.SamplingPeriod;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class ExtendedIndexReading extends AbstractRadioCommand {

    private Calendar dateOfLastMonthsEnd;
    private int numberOfEnabledInputs;
    private int applicationStatus;
    private byte[] immediateIndexesArea;
    private byte[] indexesOfEndOfMonth;
    private List<Long[]> last4LoggedIndexes = new ArrayList<Long[]>();
    private Date dateOfLastLoggedValue;
    private SamplingPeriod dataloggingMeasurementPeriod;

    public ExtendedIndexReading(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public Date getDateOfLastLoggedValue() {
        return dateOfLastLoggedValue;
    }

    public List<Long[]> getLast4LoggedIndexes() {
        return last4LoggedIndexes;
    }

    public int getIndexOfLastMonth(int channel) throws IOException {
        int offset = channel * 4;
        byte[] data = WaveflowProtocolUtils.getSubArray(indexesOfEndOfMonth, offset, 4);
        return ProtocolTools.getIntFromBytes(data);        //The index is signed!
    }

    public int getCurrentIndex(int channel) throws IOException {
        int offset = channel * 4;
        byte[] data = WaveflowProtocolUtils.getSubArray(immediateIndexesArea, offset, 4);
        return ProtocolTools.getIntFromBytes(data);        //The index is signed!
    }

    public int getNumberOfEnabledInputs() {
        return numberOfEnabledInputs;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedIndexReading;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        int offset = 0;
        operationMode = WaveflowProtocolUtils.toInt(data[offset]);
        getWaveFlow().getParameterFactory().setOperatingMode(operationMode);
        numberOfEnabledInputs = getWaveFlow().getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        offset++;

        applicationStatus = WaveflowProtocolUtils.toInt(data[offset]);
        getWaveFlow().getParameterFactory().setApplicationStatus(applicationStatus);
        offset++;

        immediateIndexesArea = WaveflowProtocolUtils.getSubArray(data, offset, 4 * getNumberOfEnabledInputs());
        offset += 4 * getNumberOfEnabledInputs();

        indexesOfEndOfMonth = WaveflowProtocolUtils.getSubArray(data, offset, 4 * getNumberOfEnabledInputs());
        offset += 4 * getNumberOfEnabledInputs();

        //Parses the profile data
        for (int input = 0; input < numberOfEnabledInputs; input++) {
            Long[] indexes = new Long[4];
            for (int index = 0; index < 4; index++) {
                indexes[index] = (long) ProtocolTools.getIntFromBytes(data, offset, 4);
                offset += 4;
            }
            last4LoggedIndexes.add(indexes);
        }

        TimeZone timeZone = getWaveFlow().getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        dateOfLastLoggedValue = TimeDateRTCParser.parse(data, offset, 6, timeZone).getTime();
        offset += 6;

        dataloggingMeasurementPeriod = new SamplingPeriod(getWaveFlow());
        dataloggingMeasurementPeriod.parse(WaveflowProtocolUtils.getSubArray(data, offset, 1));

        dateOfLastMonthsEnd = parseDateOfLastMonthsEnd();
    }

    public SamplingPeriod getDataloggingMeasurementPeriod() {
        return dataloggingMeasurementPeriod;
    }

    private Calendar parseDateOfLastMonthsEnd() throws IOException {
        TimeZone timeZone = getWaveFlow().getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        Calendar calLastOfMonth = new GregorianCalendar(timeZone);
        calLastOfMonth.setTime(dateOfLastLoggedValue);
        calLastOfMonth.set(Calendar.DATE, 1);
        calLastOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        calLastOfMonth.set(Calendar.MINUTE, 0);
        calLastOfMonth.set(Calendar.SECOND, 0);
        calLastOfMonth.set(Calendar.MILLISECOND, 0);
        return calLastOfMonth;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];        //Empty byte array = do nothing
    }

    //The date that goes with the indexes of the end of last month.
    public Date getDateOfLastMonthsEnd() {
        return dateOfLastMonthsEnd.getTime();
    }
}