/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationprocess/CosemApplicationProcess.java $
 * Version:     
 * $Id: CosemApplicationProcess.java 5799 2013-01-07 15:49:16Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 28, 2010 4:27:26 PM
 */
package com.elster.dlms.cosem.applicationprocess;

import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncActionRequest;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncApplicationLayer;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncGetRequest;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncServiceInvocation;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncServiceInvocation.InvocationListener;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncServiceInvocation.State;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncSetRequest;
import com.elster.dlms.cosem.applicationlayer.CosemAsyncTaskRequest;
import com.elster.dlms.cosem.objectmodel.AbstractCosemDataNode;
import com.elster.dlms.cosem.objectmodel.AbstractCosemDataNode.ReadState;
import com.elster.dlms.cosem.objectmodel.CosemAttribute;
import com.elster.dlms.cosem.objectmodel.CosemDataNode;
import com.elster.dlms.cosem.objectmodel.CosemExecutor.ExecutionState;
import com.elster.dlms.cosem.objectmodel.CosemMethod;
import com.elster.dlms.cosem.objectmodel.CosemTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class connects the COSEM object model to the application layer.
 *
 * @author osse
 */
public class CosemApplicationProcess
{
  //private static final Logger LOGGER = Logger.getLogger(CosemApplicationProcess.class.getName());
  private final CosemAsyncApplicationLayer asyncApplicationLayer;
  private final Map<CosemAttribute, RequestGetAttribute> getRequests =
          new HashMap<CosemAttribute, RequestGetAttribute>();
  private final Map<CosemAttribute, RequestSetAttribute> setRequests =
          new HashMap<CosemAttribute, RequestSetAttribute>();
  private final Map<CosemMethod, RequestExecuteMethod> actionRequests =
          new HashMap<CosemMethod, RequestExecuteMethod>();
  private final Map<CosemTask, RequestTask> cosemTasks =
          new HashMap<CosemTask, RequestTask>();

  /**
   * Constructor.
   *
   * @param asyncApplicationLayer The underlying {@link CosemAsyncApplicationLayer}. This layer will be used for all
   * requests.
   */
  public CosemApplicationProcess(final CosemAsyncApplicationLayer asyncApplicationLayer)
  {
    this.asyncApplicationLayer = asyncApplicationLayer;
  }

  /**
   * Reads all attributes of the specified {@link CosemDataNode} using the GET service.
   *
   * @param requester The requester (can be used to cancel the request)
   * @param dataNode The data node.
   */
  public void get(final Object requester, final CosemDataNode dataNode)
  {
    final List<CosemAttribute> attributes = collectAttributes(dataNode);

    final RequesterInfo requesterInfo = new RequesterInfo(requester, dataNode);

    for (CosemAttribute a : attributes)
    {
      getAttribute(requesterInfo, a);
    }
  }

  private List<CosemAttribute> collectAttributes(final CosemDataNode dataNode)
  {
    final List<CosemAttribute> attributes = new ArrayList<CosemAttribute>();
    dataNode.collectCosemAttributes(attributes);
    return attributes;
  }

  private void getAttribute(final RequesterInfo requesterInfo, final CosemAttribute attribute)
  {
    synchronized (getRequests)
    {
      RequestGetAttribute requestReadAttribute = getRequests.get(attribute);

      if (requestReadAttribute == null)
      {
        final ReadState oldReadState = attribute.getReadState();
        if (oldReadState == CosemAttribute.ReadState.OK)
        {
          attribute.setReadState(AbstractCosemDataNode.ReadState.UPDATING);
        }
        else
        {
          attribute.setReadState(AbstractCosemDataNode.ReadState.READING);
        }

        final CosemAsyncGetRequest asyncGetRequest = asyncApplicationLayer.getAsync(attribute.
                getCosemAttributeDescriptor());
        requestReadAttribute = new RequestGetAttribute(requesterInfo, asyncGetRequest,
                                                       attribute, oldReadState);
        getRequests.put(attribute, requestReadAttribute);
        asyncGetRequest.addListener(requestReadAttribute);
      }
      else
      {
        requestReadAttribute.addRequester(requesterInfo);
      }
    }
  }

  /**
   * Tries to cancel the specified get request.<P>
   * Tries to cancel a get request started by {@link #get(java.lang.Object, com.elster.dlms.cosem.objectmodel.CosemDataNode)}.
   *
   * @param requester The requester.
   * @param dataNode The data node.
   */
  public void cancelGet(final Object requester, final CosemDataNode dataNode)
  {
    final RequesterInfo requesterInfo = new RequesterInfo(requester, dataNode);
    final List<CosemAttribute> attributes = collectAttributes(dataNode);

    synchronized (getRequests)
    {
      for (CosemAttribute attribute : attributes)
      {
        final RequestGetAttribute requestGetAttribute = getRequests.get(attribute);

        if (requestGetAttribute != null)
        {
          final int requestCount = requestGetAttribute.removeRequester(requesterInfo);
          if (requestCount == 0)
          {
            requestGetAttribute.cancelRequest();
          }
        }
      }
    }
  }

