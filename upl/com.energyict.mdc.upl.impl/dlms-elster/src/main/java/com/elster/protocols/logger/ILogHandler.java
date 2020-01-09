/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/logger/ILogHandler.java $
 * Version:
 * $Id: ILogHandler.java 4878 2012-07-20 08:38:05Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 21, 2010 8:43:27 AM
 */
package com.elster.protocols.logger;

import com.elster.coding.CodingUtils;
import java.io.IOException;
import java.util.Date;

/**
 * Interface for log handlers.
 *
 * @author osse
 */
public interface ILogHandler
{
  public void open() throws IOException;

  public void close() throws IOException;

  public void logPacket(LogPacket logPacket) throws IOException;

  public static abstract class LogPacket
  {
    public enum PacketType
    {
      DATA, COMMENT
    }

    private final LogDirection direction;
    private final long timestamp;

    public LogPacket(final LogDirection direction, final Date timestamp)
    {
      this.direction = direction;
      this.timestamp = timestamp.getTime();
    }

    public LogPacket(final LogDirection direction, final long timestamp)
    {
      this.direction = direction;
      this.timestamp = timestamp;
    }

    public LogPacket(final LogDirection direction)
    {
      this.direction = direction;
      this.timestamp = System.currentTimeMillis();
    }

    public LogDirection getDirection()
    {
      return direction;
    }

    public final Date getTimestamp()
    {
      return new Date(timestamp);
    }

    public final long getTimestampMs()
    {
      return timestamp;
    }

    public abstract String getInfo();

    public abstract PacketType getPacketType();

    @Override
    public String toString()
    {
      switch (direction)
      {
        case IN:
          return "< " + getInfo();
        case OUT:
          return "> " + getInfo();
        case NONE:
          return "  " + getInfo();
      }
      return "???";
    }

  }

  public static class LogPacketData extends LogPacket
  {
    private final byte[] data;

    public LogPacketData(final LogDirection direction, final Date timestamp, final byte[] data)
    {
      super(direction, timestamp);
      this.data = data.clone();
    }

    public LogPacketData(LogDirection direction, long timestamp, byte[] data)
    {
      super(direction, timestamp);
      this.data = data.clone();
    }

    public LogPacketData(LogDirection direction, byte[] data)
    {
      super(direction);
      this.data = data.clone();
    }

    public byte[] getData()
    {
      return data.clone();
    }

    public String getInfo()
    {
      return CodingUtils.byteArrayToString(getData());
    }

    @Override
    public PacketType getPacketType()
    {
      return PacketType.DATA;
    }

  }

  public static class LogPacketComment extends LogPacket
  {
    private final String comment;

    public LogPacketComment(LogDirection direction, Date timestamp, String comment)
    {
      super(direction, timestamp);
      this.comment = comment;
    }

    public LogPacketComment(LogDirection direction, String comment)
    {
      super(direction);
      this.comment = comment;
    }

    public LogPacketComment(LogDirection direction, long timestamp, String comment)
    {
      super(direction, timestamp);
      this.comment = comment;
    }

    @Override
    public String getInfo()
    {
      return comment;
    }

    @Override
    public PacketType getPacketType()
    {
      return PacketType.COMMENT;
    }

  }

}
