/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrReader.java $
 * Version:     
 * $Id: AgrReader.java 1788 2010-07-26 13:35:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.07.2009 10:19:52
 */
package com.elster.agrimport.agrreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class parses AGR files.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrReader
{
  private static final Logger LOGGER = Logger.getLogger(AgrColumnHeader.class.getName());
  private BufferedReader reader;
  private boolean eof = false;
  private AgrFileHeadLine agrFileHeadLine;
  private Map<String, String> header = new HashMap<String, String>();
  private List<AgrColumnHeader> columnHeaders =
          new ArrayList<AgrColumnHeader>();
  private int rawColumnCount; // original column count - without combined status columns
  private int columnCount;
  private AgrFileType fileType = AgrFileType.UNKNOWN;
  private final Pattern tabSplitter = Pattern.compile("\t");
  private boolean combineStatusColumns;
  private AgrLineParser lineParser = null;
  private boolean headersRead = false;
  private DateFormat dateTimeFormat;

  /**
   * Creates the AgrReader<P>
   *
   * @param in                   An input stream for reading the file contents.
   * @param combineStatusColumns If {@code true} the status column will be combined with the value columns (if possible)
   */
  public AgrReader(InputStream in, boolean combineStatusColumns)
  {
    this.reader = new BufferedReader(new InputStreamReader(in));
    this.combineStatusColumns = combineStatusColumns;
    this.dateTimeFormat = AgrReaderUtils.createDateFormat(null,null,null,null,null);
    this.dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "device-time"));
  }


  /**
   * Create the AGR reader.<P>
   *
   *
   * @param in  An input stream for reading the file contents.
   * @param combineStatusColumns If {@code true} the status column will be combined with the value columns (if possible)
   * @param dateTimeFormat The DateFormat for decoding timestamps in AGR files (usually created by {@link AgrReaderUtils#createDateFormat(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}).
   *  If {@code dateTimeFormat} is {@code null} an default DateFormat will be used.
   * @param charsetName The name of the an charset to decode non ASCII chars in the AGR file.
   *  If {@code null} or emtpy the default charset is used.
   * @throws UnsupportedEncodingException - in case of error
   */
  public AgrReader(InputStream in, boolean combineStatusColumns, DateFormat dateTimeFormat, String charsetName)
          throws UnsupportedEncodingException
  {
    if ((charsetName != null) && (charsetName.length() > 0))
    {
      this.reader = new BufferedReader(new InputStreamReader(in, charsetName));
    }
    else
    {
      this.reader = new BufferedReader(new InputStreamReader(in));
    }

    this.combineStatusColumns = combineStatusColumns;

    if (dateTimeFormat != null)
    {
      this.dateTimeFormat = (DateFormat)dateTimeFormat.clone();
    }
    else
    {
      this.dateTimeFormat =  AgrReaderUtils.createDateFormat(null,null,null,null,null);
    }

    this.dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "device-time"));
  }

  public AgrReader(BufferedReader reader, boolean combineStatusColumns)
  {
    this(reader, combineStatusColumns, null);
  }

  public AgrReader(BufferedReader reader, boolean combineStatusColumns, DateFormat dateTimeFormat)
  {
    this.reader = reader;
    this.combineStatusColumns = combineStatusColumns;

    if (dateTimeFormat != null)
    {
      this.dateTimeFormat = (DateFormat)dateTimeFormat.clone();
    }
    else
    {
      this.dateTimeFormat =  AgrReaderUtils.createDateFormat(null,null,null,null,null);
    }

    this.dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "device-time"));
  }

  /**
   * Reads the headers of the agr-file<P>
   * This method
   * The headers will only be read once. Further calls will be ignored.
   *
   * @throws AgrFileException - in case of an error
   */
  public void readHeader() throws AgrFileException
  {
    if (!headersRead)
    {
      headersRead = true;
      try
      {
        readDataElement();
        readInfoLine();
        readAgrHeadLines();
        determineFileType();
        readColumnHeaders();
      }
      catch (AgrFileException ex)
      {
        headersRead = false;
        throw ex;
      }
      catch (Exception ex)
      {
        headersRead = false;
        throw new AgrFileException(ex);
      }
    }
  }

  private void checkHeadersRead()
  {
    if (!headersRead)
    {
      throw new IllegalStateException("readHeader must be called first");
    }
  }

  private String[] readNextArchiveLine() throws AgrFileException
  {

    String line = readLine().trim();
    if (endOfFile() && (line.length() == 0))
    {
      return null;
    }

    String[] result = tabSplitter.split(line, -1);

    if (result.length != rawColumnCount)
    {
      throw new AgrFileException("Wrong column count in archive");
    }

    return result;
  }

  /**
   * Builds the next archive line.<P>
   * If the end of file was reached null will be returned.
   *
   * @return The next archive line or null if the end of file was reached.
   * @throws AgrFileException - in case of an error
   */
  public AgrArchiveLine buildNextAgrLine() throws AgrFileException
  {
    checkHeadersRead();

    String[] values = readNextArchiveLine();
    if (values == null)
    {
      return null;
    }

    if (values.length != rawColumnCount)
    {
      throw new AgrFileException("Wrong column count in archive body");
    }

    if (lineParser == null)
    {
      initLineParser();
    }

    return lineParser.buildArchiveLine(values);
  }

  private void initLineParser() throws AgrFileException
  {
    lineParser = new AgrLineParser(columnHeaders, fileType,dateTimeFormat);
  }

  private void readDataElement() throws AgrFileException
  {
    boolean found = false;
    while (!endOfFile() && !found)
    {
      String line = readLine();
      found = line.trim().equals("[DATA]");
    }

    if (!found)
    {
      throw new AgrFileException("[DATA] Section not found");
    }
  }

  private void readInfoLine() throws AgrFileException
  {
    String line = "";
    String[] parts = null;
    boolean found = false;
    while (!eof && !found)
    {
      line = readLine();
      parts = tabSplitter.split(line);
      found = parts[0].toUpperCase().equals("M");
    }

    if (!found)
    {
      throw new AgrFileException("M line not found");
    }

    if (parts.length < 4)
    {
      throw new AgrFileException("M line missing elements");
    }

    agrFileHeadLine = new AgrFileHeadLine();
    agrFileHeadLine.parseLine(line);
  }

  private String peekedLine = null;

  private String peekLine() throws AgrFileException
  {
    if (peekedLine == null)
    {
      peekedLine = readLine();
    }

    return peekedLine;
  }

  private void readAgrHeadLines() throws AgrFileException
  {
    String peek = peekLine();
    while (peek.startsWith(";"))
    {
      String line = readLine();

      if (line.length() > 1)
      {
        int pos = line.indexOf('=');

        if (pos >= 0)
        {
          String key = line.substring(1, pos);
          String value = line.substring(pos + 1);
          getHeader().put(key, value);
        }
        else
        {
          if (!line.startsWith(";@"))
          {
            LOGGER.warning("Unknown header entry in agr file: " + line);
          }
        }
      }
      peek = peekLine();
    }
  }

  private String readLine() throws AgrFileException
  {

    if (peekedLine != null)
    {
      String result = peekedLine;
      peekedLine = null;
      return result;
    }

    try
    {
      String line = reader.readLine();

      if (line == null)
      {
        eof = true;
        return "";
      }

      return line;
    }
    catch (IOException ex)
    {
      LOGGER.log(Level.SEVERE, "Error reading agr line", ex);
      throw new AgrFileException("Error reading agr line", ex);
    }
  }

  private void readColumnHeaders() throws AgrFileException
  {

    /* find 1. column header line */
    String line = "";
    boolean found = false;
    while (!eof)
    {
      line = readLine();
      if (line.startsWith("#"))
      {
        break;
      }
    }

    if (eof)
    {
      throw new AgrFileException("readColumnHeaders: Premature end of file");
    }

    String[] columnHeader1 = tabSplitter.split(line, -1);
    String[] columnHeader2 = tabSplitter.split(readLine(), -1);
    String[] columnHeader3 = tabSplitter.split(readLine(), -1);

    columnCount = columnHeader1.length;
    rawColumnCount = columnCount;

    if (!columnHeader1[0].equals("#") || !columnHeader2[0].equals("E") || !columnHeader3[0].equals(
            "O"))
    {
      throw new AgrFileException("readColumnHeaders: Error in archive column header lines");
    }


    if (columnHeader2.length != getColumnCount() || columnHeader3.length != getColumnCount())
    {
      throw new AgrFileException("The wrong count of columns in the column headers");
    }


    for (int i = 0; i < getColumnCount(); i++)
    {
      AgrColumnHeader columnHeader;
      switch (fileType)
      {
        case DSFG:
          columnHeader = new AgrColumnHeaderDsfg(columnHeader1[i], columnHeader2[i],
                                                 columnHeader3[i],
                                                 i, getColumnCount());
          break;
        case LIS200:
          columnHeader = new AgrColumnHeaderLis200(columnHeader1[i], columnHeader2[i],
                                                   columnHeader3[i],
                                                   i, getColumnCount());
          break;
        default:
          throw new IllegalStateException("Unexpected file type: " + fileType);
      }
      getColumnHeaders().add(columnHeader);
    }

    if (combineStatusColumns)
    {
      combineStatusColumns();
    }
  }

  private void combineStatusColumns()
  {
    List<AgrColumnHeader> combinedHeaders = new ArrayList<AgrColumnHeader>(columnHeaders.size());

    int c = columnHeaders.size();
    for (int i = 0; i < c; i++)
    {
      AgrColumnHeader columnHeader = columnHeaders.get(i);
      AgrColumnHeader nextColumnHeader = null;
      if (i + 1 < c)
      {
        nextColumnHeader = columnHeaders.get(i + 1);
      }

      boolean nextColumnHeaderIsStatus = nextColumnHeader != null
                                         && nextColumnHeader.getColumnType()
                                            == AgrColumnHeader.AgrColType.STATUS_INT;

      if (nextColumnHeaderIsStatus)
      {
        switch (columnHeader.getColumnType())
        {
          case INTERVALL:
          case COUNTER:
          case NUMBER:
            combinedHeaders.add(new AgrColumnHeaderStated(columnHeader, nextColumnHeader, i, c));
            i++;
            break;
          default:
            combinedHeaders.add(columnHeader);
        }
      }
      else
      {
        combinedHeaders.add(columnHeader);
      }
    }

    columnCount = combinedHeaders.size();
    columnHeaders = combinedHeaders;
  }

  private boolean endOfFile()
  {
    return eof;
  }

  /**
   * @return the header
   */
  public Map<String, String> getHeader()
  {
    checkHeadersRead();
    return header;
  }

  /**
   * @return the columnHeaders
   */
  public List<AgrColumnHeader> getColumnHeaders()
  {
    checkHeadersRead();
    return columnHeaders;
  }

  /**
   * Returns the count of the columns of the archive value part of the agr file.
   *
   * @return the columnCount
   */
  public int getColumnCount()
  {
    checkHeadersRead();
    return columnCount;
  }

  /**
   * Returns information about the agr file head line.
   *
   * @return the agrFileHeadLine
   */
  public AgrFileHeadLine getAgrFileHeadLine()
  {
    checkHeadersRead();
    return agrFileHeadLine;
  }

  public Date parseDate(String dateString) throws AgrFileException
  {
    try
    {
      return dateTimeFormat.parse(dateString);
    }
    catch (ParseException ex)
    {
      LOGGER.log(Level.WARNING, "Error parsing date: " + dateString, ex);
      throw new AgrFileException("Error parsing date: " + dateString, ex);
    }
  }

  private void determineFileType()
  {
    if (getHeader().containsKey("HEADTYPE"))
    {
      fileType = AgrFileType.LIS200;
    }
    else
    {
      fileType = AgrFileType.DSFG;
    }
  }

  /**
   * Returns the file type (LIS200 or DSfG) of the agr file.
   *
   * @return
   * @throws AgrFileException
   */
  public AgrFileType getFileType()
  {
    checkHeadersRead();
    return fileType;
  }

}
