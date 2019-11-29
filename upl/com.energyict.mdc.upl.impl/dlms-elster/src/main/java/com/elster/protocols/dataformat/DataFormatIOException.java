/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/dataformat/DataFormatIOException.java $
 * Version:     
 * $Id: DataFormatIOException.java 3050 2011-06-07 12:50:30Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 09:57:08
 */

package com.elster.protocols.dataformat;

import java.io.IOException;

/**
 * This exception will be thrown if an parity error occurs.
 *
 * @author osse
 */
public class DataFormatIOException extends IOException
{

  public DataFormatIOException(String message)
  {
    super(message);
  }

  public DataFormatIOException()
  {
  }
}
