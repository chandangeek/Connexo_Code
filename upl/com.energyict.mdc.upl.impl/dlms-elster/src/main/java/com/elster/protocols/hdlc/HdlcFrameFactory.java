/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcFrameFactory.java $
 * Version:     
 * $Id: HdlcFrameFactory.java 3747 2011-11-09 11:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.05.2010 09:55:17
 */
package com.elster.protocols.hdlc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides (static) methods to create HDLC frames.
 *
 * @author osse
 */
public final class HdlcFrameFactory
{

  private HdlcFrameFactory()
  {
    //no instances allowed.
  }
  
  
  /**
   * Creates an SNRM Frame for the specified channel.<P>
   * SNRM = set normal response mode<P>
   * The pool bit will be set.
   *
   * @param channel the channel for the SNRM Frame
   * @return the created HDLC SNRM frame
   */
  static public HdlcFrame createSnrmFrame(IHdlcChannel channel)
  {
    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.SNRM);
    controlField.setPoolFinal(true);

    HdlcFrame frame = new HdlcFrame();
    frame.setDestAddress(channel.getDestAddress());
    frame.setSourceAddress(channel.getSourceAddress());
    frame.setSegmentationBit(false);
    frame.setControllField(controlField);

    return frame;
  }

  /**
   * Creates an SNRM Frame for the specified channel.<P>
   * SNRM = set normal response mode<P>
   * The pool bit will be set.
   *
   * @return the created HDLC SNRM frame
   */
  static public HdlcFrame createSnrmFrame(final HdlcAddress sourceAddress, final HdlcAddress destAddress,
                                          final HdlcNegotiationParameters negotiationParameters)
  {
    try
    {

      HdlcControlField controlField = new HdlcControlField();
      controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.SNRM);
      controlField.setPoolFinal(true);

      HdlcFrame frame = new HdlcFrame();
      frame.setDestAddress(destAddress);
      frame.setSourceAddress(sourceAddress);
      frame.setSegmentationBit(false);
      frame.setControllField(controlField);

      if (negotiationParameters != null)
      {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        negotiationParameters.encode(outputStream);
        frame.setInformationBytes(outputStream.toByteArray());

      }
      return frame;
    }
    catch (IOException ex)
    {
      //should never happen.
      Logger.getLogger(HdlcFrameFactory.class.getName()).log(Level.SEVERE, null, ex);
      throw new IllegalArgumentException("The negotiation parameters could not be encoded.");
    }
  }

  /**
   * Creates an DISC Frame for the specified channel.<P>
   * DISC = disconnect<P>
   * The pool bit will be set.
   *
   * @param channel the channel for the DISC frame
   * @return the created HDLC DISC frame
   */
  static public HdlcFrame createDiscFrame(IHdlcChannel channel)
  {
    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.DISC);
    controlField.setPoolFinal(true);

    HdlcFrame frame = new HdlcFrame();
    frame.setDestAddress(channel.getDestAddress());
    frame.setSourceAddress(channel.getSourceAddress());
    frame.setSegmentationBit(false);
    frame.setControllField(controlField);

    return frame;
  }

  /**
   * Creates an empty I Frame for the specified channel.<P>
   * I = frame<P>
   * The pool bit will be set.<br>
   * The segmentation bit will be set to false.<br>
   * The receive and send sequence numbers will be
   * set according to the channel.
   *
   * @param channel the channel for the DISC frame
   * @return the created HDLC DISC frame
   */
//  static public HdlcFrame createIFrame(IHdlcChannel channel)
//  {
//    HdlcControlField controlField = new HdlcControlField();
//    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.I);
//    controlField.setPoolFinal(true);
//    controlField.setReceiveSeqNo(channel.getReceiveSequenceNo());
//    controlField.setSendSeqNo(channel.getSendSequenceNo());
//
//    HdlcFrame frame = new HdlcFrame();
//    frame.setDestAddress(channel.getDestAddress());
//    frame.setSourceAddress(channel.getSourceAddress());
//    frame.setSegmentationBit(false);
//    frame.setControllField(controlField);
//
//    return frame;
//  }

  /**
   * Creates an RR Frame for the specified channel.<P>
   * RR = receive ready<P>
   * The pool bit will be set.<br>
   * The receive sequence number will be set according to the channel.
   *
   * @param channel the channel for the RR frame
   * @return the created HDLC RR frame
   */
//  static HdlcFrame createRRFrame(IHdlcChannel channel)
//  {
//    HdlcControlField controlField = new HdlcControlField();
//    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.RR);
//    controlField.setPoolFinal(true);
//    controlField.setReceiveSeqNo(channel.getReceiveSequenceNo());
//
//    HdlcFrame frame = new HdlcFrame();
//    frame.setDestAddress(channel.getDestAddress());
//    frame.setSourceAddress(channel.getSourceAddress());
//    frame.setSegmentationBit(false);
//    frame.setControllField(controlField);
//
//    return frame;
//  }

}
