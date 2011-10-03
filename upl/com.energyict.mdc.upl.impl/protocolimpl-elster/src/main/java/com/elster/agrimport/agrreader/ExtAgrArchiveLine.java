/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/ExtAgrArchiveLine.java $
 * Version:     
 * $Id: ExtAgrArchiveLine.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 16:56:56
 */
package com.elster.agrimport.agrreader;

import java.util.Collection;
import java.util.Date;

/**
 * This class represents on archive line.<P>
 * The line number, the order number, the global order number and the first date will be additionally stored
 * in fields.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class ExtAgrArchiveLine
{
  int lineNo = -1;
  long orderNo = -1;
  long globalOrderNo = -1;
  Date date;
  IAgrValue[] values;

  public ExtAgrArchiveLine(int valueCount)
  {
    values = new IAgrValue[valueCount];
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public long getGlobalOrderNo()
  {
    return globalOrderNo;
  }

  public void setGlobalOrderNo(long globalOrderNo)
  {
    this.globalOrderNo = globalOrderNo;
  }

  public int getLineNo()
  {
    return lineNo;
  }

  public void setLineNo(int lineNo)
  {
    this.lineNo = lineNo;
  }

  public long getOrderNo()
  {
    return orderNo;
  }

  public void setOrderNo(long orderNo)
  {
    this.orderNo = orderNo;
  }

  public IAgrValue getValue(int index)
  {
    return values[index];
  }

  public void setValue(int index, IAgrValue value)
  {
    values[index] = value;
  }

  public void setValues(Collection<IAgrValue> values)
  {
    int i = 0;
    for (IAgrValue v : values)
    {
      this.values[i++] = v;
    }
  }

  public int getValueCount()
  {
    return values.length;
  }

}
