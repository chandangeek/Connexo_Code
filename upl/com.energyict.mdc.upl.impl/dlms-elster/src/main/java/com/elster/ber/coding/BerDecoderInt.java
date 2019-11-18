/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderInt.java $
 * Version:     
 * $Id: BerDecoderInt.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 15:40:20
 */

package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueInt;
import java.io.IOException;

/**
 * Decodes integers
 *
 * @author osse
 */
public class BerDecoderInt extends BerDecoderBase<BerValueInt>
{

  @Override
  public BerValueInt decode(BerId id, BerInputStream in) throws IOException
  {
    if (id==null)
    {
      id= in.readIdentifier();
    }
    return new BerValueInt(id,in.readInt());
  }

}
