/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class ConfigureCyclicModeV1 implements IReadWriteObject
{

  public ConfigureCyclicModeV1()
  {
  }

  private static final ObisCode obisCode = new ObisCode(0, 128, 96, 194, 101, 255);

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  public void write(CosemApplicationLayer layer, Object[] data) throws IOException
  {
        CosemAttributeDescriptor attributeDescriptor;

        long distance = (Long)data[0];
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 11);
        layer.setAttribute(attributeDescriptor, new DlmsDataDoubleLongUnsigned(distance));

        attributeDescriptor = new CosemAttributeDescriptor(obisCode, 9138, 17);
        DlmsDataDoubleLongUnsigned controlFlags = (DlmsDataDoubleLongUnsigned) layer.getAttributeAndCheckResult(attributeDescriptor);
        long flags = (controlFlags.getValue() & (0xFF ^ 0x3)) | 0x01;
        controlFlags = new DlmsDataDoubleLongUnsigned(flags);
        layer.setAttributeAndCheckResult(attributeDescriptor, controlFlags);
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
