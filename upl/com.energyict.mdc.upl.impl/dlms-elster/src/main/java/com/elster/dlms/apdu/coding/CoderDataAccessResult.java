/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderDataAccessResult.java $
 * Version:     
 * $Id: CoderDataAccessResult.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  25.05.2010 16:15:14
 */

package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import java.io.IOException;

/**
 * En-/decoder for the "Data access result".
 *
 * @author osse
 */
public class CoderDataAccessResult extends AbstractAXdrCoder<DataAccessResult>
{

  @Override
  public void encodeObject(final DataAccessResult object,final AXdrOutputStream out) throws IOException
  {
    out.writeUnsigned8(object.getId());
  }

  @Override
  public DataAccessResult decodeObject(AXdrInputStream in) throws IOException
  {
    int dataAccessResultId = in.readUnsigned8();
    DataAccessResult accessResult = DataAccessResult.findById(dataAccessResultId);
    if (accessResult == null)
    {
      throw new IOException("Unknown data access result: " + dataAccessResultId);
    }
    return accessResult;
  }


}
