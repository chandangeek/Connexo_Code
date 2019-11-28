/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsData.java $
 * Version:     
 * $Id: DlmsData.java 6448 2013-04-17 14:46:56Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:10:57
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import java.math.BigInteger;

/**
 * This class is basic class for DLMS Data.<P>
 * The data types are listed in the GB ed. 7 p. 210
 *
 * @author osse
 */
public abstract class DlmsData
{
  public enum DataType
  {
    NULL_DATA(0, "null-data", Object.class, DlmsDataNull.class),
    ARRAY(1, "array",DlmsData[].class, DlmsDataArray.class),
    STRUCTURE(2, "structure",DlmsData[].class,DlmsDataStructure.class),
    BOOLEAN(3, "boolean", Boolean.class,DlmsDataBoolean.class),
    BIT_STRING(4, "bit-string",BitString.class,DlmsDataBitString.class),
    DOUBLE_LONG(5, "double-long",Integer.class,DlmsDataDoubleLong.class),
    DOUBLE_LONG_UNSIGNED(6, "double-long-unsigned",Long.class,DlmsDataDoubleLongUnsigned.class),
    FLOATING_POINT(7, "floating-point",Float.class,DlmsDataFloatingPoint.class),
    OCTET_STRING(9, "octet-string",byte[].class,DlmsDataOctetString.class),
    VISIBLE_STRING(10, "visible-string",String.class,DlmsDataVisibleString.class),
    BCD(13, "bcd",Integer.class,DlmsDataBcd.class),
    INTEGER(15, "integer",Integer.class,DlmsDataInteger.class),
    LONG(16, "long", Integer.class,DlmsDataLong.class),
    UNSIGNED(17, "unsigned",Integer.class,DlmsDataUnsigned.class),
    LONG_UNSIGNED(18, "long-unsigned",Integer.class,DlmsDataLongUnsigned.class),
    COMPACT_ARRAY(19, "compact-array",DlmsData[].class,DlmsDataCompactArray.class),
    LONG64(20, "long64",Long.class,DlmsDataLong64.class),
    LONG64_UNSIGNED(21, "long64-unsigned",BigInteger.class,DlmsDataLong64Unsigned.class),
    ENUM(22, "enum", Integer.class,DlmsDataEnum.class),
    FLOAT32(23, "float32",Float.class,DlmsDataFloat32.class),
    FLOAT64(24, "float64",Double.class,DlmsDataFloat64.class),
    DATE_TIME(25, "date_time",DlmsDateTime.class,DlmsDataDateTime.class),
    DATE(26, "date",DlmsDate.class,DlmsDataDate.class),
    TIME(27, "time",DlmsTime.class,DlmsDataTime.class),
    DONT_CARE(255, "dont-care",Object.class,DlmsDataDontCare.class);
    //
    private final int tag;
    private final String orgName;
    private final Class<?> valueClass;
    private final Class<? extends DlmsData> implementingClass;
    private static final DataType[] TAG_MAP = createTagMap();

    private DataType(final int tag, final String orgName,final Class<?> valueClass,final Class<? extends DlmsData> implementingClass)
    {
      this.tag = tag;
      this.orgName = orgName;
      this.valueClass= valueClass;
      this.implementingClass= implementingClass;
    }

    /**
     * Returns the tag (as it used for decoding and encoding).
     *
     * @return The tag.
     */
    public int getTag()
    {
      return tag;
    }

    /**
     * Returns the data type for the specified tag or {@code null} if no data type for the specified tag
     * exists.
     *
     * @param tag The tag.
     * @return The data type or {@code null}
     */
    public static DataType findDataType(final int tag)
    {
      if (tag >= 0 && tag < TAG_MAP.length)
      {
        return TAG_MAP[tag];
      }
      
      //Special handling for DONT_CARE (Tag 255) to keep the TAG_MAP small.
      //(The unit test DataTypeTest.testFindDataType ensures that all types are found.)
      if (tag == DONT_CARE.getTag())
      {
        return DONT_CARE;
      }

      return null;
    }

    /**
     * The name as declared in the decoding rules.
     * <P>
     * see GB ed. 7 p. 210
     *
     * @return The original name.
     */
    public String getOrgName()
    {
      return orgName;
    }

    /**
     * Package private: Currently only for subclasses and unit tests. 
     * @return  The value class.
     */
    Class<?> getValueClass()
    {
      return valueClass;
    }

    /**
     * Package private: Currently only for unit tests. 
     * @return  The implementing class.
     */
    Class<? extends DlmsData> getImplementingClass()
    {
      return implementingClass;
    }
    
   public boolean isNumberType()
   {
     return Number.class.isAssignableFrom(valueClass);
   }

    @Override
    public String toString()
    {
      return orgName;
    }

    private static DataType[] createTagMap()
    {
      DataType[] result = new DataType[28];
      for (DataType t : DataType.values())
      {
        if (t.getTag() >= 0 && t.getTag() < result.length)
        {
          result[t.getTag()] = t;
        }
      }
      return result;
    }

  };

  
  //Only subtypes in package allowed
  DlmsData()
  {
    //nothing to do
  }
 
  /**
   * Returns the value.
   *
   * @return The value.
   */
  public abstract Object getValue();

  /**
   * Returns the data type.
   *
   * @return The DLMS data type.
   */
  public abstract DataType getType();
  
  
  
  /**
   * String representation of the value (without further information of the DLMS-Data type).
   * 
   * @return The value as string.
   */
  
  public String stringValue()
  {
    return getValue().toString();
  }
    
  /**
   * Returns type of the Object
   * 
   * @return 
   */
  public Class<?> getValueType()
  {
    return getType().getValueClass();
  }
  

  @Override
  public String toString()
  {
    if (getValue() == null)
    {
      return getType().getOrgName() + "=null";
    }
    else
    {
      return getType().getOrgName() + "=" + getValue().toString();
    }
  }

  /**
   * Single line string.<P> (The collection types will not show their content.)
   *
   * @return
   */
  public String toSingleLineString(final int maxElementsPerCollection)
  {
    return toString();
  }

  public String toString(final String prefix)
  {
    return prefix + toString();
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

    final DlmsData other = (DlmsData)obj;
    if (getType() != other.getType())
    {
      return false;
    }

    if (!getValue().equals(other.getValue()))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + getType().hashCode();
    hash = 37 * hash + getValue().hashCode();
    return hash;
  }

}
