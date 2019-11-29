/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/CosemAttributeCaptureObjects.java $
 * Version:     
 * $Id: CosemAttributeCaptureObjects.java 3040 2011-06-06 16:53:17Z osse $
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
import com.elster.dlms.types.data.DlmsDataArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Attribute 3 for class id 7 (Profile Generic)
 *
 * @author osse
 */
public class CosemAttributeCaptureObjects extends TypedCosemAttribute<List<CaptureObjectDefinition>>
{
  public CosemAttributeCaptureObjects(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                      CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(List<CaptureObjectDefinition> value)
  {
    DlmsData[] captureDefs = new DlmsData[value.size()];

    for (int i = 0; i < value.size(); i++)
    {
      captureDefs[i] = value.get(i).toDlmsData();
    }

    return new DlmsDataArray(captureDefs);
  }

  @Override
  protected List<CaptureObjectDefinition> dlmsData2Value(DlmsData data) throws ValidationExecption
  {
    List<CaptureObjectDefinition> captureObjectDefinitions = new ArrayList<CaptureObjectDefinition>();

    DlmsDataArray dataArray = (DlmsDataArray)data;

    for (DlmsData d : dataArray)
    {
      captureObjectDefinitions.add(new CaptureObjectDefinition(d));
    }

    return captureObjectDefinitions;
  }

}