  /**
   * Returns the {@link CosemAsyncApplicationLayer} specified in the constructor.
   *
   * @return The async application layer.
   */
  public CosemAsyncApplicationLayer getAsyncApplicationLayer()
  {
    return asyncApplicationLayer;
  }

  /**
   * Sets the specified attributes using the COSEM SET service.
   *
   * @param requester The requester.
   * @param attributes The attributes.
   */
  public void set(final Object requester, final List<CosemAttribute> attributes)
  {
    for (CosemAttribute a : attributes)
    {
      set(requester, a);
    }
  }

  /**
   * Sets the specified attributes using the COSEM SET service.
   *
   * @param requester The requester.
   * @param attribute The attribute to set.
   */
  public void set(final Object requester, final CosemAttribute attribute)
  {
    synchronized (setRequests)
    {
      RequestSetAttribute setRequest = setRequests.get(attribute);

      if (setRequest == null)
      {
        attribute.setWriteState(AbstractCosemDataNode.WriteState.WRITING);


        final CosemAsyncSetRequest asyncSetRequest = asyncApplicationLayer.setAsync(attribute.
                getCosemAttributeDescriptor(),
                                                                                    attribute.getData());
        setRequest = new RequestSetAttribute(asyncSetRequest, attribute);
        setRequests.put(attribute, setRequest);
        asyncSetRequest.addListener(setRequest);
      }
    }
  }

  /**
   * Executes the the specified method using the COSEM ACTION service.
   *
   * @param requester The requester.
   * @param method The method to execute.
   * @param listener Listener which will be informed when this requests ends. Can be null {@code null}
   */
  public void execute(Object requester, CosemMethod method, IMethodResultListener listener)
  {
    synchronized (actionRequests)
    {
      RequestExecuteMethod requestExecuteMethod = actionRequests.get(method);

      if (requestExecuteMethod == null)
      {
        method.setActionResponse(ExecutionState.RUNNING, null);

        CosemAsyncActionRequest asyncActionRequest = asyncApplicationLayer.executeAsync(method.
                getMethodDescriptor(), method.getParameters());
        requestExecuteMethod = new RequestExecuteMethod(asyncActionRequest, method, listener);
        actionRequests.put(method, requestExecuteMethod);
        asyncActionRequest.addListener(requestExecuteMethod);
      }
    }
  }

  /**
   * Executes the the specified method using the COSEM ACTION service.
   *
   * @param requester The requester.
   * @param task The method to execute.
   */
  public void executeTask(Object requester, CosemTask task)
  {
    synchronized (cosemTasks)
    {
      RequestTask requestTask = cosemTasks.get(task);

      if (requestTask == null)
      {
        task.setExecutionState(ExecutionState.RUNNING);

        CosemAsyncTaskRequest asyncTaskRequest = asyncApplicationLayer.executeTask(task);

        requestTask = new RequestTask(asyncTaskRequest, task);
        cosemTasks.put(task, requestTask);
        asyncTaskRequest.addListener(requestTask);
      }
    }
  }

  public interface IMethodResultListener
  {
    void methodResult(CosemMethod method, ActionResponse actionResponse);

  }

  private static class RequesterInfo
  {
    private final Object requester;
    private final Object requestedObject;

    public RequesterInfo(Object requester, Object requestedObject)
    {
      this.requester = requester;
      this.requestedObject = requestedObject;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final RequesterInfo other = (RequesterInfo)obj;
      if (this.requester != other.requester && (this.requester == null || !this.requester.equals(
              other.requester)))
      {
        return false;
      }
      if (this.requestedObject != other.requestedObject && (this.requestedObject == null
                                                            || !this.requestedObject.equals(
              other.requestedObject)))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 53 * hash + (this.requester != null ? this.requester.hashCode() : 0);
      hash = 53 * hash + (this.requestedObject != null ? this.requestedObject.hashCode() : 0);
      return hash;
    }

  }

  private abstract class Request implements InvocationListener
  {
    protected final List<RequesterInfo> requesters = new ArrayList<RequesterInfo>();

    public void stateChanged(CosemAsyncServiceInvocation sender, State oldState, State newState)
    {
    }

    public void invocationFinished(CosemAsyncServiceInvocation sender, State newState)
    {
      onFinished();
    }

    public void addRequester(RequesterInfo requesterInfo)
    {
      synchronized (requesters)
      {
        requesters.add(requesterInfo);
      }
    }

    public int removeRequester(RequesterInfo requesterInfo)
    {
      synchronized (requesters)
      {
        requesters.remove(requesterInfo);
        return requesters.size();
      }
    }

    public boolean cancelRequest()
    {
      return getAsyncServiceInvocation().cancel();
    }

    abstract protected void onFinished();

    abstract protected CosemAsyncServiceInvocation getAsyncServiceInvocation();

  }

  private class RequestGetAttribute extends Request
  {
    protected final CosemAsyncGetRequest getRequest;
    protected final CosemAttribute attribute;
    protected final ReadState oldReadState;

