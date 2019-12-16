/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/ObisFileReader.java $
 * Version:     
 * $Id: ObisFileReader.java 4495 2012-05-11 12:39:19Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.07.2010 09:42:15
 */
package com.elster.obis;

import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.obis.IRangeMap.Pair;
import com.elster.obis.ObisCodeRange.GroupRange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read the OBIS code definitions from a file.<P>
 * The file must have the format of the definition file of the CTT.
 *
 * @author osse
 */
public class ObisFileReader
{
  //private static final Logger LOGGER = Logger.getLogger(ObisFileReader.class.getName());
  private final static String COMMENT_HEADER = "com";
  private final static String DEF_HEADER = "def";
  private final BufferedReader reader;
  private final Map<String, GroupRange> defs = new HashMap<String, GroupRange>();
  private final Pattern lineSplitter = Pattern.compile("\t");
  private final Pattern commaSplitter = Pattern.compile(",");
  private final Pattern equalSplitter = Pattern.compile("=");
  //private final Pattern defPattern = Pattern.compile("^\\s*([abcdef])\\s*=\\s*(\\d*)-(\\d*)\\s*$");
  //private final Pattern groupNamePattern = Pattern.compile("^\\s*([abcdefy&])\\s*$");
  private final Pattern singleValuePattern = Pattern.compile("^\\s*(\\d+)\\s*$");
  private final Pattern rangePattern = Pattern.compile("(\\d+)-(\\d+)");
  private final List<Pair<ObisCodeDef>> obisCodeDefs = new ArrayList<Pair<ObisCodeDef>>();
  private final NameTable nameTable = new NameTable();

  public ObisFileReader(final InputStream in)
  {
    reader = new BufferedReader(new InputStreamReader(in));
    defs.put("&", buildRanges("0-7"));
  }

  public List<Pair<ObisCodeDef>> read() throws IOException
  {
    int lineNo = 0;
    String line = "";

    try
    {
      line = reader.readLine();

      while (line != null)
      {
        lineNo++;
        processLine(line);
        line = reader.readLine();
      }

      return obisCodeDefs;
    }
    catch (Exception ex)
    {

      final IOException ioEx = new IOException("Error parsing line " + lineNo + ". Content: " + line
                                               + " Exception:" + ex.getMessage());
      ioEx.initCause(ex);
      throw ioEx;
    }
  }

  private void processLine(final String line) throws IOException
  {
    if (line.trim().length() == 0)
    {
      return;
    }

    final String[] cols = lineSplitter.split(line);

    if (cols.length >= 2 && DEF_HEADER.equals(cols[0]))
    {
      //def line
      processDef(cols);
    }
    else if (cols.length >= 7 && cols[0].length() == 0 && cols[1].length() > 0)
    {
      //normal line
      processObisDef(cols);
    }
    else if (cols.length >= 7 && cols[0].startsWith("N"))
    {
      //name table entry
      processNameTableEntry(cols);
    }
    else if (cols.length >= 1 && cols[0].startsWith(COMMENT_HEADER))
    {
      //comment - ignore
    }
    else
    {
      throw new IllegalStateException("Unable to parse line:" + line); //Throwing illegal state exception, because the definition files are a fixed part of the library.
    }

  }

  private void processNameTableEntry(final String[] cols) throws IOException
  {
    nameTable.add(cols[0], buildPair(cols));
  }

  private void processObisDef(final String[] cols) throws IOException
  {
    obisCodeDefs.add(buildPair(cols));
  }

  private Pair<ObisCodeDef> buildPair(final String[] cols) throws IOException
  {
    GroupRange[] ranges = new ObisCodeRange.GroupRange[6];

    for (int i = 0; i < 6; i++)
    {
      ranges[i] = buildRanges(cols[i + 1]);
    }

    final ObisCodeRange obisCodeRange = new ObisCodeRange(ranges);

    final String[] groupDescriptions = new String[6];
    for (int i = 1; i < 6; i++)
    {
      if (cols.length <= i + 6)
      {
        break;
      }
      groupDescriptions[i - 1] = cols[i + 6];
    }

    DlmsDataTypeSet dataTypes = null;

    if (cols.length > 14 && cols[14].length() != 0)
    {
      if (cols[14].equals("*"))
      {
        dataTypes = new DlmsDataTypeSet(EnumSet.allOf(DlmsData.DataType.class));
      }
      else
      {
        final EnumSet<DlmsData.DataType> typeSet = EnumSet.noneOf(DlmsData.DataType.class);
        final String[] dataTypeStrings = cols[14].split(",");
        for (String dataTypeString : dataTypeStrings)
        {
          final DataType dataType =
                  DlmsData.DataType.findDataType(Integer.parseInt(dataTypeString.trim()));
          if (dataType == null)
          {
            throw new IOException("No data type found for: " + dataTypeString.trim());
          }
          typeSet.add(dataType);
        }
        dataTypes = new DlmsDataTypeSet(typeSet);
      }
    }

    final ObisCodeDef obisCodeDef = new ObisCodeDef(groupDescriptions, nameTable, dataTypes);


    //obisCodeDef.setOrgLine(orgLine);

    return new Pair<ObisCodeDef>(obisCodeRange, obisCodeDef);

  }

  private void processDef(final String[] cols)
  {
    final String defString = cols[1];
    final String[] parts = equalSplitter.split(defString);
    if (parts.length != 2)
    {
      throw new IllegalStateException("unable to parse:" + defString); //Throwing illegal state exception, because the definition files are a fixed part of the library.
    }
    defs.put(parts[0].trim(), buildRanges(parts[1]));
  }

  private GroupRange buildRanges(final String string)
  {

    GroupRange result = defs.get(string.trim());
    if (result == null)
    {
      final String[] rangeStrings = commaSplitter.split(string);
      GroupRange[] ranges = new GroupRange[rangeStrings.length];

      for (int i = 0; i < rangeStrings.length; i++)
      {
        ranges[i] = buildRange(rangeStrings[i]);
      }

      if (ranges.length == 1)
      {
        result = ranges[0];
      }
      else
      {
        result = GroupRange.valueOf(ranges);
      }

      defs.put(string, result);
    }

    return result;

  }

  private GroupRange buildRange(final String string)
  {
    if (string.length() == 0)
    {
      return GroupRange.valueOf(0, 255);
    }

    final Matcher rangeMatcher = rangePattern.matcher(string);
    if (rangeMatcher.find())
    {
      return GroupRange.valueOf(Integer.parseInt(rangeMatcher.group(1)), Integer.parseInt(rangeMatcher.group(
              2)));
    }

    final Matcher singleValueMatcher = singleValuePattern.matcher(string);
    if (singleValueMatcher.find())
    {
      return GroupRange.valueOf(Integer.parseInt(singleValueMatcher.group(1)));

    }
    throw new IllegalStateException("Unable to parse " + string); //Throwing illegal state exception, because the definition file is part of jar.
  }

//  public List<ObisCodeDef> getObisCodeDefs()
//  {
//    return obisCodeDefs;
//  }

}
