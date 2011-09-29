package com.elster.protocolimpl.lis100.objects;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;

/**
 * Class to read and hold data of software version of meter
 * <p/>
 * User: heuckeg
 * Date: 21.01.11
 * Time: 16:17
 */
public class SoftwareVersionObject extends IntegerObject{

    public SoftwareVersionObject(IBaseObject baseObject) {
        super(baseObject, 10);
    }
}
