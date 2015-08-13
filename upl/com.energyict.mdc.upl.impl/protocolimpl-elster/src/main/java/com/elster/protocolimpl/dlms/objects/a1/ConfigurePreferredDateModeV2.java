/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class ConfigurePreferredDateModeV2 implements IReadWriteObject
{
  public ConfigurePreferredDateModeV2()
  {
  }

  private static final ObisCode obisCode = new ObisCode(0, 0, 2, 1, 0, 255);

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
    CosemAttributeDescriptor attributeDescriptor;

    long distance = (Long)data[0];
    attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 10);
    layer.setAttribute(attributeDescriptor, new DlmsDataDoubleLongUnsigned(distance));

        attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.AUTO_CONNECT, 35);
    DlmsDataDoubleLongUnsigned controlFlags = (DlmsDataDoubleLongUnsigned)layer.getAttributeAndCheckResult(
            attributeDescriptor);
    long flags = (controlFlags.getValue() & (0xFF ^ 0x3)) | 0x02;
    controlFlags = new DlmsDataDoubleLongUnsigned(flags);
    layer.setAttributeAndCheckResult(attributeDescriptor, controlFlags);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
