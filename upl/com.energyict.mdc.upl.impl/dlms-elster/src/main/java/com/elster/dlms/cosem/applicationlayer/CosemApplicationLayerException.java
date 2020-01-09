/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemApplicationLayerException.java $
 * Version:     
 * $Id: CosemApplicationLayerException.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 16, 2011 1:48:18 PM
 */

package com.elster.dlms.cosem.applicationlayer;

import java.io.IOException;

/**
 * Exception in the application layer.
 *
 * @author osse
 */
public class CosemApplicationLayerException extends IOException
{
  public CosemApplicationLayerException(String message)
  {
    super(message);
  }

  public CosemApplicationLayerException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);
  }

  public CosemApplicationLayerException()
  {
  }



}
