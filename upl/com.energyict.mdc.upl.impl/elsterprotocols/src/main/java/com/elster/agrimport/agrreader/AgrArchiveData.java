/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrArchiveData.java $
 * Version:     
 * $Id: AgrArchiveData.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 17:02:17
 */
package com.elster.agrimport.agrreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds the data of one AGR file.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrArchiveData
{
  List<ExtAgrArchiveLine> lines = new ArrayList<ExtAgrArchiveLine>();
  AgrFileHeadLine fileHeadLine;
  private Map<String, String> header = new HashMap<String, String>();
  private List<AgrColumnHeader> columnHeaders = new ArrayList<AgrColumnHeader>();
  private AgrFileType fileType = AgrFileType.UNKNOWN;

  public List<AgrColumnHeader> getColumnHeaders()
  {
    return columnHeaders;
  }

  public AgrFileHeadLine getFileHeadLine()
  {
    return fileHeadLine;
  }

  public void setFileHeadLine(AgrFileHeadLine fileHeadLine)
  {
    this.fileHeadLine = fileHeadLine;
  }

  public AgrFileType getFileType()
  {
    return fileType;
  }

  public void setFileType(AgrFileType fileType)
  {
    this.fileType = fileType;
  }

  public Map<String, String> getHeader()
  {
    return header;
  }

  public List<ExtAgrArchiveLine> getLines()
  {
    return lines;
  }

}
