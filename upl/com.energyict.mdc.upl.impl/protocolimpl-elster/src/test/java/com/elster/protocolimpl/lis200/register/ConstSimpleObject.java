package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.objects.SimpleObject;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * Created by IntelliJ IDEA.
 * User: heuckeg
 * Date: 15.04.11
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class ConstSimpleObject extends SimpleObject {

    private String value;
    public ConstSimpleObject(ProtocolLink link, int instance, String address) {
        super(link, instance, address);
        value = address;
    }

    public String getValue() {
        return value;
    }
}
