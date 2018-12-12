/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  24.04.2014 09:30:36
 */

package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
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
class BillingPeriod implements IReadWriteObject
{

    private final ObisCode obisCode;

    public BillingPeriod(ObisCode obisCode)
    {
        this.obisCode = obisCode;
    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.REGISTER, 2);

        DlmsDataUnsigned dlmsData = new DlmsDataUnsigned((Integer)data[0]);
        layer.setAttributeAndCheckResult(descriptor, dlmsData);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.REGISTER, 2);

        DlmsData data = layer.getAttributeAndCheckResult(descriptor);
        return ((DlmsDataUnsigned)data).getValue();
    }

}
