/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataCompactArray.java $
 * Version:     
 * $Id: DlmsDataCompactArray.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS compact array data type.<P>
 * The compact array data type is not different to the other collection types
 * ({@link DlmsDataArray} and {@link DlmsDataStructure}) but it will be encoded
 * in an other way.
 *
 * @author osse
 */
public final class DlmsDataCompactArray extends DlmsDataCollection
{
  private final TypeDescription typeDescription;

  /**
   * Creates the compact array.
   *
   * @param typeDescription The type description for the elements or <b>null</b>.
   * @param elements The elements. (The elements will be not checked in this constructor if they matches the
   * type description. This can be done by calling {@link TypeDescription#checkType(com.elster.dlms.types.data.DlmsData) and
   * it will be done by the encoder.}
   */
  public DlmsDataCompactArray(final TypeDescription typeDescription, final DlmsData[] elements)
  {
    super(elements);
    this.typeDescription = typeDescription;
  }

  /**
   * Creates the compact array without an type description.
   */
  public DlmsDataCompactArray(final DlmsData[] elements)
  {
    this(null, elements);
  }

  public DlmsDataCompactArray(final IDlmsDataProvider[] values)
  {
    this(null, convert(values));
  }

  @Override
  public DataType getType()
  {
    return DataType.COMPACT_ARRAY;
  }

  public TypeDescription getTypeDescription()
  {
    return typeDescription;
  }

  @Override
  public String toString(final String prefix)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix).append(getType().getOrgName()).append("(").append(size()).append(" elements)=" + EOL);
    sb.append(prefix).append("{" + EOL);
    sb.append(prefix).append("\tType description=" + EOL);
    sb.append(prefix).append("\t{" + EOL);
    if (getTypeDescription() == null)
    {
      sb.append(prefix).append("\t\tnull");
    }
    else
    {
      sb.append(getTypeDescription().toString(prefix + "\t\t")).append(EOL);
    }
    sb.append(prefix).append("\t}" + EOL);

    sb.append(prefix).append("\tData=" + EOL);
    sb.append(prefix).append("\t{" + EOL);
    for (DlmsData d : items)
    {
      sb.append(d.toString(prefix + "\t\t")).append(EOL);
    }
    sb.append(prefix).append("\t}" + EOL);

    sb.append(prefix).append("}");
    return sb.toString();
  }

  @Override
  protected String stringValueStartChar()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String stringValueEndChar()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
