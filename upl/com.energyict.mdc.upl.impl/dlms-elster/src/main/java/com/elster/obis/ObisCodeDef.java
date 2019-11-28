/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/ObisCodeDef.java $
 * Version:     
 * $Id: ObisCodeDef.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.07.2010 09:42:27
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;

/**
 * Informations about a set of OBIS codes.<P>
 *
 *
 * @author osse
 */
public class ObisCodeDef
{
  private final ObisCodeGroupDescription groupDescription[] = new ObisCodeGroupDescription[6];
  private final DlmsDataTypeSet valueAttributeTypes;

  public ObisCodeDef(final String[] groupDescriptions,final NameTable nameTable,
                     final DlmsDataTypeSet valueAttributeTypes)
  {
    groupDescription[0] = ObisCodeGroupDescription.getGroupADescription();
    for (int i = 0; i < groupDescriptions.length; i++)
    {
      if (groupDescriptions[i]!=null && groupDescriptions[i].length()>0)
      {
        this.groupDescription[i+1] = new ObisCodeGroupDescription(groupDescriptions[i], i+1, nameTable);
      }
    }
    this.valueAttributeTypes = valueAttributeTypes;
  }


  /**
   * A description for the set of OBIS codes managed by the object of this class.
   * <P>
   * This description may contain place holders. To get a version without
   * place holders use the {@link #describe(com.elster.dlms.types.basic.ObisCode)}.
   *
   * @return A description.
   */
  public String getDescription()
  {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 6; i++)
    {
      if (i > 0)
      {
        sb.append(";");
      }

      if (groupDescription[i] != null)
      {
        sb.append(groupDescription[i].getOrgString());
      }
    }
    return sb.toString();
  }

  /**
   * Returns the description for the specified value group.<P>
   * This description may contain place holders.
   *
   * @param valueGroupNo The number of the value group.
   * @return The description or {@code null} if no description is available.
   */
  public String getGroupDescription(final int valueGroupNo)
  {
    final ObisCodeGroupDescription gd = groupDescription[valueGroupNo];

    if (gd == null)
    {
      return null;
    }

    return gd.getOrgString();
  }

  /**
   * Returns the group description for the specified value group and OBIS code.
   *
   * @param valueGroup The value group.
   * @param obisCode The OBIS code.
   * @return The description or {@code null} if no description is available.
   */
  public String getGroupDescription(final ObisCode.Groups valueGroup, final ObisCode obisCode)
  {
    return getGroupDescription(valueGroup.ordinal(), obisCode);
  }

  /**
   * The "group number" version of {@link #getGroupDescription(com.elster.dlms.types.basic.ObisCode.Groups, com.elster.dlms.types.basic.ObisCode)}
   */
  public String getGroupDescription(final int groupNo, final ObisCode obisCode)
  {

    final ObisCodeGroupDescription gd = groupDescription[groupNo];

    if (gd == null)
    {
      return null;
    }

    return gd.describe(obisCode);
  }

  public DlmsDataTypeSet getValueAttributeTypes()
  {
    return valueAttributeTypes;
  }

  /**
   * Returns a description for the specified OBIS code.
   * <P>
   * This method does not check if the OBIS code is acceptable. To do
   * this call before.
   * <br>
   * If this definition was retrieved from an {@link IObisDefFinder } this check is not necessary.
   *
   * @param code The OBIS code.
   * @return The Description.
   */
  public String describe(final ObisCode code)
  {
    final StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 6; i++)
    {
      if (i > 0)
      {
        sb.append(";");
      }

      if (groupDescription[i] != null)
      {
        sb.append(groupDescription[i].describe(code));
      }
    }

    return sb.toString();
  }

  @Override
  public String toString()
  {
    return getDescription();
  }

}
