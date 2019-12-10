/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderUniversal.java $
 * Version:     
 * $Id: BerDecoderUniversal.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:16:43
 */
package com.elster.ber.coding;

import com.elster.ber.types.*;
import java.io.IOException;

/**
 * Decoder for the "universal" types.
 *
 * @author osse
 */
public class BerDecoderUniversal extends BerDecoderBase<BerValue>
{
  public static final int TAG_NO_INT = 2;
  public static final int TAG_NO_BITSTRING = 3;
  public static final int TAG_NO_OCTETSTRING = 4;
  public static final int TAG_NO_OBJECT_IDENTIFIER = 6;
  public static final int TAG_NO_GRAPHIC_STRING = 25;

  @Override
  public BerValue decode(BerId id, BerInputStream in) throws IOException
  {
    if (id == null)
    {
      id = in.readIdentifier();
    }

    if (id.getTag() != BerId.Tag.UNIVERSAL)
    {
      throw new IOException("Value must be UNIVERSAL to use this decoder: " + id);
    }

    if (id.isConstructed())
    {
      throw new IOException("ID must not be constructed to use this decoder: " + id);
    }

    BerValue result = null;

    if (id.getTagNumber() > 100)
    {
      result = new BerValueUnknown(id, in.readOctetString());
    }
    else
    {
      switch ((int)id.getTagNumber())
      {
        case TAG_NO_INT:
          result = new BerValueInt(id, in.readInt());
          break;
        case TAG_NO_BITSTRING:
          result = new BerValueBitString(id, in.readBitString());
          break;
        case TAG_NO_OCTETSTRING:
          result = new BerValueOctetString(id, in.readOctetString());
          break;
        case TAG_NO_OBJECT_IDENTIFIER:
          result = new BerValueObjectIdentifer(id, in.readObjectIdentifer());
          break;
        case TAG_NO_GRAPHIC_STRING:
          result = new BerValueGraphicString(id, in.readGraphicString());
          break;
        default:
          result = new BerValueUnknown(id, in.readOctetString());
      }
    }
    return result;

  }

}
