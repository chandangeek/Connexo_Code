/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrColumnHeaderStated.java $
 * Version:     
 * $Id: AgrColumnHeaderStated.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  01.07.2010 14:39:05
 */
package com.elster.agrimport.agrreader;

/**
 * This class combines two headers.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrColumnHeaderStated extends AgrColumnHeader
{
  private final AgrColumnHeader valueColumnHeader;
  private final AgrColumnHeader statusColumnHeader;
  private final AgrColType columnType;

  public AgrColumnHeaderStated(AgrColumnHeader valueColumnHeader, AgrColumnHeader statusColumnHeader,
                               int number, int columnCount)
  {
    super(valueColumnHeader.getHeadName(), valueColumnHeader.getHeadUnit(), valueColumnHeader.
            getHeadColumnType(), number, columnCount);
    this.valueColumnHeader = valueColumnHeader;
    this.statusColumnHeader = statusColumnHeader;

    switch (valueColumnHeader.getColumnType())
    {
      case COUNTER:
        this.columnType = AgrColType.STATED_COUNTER;
        break;
      case INTERVALL:
        this.columnType = AgrColType.STATED_INTERVALL;
        break;
      case NUMBER:
        this.columnType = AgrColType.STATED_NUMBER;
        break;
      default:
        throw new IllegalArgumentException("ColumnType " + valueColumnHeader.getColumnType()
                                           + " not supported for stated values");
    }
  }

  @Override
  public AgrColType getColumnType()
  {
    return columnType;
  }

  public AgrColumnHeader getStatusColumnHeader()
  {
    return statusColumnHeader;
  }

  public AgrColumnHeader getValueColumnHeader()
  {
    return valueColumnHeader;
  }

}
