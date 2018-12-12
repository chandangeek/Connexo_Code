/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author heuckeg
 */
public class Clock implements IReadWriteObject
{
  private static final ObisCode obisCode = new ObisCode("0.0.1.0.0.255");

  public Clock()
  {

  }

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.CLOCK, 2);
    DlmsDateTime dateTime = new DlmsDateTime((Date)param[0]);
    DlmsData data = new DlmsDataOctetString(dateTime.toBytes());
    layer.setAttributeAndCheckResult(valueDescriptor, data);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
