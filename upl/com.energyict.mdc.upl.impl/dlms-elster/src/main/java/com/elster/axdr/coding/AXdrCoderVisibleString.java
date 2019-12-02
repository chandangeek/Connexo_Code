/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderVisibleString.java $
 * Version:     
 * $Id: AXdrCoderVisibleString.java 1865 2010-08-10 09:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 14:27:59
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class encodes and decodes "visible" strings.
 *
 * @author osse
 */
public class AXdrCoderVisibleString extends AbstractAXdrCoder<String>
{

  @Override
  public void encodeObject(String object, AXdrOutputStream out) throws IOException
  {
    out.writeVisibleString(object);
  }

  @Override
  public String decodeObject(AXdrInputStream in) throws IOException
  {
    return in.readVisibleString();
  }

}
