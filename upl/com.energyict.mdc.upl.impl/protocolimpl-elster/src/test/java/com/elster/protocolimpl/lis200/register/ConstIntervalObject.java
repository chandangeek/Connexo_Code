package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.IntervalObject;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * User: heuckeg
 * Date: 15.04.11
 * Time: 10:26
 */
public class ConstIntervalObject extends IntervalObject {

    private String value;

    public ConstIntervalObject(ProtocolLink link, int instance, String address) {
        super(link, instance, address);
        value = address;
    }

    @Override
    public String getValue() {
        return value;
    }
}
