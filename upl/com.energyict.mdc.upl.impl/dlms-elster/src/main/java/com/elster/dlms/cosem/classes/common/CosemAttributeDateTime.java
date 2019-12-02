/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/CosemAttributeDateTime.java $
 * Version:     
 * $Id: CosemAttributeDateTime.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 10, 2010 9:13:09 AM
 */
package com.elster.dlms.cosem.classes.common;

import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;

/**

 *
 * @author osse
 */
public class CosemAttributeDateTime extends TypedCosemAttribute<DlmsDateTime>
{
  public CosemAttributeDateTime(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                  CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(DlmsDateTime value)
  {
    return new DlmsDataOctetString(value.toBytes());
  }

  @Override
  protected DlmsDateTime dlmsData2Value(DlmsData data)
  {
     return new  DlmsDateTime(((DlmsDataOctetString)data).getValue());
  }

}
