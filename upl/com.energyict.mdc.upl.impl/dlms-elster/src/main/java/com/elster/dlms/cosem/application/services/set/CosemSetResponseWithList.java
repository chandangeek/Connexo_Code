/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/set/CosemSetResponseWithList.java $
 * Version:     
 * $Id: CosemSetResponseWithList.java 5122 2012-09-10 09:54:11Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */

package com.elster.dlms.cosem.application.services.set;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import java.util.ArrayList;
import java.util.List;

/**
 * The COSEM set response "with list".
 *
 * @author osse
 */
public class CosemSetResponseWithList extends CosemSetResponse
{
  private final List<DataAccessResult> dataAccessResults= new ArrayList<DataAccessResult>();

  @Override
  public ResponseType getResponseType()
  {
    return ResponseType.WITH_LIST;
  }


  public  List<DataAccessResult> getDataAccessResults()
  {
    return dataAccessResults;
  }

  @Override
  public String toString()
  {
    return "CosemSetResponseWithList{" + "dataAccessResults=" + dataAccessResults + '}';
  }


}
