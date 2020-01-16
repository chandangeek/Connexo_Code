/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataParser.java $
 * Version:     
 * $Id: DlmsDataParser.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 22, 2010 3:02:41 PM
 */
package com.elster.dlms.types.data;

import com.elster.coding.CodingUtils;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.BitStringBuilder;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class provides parsing methods for {@link DlmsData} types.<P>
 * Use {@link #getInstance()} to get an instance.
 *
 * @author osse
 */
public final class DlmsDataParser
{
  private final NumberFormat numberFormat;
  /**
   * Set of {@link DlmsData.DataType} which can be parsed by {@link #parse(com.elster.dlms.types.data.DlmsData.DataType, java.lang.String)}
   */
  public static final Set<DlmsData.DataType> PARSEABLE_STRING_TYPES =
          EnumSet.of(DlmsData.DataType.BOOLEAN,
                     DlmsData.DataType.DOUBLE_LONG,
                     DlmsData.DataType.DOUBLE_LONG_UNSIGNED,
                     DlmsData.DataType.FLOATING_POINT,
                     DlmsData.DataType.OCTET_STRING,
                     DlmsData.DataType.VISIBLE_STRING,
                     DlmsData.DataType.BCD,
                     DlmsData.DataType.INTEGER,
                     DlmsData.DataType.LONG,
                     DlmsData.DataType.UNSIGNED,
                     DlmsData.DataType.LONG_UNSIGNED,
                     DlmsData.DataType.LONG64,
                     DlmsData.DataType.LONG64_UNSIGNED,
                     DlmsData.DataType.ENUM,
                     DlmsData.DataType.FLOAT32,
                     DlmsData.DataType.FLOAT64,
                     DlmsData.DataType.BIT_STRING,
                     DlmsData.DataType.DATE_TIME,
                     DlmsData.DataType.DATE,
                     DlmsData.DataType.TIME);
  public static final Set<DlmsData.DataType> PARSEABLE_NUMBER_TYPES =
          EnumSet.of(DlmsData.DataType.DOUBLE_LONG,
                     DlmsData.DataType.DOUBLE_LONG_UNSIGNED,
                     DlmsData.DataType.FLOATING_POINT,
                     DlmsData.DataType.BCD,
                     DlmsData.DataType.INTEGER,
                     DlmsData.DataType.LONG,
                     DlmsData.DataType.UNSIGNED,
                     DlmsData.DataType.LONG_UNSIGNED,
                     DlmsData.DataType.LONG64,
                     DlmsData.DataType.LONG64_UNSIGNED,
                     DlmsData.DataType.ENUM,
                     DlmsData.DataType.FLOAT32,
                     DlmsData.DataType.FLOAT64);
  private static final DlmsDataParser INSTANCE = new DlmsDataParser();
  private final DlmsDateParser dateParser;
  private final String octetStringSeparator;

  /**
   * Returns the parser instance.
   *
   */
  public static DlmsDataParser getInstance()
  {
    return INSTANCE;
  }

  /**
   * Returns the parser instance.
   *
   */
  public static DlmsDataParser getInstance(final Locale locale)
  {
    return new DlmsDataParser(locale, " ");
  }

  /**
   * Returns the parser instance.
   *
   */
  public static DlmsDataParser getInstanceForXml()
  {
    return new DlmsDataParser(Locale.US, "");
  }

  private DlmsDataParser(final Locale locale, final String octetStringSeparator)
  {
    this.octetStringSeparator = octetStringSeparator;
    numberFormat = NumberFormat.getNumberInstance(locale);
    numberFormat.setGroupingUsed(false);
    dateParser = DlmsDateParser.getInstance(locale);
  }

  private DlmsDataParser()
  {
    octetStringSeparator = " ";
    numberFormat = NumberFormat.getNumberInstance();
    numberFormat.setGroupingUsed(false);
    dateParser = DlmsDateParser.getInstance();
  }

  /**
   * Converts the specified {@link DlmsData} to an string.<P> The local number format will be used.
   *
   * @param data The {@link DlmsData} to format.
   * @return A string representing the DlmsData.
   */
  public String format(final DlmsData data)
  {
    if (data instanceof DlmsDataCollection)
    {
      return formatCollection((DlmsDataCollection)data);
    }

    final Object value = data.getValue();

    if (value instanceof Number)
    {
      return numberFormat.format(value);
    }
    else if (value instanceof byte[])
    {
      return CodingUtils.byteArrayToString((byte[])value, octetStringSeparator);
    }
    else if (value instanceof BitString)
    {
      final BitString bitString = (BitString)value;
      return bitString.toString(true, 800);
    }
    else if (value instanceof DlmsDateTime)
    {
      final DlmsDateTime dlmsDateTime = (DlmsDateTime)value;
      return dateParser.formatDlmsDateTime(dlmsDateTime);
    }
    else if (value instanceof DlmsDate)
    {
      final DlmsDate dlmsDate = (DlmsDate)value;
      return dateParser.formatDlmsDate(dlmsDate);
    }
    else if (value instanceof DlmsTime)
    {
      final DlmsTime dlmsTime = (DlmsTime)value;
      return dateParser.formatDlmsTime(dlmsTime);
    }
    else
    {
      if (value == null)
      {
        return "-";
      }
      else
      {
        return value.toString();
      }
    }
  }

  private String formatCollection(final DlmsDataCollection collection)
  {
    final StringBuilder sb = new StringBuilder();

    if (collection.getType() == DlmsData.DataType.STRUCTURE)
    {
      sb.append("{");
    }
    else
    {
      sb.append("[");
    }


    for (int i = 0; i < collection.size(); i++)
    {
      if (i > 0)
      {
        sb.append(", ");
      }
      sb.append(format(collection.get(i)));
    }

    if (collection.getType() == DlmsData.DataType.STRUCTURE)
    {
      sb.append("}");
    }
    else
    {
      sb.append("]");
    }

    return sb.toString();
  }

  /**
   * Creates a {@link DlmsData } object of specified {@link DlmsData.DataType} by parsing the specified
   * string. <P> Only types of the {@link #PARSEABLE_STRING_TYPES } can be created by this method.
   *
   * @param dataType The data type for the object.
   * @param value The string to parse.
   * @return The created object.
   * @throws ParseException
   */
  public DlmsData parse(final DlmsData.DataType dataType, final String value) throws ParseException
  {
    try
    {
      switch (dataType)
      {
//      case NULL_DATA:
//        return new DlmsDataNull();
//      case ARRAY:
//        return new DlmsDataArray(decodeCollection(in));
//      case STRUCTURE:
//        return new DlmsDataStructure(decodeCollection(in));
        case BOOLEAN:
          return new DlmsDataBoolean(Boolean.valueOf(value));
        case BIT_STRING:
          return new DlmsDataBitString(parseBitString(value));
        case DOUBLE_LONG:
          return new DlmsDataDoubleLong(Integer.parseInt(value));
        case DOUBLE_LONG_UNSIGNED:
          return new DlmsDataDoubleLongUnsigned(Long.parseLong(value));
        case FLOATING_POINT:
          return new DlmsDataFloatingPoint(numberFormat.parse(value).floatValue());
        case OCTET_STRING:
          return new DlmsDataOctetString(CodingUtils.string2ByteArray(value));
        case VISIBLE_STRING:
          return new DlmsDataVisibleString(value);
        case BCD:
          return new DlmsDataBcd(Integer.parseInt(value));
        case INTEGER:
          return new DlmsDataInteger(Integer.parseInt(value));
        case LONG:
          return new DlmsDataLong(Integer.parseInt(value));
        case UNSIGNED:
          return new DlmsDataUnsigned(Integer.parseInt(value));
        case LONG_UNSIGNED:
          return new DlmsDataLongUnsigned(Integer.parseInt(value));
//      case COMPACT_ARRAY:
//        return decodeCompactArray(in);
        case LONG64:
          return new DlmsDataLong64(Long.parseLong(value));
        case LONG64_UNSIGNED:
          return new DlmsDataLong64Unsigned(new BigInteger(value));
        case ENUM:
          return new DlmsDataEnum(Integer.parseInt(value));
        case FLOAT32:
          return new DlmsDataFloat32(numberFormat.parse(value).floatValue());
        case FLOAT64:
          return new DlmsDataFloat64(numberFormat.parse(value).doubleValue());
        case DATE_TIME:
          return new DlmsDataDateTime(dateParser.parseDateTime(value, false));
        case DATE:
          return new DlmsDataDate(dateParser.parseDate(value, false));
        case TIME:
          return new DlmsDataTime(dateParser.parseTime(value, false));
//      case DONT_CARE:
//        return new DlmsDataDontCare();
        default:
          throw new ParseException("Type not supported: " + dataType, 0);
      }
    }
    catch (Exception ex)
    {
      throw new ParseException(ex.getMessage(), 0);
    }


  }

  /**
   * Creates a {@link DlmsData } object of specified {@link DlmsData.DataType} by parsing the specified
   * number. <P> Only types of the {@link #PARSEABLE_NUMBER_TYPES } can be created by this method.
   *
   * @param dataType The data type for the object.
   * @param value The string to parse.
   * @return The created object.
   */
  public DlmsData parse(final DlmsData.DataType dataType, final Number value)
  {
    switch (dataType)
    {
//      case NULL_DATA:
//        return new DlmsDataNull();
//      case ARRAY:
//        return new DlmsDataArray(decodeCollection(in));
//      case STRUCTURE:
//        return new DlmsDataStructure(decodeCollection(in));
//      case BOOLEAN:
//        return new DlmsDataBoolean(Boolean.valueOf(value));
//      case BIT_STRING:
//        return new DlmsDataBitString(in.readBitString());
      case DOUBLE_LONG:
        return new DlmsDataDoubleLong(value.intValue());
      case DOUBLE_LONG_UNSIGNED:
        return new DlmsDataDoubleLongUnsigned(value.longValue());
      case FLOATING_POINT:
        return new DlmsDataFloatingPoint(value.floatValue());
//      case OCTET_STRING:
//        return new DlmsDataOctetString(CodingUtils.string2ByteArray(value));
//      case VISIBLE_STRING:
//        return new DlmsDataVisibleString(value);
      case BCD:
        return new DlmsDataBcd(value.intValue());
      case INTEGER:
        return new DlmsDataInteger(value.intValue());
      case LONG:
        return new DlmsDataLong(value.intValue());
      case UNSIGNED:
        return new DlmsDataUnsigned(value.intValue());
      case LONG_UNSIGNED:
        return new DlmsDataLongUnsigned(value.intValue());
//      case COMPACT_ARRAY:
//        return decodeCompactArray(in);
      case LONG64:
        return new DlmsDataLong64(value.longValue());
      case LONG64_UNSIGNED:
      {
        if (value instanceof BigInteger)
        {
          return new DlmsDataLong64Unsigned((BigInteger)value);
        }
        else if (value instanceof BigDecimal)
        {
          return new DlmsDataLong64Unsigned(((BigDecimal)value).toBigInteger());
        }
        else
        {
          return new DlmsDataLong64Unsigned(value.longValue());
        }
      }
      case ENUM:
        return new DlmsDataEnum(value.intValue());
      case FLOAT32:
        return new DlmsDataFloat32(value.floatValue());
      case FLOAT64:
        return new DlmsDataFloat64(value.doubleValue());
//      case DATE_TIME:
//        return new DlmsDataDateTime(in.readOctetString(12));
//      case DATE:
//        return new DlmsDataDate(in.readOctetString(5));
//      case TIME:
//        return new DlmsDataTime(in.readOctetString(4));
//      case DONT_CARE:
//        return new DlmsDataDontCare();
    }

    throw new UnsupportedOperationException("Type not supported: " + dataType);
  }

  /**
   * Check if a string can be parsed to create the specified type.
   *
   * @param dataType The data type.
   * @return true if {@link #parse(com.elster.dlms.types.data.DlmsData.DataType, java.lang.String)} can be
   * used for specified type
   */
  public boolean isStringParsable(final DlmsData.DataType dataType)
  {
    return PARSEABLE_STRING_TYPES.contains(dataType);
  }

  /**
   * Checks if a number can be parsed to create the specified type.
   *
   * @param dataType The data type.
   * @return true if {@link #parse(com.elster.dlms.types.data.DlmsData.DataType, java.lang.Number)} can be
   * used for specified type
   */
  public boolean isNumerParsable(final DlmsData.DataType dataType)
  {
    return PARSEABLE_NUMBER_TYPES.contains(dataType);
  }

  private final static Pattern BIT_STRING_PATTERN = Pattern.compile("([01]\\s*)*"); // 0 and 1 with white space.

  private BitString parseBitString(final String string) throws ParseException
  {
    if (!BIT_STRING_PATTERN.matcher(string).matches())
    {
      throw new ParseException("Error in bit string:" + string, 0);
    }

    //count the bits.
    int c = 0;
    for (int i = 0; i < string.length(); i++)
    {
      final char chr = string.charAt(i);
      if (chr == '1' || chr == '0')
      {
        c++;
      }
    }

    //build the bit string.
    final BitStringBuilder bitStringBuilder = new BitStringBuilder(c);

    int pos = 0;

    for (int i = 0; i < string.length(); i++)
    {
      final char chr = string.charAt(i);
      {
        switch (chr)
        {
          case '1':
          {
            bitStringBuilder.setBit(pos, true);
            pos++;
            break;
          }
          case '0':
          {
            pos++;
            break;
          }
        }
      }
    }
    return bitStringBuilder.toBitString();
  }

  public DlmsDateParser getDateParser()
  {
    return dateParser;
  }

}
