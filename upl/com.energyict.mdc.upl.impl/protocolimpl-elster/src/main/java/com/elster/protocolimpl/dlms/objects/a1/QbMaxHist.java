/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.objects.a1.utils.DailyProfileReader;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
class QbMaxHist implements IReadWriteObject
{
  private final ObisCode obisCode;

  public QbMaxHist(final int hist)
  {
    obisCode = A1ObjectPool.DlyQbMaxHist.derive(5, hist);
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
    int fValue = obisCode.getF();
    int daysBack = fValue - 101;

    return DailyProfileReader.getInstance().getDailyProfileEntry(layer, daysBack);
  }

}
