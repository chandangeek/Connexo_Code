/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetDataResult.java $
 * Version:     
 * $Id: CoderGetDataResult.java 3657 2011-09-30 17:25:40Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS data result".
 *
 * @author osse
 */
public class CoderGetDataResult extends AbstractAXdrCoder<GetDataResult>
{
  private final CoderDlmsData coderData = new CoderDlmsData();
  private final CoderDataAccessResult coderDataAccessResult = new CoderDataAccessResult();

  @Override
  public void encodeObject(final GetDataResult object, final AXdrOutputStream out) throws IOException
  {
    if ((object.getAccessResult() != DataAccessResult.SUCCESS && object.getData() != null)
        || (object.getAccessResult() == DataAccessResult.SUCCESS && object.getData() == null))
    {
      throw new IOException("Invalid data result");
    }


    if (object.getData() != null)
    {
      out.write(0);
      coderData.encodeObject(object.getData(), out);
    }
    else
    {
      out.write(1);
      coderDataAccessResult.encodeObject(object.getAccessResult(), out);
    }
  }

  @Override
  public GetDataResult decodeObject(AXdrInputStream in) throws IOException
  {
    int selection = in.readUnsigned8();

    switch (selection)
    {
      case 0:
        return new GetDataResult(DataAccessResult.SUCCESS, coderData.decodeObject(in));
      case 1:
        return new GetDataResult(coderDataAccessResult.decodeObject(in), null);
      default:
        throw new IOException("Unexpected selection number");
    }
  }

}
