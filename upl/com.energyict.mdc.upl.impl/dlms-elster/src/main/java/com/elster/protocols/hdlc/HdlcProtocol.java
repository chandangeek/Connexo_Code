/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcProtocol.java $
 * Version:     
 * $Id: HdlcProtocol.java 6772 2013-06-14 15:12:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 14:23:47
 */
package com.elster.protocols.hdlc;

import com.elster.protocols.IProtocol;
import com.elster.protocols.IProtocolStateObserver;
import com.elster.protocols.hdlc.NormalResponseModeSupport.InformationFragment;
import com.elster.protocols.hdlc.NormalResponseModeSupport.NrmFatalException;
import com.elster.protocols.hdlc.NormalResponseModeSupport.NrmTempException;
import com.elster.protocols.streams.ChecksumIOException;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.ProtocolStateObservableSupport;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.hdlc.HdlcControlField.CommandAndResponseType;
import com.elster.protocols.streams.TimeoutIOException;
import com.elster.protocols.streams.AbstractTimeoutableInputStream;
import com.elster.protocols.streams.TimeoutInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the HDLC Protocol.
 * <P>
 * This implementation supports only the primary station part of
 * the TWA (two way alternating) NRM (Normal response mode)
 * <P>
 * At the moment there are following restrictions:
 * <ul>
 * <li>Only window sizes of 1 are supported</li>
 * </ul>
 *
 * @author osse
 */
public class HdlcProtocol implements IProtocol
{


  public static final int FLAG = 0x7E;
  //private IStreamProtocol sublayer;
  private TimeoutInputStream in;
  private OutputStream out;
  private final List<HdlcChannel> channels = new ArrayList<HdlcChannel>();
  private SendAndReceiveThread sendAndReceiveThread;
  private final ProtocolStateObservableSupport openStateSupport = new ProtocolStateObservableSupport(this);
  private static final Logger LOGGER = Logger.getLogger(HdlcProtocol.class.getName());
  private static final HdlcChannel[] EMPTY_CHANNELS = new HdlcChannel[0];
  private final boolean sendDiscBeforeOpen = false;
//  private ITimeoutControl timeoutControl = null;
  private int pollingIntervall = 2000;
  private int responseTimeout = 2000;
  private int internalIntervall = 250;

  /**
   * Creates a new HDLC Protocol instance and connects it with the specified
   * sub layer.
   */
  public HdlcProtocol(IStreamProtocol sublayer)
  {
    connect(sublayer);
  }

  /**
   * Connects this instance with an sub layer.
   * <P>
   * Through this sub layer all communication is done.<P>
   * To enable timeout functionality the sub layer should provide an {@link AbstractTimeoutableInputStream }
   *
   * @param sublayer
   */
  private void connect(IStreamProtocol sublayer)
  {
    //this.sublayer = sublayer;
    in = new TimeoutInputStream(sublayer.getInputStream(), responseTimeout);
    out = new BufferedOutputStream(sublayer.getOutputStream());
  }

  /**
   * Sets the (interval) timeout for the HDLC protocol.
   *
   * @param timeoutMillis The timeout in milliseconds
   * @return <b>true</b> if the timeout could be set.
   */
  public boolean setResponseTimeout(int timeoutMillis)
  {
    responseTimeout = timeoutMillis;
    in.setTimeout(timeoutMillis);
    return true;
  }

  public int getPollingIntervall()
  {
    return pollingIntervall;
  }

  public void setPollingIntervall(int pollingIntervall)
  {
    this.pollingIntervall = pollingIntervall;
  }

  public int getInternalIntervall()
  {
    return internalIntervall;
  }

  public void setInternalIntervall(int internalIntervall)
  {
    this.internalIntervall = internalIntervall;
  }

  /**
   * "Opens" the protocol.
   * <p>
   * It starts the send an receiving thread.
   * <p>
   * A real connection can be established by the {@link #openChannel(com.elster.protocols.hdlc.HdlcAddress, com.elster.protocols.hdlc.HdlcAddress, boolean)}
   * method.
   *
   * @throws IOException
   */
  public void open() throws IOException
  {
    setOpenState(ProtocolState.OPENING, false);
    setResponseTimeout(responseTimeout);
    synchronized (this)
    {
//      try
      {
        if (sendAndReceiveThread == null || !sendAndReceiveThread.isAlive())
        {
//          discardIncomingBytes(200, 3000);
          sendAndReceiveThread = new SendAndReceiveThread();
          sendAndReceiveThread.setDaemon(true);
          sendAndReceiveThread.start();
          setOpenState(ProtocolState.OPEN, false);
        }
      }
//      catch (IOException exception)
//      {
//        if (sendAndReceiveThread != null)
//        {
//          sendAndReceiveThread.interrupt();
//        }
//        setOpenState(ProtocolState.CLOSE, false);
//        throw exception;
//      }
    }
    openStateSupport.notifyObservers();
  }

