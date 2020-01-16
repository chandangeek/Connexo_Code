/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemAsyncApplicationLayer.java $
 * Version:     
 * $Id: CosemAsyncApplicationLayer.java 3911 2012-01-13 17:04:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 28, 2010 4:28:08 PM
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;
import com.elster.protocols.IProtocolStateObservable;
import com.elster.protocols.IProtocolStateObserver;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.ProtocolStateObservableSupport;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  This layer provides async background access for the {@link CosemApplicationLayer}
 *
 * @author osse
 */
public class CosemAsyncApplicationLayer implements IProtocolStateObservable
{
  private static final Logger LOGGER = Logger.getLogger(CosemAsyncApplicationLayer.class.getName());
  private final CosemApplicationLayer sublayer;
  private final BlockingQueue<CosemAsyncServiceInvocation> queue =
          new LinkedBlockingQueue<CosemAsyncServiceInvocation>();
  private final static MarkerInvocation POISON = new MarkerInvocation();
  private final ProtocolStateObservableSupport protocolStateSupport = new ProtocolStateObservableSupport(
          this);
  private Thread workerThread;

  /**
   * Constructs this layer on top of specified {@link CosemApplicationLayer }
   *
   * @param sublayer
   */
  public CosemAsyncApplicationLayer(final CosemApplicationLayer sublayer)
  {
    this.sublayer = sublayer;
  }

  /**
   * Opens this layer.<P>
   * Internally the worker thread will be started by this method.
   */
  public synchronized void open()
  {
    workerThread = new Thread(new Worker());
    workerThread.setName("COSEM async appl layer thread");
    workerThread.start();
    protocolStateSupport.setState(ProtocolState.OPEN, true);
  }

  /**
   * Calls the COSEM GET Service.
   *
   * @param attributeDescriptor The attribute descriptor for the attribute to get.
   * @return The get request handler.
   */
  public CosemAsyncGetRequest getAsync(final CosemAttributeDescriptor attributeDescriptor)
  {
    final CosemAsyncGetRequest asyncGetRequest = new CosemAsyncGetRequest(attributeDescriptor);
    addToQueue(asyncGetRequest);
    return asyncGetRequest;
  }

  /**
   * Calls the COSEM ACTION service.
   *
   * @param methodDescriptor The method descriptor for the method to execute.
   * @param parameters The parameters.
   * @return The async action request handler.
   */
  public CosemAsyncActionRequest executeAsync(final CosemMethodDescriptor methodDescriptor,
                                              final DlmsData parameters)
  {
    final CosemAsyncActionRequest asyncActionRequest = new CosemAsyncActionRequest(methodDescriptor,
                                                                                   parameters);
    addToQueue(asyncActionRequest);
    return asyncActionRequest;
  }

  /**
   * Calls the COSEM SET service.
   *
   * @param attributeDescriptor The attribute descriptor for the attribute to set.
   * @param data The data to set.
   * @return The async set request handler.
   */
  public CosemAsyncSetRequest setAsync(final CosemAttributeDescriptor attributeDescriptor, final DlmsData data)
  {
    final CosemAsyncSetRequest asyncSetRequest = new CosemAsyncSetRequest(attributeDescriptor, data);

    addToQueue(asyncSetRequest);
    return asyncSetRequest;
  }
  
  
  public CosemAsyncTaskRequest executeTask(final ICosemApplicationLayerTask task)
  {
    final CosemAsyncTaskRequest asyncExecutorRequest= new CosemAsyncTaskRequest(task);
    addToQueue(asyncExecutorRequest);
    return asyncExecutorRequest;
  }

