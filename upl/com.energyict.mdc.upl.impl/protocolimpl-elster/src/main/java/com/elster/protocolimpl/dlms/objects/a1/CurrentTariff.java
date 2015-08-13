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
import com.elster.dlms.types.data.DlmsDataOctetString;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class CurrentTariff implements IReadWriteObject
{

  public CurrentTariff()
  {
  }

  public ObisCode getObisCode()
  {
    return A1ObjectPool.CurrentTariff;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
   final ObisCode obisCode = new ObisCode(0, 0, 13, 0, 0, 255);
   CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 2);
   DlmsData data = layer.getAttributeAndCheckResult(descriptor);


   String tariff = "";

   byte[] ba = ((DlmsDataOctetString)data).getValue();
   for (byte b : ba)
   {
        if ((b >= 0x20) && (b < 0x80))
        {
            tariff += (char)b;
        }
   }
   return tariff;
  }

}
