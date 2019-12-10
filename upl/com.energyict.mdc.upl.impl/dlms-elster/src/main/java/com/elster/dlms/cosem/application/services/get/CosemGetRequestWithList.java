/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetRequestWithList.java $
 * Version:     
 * $Id: CosemGetRequestWithList.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:50:23
 */

package com.elster.dlms.cosem.application.services.get;

import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 * This COSEM GET request "with list"
 *
 * @author osse
 */
public class CosemGetRequestWithList extends CosemGetRequest
{
  private final List<CosemAttributeDescriptor> attributeDescriptors= new ArrayList<CosemAttributeDescriptor>();

  public List<CosemAttributeDescriptor> getAttributeDescriptors()
  {
    return attributeDescriptors;
  }

  public RequestType getRequestType()
  {
    return RequestType.WITH_LIST;
  }

  @Override
  public String toString()
  {
    return "CosemGetRequestWithList{" + "invocationId=" + getInvocationId() + ", attributeDescriptors=" + attributeDescriptors + '}';
  }
  


}
