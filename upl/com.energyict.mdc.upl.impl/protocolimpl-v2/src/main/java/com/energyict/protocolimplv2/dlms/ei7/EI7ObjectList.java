package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.UniversalObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.A2ObjectList;

public class EI7ObjectList extends A2ObjectList {
    private UniversalObject[] objectList = {
            new UniversalObject(ObisCode.fromString("0.1.96.5.4.255").getLN(),1,0),
    };

    public UniversalObject[] getObjectList() {
        return objectList;
    }
}
