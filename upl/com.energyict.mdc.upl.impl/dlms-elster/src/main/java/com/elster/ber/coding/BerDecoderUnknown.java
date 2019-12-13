/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderUnknown.java $
 * Version:     
 * $Id: BerDecoderUnknown.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 14:25:47
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValue;
import com.elster.ber.types.BerValueUnknown;
import java.io.IOException;

/**
 * Default decoder for unknown types.<P>
 * Simply reads the value as an octet string to an {@link BerValueUnknown}.
 *
 * @author osse
 */
public class BerDecoderUnknown extends BerDecoderBase<BerValue>
{
  @Override
  public BerValue decode(BerId id, BerInputStream in) throws IOException
  {
    if (id == null)
    {
      id = in.readIdentifier();
    }
    return new BerValueUnknown(id, in.readOctetString());
  }

}
