/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueBitString.java $
 * Version:     
 * $Id: BerValueBitString.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:30:30
 */
package com.elster.ber.types;

import com.elster.dlms.types.basic.BitString;

/**
 * BER Value for an {@link BitString }
 *
 * @author osse
 */
public class BerValueBitString extends BerValueBase<BitString>
{
  public BerValueBitString(BerId identifier, BitString value)
  {
    super(identifier, value);
  }

}
