/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataArray.java $
 * Version:     
 * $Id: DlmsDataArray.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;
import java.util.Collection;

/**
 * This class implements the DLMS array data type.<P>

 * @author osse
 */
public final class DlmsDataArray extends DlmsDataCollection
{
  public static final DlmsData[] EMPTY_ARRAY = new DlmsData[0];
  public final static Class<?> VALUE_TYPE = DlmsData[].class;

  public DlmsDataArray(final DlmsData[] values)
  {
    super(values);
  }

  public DlmsDataArray(final DlmsData first, final DlmsData... more)
  {
    super(first, more);
  }

  public DlmsDataArray(final Collection<? extends DlmsData> values)
  {
    super(values);
  }

  public DlmsDataArray(final IDlmsDataProvider[] values)
  {
    super(convert(values));
  }

  @Override
  public DataType getType()
  {
    return DataType.ARRAY;
  }

  @Override
  protected String stringValueStartChar()
  {
    return "[";
  }

  @Override
  protected String stringValueEndChar()
  {
    return "]";
  }

}
