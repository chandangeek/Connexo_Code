/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueDate.java $
 * Version:     
 * $Id: AgrValueDate.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 09:41:54
 */
package com.elster.agrimport.agrreader;

import java.util.Date;

/**
 * This class saves an archive value as date.
 *
 * @author osse
 */
public class AgrValueDate extends AbstractAgrValue<Date>
{
  AgrValueDate(Date value)
  {
    setValue(value);
  }

  public AgrValueDate()
  {
  }

}
