/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/CosemAttributeSortObject.java $
 * Version:     
 * $Id: CosemAttributeSortObject.java 3040 2011-06-06 16:53:17Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 3:58:26 PM
 */
package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;

/**
 * Attribute 6 for class id 7 (Profile Generic)
 *
 * @author osse
 */
public class CosemAttributeSortObject extends TypedCosemAttribute<CaptureObjectDefinition>
{
  public CosemAttributeSortObject(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                  CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(CaptureObjectDefinition value)
  {
    return value.toDlmsData();
  }

  @Override
  protected CaptureObjectDefinition dlmsData2Value(DlmsData data) throws ValidationExecption
  {
    return new CaptureObjectDefinition(data);
  }

}
