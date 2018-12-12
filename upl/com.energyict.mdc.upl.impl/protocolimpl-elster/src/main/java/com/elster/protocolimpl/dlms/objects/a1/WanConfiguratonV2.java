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
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author heuckeg
 */
class WanConfiguratonV2 implements IReadWriteObject
{
  public WanConfiguratonV2()
  {
  }

  private static final ObisCode obisCode = new ObisCode(0, 0, 2, 1, 0, 255);

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    CosemAttributeDescriptor attributeDescriptor;

    DlmsDataOctetString dest2 = new DlmsDataOctetString("".getBytes());
    DlmsDataOctetString dest3 = new DlmsDataOctetString("".getBytes());

    long flags = 0x08;
    DestinationData destData1 = new DestinationData((String)param[0]);
    DlmsDataOctetString dest1 = new DlmsDataOctetString(destData1.toString().getBytes());

    if (param.length > 1)
    {
      DestinationData destData2 = new DestinationData((String)param[1]);
      if (!destData2.isEmpty())
      {
        flags |= (0x04 | 0x10);
        dest2 = new DlmsDataOctetString(destData2.toString().getBytes());
      }
    }
    if (param.length > 2)
    {
      DestinationData destData3 = new DestinationData((String)param[2]);
      if (!destData3.isEmpty())
      {
         dest3 = new DlmsDataOctetString(destData3.toString().getBytes());
      }
    }

    DlmsDataArray dests = new DlmsDataArray(dest1, dest2, dest3);

    attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 6);
    layer.setAttributeAndCheckResult(attributeDescriptor, dests);

    attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 35);

    DlmsDataDoubleLongUnsigned controlFlags = (DlmsDataDoubleLongUnsigned)layer.getAttributeAndCheckResult(
            attributeDescriptor);
    flags |= (controlFlags.getValue() & (0xFF ^ 0x1C));
    controlFlags = new DlmsDataDoubleLongUnsigned(flags);
    layer.setAttributeAndCheckResult(attributeDescriptor, controlFlags);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor descriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 6);
    DlmsData data = layer.getAttributeAndCheckResult(descriptor);
    DlmsDataArray dests = (DlmsDataArray)data;

    String result = "";
    for(Iterator<DlmsData> i = dests.iterator(); i.hasNext(); )
    {
      DlmsData dd = i.next();
      if (result.length() > 0)
      {
        result += " - ";
      }
      result += new String(((DlmsDataOctetString)dd).getValue());
    }
    return result;
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

    @Override
    public String toString()
    {
      return ip + ":" + port;
    }
  }

}
