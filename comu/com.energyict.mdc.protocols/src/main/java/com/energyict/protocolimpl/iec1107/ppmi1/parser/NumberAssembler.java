/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus;

import java.io.IOException;
import java.math.BigDecimal;

class NumberAssembler implements Assembler {

    private static final int BYTES_PER_CHANNEL = 3;

    private int byteNr;
    private int[] val = null;

    private ProfileParser profileParser;

    public NumberAssembler(ProfileParser profileParser) {
        this.profileParser = profileParser;
    }

    public void workOn(Assembly ta) throws IOException {

        Day day = (Day) ta.getTarget();
        byte bte = ((Byte) ta.pop()).byteValue();

        if (day == null) {
            return;
        }

        getVal()[this.byteNr] = bte;
        this.byteNr++;

        if (this.byteNr != (getProfileParser().getNrOfChannels() * BYTES_PER_CHANNEL)) {
            return;
        }

        Interval interval = day.getReading()[day.getReadIndex()];

        // TODO can be 49 too ... // sh*t!
        if ((day.getReadIndex() < 48) && interval.getDate().before(getProfileParser().getMeterTime())) {

            /* 1) create a status object */
            day.setStatus(new LoadProfileStatus((byte) ((getVal()[0] >> 4) & 0x0F)), day.getReadIndex());

            /* 2) create a reading */
            for (int channelNr = 0; channelNr < getProfileParser().getNrOfChannels(); channelNr++) {
                BigDecimal value = constructValue(getVal(), channelNr * BYTES_PER_CHANNEL);
                interval.setValue(value, channelNr);
            }

            /* 3) some debugging info */
            day.setReadingString(
                    " ->" + getVal()[0] +
                            " " + getVal()[1] +
                            " " + getVal()[2],
                    day.getReadIndex()
            );

        }
        this.byteNr = 0;
        day.incReadIndex();

    }

    public void setByteNr(int byteNr) {
        this.byteNr = byteNr;
    }

    private int[] getVal() {
        if (this.val == null) {
            this.val = new int[getProfileParser().getNrOfChannels() * BYTES_PER_CHANNEL];
        }
        return this.val;
    }

    private BigDecimal constructValue(int[] iArray, int i) throws IOException {
        long v = PPMUtils.hex2dec((byte) (iArray[i] & 0x0F)) * 10000;
        v += PPMUtils.hex2dec((byte) ((iArray[i + 1]))) * 100;
        v += PPMUtils.hex2dec((byte) iArray[i + 2]);
        return getProfileParser().getScalingFactor().toProfileNumber(v);
    }

    /**
     * Getter for the profileParser
     * @return
     */
    public ProfileParser getProfileParser() {
        return profileParser;
    }

}