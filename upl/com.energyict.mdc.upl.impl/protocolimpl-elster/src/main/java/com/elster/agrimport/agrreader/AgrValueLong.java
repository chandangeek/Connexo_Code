/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueLong.java $
 * Version:     
 * $Id: AgrValueLong.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 10:31:24
 */
package com.elster.agrimport.agrreader;

/**
 * This class saves an archive value as long.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrValueLong extends AbstractAgrValue<Long>
{
  AgrValueLong(long value)
  {
    setValue(value);
  }

  public AgrValueLong()
  {
  }

}

