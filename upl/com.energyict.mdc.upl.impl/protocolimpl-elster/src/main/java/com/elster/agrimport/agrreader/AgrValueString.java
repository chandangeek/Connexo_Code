/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueString.java $
 * Version:     
 * $Id: AgrValueString.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 09:38:48
 */
package com.elster.agrimport.agrreader;

/**
 * This class saves an archive value as string.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrValueString extends AbstractAgrValue<String>
{
  public AgrValueString()
  {
  }

  AgrValueString(String value)
  {
    super(value);
  }

}



