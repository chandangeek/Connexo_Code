/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderObjectIdentifer.java $
 * Version:     
 * $Id: BerDecoderObjectIdentifer.java 1836 2010-08-06 17:08:57Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 16:53:46
 */

package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueObjectIdentifer;
import java.io.IOException;

/**
 * Decodes object identifiers.
 *
 * @author osse
 */
public class BerDecoderObjectIdentifer extends BerDecoderBase<BerValueObjectIdentifer>
{

  @Override
  public BerValueObjectIdentifer decode(BerId id, BerInputStream in) throws IOException
  {
    if (id==null)
    {
      id= in.readIdentifier();
    }
    return new BerValueObjectIdentifer(id,in.readObjectIdentifer());
  }

}
