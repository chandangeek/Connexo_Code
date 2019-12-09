/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class06/CosemAttributeRegisterMaskList.java $
 * Version:     
 * $Id: CosemAttributeRegisterMaskList.java 3040 2011-06-06 16:53:17Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class06;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * COSEM Attribute for register mask list of the Register Activation class.
 *
 * @author osse
 */
public class CosemAttributeRegisterMaskList extends TypedCosemAttribute<RegisterActivationMask[]>
{
  public CosemAttributeRegisterMaskList(final CosemObject parent, final int attributeId,
                                        final AttributeAccessMode accessMode,
                                        final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(final RegisterActivationMask[] value)
  {
    DlmsData data[] = new DlmsData[value.length];

    for (int i = 0; i < value.length; i++)
    {
      data[i] = value[i].toDlmsData();
    }
    return new DlmsDataArray(data);
  }

  @Override
  protected RegisterActivationMask[] dlmsData2Value(final DlmsData data) throws ValidationExecption
  {
    final DlmsDataArray array = (DlmsDataArray)data;
    final RegisterActivationMask[] result = new RegisterActivationMask[array.size()];

    for (int i = 0; i < result.length; i++)
    {
      result[i] = new RegisterActivationMask(array.get(i));
    }
    return result;
  }

}
