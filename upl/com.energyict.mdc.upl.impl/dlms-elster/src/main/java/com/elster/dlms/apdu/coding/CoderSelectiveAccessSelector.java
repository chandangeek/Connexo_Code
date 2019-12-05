/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSelectiveAccessSelector.java $
 * Version:     
 * $Id: CoderSelectiveAccessSelector.java 2628 2011-02-03 18:57:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:11:17
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;

/**
 * En-/decoder for the "selective access selector" PDU part.
 *
 * @author osse
 */
public class CoderSelectiveAccessSelector extends AbstractAXdrCoder<AccessSelectionParameters>
{
  private final CoderDlmsData coderAccessParameters= new CoderDlmsData();

  @Override
  public void encodeObject(final AccessSelectionParameters object, final AXdrOutputStream out) throws
          IOException
  {
    out.writeUnsigned8(object.getSelector());
    coderAccessParameters.encodeObject(object.getAccessParameters(), out);
  }

  @Override
  public AccessSelectionParameters decodeObject(final AXdrInputStream in) throws IOException
  {
    int selector = in.readUnsigned8();
    DlmsData accessParameters= coderAccessParameters.decodeObject(in);
    return new AccessSelectionParameters(selector, accessParameters);
  }

}
