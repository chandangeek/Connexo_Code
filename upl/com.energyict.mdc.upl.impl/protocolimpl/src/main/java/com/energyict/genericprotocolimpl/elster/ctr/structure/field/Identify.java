package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Class for the Identify field in a CTR Structure Object - used in firmware upgrade process
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Identify extends AbstractField<Identify> {

    private int identify;
    private static final int LENGTH = 4;

    public Identify(byte[] identify) {
        this.identify = ProtocolTools.getIntFromBytes(identify);
    }

    public Identify(int identify) {
        this.identify = identify;
    }

    public Identify() {
    }

    public int getIdentify() {
        return identify;
    }

    public String getHexIdentify() {
        return Integer.toHexString(identify);
    }

    public void setIdentify(int identify) {
        this.identify = identify;
    }

    public byte[] getBytes() {
        return getBytesFromInt(getIdentify(), LENGTH);
    }

    public Identify parse(byte[] rawData, int offset) throws CTRParsingException {
        setIdentify(getIntFromBytes(rawData, offset, LENGTH));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }
}