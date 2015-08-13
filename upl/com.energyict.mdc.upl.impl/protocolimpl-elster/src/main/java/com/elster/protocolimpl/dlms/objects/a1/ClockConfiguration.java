/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataBoolean;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataLong;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class ClockConfiguration implements IReadWriteObject
{
    private static final ObisCode obisCode = A1ObjectPool.DstEnabled;

    public ClockConfiguration()
    {

    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object param[]) throws IOException
    {
        // Timezone
        final CosemAttributeDescriptor tzDescriptor = new CosemAttributeDescriptor(A1ObjectPool.Clock,
                                                                            CosemClassIds.CLOCK, 3);
        DlmsData timezone = new DlmsDataLong((Integer)param[0]);
        layer.setAttributeAndCheckResult(tzDescriptor, timezone);

        // DST
        final CosemAttributeDescriptor dstDescriptor = new CosemAttributeDescriptor(A1ObjectPool.Clock,
                                                                            CosemClassIds.CLOCK, 8);
        DlmsData dst = new DlmsDataBoolean((Integer)param[1] > 0);
        layer.setAttributeAndCheckResult(dstDescriptor, dst);

        // Deviation
        final CosemAttributeDescriptor devDescriptor = new CosemAttributeDescriptor(A1ObjectPool.Clock,
                                                                            CosemClassIds.CLOCK, 7);
        DlmsData dev = new DlmsDataInteger((Integer)param[2]);
        layer.setAttributeAndCheckResult(devDescriptor, dev);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        // Timezone
        final CosemAttributeDescriptor tzDescriptor = new CosemAttributeDescriptor(A1ObjectPool.Clock,
                                                                            CosemClassIds.CLOCK, 3);
        DlmsData timezone = layer.getAttributeAndCheckResult(tzDescriptor);

        // DST
        final CosemAttributeDescriptor dstDescriptor = new CosemAttributeDescriptor(A1ObjectPool.Clock,
                                                                            CosemClassIds.CLOCK, 8);
        DlmsData dst = layer.getAttributeAndCheckResult(dstDescriptor);

        // Deviation
        final CosemAttributeDescriptor devDescriptor = new CosemAttributeDescriptor(A1ObjectPool.Clock,
                                                                            CosemClassIds.CLOCK, 7);
        DlmsData dev = layer.getAttributeAndCheckResult(devDescriptor);

        return String.format("Timezone: %s min  DST enabled: %s  DST deviation: %s min",
                             timezone.stringValue(), dst.stringValue(), dev.stringValue());
    }

}
