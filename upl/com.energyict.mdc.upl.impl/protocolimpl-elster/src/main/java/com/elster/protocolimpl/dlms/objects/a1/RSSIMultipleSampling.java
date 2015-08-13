/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  19.05.2014 10:20:59
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
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
class RSSIMultipleSampling implements IReadWriteObject
{
    private final ObisCode obisCode;

    public RSSIMultipleSampling(ObisCode obisCode)
    {
        this.obisCode = obisCode;
    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, 9136, 8);

        DlmsDataDoubleLongUnsigned dlmsData = new DlmsDataDoubleLongUnsigned((Integer)data[0]);

        layer.setAttributeAndCheckResult(descriptor, dlmsData);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, 9136, 8);

        DlmsData data = layer.getAttributeAndCheckResult(descriptor);

        return data.getValue().toString();
    }

}
