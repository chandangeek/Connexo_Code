package test.com.energyict.protocolimplV2.elster.ctr.MTU155.structure.field;

import test.com.energyict.protocolimplV2.elster.ctr.MTU155.common.AbstractField;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the Counter_Q field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Counter_Q extends AbstractField<Counter_Q> {

    private int counter_Q;

    public Counter_Q() {
        this(0);
    }

    public Counter_Q(int counter_Q) {
        this.counter_Q = counter_Q;
    }

    public byte[] getBytes() {
        return getBytesFromInt(counter_Q, getLength());
    }

    public Counter_Q parse(byte[] rawData, int offset) throws CTRParsingException {
        this.counter_Q = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getLength() {
        return 1;
    }

    public int getCounter_Q() {
        return counter_Q;
    }

    public void setCounter_Q(int counter_Q) {
        this.counter_Q = counter_Q;
    }
}
