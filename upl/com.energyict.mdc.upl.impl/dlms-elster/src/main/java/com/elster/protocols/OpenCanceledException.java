/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.04.2013 15:34:03
 */

package com.elster.protocols;

import java.io.IOException;

/**
 * Exception to indicate that  {@link IProtocol#open()} was canceled.
 *
 * @author osse
 */
public class OpenCanceledException extends IOException
{

  public OpenCanceledException()
  {
  }

  public OpenCanceledException(String message)
  {
    super(message);
  }

  public OpenCanceledException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);
  }

  public OpenCanceledException(Throwable cause)
  {
    super();
    initCause(cause);
  }
  

}
