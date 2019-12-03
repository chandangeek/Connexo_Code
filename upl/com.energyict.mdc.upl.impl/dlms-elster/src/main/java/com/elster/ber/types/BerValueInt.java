/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueInt.java $
 * Version:     
 * $Id: BerValueInt.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:27:08
 */
package com.elster.ber.types;

/**
 * BER Value for an integer.
 *
 * @author osse
 */
public class BerValueInt extends BerValueBase<Integer>
{
  public BerValueInt(BerId identifier, Integer value)
  {
    super(identifier, value);
  }

}
