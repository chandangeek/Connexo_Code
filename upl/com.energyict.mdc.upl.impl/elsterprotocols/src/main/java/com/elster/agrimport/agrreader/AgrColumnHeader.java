/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrColumnHeader.java $
 * Version:     
 * $Id: AgrColumnHeader.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.07.2009 14:24:18
 */
package com.elster.agrimport.agrreader;

import java.util.logging.Logger;

/**
 * This class represents one column (channel) header of the table part of an AGR file.
 * <p/>
 * This subclasses of this class are responsible for interpreting the column type.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public abstract class AgrColumnHeader implements IAgrColTypeDefinitions
{
  private static final Logger LOGGER = Logger.getLogger(AgrColumnHeader.class.getName());
  private final String headName;
  private final String headUnit;
  private final String headColumnType;
  private int number;
  private int columnCount;

  /**
   * @return the headName
   */
  public String getHeadName()
  {
    return headName;
  }

  /**
   * @return the headUnit
   */
  public String getHeadUnit()
  {
    return headUnit;
  }

  /**
   * @return the headColumnType
   */
  public String getHeadColumnType()
  {
    return headColumnType;
  }

  /**
   * @return the number
   */
  public int getNumber()
  {
    return number;
  }

  /**
   * The column count.<P>
   * Some columns needs this information to determine the correct column type.
   *
   * @return int
   */
  public int getColumnCount()
  {
    return columnCount;
  }

  /**
   * @return the columnType
   */
  public abstract AgrColType getColumnType();

  AgrColumnHeader(final String head1, final String head2, final String head3, final int number,
                  final int columnCount)
  {
    this.headName = head1;
    this.headUnit = head2;
    this.headColumnType = head3;
    this.number = number;
    this.columnCount = columnCount;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + getHeadName() + "," + getHeadUnit() + ","
           + getHeadColumnType() + "," + getColumnType() + ")";
  }

}