  /**
   * Closes all HDLC connections and the HDLC protocol
   *
   */
  public void close() throws IOException
  {
    if (!openStateSupport.setState(ProtocolState.OPEN, ProtocolState.CLOSING, true))
    {
      return;
    }

    HdlcChannel localChannels[];
    synchronized (channels)
    {
      localChannels = channels.toArray(EMPTY_CHANNELS);
    }

    for (HdlcChannel c : localChannels)
    {
      try
      {
        c.close();
      }
      catch (IOException ex)
      {
        c.closeOnException(ex);
      }
    }

    synchronized (this)
    {
      if (sendAndReceiveThread != null)
      {
        try
        {
          sendAndReceiveThread.interrupt();
          sendAndReceiveThread.join(5000);

          if (sendAndReceiveThread.isAlive())
          {
            LOGGER.severe("HDLC send and receive thread not succesfully terminated");
          }
        }
        catch (InterruptedException ex)
        {
          Thread.currentThread().interrupt(); //Set the interrupt flag again.
        }
        sendAndReceiveThread = null;
      }
    }

    openStateSupport.setState(ProtocolState.CLOSE, true);
  }

  /**
   * Opens a new connection.
   * <p>
   * Through this connection data can be sent an received.
   *
   * @param sourceAddress The source address.
   * @param destAddress The destination address.
   * @return The established connection
   * @throws IOException
   */
  public HdlcChannel openChannel(HdlcAddress sourceAddress, HdlcAddress destAddress, boolean wait) throws
          IOException
  {
    HdlcChannel channel = new HdlcChannel(this, sourceAddress, destAddress, ProtocolState.OPENING);
    synchronized (channels)
    {
      channels.add(channel);
    }

    trigger();

    if (wait)
    {
      channel.waitForOpen();
    }
    return channel;
  }

  void closeChannel(HdlcChannel channel) throws IOException
  {
    try
    {
      HdlcFrame discFrame = HdlcFrameFactory.createDiscFrame(channel);
      sendFrame(discFrame);

      HdlcFrame uaFrame = receiveFrame();

      if (HdlcControlField.CommandAndResponseType.UA != uaFrame.getControllField().getCommandAndResponseType())
      {
        throw new HdlcProtocolException("Unexpected frame after disconnect frame: "
                                        + uaFrame.getControllField().getCommandAndResponseType()
                                        + " expected: " + HdlcControlField.CommandAndResponseType.UA);
      }
    }
    catch (IOException ex)
    {
      LOGGER.log(Level.INFO, "HDLC- Error closing channel: {0}", ex.toString());
      //--- futher handling not reasonable
    }
    finally
    {
      channel.setOpenState(ProtocolState.CLOSE);
      synchronized (channels)
      {
        channels.remove(channel);
      }
    }
  }

  void sendAndReceiveInformation(HdlcChannel channel, boolean poll) throws IOException
  {
    boolean moreDataAvailable = false;
    boolean sync = false;
    boolean rnr = channel.rnrState;

    int trialsLeft = 6;
    //   int timeoutTrialsLeft = 3;

    IOException lastException = null;

    do
    {
      LOGGER.log(Level.FINER, "Send and receive information. Trials left: {0}", trialsLeft);
      try
      {
        if (sync)
        {
          doSync(channel);
          sync = false;
          moreDataAvailable = true;
          trialsLeft = 6;
        }
        else if (rnr)
        {
          moreDataAvailable = doSync(channel);
          rnr = false;
          sync = false;
          trialsLeft = 6;
        }
        else
        {
          moreDataAvailable = doInformationExchange(channel, poll || moreDataAvailable);
          poll = false;
          sync = false;
          trialsLeft = 6;
        }
      }
      catch (NrmTempException ex)
      {
        lastException = new IOException(ex.getMessage());
        //discardFrame();
        sync = true;
        trialsLeft--;
        LOGGER.log(Level.FINE, "NrmTempException", ex);
      }
      catch (ChecksumIOException ex)
      {
        LOGGER.log(Level.FINE, "ChecksumIOException", ex);
        lastException = ex;
        discardFrame();
        sync = true;
        trialsLeft--;
      }
      catch (HdlcDecodingIOException ex)
      {
        LOGGER.log(Level.FINE, "HdlcDecodingIOException", ex);
        lastException = ex;
        discardFrame();
        sync = true;
        trialsLeft--;
      }
      catch (TimeoutIOException ex)
      {
        //---- Discarding the frame is not necessary here!!!
        LOGGER.log(Level.FINE, "TimeoutIOException", ex);
        lastException = ex;
        sync = true;
        trialsLeft -= 2; //Timeouts should end comunication faster.
      }
      catch (NrmFatalException ex)
      {
        LOGGER.log(Level.FINE, "NrmFatalException", ex);
        throw new IOException(ex.getMessage());
      }

    }
    while ((moreDataAvailable || sync) && trialsLeft > 0);

    if (trialsLeft <= 0)
    {
      throw lastException;
    }

  }

