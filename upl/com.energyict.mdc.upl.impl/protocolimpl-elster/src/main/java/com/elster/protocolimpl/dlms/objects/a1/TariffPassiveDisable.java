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

/**
 *
 * @author heuckeg
 */
class TariffPassiveDisable implements IReadWriteObject
{

  public TariffPassiveDisable()
  {
  }

  private static final ObisCode obisCode = new ObisCode("0.0.13.0.0.255");

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object[] param) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 10);
    DlmsDateTime dateTime = DlmsDateTime.NOT_SPECIFIED_DATE_TIME;
    DlmsData data = new DlmsDataOctetString(dateTime.toBytes());
    layer.setAttributeAndCheckResult(valueDescriptor, data);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
