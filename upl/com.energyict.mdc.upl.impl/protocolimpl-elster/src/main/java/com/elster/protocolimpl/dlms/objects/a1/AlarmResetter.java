/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  28.03.2014 13:24:24
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;

import java.io.IOException;

/**
 * This class ...
 *
 * @author heuckeg
 */
class AlarmResetter implements IReadWriteObject
{

    private final ObisCode[] objects;

    public AlarmResetter(ObisCode object1, ObisCode object2)
    {
        objects = new ObisCode[] { object1, object2};
    }

    public ObisCode getObisCode()
    {
        return objects[0];
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        DlmsData parameter = new DlmsDataDoubleLongUnsigned(DlmsDataDoubleLongUnsigned.MAX_VALUE);

        for (ObisCode oc : objects)
        {
            CosemAttributeDescriptor objectDescriptor = new CosemAttributeDescriptor(oc, CosemClassIds.DATA, 2);
            layer.setAttributeAndCheckResult(objectDescriptor, parameter);
        }
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
