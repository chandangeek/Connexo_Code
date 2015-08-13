/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataVisibleString;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class WanConfiguratonV1 implements IReadWriteObject
{
  public WanConfiguratonV1()
  {
  }

  private static final ObisCode obisCode = new ObisCode(0, 128, 96, 194, 101, 255);

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    CosemAttributeDescriptor attributeDescriptor;

    long flags = 0x08;
    DestinationData destData1 = new DestinationData((String)param[0]);
    attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 2);
    layer.setAttribute(attributeDescriptor, new DlmsDataVisibleString(destData1.getIp()));
    attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 3);
    layer.setAttribute(attributeDescriptor, new DlmsDataLongUnsigned(destData1.getPort()));

    DestinationData destData2 = new DestinationData((String)param[1]);
    attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 4);
    layer.setAttribute(attributeDescriptor, new DlmsDataVisibleString(destData2.getIp()));
    attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 5);
    layer.setAttribute(attributeDescriptor, new DlmsDataLongUnsigned(destData2.getPort()));

    if (!destData2.isEmpty())
    {
      flags |= (0x04 | 0x10);
    }
    attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 17);
    DlmsDataDoubleLongUnsigned controlFlags = (DlmsDataDoubleLongUnsigned)layer.getAttributeAndCheckResult(
            attributeDescriptor);
    flags |= (controlFlags.getValue() & (0xFF ^ 0x1C));
    controlFlags = new DlmsDataDoubleLongUnsigned(flags);
    layer.setAttributeAndCheckResult(attributeDescriptor, controlFlags);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private class DestinationData
  {
    private final String ip;
    private final int port;

    public DestinationData(final String data)
    {
      if ((data != null) && (data.length() > 0))
      {
        String[] s = data.split(":");
        ip = s[0];
        port = Integer.parseInt(s[1]);
      }
      else
      {
        ip = "";
        port = 0;
      }
    }

    public boolean isEmpty()
    {
      return ip.length() == 0;
    }

    public String getIp()
    {
      return ip;
    }

    public int getPort()
    {
      return port;
    }

  }

}
