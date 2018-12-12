/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  23.04.2014 10:10:51
 */

package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;

import java.io.IOException;

/**
 * This class ...
 *
 * @author heuckeg
 */
class BillingPeriodStart implements IReadWriteObject
{
    private final ObisCode obisCode;

    public BillingPeriodStart(ObisCode obisCode)
    {
        this.obisCode = obisCode;
    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);

        DlmsDate date = new DlmsDate((Integer)data[0], (Integer)data[1], (Integer)data[2], (Integer)data[3]);
        DlmsDataOctetString dateData = new DlmsDataOctetString(date.toBytes());
        layer.setAttributeAndCheckResult(descriptor, dateData);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);

        DlmsData data = layer.getAttributeAndCheckResult(descriptor);
        DlmsDate date = new DlmsDate(((DlmsDataOctetString)data).getValue());
        return date.stringValue();
    }

}
