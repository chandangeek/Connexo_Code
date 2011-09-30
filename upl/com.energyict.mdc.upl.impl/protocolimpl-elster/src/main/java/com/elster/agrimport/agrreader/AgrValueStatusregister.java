/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueStatusregister.java $
 * Version:     
 * $Id: AgrValueStatusregister.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 10:19:23
 */
package com.elster.agrimport.agrreader;

import java.util.List;

/**
 * This class represents an status register.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrValueStatusregister extends AbstractAgrValue<List<Integer>>
{
  public AgrValueStatusregister()
  {
  }

  AgrValueStatusregister(List<Integer> stateList)
  {
    setValue(stateList);
  }

  public short shortValue()
  {
    short shortValue = 0;


    for (Integer i : getValue())
    {
      if (i > 0)
      {
        shortValue |= 1 << (i - 1);
      }
    }

    return shortValue;
  }

}
