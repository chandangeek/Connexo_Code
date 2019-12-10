/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderCollection.java $
 * Version:     
 * $Id: BerDecoderCollection.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 12:03:17
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerCollection;
import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValue;
import java.io.IOException;

/**
 * Abstract base class for decoding collections.
 * <P>
 * A collection can be a Sequence, a Choice or simple wrapper for a single value.
 *
 * @author osse
 */
public abstract class BerDecoderCollection extends BerDecoderBase<BerCollection>
{
  @Override
  public BerCollection decode(BerId id, BerInputStream in) throws IOException
  {
    if (id == null)
    {
      id = in.readIdentifier();
    }

    BerCollection collection = new BerCollection(id);

    int contentLength = in.readLength();

    //children = new ArrayList<BerBase>();
    int expectedContentEnd = in.getPosition() + contentLength;

    while (expectedContentEnd > in.getPosition())
    {
      BerId nextIdentifier = in.readIdentifier();
      BerDecoder childDecoder = getDecoder(nextIdentifier);

      if (childDecoder == null)
      {
        throw new IOException("No decoder available for id: " + nextIdentifier);
      }

      BerValue child = childDecoder.decode(nextIdentifier, in);
      collection.add(child);
    }

    return collection;
  }

  /**
   * Method to find an decoder for an read id.
   *
   * @param id The id for the decoder.
   * @return The decoder for the id.
   */
  protected abstract BerDecoder getDecoder(BerId id);

}
