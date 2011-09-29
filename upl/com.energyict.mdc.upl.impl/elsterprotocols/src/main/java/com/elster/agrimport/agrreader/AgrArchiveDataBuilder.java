/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrArchiveDataBuilder.java $
 * Version:     
 * $Id: AgrArchiveDataBuilder.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 17:12:34
 */
package com.elster.agrimport.agrreader;

import java.util.Date;

/**
 * This class builds AgrArchiveData instances using an AgrReader.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrArchiveDataBuilder
{
  public static AgrArchiveData buildArchiveData(AgrReader reader) throws AgrFileException
  {
    AgrArchiveData archiveData = new AgrArchiveData();

    reader.readHeader();

    archiveData.setFileHeadLine(reader.getAgrFileHeadLine());
    archiveData.setFileType(reader.getFileType());
    archiveData.getColumnHeaders().addAll(reader.getColumnHeaders());

    readLines(reader, archiveData);

    return archiveData;
  }

  private static int findCol(AgrArchiveData archiveData, IAgrColTypeDefinitions.AgrColType colType)
  {
    int index = -1;

    for (int i = 0; i < archiveData.getColumnHeaders().size(); i++)
    {
      if (archiveData.getColumnHeaders().get(i).getColumnType() == colType)
      {
        index = i;
        break;
      }
    }

    return index;
  }

  private static void readLines(AgrReader reader, AgrArchiveData archiveData) throws
          AgrFileException
  {

    int indexTimeStamp = findCol(archiveData, IAgrColTypeDefinitions.AgrColType.TIMESTAMP);
    int indexOrderNumber = findCol(archiveData, IAgrColTypeDefinitions.AgrColType.ORDERNUMBER);
    int indexGlobalOrdernumber = findCol(archiveData,
                                         IAgrColTypeDefinitions.AgrColType.GLOBORDERNUMBER);
    int lineNo = 0;

    AgrArchiveLine archiveLine = reader.buildNextAgrLine();

    while (archiveLine != null)
    {
      ExtAgrArchiveLine extLine = new ExtAgrArchiveLine(archiveLine.size());
      extLine.setValues(archiveLine);
      extLine.setLineNo(lineNo);

      if (indexTimeStamp >= 0)
      {
        extLine.setDate((Date)extLine.getValue(indexTimeStamp).getValue());
      }
      if (indexOrderNumber >= 0)
      {
        extLine.setOrderNo((Long)extLine.getValue(indexOrderNumber).getValue());
      }
      if (indexGlobalOrdernumber >= 0)
      {
        extLine.setGlobalOrderNo((Long)extLine.getValue(indexGlobalOrdernumber).getValue());
      }
      archiveData.getLines().add(extLine);

      lineNo++;
      archiveLine = reader.buildNextAgrLine();
    }
  }

}
