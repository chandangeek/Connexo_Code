/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueOctetString.java $
 * Version:     
 * $Id: BerValueOctetString.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 13:29:50
 */

package com.elster.ber.types;

import com.elster.coding.CodingUtils;

/**
 * BER Value for an octet string.
 *
 * @author osse
 */
public class BerValueOctetString extends BerValueBase<byte[]>
{
  public BerValueOctetString(final BerId identifier,final byte[] value)
  {
    super(identifier, value);
  }

  @Override
  protected void describeValue(final BerDescriber describer)
  {
    describer.writeLn(CodingUtils.byteArrayToString(getValue()));
  }



}
