/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderUnsigned8.java $
 * Version:     
 * $Id: AXdrCoderUnsigned8.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 16:57:26
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes 8 bit unsigned integers.
 *
 * @author osse
 */
public class AXdrCoderUnsigned8 extends AbstractAXdrCoder<Integer>
{

  @Override
  public void encodeObject(Integer object, AXdrOutputStream out) throws IOException
  {
    out.writeUnsigned8(object);
  }

  @Override
  public Integer decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readUnsigned8();
  }

}