  /**
   * Sends one RR frame to (re)synchronize the too sides.
   * 
   * @param channel
   * @throws IOException
   * @throws com.elster.protocols.hdlc.NormalResponseModeSupport.NrmFatalException
   * @throws com.elster.protocols.hdlc.NormalResponseModeSupport.NrmTempException 
   */
  private boolean doSync(HdlcChannel channel) throws IOException, NrmFatalException, NrmTempException
  {
    NormalResponseModeSupport nrm = channel.normalResponseModeSupport;

    HdlcFrame txRrFrame = nrm.getRrFrame();
    sendFrame(txRrFrame);
    HdlcFrame rxFrame = receiveFrame();
    handleReceivedFrame(channel, rxFrame);
    return rxFrame.isSegmentationBit();
  }

  /**
   * Standard data exchange via I-Frames
   * 
   * @param channel The channel
   * @param forceRr Sends an RR frame if no new data to send is available.
   * @throws IOException
   * @throws com.elster.protocols.hdlc.NormalResponseModeSupport.NrmFatalException
   * @throws com.elster.protocols.hdlc.NormalResponseModeSupport.NrmTempException 
   */
  private boolean doInformationExchange(HdlcChannel channel, boolean forceRr) throws IOException,
                                                                                     NrmFatalException,
                                                                                     NrmTempException
  {
    boolean moreDataToReceive = forceRr;

    NormalResponseModeSupport nrm = channel.normalResponseModeSupport;

    //Supply data to the NRM.
    if (nrm.fragmentToSendRequired())
    {
      InformationFragment nextFraqmentToSend = channel.getNextFraqmentToSend();
      if (nextFraqmentToSend != null)
      {
        nrm.addFragmentToSend(nextFraqmentToSend);
      }
    }

    HdlcFrame rxFrame = null;

    //Send an I-Frame (if available)
    HdlcFrame txIFrame = nrm.getNextIFrame();
    if (txIFrame != null)
    {
      sendFrame(txIFrame);
      rxFrame = receiveFrame();
    }
    else if (moreDataToReceive)
    {
      HdlcFrame txRrFrame = nrm.getRrFrame();
      sendFrame(txRrFrame);
      rxFrame = receiveFrame();
    }

    if (rxFrame != null)
    {
      handleReceivedFrame(channel, rxFrame);
      
      while (rxFrame!=null && !rxFrame.getControllField().isPoolFinal())
      {
        rxFrame = receiveFrame();
        handleReceivedFrame(channel, rxFrame);
      }
      moreDataToReceive = rxFrame.isSegmentationBit();
    }
    else
    {
      moreDataToReceive = false;
    }

    return moreDataToReceive;
  }

  private void handleReceivedFrame(HdlcChannel channel, HdlcFrame rxFrame) throws NrmFatalException,
                                                                                  NrmTempException,
                                                                                  IOException
  {

    if (!rxFrame.getDestAddress().equals(channel.getSourceAddress()))
    {
      throw new HdlcProtocolException("The received frame has a wrong destination address."
                                      + " Expected:" + channel.getSourceAddress() + " Received:" + rxFrame.
              getDestAddress());
    }

    if (!rxFrame.getSourceAddress().equals(channel.getDestAddress()))
    {
      throw new HdlcProtocolException("The received frame has a wrong source address."
                                      + " Expected:" + channel.getDestAddress() + " Received:" + rxFrame.
              getSourceAddress());
    }

    NormalResponseModeSupport nrm = channel.normalResponseModeSupport;
    switch (rxFrame.getControllField().getCommandAndResponseType())
    {
      case I:
      case RR:
      case RNR:
      case UI:
        nrm.handleReveivedFrame(rxFrame);
        while (nrm.nextConfirmedFraqmentAvailable())
        {
          channel.confirmFraqment(nrm.getNextConfirmedFraqment());
        }

        while (nrm.nextReceivedFraqmentAvailable())
        {
          channel.addReceivedFragment(nrm.getNextReceivedFraqment());
        }
        
        if (rxFrame.getControllField().getCommandAndResponseType()==CommandAndResponseType.RNR)
        {
          channel.rnrState = true;
          channel.setNextPoll(System.currentTimeMillis() + 3000);
          channel.notifyRnr();          
        }
        else
        {
          channel.rnrState = false;
          channel.setNextPoll(System.currentTimeMillis() + pollingIntervall);
        }
        break;
      default:
        throw new HdlcProtocolException("unexpected frame type: " + rxFrame.getControllField().
                getCommandAndResponseType());
    }

  }

