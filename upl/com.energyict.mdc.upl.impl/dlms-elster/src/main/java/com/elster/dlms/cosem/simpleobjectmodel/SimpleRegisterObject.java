/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleRegisterObject.java $
 * Version:     
 * $Id: SimpleRegisterObject.java 4010 2012-02-13 15:52:14Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 3:55:47 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataFloat32;
import com.elster.dlms.types.data.DlmsDataFloat64;
import com.elster.dlms.types.data.DlmsDataFloatingPoint;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataLong;
import com.elster.dlms.types.data.DlmsDataStructure;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Class for Objects of COSEM class "Register".
 *
 * @author osse
 */
public class SimpleRegisterObject extends SimpleDataObject
{
  /*package private*/
  SimpleRegisterObject(final SimpleCosemObjectDefinition definition, final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  /**
   * Return the scaler unit.
   *
   * @return The scaler unit.
   */
  public ScalerUnit getScalerUnit() throws IOException
  {
    try
    {
      final DlmsData scalerUnitData = executeGet(3, DlmsDataStructure.class, false);
      return new ScalerUnit(scalerUnitData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  /**
   * Returns {@code true} if the value can be scaled.
   *
   * @return  {@code true} if the value can be scaled.
   */
  public boolean isScalable() throws IOException
  {
    return ScalerUnit.isScalable(getValue(false));
  }

  /**
   * Return the scaled value.<P>
   * The value from "value" attribute will scaled by the scaler from the "scaler unit" attribute.
   *
   * @return The scaled value or {@code null} if the value is not scalable.
   */
  public BigDecimal getScaledValue() throws IOException
  {
    final DlmsData value = getValue();
    if (!ScalerUnit.isScalable(value))
    {
      return null;
    }
    return getScalerUnit().scale(value);
  }

  public void setDoubleValue(final double dval) throws IOException
  {
    setBigDecimalValue(new BigDecimal(dval));
  }

  public void setBigDecimalValue(BigDecimal bdval) throws IOException
  {
    if (isScalable())
    {
      bdval = bdval.scaleByPowerOfTen(-getScalerUnit().getScaler());
    }
    
    final DataType type = getValue(false).getType();

    switch (type)
    {
      case DOUBLE_LONG_UNSIGNED:
        setValue(new DlmsDataDoubleLongUnsigned(bdval.longValue()));
        break;
      case LONG:
        setValue(new DlmsDataLong(bdval.intValue()));
        break;

      case INTEGER:
        setValue(new DlmsDataInteger(bdval.intValue()));
        break;

      case FLOAT32:
        setValue(new DlmsDataFloat32(bdval.floatValue()));
        break;
      case FLOAT64:
        setValue(new DlmsDataFloat64(bdval.doubleValue()));
        break;
      case FLOATING_POINT:
        setValue(new DlmsDataFloatingPoint(bdval.floatValue()));
        break;
      default:
        throw new IllegalStateException("BigDecimal cannot applied to "+type);
    }
  }
  
  /**
   * Executes the reset method of this register object.
   * See BB ed.10 p.37
   * 
   * @throws IOException 
   */
  public void reset() throws IOException
  {
    executeMethod(1,new DlmsDataInteger(0));
  }
  
  
}