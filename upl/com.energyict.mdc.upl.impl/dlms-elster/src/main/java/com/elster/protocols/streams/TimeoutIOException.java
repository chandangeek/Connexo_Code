/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/TimeoutIOException.java $
 * Version:     
 * $Id: TimeoutIOException.java 3050 2011-06-07 12:50:30Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 14:45:03
 */

package com.elster.protocols.streams;

import java.io.IOException;

/**
 * This class will be thrown if an timeout occurs.
 *
 * @author osse
 */
public class TimeoutIOException extends IOException
{

  public TimeoutIOException(String message)
  {
    super(message);
  }

  public TimeoutIOException()
  {
  }


}
