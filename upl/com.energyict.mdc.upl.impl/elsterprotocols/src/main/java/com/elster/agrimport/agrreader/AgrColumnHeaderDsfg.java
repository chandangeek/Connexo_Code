/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrColumnHeaderDsfg.java $
 * Version:     
 * $Id: AgrColumnHeaderDsfg.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.07.2009 14:24:18
 */
package com.elster.agrimport.agrreader;

import java.util.logging.Logger;

/**
 * This class represents one column header of an DSfG AGR file.
 *
 * @author osse
 */
public class AgrColumnHeaderDsfg extends AgrColumnHeader
{
  private static final Logger LOGGER = Logger.getLogger(AgrColumnHeaderDsfg.class.getName());
  private IAgrColTypeDefinitions.AgrColType columnType;

  public AgrColumnHeaderDsfg(String head1, String head2, String head3, int number, int columnCount)
  {
    super(head1, head2, head3, number, columnCount);
  }

  /**
   * @return the columnType
   */
  @Override
  public AgrColumnHeader.AgrColType getColumnType()
  {
    if (columnType == null)
    {
      if (getHeadColumnType().length() == 1)
      {
        switch (getHeadColumnType().charAt(0))
        {
          case 'O':
            columnType = AgrColType.ORDERNUMBER;
            break;
          case 'T':
            columnType = AgrColType.TIMESTAMP;
            break;
          case 'Z':
            columnType = AgrColType.COUNTER;
            break;
          case 'S':
            columnType = determineStatusType();
            break;
          case 'H':
            columnType = AgrColType.HEX;
            break;
          case 'D':
            columnType = AgrColType.DATA;
            break;
          case 'N':
            columnType = AgrColType.NUMBER;
            break;
          case 'I':
            columnType = AgrColType.INTERVALL;
            break;
          case 'A':
            columnType = AgrColType.APHANUMERIC;
            break;
          default:
            columnType = AgrColType.UNKNOWN;
        }
      }
      else
      {
        columnType = AgrColType.UNKNOWN;
      }
      if (columnType == AgrColType.UNKNOWN)
      {
        LOGGER.warning("unknown column type \"" + getHeadColumnType() + "\" in column "
                       + getNumber());
      }
    }

    return columnType;
  }

  private AgrColType determineStatusType()
  {
    if (getHeadName().endsWith("_S"))
    {
      return AgrColType.STATUS_INT;
    }

    if (getHeadName().startsWith("S_"))
    {
      return AgrColType.STATUS_INT;
    }

    return AgrColType.STATUS_STRING;
  }

}
