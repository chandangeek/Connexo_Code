/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrLineParser.java $
 * Version:     
 * $Id: AgrLineParser.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 11:05:31
 */
package com.elster.agrimport.agrreader;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class parses archive lines from an AGR file.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
class AgrLineParser
{
  private final List<AgrColumnHeader> columnHeaders;
  private final AgrFileType fileType;
  private List<Parser> parsers;
  private final int colCount;
  private ValueStream valueStream = new ValueStream();
  private final DateFormat dateTimeFormat;


  public AgrLineParser(final List<AgrColumnHeader> columnHeaders, final AgrFileType fileType, DateFormat dateTimeFormat) throws
          AgrFileException
  {
    this.columnHeaders = columnHeaders;
    this.fileType = fileType;
    this.colCount = columnHeaders.size();
    this.dateTimeFormat= dateTimeFormat;
    buildParser();
  }

  public AgrArchiveLine buildArchiveLine(final String[] values) throws AgrValueParsingException
  {
    AgrArchiveLine archiveLine = new AgrArchiveLine(colCount);
    valueStream.setValues(values);

    for (Parser p : parsers)
    {
      archiveLine.add(p.buildValue(valueStream));
    }

    return archiveLine;
  }

  private void buildParser() throws AgrFileException
  {

    parsers = new ArrayList<Parser>(columnHeaders.size());

    for (AgrColumnHeader columnHeader : columnHeaders)
    {
      switch (columnHeader.getColumnType())
      {
        case COUNTER:
        case INTERVALL:
        case NUMBER:
          parsers.add(new ParserBigDecimal());
          break;
        case STATED_COUNTER:
        case STATED_INTERVALL:
        case STATED_NUMBER:
          parsers.add(new ParserStatedBigDecimal());
          break;
        case STATUS_STRING:
        case DATA:
        case APHANUMERIC:
        case UNKNOWN:
          parsers.add(new ParserString());
          break;
        case ORDERNUMBER:
        case GLOBORDERNUMBER:
          parsers.add(new ParserLong());
          break;
        case TIMESTAMP:
          parsers.add(new ParserDate(dateTimeFormat));
          break;
        case STATUS_INT:
          parsers.add(new ParserInt());
          break;
        case HEX:
          parsers.add(new ParserHex());
          break;
        case STATUS_REGISTER:
          parsers.add(new ParserStatusregister());
          break;
        default:
          throw new AgrFileException("Unexpected type to parse: " + columnHeader.getColumnType());
      }
    }


  }

    public AgrFileType getFileType() {
        return fileType;
    }

    public static class ValueStream
  {
    private String[] values;
    private int index;

    public ValueStream()
    {
    }

    /**
     * Constructor for testing purposes.
     *
     * @param values - strings...
     */
    public ValueStream(String... values)
    {
      this.values = values;
    }

    public void setValues(final String[] values)
    {
      this.values = values;
      index = 0;
    }

    String getNext()
    {
      return values[index++];
    }

  }

  public interface Parser<T extends IAgrValue>
  {
    T buildValue(ValueStream values) throws AgrValueParsingException;

  }

