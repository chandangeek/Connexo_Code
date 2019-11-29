/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderOctetStringVariableLength.java $
 * Version:     
 * $Id: AXdrCoderOctetStringVariableLength.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 17:04:47
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class decodes and encodes octet strings with a variable length.<P>
 * Variable length means that the length itself will be encoded to/ decoded from the streams.
 *
 * @author osse
 */
public class AXdrCoderOctetStringVariableLength extends AbstractAXdrCoder<byte[]>
{

  @Override
  public void encodeObject(byte[] object, AXdrOutputStream out) throws IOException
  {
    out.writeOctetStringVariableLength(object);
  }

  @Override
  public byte[] decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readOctetString();
  }

}
