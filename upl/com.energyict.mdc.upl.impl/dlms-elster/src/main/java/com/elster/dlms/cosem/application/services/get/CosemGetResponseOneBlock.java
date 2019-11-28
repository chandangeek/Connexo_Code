/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetResponseOneBlock.java $
 * Version:     
 * $Id: CosemGetResponseOneBlock.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */
package com.elster.dlms.cosem.application.services.get;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;

/**
 * This COSEM GET response "ONE-BLOCK" and the COSEM GET response "LAST-BLOCK"
 *
 * @author osse
 */
public class CosemGetResponseOneBlock extends CosemGetResponse
{
  DataAccessResult dataAccessResult;
  boolean lastBlock = false;
  long blocknumber;
  byte[] rawData;

  @Override
  public ResponseType getResponseType()
  {
    if (lastBlock)
    {
      return ResponseType.LAST_BLOCK;
    }
    else
    {
      return ResponseType.ONE_BLOCK;
    }
  }

  public DataAccessResult getDataAccessResult()
  {
    return dataAccessResult;
  }

  public void setDataAccessResult(DataAccessResult dataAccessResult)
  {
    this.dataAccessResult = dataAccessResult;
  }

  public long getBlocknumber()
  {
    return blocknumber;
  }

  public void setBlocknumber(long blocknumber)
  {
    this.blocknumber = blocknumber;
  }

  public boolean isLastBlock()
  {
    return lastBlock;
  }

  public void setLastBlock(boolean lastBlock)
  {
    this.lastBlock = lastBlock;
  }

  public byte[] getRawData()
  {
    return rawData;
  }

  public void setRawData(byte[] rawData)
  {
    this.rawData = rawData;
  }

  @Override
  public String toString()
  {
    return "CosemGetResponseOneBlock{" + "invocationId=" + getInvocationId() + ", dataAccessResult="
           + dataAccessResult + ", lastBlock=" + lastBlock + ", blocknumber=" + blocknumber + ", rawData=" + CodingUtils.
            byteArrayToString(rawData) + '}';
  }

}
