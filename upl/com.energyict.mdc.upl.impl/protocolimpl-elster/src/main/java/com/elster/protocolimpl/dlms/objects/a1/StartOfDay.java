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
public class StartOfDay implements IReadWriteObject
{
  public StartOfDay()
  {
  }

  private static final ObisCode obisCode = A1ObjectPool.BOD;

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode,
                                                                        CosemClassIds.DATA, 2);
    byte[] bytes = new byte[4];
    bytes[0] = (byte)(((Integer)data[0]) & 0xFF);
    bytes[1] = (byte)(((Integer)data[1]) & 0xFF);
    bytes[2] = (byte)(((Integer)data[2]) & 0xFF);
    bytes[3] = 0;

    DlmsData dlmsData = new DlmsDataOctetString(bytes);

    layer.setAttributeAndCheckResult(descriptor, dlmsData);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(obisCode,
                                                                        CosemClassIds.DATA, 2);

    DlmsData data = layer.getAttributeAndCheckResult(value);

    byte b[] = ((DlmsDataOctetString)data).getValue();

    return String.format("%02d:%02d:%02d", b[0], b[1], b[2]);
  }

}
