/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataVisibleString;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class GprsConfigurationV2 implements IReadWriteObject
{
  private static final ObisCode obisCode = new ObisCode(0, 0, 25, 4, 0, 255);

  public GprsConfigurationV2()
  {
  }

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    CosemAttributeDescriptor descriptor;
    DataAccessResult result;

    descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.GPRS_MODEM_SETUP, 2);
    result = layer.setAttribute(descriptor, new DlmsDataOctetString(((String)param[0]).getBytes()));
    if (result != DataAccessResult.SUCCESS)
    {
      throw new IOException(String.format("APN setup message: Error when writing APN (%s)", result.getName()));
    }

    descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.GPRS_MODEM_SETUP, 30);
    result = layer.setAttribute(descriptor, new DlmsDataVisibleString((String)param[1]));
    if (result != DataAccessResult.SUCCESS)
    {
      throw new IOException(String.format("APN setup message: Error when writing user (%s)", result.getName()));
    }

    descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.GPRS_MODEM_SETUP, 31);
    result = layer.setAttribute(descriptor, new DlmsDataVisibleString((String)param[2]));
    if (result != DataAccessResult.SUCCESS)
    {
      throw new IOException(String.format("APN setup message: Error when writing password (%s)", result.
              getName()));
    }
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
