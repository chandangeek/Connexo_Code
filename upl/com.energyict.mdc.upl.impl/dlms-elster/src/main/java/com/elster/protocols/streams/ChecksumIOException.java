/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/ChecksumIOException.java $
 * Version:     
 * $Id: ChecksumIOException.java 3050 2011-06-07 12:50:30Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 10:00:40
 */

package com.elster.protocols.streams;

import java.io.IOException;

/**
 * This exception will be thrown if an incorrect checksum was detected.
 *
 * @author osse
 */
public class ChecksumIOException extends IOException
{

  public ChecksumIOException(String message)
  {
    super(message);
  }

  public ChecksumIOException()
  {
  }


}
