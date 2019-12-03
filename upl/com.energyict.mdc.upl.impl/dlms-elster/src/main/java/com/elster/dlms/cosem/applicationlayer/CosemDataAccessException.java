/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemDataAccessException.java $
 * Version:     
 * $Id: CosemDataAccessException.java 3163 2011-07-01 14:17:40Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  01.07.2011 15:40:06
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;

/**
 * Exception to handle data access errors.
 *
 * @author osse
 */
public class CosemDataAccessException extends CosemApplicationLayerException
{
  private final DataAccessResult dataAccessResult;

  public CosemDataAccessException(DataAccessResult dataAccessResult)
  {
    this.dataAccessResult = dataAccessResult;
  }

  public CosemDataAccessException(String message, DataAccessResult dataAccessResult)
  {
    super(message);
    this.dataAccessResult = dataAccessResult;
  }

  public DataAccessResult getDataAccessResult()
  {
    return dataAccessResult;
  }
  
  
    
}
