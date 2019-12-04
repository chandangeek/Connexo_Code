/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class18/CosemImageTransferObject.java $
 * Version:     
 * $Id: CosemImageTransferObject.java 3891 2012-01-09 11:03:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 7, 2010 4:15:21 PM
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.dlms.cosem.classes.common.CosemAttributeEnum;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.LogicalDevice;
import com.elster.dlms.types.basic.ObisCode;

/**
 * Special object class  for COSEM class id 18 (Image Transfer).
 *
 * @author osse
 */
public class CosemImageTransferObject extends CosemObject
{

  public CosemImageTransferObject(LogicalDevice parent, ObisCode logicalName, int classId, int version)
  {
    super(parent, logicalName, classId, version);
  }
  

  private ImageTransferProcess imageTransferProcess = null;

  public ImageTransferProcess getImageTransferProcess()
  {
    return imageTransferProcess;
  }

  public void setImageTransferProcess(ImageTransferProcess imageTransferProcess)
  {
    this.imageTransferProcess = imageTransferProcess;
  }

  @SuppressWarnings("unchecked") //TODO: The CosemAttributeFactory creates the Attribute in this way. But it is not safe if the attributes are created by an other mechanism.
  public CosemAttributeEnum<ImageTransferStatusEnum> getAttributeImageTransferStatus()
  {
    return (CosemAttributeEnum<ImageTransferStatusEnum>)getAttribute(6);
  }

  public CosemAttributeImageToActivateInfo getAttributeImageToActivateInfo()
  {
    return (CosemAttributeImageToActivateInfo)getAttribute(7);
  }

}
