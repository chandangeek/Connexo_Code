package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.UniversalObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.A2ObjectList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EI7ObjectList extends A2ObjectList {
    private UniversalObject[] objectList = {
            new UniversalObject(ObisCode.fromString("0.1.96.5.4.255").getLN(), 1, 0),
    };

    public UniversalObject[] getObjectList() {
        List<UniversalObject> list = Arrays.stream(super.getObjectList()).collect(Collectors.toList());
        list.addAll(Arrays.stream(objectList).collect(Collectors.toList()));

        return (UniversalObject[]) list.toArray();
    }
}
