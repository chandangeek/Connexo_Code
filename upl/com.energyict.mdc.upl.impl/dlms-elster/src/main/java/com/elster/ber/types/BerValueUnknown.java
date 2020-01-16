/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueUnknown.java $
 * Version:     
 * $Id: BerValueUnknown.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:32:45
 */
package com.elster.ber.types;

/**
 * Special BER Value to hold data for unknown types.
 *
 * @author osse
 */
public class BerValueUnknown extends BerValueOctetString
{
  public BerValueUnknown(final BerId identifier, final byte[] value)
  {
    super(identifier, value);
  }

}
