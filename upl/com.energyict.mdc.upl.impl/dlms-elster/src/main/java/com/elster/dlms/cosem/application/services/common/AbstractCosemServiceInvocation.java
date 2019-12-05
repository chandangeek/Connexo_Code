/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/common/AbstractCosemServiceInvocation.java $
 * Version:     
 * $Id: AbstractCosemServiceInvocation.java 3981 2012-01-31 11:10:13Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 18, 2011 5:02:47 PM
 */
package com.elster.dlms.cosem.application.services.common;

/**
 * Base class for COSEM service objects.
 *
 * @author osse
 */
public abstract class AbstractCosemServiceInvocation
{
  public enum ServiceType
  {
    OPEN, GET, SET, ACTION, INITIATE, SERVICE_ERROR, RELEASE
  };

  public enum ServiceInvocationType
  {
    REQUEST, RESPONSE
  };
  
  
  public enum ServiceInvocation
  {
    OPEN_REQUEST,OPEN_RESPONSE ,GET_REQUEST,GET_RESPONSE, SET_REQUEST, SET_RESPONSE, ACTION_REQUEST, ACTION_RESPONSE, INITIATE_REQUEST, INITIATE_RESPONSE, SERVICE_ERROR, RELEASE_REQUEST, RELEASE_RESPONSE
  }



  private SecurityControlField securityControlField;

  public SecurityControlField getSecurityControlField()
  {
    return securityControlField;
  }

  public void setSecurityControlField(final SecurityControlField securityControlField)
  {
    this.securityControlField = securityControlField;
  }

  public abstract ServiceType getServiceType();

  public abstract ServiceInvocationType getServiceInvocationType();

  public abstract ServiceInvocation getServiceInvocation();

}
