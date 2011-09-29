/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrColumnHeaderLis200.java $
 * Version:     
 * $Id: AgrColumnHeaderLis200.java 1972 2010-09-02 08:48:51Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.07.2009 14:24:18
 */
package com.elster.agrimport.agrreader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class represents one column header of an LIS200 AGR file.
 *
 * @author osse
 */
class AgrColumnHeaderLis200 extends AgrColumnHeader
{
  private static final Logger LOGGER = Logger.getLogger(AgrColumnHeaderLis200.class.getName());
  private static final String[] globOrderNoIdentifiers =
  {
    "GONr"
  };
  private static Set<String> globOrderNoIdentifierSet;
  private static final String[] intStatusIdentifiers =
  {
    "St.ES", "St.HS", "St.LS", "St.KS"
  };
  private static Set<String> intStatusIdentifierSet;
  private static final String[] analogUnits =
  {
    "", "MPa", "psi", "bar", "kPa", "{C", "{F", "°C", "°F"
  };
  private static Set<String> analogUnitsSet;
  private AgrColType columnType;

  private static synchronized Set getGlobOrderNoIdentifierSet()
  {
    if (globOrderNoIdentifierSet == null)
    {
      globOrderNoIdentifierSet = new HashSet<String>();
      globOrderNoIdentifierSet.addAll(Arrays.asList(globOrderNoIdentifiers));
    }
    return globOrderNoIdentifierSet;
  }

  private static synchronized Set getIntStatusIdentifierSet()
  {
    if (intStatusIdentifierSet == null)
    {
      intStatusIdentifierSet = new HashSet<String>();
      intStatusIdentifierSet.addAll(Arrays.asList(intStatusIdentifiers));
    }
    return intStatusIdentifierSet;
  }

  private static synchronized Set getAnalogUnitsSet()
  {
    if (analogUnitsSet == null)
    {
      analogUnitsSet = new HashSet<String>();
      analogUnitsSet.addAll(Arrays.asList(analogUnits));
    }
    return analogUnitsSet;
  }

  public AgrColumnHeaderLis200(String head1, String head2, String head3, int number, int columnCount)
  {
    super(head1, head2, head3, number, columnCount);
  }

  protected boolean isGlobOrderNumber()
  {
    return (getNumber() == getColumnCount() - 2 && getGlobOrderNoIdentifierSet().contains(
            getHeadName()) && "D".equals(getHeadColumnType()));
  }

  /**
   * @return the columnType
   */
  @Override
  public AgrColumnHeader.AgrColType getColumnType()
  {
    if (columnType == null)
    {
      if (isGlobOrderNumber())
      {
        columnType = AgrColType.GLOBORDERNUMBER;
      }
      else
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
              columnType = determineZType();
              break;
            case 'S':
              columnType = determineStatusType();
              break;
            case 'A':
              columnType = AgrColType.STATUS_REGISTER;
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
            default:
              columnType = AgrColType.UNKNOWN;
          }
        }
        else
        {
          columnType = AgrColType.UNKNOWN;
        }
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
    if (getIntStatusIdentifierSet().contains(getHeadName()))
    {
      return AgrColType.STATUS_INT;
    }

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

  private AgrColType determineZType()
  {
    String trimedUnit = getHeadUnit().trim();
    if (getAnalogUnitsSet().contains(trimedUnit))
    {
      return AgrColType.NUMBER;
    }
    else
    {
      return AgrColType.COUNTER;
    }
  }

}