  private void sendFrame(HdlcFrame frame) throws IOException
  {
    LOGGER.log(Level.FINE, "sendFrame: sending {0}-Frame",
               frame.getControllField().getCommandAndResponseType());
    out.write(FLAG);
    frame.encode(out);
    out.write(FLAG);
    out.flush();
  }

  private HdlcFrame receiveFrame() throws IOException
  {
    return receiveFrame(false);
  }

  private HdlcFrame receiveFrame(boolean ignoreLeadingNot7EBytes) throws IOException
  {
    LOGGER.log(Level.FINE, "Receiving frame");
    InputStream inputStream = in;

    if (ignoreLeadingNot7EBytes)
    {
      int b;
      do
      {
        b = inputStream.read();
        if (b < 0)
        {
          throw new EOFException("EOF while waiting for 7E (HDLC)");
        }
      }
      while (b != FLAG);
    }
    HdlcFrame frame = new HdlcFrame();
    frame.decode(inputStream);

    int endingFlag = inputStream.read();
    if (endingFlag != FLAG)
    {
      throw new HdlcDecodingIOException("No ending flag received. Expected: " + FLAG + ", Received: "
                                        + endingFlag);
    }

    LOGGER.log(Level.FINE, "Received {0}-Frame", frame.getControllField().getCommandAndResponseType());

    return frame;
  }

  private void discardFrame() throws IOException
  {
    LOGGER.fine("Discarding frame");
    if (!discardIncomingBytes(responseTimeout,5* responseTimeout))
    {
      throw new HdlcProtocolException("Discarding frame failed.");
    }
  }

  private boolean discardIncomingBytes(final int expectedIdleTime, final int totalTimeout) throws IOException
  {
    LOGGER.fine("Discarding incomming chars");

    boolean timeoutExceptionCaught = false;
 
    long maximumWaitTime = System.currentTimeMillis() + totalTimeout;
    byte[] skipBuffer = new byte[256];

    while (!timeoutExceptionCaught)
    {
//      int available = in.available();
//
//      if (available > 0)
//      {
//        in.skip(available);
//      }

      try
      {
        int b = in.readTO(skipBuffer, 0, skipBuffer.length, expectedIdleTime);

        if (b < 0)
        {
          throw new HdlcProtocolException("Discarding incomming bytes : unexpected end of stream");
        }

        if (System.currentTimeMillis() > maximumWaitTime)
        {
          break;
        }

      }
      catch (TimeoutIOException ignore)
      {
        timeoutExceptionCaught = true;
      }

    }
    return timeoutExceptionCaught;
  }

  public synchronized void trigger()
  {
    if (sendAndReceiveThread != null)
    {
      sendAndReceiveThread.trigger();
    }

  }

