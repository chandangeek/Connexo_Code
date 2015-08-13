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
public class MeteringPointId implements IReadWriteObject
{
  private static final ObisCode obisCode = A1ObjectPool.MPI;

  public MeteringPointId()
  {

  }

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);
    String mpi = (String)param[0];
    DlmsData data = new DlmsDataOctetString(mpi.getBytes());
    layer.setAttributeAndCheckResult(valueDescriptor, data);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);
    DlmsData data = layer.getAttributeAndCheckResult(descriptor);
    String value = new String(((DlmsDataOctetString)data).getValue());
    while (value.charAt(0) < 32)
    {
      value = value.substring(1);
    }
    return value;
  }
}
