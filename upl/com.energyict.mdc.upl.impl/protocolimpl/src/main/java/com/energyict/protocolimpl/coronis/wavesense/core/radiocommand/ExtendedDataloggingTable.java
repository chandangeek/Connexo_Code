package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.coronis.wavesense.core.parameter.OperatingMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
    private BigDecimal[] profileData = new BigDecimal[]{};
    private Date mostRecentRecordTimeStamp = null;
    private long offset = -1;
    private static final int MAX_NR_OF_VALUES = 4500;    //See documentation
    private static final double VOLTAGE_MULTIPLIER = (1 / 819);
    private static final double AMPERE_MULTIPLIER = (1 / 256);
    private static final int AMPERE_OFFSET = 4;
    private static final double AMPERE_MIN_VALUE = 4;
    private static final double AMPERE_MAX_VALUE = 20;
    private int indexFirst = -1;


    public Date getMostRecentRecordTimeStamp() {
        return mostRecentRecordTimeStamp;
    }

    public long getOffset() throws IOException {
        if (offset == -1) {
            offset = convertToIndexNumber(toDate);
        }
        return offset;
    }

    final public BigDecimal[] getProfileData() {
        return profileData;
    }

    public ExtendedDataloggingTable(final WaveSense waveSense) {
        super(waveSense);
    }

    public ExtendedDataloggingTable(final WaveSense waveSense, final int nrOfValues, final Date toDate) {
        super(waveSense);
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.toDate = toDate;
    }

    public ExtendedDataloggingTable(final WaveSense waveSense, final int nrOfValues, long offset) {
        super(waveSense);
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.offset = offset;
    }

    private int checkNrOfValues(int nrOfValues) {
        return (nrOfValues < MAX_NR_OF_VALUES ? nrOfValues : MAX_NR_OF_VALUES - 1);
    }

    @Override
    public String toString() {
        return "ExtendedDataloggingTable{" +
                "mostRecentRecordTimeStamp=" + mostRecentRecordTimeStamp +
                ", nrOfValues=" + nrOfValues +
                ", toDate=" + toDate +
                ", profileData=" + Arrays.toString(profileData) +
                ", offset=" + offset +
                '}';
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
            List<BigDecimal> readingsInputLists = new ArrayList<BigDecimal>();

            //Parse all sent frames, containing a chain of profile data for ONE channel.
            do {
                if (frameCounter == 0) {
                    frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                    nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
                    byte[] temp = new byte[6];
                    dais.read(temp);
                    mostRecentRecordTimeStamp = TimeDateRTCParser.parse(temp, getWaveSense().getTimeZone()).getTime();
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
                for (int i = 0; i < nrOfReadings; i++) {
                    readingsInputLists.add(calcValue((int) dais.readShort() & 0xFFFF));
                }

            } while (frameCounter < nrOfFrames);

            profileData = readingsInputLists.toArray(profileData);
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getWaveSense().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private BigDecimal calcValue(int value) throws IOException {
        if (getWaveSense().getRadioCommandFactory().readModuleType().isOfType05Voltage()) {
            return new BigDecimal(value * VOLTAGE_MULTIPLIER);
        } else if (getWaveSense().getRadioCommandFactory().readModuleType().isOfType420MilliAmpere()) {
            if (value == 0xEEEE) {
                return new BigDecimal(AMPERE_MIN_VALUE);
            } else if (value == 0xFFFF) {
                return new BigDecimal(AMPERE_MAX_VALUE);
            } else {
                return new BigDecimal((value * AMPERE_MULTIPLIER) + AMPERE_OFFSET);
            }
        } else {
            throw new WaveFlowException("Unrecognized module type. Should be Wavesense 0-5 V or Wavesense 4-20 mA");
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
                    getWaveSense().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
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
        Calendar now = new GregorianCalendar(getWaveSense().getTimeZone());
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Calendar toCal = new GregorianCalendar(getWaveSense().getTimeZone());
        toCal.setTime(toDate);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);
        OperatingMode operatingMode = getWaveSense().getParameterFactory().readOperatingMode();

        if (now.getTime().equals(toCal.getTime())) {
            return 0;
        }

        if (toCal.getTime().after(now.getTime())) {
            return 0;
        } else if (operatingMode.isMonthlyMeasurement()) {
            Calendar lastLogged = Calendar.getInstance(getWaveSense().getTimeZone());
            ExtendedDataloggingTable mostRecentRecord = getWaveSense().getRadioCommandFactory().getMostRecentRecord();
            indexFirst = mostRecentRecord.getNumberOfFirstIndex();
            lastLogged.setTime(mostRecentRecord.getMostRecentRecordTimeStamp());
            lastLogged.setLenient(true);
            int numberOfMonthsEarlier = 0;

            //Go back month by month until you have the date closest to the toDate.
            while (lastLogged.getTime().after(toDate)) {
                lastLogged.add(Calendar.MONTH, -1);
                numberOfMonthsEarlier++;
            }
            return indexFirst - numberOfMonthsEarlier;
        } else {
            ExtendedDataloggingTable mostRecentRecord = getWaveSense().getRadioCommandFactory().getMostRecentRecord();
            long timeDiff = (mostRecentRecord.getMostRecentRecordTimeStamp().getTime() - toCal.getTimeInMillis()) / 1000;
            indexFirst = mostRecentRecord.getNumberOfFirstIndex();
            return indexFirst - (timeDiff / getWaveSense().getProfileInterval());
        }
    }

    public int getNumberOfFirstIndex() {
        return indexFirst;
    }
}