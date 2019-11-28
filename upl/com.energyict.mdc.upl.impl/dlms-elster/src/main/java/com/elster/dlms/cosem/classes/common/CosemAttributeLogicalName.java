/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/CosemAttributeLogicalName.java $
 * Version:     
 * $Id: CosemAttributeLogicalName.java 2583 2011-01-26 16:16:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 10, 2010 9:13:09 AM
 */
package com.elster.dlms.cosem.classes.common;

import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;

/**
 * COSEM attribute logical name (ObisCode).<P>
 * (Attribute 1 of all COSEM classes)
 *
 * @author osse
 */
public class CosemAttributeLogicalName extends TypedCosemAttribute<ObisCode>
{
  public CosemAttributeLogicalName(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                  CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(ObisCode value)
  {
    return new DlmsDataOctetString(value.toByteArray());
  }

  @Override
  protected ObisCode dlmsData2Value(DlmsData data)
  {
    return new ObisCode(((DlmsDataOctetString)data).getValue());
  }

}
