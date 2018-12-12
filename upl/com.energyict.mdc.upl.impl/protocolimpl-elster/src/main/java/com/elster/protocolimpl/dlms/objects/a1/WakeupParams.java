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
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataDateTime;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author heuckeg
 */
class WakeupParams implements IReadWriteObject
{

  private static final ObisCode obisCode = new ObisCode(0, 0, 2, 1, 0, 255);

  public WakeupParams()
  {
  }

  public ObisCode getObisCode()
  {
    return A1ObjectPool.WakeUpParams;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 3);
    DlmsData mode = layer.getAttributeAndCheckResult(descriptor);

    descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 5);
    DlmsData cw = layer.getAttributeAndCheckResult(descriptor);

    String result = "" + ((DlmsDataUnsigned)mode).getValue();

    DlmsDataArray cwArray = (DlmsDataArray)cw;
    for (Iterator<DlmsData> windowElement = cwArray.iterator(); windowElement.hasNext() ;)
    {
      DlmsData dd = windowElement.next();
      DlmsDataStructure weStructure = (DlmsDataStructure)dd;
      for (Iterator<DlmsData> time = weStructure.iterator(); time.hasNext() ;)
      {
        DlmsData timedata = time.next();
        DlmsDataDateTime dt = new DlmsDataDateTime(((DlmsDataOctetString)timedata).getValue());
        result += (" - " + dt.stringValue());
      }
    }

    return result;
  }

}
