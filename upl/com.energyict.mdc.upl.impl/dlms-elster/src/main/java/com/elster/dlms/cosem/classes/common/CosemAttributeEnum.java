/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/CosemAttributeEnum.java $
 * Version:     
 * $Id: CosemAttributeEnum.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 10, 2010 9:13:09 AM
 */
package com.elster.dlms.cosem.classes.common;

import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataEnum;

/**
 *
 *
 * @author osse
 */
public class CosemAttributeEnum<T extends ICosemEnum> extends TypedCosemAttribute<T>
{
  private final CosemEnumFactory<T> enumFactory;

  public CosemAttributeEnum(final CosemObject parent,final int attributeId,final  AttributeAccessMode accessMode,
                            final CosemAttributeInfo attributeInfo,final int[] accessSelectors ,final CosemEnumFactory<T> enumFactory)
  {
    super(parent, attributeId, accessMode, attributeInfo,accessSelectors);
    this.enumFactory = enumFactory;
  }

  @Override
  protected DlmsData value2DlmsData(final T value)
  {
    return new DlmsDataEnum(value.getId());
  }

  @Override
  protected T dlmsData2Value(final DlmsData data)
  {
    if (data.getType() != DlmsData.DataType.ENUM)
    {
      throw new IllegalArgumentException("Dlms data type must be \"enum\"");
    }
    return enumFactory.findValue(((DlmsDataEnum)data).getValue());
  }

}
