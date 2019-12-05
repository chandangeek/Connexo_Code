/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderInteger64.java $
 * Version:     
 * $Id: AXdrCoderInteger64.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:32:10
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes 32 bit integers.
 *
 * @author osse
 */
public class AXdrCoderInteger64 extends AbstractAXdrCoder<Long>
{
  @Override
  public void encodeObject(Long object, AXdrOutputStream out) throws IOException
  {
    out.writeInteger64(object);
  }

  @Override
  public Long decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readInteger64();
  }

}
