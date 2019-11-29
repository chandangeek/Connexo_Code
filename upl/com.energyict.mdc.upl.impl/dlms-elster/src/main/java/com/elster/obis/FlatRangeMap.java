/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/FlatRangeMap.java $
 * Version:     
 * $Id: FlatRangeMap.java 4767 2012-07-02 16:10:34Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 10, 2012 10:29:08 AM
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class ...
 *
 * @author osse
 */
public class FlatRangeMap<T> implements IRangeMap<T>
{
  private final List<Pair<T>> items;

  public FlatRangeMap(final List<Pair<T>> items)
  {
    this.items = Collections.unmodifiableList(new ArrayList<Pair<T>>(items));
  }

  public T find(final ObisCode obisCode)
  {
    for (Pair<T> p : items)
    {
      if (p.getRange().contains(obisCode))
      {
        return p.getItem();
      }
    }

    return null;
  }

  public List<T> findAll(final ObisCode obisCode)
  {
    final List<T> result = new ArrayList<T>();

    for (Pair<T> p : items)
    {
      if (p.getRange().contains(obisCode))
      {
        result.add(p.getItem());
      }
    }
    return result;
  }

  public List<Pair<T>> findAllPairs(ObisCode obisCode)
  {
    final List<Pair<T>> result = new ArrayList<Pair<T>>();

    for (Pair<T> p : items)
    {
      if (p.getRange().contains(obisCode))
      {
        result.add(p);
      }
    }
    return result;
  }

  public List<Pair<T>> getPairs()
  {
    return items;
  }

}
