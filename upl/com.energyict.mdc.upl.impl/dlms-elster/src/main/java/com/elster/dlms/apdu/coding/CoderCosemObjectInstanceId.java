/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderCosemObjectInstanceId.java $
 * Version:     
 * $Id: CoderCosemObjectInstanceId.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 14:20:38
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.types.basic.ObisCode;
import java.io.IOException;

/**
 * En-/decoder for the "COSEM Object instance id" (OBIS code).
 *
 * @author osse
 */
public class CoderCosemObjectInstanceId extends AbstractAXdrCoder<ObisCode>
{

  @Override
  public void encodeObject(final ObisCode object,final AXdrOutputStream out) throws IOException
  {
    out.writeOctetStringFixLength(object.toByteArray());
  }

  @Override
  public ObisCode decodeObject(final AXdrInputStream in) throws IOException
  {
    return new ObisCode(in.readOctetString(6));
  }

}
