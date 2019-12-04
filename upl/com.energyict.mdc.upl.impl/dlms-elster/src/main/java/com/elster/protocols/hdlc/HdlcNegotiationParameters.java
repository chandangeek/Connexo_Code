/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcNegotiationParameters.java $
 * Version:     
 * $Id: HdlcNegotiationParameters.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 13:18:57
 */
package com.elster.protocols.hdlc;

import com.elster.protocols.streams.CountingInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class manages the HDLC negotiation parameters which are used
 * during an connection establishment.
 *
 * @author osse
 */
public class HdlcNegotiationParameters
{
  private int maxInformationFieldLengthTransmit = -1;
  private int maxInformationFieldLengthReceive = -1;
  private int windowSizeTransmit = -1;
  private int windowSizeReceive = -1;

  /**
   * Decodes the negotiation parameters from an input stream.
   *
   * @param inputStream The input stream.
   * @param skipHeader If true the first byte (the format identifier) will be ignored ohterwise the first byte must the
   * format identifier and must be 0x81.
   * @throws IOException
   */
  public void decode(InputStream inputStream, boolean skipHeader) throws IOException
  {
    if (!skipHeader)
    {
      if (0x81 != inputStream.read())
      {
        throw new HdlcDecodingIOException("unexpected format identifier");
      }
    }

    while (inputStream.available() > 0)
    {
      int groupId = inputStream.read();

      if (groupId == 0x80)
      {
        int groupLength = inputStream.read();
        CountingInputStream countingInputStream = new CountingInputStream(inputStream);

        while (countingInputStream.getCount() < groupLength)
        {
          int parameterId = countingInputStream.read();

          switch (parameterId)
          {
            case 0x05:
              maxInformationFieldLengthTransmit = readInteger(countingInputStream);
              break;
            case 0x06:
              maxInformationFieldLengthReceive = readInteger(countingInputStream);
              break;
            case 0x07:
              windowSizeTransmit = readInteger(countingInputStream);
              break;
            case 0x08:
              windowSizeReceive = readInteger(countingInputStream);
              break;
            default:
              skipParameter(countingInputStream);
          }
        }
      }
      else
      {
        skipParameter(inputStream);
      }
    }
  }

  /**
   * Decodes the negotiation parameters to specified output stream.
   *
   * @param outputStream
   * @throws IOException
   */
  public void encode(OutputStream outputStream) throws IOException
  {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    writeParameter(buffer, 0x05, maxInformationFieldLengthTransmit, 2);
    writeParameter(buffer, 0x06, maxInformationFieldLengthReceive, 2);
    writeParameter(buffer, 0x07, windowSizeTransmit, 4);
    writeParameter(buffer, 0x08, windowSizeReceive, 4);

    byte[] block = buffer.toByteArray();

    outputStream.write(0x81);
    outputStream.write(0x80);
    outputStream.write(block.length);
    outputStream.write(block);
  }


  /**
   * Returns the "maximum information field length – receive"
   *
   * @return The "maximum information field length – receive"
   */
  public int getMaxInformationFieldLengthReceive()
  {
    return maxInformationFieldLengthReceive;
  }

  /**
   * Sets the "maximum information field length – receive"
   *
   * @param maxInformationFieldLenghReceive The "maximum information field length – receive"
   */
  public void setMaxInformationFieldLengthReceive(int maxInformationFieldLenghReceive)
  {
    this.maxInformationFieldLengthReceive = maxInformationFieldLenghReceive;
  }

  /**
   * Returns the "maximum information field length – transmit"
   *
   * @return The "maximum information field length – transmit"
   */
  public int getMaxInformationFieldLengthTransmit()
  {
    return maxInformationFieldLengthTransmit;
  }

  /**
   * Sets the "maximum information field length – transmit"
   *
   * @param maxInformationFieldLenghTransmit The "maximum information field length – transmit"
   */
  public void setMaxInformationFieldLengthTransmit(int maxInformationFieldLenghTransmit)
  {
    this.maxInformationFieldLengthTransmit = maxInformationFieldLenghTransmit;
  }

  /**
   * Returns the "window size – receive"
   *
   * @return The "window size – receive"
   */
  public int getWindowSizeReceive()
  {
    return windowSizeReceive;
  }

  /**
   * Sets the "window size – receive"
   *
   * @param windowSizeReceive The "window size – receive"
   */
  public void setWindowSizeReceive(int windowSizeReceive)
  {
    this.windowSizeReceive = windowSizeReceive;
  }

  /**
   * Returns the "window size – transmit"
   *
   * @return The "window size – transmit"
   */
  public int getWindowSizeTransmit()
  {
    return windowSizeTransmit;
  }

  /**
   * Sets the "window size – transmit"
   *
   * @param windowSizeTransmit The "window size – transmit"
   */
  public void setWindowSizeTransmit(int windowSizeTransmit)
  {
    this.windowSizeTransmit = windowSizeTransmit;
  }

  @Override
  public String toString()
  {
    return "Negoation parameters (Max information field ln. TX:" + maxInformationFieldLengthTransmit
           + ", Max information field ln. RX:" + maxInformationFieldLengthReceive
           + ", window size TX:" + windowSizeTransmit
           + ", window size RX:" + windowSizeReceive + ")";
  }


  private void writeParameter(OutputStream outputStream, int id, int parameter, int minFieldLength) throws
          IOException
  {
    if (parameter > 0)
    {
      outputStream.write(id);

      int temp = parameter;
      int l = 0;

      while (temp != 0)
      {
        l++;
        temp = temp >> 8;
      }
      l = Math.max(l, minFieldLength);

      outputStream.write(l);

      for (int i = l - 1; i >= 0; i--)
      {
        outputStream.write((parameter >> (i * 8)) & 0xFF);
      }
    }
  }

    private void skipParameter(InputStream inputStream) throws IOException
  {
    int l = inputStream.read();
    while (0 < l--)
    {
      inputStream.read();
    }
  }

  private int readInteger(InputStream inputStream) throws IOException
  {
    int l = inputStream.read();

    int result = 0;

    while (0 < l--)
    {
      if (0 != (result & 0x800000))
      {
        throw new HdlcDecodingIOException("Negoation parameter is to big");
      }
      result = (result << 8) | inputStream.read();
    }
    return result;
  }



}