    public RequestGetAttribute(RequesterInfo requesterInfo, CosemAsyncGetRequest getRequest,
                               CosemAttribute attribute, ReadState oldReadState)
    {
      this.requesters.add(requesterInfo);
      this.getRequest = getRequest;
      this.attribute = attribute;
      this.oldReadState = oldReadState;
    }

    @Override
    protected void onFinished()
    {
      synchronized (getRequests)
      {
        getRequests.remove(attribute);

        switch (getRequest.getState())
        {
          case FINISHED:
            if (getRequest.getGetDataResult().getAccessResult() == DataAccessResult.SUCCESS)
            {
              attribute.setStoredData(getRequest.getGetDataResult().getData(), DataAccessResult.SUCCESS,
                                      CosemAttribute.ReadState.OK, null,getRequest.getTimestamp());
            }
            else
            {
              attribute.setStoredData(getRequest.getGetDataResult().getData(), getRequest.getGetDataResult().
                      getAccessResult(), CosemAttribute.ReadState.ERROR, null,getRequest.getTimestamp());
            }

            break;
          case ERROR:
            attribute.setStoredData(null, null, CosemAttribute.ReadState.ERROR, null,null);
            break;
          case CANCELED:
            attribute.setReadState(oldReadState);
            break;
          default:
            attribute.setStoredData(null, null, CosemAttribute.ReadState.UNREAD, null,null);
            break;
        }
      }
    }

    @Override
    protected CosemAsyncServiceInvocation getAsyncServiceInvocation()
    {
      return getRequest;
    }

  }

  private class RequestSetAttribute extends Request
  {
    protected final CosemAsyncSetRequest asyncSetRequest;
    protected final CosemAttribute attribute;

    public RequestSetAttribute(CosemAsyncSetRequest setRequest, CosemAttribute attribute)
    {
      this.asyncSetRequest = setRequest;
      this.attribute = attribute;
    }

    @Override
    protected void onFinished()
    {
      synchronized (setRequests)
      {
        setRequests.remove(attribute);
        switch (asyncSetRequest.getState())
        {
          case FINISHED:
            if (asyncSetRequest.getDataAccessResult() == DataAccessResult.SUCCESS)
            {
              attribute.setStoredData(asyncSetRequest.getData(), null, null,
                                      AbstractCosemDataNode.WriteState.OK, null);
            }
            else
            {
              synchronized (attribute)
              {
                attribute.setWriteDataAccessResult(asyncSetRequest.getDataAccessResult());
                attribute.setWriteState(AbstractCosemDataNode.WriteState.ERROR);
              }
            }
            break;
          default:
            attribute.setWriteState(AbstractCosemDataNode.WriteState.ERROR);
        }
      }
    }

    @Override
    protected CosemAsyncServiceInvocation getAsyncServiceInvocation()
    {
      return asyncSetRequest;
    }

  }

  private class RequestExecuteMethod extends Request
  {
    protected final CosemAsyncActionRequest actionRequest;
    protected final CosemMethod method;
    protected final IMethodResultListener methodResultListener;

    public RequestExecuteMethod(CosemAsyncActionRequest actionRequest, CosemMethod method,
                                IMethodResultListener methodResultListener)
    {
      this.actionRequest = actionRequest;
      this.method = method;
      this.methodResultListener = methodResultListener;
    }

    @Override
    protected void onFinished()
    {
      synchronized (actionRequests)
      {
        actionRequests.remove(method);
        switch (actionRequest.getState())
        {
          case FINISHED:
            method.setActionResponse(CosemMethod.ExecutionState.FINISHED, actionRequest.
                    getActionResponseWithOptionalData());
            break;
          default:
            method.setActionResponse(CosemMethod.ExecutionState.ERROR, null);
        }

        if (methodResultListener != null)
        {
          methodResultListener.methodResult(method, actionRequest.getActionResponseWithOptionalData());
        }
      }
    }

    @Override
    protected CosemAsyncServiceInvocation getAsyncServiceInvocation()
    {
      return actionRequest;
    }

  }

  private class RequestTask extends Request
  {
    protected final CosemAsyncTaskRequest asyncTask;
    protected final CosemTask task;
    //protected final IMethodResultListener methodResultListener;

    public RequestTask(CosemAsyncTaskRequest asyncTask, CosemTask task)
    {
      this.asyncTask = asyncTask;
      this.task = task;
    }

    @Override
    protected void onFinished()
    {
      synchronized (cosemTasks)
      {
        cosemTasks.remove(task);
        switch (asyncTask.getState())
        {
          case FINISHED:
            task.setExecutionState(ExecutionState.FINISHED);
            break;
            
          case ERROR:
            task.setErrorReason(asyncTask.getErrorReason());
            task.setExecutionState(ExecutionState.ERROR);
            break;
          default:
            task.setExecutionState(ExecutionState.ERROR);
        }

//        if (methodResultListener != null)
//        {
//          methodResultListener.methodResult(method, actionRequest.getActionResponseWithOptionalData());
//        }
      }
    }

    @Override
    protected CosemAsyncServiceInvocation getAsyncServiceInvocation()
    {
      return asyncTask;
    }

  }

}
