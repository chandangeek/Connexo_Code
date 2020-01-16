/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetResponseWithList.java $
 * Version:     
 * $Id: CosemGetResponseWithList.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */
package com.elster.dlms.cosem.application.services.get;

import java.util.ArrayList;
import java.util.List;

/**
 * This COSEM GET response "with list"
 *
 * @author osse
 */
public class CosemGetResponseWithList extends CosemGetResponse
{
  private final List<GetDataResult> getDataResults = new ArrayList<GetDataResult>();
  
  @Override
  public ResponseType getResponseType()
  {
    return ResponseType.WITH_LIST;
  }

  public List<GetDataResult> getGetDataResults()
  {
    return getDataResults;
  }

}
