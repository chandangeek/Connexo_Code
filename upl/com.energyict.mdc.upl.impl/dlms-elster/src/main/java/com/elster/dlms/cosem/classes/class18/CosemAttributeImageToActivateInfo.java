/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class18/CosemAttributeImageToActivateInfo.java $
 * Version:     
 * $Id: CosemAttributeImageToActivateInfo.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 3:44:16 PM
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * Special attribute "image_to_activate_info" (class_id=18, version=0, attribute_id=7)
 *
 * @author osse
 */
public class CosemAttributeImageToActivateInfo extends TypedCosemAttribute<ImageToActivateInfo[]>
{
  public CosemAttributeImageToActivateInfo(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                           CosemAttributeInfo attributeInfo, int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(ImageToActivateInfo[] value)
  {
    DlmsData[] infos = new DlmsData[value.length];

    for (int i = 0; i < value.length; i++)
    {
      infos[i] = value[i].toDlmsData();
    }
    return new DlmsDataArray(infos);
  }

  @Override
  protected ImageToActivateInfo[] dlmsData2Value(final DlmsData data) throws ValidationExecption
  {
    final DlmsDataArray dataArray = (DlmsDataArray)data;
    final ImageToActivateInfo[] imageToActivateInfos = new ImageToActivateInfo[dataArray.size()];

    for (int i = 0; i < dataArray.size(); i++)
    {
      imageToActivateInfos[i] = new ImageToActivateInfo(dataArray.get(i));
    }
    return imageToActivateInfos;
  }

}
