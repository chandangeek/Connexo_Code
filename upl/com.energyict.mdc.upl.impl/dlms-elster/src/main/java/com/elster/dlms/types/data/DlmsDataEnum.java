/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataEnum.java $
 * Version:     
 * $Id: DlmsDataEnum.java 6448 2013-04-17 14:46:56Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:29:11
 */
package com.elster.dlms.types.data;

/**
 * This class implements the DLMS enum data type.
 * <P>
 * This data type is directly inherited from DlmsData because it is not a number like the other number 
 * types.
 *
 * @author osse
 */
public final class DlmsDataEnum extends DlmsData
{
  public static final int MIN_VALUE = 0;
  public static final int MAX_VALUE = 255;
  
  private final int value;

  public DlmsDataEnum(final int value)
  {
    super();
    DlmsDataInteger.rangeCheck(value, MIN_VALUE, MAX_VALUE);
    this.value=value;
  }

  @Override
  public DataType getType()
  {
    return DataType.ENUM;
  }

  @Override
  public Integer getValue()
  {
    return value;
  }

}
