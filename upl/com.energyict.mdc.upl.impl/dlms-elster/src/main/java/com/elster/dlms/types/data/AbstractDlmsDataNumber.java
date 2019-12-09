/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/AbstractDlmsDataNumber.java $
 * Version:     
 * $Id: AbstractDlmsDataNumber.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 15, 2012 4:36:47 PM
 */

package com.elster.dlms.types.data;

import java.math.BigDecimal;

/**
 * Base class for all DLMS number types.
 * (Preparation for better access to the concrete value)
 *
 * @author osse
 */
public abstract class AbstractDlmsDataNumber extends DlmsData
{

  //Only subtypes in package allowed
  AbstractDlmsDataNumber()
  {
    super();
  }
  
  public abstract BigDecimal bigDecimalValue();
  
  
  @Override
  public abstract Number getValue();
  
}
