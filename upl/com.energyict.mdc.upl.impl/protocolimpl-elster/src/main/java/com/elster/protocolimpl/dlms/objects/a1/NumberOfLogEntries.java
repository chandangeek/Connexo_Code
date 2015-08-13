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
class NumberOfLogEntries implements IReadWriteObject
{
  private final ObisCode logOC;
  private final ObisCode ownOC;

  public NumberOfLogEntries(final ObisCode logOC, final ObisCode ownOC)
  {
    this.logOC = logOC;
    this.ownOC = ownOC;
  }

  public ObisCode getObisCode()
  {
     return ownOC;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(logOC, CosemClassIds.PROFILE_GENERIC, 7);
    DlmsData data = layer.getAttributeAndCheckResult(value);
    return ((DlmsDataDoubleLongUnsigned)data).getValue();
  }

}
