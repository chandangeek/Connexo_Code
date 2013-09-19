package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

import java.util.*;

/**
 * Containing profile data for one input channel
 */
public class ExtendedDataloggingTable extends AbstractRadioCommand {

    /**
     * Can only get one input channel at a time...
     * bit0: input A
     * bit1: input B
     * bit2: input C
     * bit3: input D
     */
    private int indexMask = 0x00;
    private int nrOfValues = 1;
    private Date toDate;
    private Long[] readingsInputs = new Long[]{};
    private final int MAX_NR_OF_INPUTS = 4;
    private int[] nrOfReadings = new int[MAX_NR_OF_INPUTS];
    private Date mostRecentRecordTimeStamp = null;
    private static final int MAX_NR_OF_VALUES = 0xFFFF;    //See documentation v2
    private long offset = -1;
    private int indexFirst = -1;
    private boolean monthly = false;

    public long getOffset() {
        if (offset == -1) {
            offset = convertToIndexNumber(toDate);
        }
        if (offset > Integer.MAX_VALUE || offset < 0) {
            throw createWaveFlowException("Error requesting the extended index logging data");
        }
        return offset;
    }

    public int getNumberOfFirstIndex() {
        return indexFirst;
    }

    public final int[] getNrOfReadings() {
        return nrOfReadings;
    }

    public void getMostRecentRecord() {
        ExtendedDataloggingTable mostRecentRecord = getWaveFlow().getRadioCommandFactory().getMostRecentRecord();
        indexFirst = mostRecentRecord.getNumberOfFirstIndex();
        mostRecentRecordTimeStamp = mostRecentRecord.getMostRecentRecordTimeStamp();
    }

    public Date getMostRecentRecordTimeStamp() {
        return mostRecentRecordTimeStamp;
    }

    final public Long[] getReadingsInputs() {
        return readingsInputs;
    }

    ExtendedDataloggingTable(final WaveFlow waveFlow) {
        super(waveFlow);
    }

    ExtendedDataloggingTable(final WaveFlow waveFlow, int inputChannelIndex, final int nrOfValues, final Date toDate) {
        super(waveFlow);
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.toDate = toDate;
        this.indexMask = getIndexMask(inputChannelIndex);
    }

    ExtendedDataloggingTable(final WaveFlow waveFlow, int inputChannelIndex, final int nrOfValues, final Date toDate, final long offset) {
        super(waveFlow);
        this.offset = offset;       //Offset is the same like the other input channel requests.
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.toDate = toDate;
        this.indexMask = getIndexMask(inputChannelIndex);
    }

    ExtendedDataloggingTable(final WaveFlow waveFlow, int inputChannelIndex, final int nrOfValues, final long offset) {
        super(waveFlow);
        this.offset = offset;       //Offset is the same like the other input channel requests.
        this.nrOfValues = checkNrOfValues(nrOfValues);
        this.indexMask = getIndexMask(inputChannelIndex);
    }

    private int checkNrOfValues(int nrOfValues) {
        return (nrOfValues < MAX_NR_OF_VALUES ? nrOfValues : MAX_NR_OF_VALUES - 1);
    }

    /**
     * replaces index [1, 2, 3, 4] by mask [1, 2, 4, 8]
     *
     * @param inputChannelIndex
     * @return mask
     */
    private int getIndexMask(int inputChannelIndex) {
        return (int) Math.pow((double) 2, (double) (inputChannelIndex - 1));
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedDataloggingTable;
    }

