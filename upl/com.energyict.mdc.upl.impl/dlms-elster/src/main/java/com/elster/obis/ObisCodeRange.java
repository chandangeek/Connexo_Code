/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/ObisCodeRange.java $
 * Version:     
 * $Id: ObisCodeRange.java 4767 2012-07-02 16:10:34Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 10, 2012 8:57:33 AM
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable class to define a range of OBIS codes.
 * <P>
 * Often it is necessary to check if an OBIS code belongs to an range of OBIS codes, like "7.0.b.96.11.0" there "b"
 * can be "freely" chosen.
 * <P>
 * The mentioned range can be created by {@code ObisCodeRange.parse("7.0.*.96.11.0")}. If an OBIS code belongs to 
 * this range can be checked by calling {@link #contains(com.elster.dlms.types.basic.ObisCode)}
 * 
 * @author osse
 */
public class ObisCodeRange
{
  private final GroupRange[] groups;

  public static ObisCodeRange parse(final String text)
  {
    final String[] groups = text.split("\\.");

    if (groups.length != 6)
    {
      throw new IllegalArgumentException("Not parseable: " + text);
    }

    GroupRange[] groupRanges = new GroupRange[6];

    for (int i = 0; i < 6; i++)
    {
      groupRanges[i] = GroupRange.parse(groups[i]);
    }
    return new ObisCodeRange(groupRanges);
  }

  public ObisCodeRange(final GroupRange[] groupRanges)
  {
    if (groupRanges.length != 6)
    {
      throw new IllegalArgumentException("groupRanges.length must be 6");
    }
    this.groups = groupRanges.clone();
  }

  public ObisCodeRange(final GroupRange a, final GroupRange b, final GroupRange c, final GroupRange d,
                       final GroupRange e, final GroupRange f)
  {
    this.groups = new GroupRange[6];
    this.groups[0] = a;
    this.groups[1] = b;
    this.groups[2] = c;
    this.groups[3] = d;
    this.groups[4] = e;
    this.groups[5] = f;
  }

  public boolean contains(final ObisCode obisCode)
  {
    for (int i = 0; i < 6; i++)
    {
      if (!groups[i].contains(obisCode.getValueGroup(i)))
      {
        return false;
      }
    }
    return true;
  }

  public GroupRange getGroupRange(final int group)
  {
    return groups[group];
  }

  public boolean intersects(final ObisCodeRange other)
  {
    for (int i = 0; i < 6; i++)
    {
      if (!groups[i].intersects(other.groups[i]))
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();
    for (GroupRange g : groups)
    {
      if (sb.length() != 0)
      {
        sb.append(".");
      }
      sb.append(g.toString());
    }
    return sb.toString();
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
    final ObisCodeRange other = (ObisCodeRange)obj;
    if (!Arrays.deepEquals(this.groups, other.groups))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    return Arrays.deepHashCode(this.groups);
  }

  public static abstract class GroupRange
  {
    public static GroupRange parse(final String text)
    {
      if (text.startsWith("["))
      {
        return GroupRangeList.parse(text);
      }
      else
      {
        return SingleGroupRange.parse(text);
      }
    }

    public static GroupRange valueOf(final int first, final int last)
    {
      return SingleGroupRange.valueOf(first, last);
    }

    public static GroupRange valueOf(final int value)
    {
      return valueOf(value, value);
    }

    public static GroupRange valueOf(final GroupRange... ranges)
    {
      return valueOf(Arrays.asList(ranges));
    }

    public static GroupRange valueOf(final Collection<? extends GroupRange> ranges)
    {
      final List<SingleGroupRange> singleRanges = new ArrayList<SingleGroupRange>();
      for (GroupRange gr : ranges)
      {
        singleRanges.addAll(gr.getSingleRanges());
      }

      if (singleRanges.size() == 1)
      {
        return singleRanges.get(0);
      }
      else
      {
        return new GroupRangeList(singleRanges);
      }
    }

    public abstract boolean contains(int groupValue);

    protected abstract List<SingleGroupRange> getSingleRanges();

    public abstract int getMaxGroupValue();

    public abstract int getMinGroupValue();

    public abstract boolean isSingleValue();

    public abstract int getSingleValue();

    public boolean intersects(final GroupRange other)
    {
      final List<SingleGroupRange> mySingleRanges = getSingleRanges();
      final List<SingleGroupRange> otherSingleRanges = other.getSingleRanges();

      for (int i = 0; i < mySingleRanges.size(); i++)
      {
        for (int j = 0; j < otherSingleRanges.size(); j++)
        {
          if (mySingleRanges.get(i).intersects(otherSingleRanges.get(j)))
          {
            return true;
          }
        }
      }
      return false;
    }

  }

  // <editor-fold defaultstate="collapsed" desc="inner class SingleGroupRange">
  private static class SingleGroupRange extends GroupRange implements Comparable<SingleGroupRange>
  {
    private final int first;
    private final int last;
    protected static final SingleGroupRange ALL = new SingleGroupRange(0, 255);

    public static SingleGroupRange valueOf(final int first, final int last)
    {
      if (first > last || first < 0 || first > 255 || last < 0 || last > 255)
      {
        throw new IllegalArgumentException("Illegal range: " + first + "-" + last);
      }

      if (first == 0 && last == 255)
      {
        return SingleGroupRange.ALL;
      }

      return new SingleGroupRange(first, last);
    }

    public static SingleGroupRange valueOf(final int value)
    {
      return valueOf(value, value);
    }

    public static SingleGroupRange parse(final String text)
    {
      if (text.contains("-"))
      {
        final String[] parts = text.split("-");
        final int first = Integer.parseInt(parts[0]);
        final int last = Integer.parseInt(parts[1]);
        return valueOf(first, last);
      }
      else
      {
        if ("*".equals(text))
        {
          return ALL;
        }
        else
        {
          return valueOf(Integer.parseInt(text));
        }
      }
    }

    private SingleGroupRange(final int first, final int last)
    {
      super();
      this.first = first;
      this.last = last;
    }

    public int getFirst()
    {
      return first;
    }

    public int getLast()
    {
      return last;
    }

    @Override
    public boolean contains(final int groupValue)
    {
      return first <= groupValue && groupValue <= last;
    }

    @Override
    public boolean isSingleValue()
    {
      return first == last;
    }

    @Override
    public String toString()
    {
      if (first == last)
      {
        return Integer.toString(first);
      }
      else
      {
        return first + "-" + last;
      }
    }

    public boolean intersects(final SingleGroupRange otherRange)
    {
      return (otherRange.first <= first && otherRange.last >= last)
             || (otherRange.first >= first && otherRange.first <= last)
             || (otherRange.last >= first && otherRange.last <= last);
    }

//    private boolean includes(SingleGroupRange otherRange)
//    {
//      return (otherRange.first >= first && otherRange.last <= last);
//    }
    @Override
    protected List<SingleGroupRange> getSingleRanges()
    {

      return Collections.singletonList(this);
    }

    public int compareTo(final SingleGroupRange o)
    {
      if (first < o.first)
      {
        return -1;
      }
      if (first > o.first)
      {
        return 1;
      }

      if (last < o.last)
      {
        return -1;
      }
      if (last > o.last)
      {
        return 1;
      }
      return 0;
    }

    @Override
    public int getMaxGroupValue()
    {
      return last;
    }

    @Override
    public int getMinGroupValue()
    {
      return first;
    }

    @Override
    public int getSingleValue()
    {
      if (!isSingleValue())
      {
        throw new UnsupportedOperationException("Not supported.");
      }

      return first;
    }

    public boolean equals(final SingleGroupRange other)
    {
      return first == other.first && last == other.last;
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
      final SingleGroupRange other = (SingleGroupRange)obj;
      if (this.first != other.first)
      {
        return false;
      }
      if (this.last != other.last)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      return first << 8 + last;
    }

  }// </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="inner class GroupRangeList">  
  private static class GroupRangeList extends GroupRange
  {
    private final List<SingleGroupRange> ranges;
    private final int maxGroupValue;
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[(.*)\\]");

    public static GroupRangeList parse(final String text)
    {
      final Matcher matcher = BRACKET_PATTERN.matcher(text);
      if (!matcher.matches())
      {
        throw new IllegalArgumentException("Not parseable: " + text);
      }

      final String[] content = matcher.group(1).split(",");

      final List<SingleGroupRange> ranges = new ArrayList<SingleGroupRange>(content.length);

      for (String c : content)
      {
        ranges.add(SingleGroupRange.parse(c));
      }
      return new GroupRangeList(ranges);
    }

    protected GroupRangeList(final SingleGroupRange... ranges)
    {
      this(Arrays.asList(ranges));
    }

    protected GroupRangeList(final Collection<? extends SingleGroupRange> ranges)
    {
      super();
      if (ranges.isEmpty())
      {
        throw new IllegalArgumentException("The ranges must not be empty");
      }

      final List<SingleGroupRange> sortedList =
              new ArrayList<SingleGroupRange>(ranges);
      Collections.sort(sortedList);
      this.ranges = Collections.unmodifiableList(sortedList);
      maxGroupValue = calcMaxGroupValue();
    }

    @Override
    public boolean contains(final int group)
    {
      for (int i = 0; i < ranges.size(); i++)
      {
        if (ranges.get(i).contains(group))
        {
          return true;
        }
      }
      return false;
    }

    private final int calcMaxGroupValue()
    {
      int result = 0;
      for (int j = 0; j < ranges.size(); j++)
      {
        if (ranges.get(j).getLast() > result)
        {
          result = ranges.get(j).getLast();
        }
      }
      return result;
    }

    public int getMinGroupValue()
    {
      return ranges.get(0).getFirst();
    }

    @Override
    public int getMaxGroupValue()
    {
      return maxGroupValue;
    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int j = 0; j < ranges.size(); j++)
      {
        if (j > 0)
        {
          sb.append(",");
        }
        sb.append(ranges.get(j).toString());
      }
      sb.append("]");
      return sb.toString();
    }

    @Override
    protected List<SingleGroupRange> getSingleRanges()
    {
      return ranges;
    }

    @Override
    public boolean isSingleValue()
    {
      return false;
    }

    @Override
    public int getSingleValue()
    {
      throw new UnsupportedOperationException("Not supported.");
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
      final GroupRangeList other = (GroupRangeList)obj;
      if (this.ranges != other.ranges && (this.ranges == null || !this.ranges.equals(other.ranges)))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 61 * hash + (this.ranges != null ? this.ranges.hashCode() : 0);
      return hash;
    }

  }// </editor-fold>

}
