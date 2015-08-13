/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class11.SpecialDayEntry;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class SpecialDaysTable implements IReadWriteObject
{
  private static final ObisCode obisCode = new ObisCode("0.0.11.0.0.255");

  public SpecialDaysTable()
  {

  }

  public ObisCode getObisCode()
  {
    return obisCode;
  }

  private final static int MAXATONCE = 10;

  public void write(CosemApplicationLayer layer, Object param[]) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode,
                                                                                  CosemClassIds.SPECIAL_DAYS_TABLE,
                                                                                  2);
    // clear list in device
    SpecialDayEntry list[] = new SpecialDayEntry[]
    {
    };
    DlmsData data = new DlmsDataArray(list);
    layer.setAttributeAndCheckResult(valueDescriptor, data);

    if (param.length == 0)
    {
      return;
    }


    // first 7 entries can be set directly
    int cEntriesToProcess = param.length;
    int cDirect = cEntriesToProcess < MAXATONCE ? cEntriesToProcess : MAXATONCE;
    list = new SpecialDayEntry[cDirect];
    System.arraycopy(param, 0, list, 0, cDirect);
    data = new DlmsDataArray(list);
    layer.setAttributeAndCheckResult(valueDescriptor, data);

    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(obisCode,
                                                                             CosemClassIds.SPECIAL_DAYS_TABLE,
                                                                             1);
    // the rest of the entries are added via method "insert"
    for (int i = MAXATONCE; i < param.length; i++)
    {
      DlmsData edata = ((SpecialDayEntry)param[i]).toDlmsData();
      layer.executeActionAndCheckResponse(methodDescriptor, edata);
    }
  }

  public Object read(CosemApplicationLayer layer) throws IOException
  {
    final CosemAttributeDescriptor valueDescriptor = new CosemAttributeDescriptor(obisCode,
                                                                                  CosemClassIds.SPECIAL_DAYS_TABLE,
                                                                                  2);
    final DlmsData dlmsData = layer.getAttributeAndCheckResult(valueDescriptor);
    try
    {
      return SpecialDayEntry.fromDlmsDataArray(dlmsData);
    }
    catch (ValidationExecption ex)
    {
      throw new IOException(ex.getMessage());
    }
  }
}
