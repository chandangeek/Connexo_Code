package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

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

    public long getOffset() throws IOException {
        if (offset == -1) {
            offset = convertToIndexNumber(toDate);
        }
        if (offset > Integer.MAX_VALUE || offset < 0) {
            throw new WaveFlowException("Error requesting the extended index logging data");
        }
        return offset;
    }

    public int getNumberOfFirstIndex() {
        return indexFirst;
    }

    public final int[] getNrOfReadings() {
        return nrOfReadings;
    }

    public void getMostRecentRecord() throws IOException {
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
    protected void parse(byte[] data) throws IOException {

        int frameCounter = 0;
        int nrOfFrames;

        if (WaveflowProtocolUtils.toInt(data[0]) == 0xFF) {
            throw new WaveFlowException("Error requesting load profile, returned [FF]");
        }

        DataInputStream dais = null;
        try {

            dais = new DataInputStream(new ByteArrayInputStream(data));
            List<Long> readingsInputLists = new ArrayList<Long>();

            //Parse all sent frames, containing a chain of profile data for ONE channel.
            do {
                if (frameCounter == 0) {
                    frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                    nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
                    byte[] temp = new byte[7];
                    dais.read(temp);
                    mostRecentRecordTimeStamp = TimeDateRTCParser.parse(temp, getWaveFlow().getTimeZone()).getTime();
                } else {
                    // in case of a multiple frame, the first byte of the following data is the commmandId acknowledge
                    int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
                        throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
                    }
                    frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                    nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
                }
                int requestedIndexMask = WaveflowProtocolUtils.toInt(dais.readByte());
                int inputIndex = log2Lookup(requestedIndexMask);
                if (requestedIndexMask != indexMask) {
                    throw new WaveFlowException("The received input channel [" + inputIndex + "] doesn't match the requested channel [" + log2Lookup(indexMask) + "]");
                }
                int indexFirstRequestedRecord = dais.readShort();
                if (indexFirst < 0) {
                    indexFirst = indexFirstRequestedRecord;
                }
                int indexLast = dais.readShort();
                int nrOfReadings = (indexFirstRequestedRecord - indexLast) + 1;
                this.nrOfReadings[inputIndex] += nrOfReadings;
                for (int i = 0; i < nrOfReadings; i++) {
                    readingsInputLists.add((long) dais.readInt());
                }

            } while (frameCounter < nrOfFrames);

            readingsInputs = readingsInputLists.toArray(readingsInputs);
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private int log2Lookup(int mask) throws IOException {
        if (mask == 8) {
            return 3;
        } else if (mask == 4) {
            return 2;
        } else if (mask == 2) {
            return 1;
        } else if (mask == 1) {
            return 0;
        } else {
            throw new WaveFlowException("Invalid input mask, [" + mask + "]");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(indexMask);
            daos.writeShort(nrOfValues);
            daos.writeShort((int) getOffset());
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    /**
     * Check the timestamp of the meter's newest data record and compare it to the given toDate in order to calculate the necessary offset
     *
     * @param toDate the to date
     * @return the offset between the given to date and the last record's time stamp.
     */
    private long convertToIndexNumber(Date toDate) throws IOException {
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