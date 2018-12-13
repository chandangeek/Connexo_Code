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
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author heuckeg
 */
class OnDemandSnapshotTime implements IReadWriteObject
{
  private final ObisCode obisCode = A1ObjectPool.OnDemandSnapshotTime;

  public OnDemandSnapshotTime()
  {
  }

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    DlmsDateTime snapshotDate = new DlmsDateTime((Date)data[0]);
    DlmsDataOctetString date = new DlmsDataOctetString(snapshotDate.toBytes());

    DlmsDataUnsigned reason = new DlmsDataUnsigned((Integer)data[1]);
    
    DlmsDataStructure struct = new DlmsDataStructure(date, reason);

    CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);

    layer.setAttributeAndCheckResult(attributeDescriptor, struct);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.DATA, 2);
    DlmsData data = layer.getAttributeAndCheckResult(attributeDescriptor);

    DlmsData se0 = ((DlmsDataStructure)data).get(0);
    DlmsDateTime date = new DlmsDateTime(((DlmsDataOctetString)se0).getValue());
    String result = date.stringValue();

    DlmsData se1 = ((DlmsDataStructure)data).get(1);
    result += " (" + se1.stringValue() + ")";

    return result;
  }

}
