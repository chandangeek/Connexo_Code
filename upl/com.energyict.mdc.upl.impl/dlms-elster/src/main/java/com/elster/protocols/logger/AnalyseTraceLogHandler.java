/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/logger/AnalyseTraceLogHandler.java $
 * Version:     
 * $Id: AnalyseTraceLogHandler.java 4878 2012-07-20 08:38:05Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 20, 2010 2:56:30 PM
 */
package com.elster.protocols.logger;

import com.elster.coding.CodingUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This log handler writes a AnalyseTrace compatible log file.
 *
 * @author osse
 */
public class AnalyseTraceLogHandler implements ILogHandler
{
  private final String fileName;
  private BufferedWriter writer;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.US);

  public AnalyseTraceLogHandler(final String fileName)
  {
    this.fileName = fileName;
  }

  //@Override
  public void open() throws IOException
  {
    writer = new BufferedWriter(new FileWriter(fileName));
  }

  //@Override
  public void close() throws IOException
  {
    writer.close();
  }

  public void logPacket(final LogPacket logPacket) throws IOException
  {
    switch (logPacket.getPacketType())
    {
      case DATA:
        logDataPacket((LogPacketData)logPacket);
        break;
      default:
        logComment(logPacket);
    }
  }

  private void logDataPacket(final LogPacketData dataPacket) throws IOException
  {
    final String timeStamp = dateFormat.format(dataPacket.getTimestamp());

    for (byte b : dataPacket.getData())
    {
      writer.write(timeStamp);
      if (dataPacket.getDirection() == LogDirection.IN)
      {
        writer.write("<   . (");
        writer.write(CodingUtils.intToHex(b & 0xFF, 2));
        writer.write(")\r\n");
      }
      else
      {
        writer.write("> .   (");
        writer.write(CodingUtils.intToHex(b & 0xFF, 2));
        writer.write(")\r\n");
      }
    }
    writer.flush();
  }

  private void logComment(final LogPacket commentPacket) throws IOException
  {
    final String timeStamp = dateFormat.format(commentPacket.getTimestamp());
    writer.write(timeStamp);
    writer.write(" ");
    writer.write(commentPacket.getInfo());
    writer.write(")\r\n");
    writer.flush();
  }
}
