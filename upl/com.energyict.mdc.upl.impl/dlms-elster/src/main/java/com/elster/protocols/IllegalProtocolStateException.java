/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/IllegalProtocolStateException.java $
 * Version:     
 * $Id: IllegalProtocolStateException.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 24, 2011 5:20:18 PM
 */

package com.elster.protocols;

import java.io.IOException;

/**
 * Exception to throw on unexpected protocol states.
 *
 * @author osse
 */
public class IllegalProtocolStateException extends IOException
{
  private final ProtocolState expected;
  private final ProtocolState actual;

  public IllegalProtocolStateException(ProtocolState expected, ProtocolState actual)
  {
    super("Unexpected protocol state. Expected:"+expected.toString()+", Actual:"+actual.toString());
    this.expected = expected;
    this.actual = actual;
  }

  public ProtocolState getActual()
  {
    return actual;
  }

  public ProtocolState getExpected()
  {
    return expected;
  }

}
