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
import com.elster.dlms.types.data.DlmsDataStructure;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class SynchronizationRegister implements IReadWriteObject
{

  public SynchronizationRegister()
  {
  }

  private static final ObisCode obisCode = A1ObjectPool.SyncReg;

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);

    DlmsData data = layer.getAttributeAndCheckResult(value);

    DlmsDataStructure struct = (DlmsDataStructure)data;
    DlmsData[] member = struct.getValue();

    String result = "";
    for (DlmsData dd: member)
    {
      if (result.length() > 0)
      {
        result += ":";
      }
      result += dd.stringValue();
    }
    return result;
  }

}