  /**
   * Closes this layer.<P>
   * This method returns after all invocations (get, set, action) were made.
   */
  public synchronized void close()
  {
    protocolStateSupport.setState(ProtocolState.CLOSING, true);
    try
    {

      queue.add(POISON);
      if (workerThread != null)
      {
        try
        {
          workerThread.join();
        }
        catch (InterruptedException ex)
        {
          Logger.getLogger(CosemAsyncApplicationLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    finally
    {
      protocolStateSupport.setState(ProtocolState.CLOSE, true);
    }
  }

  //@Override
  public void addProtocolStateListener(final IProtocolStateObserver observer)
  {
    protocolStateSupport.addProtocolStateListener(observer);
  }

  //@Override
  public void removeProtocolStateListener(final IProtocolStateObserver observer)
  {
    protocolStateSupport.removeProtocolStateListener(observer);
  }

  //@Override
  public ProtocolState getProtocolState()
  {
    return protocolStateSupport.getProtocolState();
  }

  //@Override
  public boolean isOpen()
  {
    return protocolStateSupport.isOpen();
  }

  protected void addToQueue(final CosemAsyncServiceInvocation serviceInvocation)
  {
    serviceInvocation.setState(CosemAsyncServiceInvocation.State.ENQUEUED);
    queue.add(serviceInvocation);
  }

  private class Worker implements Runnable
  {
    //@Override
    public void run()
    {
      try
      {
        while (true)
        {
          final CosemAsyncServiceInvocation invocation = queue.take();
          if (invocation == POISON)
          {
            break;
          }

          LOGGER.finer("Start executing invocation");
          if (invocation.setState(CosemAsyncServiceInvocation.State.ENQUEUED,
                                  CosemAsyncServiceInvocation.State.EXECUTING))
          {
            try
            {
              execute(invocation);
              LOGGER.finer("Finished executing invocation. Notify...");

              invocation.setState(CosemAsyncServiceInvocation.State.FINISHED);
              LOGGER.finer("Notification finished");
              
            }
            catch (IOException ex)
            {
              invocation.setError(ex);
            }
          }
        }
      }
      catch (InterruptedException ex)
      {
        Logger.getLogger(CosemAsyncApplicationLayer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    private void execute(final CosemAsyncServiceInvocation invocation) throws IOException
    {
      switch (invocation.getServiceType())
      {
        case GET:
          executeGet((CosemAsyncGetRequest)invocation);
          break;
        case SET:
          extecuteSet((CosemAsyncSetRequest)invocation);
          break;
        case ACTION:
          executeAction((CosemAsyncActionRequest)invocation);
          break;
        case TASK:
          executeTask((CosemAsyncTaskRequest)invocation);
          break;
        default:
          LOGGER.log(Level.SEVERE, "unknown service type: {0}", invocation.getServiceType());
          
      }
    }

    private void executeGet(final CosemAsyncGetRequest cosemAsyncGetRequest) throws IOException
    {
      final CosemAttributeDescriptor attributeDescriptor = cosemAsyncGetRequest.getAttributeDescriptor();
      final long start= System.currentTimeMillis();
      final GetDataResult getDataResult = sublayer.getAttribute(attributeDescriptor);
      final long end= System.currentTimeMillis();
      final long duration= end-start;
      final long middle= start+ (duration/2);
      cosemAsyncGetRequest.setGetDataResult(getDataResult,new Date(middle));
    }

    private void executeAction(final CosemAsyncActionRequest cosemAsyncActionRequest) throws IOException
    {
      final ActionResponse actionResponse =
              sublayer.executeAction(cosemAsyncActionRequest.getMethodDecriptor(),
                                     cosemAsyncActionRequest.getParameters(), null);
      cosemAsyncActionRequest.setActionResponseWithOptionalData(actionResponse);
    }

    private void extecuteSet(final CosemAsyncSetRequest cosemAsyncSetRequest) throws IOException
    {
      final DataAccessResult dataAccessResult =
              sublayer.setAttribute(cosemAsyncSetRequest.getAttributeDescriptor(),
                                    cosemAsyncSetRequest.getData(), null);

      cosemAsyncSetRequest.setDataAccessResult(dataAccessResult);
    }

    private void executeTask(CosemAsyncTaskRequest cosemAsyncExecutorRequest) throws IOException
    {
      cosemAsyncExecutorRequest.getExecutor().execute(sublayer);
    }

  }

  private static class MarkerInvocation extends CosemAsyncServiceInvocation
  {
    @Override
    public ServiceType getServiceType()
    {
      return null;
    }

  }

}
