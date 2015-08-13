/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  20.05.2014 14:02:25
 */

package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataUnsigned;

import java.io.IOException;

/**
 * This class ...
 *
 * @author heuckeg
 */
class FlagGasDayConfig implements IReadWriteObject
{
    private final ObisCode obisCode = new ObisCode("7.128.0.9.23.255");

    public FlagGasDayConfig()
    {
    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, 1, 2);

        DlmsDataUnsigned dlmsData = new DlmsDataUnsigned((Integer)data[0]);

        layer.setAttributeAndCheckResult(descriptor, dlmsData);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, 1, 2);

        DlmsData data = layer.getAttributeAndCheckResult(descriptor);

        return data.getValue().toString();
    }

}
