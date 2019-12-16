/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetRequestNormal.java $
 * Version:     
 * $Id: CosemGetRequestNormal.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:50:23
 */

package com.elster.dlms.cosem.application.services.get;

import com.elster.dlms.types.basic.CosemAttributeDescriptor;

/**
 * This COSEM GET request "normal"
 *
 * @author osse
 */
public class CosemGetRequestNormal extends CosemGetRequest
{

  private CosemAttributeDescriptor attributeDescriptor;

  public CosemAttributeDescriptor getAttributeDescriptor()
  {
    return attributeDescriptor;
  }

  public void setAttributeDescriptor(CosemAttributeDescriptor attributeDescriptor)
  {
    this.attributeDescriptor = attributeDescriptor;
  }

  public RequestType getRequestType()
  {
    return RequestType.NORMAL;
  }


  @Override
  public String toString()
  {
    return "CosemGetRequestNormal{" + "invocationId=" + getInvocationId() + ", attributeDescriptor=" + attributeDescriptor + '}';
  }



}
