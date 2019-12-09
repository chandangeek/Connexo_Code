package com.elster.dlms.cosem.application.services.get;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.types.data.DlmsData;

/**
 * The "GetDataResult" of the COSEM GET service.<P>
 * See GB ed.7 p.146
 *
 * @author osse
 */
public class GetDataResult
{
  private final DataAccessResult accessResult;
  private final DlmsData data;

  public GetDataResult(DataAccessResult accessResult, DlmsData data)
  {
    this.accessResult = accessResult;
    this.data = data;
  }

  public DataAccessResult getAccessResult()
  {
    return accessResult;
  }

  public DlmsData getData()
  {
    return data;
  }

  @Override
  public String toString()
  {
    return "GetDataResult{" + "accessResult=" + accessResult + ", data=" + data + '}';
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final GetDataResult other = (GetDataResult)obj;
    if (this.accessResult != other.accessResult)
    {
      return false;
    }
    if (this.data != other.data && (this.data == null || !this.data.equals(other.data)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 61 * hash + (this.accessResult != null ? this.accessResult.hashCode() : 0);
    hash = 61 * hash + (this.data != null ? this.data.hashCode() : 0);
    return hash;
  }
  
  

  
}
