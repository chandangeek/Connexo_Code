/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/secondary/HdlcSecStationEchoProtocol.java $
 * Version:     
 * $Id: HdlcSecStationEchoProtocol.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  04.05.2010 10:32:08
 */
package com.elster.protocols.hdlc.secondary;

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
 * of the hdlc protocol.<P>
 * It answers every I-Frame with an I-Frame with slightly modified data:<br>
 * Every byte will be increased by one. 255 will be modified to 0.
 * <P>
 *
 *
 *
 * @author osse
 */
public class HdlcSecStationEchoProtocol
{
  //private static final Logger LOGGER = Logger.getLogger(HdlcSecStationEchoProtocol.class.getName());
  public static final int FLAG = 0x7E;
  Thread receivingThread = null;
  IStreamProtocol sublayer;

  HdlcAddress address;
  int recSeqNo = 0;
  int sendSeqNo = 0;

  public HdlcSecStationEchoProtocol(IStreamProtocol sublayer, HdlcAddress address)
  {
    this.sublayer= sublayer;
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
    receivingThread = new Thread(new ReceivingRunnable(),"HDLC sec. station thread");
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

  private HdlcFrame processIFrame(HdlcFrame receivedFrame)
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

    byte [] info=receivedFrame.getInformation();
    
    for (int i=0; i<info.length; i++)
    {
      info[i]=(byte) (((0xFF&info[i])+1) % 255);
    }
    
    iFrame.setInformation(info);

    sendSeqNo++;
    return iFrame;
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
