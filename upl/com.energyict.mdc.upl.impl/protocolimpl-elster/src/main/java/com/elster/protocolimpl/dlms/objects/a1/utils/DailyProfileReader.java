/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class07.EntryDescriptor;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataDateTime;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.protocolimpl.dlms.objects.a1.A1ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.utils.ExtendedRegisterReader.ExtendedRegisterResult;
import com.elster.protocolimpl.dlms.util.A1Defs;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author heuckeg
 */
public class DailyProfileReader
{
  private static DailyProfileReader dpReader = null;
  private static ScalerUnit scalerUnit = null;

  private DailyProfileReader()
  {
  }

  public static DailyProfileReader getInstance()
  {
    if (dpReader == null)
    {
      dpReader = new DailyProfileReader();
    }
    return dpReader;
  }

  private ScalerUnit getScalerUnit(CosemApplicationLayer layer) throws IOException
  {
    if (scalerUnit == null)
    {
      final CosemAttributeDescriptor scaler = new CosemAttributeDescriptor(A1ObjectPool.DlyQbMaxHist,
                                                                           CosemClassIds.EXTENDED_REGISTER, 3);
      try
      {
        scalerUnit = new ScalerUnit(layer.getAttributeAndCheckResult(scaler));
      }
      catch (ValidationExecption ex)
      {
        throw new IOException(ex.getMessage());
      }
    }
    return scalerUnit;
  }

  private static final int msecPerDay = 24 * 60 * 60 * 1000;

  public Object getDailyProfileEntry(CosemApplicationLayer layer, int daysBack) throws IOException
  {
    SimpleCosemObjectManager objectManager = new SimpleCosemObjectManager(layer, A1Defs.DEFINITIONS);
    SimpleProfileObject profile = (SimpleProfileObject)objectManager.getSimpleCosemObject(
            A1ObjectPool.DailyProfile);

    if (profile.getEntriesInUse() <= daysBack)
    {
      throw new IOException("no value");
    }

    long lineToRead = profile.getEntriesInUse() - 1 - daysBack;
    EntryDescriptor entryDescriptor = new EntryDescriptor(lineToRead, lineToRead, 1, profile.
            getColumnCount());

    if (profile.readProfileData(entryDescriptor, false, true) == 0)
    {
      throw new IOException("no value");
    }

    Date readDate = new Date();
    Date fromDate = new Date();

    DlmsData tst = profile.getRawValue(0, 0);
    DlmsData val = profile.getRawValue(0, 2);
    DlmsData vdt = profile.getRawValue(0, 3);

    BigDecimal scaledVal = getScalerUnit(layer).scale(val);
    DlmsDataDateTime eventTimeRaw = new DlmsDataDateTime(((DlmsDataOctetString)vdt).getValue());
    DlmsDateTime eventTimeRead = eventTimeRaw.getValue();
    ExtendedRegisterResult registerResult = new ExtendedRegisterResult(scaledVal, getScalerUnit(layer).
            getUnit(), eventTimeRead.getLocalDate());

    DlmsDataDateTime toTimeRaw = new DlmsDataDateTime(((DlmsDataOctetString)tst).getValue());
    DlmsDateTime toTimeRead = toTimeRaw.getValue();
    fromDate.setTime(toTimeRead.getLocalDate().getTime() - msecPerDay);

    return new HistoricRegisterResult(readDate, fromDate, toTimeRead.getLocalDate(), registerResult);
  }
}
