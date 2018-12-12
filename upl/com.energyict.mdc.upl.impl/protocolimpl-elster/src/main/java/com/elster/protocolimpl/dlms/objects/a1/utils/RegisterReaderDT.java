/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class RegisterReaderDT implements IReadWriteObject
{
  private final ObisCode obisCode;

  public RegisterReaderDT(ObisCode obisCode)
  {
    this.obisCode = obisCode;
  }

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
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(obisCode, CosemClassIds.REGISTER, 2);

    DlmsData data = layer.getAttributeAndCheckResult(value);

    DlmsDateTime dt = new DlmsDateTime(((DlmsDataOctetString)data).getValue());
    return dt.getLocalDate();
  }

}
