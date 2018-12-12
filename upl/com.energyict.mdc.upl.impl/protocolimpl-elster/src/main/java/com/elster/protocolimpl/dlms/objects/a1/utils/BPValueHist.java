/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.elster.protocolimpl.dlms.util.A1Defs;

import java.io.IOException;
import java.util.Date;

/**
 * @author heuckeg
 */
public class BPValueHist implements IReadWriteObject
{
    private final ObisCode obisCode;
    private final int histCode;
    private BillingProfileReader billingProfileReader = null;

    public BPValueHist(ObisCode obisCode, int i)
    {
        this.obisCode = obisCode;
        this.histCode = i;
    }

    public ObisCode getObisCode()
    {
        return obisCode.derive(5, histCode);
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        if (billingProfileReader == null)
        {
            throw new IOException("Billing profile reader not set!");
        }

        Object o = billingProfileReader.getValue(obisCode, histCode);
        if (o == null)
        {
            throw new IOException("no value");
        }
        Date tst1 = (Date) billingProfileReader.getValue(A1Defs.CLOCK_OBJECT, histCode);
        Date tst2 = (Date) billingProfileReader.getValue(A1Defs.CLOCK_OBJECT, histCode + 1);

        return new HistoricRegisterResult(new Date(), tst2, tst1, o);
    }

    public void setBillingProfileReader(BillingProfileReader billingProfileReader)
    {
        this.billingProfileReader = billingProfileReader;
    }
}
