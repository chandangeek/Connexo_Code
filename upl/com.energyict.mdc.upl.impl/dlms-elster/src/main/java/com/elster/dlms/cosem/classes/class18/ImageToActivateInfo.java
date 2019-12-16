/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class18/ImageToActivateInfo.java $
 * Version:     
 * $Id: ImageToActivateInfo.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 8, 2010 3:31:41 PM
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;

/**
 * Image activate info<P>
 * See BB ed.10 ch.4.4.4.4 p.69
 *
 * @author osse
 */
public class ImageToActivateInfo
{
  private final long imageSize;
  private final byte[] imageIdentification;
  private final byte[] imageSignature;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.DOUBLE_LONG_UNSIGNED),
          new ValidatorSimpleType(DataType.OCTET_STRING),
          new ValidatorSimpleType(DataType.OCTET_STRING));
  
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static ImageToActivateInfo[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    ImageToActivateInfo[] result = new ImageToActivateInfo[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new ImageToActivateInfo(array.get(i));
    }
    return result;
  }

  

  public ImageToActivateInfo(long imageSize, byte[] imageIdentification, byte[] imageSignature)
  {
    this.imageSize = imageSize;
    this.imageIdentification = imageIdentification.clone();
    this.imageSignature = imageSignature.clone();
  }

  public ImageToActivateInfo(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    DlmsDataStructure structure = (DlmsDataStructure)data;
    imageSize = ((DlmsDataDoubleLongUnsigned)structure.get(0)).getValue();
    imageIdentification = ((DlmsDataOctetString)structure.get(1)).getValue();
    imageSignature = ((DlmsDataOctetString)structure.get(2)).getValue();
  }
  
  public DlmsData toDlmsData()
  {
     return new DlmsDataStructure(
              new DlmsDataDoubleLongUnsigned(imageSize),
              new DlmsDataOctetString(imageIdentification),
              new DlmsDataOctetString(imageSignature));
  }

  public byte[] getImageIdentification()
  {
    return imageIdentification.clone();
  }

  public byte[] getImageSignature()
  {
    return imageSignature.clone();
  }

  public long getImageSize()
  {
    return imageSize;
  }

  @Override
  public String toString()
  {
    return "ImageToActivateInfo{" + "imageSize=" + imageSize + ", imageIdentification=\"" + CodingUtils.
            byteArrayToString(imageIdentification, " ") + "\", imageSignature=\"" + CodingUtils.
            byteArrayToString(imageSignature, " ") + "\"}";
  }

}
