/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/CosemAttributeObjectList.java $
 * Version:     
 * $Id: CosemAttributeObjectList.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 3:44:16 PM
 */

package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import java.util.Arrays;
import java.util.List;

/**
 * Special CosemAttribute (for the object model) representing the object list.
 *
 * @author osse
 */
public class CosemAttributeObjectList extends TypedCosemAttribute<List<CosemObjectListElement>>
{

  public CosemAttributeObjectList(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                  CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }
  

  public static List<CosemObjectListElement> dataToObjectList(DlmsData data) throws ValidationExecption
  {
//    List<CosemObjectListElement> objectList= new ArrayList<CosemObjectListElement>();
    return Arrays.asList(CosemObjectListElement.buildElements(data));
  }

  @Override
  protected DlmsData value2DlmsData(List<CosemObjectListElement> value)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected List<CosemObjectListElement> dlmsData2Value(DlmsData data) throws ValidationExecption
  {
    return dataToObjectList(data);
  }
}
