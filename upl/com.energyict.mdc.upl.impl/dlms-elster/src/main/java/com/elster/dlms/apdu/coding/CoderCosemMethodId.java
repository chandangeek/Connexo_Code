/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderCosemMethodId.java $
 * Version:     
 * $Id: CoderCosemMethodId.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 10:54:53
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrCoderInteger8;

/**
 * En-/decoder for the "COSEM method id".
 *
 * @author osse
 */
public class CoderCosemMethodId extends AXdrCoderInteger8
{
  public CoderCosemMethodId()
  {
    //nothing to do.
  }
}
