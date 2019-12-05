/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetResponseWithDataBlock.java $
 * Version:     
 * $Id: CoderGetResponseWithDataBlock.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.08.2010 14:47:32
 */

package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseOneBlock;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get response with data block".
 *
 * @author osse
 */
class CoderGetResponseWithDataBlock  extends AbstractAXdrCoder<CosemGetResponseOneBlock>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority= new CoderInvokeIdAndPriority();
  private final CoderDataAccessResult coderDataAccessResult= new CoderDataAccessResult();


  @Override
  public void encodeObject(final CosemGetResponseOneBlock object, final AXdrOutputStream out) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public CosemGetResponseOneBlock decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemGetResponseOneBlock result= new CosemGetResponseOneBlock();
    result.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    //--- DataBlockG ---
    result.setLastBlock(in.readBoolean());
    result.setBlocknumber(in.readUnsigned32());

    int tag= in.readTag();

    switch (tag)
    {
      case 0:  //raw data
        result.setDataAccessResult(DataAccessResult.SUCCESS);
        result.setRawData(in.readOctetString());
        break;
      case 1:
        result.setDataAccessResult(coderDataAccessResult.decodeObject(in));
        result.setRawData(null);
        break;
      default:
        throw new IOException("Unknown tag in DataBlockG: "+tag);
    }
    return result;
  }



}
