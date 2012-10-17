package test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception;

import com.energyict.protocol.IntervalData;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/03/11
 * Time: 16:16
 */
public class CTRExceptionWithIntervalData extends CTRException {

    private final List<IntervalData> intervalDatas;
    private final Exception exception;

    public CTRExceptionWithIntervalData(Exception e, List<IntervalData> intervalDatas) {
        super(e);
        this.intervalDatas = intervalDatas != null ? intervalDatas : new ArrayList<IntervalData>();
        this.exception = e;
    }

    public CTRExceptionWithIntervalData(String message, Exception e, List<IntervalData> intervalDatas) {
        super(message, e);
        this.intervalDatas = intervalDatas != null ? intervalDatas : new ArrayList<IntervalData>();
        this.exception = e;
    }

    public List<IntervalData> getIntervalDatas() {
        return intervalDatas;
    }

    public Exception getException() {
        return exception;
    }
}