    @Override
    protected void parse(byte[] data) {

        int frameCounter = 0;
        int nrOfFrames;

        if (WaveflowProtocolUtils.toInt(data[0]) == 0xFF) {
            throw createWaveFlowException("Error requesting load profile, returned [FF]");
        }

        List<Long> readingsInputLists = new ArrayList<Long>();
        int offset = 0;

        //Parse all sent frames, containing a chain of profile data for ONE channel.
        do {
            if (frameCounter == 0) {
                frameCounter = data[offset++] & 0xFF;
                nrOfFrames = data[offset++] & 0xFF;
                mostRecentRecordTimeStamp = TimeDateRTCParser.parse(ProtocolTools.getSubArray(data, offset, offset + 7), getWaveFlow().getTimeZone()).getTime();
                offset += 7;
            } else {
                // in case of a multiple frame, the first byte of the following data is the commmandId acknowledge
                int commandIdAck = data[offset++] & 0xFF;
                if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
                    throw createWaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
                }
                frameCounter = data[offset++] & 0xFF;
                nrOfFrames = data[offset++] & 0xFF;
            }
            int requestedIndexMask = data[offset++] & 0xFF;
            int inputIndex = log2Lookup(requestedIndexMask);
            if (requestedIndexMask != indexMask) {
                throw createWaveFlowException("The received input channel [" + inputIndex + "] doesn't match the requested channel [" + log2Lookup(indexMask) + "]");
            }
            int indexFirstRequestedRecord = ProtocolTools.getIntFromBytes(data, offset, 2);
            offset += 2;
            if (indexFirst < 0) {
                indexFirst = indexFirstRequestedRecord;
            }
            int indexLast = ProtocolTools.getIntFromBytes(data, offset, 2);
            offset += 2;
            int nrOfReadings = (indexFirstRequestedRecord - indexLast) + 1;
            this.nrOfReadings[inputIndex] += nrOfReadings;
            for (int i = 0; i < nrOfReadings; i++) {
                readingsInputLists.add((long) ProtocolTools.getIntFromBytes(data, offset, 4));
                offset += 4;
            }

        } while (frameCounter < nrOfFrames);

        readingsInputs = readingsInputLists.toArray(readingsInputs);

    }

    private int log2Lookup(int mask) {
        if (mask == 8) {
            return 3;
        } else if (mask == 4) {
            return 2;
        } else if (mask == 2) {
            return 1;
        } else if (mask == 1) {
            return 0;
        } else {
            throw createWaveFlowException("Invalid input mask, [" + mask + "]");
        }
    }

    @Override
    protected byte[] prepare() {
        byte[] indexMaskBytes = {(byte) indexMask};
        byte[] nrOfValuesBytes = ProtocolTools.getBytesFromInt(nrOfValues, 2);
        byte[] offsetBytes = ProtocolTools.getBytesFromInt((int) getOffset(), 2);
        return ProtocolTools.concatByteArrays(indexMaskBytes, nrOfValuesBytes, offsetBytes);
    }

    /**
     * Check the timestamp of the meter's newest data record and compare it to the given toDate in order to calculate the necessary offset
     *
     * @param toDate the to date
     * @return the offset between the given to date and the last record's time stamp.
     */
    private long convertToIndexNumber(Date toDate) {
        Calendar now = new GregorianCalendar(getWaveFlow().getTimeZone());
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Calendar toCal = new GregorianCalendar(getWaveFlow().getTimeZone());
        toCal.setTime(toDate);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);

        if (now.getTime().equals(toCal.getTime())) {
            return 0;
        }

        if (toCal.getTime().after(now.getTime())) {
            return 0;
        } else if (monthly) {
            getMostRecentRecord();
            Calendar lastLogged = Calendar.getInstance(getWaveFlow().getTimeZone());
            lastLogged.setTime(getMostRecentRecordTimeStamp());
            lastLogged.setLenient(true);
            int numberOfMonthsEarlier = 0;

            //Go back month by month until you have the date closest to the toDate.
            while (lastLogged.getTime().after(toDate)) {
                lastLogged.add(Calendar.MONTH, -1);
                numberOfMonthsEarlier++;
            }
            return indexFirst - numberOfMonthsEarlier;
        } else {
            getMostRecentRecord();
            long timeDiff = (getMostRecentRecordTimeStamp().getTime() - toCal.getTimeInMillis()) / 1000;
            return indexFirst - (timeDiff / getWaveFlow().getProfileInterval());
        }
    }

    public void setMonthly(boolean monthly) {
        this.monthly = monthly;
    }
}