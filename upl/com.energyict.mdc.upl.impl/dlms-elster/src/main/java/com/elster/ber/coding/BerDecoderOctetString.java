/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderOctetString.java $
 * Version:     
 * $Id: BerDecoderOctetString.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 15:40:20
 */

package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueOctetString;
import java.io.IOException;

/**
 * Decodes octet strings.
 *
 * @author osse
 */
public class BerDecoderOctetString extends BerDecoderBase<BerValueOctetString>
{

  @Override
  public BerValueOctetString decode(BerId id, BerInputStream in) throws IOException
  {
    if (id==null)
    {
      id= in.readIdentifier();
    }
    return new BerValueOctetString(id,in.readOctetString());
  }

}
