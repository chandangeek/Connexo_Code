/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class ConfigurePreferredDateModeV1 implements IReadWriteObject
{
    public ConfigurePreferredDateModeV1()
    {
    }

    private static final ObisCode obisCode = new ObisCode(0, 128, 96, 194, 101, 255);
    private static final ObisCode ocWanStdControl = new ObisCode(0, 128, 96, 194, 102, 255);

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        CosemAttributeDescriptor attributeDescriptor;

        long distance = (Long)data[0];
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 10);
        layer.setAttribute(attributeDescriptor, new DlmsDataDoubleLongUnsigned(distance));

        attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 17);
        DlmsDataDoubleLongUnsigned controlFlags = (DlmsDataDoubleLongUnsigned)layer.
                getAttributeAndCheckResult(
                        attributeDescriptor);
        long flags = (controlFlags.getValue() & (0xFF ^ 0x3)) | 0x02;
        controlFlags = new DlmsDataDoubleLongUnsigned(flags);
        layer.setAttributeAndCheckResult(attributeDescriptor, controlFlags);

        // send reinit
        attributeDescriptor = new CosemAttributeDescriptor(ocWanStdControl, 9139, 4);
        DlmsDataLongUnsigned command = new DlmsDataLongUnsigned(3);
        layer.setAttributeAndCheckResult(attributeDescriptor, command);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
