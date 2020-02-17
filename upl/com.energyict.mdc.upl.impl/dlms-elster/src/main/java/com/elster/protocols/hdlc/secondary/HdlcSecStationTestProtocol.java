/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/secondary/HdlcSecStationTestProtocol.java $
 * Version:     
 * $Id: HdlcSecStationTestProtocol.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  04.05.2010 10:32:08
 */
package com.elster.protocols.hdlc.secondary;

import com.elster.coding.CodingUtils;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.hdlc.HdlcAddress;
import com.elster.protocols.hdlc.HdlcControlField;
import com.elster.protocols.hdlc.HdlcFrame;
import com.elster.protocols.hdlc.HdlcNegotiationParameters;
import com.elster.protocols.hdlc.HdlcProtocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implement a simple implementation of the secondary station part
 * of the HDLC protocol.<P>
 * It answers I-Frames as follows:<br>
 * If the I-Frame starts with 0x01, the rest of the I-FRAME will be echoed.<br>
 * If the I-Frame starts with 0x02, the protocol will go into the RNR state.
 *
 * @author osse
 */
public class HdlcSecStationTestProtocol
{
  //private static final Logger LOGGER = Logger.getLogger(HdlcSecStationTestProtocol.class.getName());
  public static final int FLAG = 0x7E;
  Thread receivingThread = null;
  IStreamProtocol sublayer;
  HdlcAddress address;
  int recSeqNo = 0;
  int sendSeqNo = 0;
  long rnrUntil = 0;
  byte[] pendingInformation= null;
  long rnrCount= 0;


  public HdlcSecStationTestProtocol(IStreamProtocol sublayer, HdlcAddress address)
  {
    this.sublayer = sublayer;
    this.address = address;
  }

  public void start()
  {
    startReceivingThread();
  }

  public void stop()
  {
    receivingThread.interrupt();
  }

  private void startReceivingThread()
  {
    receivingThread = new Thread(new ReceivingRunnable(), "HDLC sec. station thread");
    receivingThread.start();
  }

  private HdlcFrame processFrame(HdlcFrame receivedFrame) throws IOException
  {
    if (!receivedFrame.getDestAddress().equals(address))
    {
      return null;
    }

    HdlcFrame answer = null;

    switch (receivedFrame.getControllField().getCommandAndResponseType())
    {
      case SNRM:
        return processSnrmFrame(receivedFrame);
      case DISC:
        return processDiscFrame(receivedFrame);
      case RR:
        return processRrFrame(receivedFrame);
      case I:
        return processIFrame(receivedFrame);

    }
    return answer;
  }

  private HdlcFrame processSnrmFrame(HdlcFrame receivedFrame) throws IOException
  {
    HdlcFrame uaFrame = new HdlcFrame();

    uaFrame.setSourceAddress(address);
    uaFrame.setDestAddress(receivedFrame.getSourceAddress());

    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.UA);
    controlField.setPoolFinal(true);

    uaFrame.setControllField(controlField);
    uaFrame.setSegmentationBit(false);

    HdlcNegotiationParameters negotiationParameters = new HdlcNegotiationParameters();
    negotiationParameters.setMaxInformationFieldLengthReceive(255);
    negotiationParameters.setMaxInformationFieldLengthTransmit(255);
    negotiationParameters.setWindowSizeReceive(1);
    negotiationParameters.setWindowSizeTransmit(1);

    ByteArrayOutputStream negotiationOut = new ByteArrayOutputStream();
    negotiationParameters.encode(negotiationOut);

    uaFrame.setInformation(negotiationOut.toByteArray());

    recSeqNo = 0;
    sendSeqNo = 0;

    return uaFrame;
  }

  private HdlcFrame processDiscFrame(HdlcFrame receivedFrame)
  {
    HdlcFrame discFrame = new HdlcFrame();

    discFrame.setSourceAddress(address);
    discFrame.setDestAddress(receivedFrame.getSourceAddress());

    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.UA);
    controlField.setPoolFinal(true);

    discFrame.setControllField(controlField);
    discFrame.setSegmentationBit(false);

    recSeqNo = 0;
    sendSeqNo = 0;

    return discFrame;
  }

  private HdlcFrame processRrFrame(HdlcFrame receivedFrame) throws IOException
  {
    if (isInRnr())
    {
      return buildRnrFrame(receivedFrame);
    }

    if (pendingInformation!=null)
    {
      HdlcFrame frame= buildIFrame(pendingInformation,receivedFrame);
      pendingInformation=null;
      return frame;
    }

    HdlcFrame rrFrame = new HdlcFrame();

    rrFrame.setSourceAddress(address);
    rrFrame.setDestAddress(receivedFrame.getSourceAddress());

    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.RR);
    controlField.setPoolFinal(true);
    controlField.setReceiveSeqNo(recSeqNo);
    controlField.setSendSeqNo(sendSeqNo);

    rrFrame.setControllField(controlField);
    rrFrame.setSegmentationBit(false);

    return rrFrame;
  }

