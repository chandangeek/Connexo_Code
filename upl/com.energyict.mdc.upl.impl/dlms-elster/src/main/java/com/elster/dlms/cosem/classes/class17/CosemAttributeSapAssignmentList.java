/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class17/CosemAttributeSapAssignmentList.java $
 * Version:     
 * $Id: CosemAttributeSapAssignmentList.java 3180 2011-07-08 13:10:54Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class17;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * COSEM attribute for the entries of the special days table.
 *
 * @author osse
 */
public class CosemAttributeSapAssignmentList extends TypedCosemAttribute<SapAssignment[]>
{
  public CosemAttributeSapAssignmentList(final CosemObject parent, final int attributeId,
                                         final AttributeAccessMode accessMode,
                                         final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(final SapAssignment[] values)
  {
    DlmsData data[] = new DlmsData[values.length];

    for (int i = 0; i < values.length; i++)
    {
      data[i] = values[i].toDlmsData();
    }

    return new DlmsDataArray(data);
  }

  @Override
  protected SapAssignment[] dlmsData2Value(final DlmsData data) throws ValidationExecption
  {
    return SapAssignment.fromDlmsDataArray(data);
  }

}
