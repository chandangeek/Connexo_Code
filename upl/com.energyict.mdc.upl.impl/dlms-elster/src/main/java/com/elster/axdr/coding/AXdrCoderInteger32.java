/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderInteger32.java $
 * Version:     
 * $Id: AXdrCoderInteger32.java 3598 2011-09-29 09:10:44Z osse $
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
public class AXdrCoderInteger32 extends AbstractAXdrCoder<Integer>
{
  @Override
  public void encodeObject(Integer object, AXdrOutputStream out) throws IOException
  {
    out.writeInteger32(object);
  }

  @Override
  public Integer decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readInteger32();
  }

}
