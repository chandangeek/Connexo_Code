/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderUnsigned16.java $
 * Version:     
 * $Id: AXdrCoderUnsigned16.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:12:08
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes 16 bit unsigned integers.
 *
 * @author osse
 */
public class AXdrCoderUnsigned16 extends AbstractAXdrCoder<Integer>
{

  @Override
  public void encodeObject(Integer object, AXdrOutputStream out) throws IOException
  {
    out.writeUnsigned16(object);
  }

  @Override
  public Integer decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readUnsigned16();
  }

}
