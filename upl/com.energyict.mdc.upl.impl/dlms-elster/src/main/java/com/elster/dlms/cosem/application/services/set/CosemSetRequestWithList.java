/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/set/CosemSetRequestWithList.java $
 * Version:     
 * $Id: CosemSetRequestWithList.java 5118 2012-09-07 12:58:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:50:23
 */

package com.elster.dlms.cosem.application.services.set;

import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.util.ArrayList;
import java.util.List;

/**
 * This COSEM SET request "with list"
 *
 * @author osse
 */
public class CosemSetRequestWithList extends CosemSetRequest
{
  private final List<CosemAttributeDescriptor> attributeDescriptors= new ArrayList<CosemAttributeDescriptor>();
  private final List<DlmsData> values= new ArrayList<DlmsData>();

  public List<CosemAttributeDescriptor> getAttributeDescriptors()
  {
    return attributeDescriptors;
  }

  public List<DlmsData> getValues()
  {
    return values;
  }

  public RequestType getRequestType()
  {
    return RequestType.WITH_LIST;
  }

  @Override
  public String toString()
  {
    return "CosemSetRequestWithList{" + "attributeDescriptors=" + attributeDescriptors + ", values=" + values +
           '}';
  }


}
