/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcChannel.java $
 * Version:     
 * $Id: HdlcChannel.java 3843 2011-12-12 16:55:48Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 15:37:30
 */
package com.elster.protocols.hdlc;

import com.elster.protocols.ILongOperationListener;
import com.elster.protocols.IProtocolStateObservable;
import com.elster.protocols.IProtocolStateObserver;
import com.elster.protocols.IllegalProtocolStateException;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.ProtocolStateObservableSupport;
import com.elster.protocols.streams.TimeoutIOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents one channel of an HDLC connection.<P>
 * Through this channel information packages can be sent and received. To open
 * a channel use the {@link HdlcProtocol#openChannel} method.
 * <P>
 * (Most of methods are package private and will be used by the HdlcProtocol)
 *
 *
 * @author osse
 */
public class HdlcChannel implements IHdlcChannel, IProtocolStateObservable
{
  private static final Logger LOGGER = Logger.getLogger(HdlcChannel.class.getName());
  //TODO: move this parameters.
  NormalResponseModeSupport normalResponseModeSupport;
  boolean rnrState = false;
  //--- Channel parameters ---
  private final HdlcProtocol protocol;
  private final HdlcAddress sourceAddress;
  private final HdlcAddress destAddress;
  private HdlcNegotiationParameters negotiationParameters;
  private int maxInformationFieldLengthTransmit = 128;
  private int maxInformationFieldLengthReceive = 1024;
  private int windowSizeTransmit = 1;
  private int windowSizeReceive = 1;
  //--- State parameters ---
  private boolean closeRequested = false;
  private long nextPoll = 0;
  //--- Observers ---
  private final ProtocolStateObservableSupport openStateSupport;
  //--- Frames ---
  private final Queue<HdlcInformationBlockOut> out = new ConcurrentLinkedQueue<HdlcInformationBlockOut>();
  private final Queue<HdlcInformationBlockIn> in = new LinkedList<HdlcInformationBlockIn>(); //(replaced for jdk 1.5)
  private HdlcFraqmentBuilder currentFragmentBuilder = null;
  private HdlcInformationBlockIn currentBlockIn;
  private long timeStampLastRxIOrRnrFrame = 0;
  private final Map<NormalResponseModeSupport.InformationFragment, HdlcFraqmentBuilder> unconfirmedFragmentMap =
          new HashMap<NormalResponseModeSupport.InformationFragment, HdlcFraqmentBuilder>();

  HdlcChannel(final HdlcProtocol protocol, final HdlcAddress sourceAddress, final HdlcAddress destAddress,
              final ProtocolState initialState)
  {
    this.protocol = protocol;
    this.sourceAddress = sourceAddress;
    this.destAddress = destAddress;
    this.openStateSupport = new ProtocolStateObservableSupport(this, initialState);

    negotiationParameters = new HdlcNegotiationParameters();

    this.normalResponseModeSupport = new NormalResponseModeSupport(sourceAddress, destAddress);
  }

  /**
   * Closes the channel.
   * <P>
   * This is done by sending an DISC Frame and waiting for the UA-Frame.
   *
   * @throws IOException
   */
  //@Override
  public void close() throws IOException
  {
    close(true);
  }

  /**
   * Closes the channel.
   * <P>
   * All remaining data will be sent (and received). No new data for sent will be accepted.
   *
   * @param wait If {@code false} the method will return immediately. If {@code true} the method will wait until
   * the UA-Frame was received (or an exception occurred).
   *
   * @throws IOException
   */
  public void close(boolean wait) throws IOException
  {
    synchronized (this)
    {
      closeRequested = true;
    }
    protocol.trigger();

    if (wait)
    {
      waitForClose();
    }
  }

  void closeOnException(IOException exception)
  {

    synchronized (this)
    {
      openStateSupport.setState(ProtocolState.CLOSE, false);
      errorReason = exception;
      cancelOutFrames(exception);
      notifyAll();
    }
    synchronized (in)
    {
      in.notifyAll();
    }

    if (currentBlockIn != null)
    {
      endLongOpertation(ILongOperationListener.Operation.READ);
    }


    openStateSupport.notifyObservers();
    openStateSupport.notifyConnectionBroken(this, exception);
  }

  boolean frameToSendAvailable()
  {
    return currentFragmentBuilder != null || !out.isEmpty() || !normalResponseModeSupport.allDataSent();
  }

  NormalResponseModeSupport.InformationFragment getNextFraqmentToSend() throws IOException
  {
    NormalResponseModeSupport.InformationFragment nextFragment = null;

    if (currentFragmentBuilder == null)
    {
      HdlcInformationBlockOut nextBlock = out.poll();

      if (nextBlock != null)
      {
        nextBlock.setState(HdlcInformationBlockOut.State.SENDING);
        currentFragmentBuilder =
                new HdlcFraqmentBuilder(nextBlock, maxInformationFieldLengthTransmit);
      }
    }

    if (currentFragmentBuilder != null)
    {
      if (!currentFragmentBuilder.eof())
      {
        nextFragment = currentFragmentBuilder.buildNextFraqment();
        unconfirmedFragmentMap.put(nextFragment, currentFragmentBuilder);
      }

      if (currentFragmentBuilder.eof())
      {
        currentFragmentBuilder = null;
      }
    }

    return nextFragment;
  }

  void cancelOutFrames(IOException reason)
  {
    if (currentFragmentBuilder != null)
    {
      currentFragmentBuilder.getBlock().setState(HdlcInformationBlockOut.State.ERROR,
                                                 reason);
      currentFragmentBuilder = null;
    }

    for (NormalResponseModeSupport.InformationFragment f : unconfirmedFragmentMap.keySet())
    {
      HdlcFraqmentBuilder builder = unconfirmedFragmentMap.get(f);
      builder.errorFrame(f, reason);
    }
    unconfirmedFragmentMap.clear();

    HdlcInformationBlockOut block = out.poll();
    while (block != null)
    {
      block.setState(HdlcInformationBlockOut.State.ERROR, reason);
      block = out.poll();
    }
  }

  void confirmFraqment(NormalResponseModeSupport.InformationFragment fragment)
  {
    HdlcFraqmentBuilder builder = unconfirmedFragmentMap.remove(fragment);
    if (builder != null)
    {
      builder.confirmFragment(fragment);
    }
  }

  void errorFragment(NormalResponseModeSupport.InformationFragment fragment, IOException ex)
  {
    HdlcFraqmentBuilder builder = unconfirmedFragmentMap.remove(fragment);
    if (builder != null)
    {
      builder.errorFrame(fragment, ex);
    }
  }

  void notifyRnr()
  {
    timeStampLastRxIOrRnrFrame = System.currentTimeMillis();
  }

  void addReceivedFragment(NormalResponseModeSupport.InformationFragment fragment) throws IOException
  {
    timeStampLastRxIOrRnrFrame = System.currentTimeMillis();

    boolean newBlock = false;



    if (currentBlockIn == null)
    {
      currentBlockIn = new HdlcInformationBlockIn();
      newBlock = true;
    }

    currentBlockIn.getOutputStream().write(fragment.getInformation());
    currentBlockIn.getOutputStream().flush();

    if (fragment.isSegmented())
    {
      if (newBlock)
      {
        startLongOpertation(ILongOperationListener.Operation.READ);
      }
      setLongOpertationProgress(ILongOperationListener.Operation.READ, currentBlockIn.size());
    }
    else
    {
      if (!newBlock)
      {
        endLongOpertation(ILongOperationListener.Operation.READ);
      }
    }


    if (!fragment.isSegmented())
    {
      synchronized (in)
      {
        currentBlockIn.finishBlock();
        in.add(currentBlockIn);
        currentBlockIn = null;
        in.notifyAll();
      }
    }
  }

  /**
   * Returns the destination address for this channel.
   *
   * @return the destination address.
   */
  //@Override
  public HdlcAddress getDestAddress()
  {
    return destAddress;
  }

  /**
   * Returns the HDLC protocol for this channel.
   *
   *
   * @return the HDLC protocol
   */
  public HdlcProtocol getProtocol()
  {
    return protocol;
  }

  /**
   * Returns the source address for this connection.
   * 
   * @return the source address.
   */
  //@Override
  public HdlcAddress getSourceAddress()
  {
    return sourceAddress;
  }

  /**
   * Gets the HDLC negotiation parameters
   *
   * @return The HDLC negotiation parameters.
   */
  public HdlcNegotiationParameters getNegotiationParameters()
  {
    return negotiationParameters;
  }

  void applyNegoationParametersFromSecondaryStation(HdlcNegotiationParameters negotiationParameters)
  {
    //(TX from the secondary station is RX here.)
    this.negotiationParameters = negotiationParameters;
    if (negotiationParameters.getMaxInformationFieldLengthTransmit() > 0)
    {
      maxInformationFieldLengthReceive = negotiationParameters.getMaxInformationFieldLengthTransmit();
    }
    if (negotiationParameters.getMaxInformationFieldLengthReceive() > 0)
    {
      maxInformationFieldLengthTransmit = negotiationParameters.getMaxInformationFieldLengthReceive();
    }
    if (negotiationParameters.getWindowSizeTransmit() > 0)
    {
      windowSizeReceive = negotiationParameters.getWindowSizeTransmit();
    }

    if (negotiationParameters.getWindowSizeReceive() > 0)
    {
      windowSizeTransmit = negotiationParameters.getWindowSizeReceive();
    }
  }

  /**
   * Returns the (negotiated) maximum field length of an information field to receive.
   *
   * @return The maximum field length of an information field to receive.
   */
  //@Override
  public int getMaxInformationFieldLengthReceive()
  {
    return maxInformationFieldLengthReceive;
  }

  /**
   * Returns the (negotiated) maximum field length of an information field to transmit.
   *
   * @return The maximum field length of an information field to transmit.
   */
  //@Override
  public int getMaxInformationFieldLengthTransmit()
  {
    return maxInformationFieldLengthTransmit;
  }

  /**
   * Returns the (negotiated) windows size for receiving information.
   *
   * @return The (negotiated) windows size for receiving information.
   */
  //@Override
  public int getWindowSizeReceive()
  {
    return windowSizeReceive;
  }

  /**
   * Returns the (negotiated) windows size for transmitting information.
   *
   * @return The (negotiated) windows size for transmitting information.
   */
  //@Override
  public int getWindowSizeTransmit()
  {
    return windowSizeTransmit;
  }

  /**
   * Enqueues (and sends) the specified information block.
   *
   * @param informationBlock The information block.
   * @param wait If {@code true} this method waits until the complete block was transmitted <b>and acknowledged</b>.
   * @throws IOException
   */
  public void sendInformationBlock(HdlcInformationBlockOut informationBlock, boolean wait) throws
          IOException
  {
    LOGGER.log(Level.FINER, "Start sending information block. Wait={0}", wait);

    try
    {
      synchronized (this)
      {
        checkForError();

        if (closeRequested || getProtocolState() == ProtocolState.CLOSING)
        {
          throw new IllegalProtocolStateException(ProtocolState.OPEN, ProtocolState.CLOSING);
        }
        if (getProtocolState() == ProtocolState.CLOSE)
        {
          throw new IllegalProtocolStateException(ProtocolState.OPEN, ProtocolState.CLOSE);
        }

        out.add(informationBlock);
      }

      protocol.trigger();
      if (wait)
      {
        informationBlock.waitFor();
        if (informationBlock.getState() == HdlcInformationBlockOut.State.ERROR)
        {
          throw informationBlock.getErrorReason();
        }
      }

    }
    catch (InterruptedException ex)
    {
      throw new InterruptedIOException(ex.getMessage());
    }

    LOGGER.log(Level.FINER, "Finished sending information block. Wait={0}", wait);
  }

  private synchronized void checkForError() throws IOException
  {
    if (errorReason != null)
    {
      throw errorReason;
    }
  }

  /**
   * Sends the given information and waits until the frame(s) was(/were) acknowledged.
   *
   * @param information The information as byte array.
   * @throws IOException
   */
  public void sendInformation(byte[] information) throws IOException
  {
    HdlcInformationBlockOut informationBlock = new HdlcInformationBlockOut(information);
    sendInformationBlock(informationBlock, true);
  }

  /**
   * Sends the given information and waits until the frame(s) was(/were) acknowledged.
   *
   * @param inputStream The information will be read from this input stream.
   * @throws IOException
   */
  public void sendInformation(InputStream inputStream) throws IOException
  {
    HdlcInformationBlockOut informationBlock = new HdlcInformationBlockOut(inputStream);
    sendInformationBlock(informationBlock, true);
  }

  //@Override
  public boolean isOpen()
  {
    return openStateSupport.isOpen();
  }

  public HdlcInformationBlockIn receiveInformationBlock(boolean wait, int timeoutMs) throws IOException
  {
    LOGGER.log(Level.FINER, "Start receiving information block. Wait={0}", wait);

    HdlcInformationBlockIn informationBlockIn = null;

    synchronized (in)
    {
      if (in.size() > 0)
      {
        return in.poll();
      }

      long endTime;

      if (timeoutMs < 0)
      {
        endTime = Long.MAX_VALUE;
      }
      else
      {
        endTime = System.currentTimeMillis() + timeoutMs;
      }

      if (wait)
      {
        while (in.size() == 0)
        {
          try
          {
            checkForError();
            if (getProtocolState() == ProtocolState.CLOSE)
            {
              throw new IllegalProtocolStateException(ProtocolState.OPEN, getProtocolState());
            }

            long waitTime = 2000;

            if (timeoutMs >= 0)
            {
              if (endTime < timeStampLastRxIOrRnrFrame + timeoutMs)
              {
                endTime = timeStampLastRxIOrRnrFrame + timeoutMs;
              }

              waitTime = endTime - System.currentTimeMillis();

              if (waitTime > 2000)
              {
                waitTime = 2000;
              }

              if (waitTime <= 0)
              {
                throw new TimeoutIOException("Timeout waiting for I-Frame");
              }
            }
            in.wait(waitTime);
          }
          catch (InterruptedException ex)
          {
            throw new InterruptedIOException();
          }
        }

        informationBlockIn = in.poll();
      }
    }
    LOGGER.log(Level.FINER, "Finished receiving information block. Wait={0}", wait);
    return informationBlockIn;
  }

  public ProtocolState getProtocolState()
  {
    return openStateSupport.getProtocolState();
  }

  void setOpenState(ProtocolState openState)
  {
    synchronized (this)
    {
      openStateSupport.setState(openState, false);
      notifyAll();
    }
    synchronized (in)
    {
      in.notifyAll();
    }
    openStateSupport.notifyObservers();
  }

  public synchronized void waitForOpen() throws IOException
  {
    try
    {
      while (getProtocolState() == ProtocolState.OPENING)
      {
        wait();
      }
      checkForError();
    }
    catch (InterruptedException ex)
    {
      throw new InterruptedIOException();
    }
  }

  public synchronized void waitForClose() throws IOException
  {
    try
    {
      while (getProtocolState() != ProtocolState.CLOSE)
      {
        wait();
      }
      checkForError();
    }
    catch (InterruptedException ex)
    {
      throw new InterruptedIOException();
    }
  }

  private IOException errorReason;

  public synchronized IOException getReason()
  {
    return errorReason;
  }

  public synchronized boolean isCloseRequested()
  {
    return closeRequested;
  }

  //@Override
  public void addProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateSupport.addProtocolStateListener(observer);
  }

  //@Override
  public void removeProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateSupport.addProtocolStateListener(observer);
  }

  public long getNextPoll()
  {
    return nextPoll;
  }

  void setNextPoll(long nextPoll)
  {
    this.nextPoll = nextPoll;
  }

  volatile ILongOperationListener longOperationListener = null;

  public ILongOperationListener getLongOperationListener()
  {
    return longOperationListener;
  }

  public void setLongOperationListener(final ILongOperationListener longOperationListener)
  {
    this.longOperationListener = longOperationListener;
  }

  private void startLongOpertation(final ILongOperationListener.Operation operation)
  {
    final ILongOperationListener l = longOperationListener;
    if (l != null)
    {
      l.longOperationStart(this, operation);
    }
  }

  private void endLongOpertation(final ILongOperationListener.Operation operation)
  {
    final ILongOperationListener l = longOperationListener;
    if (l != null)
    {
      l.longOperationEnd(this, operation);
    }
  }

  private void setLongOpertationProgress(final ILongOperationListener.Operation operation,
                                         final long byteCount)
  {
    final ILongOperationListener l = longOperationListener;
    if (l != null)
    {
      l.longOperationBytesTransfered(this, operation, byteCount);
    }
  }

//  public int getReceiveSequenceNo()
//  {
//    return normalResponseModeSupport.getReceiveSequenceNo();
//  }
//
//  public int getSendSequenceNo()
//  {
//    return normalResponseModeSupport.getSendSequenceNo();
//  }
}