//  private HdlcFrame buildModEcho(HdlcFrame receivedFrame)
//  {
//    recSeqNo++;
//    HdlcFrame iFrame = new HdlcFrame();
//
//    iFrame.setSourceAddress(address);
//    iFrame.setDestAddress(receivedFrame.getSourceAddress());
//
//
//    HdlcControlField controlField = new HdlcControlField();
//    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.I);
//    controlField.setPoolFinal(true);
//    controlField.setReceiveSeqNo(recSeqNo);
//    controlField.setSendSeqNo(sendSeqNo);
//
//    iFrame.setControllField(controlField);
//    iFrame.setSegmentationBit(receivedFrame.isSegmentationBit());
//
//    byte[] info = receivedFrame.getInformation();
//
//    for (int i = 0; i < info.length; i++)
//    {
//      info[i] = (byte)(((0xFF & info[i]) + 1) % 255);
//    }
//
//    iFrame.setInformation(info);
//
//    sendSeqNo++;
//    return iFrame;
//  }

  private HdlcFrame buildEcho(HdlcFrame receivedFrame)
  {
    recSeqNo++;
    HdlcFrame iFrame = new HdlcFrame();

    iFrame.setSourceAddress(address);
    iFrame.setDestAddress(receivedFrame.getSourceAddress());


    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.I);
    controlField.setPoolFinal(true);
    controlField.setReceiveSeqNo(recSeqNo);
    controlField.setSendSeqNo(sendSeqNo);

    iFrame.setControllField(controlField);
    iFrame.setSegmentationBit(receivedFrame.isSegmentationBit());

    byte[] receivedInfo =receivedFrame.getInformation();
    
    byte[] info =CodingUtils.copyOfRange(receivedInfo, 1, receivedInfo.length);

    iFrame.setInformation(info);

    sendSeqNo++;
    return iFrame;
  }

  private HdlcFrame buildIFrame(byte[] information, HdlcFrame receivedFrame)
  {
    recSeqNo++;
    HdlcFrame iFrame = new HdlcFrame();

    iFrame.setSourceAddress(address);
    iFrame.setDestAddress(receivedFrame.getSourceAddress());


    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.I);
    controlField.setPoolFinal(true);
    controlField.setReceiveSeqNo(recSeqNo);
    controlField.setSendSeqNo(sendSeqNo);

    iFrame.setControllField(controlField);
    iFrame.setSegmentationBit(false);

    iFrame.setInformation(information);

    sendSeqNo++;
    return iFrame;
  }

  private HdlcFrame buildRnrFrame(HdlcFrame receivedFrame)
  {
    rnrCount++;

    HdlcFrame rnrFrame = new HdlcFrame();

    rnrFrame.setSourceAddress(address);
    rnrFrame.setDestAddress(receivedFrame.getSourceAddress());


    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.RNR);
    controlField.setPoolFinal(true);
    controlField.setReceiveSeqNo(recSeqNo);
    controlField.setSendSeqNo(sendSeqNo);

    rnrFrame.setControllField(controlField);
    rnrFrame.setSegmentationBit(false);
    return rnrFrame;
  }

  private boolean isInRnr()
  {
    return rnrUntil > System.currentTimeMillis();
  }

  private HdlcFrame processIFrame(HdlcFrame receivedFrame)
  {
    if (isInRnr())
    {
      return buildRnrFrame(receivedFrame);
    }

    byte[] info = receivedFrame.getInformation();
    if (info.length > 0)
    {
      switch (info[0] & 0xFF)
      {
        case 0x01:  //echo
          return buildEcho(receivedFrame);
        case 0x02:
        {
          //recSeqNo++;

          if (info.length>1)
          {
            rnrUntil = System.currentTimeMillis() + 1000* (0xFF & info[1]);
            pendingInformation= CodingUtils.copyOfRange(info, 2, info.length);
          }

          return buildRnrFrame(receivedFrame);
        }
      }
    }
    return null;
  }

  public long getRnrCount()
  {
    return rnrCount;
  }


  private class ReceivingRunnable implements Runnable
  {
    public ReceivingRunnable()
    {
    }

    //@Override
    public void run()
    {
      //InputStream inputStream = new BufferedInputStream(sublayer.getInputStream());
      InputStream inputStream = sublayer.getInputStream();

      int b;
      try
      {
        while (true)
        {
          b = inputStream.read();
          if (b == FLAG)
          {
//            inputStream.mark(4096);
//            try
            {
              HdlcFrame frame = new HdlcFrame();
              frame.decode(inputStream);
              HdlcFrame answer = processFrame(frame);
              if (answer != null)
              {
                sublayer.getOutputStream().write(FLAG);
                answer.encode(sublayer.getOutputStream());
                sublayer.getOutputStream().write(FLAG);
                sublayer.getOutputStream().flush();
              }
            }
//            catch (IOException ex)
//            {
//              LOGGER.log(Level.WARNING, "Exception",ex);
//  //            inputStream.reset();
//            }
          }
        }
      }
      catch (InterruptedIOException ex)
      {
      }
      catch (IOException ex)
      {
        Logger.getLogger(HdlcProtocol.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  }

}