  public static class ParserBigDecimal implements Parser<AgrValueBigDecimal>
  {
    public AgrValueBigDecimal buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        return new AgrValueBigDecimal(new BigDecimal(values.getNext()));
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Wrong number format", ex);
      }
    }

  }

  public static class ParserStatedBigDecimal implements Parser<AgrValueStatedBigDecimal>
  {
    public AgrValueStatedBigDecimal buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        return new AgrValueStatedBigDecimal(new BigDecimal(values.getNext()), Integer.parseInt(
                values.getNext()));
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Wrong number format", ex);
      }
    }

  }

  public static class ParserLong implements Parser<AgrValueLong>
  {
    public AgrValueLong buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        return new AgrValueLong(Long.parseLong(values.getNext()));
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Wrong number format", ex);
      }
    }

  }

  public static class ParserInt implements Parser<AgrValueInt>
  {
    public AgrValueInt buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        return new AgrValueInt(Integer.parseInt(values.getNext()));
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Wrong number format", ex);
      }
    }

  }

  public static class ParserHex implements Parser<AgrValueInt>
  {
    public AgrValueInt buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        String value = values.getNext();
        if (value.startsWith("0x"))
        {
          return new AgrValueInt(Integer.parseInt(value.substring(2), 16));
        }
        else
        {
          return new AgrValueInt(Integer.parseInt(value, 16));
        }
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Wrong number format", ex);
      }
    }

  }

  public static class ParserStatusregister implements Parser<AgrValueStatusregister>
  {
    private Pattern statusSplitter = Pattern.compile(";");

    public AgrValueStatusregister buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        String[] states = statusSplitter.split(values.getNext(), -1);

        List<Integer> stateList = new ArrayList<Integer>(states.length);

        for (String state : states)
        {
          int s = Integer.parseInt(state);
          stateList.add(s);
        }
        return new AgrValueStatusregister(stateList);
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Wrong number format", ex);
      }
    }

  }

  public static class ParserString implements Parser<AgrValueString>
  {
    public AgrValueString buildValue(ValueStream values) throws AgrValueParsingException
    {
      try
      {
        return new AgrValueString(values.getNext());
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Error reading string", ex);
      }
    }

  }

  public static class ParserDate implements Parser<AgrValueDate>
  {
    private final DateFormat formatter;

    public ParserDate(DateFormat formatter)
    {
      this.formatter = formatter;
    }


    /*
    private boolean formatterReady = false;
    private boolean checkDateFormat(String regEx, String dateString)
    {
      //Pattern pattern= Pattern.compile(regEx);
      return Pattern.matches(regEx, dateString);
    }

    private void initFormatter(String value) throws AgrFileException, ParseException
    {

      //DE: dd.MM.yyyy HH:mm:ss  ^\d{1,2}\.\d{1,2}\.\d{4} \d{1,2}:\d{1,2}:\d{1,2}$

      //Bangladesh: 04/06/2005 11:59:00 AM ^\d{1,2}\/\d{1,2}\/\d{4} \d{1,2}:\d{1,2}:\d{1,2} (AM|PM)$
      //todo: test of  04/06/2005 00:00:00

      //Griechenland: 06/11/2005 23:00:00 ^\d{1,2}\/\d{1,2}\/\d{4} \d{1,2}:\d{1,2}:\d{1,2}$


      if (checkDateFormat("^\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}$", value))
      {
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(new SimpleTimeZone(0, "unknown"));

        Calendar cal = formatter.getCalendar();
        cal.setTime(formatter.parse(value));

        formatterReady = cal.get(Calendar.HOUR_OF_DAY) != 0;
      }
      else
      {
        if (checkDateFormat("^\\d{1,2}\\/\\d{1,2}\\/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} (AM|PM)$",
                            value))
        {
          formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.ENGLISH);
          formatter.setTimeZone(new SimpleTimeZone(0, "unknown"));
          formatterReady = true;
        }
        else
        {
          if (checkDateFormat("^\\d{1,2}\\/\\d{1,2}\\/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}$", value))
          {
            formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(new SimpleTimeZone(0, "unknown"));
            formatterReady = true;
          }
          else
          {
            //todo Fallback to systems local date format.
            throw new AgrFileException("unknown date format: " + value);
          }
        }
      }

    }
     *
     */


    public AgrValueDate buildValue(ValueStream values) throws AgrValueParsingException
    {
      String value = "";
      try
      {
        value = values.getNext();
//        if (!formatterReady)
//        {
//          initFormatter(value);
//        }

        return new AgrValueDate(formatter.parse(value));
      }
      catch (Exception ex)
      {
        throw new AgrValueParsingException("Error reading date: " + value, ex);
      }
    }

  }

}
