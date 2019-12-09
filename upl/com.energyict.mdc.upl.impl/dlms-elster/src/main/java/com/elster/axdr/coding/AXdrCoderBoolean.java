/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderBoolean.java $
 * Version:     
 * $Id: AXdrCoderBoolean.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.04.2010 10:50:52
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes booleans.
 *
 * @author osse
 */
public class AXdrCoderBoolean extends AbstractAXdrCoder<Boolean>
{

  @Override
  public void encodeObject(Boolean object, AXdrOutputStream out) throws IOException
  {
    out.writeBoolean(object);
  }

  @Override
  public Boolean decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readBoolean();
  }

}
