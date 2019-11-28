/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class03/CosemAttributeScalerUnit.java $
 * Version:     
 * $Id: CosemAttributeScalerUnit.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class03;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;

/**
 * CosemAttribute for the Scaler unit
 *
 * @author osse
 */
public class CosemAttributeScalerUnit extends TypedCosemAttribute<ScalerUnit>
{
  public CosemAttributeScalerUnit(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                  CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(ScalerUnit value)
  {
    return value.toDlmsData();
  }

  @Override
  protected ScalerUnit dlmsData2Value(DlmsData data) throws ValidationExecption
  {
    return new ScalerUnit(data);
  }

}
