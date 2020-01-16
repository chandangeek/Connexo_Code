/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderBitString.java $
 * Version:     
 * $Id: AXdrCoderBitString.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.04.2010 10:57:54
 */
package com.elster.axdr.coding;

import com.elster.dlms.types.basic.BitString;
import java.io.IOException;

/**
 * This class decodes and encodes AXdrBitStrings
 *
 * @author osse
 */
public class AXdrCoderBitString extends AbstractAXdrCoder<BitString>
{

  @Override
  public void encodeObject(BitString object, AXdrOutputStream out) throws IOException
  {
    out.writeBitString(object);
  }

  @Override
  public BitString decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readBitString();
  }

}
