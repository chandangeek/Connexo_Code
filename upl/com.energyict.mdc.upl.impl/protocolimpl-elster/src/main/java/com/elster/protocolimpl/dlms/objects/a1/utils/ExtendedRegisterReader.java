/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author heuckeg
 */
public class ExtendedRegisterReader implements IReadWriteObject
{
  private final ObisCode obisCode;

  public ExtendedRegisterReader(ObisCode obisCode)
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public static class ExtendedRegisterResult
  {
    private final BigDecimal value;
    private final Unit unit;
    private final Date date;

    public ExtendedRegisterResult(final BigDecimal value, final Unit unit, final Date date)
    {
      this.value = value;
      this.unit = unit;
      this.date = date;
    }

    public BigDecimal getValue()
    {
      return value;
    }

    public Unit getUnit()
    {
      return unit;
    }

    public Date getDate()
    {
      return date;
    }

    @Override
    public String toString()
    {
      return value.toPlainString() + " " + unit.getDisplayName() + " at " + date.toString();
    }
  }


}
