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
import com.elster.dlms.types.data.DlmsDataBoolean;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class DstEnabled implements IReadWriteObject
{
  private static final ObisCode obisCode = A1ObjectPool.DstEnabled;

  public DstEnabled()
  {

  }

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(A1ObjectPool.Clock, CosemClassIds.CLOCK, 8);
    DlmsData data = layer.getAttributeAndCheckResult(value);
    return ((DlmsDataBoolean)data).getValue();
  }
}
