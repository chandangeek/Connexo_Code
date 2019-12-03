/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataStructure.java $
 * Version:     
 * $Id: DlmsDataStructure.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;
import java.util.Collection;

/**
 * This class implements the DLMS structure data type.
 *
 * @author osse
 */
public final class DlmsDataStructure extends DlmsDataCollection
{
  public DlmsDataStructure(final DlmsData[] newValue)
  {
    super(newValue);
  }

  public DlmsDataStructure(final DlmsData first, final DlmsData... more)
  {
    super(first, more);
  }

  public DlmsDataStructure(final Collection<DlmsData> values)
  {
    super(values);
  }

  public DlmsDataStructure(final IDlmsDataProvider[] values)
  {
    super(convert(values));
  }

  @Override
  public DataType getType()
  {
    return DataType.STRUCTURE;
  }
  
  
  @Override
  protected String stringValueStartChar()
  {
    return "{";
  }

  @Override
  protected String stringValueEndChar()
  {
    return "}";
  }

}
