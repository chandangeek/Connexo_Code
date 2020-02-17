/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/CosemEnumFactory.java $
 * Version:     
 * $Id: CosemEnumFactory.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 2:52:28 PM
 */
package com.elster.dlms.cosem.classes.common;

/**
 * Factory to return ICosemEnum's or to create default enums for unknown id's.
 *
 * @author osse
 */
public class CosemEnumFactory<T extends ICosemEnum>
{
  private final T[] values;

  public CosemEnumFactory(T[] values)
  {
    this.values = values.clone();
  }

  public T[] getValues()
  {
    return values.clone();
  }

  public ICosemEnum createDefault(final int id, final String text)
  {
    return new ICosemEnum()
    {
      public int getId()
      {
        return id;
      }

      public String getName()
      {
        return "Unknown id " + id;
      }
    };
  }

  public T findValue(final int id)
  {
    int l = 0;
    int h = values.length - 1;
    int m = id;

    while (l <= h)
    {
      if (m > h || m < l)
      {
        m = (l + h) >>> 1;
      }
      final int val = values[m].getId();
      if (val < id)
      {
        l = m + 1;
      }
      else if (val > id)
      {
        h = m - 1;
      }
      else
      {
        return values[m];
      }

      m = m + (id - val);
    }
    return null;
  }

  public ICosemEnum findValueWithDefault(final int id)
  {
    ICosemEnum result = findValue(id);
    if (result == null)
    {
      result = createDefault(id, "-unknown id " + id + "-");
    }
    return result;
  }
  
  public static <A extends ICosemEnum> A find(final A[] values,final int id)
  {
    int l = 0;
    int h = values.length - 1;
    int m = id;

    while (l <= h)
    {
      if (m > h || m < l)
      {
        m = (l + h) >>> 1;
      }
      final int val = values[m].getId();
      if (val < id)
      {
        l = m + 1;
      }
      else if (val > id)
      {
        h = m - 1;
      }
      else
      {
        return values[m];
      }

      m = m + (id - val);
    }
    return null;
  }

}
