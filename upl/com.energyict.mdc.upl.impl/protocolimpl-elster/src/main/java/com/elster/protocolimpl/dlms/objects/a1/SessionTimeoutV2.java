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
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class SessionTimeoutV2 implements IReadWriteObject
{

  public SessionTimeoutV2()
  {
  }

  private static final ObisCode obisCode = new ObisCode("0.0.2.1.0.255");

  public ObisCode getObisCode()
  {
    return A1ObjectPool.ModemTimeout;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 31);
    DlmsDataDoubleLongUnsigned timeout = new DlmsDataDoubleLongUnsigned((Long)param[0]);
    layer.setAttributeAndCheckResult(valueDescriptor, timeout);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 31);
    DlmsData data = layer.getAttributeAndCheckResult(valueDescriptor);
    Long timeout = ((DlmsDataDoubleLongUnsigned)data).getValue();
    return timeout;
  }
}
