/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueInt.java $
 * Version:     
 * $Id: AgrValueInt.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 10:14:34
 */
package com.elster.agrimport.agrreader;

/**
 * This class save an archive value as int
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrValueInt extends AbstractAgrValue<Integer>
{
  public AgrValueInt()
  {
  }

  AgrValueInt(int value)
  {
    setValue(value);
  }

}
