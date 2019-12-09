/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcControlField.java $
 * Version:     
 * $Id: HdlcControlField.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 18:10:09
 */
package com.elster.protocols.hdlc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class represents the hdlc control field.
 * <P>
 * See 8.4.3 of the DLMS Green Book 7th edition.
 *
 * @author osse
 */
public class HdlcControlField
{
  public enum CommandAndResponseType
  {
    I(true, true),
    RR(true, false),
    RNR(true, false),
    SNRM(false, false),
    DISC(false, false),
    UA(false, false),
    DM(false, false),
    FRMR(false, false),
    UI(false, false);
    //--
    private final boolean hasReceiveSequenceNo;
    private final boolean hasSendSequenceNo;

    private CommandAndResponseType(boolean hasReceiveSequenceNumber, boolean hasSendSequenceNumber)
    {
      this.hasReceiveSequenceNo = hasReceiveSequenceNumber;
      this.hasSendSequenceNo = hasSendSequenceNumber;
    }

    public boolean hasReceiveSequenceNumber()
    {
      return hasReceiveSequenceNo;
    }

    public boolean hasSendSequenceNumber()
    {
      return hasSendSequenceNo;
    }

  };

  private int controlField;
  private int receiveSeqNo;
  private int sendSeqNo;
  private boolean poolFinal;
  private CommandAndResponseType commandAndResponseType;

  /**
   * Decodes this control field from an input stream.
   *
   * @param inputStream The input stream
   * @return The count of bytes read. (Allways 1)
   * @throws IOException
   */
  public int decode(InputStream inputStream) throws IOException
  {
    controlField = inputStream.read();

    poolFinal = 0 != (controlField & 0x10);

    if (0 == (0x01 & controlField))
    {
      commandAndResponseType = CommandAndResponseType.I;
      receiveSeqNo = controlField >> 5;
      sendSeqNo = (controlField >> 1) & 0x07;
    }
    else if ((0x0F & controlField) == 0x01)
    {
      receiveSeqNo = controlField >> 5;
      sendSeqNo = 0;
      commandAndResponseType = CommandAndResponseType.RR;
    }
    else if ((0x0F & controlField) == 0x05)
    {
      receiveSeqNo = controlField >> 5;
      sendSeqNo = 0;
      commandAndResponseType = CommandAndResponseType.RNR;
    }
    else
    {
      switch (controlField & 0xEF)
      {
        case 0x83:
          commandAndResponseType = CommandAndResponseType.SNRM;
          break;
        case 0x43:
          commandAndResponseType = CommandAndResponseType.DISC;
          break;
        case 0x63:
          commandAndResponseType = CommandAndResponseType.UA;
          break;
        case 0x0F:
          commandAndResponseType = CommandAndResponseType.DM;
          break;
        case 0x87:
          commandAndResponseType = CommandAndResponseType.FRMR;
          break;
        case 0x03:
          commandAndResponseType = CommandAndResponseType.UI;
          break;
      }
    }
    return 1;
  }

  /**
   * Encodes this control field to an output stream.
   *
   * @param outputStream The output stream.
   * @throws IOException
   */
  public void encode(OutputStream outputStream) throws IOException
  {
    controlField = 0;

    if (poolFinal)
    {
      controlField |= 0x10;
    }

    switch (commandAndResponseType)
    {
      case I:
        controlField |= ((receiveSeqNo & 0x07) << 5);
        controlField |= ((sendSeqNo & 0x07) << 1);
        break;
      case RR:
        controlField |= ((receiveSeqNo & 0x07) << 5);
        controlField |= 0x01;
        break;
      case RNR:
        controlField |= ((receiveSeqNo & 0x07) << 5);
        controlField |= 0x05;
        break;
      case SNRM:
        controlField |= 0x04 << 5;
        controlField |= 0x03;
        break;
      case DISC:
        controlField |= 0x02 << 5;
        controlField |= 0x03;
        break;
      case UA:
        controlField |= 0x03 << 5;
        controlField |= 0x03;
        break;
      case DM:
        //controllField |= 0x00 << 5;
        controlField |= 0x0F;
        break;
      case FRMR:
        controlField |= 0x04 << 5;
        controlField |= 0x07;
        break;
      case UI:
        //controllField |= 0x00 << 5;
        controlField |= 0x03;
        break;
    }

    outputStream.write(controlField);
  }

  public CommandAndResponseType getCommandAndResponseType()
  {
    return commandAndResponseType;
  }

  public void setCommandAndResponseType(CommandAndResponseType commandAndResponseType)
  {
    this.commandAndResponseType = commandAndResponseType;
  }

  public boolean isPoolFinal()
  {
    return poolFinal;
  }

  public void setPoolFinal(boolean poolFinal)
  {
    this.poolFinal = poolFinal;
  }

  public int getReceiveSeqNo()
  {
    return receiveSeqNo;
  }

  public void setReceiveSeqNo(int receiveSeqNo)
  {
    this.receiveSeqNo = receiveSeqNo;
  }

  public int getSendSeqNo()
  {
    return sendSeqNo;
  }

  public void setSendSeqNo(int sendSeqNo)
  {
    this.sendSeqNo = sendSeqNo;
  }

  @Override
  public String toString()
  {
    return "HdlcControlField{" + "controlField=" + controlField + ", receiveSeqNo=" + receiveSeqNo
           + ", sendSeqNo=" + sendSeqNo + ", poolFinal=" + poolFinal + ", commandAndResponseType="
           + commandAndResponseType + '}';
  }

}
