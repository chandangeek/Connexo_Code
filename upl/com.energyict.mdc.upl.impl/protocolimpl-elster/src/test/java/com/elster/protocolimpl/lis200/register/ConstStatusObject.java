package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.StatusObject;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Created by IntelliJ IDEA.
 * User: heuckeg
 * Date: 15.04.11
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class ConstStatusObject extends StatusObject {

    private String value;

    public ConstStatusObject(ProtocolLink link, int instance, String address) {
        super(link, instance, address);
        value = address;
    }

    @Override
    public String readRawValue() {
        return value;
    }
}
