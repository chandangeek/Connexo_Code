/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderBase.java $
 * Version:     
 * $Id: BerDecoderBase.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 15, 2012 1:56:22 PM
 */

package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValue;
import java.io.IOException;
import java.io.InputStream;

/**
 * Basic decoder for {@code BerValue}s.
 *
 * @author osse
 */
public abstract class BerDecoderBase<T extends BerValue> extends BerDecoder
{

  @Override
  public abstract T decode(BerId id, BerInputStream in) throws IOException;
   
  @Override 
  public final T decode(final InputStream in) throws IOException
  {
    if (in instanceof BerInputStream)
    {
      return decode(null,(BerInputStream) in);
    }
    else
    {
      return decode(null,new BerInputStream(in));
    }
  }  

}
