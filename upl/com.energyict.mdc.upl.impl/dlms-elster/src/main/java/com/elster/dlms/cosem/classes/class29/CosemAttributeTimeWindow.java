/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class29/CosemAttributeTimeWindow.java $
 * Version:     
 * $Id: CosemAttributeTimeWindow.java 3583 2011-09-28 15:44:22Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class29;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.common.TimeWindow;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * COSEM attribute for an time window
 *
 * @author osse
 */
public class CosemAttributeTimeWindow extends TypedCosemAttribute<TimeWindow[]>
{
  public CosemAttributeTimeWindow(final CosemObject parent, final int attributeId,
                                        final AttributeAccessMode accessMode,
                                        final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(final TimeWindow[] value)
  {
    
    return new DlmsDataArray(value);
  }

  @Override
  protected TimeWindow[] dlmsData2Value(final DlmsData data) throws ValidationExecption
  {
    return TimeWindow.fromDlmsDataArray(data);
  }

}
