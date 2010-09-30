package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class Channel extends AbstractField<Channel> {

    public static final int LENGTH = 1;
    private int channel;

    public byte[] getBytes() {
        return getBytesFromInt(channel, LENGTH);
    }

    public Channel parse(byte[] rawData, int offset) {
        channel = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

}