  //@Override
  public void addProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateSupport.addProtocolStateListener(observer);
  }

  //@Override
  public void removeProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateSupport.removeProtocolStateListener(observer);
  }

  public ProtocolState getProtocolState()
  {
    return openStateSupport.getProtocolState();
  }

  private void setOpenState(ProtocolState openState, boolean notify)
  {
    openStateSupport.setState(openState, notify);
  }

  public boolean isOpen()
  {
    return openStateSupport.isOpen();
  }

  private class SendAndReceiveThread extends Thread
  {
    private final Trigger triggerSupport = new Trigger();

    public SendAndReceiveThread(String name)
    {
      super(name);
    }

    public SendAndReceiveThread()
    {
      super("HDLC send an receive thread");
    }

    @Override
    public void run()
    {
      try
      {
        while (!Thread.interrupted())
        {
          HdlcChannel[] localChannels;

          synchronized (channels)
          {
            localChannels = channels.toArray(new HdlcChannel[channels.size()]);
          }

          for (HdlcChannel c : localChannels)
          {
            try
            {
              if (c.isCloseRequested())
              {
                if (c.getProtocolState() == ProtocolState.OPEN)
                {
                  c.setOpenState(ProtocolState.CLOSING);
                }
              }

              switch (c.getProtocolState())
              {
                case OPENING:
                  openChannel(c);
                  c.setNextPoll(System.currentTimeMillis() + pollingIntervall);
                  break;
                case OPEN:
                  long currentTime = System.currentTimeMillis();
                  sendAndReceiveInformation(c, c.getNextPoll() < currentTime);
                  break;
                case CLOSING:
                  if (c.frameToSendAvailable())
                  {
                    sendAndReceiveInformation(c, false);
                  }
                  else
                  {
                    closeChannel(c);
                    break;
                  }
              }
            }
            catch (InterruptedIOException ex)
            {
              LOGGER.log(Level.INFO, "Closing channel caused by exception: {0}", ex);
              c.closeOnException(ex);
              Thread.currentThread().interrupt(); //keep the interrupt state.
            }
            catch (IOException ex)
            {
              LOGGER.log(Level.INFO, "Closing channel caused by exception: {0}", ex);
              c.closeOnException(ex);
            }
            catch (RuntimeException ex)
            {
              LOGGER.log(Level.INFO, "Closing channel caused by exception: {0}", ex);
              c.closeOnException(new IOException("Unexpected communication exception", ex));
            }
          }


          boolean dataAvailable = false;
          for (HdlcChannel c : localChannels)
          {
            if (c.isOpen() && c.frameToSendAvailable())
            {
              dataAvailable = true;
            }
          }

          if (!dataAvailable)
          {
            triggerSupport.waitForTrigger(internalIntervall, true);
          }
        }
      }
      catch (InterruptedException ex)
      {
      }

    }

    public void trigger()
    {
      triggerSupport.trigger();
    }

    private void openChannel(HdlcChannel channel) throws
            IOException
    {

      try
      {
        int trialsLeft = 3;
        boolean finished = false;
        IOException lastException = null;

        while (trialsLeft > 0 && !finished)
        {

          try
          {
            if (sendDiscBeforeOpen)
            {
              HdlcFrame discFrame = HdlcFrameFactory.createDiscFrame(channel);
              sendFrame(discFrame);
              receiveFrame();
            }

            HdlcFrame snrmFrame = HdlcFrameFactory.createSnrmFrame(channel);

            HdlcNegotiationParameters negoationParametersPrimary = new HdlcNegotiationParameters();
            negoationParametersPrimary.setMaxInformationFieldLengthReceive(2030);
            negoationParametersPrimary.setMaxInformationFieldLengthTransmit(2030);
            negoationParametersPrimary.setWindowSizeReceive(1);
            negoationParametersPrimary.setWindowSizeTransmit(1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            negoationParametersPrimary.encode(outputStream);

            snrmFrame.setInformationBytes(outputStream.toByteArray());

            sendFrame(snrmFrame);

            HdlcFrame uaFrame = receiveFrame(true);

            if (HdlcControlField.CommandAndResponseType.UA
                != uaFrame.getControllField().getCommandAndResponseType())
            {
              throw new HdlcProtocolException("Unexpected Frame: " + uaFrame.getControllField().
                      getCommandAndResponseType() + " expected: " + HdlcControlField.CommandAndResponseType.UA);
            }

            if (uaFrame.getInformationBytes().length > 0)
            {
              byte[] information = uaFrame.getInformationBytes();
              if ((0xFF & information[0]) == 0x81)
              {
                HdlcNegotiationParameters negoationParametersSecondary = new HdlcNegotiationParameters();
                negoationParametersSecondary.decode(new ByteArrayInputStream(information), false);
                channel.applyNegoationParametersFromSecondaryStation(negoationParametersSecondary);
              }
            }

            channel.setOpenState(ProtocolState.OPEN);
            LOGGER.log(Level.FINE, "HDLC channel opened: {0}", channel);
            finished = true;
          }
          catch (ChecksumIOException ex)
          {
            LOGGER.log(Level.FINE, "ChecksumIOException", ex);
            lastException = ex;
            discardFrame();
            trialsLeft--;
          }
          catch (HdlcDecodingIOException ex)
          {
            LOGGER.log(Level.FINE, "HdlcDecodingIOException", ex);
            lastException = ex;
            discardFrame();
            trialsLeft--;
          }
          catch (TimeoutIOException ex)
          {
            //---- Discarding the frame is not necessary here!!!
            LOGGER.log(Level.FINE, "TimeoutIOException", ex);
            lastException = ex;
            trialsLeft -= 2;
          }
        }

        if (!finished)
        {
          throw lastException;
        }

      }
      catch (IOException ex)
      {
        channel.closeOnException(ex);
      }
    }

  }
  
  //@Override
  public void cancelOpen()
  {
  }
  
  

}
