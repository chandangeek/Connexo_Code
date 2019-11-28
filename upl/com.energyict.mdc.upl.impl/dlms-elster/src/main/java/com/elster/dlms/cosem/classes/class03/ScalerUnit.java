/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class03/ScalerUnit.java $
 * Version:     
 * $Id: ScalerUnit.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 8, 2010 1:31:03 PM
 */
package com.elster.dlms.cosem.classes.class03;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.*;
import java.math.BigDecimal;

/**
 * DLMS/COSEM Scaler unit <P>
 * See BB ed.10 ch.4.3.2 p.37
 *
 * @author osse
 */
public class ScalerUnit
{
  private final int scaler;
  private final Unit unit;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.INTEGER),
          new ValidatorSimpleType(DataType.ENUM));

  public ScalerUnit(final int scaler, final Unit unit)
  {
    this.scaler = scaler;
    this.unit = unit;
  }

  public ScalerUnit(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    this.scaler = ((DlmsDataInteger)structure.get(0)).getValue();
    final int enumId = ((DlmsDataEnum)structure.get(1)).getValue();
    this.unit = Unit.findById(enumId);
  }

  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataInteger(scaler),
            new DlmsDataEnum(unit.getId()));
  }

  public int getScaler()
  {
    return scaler;
  }

  public Unit getUnit()
  {
    return unit;
  }

  /**
   * Returns {@code true} if the value can be scaled.
   *
   * @param data The DLMS data element which should be scaled.
   * @return  {@code true} if the value can be scaled.
   */
  public static boolean isScalable(final DlmsData data)
  {
    return data instanceof AbstractDlmsDataNumber;
  }

  /**
   * Scales the specified value.<P>
   *
   * @return The scaled value or {@code null} if the value is not scalable.
   */
  public BigDecimal scale(final DlmsData value)
  {
    if (!(value instanceof AbstractDlmsDataNumber))
    {
      return null;
    }
    final AbstractDlmsDataNumber dlmsDataNumber = (AbstractDlmsDataNumber)value;
    return dlmsDataNumber.bigDecimalValue().scaleByPowerOfTen(scaler);
  }

  @Override
  public String toString()
  {
    return "ScalerUnit{" + "scaler=" + scaler + ", unit=" + unit + '}';
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final ScalerUnit other = (ScalerUnit)obj;
    if (this.scaler != other.scaler)
    {
      return false;
    }
    if (this.unit != other.unit)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 89 * hash + this.scaler;
    hash = 89 * hash + (this.unit != null ? this.unit.hashCode() : 0);
    return hash;
  }

}
