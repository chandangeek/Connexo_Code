/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerEncoder.java $
 * Version:     
 * $Id: BerEncoder.java 1836 2010-08-06 17:08:57Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  05.08.2010 17:09:58
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerCollection;
import com.elster.ber.types.BerValue;
import com.elster.ber.types.BerValueBitString;
import com.elster.ber.types.BerValueGraphicString;
import com.elster.ber.types.BerValueInt;
import com.elster.ber.types.BerValueObjectIdentifer;
import com.elster.ber.types.BerValueOctetString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Encoder for BER values.
 *
 * @author osse
 */
public class BerEncoder
{
  /**
   * Writes the specified {@link BerValue } to the output stream.
   *
   * @param out The output stream
   * @param value The value to write.
   * @throws IOException
   */
  public void encode(OutputStream out, BerValue value) throws IOException
  {
    encode(new BerOutputStream(out), value);
  }

  public void encode(BerOutputStream out, BerValue value) throws IOException
  {
    out.writeIdentifier(value.getIdentifier());

    if (value instanceof BerCollection)
    {
      encodeCollection(out, (BerCollection) value);
    }
    else if (value instanceof BerValueBitString)
    {
      out.writeBitString(((BerValueBitString)value).getValue());
    }
    else if (value instanceof BerValueGraphicString)
    {
      out.writeGraphicString(((BerValueGraphicString)value).getValue());
    }
    else if (value instanceof BerValueInt)
    {
      out.writeInt(((BerValueInt)value).getValue());
    }
    else if (value instanceof BerValueObjectIdentifer)
    {
      out.writeObjectIdentifier(((BerValueObjectIdentifer)value).getValue());
    }
    else if (value instanceof BerValueOctetString)
    {
      out.writeOctetString(((BerValueOctetString)value).getValue());
    }
    else
    {
      throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getSimpleName());
    }
  }

  private void encodeCollection(BerOutputStream out, BerCollection collection) throws IOException
  {
    ByteArrayOutputStream tempByteArrayOut= new ByteArrayOutputStream();
    BerOutputStream tempBerOut= new BerOutputStream(tempByteArrayOut);

    for (BerValue valueInCollection : collection)
    {
      encode(tempBerOut, valueInCollection);
    }
    tempBerOut.close();

    byte[] bytes= tempByteArrayOut.toByteArray();

    out.writeLength(bytes.length);
    out.write(bytes);

    tempByteArrayOut.close();
  }

}
