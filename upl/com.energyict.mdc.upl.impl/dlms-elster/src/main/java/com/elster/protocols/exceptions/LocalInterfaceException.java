/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/exceptions/LocalInterfaceException.java $
 * Version:     
 * $Id: LocalInterfaceException.java 5724 2012-12-11 15:07:48Z SchulteM $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  01.08.2011 12:21:31
 */
package com.elster.protocols.exceptions;

import java.io.IOException;

/**
 * Exception to throw on errors of local interfaces (like the COM port)
 *
 * @author osse
 */
public class LocalInterfaceException extends IOException
{
  public enum DetailedReason{OPEN, INIT, OTHER};
  
  private final String interfaceName;
  private final DetailedReason reason;


  public LocalInterfaceException(final String interfaceName,DetailedReason detail, final String message)
  {
    super(message);
    this.interfaceName=interfaceName;
    this.reason= detail;
  }

  public String getInterfaceName()
  {
    return interfaceName;
  }
  
  public DetailedReason getReason()
  {
    return reason;
  }
  
  
  

}