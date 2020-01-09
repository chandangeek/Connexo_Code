/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class18/ImageTransferProcess.java $
 * Version:     
 * $Id: ImageTransferProcess.java 2772 2011-03-11 16:19:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 11, 2010 10:08:24 AM
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.dlms.cosem.applicationlayer.CosemAsyncActionRequest;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncGetRequest;
import com.elster.dlms.cosem.applicationprocess.CosemApplicationProcess;
import com.elster.dlms.cosem.objectmodel.CosemExecutor;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.protocols.streams.SafeReadInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Image Transfer Process
 *
 * @author osse
 */
public class ImageTransferProcess extends CosemExecutor implements Runnable
{
  private static final Logger LOGGER = Logger.getLogger(ImageTransferProcess.class.getName());

  public interface ProgressListener
  {
    public void progress(ImageTransferProcess sender, int blocksTransfered, int blocksTotal);
  }

  private final CosemApplicationProcess applicationProcess;
  private final CosemObject imageTransferObject;
  private final boolean continueTransfer;
  private final InputStream in;
  private final int imageSize;
  private final String imageName;
  private int position = 0;
  private long imageBlockSize;
  private Exception errorReason;
  private boolean canceled = false;
  private final List<ProgressListener> progressListeners = new ArrayList<ProgressListener>();

  public ImageTransferProcess(CosemApplicationProcess applicationProcess, CosemObject imageTransferObject,
                              boolean continueTransfer, String imageName,
                              InputStream image,
                              int imageSize)
  {
    this.applicationProcess = applicationProcess;
    this.imageTransferObject = imageTransferObject;
    this.continueTransfer = continueTransfer;
    this.in = new SafeReadInputStream(new BufferedInputStream(image));
    this.imageSize = imageSize;
    this.imageName = imageName;
  }

  public void reset()
  {
    position = 0;
    setExecutionState(ExecutionState.NONE);
    errorReason = null;
    canceled = false;
  }

  public void run()
  {
    reset();

    LOGGER.finer("Starting image transfer");

    setExecutionState(ExecutionState.RUNNING);
    try
    {
      BitString imageTransferredBlocksStatus = null;

      if (!continueTransfer)
      {
        initiate();
      }
      else
      {
        imageTransferredBlocksStatus = readTransferedBlocksStatus();
      }

      imageBlockSize= readBlockSize();
      int blockCount =(int) ((long)imageSize / imageBlockSize + ((long) imageSize % imageBlockSize > 0 ? 1 : 0));

      if (imageTransferredBlocksStatus != null && imageTransferredBlocksStatus.getBitCount() != blockCount)
      {
        throw new IOException("Wrong block count in transfered block status");
      }

      for (int blockNo = 0; blockNo < blockCount; blockNo++)
      {
        if (canceled)
        {
          LOGGER.finer("Image transfer canceled");
          setExecutionState(ExecutionState.CANCELED);
          return;
        }

        if (imageTransferredBlocksStatus == null || !imageTransferredBlocksStatus.isBitSet(blockNo))
        {
          LOGGER.log(Level.FINER, "Transfering block number {0}", new Object[]
                  {
                    blockNo
                  });
          transferNextBlock(blockNo);
          fireProgress(blockNo + 1, blockCount);
        }
        else
        {
          LOGGER.log(Level.FINER, "Skipping block number {0}", new Object[]
                  {
                    blockNo
                  });
          skipNextBlock();
        }
      }
      LOGGER.log(Level.FINER, "Image transfer finished");
      setExecutionState(ExecutionState.FINISHED);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Error transfering image", ex);
      errorReason = ex;
      setExecutionState(ExecutionState.ERROR);
    }
  }

