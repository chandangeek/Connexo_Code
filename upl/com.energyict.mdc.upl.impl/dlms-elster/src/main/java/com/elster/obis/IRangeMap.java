/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/IRangeMap.java $
 * Version:
 * $Id: IRangeMap.java 4767 2012-07-02 16:10:34Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.07.2010 13:35:45
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import java.util.List;

/**
 * Find methods for {@link ObisCodeDef }
 *
 * @author osse
 */
public interface IRangeMap<T>
{
  /**
   * Searches for a definition for the specified OBIS code.<P>
   * The first definition found, will be returned.
   *
   * @param obisCode The OBIS code
   * @return The found definition or {@code null } if no definition was found.
   */
  T find (ObisCode obisCode);

  /**
   * Searches for definitions for the specified OBIS code.<P>
   * All definitions which satisfies the OBIS code will be returned.
   * @return An array with the definitions. This array will be empty if no definition was found.
   */
  List<T> findAll(ObisCode obisCode);
  
  
  List<Pair<T>> findAllPairs(ObisCode obisCode);
  
  
  List<Pair<T>> getPairs();
  
  class Pair<T>
  {
    final ObisCodeRange range;
    final T item;

    public Pair(ObisCodeRange range, T item)
    {
      this.range = range;
      this.item = item;
    }

    public T getItem()
    {
      return item;
    }

    public ObisCodeRange getRange()
    {
      return range;
    }

    @Override
    public String toString()
    {
      return range.toString()+" = "+item.toString();
    }
    
    
  }
  

}
