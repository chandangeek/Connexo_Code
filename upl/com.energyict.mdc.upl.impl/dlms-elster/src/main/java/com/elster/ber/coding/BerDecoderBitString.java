/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderBitString.java $
 * Version:     
 * $Id: BerDecoderBitString.java 1836 2010-08-06 17:08:57Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 15:40:20
 */

package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueBitString;
import com.elster.dlms.types.basic.BitString;
import java.io.IOException;

/**
 * Decoder for {@link BitString}s.
 *
 * @author osse
 */
public class BerDecoderBitString extends BerDecoderBase<BerValueBitString>
{

  @Override
  public BerValueBitString decode(BerId id, BerInputStream in) throws IOException
  {
    if (id==null)
    {
      id= in.readIdentifier();
    }
    return new BerValueBitString(id,in.readBitString());

  }

}
