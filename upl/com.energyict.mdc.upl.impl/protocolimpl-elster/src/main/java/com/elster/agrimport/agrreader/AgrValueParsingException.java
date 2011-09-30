/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueParsingException.java $
 * Version:     
 * $Id: AgrValueParsingException.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 11:29:15
 */
package com.elster.agrimport.agrreader;

/**
 * This Exception will be thrown if an exception occurs parsing a archive value.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrValueParsingException extends AgrFileException
{
  public AgrValueParsingException(Throwable cause)
  {
    super(cause);
  }

  public AgrValueParsingException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AgrValueParsingException(String message)
  {
    super(message);
  }

  public AgrValueParsingException()
  {
  }

}
