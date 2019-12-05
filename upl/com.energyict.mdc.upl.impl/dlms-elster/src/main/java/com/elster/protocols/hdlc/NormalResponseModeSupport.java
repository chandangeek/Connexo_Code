/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/NormalResponseModeSupport.java $
 * Version:     
 * $Id: NormalResponseModeSupport.java 3843 2011-12-12 16:55:48Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.07.2011 11:58:16
 */
package com.elster.protocols.hdlc;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class for managing the normal response mode of one HDLC channel
 *
 * @author osse
 */
public class NormalResponseModeSupport
{
  Queue<InformationFragment> fragmentsToSend = new ConcurrentLinkedQueue<InformationFragment>();
  Queue<InformationFragment> confirmedFragments = new ConcurrentLinkedQueue<InformationFragment>();
  Queue<InformationFragment> receivedFragments = new ConcurrentLinkedQueue<InformationFragment>();
  private final HdlcAddress sourceAddress;
  private final HdlcAddress destAddress;
  private int receiveSequenceNo = 0;
  private int sendSequenceNo = 0;
  private int secondaryReceiveSequenceNo = 0;
  private int secondarySendSequenceNo = -1;
  private final SentFrameInfo[] sentFrames = new SentFrameInfo[8];

  public NormalResponseModeSupport(HdlcAddress sourceAddress, HdlcAddress destAddress)
  {
    this.sourceAddress = sourceAddress;
    this.destAddress = destAddress;
  }

  /**
   * Returns the next Frame to send or null if no frame has to be sent.
   * 
   * @return The next frame to send.
   */
  public HdlcFrame getNextIFrame() throws IOException, NrmFatalException
  {

    if (secondaryReceiveSequenceNo == sendSequenceNo)
    {
      //new I frame
      return nextIFrame();
    }
    else
    {
      //resent frame
      return frameToResent();
    }
  }

  public boolean nextFrameIsARepeatedFrame()
  {
    return secondaryReceiveSequenceNo != sendSequenceNo;
  }

  public boolean fragmentToSendRequired()
  {
    return secondaryReceiveSequenceNo == sendSequenceNo && fragmentsToSend.isEmpty();
  }

  public boolean allDataSent()
  {
    return secondaryReceiveSequenceNo == sendSequenceNo && fragmentsToSend.isEmpty();
  }

  public void addFragmentToSend(InformationFragment fragment)
  {
    fragmentsToSend.add(fragment);
  }

  public boolean nextConfirmedFraqmentAvailable()
  {
    return !confirmedFragments.isEmpty();
  }

  public InformationFragment getNextConfirmedFraqment()
  {
    return confirmedFragments.poll();
  }

  public boolean nextReceivedFraqmentAvailable()
  {
    return !receivedFragments.isEmpty();
  }

  public InformationFragment getNextReceivedFraqment()
  {
    return receivedFragments.poll();
  }

  /**
   * Returns a RR-Frame with the current receive sequence number.
   * 
   * @return 
   */
  public HdlcFrame getRrFrame()
  {
    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.RR);
    controlField.setPoolFinal(true);
    controlField.setReceiveSeqNo(receiveSequenceNo);

    HdlcFrame frame = new HdlcFrame();
    frame.setDestAddress(destAddress);
    frame.setSourceAddress(sourceAddress);
    frame.setSegmentationBit(false);
    frame.setControllField(controlField);

