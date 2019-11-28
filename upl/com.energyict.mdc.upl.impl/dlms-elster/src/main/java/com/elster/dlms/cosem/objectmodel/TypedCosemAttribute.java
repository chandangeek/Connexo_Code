/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/TypedCosemAttribute.java $
 * Version:     
 * $Id: TypedCosemAttribute.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:24:29 AM
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsData;

/**
 * Base class for specialized COSEM attributes.
 *
 * @author osse
 */
public abstract class TypedCosemAttribute<ValueType> extends CosemAttribute
{
  public TypedCosemAttribute(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                             CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  /**
   * Returns the "specialized" value for this attribute.
   *
   * @return The "specialized" value or null if value is not set.
   * @throws com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption
   */
  public final ValueType getValue() throws ValidationExecption
  {
    DlmsData data = getData();

    if (data == null)
    {
      return null;
    }
    else
    {
      validate(data);
      return dlmsData2Value(data);
    }
  }

  /**
   * Sets the "specialized" value for this attribute.
   *
   * @param value The "specialized" value
   */
  public final void setValue(ValueType value)
  {
    if (value == null)
    {
      setData(null);
    }
    else
    {
      setData(value2DlmsData(value));
    }
  }

  /**
   * Called to convert the value to DlmsData.<P>
   *
   * @param value The value to be converted. (Never null)
   * @return The DlmsData
   */
  protected abstract DlmsData value2DlmsData(ValueType value);

  /**
   * Called to convert DlmsData to the ValueType<P>
   * The data will be validated before and never be null.
   *
   * @param data Validated data (not null) to be converted
   * @return The converted value.
   */
  protected abstract ValueType dlmsData2Value(DlmsData data) throws ValidationExecption;

}
