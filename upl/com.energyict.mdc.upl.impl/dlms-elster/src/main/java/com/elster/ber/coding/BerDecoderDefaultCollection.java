/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderDefaultCollection.java $
 * Version:     
 * $Id: BerDecoderDefaultCollection.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 14:07:35
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;

/**
 * Simple collection decoder.<P>
 * Decodes "universal" TLVs with the {@link BerDecoderUniversal} and "constructed" TLVs
 * with an {@code BerDecoderDefaultCollection}.<P>
 * All other TLVs will be decoded by the {@code BerDecoderUnknown}.
 *
 * @author osse
 */
public class BerDecoderDefaultCollection extends BerDecoderCollection
{
  private static final BerDecoderUniversal CODING_UNIVERSAL = new BerDecoderUniversal();
  private static final BerDecoderDefaultCollection CODING_COLLECTION = new BerDecoderDefaultCollection();
  private static final BerDecoderUnknown CODING_UNKWNON = new BerDecoderUnknown();

  @Override
  protected BerDecoder getDecoder(BerId id)
  {
    BerDecoder result = null;

    if (!id.isConstructed() && id.getTag() == BerId.Tag.UNIVERSAL)
    {
      result = CODING_UNIVERSAL;
    }
    else if (id.isConstructed())
    {
      result = CODING_COLLECTION;
    }
    else
    {
      result = CODING_UNKWNON;
    }

    return result;
  }

}
