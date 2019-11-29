/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcProtocolException.java $
 * Version:     
 * $Id: HdlcProtocolException.java 3050 2011-06-07 12:50:30Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.05.2010 10:09:22
 */

package com.elster.protocols.hdlc;

import java.io.IOException;

/**
 * This class will be thrown if an error occurs in the HDLC protocol.
 *
 * @author osse
 */
public class HdlcProtocolException extends IOException
{
  public HdlcProtocolException(String message)
  {
    super(message);
  }

  public HdlcProtocolException()
  {
  }


}
