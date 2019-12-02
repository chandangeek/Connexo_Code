/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/security/CipherException.java $
 * Version:     
 * $Id: CipherException.java 4022 2012-02-16 17:07:53Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 18, 2011 3:35:48 PM
 */

package com.elster.dlms.security;

import java.io.IOException;

/**
 * Exception for errors during ciphering.
 *
 * @author osse
 */
public class CipherException extends IOException
{

  public CipherException(final String message)
  {
    super(message);
  }

  public CipherException()
  {
    super();
  }

  public CipherException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);
  }
  
  

}
