/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.coronis.wavetherm.core.parameter.OperatingMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Containing profile data for one input channel
 */
public class ExtendedDataloggingTable extends AbstractRadioCommand {

    private int nrOfValues = 1;
    private Date toDate;

    private List<ProfileDataValue> profileDataSensor1 = new ArrayList<ProfileDataValue>();
    private List<ProfileDataValue> profileDataSensor2 = new ArrayList<ProfileDataValue>();
    private Date mostRecentRecordTimeStamp = null;
    private long offset = -1;
    private int indexFirst = -1;

    private static final int MAX_NR_OF_VALUES = 4500;    //See documentation

    public Date getMostRecentRecordTimeStamp() {
        return mostRecentRecordTimeStamp;
    }

    public int getNumberOfFirstIndex() {
        return indexFirst;
    }

    public long getOffset() throws IOException {
        if (offset == -1) {
            offset = convertToIndexNumber(toDate);
        }
        if (offset > Integer.MAX_VALUE || offset < 0) {
            throw new WaveFlowException("Error requesting the extended index logging data");
        }
        return offset;
    }

    public List<ProfileDataValue> getProfileDataSensor1() {
        return profileDataSensor1;
    }

    public List<ProfileDataValue> getProfileDataSensor2() {
        return profileDataSensor2;
    }

    public ExtendedDataloggingTable(final WaveTherm waveTherm) {
        super(waveTherm);
    }

    public ExtendedDataloggingTable(final WaveTherm waveTherm, final int nrOfValues, final Date toDate) throws IOException {
        super(waveTherm);
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.toDate = toDate;
    }

    public ExtendedDataloggingTable(final WaveTherm waveTherm, final int nrOfValues, long offset) throws IOException {
        super(waveTherm);
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.offset = offset;
    }

    private int checkNrOfValues(int nrOfValues) throws IOException {
        return (nrOfValues < getMaxNumberOfValues() ? nrOfValues : getMaxNumberOfValues() - 1);
    }

    private int getMaxNumberOfValues() throws IOException {
        return (MAX_NR_OF_VALUES / getNumberOfChannels());

    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedDataloggingTable;
    }

    @Override
    protected void parse(byte[] data) throws IOException {

        int frameCounter = 0;
        int nrOfFrames;

        if (WaveflowProtocolUtils.toInt(data[0]) == 0xFF) {
            throw new WaveFlowException("Error requesting load profile, returned [FF]");
        }

        DataInputStream dais = null;
        try {

            dais = new DataInputStream(new ByteArrayInputStream(data));
            do {
                if (frameCounter == 0) {
                    frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                    nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
                    byte[] temp = new byte[6];
                    dais.read(temp);
                    mostRecentRecordTimeStamp = TimeDateRTCParser.parse(temp, getWaveTherm().getTimeZone()).getTime();
                } else {
                    // in case of a multiple frame, the first byte of the following data is the commmandId acknowledge
                    int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
                        throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
                    }
                    frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                    nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
                }
                int indexFirstRequestedRecord = dais.readShort();
                if (indexFirst < 0) {
                    indexFirst = indexFirstRequestedRecord;
                }
                int indexLast = dais.readShort();
                int nrOfReadings = (indexFirstRequestedRecord - indexLast) + 1;
                for (int i = 0; i < (nrOfReadings / getNumberOfChannels()); i++) {
                    profileDataSensor1.add(new ProfileDataValue((int) dais.readShort() & 0xFFFF));
                    if (getNumberOfChannels() > 1) {
                        profileDataSensor2.add(new ProfileDataValue((int) dais.readShort() & 0xFFFF));
                    }
                }

            } while (frameCounter < nrOfFrames);
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeShort(nrOfValues);
            daos.writeShort((int) getOffset());
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    /**
     * Check the timestamp of the meter's newest data record (via the radio command factory),
     * and compare it to the given toDate in order to calculate the necessary offset
     *
     * @param toDate the to date
     * @return the offset between the given to date and the last record's time stamp.
     */
    private long convertToIndexNumber(Date toDate) throws IOException {

        Calendar now = new GregorianCalendar(getWaveTherm().getTimeZone());
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Calendar toCal = new GregorianCalendar(getWaveTherm().getTimeZone());
        toCal.setTime(toDate);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);
        OperatingMode operatingMode = getWaveTherm().getParameterFactory().readOperatingMode();

        if (now.getTime().equals(toCal.getTime())) {
            return 0;
        }

        if (toCal.getTime().after(now.getTime())) {
            return 0;
        }  else if (operatingMode.isMonthlyMeasurement()) {
            Calendar lastLogged = Calendar.getInstance(getWaveTherm().getTimeZone());
            ExtendedDataloggingTable newestRecord = getWaveTherm().getRadioCommandFactory().getMostRecentRecord();
            indexFirst = newestRecord.getNumberOfFirstIndex();
            lastLogged.setTime(newestRecord.getMostRecentRecordTimeStamp());
            lastLogged.setLenient(true);
            int numberOfMonthsEarlier = 0;

            //Go back month by month until you have the date closest to the toDate.
            while (lastLogged.getTime().after(toDate)) {
                lastLogged.add(Calendar.MONTH, -1);
                numberOfMonthsEarlier++;
            }
            return indexFirst - numberOfMonthsEarlier;
        } else {
            ExtendedDataloggingTable newestRecord = getWaveTherm().getRadioCommandFactory().getMostRecentRecord();
            long timeDiff = (newestRecord.getMostRecentRecordTimeStamp().getTime() - toCal.getTimeInMillis()) / 1000;
            indexFirst = newestRecord.getNumberOfFirstIndex();
            return indexFirst - (timeDiff / getWaveTherm().getProfileInterval());
        }
    }

    public int getNumberOfChannels() throws IOException {
        return getWaveTherm().getNumberOfChannels();
    }
}