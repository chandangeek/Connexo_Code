/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderInteger.java $
 * Version:     
 * $Id: AXdrCoderInteger.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.04.2010 16:52:53
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes integers.
 *
 * @author osse
 */
public class AXdrCoderInteger extends AbstractAXdrCoder<Integer>
{

  @Override
  public void encodeObject(Integer object, AXdrOutputStream out) throws IOException
  {
    out.writeInteger(object);
  }

  @Override
  public Integer decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readInteger();
  }

}
