/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrFileException.java $
 * Version:     
 * $Id: AgrFileException.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.07.2009 12:27:25
 */
package com.elster.agrimport.agrreader;

/**
 * This exception will be raised by the AgrReader, if an exception occurs during reading an
 * AGR file.
 *
 * @author osse
 */
public class AgrFileException extends Exception
{
  public AgrFileException()
  {
    super();
  }

  public AgrFileException(final String message)
  {
    super(message);
  }

  public AgrFileException(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public AgrFileException(final Throwable cause)
  {
    super(cause);
  }

}