  private void initiate() throws IOException
  {
    CosemMethodDescriptor methodDescriptor = imageTransferObject.getMethod(1).getMethodDescriptor();

    DlmsDataStructure imageTransferInitiate = new DlmsDataStructure(
            new DlmsData[]
            {
              new DlmsDataOctetString(imageName.getBytes()),
              new DlmsDataDoubleLongUnsigned((long)imageSize)
            });

    CosemAsyncActionRequest executeAsync =
            applicationProcess.getAsyncApplicationLayer().executeAsync(methodDescriptor, imageTransferInitiate);
    executeAsync.waitFor();
    executeAsync.checkResult();
  }

  private void transferNextBlock(int blockNo) throws IOException
  {
    CosemMethodDescriptor methodDescriptor = imageTransferObject.getMethod(2).getMethodDescriptor();
    DlmsDataStructure imageBlockTransfer = new DlmsDataStructure(new DlmsData[]
            {
              new DlmsDataDoubleLongUnsigned(blockNo),
              new DlmsDataOctetString(getNextBlock())
            });
    CosemAsyncActionRequest executeAsync =
            applicationProcess.getAsyncApplicationLayer().executeAsync(methodDescriptor, imageBlockTransfer);
    executeAsync.waitFor();
    executeAsync.checkResult();
  }

  private BitString readTransferedBlocksStatus() throws IOException
  {
    CosemAttributeDescriptor attributeDescriptor = imageTransferObject.getAttribute(3).
            getCosemAttributeDescriptor();
    CosemAsyncGetRequest getAsync =
            applicationProcess.getAsyncApplicationLayer().getAsync(attributeDescriptor);
    getAsync.waitFor();
    getAsync.checkResult();

    if (getAsync.getGetDataResult().getData().getType() != DlmsData.DataType.BIT_STRING)
    {
      throw new IOException("Unexpected data type: " + getAsync.getGetDataResult().getData().getType());
    }

    return (BitString)getAsync.getGetDataResult().getData().getValue();
  }


  private long readBlockSize() throws IOException
  {
    CosemAttributeDescriptor attributeDescriptor = imageTransferObject.getAttribute(2).
            getCosemAttributeDescriptor();
    CosemAsyncGetRequest getAsync =
            applicationProcess.getAsyncApplicationLayer().getAsync(attributeDescriptor);
    getAsync.waitFor();
    getAsync.checkResult();

    if (getAsync.getGetDataResult().getData().getType() != DlmsData.DataType.DOUBLE_LONG_UNSIGNED)
    {
      throw new IOException("Unexpected data type: " + getAsync.getGetDataResult().getData().getType());
    }

    return ((Number) getAsync.getGetDataResult().getData().getValue()).longValue();
  }


  private void skipNextBlock() throws IOException
  {
    position += in.skip(getNextBlockSize());
  }

  private byte[] getNextBlock() throws IOException
  {
    int size = getNextBlockSize();

    byte[] result = new byte[size];
    position += in.read(result);
    return result;
  }

  private int getNextBlockSize()
  {
    int size;
    if (position + imageBlockSize > imageSize)
    {
      size = imageSize - position;
    }
    else
    {
      size =(int) imageBlockSize;
    }
    return size;
  }


  public void addProgressListener(ProgressListener progressListener)
  {
    synchronized (progressListeners)
    {
      progressListeners.add(progressListener);
    }
  }

  public void removeProgressListener(ProgressListener progressListener)
  {
    synchronized (progressListeners)
    {
      progressListeners.remove(progressListener);
    }
  }

  private final static ProgressListener[] EMPTY_PROGRESS_LISTENERS = new ProgressListener[0];

  private void fireProgress(final int blocksTransfered,final int blocksTotal)
  {
    ProgressListener[] localListeners;

    synchronized (progressListeners)
    {
      localListeners = progressListeners.toArray(EMPTY_PROGRESS_LISTENERS);
    }

    for (ProgressListener localListener : localListeners)
    {
      localListener.progress(this, blocksTransfered, blocksTotal);
    }
  }

  public Exception getErrorReason()
  {
    return errorReason;
  }

  public void cancel()
  {
    canceled = true;
  }

}
