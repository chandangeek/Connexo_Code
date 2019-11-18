/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class18/ImageTransferStatusEnum.java $
 * Version:     
 * $Id: ImageTransferStatusEnum.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.ICosemEnum;

/**
 * Enumeration for the image transfer status<P>
 * COSEM class id 18, attribute 6.<P>
 * See BB ed.10 p.69
 *
 * @author osse
 */
public enum  ImageTransferStatusEnum implements ICosemEnum
{
  IMAGE_TRANSFER_NOT_INITIATED(0, "Image transfer not initiated"),
  IMAGE_TRANSFER_INITIATED(1, "Image transfer initiated"),
  IMAGE_VERIFICATION_INITIATED(2, "Image verification initiated"),
  IMAGE_VERIFICATION_SUCCESSFUL(3, "Image verification successful"),
  IMAGE_VERIFICATION_FAILED(4, "Image verification failed"),
  IMAGE_ACTIVATION_INITIATED(5, "Image activation initiated"),
  IMAGE_ACTIVATION_SUCCESSFUL(6, "Image activation successful"),
  IMAGE_ACTIVATION_FAILED(7, "Image activation failed");
  
  
  public static CosemEnumFactory<ImageTransferStatusEnum> getFactory()
  {
    return new CosemEnumFactory<ImageTransferStatusEnum>(values());
  }
  
  private final int id;
  private final String name;

  private ImageTransferStatusEnum(final int id,final String name)
  {
    this.id = id;
    this.name = name;
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }
  
}