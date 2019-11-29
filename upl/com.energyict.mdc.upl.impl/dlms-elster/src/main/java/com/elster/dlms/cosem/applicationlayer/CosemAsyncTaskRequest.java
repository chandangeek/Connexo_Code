/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemAsyncTaskRequest.java $
 * Version:     
 * $Id: CosemAsyncTaskRequest.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 28, 2010 4:36:41 PM
 */
package com.elster.dlms.cosem.applicationlayer;

/**
 * Enqueues the executer in the Queue.<P>
 *
 * @author osse
 */
public class CosemAsyncTaskRequest extends CosemAsyncServiceInvocation
{
  private final ICosemApplicationLayerTask executor;

  public CosemAsyncTaskRequest(ICosemApplicationLayerTask executor)
  {
    this.executor = executor;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.TASK;
  }

  public ICosemApplicationLayerTask getExecutor()
  {
    return executor;
  }

}
