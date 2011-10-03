package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.MaxDemandObject;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Created by IntelliJ IDEA.
 * User: heuckeg
 * Date: 15.04.11
 * Time: 11:14
 * To change this template use File | Settings | File Templates.
 */
public class ConstMaxDemandObject extends MaxDemandObject {

    private String value;

    public ConstMaxDemandObject(ProtocolLink link, int instance, String address) {
        super(link, instance, address);
        value = address;
    }

    public String readRawValue() {
        return value;
    }

}
