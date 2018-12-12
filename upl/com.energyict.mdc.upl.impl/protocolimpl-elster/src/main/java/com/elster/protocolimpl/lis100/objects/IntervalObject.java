package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.objects.api.IIntervalObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * Class to read and hold data of meter interval
 *
 * User: heuckeg
 * Date: 24.01.11
 * Time: 14:20
 */
public class IntervalObject extends IntegerObject implements IIntervalObject {

    /**
     * Constructor of interval object
     *
     * @param base - base object to get int value for interval
     */
    public IntervalObject(IntegerObject base) {
        super(base, 10);
    }

    /**
     * get meter interval (coded)
     *
     * @return meter interval in sec
     * @throws NestedIOException   - in case of an io error
     * @throws ConnectionException - in case of an io error
     */
    public int getIntValue() throws NestedIOException, ConnectionException {
        return super.getIntValue();
    }

    /**
     * get meter interval (in sec)
     *
     * @return meter interval in sec
     * @throws NestedIOException   - in case of an io error
     * @throws ConnectionException - in case of an io error
     */
    public int getIntervalSeconds() throws NestedIOException, ConnectionException {
        return StringToIntervalSec(super.getValue());
    }

    /**
     * calculates the interval in sec of a given device value
     *
     * @param s - interval value out of device
     * @return interval in s
     */
    public static int StringToIntervalSec(String s) {
        switch (Integer.parseInt(s)) {
            case 88:
                return 0;
            case 98:
                return 30 * 24 * 60 * 60;
            case 99:
                return 24 * 60 * 60;
            default:
                return Integer.parseInt(s) * 60;
        }
   }
}
