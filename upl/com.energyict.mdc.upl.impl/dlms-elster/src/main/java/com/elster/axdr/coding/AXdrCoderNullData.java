/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderNullData.java $
 * Version:     
 * $Id: AXdrCoderNullData.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 17:17:51
 */
package com.elster.axdr.coding;

import java.io.IOException;

/**
 * This class is an special AbstractAXdrCoder which does nothing in its encoding methods.
 *
 * @author osse
 */
public class AXdrCoderNullData extends AbstractAXdrCoder<Object>
{
  /**
   * Does nothing.
   */
  @Override
  public void encodeObject(final Object object, final AXdrOutputStream out) throws IOException
  {
    //nothing to do
  }

  /**
   * Reads nothing, returns null.
   */
  @Override
  public Object decodeObject(final AXdrInputStream in) throws IOException
  {
    return null;
  }

}
