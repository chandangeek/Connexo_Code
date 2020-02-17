/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/NameTable.java $
 * Version:     
 * $Id: NameTable.java 4495 2012-05-11 12:39:19Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.08.2010 13:12:08
 */
package com.elster.obis;

import com.elster.obis.IRangeMap.Pair;
import java.util.HashMap;
import java.util.Map;

/**
 * Name table for looking up names in ObisCodeDefs.
 *
 * @author osse
 */
public class NameTable
{
  private final Map<Key, Pair<ObisCodeDef>> map = new HashMap<Key, Pair<ObisCodeDef>>();
  private final static String GROUPS = "ABCDEF";

  public NameTable()
  {
    //nothing to do.
  }

  /**
   * Add an entry to the table.
   *
   * @param nameDef definition of the name in the form "N2D".
   * @param pair The definition.
   */
  public void add(final String nameDef,final Pair<ObisCodeDef> pair)
  {
    if (nameDef.length() < 3 || nameDef.charAt(0) != 'N')
    {
      throw new IllegalArgumentException("Cannot parse: " + nameDef);
    }

    final int index = Integer.parseInt(nameDef.substring(1, nameDef.length() - 1));
    final int discriminatorGroup = GROUPS.indexOf(nameDef.charAt(nameDef.length() - 1));

    if (discriminatorGroup < 1)
    {
      throw new IllegalArgumentException("Cannot parse: " + nameDef);
    }

    if (!pair.getRange().getGroupRange(discriminatorGroup).isSingleValue())
    {
      throw new IllegalArgumentException("The definition must provide an single Value in group "
                                         + discriminatorGroup + " :" + pair.toString());
    }
    map.put(new Key(index, discriminatorGroup, pair.getRange().getGroupRange(discriminatorGroup).getSingleValue()),
            pair);
  }

  /**
   * Searches a name.
   *
   * @param tableIndex Index of the table.
   * @param groupNo Number of the group.
   * @param groupValue Value of the group.
   * @return The name or {@code null} if no name was found.
   */
  public String findName(final int tableIndex,final int groupNo,final int groupValue)
  {
    final Key key = new Key(tableIndex, groupNo, groupValue);
    
    final Pair<ObisCodeDef> def = map.get(key);

    if (def == null)
    {
      return null;
    }

    return def.getItem().getGroupDescription(groupNo);
  }

  private static final class Key
  {
    private final int index;
    private final int discriminatorGroup;
    private final int discriminatorGroupValue;

    public Key(final int index,final int discriminatorGroup,final int discriminatorGroupValue)
    {
      this.index = index;
      this.discriminatorGroup = discriminatorGroup;
      this.discriminatorGroupValue = discriminatorGroupValue;
    }

    @Override
    public boolean equals(final Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final Key other = (Key)obj;
      if (this.index != other.index)
      {
        return false;
      }
      if (this.discriminatorGroup != other.discriminatorGroup)
      {
        return false;
      }
      if (this.discriminatorGroupValue != other.discriminatorGroupValue)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 29 * hash + this.index;
      hash = 29 * hash + this.discriminatorGroup;
      hash = 29 * hash + this.discriminatorGroupValue;
      return hash;
    }

  }

}
