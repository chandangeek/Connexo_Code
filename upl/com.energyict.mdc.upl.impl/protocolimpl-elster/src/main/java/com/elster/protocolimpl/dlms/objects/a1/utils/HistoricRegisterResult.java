/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import java.util.Date;

/**
 *
 * @author heuckeg
 */
public class HistoricRegisterResult
{
  private final Date readDate;
  private final Date fromDate;
  private final Date toDate;
  private final Object registerResult;

  public HistoricRegisterResult(final Date readDate, final Date fromDate, final Date toDate,
                                final Object registerResult)
  {
    this.readDate = readDate;
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.registerResult = registerResult;
  }

  public Date getReadDate()
  {
    return readDate;
  }

  public Date getFromDate()
  {
    return fromDate;
  }

  public Date getToDate()
  {
    return toDate;
  }

  public Object registerResult()
  {
    return registerResult;
  }

  @Override
  public String toString()
  {
    String result = readDate.toString() + " : ";
    if (fromDate == null)
    {
      result += "?";
    } else
    {
      result += fromDate.toString();
    }
    result += " - ";
    if (toDate == null)
    {
      result += "?";
    } else
    {
      result += toDate.toString();
    }
    return result + " = " + registerResult.toString();
  }

}
