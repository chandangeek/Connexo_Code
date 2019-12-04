/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemAsyncSetRequest.java $
 * Version:     
 * $Id: CosemAsyncSetRequest.java 2887 2011-05-02 17:07:35Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 28, 2010 4:36:41 PM
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsData;

/**
 * Handler for an asynchronous SET invocation.
 *
 * @author osse
 */
public class CosemAsyncSetRequest extends CosemAsyncServiceInvocation
{
  private final CosemAttributeDescriptor attributeDescriptor;
  private final DlmsData data;
  private DataAccessResult dataAccessResult;


  public CosemAsyncSetRequest(CosemAttributeDescriptor attributeDescriptor, DlmsData data)
  {
    this.attributeDescriptor = attributeDescriptor;
    this.data= data;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.SET;
  }

  public CosemAttributeDescriptor getAttributeDescriptor()
  {
    return attributeDescriptor;
  }

  public DataAccessResult getDataAccessResult()
  {
    return dataAccessResult;
  }

  void setDataAccessResult(DataAccessResult dataAccessResult)
  {
    this.dataAccessResult = dataAccessResult;
  }

  public DlmsData getData()
  {
    return data;
  }

  





}
