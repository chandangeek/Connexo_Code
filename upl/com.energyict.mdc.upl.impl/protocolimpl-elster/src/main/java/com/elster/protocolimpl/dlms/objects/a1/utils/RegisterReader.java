/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author heuckeg
 */
public class RegisterReader implements IReadWriteObject
{
  private final ObisCode obisCode;

  public RegisterReader(ObisCode obisCode)
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
    final CosemAttributeDescriptor value = new CosemAttributeDescriptor(obisCode, CosemClassIds.REGISTER, 2);

    DlmsData data = layer.getAttributeAndCheckResult(value);

    final CosemAttributeDescriptor scaler = new CosemAttributeDescriptor(obisCode, CosemClassIds.REGISTER, 3);

    final ScalerUnit scalerUnit;
    try
    {
      scalerUnit = new ScalerUnit(layer.getAttributeAndCheckResult(scaler));
    }
    catch (ValidationExecption ex)
    {
      String msg = "RegisterReader.read - validationException";
      if ((ex.getMessage() != null) && (ex.getMessage().length() > 0))
      {
        msg += ":" + ex.getMessage();
      }
      throw new IOException(msg);
    }

    if (!ScalerUnit.isScalable(data))
    {
      return null;
    }
    return new RegisterResult(scalerUnit.scale(data), scalerUnit.getUnit());
  }

  public static class RegisterResult
  {
    private final BigDecimal value;
    private final Unit unit;

    public RegisterResult(final BigDecimal value, final Unit unit)
    {
      this.value = value;
      this.unit = unit;
    }

    public BigDecimal getValue()
    {
      return value;
    }

    public Unit getUnit()
    {
      return unit;
    }

    @Override
    public String toString()
    {
      return value.toPlainString() + " " + unit.getDisplayName();
    }

  }

}
