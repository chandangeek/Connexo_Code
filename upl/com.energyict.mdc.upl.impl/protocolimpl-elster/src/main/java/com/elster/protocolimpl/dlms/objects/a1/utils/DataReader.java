/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataLong64Unsigned;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class DataReader implements IReadWriteObject
{
  private final ObisCode obisCode;
  private final int classId;
  private final int attributeId;

  public DataReader(ObisCode obisCode)
  {
    this(obisCode, CosemClassIds.DATA, 2);
  }

  public DataReader(ObisCode obisCode, int classId, int attributeId)
  {
      this.obisCode = obisCode;
      this.classId =classId;
      this.attributeId = attributeId;
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
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(obisCode, classId, attributeId);

    DlmsData data = layer.getAttributeAndCheckResult(value);

    if (data.getType() == DataType.UNSIGNED)
    {
      return ((DlmsDataUnsigned)data).getValue();
    }
    if (data.getType() == DataType.VISIBLE_STRING)
    {
      return ((DlmsDataVisibleString)data).getValue();
    }
    if (data.getType() == DataType.OCTET_STRING)
    {
      return data.stringValue();
    }
    if (data.getType() == DataType.LONG_UNSIGNED)
    {
      return ((DlmsDataLongUnsigned)data).getValue();
    }
    if (data.getType() == DataType.LONG64_UNSIGNED)
    {
      return ((DlmsDataLong64Unsigned)data).getValue();
    }
    if (data.getType() == DataType.DOUBLE_LONG_UNSIGNED)
    {
      return ((DlmsDataDoubleLongUnsigned)data).getValue();
    }
    return null;
  }
}
