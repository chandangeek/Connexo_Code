/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class18/ImageTransferSupport.java $
 * Version:     
 * $Id: ImageTransferSupport.java 3956 2012-01-23 17:15:15Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 11, 2010 10:08:24 AM
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.protocols.streams.SafeReadInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for image transfer tasks.
 *
 * @author osse
 */
public class ImageTransferSupport
{
  private final ObisCode imageTransferObject;
  private final boolean continueTransfer;
  private final InputStream in;
  private final long imageSize;
  private final String imageName;
  private final SecurityControlField securityControl;
  private int position = 0;
  private long imageBlockSize;
  private BitString imageTransferredBlocksStatus = null;
  private int blockCount;
  private int currentBlock;
  private boolean prepared = false;

  public ImageTransferSupport(
          final ObisCode imageTransferObject,
          final boolean continueTransfer,
          final String imageName,
          final InputStream image,
          final long imageSize,
          final SecurityControlField securityControl)
  {
    this.imageTransferObject = imageTransferObject;
    this.continueTransfer = continueTransfer;
    this.in = new SafeReadInputStream(new BufferedInputStream(image));
    this.imageSize = imageSize;
    this.imageName = imageName;
    this.securityControl = securityControl;
  }

  public void initiate(final CosemApplicationLayer applicationLayer) throws IOException
  {
    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(imageTransferObject, 18, 1);

    final DlmsDataStructure imageTransferInitiate = new DlmsDataStructure(
            new DlmsDataOctetString(imageName.getBytes()),
            new DlmsDataDoubleLongUnsigned(imageSize));

    applicationLayer.executeActionAndCheckResponse(methodDescriptor, imageTransferInitiate, securityControl);
  }

  public void prepareTransfer(final CosemApplicationLayer applicationLayer) throws IOException
  {
    if (prepared)
    {
      throw new IllegalStateException("Transfer allready prepared");
    }


    imageBlockSize = readBlockSize(applicationLayer);
    blockCount = (int)(imageSize / imageBlockSize + (imageSize % imageBlockSize > 0 ? 1 : 0));

    if (continueTransfer)
    {
      imageTransferredBlocksStatus = readTransferedBlocksStatus(applicationLayer);

      if (imageTransferredBlocksStatus.getBitCount() != blockCount)
      {
        throw new IOException("Wrong block count in transfered block status");
      }
    }

    prepared = true;
  }

  public boolean transferNext(final CosemApplicationLayer applicationLayer) throws IOException
  {
    if (!prepared)
    {
      throw new IllegalStateException("Transfer not prepared");
    }

    if (currentBlock >= blockCount)
    {
      return true;
    }

    if (continueTransfer && imageTransferredBlocksStatus.isBitSet(currentBlock))
    {
      skipNextBlock();
    }
    else
    {
      transferNextBlock(applicationLayer, currentBlock);
    }
    currentBlock++;
    return currentBlock >= blockCount;
  }

  public void verify(final CosemApplicationLayer cosemApplicationLayer) throws
          IOException
  {
    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(imageTransferObject, 18, 3);
    cosemApplicationLayer.executeActionAndCheckResponse(methodDescriptor, new DlmsDataInteger(0),
                                                        securityControl);
  }
  
  public void activate(final CosemApplicationLayer cosemApplicationLayer) throws
          IOException
  {
    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(imageTransferObject, 18, 4);
    cosemApplicationLayer.executeActionAndCheckResponse(methodDescriptor, new DlmsDataInteger(0),
                                                        securityControl);
  }
  


  private void transferNextBlock(final CosemApplicationLayer applicationLayer, final int blockNo) throws
          IOException
  {
    final CosemMethodDescriptor methodDescriptor = new CosemMethodDescriptor(imageTransferObject, 18, 2);
    final DlmsDataStructure imageBlockTransfer = new DlmsDataStructure(new DlmsData[]
            {
              new DlmsDataDoubleLongUnsigned(blockNo),
              new DlmsDataOctetString(getNextBlock())
            });
    applicationLayer.executeActionAndCheckResponse(methodDescriptor, imageBlockTransfer, securityControl);
  }

  public BitString readTransferedBlocksStatus(final CosemApplicationLayer applicationLayer) throws
          IOException
  {
    final CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(imageTransferObject, 18,
                                                                                      3);
    final DlmsData data = applicationLayer.getAttributeAndCheckResult(attributeDescriptor, securityControl);

    if (data.getType() != DlmsData.DataType.BIT_STRING)
    {
      throw new IOException("Unexpected data type: " + data.getType() + ", Expected:"
                            + DlmsData.DataType.BIT_STRING + ", Attribute descriptor: " + attributeDescriptor);
    }

    return (BitString)data.getValue();
  }

  public long readBlockSize(final CosemApplicationLayer applicationLayer) throws IOException
  {
    final CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(imageTransferObject, 18,
                                                                                      2);

    final DlmsData data = applicationLayer.getAttributeAndCheckResult(attributeDescriptor, securityControl);

    if (data.getType() != DlmsData.DataType.DOUBLE_LONG_UNSIGNED)
    {
      throw new IOException("Unexpected data type: " + data.getType() + ", Expected:"
                            + DlmsData.DataType.DOUBLE_LONG_UNSIGNED + ", Attribute descriptor: "
                            + attributeDescriptor);
    }

    return ((Number)data.getValue()).longValue();
  }

  private void skipNextBlock() throws IOException
  {
    position += in.skip(getNextBlockSize());
  }

  private byte[] getNextBlock() throws IOException
  {
    final long size = getNextBlockSize();

    final byte[] result = new byte[(int)size];
    position += in.read(result);
    return result;
  }

  private long getNextBlockSize()
  {
    long size;
    if (position + imageBlockSize > imageSize)
    {
      size = imageSize - position;
    }
    else
    {
      size = (int)imageBlockSize;
    }
    return size;
  }

  public int getBlockCount()
  {
    return blockCount;
  }

  public int getCurrentBlock()
  {
    return currentBlock;
  }

}
