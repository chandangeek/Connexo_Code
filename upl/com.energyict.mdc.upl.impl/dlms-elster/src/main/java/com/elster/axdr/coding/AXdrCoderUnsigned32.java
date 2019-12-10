/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderUnsigned32.java $
 * Version:     
 * $Id: AXdrCoderUnsigned32.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.04.2010 16:34:21
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes 32 bit integers.
 *
 * @author osse
 */
public class AXdrCoderUnsigned32 extends AbstractAXdrCoder<Long>
{

  @Override
  public void encodeObject(Long object, AXdrOutputStream out) throws IOException
  {
    out.writeUnsigned32(object);
  }

  @Override
  public Long decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readUnsigned32();
  }

}


