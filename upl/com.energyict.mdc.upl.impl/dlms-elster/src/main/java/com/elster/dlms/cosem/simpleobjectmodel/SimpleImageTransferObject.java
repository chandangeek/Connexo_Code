/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleImageTransferObject.java $
 * Version:     
 * $Id: SimpleImageTransferObject.java 6465 2013-04-22 14:45:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  01.09.2011 13:15:07
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class18.ImageToActivateInfo;
import com.elster.dlms.cosem.classes.class18.ImageTransferStatusEnum;
import com.elster.dlms.cosem.classes.class18.ImageTransferSupport;
import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataBitString;
import com.elster.dlms.types.data.DlmsDataBoolean;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataInteger;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple COSEM object for image transfer.
 *
 * @author osse
 */
public class SimpleImageTransferObject extends SimpleCosemObject
{
  private final CosemApplicationLayer applicationLayer;

  /*package private*/
  SimpleImageTransferObject(final SimpleCosemObjectDefinition definition,
                            final SimpleCosemObjectManager objectManager,
                            final CosemApplicationLayer applicationLayer)
  {
    super(definition, objectManager);
    this.applicationLayer = applicationLayer;
  }

  public long getFirstNotTransferredBlockNo() throws IOException
  {
    return executeGet(4, DlmsDataDoubleLongUnsigned.class, false).getValue();
  }

  public long getImageBlockSize() throws IOException
  {
    return executeGet(2, DlmsDataDoubleLongUnsigned.class, false).getValue();
  }

  public ImageToActivateInfo[] getImageToActivateInfo() throws IOException
  {
    try
    {
      final DlmsDataArray imageToActivateInfoData = executeGet(7, DlmsDataArray.class, false);
      return ImageToActivateInfo.fromDlmsDataArray(imageToActivateInfoData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException("Validation of received data failed: ", ex);
    }
  }

  public boolean isImageTransferEnabled() throws IOException
  {
    return executeGet(5, DlmsDataBoolean.class, false).getValue();
  }

  public ImageTransferStatusEnum getImageTransferStatus() throws IOException
  {
    //--- image_transfer_status ---
    final DlmsDataEnum imageTransferStatusData = executeGet(6, DlmsDataEnum.class, false);
    final ImageTransferStatusEnum value =
            CosemEnumFactory.find(ImageTransferStatusEnum.values(), imageTransferStatusData.getValue());
    if (value == null)
    {
      throw new UnexpectedDlmsDataTypeIOException("Unexpected enum ID for image transfer status: "
                                                  + imageTransferStatusData.getValue());
    }
    return value;
  }

  public BitString getImageTransferedBlockStatus() throws IOException
  {
    //--- image_transferred_blocks_status ---
    return executeGet(3, DlmsDataBitString.class, false).getValue();
  }

  private void invalidateStatusAttributes()
  {
    getManager().removeFromCache(getDefinition().getLogicalName());
  }

  public boolean canImageTransferBeContinued(final String imageName, final int imageSize) throws IOException
  {
    return getImageTransferStatus() == ImageTransferStatusEnum.IMAGE_TRANSFER_INITIATED
           && isSameImage(imageName, imageSize);
  }

  public boolean isTransferComplete(final String imageName, final long imageSize) throws IOException
  {
    return getImageTransferStatus() == ImageTransferStatusEnum.IMAGE_TRANSFER_INITIATED
           && isSameImage(imageName, imageSize)
           && getImageTransferedBlockStatus().getPopulationCount() == getImageTransferedBlockStatus().
            getBitCount();
  }

  public boolean isSameImage(final String imageName, final long imageSize) throws IOException
  {
    return getImageToActivateInfo().length == 1
           && getImageToActivateInfo()[0].getImageSize() == imageSize
           && imageName.equals(new String(getImageToActivateInfo()[0].getImageIdentification()));
  }
  
  public void initiateAndTransferImage(final String imageName,
                                       final InputStream image,
                                       final long imageSize) throws IOException
  {
    final ImageTransferSupport imageTransferSupport = new ImageTransferSupport(
            getDefinition().getLogicalName(), false, imageName, image, imageSize, null);

    invalidateStatusAttributes();

    imageTransferSupport.prepareTransfer(applicationLayer);
    imageTransferSupport.initiate(applicationLayer);

    boolean finshed = false;
    while (!finshed)
    {
      finshed = imageTransferSupport.transferNext(applicationLayer);
    }
  }

  public void continueImageTransfer(final String imageName,
                                    final InputStream image,
                                    final long imageSize) throws IOException
  {
    final ImageTransferSupport imageTransferSupport = new ImageTransferSupport(
            getDefinition().getLogicalName(), true, imageName, image, imageSize, null);

    invalidateStatusAttributes();
    imageTransferSupport.prepareTransfer(applicationLayer);

    boolean finshed = false;
    while (!finshed)
    {
      finshed = imageTransferSupport.transferNext(applicationLayer);
    }
  }

  public void verifyImage() throws IOException
  {
    invalidateStatusAttributes();

    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(getDefinition().getLogicalName(),
                                                                             18, 3);

    applicationLayer.executeActionAndCheckResponse(methodDescriptor, new DlmsDataInteger(0));
  }

  public void activateImage() throws IOException
  {
    invalidateStatusAttributes();
    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(getDefinition().getLogicalName(),
                                                                             18, 4);

    applicationLayer.executeActionAndCheckResponse(methodDescriptor, new DlmsDataInteger(0));
  }

}
