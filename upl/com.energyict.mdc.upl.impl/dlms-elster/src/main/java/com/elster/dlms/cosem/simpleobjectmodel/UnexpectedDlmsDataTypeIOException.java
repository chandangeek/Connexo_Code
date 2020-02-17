/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/UnexpectedDlmsDataTypeIOException.java $
 * Version:     
 * $Id: UnexpectedDlmsDataTypeIOException.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.09.2011 11:59:53
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import java.io.IOException;

/**
 * Exception to throw if an DLMS data type doesn't match an expected DLMS data type
 *
 * @author osse
 */
public class UnexpectedDlmsDataTypeIOException extends IOException
{

  public UnexpectedDlmsDataTypeIOException(final Throwable cause)
  {
    super();
    initCause(cause); //separated for jdk 1.5
  } 

  public UnexpectedDlmsDataTypeIOException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);//separated for jdk 1.5
  }

  public UnexpectedDlmsDataTypeIOException(String message)
  {
    super(message);
  }

  public UnexpectedDlmsDataTypeIOException()
  {
  }
  
  
}
