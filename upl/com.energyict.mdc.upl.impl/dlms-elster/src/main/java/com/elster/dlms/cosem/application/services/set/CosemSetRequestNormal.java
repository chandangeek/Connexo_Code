/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/set/CosemSetRequestNormal.java $
 * Version:     
 * $Id: CosemSetRequestNormal.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:50:23
 */

package com.elster.dlms.cosem.application.services.set;

import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsData;


/**
 * The set request "normal".
 *
 * @author osse
 */
public class CosemSetRequestNormal extends CosemSetRequest
{

  private CosemAttributeDescriptor attributeDescriptor;
  private DlmsData data;

  public CosemAttributeDescriptor getAttributeDescriptor()
  {
    return attributeDescriptor;
  }

  public void setAttributeDescriptor(CosemAttributeDescriptor attributeDescriptor)
  {
    this.attributeDescriptor = attributeDescriptor;
  }

  @Override
  public RequestType getRequestType()
  {
    return RequestType.NORMAL;
  }

  public DlmsData getData()
  {
    return data;
  }

  public void setData(DlmsData data)
  {
    this.data = data;
  }

  @Override
  public String toString()
  {
    return "CosemSetRequestNormal{" + "invocationId=" + getInvocationId() + ", attributeDescriptor=" + attributeDescriptor + "data=" + data + '}';
  }

}
