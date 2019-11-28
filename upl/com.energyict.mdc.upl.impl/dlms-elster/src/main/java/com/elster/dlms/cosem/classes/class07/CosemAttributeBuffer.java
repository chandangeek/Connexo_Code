/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/CosemAttributeBuffer.java $
 * Version:     
 * $Id: CosemAttributeBuffer.java 2628 2011-02-03 18:57:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 3:58:26 PM
 */
package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * Attribute 2 for class id 7 (Profile Generic)
 *
 * @author osse
 */
public class CosemAttributeBuffer extends TypedCosemAttribute<DlmsDataArray>
{
  public CosemAttributeBuffer(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                      CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(DlmsDataArray value)
  {
    return value;
  }

  @Override
  protected DlmsDataArray dlmsData2Value(DlmsData data)
  {
    return (DlmsDataArray) data;
  }




}
