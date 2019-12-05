/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderOctetStringFixLength.java $
 * Version:     
 * $Id: AXdrCoderOctetStringFixLength.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:24:00
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class decodes and encodes octet strings with a fixed (predefined) length.
 *
 * @author osse
 */
public class AXdrCoderOctetStringFixLength extends AbstractAXdrCoder<byte[]>
{
  private final int length;

  public AXdrCoderOctetStringFixLength(int length)
  {
    this.length = length;
  }

  @Override
  public void encodeObject(byte[] object, AXdrOutputStream out) throws IOException
  {
    if (object.length != length)
    {
      throw new IllegalArgumentException("Wrong length of the byte array. Expected: " + length + " Actual:"
                                         + object.length);
    }
    out.writeOctetStringFixLength(object);
  }

  @Override
  public byte[] decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readOctetString(length);
  }

}
