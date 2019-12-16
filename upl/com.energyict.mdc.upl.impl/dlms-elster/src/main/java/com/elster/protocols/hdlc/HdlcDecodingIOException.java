/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcDecodingIOException.java $
 * Version:     
 * $Id: HdlcDecodingIOException.java 3050 2011-06-07 12:50:30Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 17:43:43
 */

package com.elster.protocols.hdlc;

import java.io.IOException;

/**
 * This class exception will be thrown if an exception occurs during decoding hdlc frames.
 *
 * @author osse
 */
public class HdlcDecodingIOException extends IOException
{

  public HdlcDecodingIOException(String message)
  {
    super(message);
  }

  public HdlcDecodingIOException()
  {
  }

}
