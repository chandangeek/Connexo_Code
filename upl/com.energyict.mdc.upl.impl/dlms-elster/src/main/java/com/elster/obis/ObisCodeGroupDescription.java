/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/ObisCodeGroupDescription.java $
 * Version:     
 * $Id: ObisCodeGroupDescription.java 4495 2012-05-11 12:39:19Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.08.2010 11:26:15
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal class to build descriptions for OBIS codes.
 *
 * @author osse
 */
class ObisCodeGroupDescription
{
  private static final String GROUPS = "ABCDEF";
  private static final String NAME_TABLE_INDICES = "0123456789";
  private final String orgString;
  private final int descriptionGroup;
  private final NameTable nameTable;
  private final List<? extends IPart> parts;

  public ObisCodeGroupDescription(final String orgString,final int groupNo,final NameTable nameTable)
  {
    this.orgString = orgString.intern();
    this.descriptionGroup = groupNo;
    this.nameTable = nameTable;
    this.parts= parse(orgString);
  }

  private ObisCodeGroupDescription(final String orgString,final int descriptionGroup,final NameTable nameTable,
                                  final List<? extends IPart> parts)
  {
    this.orgString = orgString;
    this.descriptionGroup = descriptionGroup;
    this.nameTable = nameTable;
    this.parts = parts;
  }
  
  
    

  private static ObisCodeGroupDescription groupADescription = null;

  public static synchronized ObisCodeGroupDescription getGroupADescription()
  {
    if (groupADescription == null)
    {
//      List<IPart> parts= new ArrayList<IPart>(1);
//      parts.add(new PartADescription());
      groupADescription = new ObisCodeGroupDescription("", 0, null,Collections.singletonList(new PartADescription()));
    }
    return groupADescription;
  }

  public String describe(final ObisCode code)
  {
    if (parts == null)
    {
      return orgString;
    }
    else
    {
      final StringBuilder sb = new StringBuilder();
      for (IPart p : parts)
      {
        sb.append(p.describe(code));
      }
      return sb.toString();
    }

  }

  public String getOrgString()
  {
    return orgString;
  }

  private List<IPart> parse(final String definition)
  {
    List<IPart> result= null;
    
    if (definition.indexOf('$') >= 0)
    {
      result = new ArrayList<IPart>();

      int pos = 0;

      while (pos < definition.length())
      {
        if (definition.charAt(pos) != '$')
        {
          int next = definition.indexOf('$', pos);
          if (next < 0)
          {
            next = definition.length();
          }
          result.add(new PartStatic(definition.substring(pos, next)));
          pos = next;
        }
        else
        {
          if (definition.charAt(pos + 1) == '(')
          {
            int end = definition.indexOf(')', pos);
            if (end < 0)
            {
              throw new IllegalArgumentException("Cannot parse: " + definition);
            }
            result.add(new PartGroupReplace(definition.substring(pos, end + 1)));
            pos = end + 1;
          }
          else
          {
            if (GROUPS.indexOf(definition.charAt(pos + 1)) >= 0)
            {
              result.add(new PartGroupReplace(definition.substring(pos, pos + 2)));
              pos += 2;
            }
            else if (NAME_TABLE_INDICES.indexOf(definition.charAt(pos + 1)) >= 0)
            {
              result.add(new PartNameReplace(NAME_TABLE_INDICES.indexOf(definition.charAt(pos + 1))));
              pos += 2;
            }
            else
            {
              throw new IllegalArgumentException("Cannot parse: " + definition);
            }
          }
        }
      }
    }
    return result;
  }

  private static interface IPart
  {
    String describe(ObisCode code);
  }

  private static final class PartStatic implements IPart
  {
    private final String part;

    public PartStatic(final String part)
    {
      this.part = part.intern();
    }

    public String describe(final ObisCode code)
    {
      return part;
    }

  }

  private static final class PartGroupReplace implements IPart
  {
    private final int groupNo;
    private final int offset;
    private static final Pattern OFFSET_PATTERN = Pattern.compile("^\\$\\(([ABCDEF])([+-])(\\d+)\\)$");

    public PartGroupReplace(final String part)
    {
      if (!part.startsWith("$"))
      {
        throw new IllegalArgumentException("Part must start with $: " + part);
      }

      if (part.length() == 2)
      {
        offset = 0;
        groupNo = GROUPS.indexOf(part.charAt(1));
        if (groupNo < 0)
        {
          throw new IllegalArgumentException("Groupname not found: " + part);
        }
      }
      else if (part.length() >= 6)
      {
        final Matcher matcher = OFFSET_PATTERN.matcher(part);

        if (!matcher.matches())
        {
          throw new IllegalArgumentException("Cannot parse " + part);
        }

        groupNo = GROUPS.indexOf(matcher.group(1));

        if (matcher.group(2).equals("+"))
        {
          offset = Integer.parseInt(matcher.group(3));
        }
        else
        {
          offset = -Integer.parseInt(matcher.group(3));
        }
      }
      else
      {
        throw new IllegalArgumentException("Cannot parse " + part);
      }
    }

    public String describe(final ObisCode code)
    {
      return Integer.toString(code.getValueGroup(groupNo) + offset);
    }

  }

  private final class PartNameReplace implements IPart
  {
    private final int tableIndex;

    public PartNameReplace(final int tableIndex)
    {
      this.tableIndex = tableIndex;
    }

    public String describe(final ObisCode code)
    {
      final int group = code.getValueGroup(descriptionGroup);
      return nameTable.findName(tableIndex, descriptionGroup, group);
    }

  }

  private static class PartADescription implements IPart
  {
    private static final Map<Integer, String> VALUE_GROUP_A_NAMES = createGroupAMap();

    private static Map<Integer, String> createGroupAMap()
    {
      final Map<Integer, String> result = new HashMap<Integer, String>();
      result.put(0, "Abstract");
      result.put(7, "Gas");
      return result;
    }

    public String describe(final ObisCode code)
    {
      return VALUE_GROUP_A_NAMES.get(code.getA());
    }

  }

}