    return frame;
  }

  private HdlcFrame frameToResent() throws NrmFatalException
  {
    SentFrameInfo frameInfo = sentFrames[secondaryReceiveSequenceNo];
    if (frameInfo == null)
    {
      throw new NrmFatalException("HDLC frame to resent not available");
    }
    return frameInfo.getFrame();
  }

  private HdlcFrame nextIFrame()
  {
    InformationFragment nextBlock = fragmentsToSend.poll();
    if (nextBlock == null)
    {
      return null;
    }
    HdlcFrame iFrame = buildIFrame(nextBlock);
    increaseSendSequenceNo();
    sentFrames[iFrame.getControllField().getSendSeqNo()] = new SentFrameInfo(nextBlock, iFrame);

    return iFrame;
  }

  public void handleReveivedFrame(HdlcFrame frame) throws NrmFatalException, NrmTempException
  {
    switch (frame.getControllField().getCommandAndResponseType())
    {
      case I:
      {
        confirmSentFrames(frame.getControllField().getReceiveSeqNo());
        processReceivedData(frame);
        break;
      }
      case RR:
      case RNR:
      {
        confirmSentFrames(frame.getControllField().getReceiveSeqNo());
        break;
      }
    }
  }

  private void processReceivedData(HdlcFrame iFrame) throws NrmFatalException, NrmTempException
  {
    int newSecondarySendSeqNo = iFrame.getControllField().getSendSeqNo();

    if (newSecondarySendSeqNo == secondarySendSequenceNo)
    {
      throw new NrmTempException("IFrame with the same send sequence nummber received: "
                                 + newSecondarySendSeqNo);
    }

    if (newSecondarySendSeqNo != receiveSequenceNo)
    {
      throw new NrmFatalException("Unexpected send sequence number received: " + newSecondarySendSeqNo);
    }

    InformationFragment receivedBlock;

    if (iFrame.getInformationBytes() != null)
    {
      receivedBlock = new InformationFragment(iFrame.getInformationBytes(), iFrame.isSegmentationBit(), null);
    }
    else
    {
      receivedBlock = new InformationFragment(new byte[0], iFrame.isSegmentationBit(), null);
    }

    receivedFragments.add(receivedBlock);

    secondarySendSequenceNo = newSecondarySendSeqNo;
    increaseReceiveSequenceNo();
  }

  private void confirmSentFrames(int newSecondaryReceiveSequenceNo) throws NrmTempException, NrmFatalException
  {
    if (newSecondaryReceiveSequenceNo != secondaryReceiveSequenceNo) //changed
    {
//      throw new NrmTempException("Frame with the same receive sequence nummber received");
      if (newSecondaryReceiveSequenceNo != sendSequenceNo) //windows size 1 --> must match
      {
        throw new NrmFatalException("Unexpected receive sequence number received: "
                                    + newSecondaryReceiveSequenceNo);
      }

      SentFrameInfo sentFrameInfo = sentFrames[secondaryReceiveSequenceNo];

      if (sentFrameInfo == null)
      {
        throw new NrmFatalException("Frame to confirm not found");
      }

      confirmedFragments.add(sentFrameInfo.getFragment());
      sentFrames[secondaryReceiveSequenceNo] = null;

      secondaryReceiveSequenceNo = newSecondaryReceiveSequenceNo;
    }


  }

  private HdlcFrame buildIFrame(InformationFragment block)
  {
    HdlcControlField controlField = new HdlcControlField();
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.I);
    controlField.setPoolFinal(true);
    controlField.setReceiveSeqNo(receiveSequenceNo);
    controlField.setSendSeqNo(sendSequenceNo);

    HdlcFrame frame = new HdlcFrame();
    frame.setDestAddress(destAddress);
    frame.setSourceAddress(sourceAddress);
    frame.setSegmentationBit(block.isSegmented());
    frame.setControllField(controlField);
    frame.setInformation(block.getInformation());
    return frame;
  }

  private void increaseSendSequenceNo()
  {
    sendSequenceNo = (sendSequenceNo + 1) % 8;
  }

  private void increaseReceiveSequenceNo()
  {
    receiveSequenceNo = (receiveSequenceNo + 1) % 8;


  }

  private static class SentFrameInfo
  {
    private final InformationFragment fraqment;
    private final HdlcFrame frame;

    public SentFrameInfo(InformationFragment block, HdlcFrame frame)
    {
      this.fraqment = block;
      this.frame = frame;
    }

    public InformationFragment getFragment()
    {
      return fraqment;
    }

    public HdlcFrame getFrame()
    {
      return frame;
    }

  }

  public static class InformationFragment
  {
    private final byte[] information;
    private final boolean segmented;
    private final Object source;

    public InformationFragment(final byte[] information, final boolean segmented, final Object source)
    {
      this.information = information;
      this.segmented = segmented;
      this.source = source;
    }

    public byte[] getInformation()
    {
      return information;
    }

    public boolean isSegmented()
    {
      return segmented;
    }

    public Object getSource()
    {
      return source;
    }

 }

  /**
   * Thrown if an exception occurs, that does not requires to end the NRM.
   */
  public static class NrmTempException extends Exception
  {
    public NrmTempException(String message)
    {
      super(message);
    }

  }

  /**
   * Thrown if an exception occurs, that requires to end the NRM.
   */
  public static class NrmFatalException extends Exception
  {
    public NrmFatalException(String message)
    {
      super(message);
    }

  }

  /**
   * Returns the current receive sequence no.
   * 
   * @return The current receive sequence no.
   */
  public int getReceiveSequenceNo()
  {
    return receiveSequenceNo;
  }

  /**
   * Returns the current send sequence no.
   * 
   * @return The current send sequence no.
   */
  public int getSendSequenceNo()
  {
    return sendSequenceNo;
  }

}
