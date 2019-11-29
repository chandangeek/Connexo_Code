/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/profiles/hdlc/DlmsLlcHdlc.java $
 * Version:     
 * $Id: DlmsLlcHdlc.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  18.05.2010 09:40:26
 */
package com.elster.dlms.cosem.profiles.hdlc;

import com.elster.dlms.cosem.applicationlayer.IDlmsLlc;
import com.elster.dlms.cosem.applicationlayer.ProtocolDataUnitIn;
import com.elster.dlms.cosem.applicationlayer.ProtocolDataUnitOut;
import com.elster.protocols.IProtocolStateObservable;
import com.elster.protocols.IProtocolStateObserver;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.ProtocolStateObservableSupport;
import com.elster.protocols.hdlc.HdlcAddress;
import com.elster.protocols.hdlc.HdlcChannel;
import com.elster.protocols.hdlc.HdlcInformationBlockIn;
import com.elster.protocols.hdlc.HdlcInformationBlockOut;
import com.elster.protocols.hdlc.HdlcProtocol;
import com.elster.protocols.streams.HeaderAddingInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Logical Link Control (LLC) layer for HDLC.
 *
 * @author osse
 */
public class DlmsLlcHdlc implements IDlmsLlc, IProtocolStateObservable
{
  private int serverLowerHdlcAddress = -1;
  private int logicalDeviceId = -1;
  private int hdlcAddresseLength;
  private final HdlcProtocol hdlcProtocol;
  private HdlcChannel hdlcChannel;
  private final ProtocolStateObservableSupport protocolStateSupport = new ProtocolStateObservableSupport(this);
  private final HdlcChannelObserver channelObserver = new HdlcChannelObserver();
  private static final byte[] LLC_HEADER_SEND =
  {
    (byte)0xE6, (byte)0xE6, (byte)0x00
  };
  private static final byte[] LLC_HEADER_RECEIVE =
  {
    (byte)0xE6, (byte)0xE7, (byte)0x00
  };

  /**
   * Creates the LLC.<P>
   *
   * @param hdlcProtocol The underlying HDLC protocol.
   * @param lowerHdlcAddress The server lower HDLC address.
   */
  public DlmsLlcHdlc(HdlcProtocol hdlcProtocol, int lowerHdlcAddress)
  {
    this(hdlcProtocol, lowerHdlcAddress, 4);
  }

  /**
   * Creates the LLC.<P>
   *
   * @param hdlcProtocol The underlying HDLC protocol.
   * @param lowerHdlcAddress The server lower HDLC address.
   * @param hdlcAddressLength  Length of the server HDLC address (normally 4 or 2)
   */
  public DlmsLlcHdlc(HdlcProtocol hdlcProtocol, int lowerHdlcAddress, int hdlcAddressLength)
  {
    this.hdlcProtocol = hdlcProtocol;
    this.serverLowerHdlcAddress = lowerHdlcAddress;
    this.hdlcAddresseLength = hdlcAddressLength;
  }

  //@Override
  public void openLogicalDeviceLink(int clientId, int logicalDeviceId, boolean wait) throws IOException
  {
    this.logicalDeviceId = logicalDeviceId;
    hdlcChannel = hdlcProtocol.openChannel(new HdlcAddress(clientId), new HdlcAddress(logicalDeviceId,
                                                                                      serverLowerHdlcAddress,
                                                                                      hdlcAddresseLength),
                                           wait);
    hdlcChannel.addProtocolStateListener(channelObserver);
    protocolStateSupport.setState(hdlcChannel.getProtocolState(), true);
  }

  //@Override
  public void closeLogicalDeviceLink(boolean wait) throws IOException
  {
    hdlcChannel.close();
    hdlcChannel.removeProtocolStateListener(channelObserver);
    //hdlcChannel = null;  TODO: check if can be set to null
    //logicalDeviceId = -1;
  }

  //@Override
  public int getLogicalDeviceId()
  {
    return logicalDeviceId;
  }

  //@Override
  public void writePdu(ProtocolDataUnitOut pdu, boolean wait) throws IOException
  {
    //todo: check if protocol is open
    HdlcInformationBlockOut hdlcBlock = new HdlcInformationBlockOut(new HeaderAddingInputStream(pdu.
            getDataInputStream(), LLC_HEADER_SEND));
    hdlcChannel.sendInformationBlock(hdlcBlock, wait);
  }

  //@Override
  public ProtocolDataUnitIn readPdu(boolean wait, int timeoutMs) throws IOException
  {
    //todo: check if protocol is open
    HdlcInformationBlockIn hdlcBlock = hdlcChannel.receiveInformationBlock(wait, timeoutMs);

    if (hdlcBlock == null)
    {
      return null;
    }

    byte[] header = new byte[3];

    int bytesRead = hdlcBlock.getInputStream().read(header, 0, 3);


    if (bytesRead != 3 || !Arrays.equals(header, LLC_HEADER_RECEIVE))
    {
      throw new IOException("Unexpected LLC header"); //eventuell Block einfach ignorieren.
    }

    return new ProtocolDataUnitIn(hdlcBlock.getInputStream());
  }

  //@Override
  public ProtocolState getProtocolState()
  {
    return protocolStateSupport.getProtocolState();
  }

  //@Override
  public void addProtocolStateListener(IProtocolStateObserver observer)
  {
    protocolStateSupport.addProtocolStateListener(observer);
  }

  //@Override
  public void removeProtocolStateListener(IProtocolStateObserver observer)
  {
    protocolStateSupport.removeProtocolStateListener(observer);
  }

  public boolean isOpen()
  {
    return protocolStateSupport.isOpen();
  }

  private class HdlcChannelObserver implements IProtocolStateObserver
  {
    //@Override
    public void openStateChanged(Object sender, ProtocolState oldState, ProtocolState newState)
    {
      protocolStateSupport.setState(newState, true);
    }

    public void connectionBroken(Object sender, Object orign, Exception reason)
    {
      protocolStateSupport.notifyConnectionBroken(orign, reason);
    }

  }

  public HdlcChannel getHdlcChannel()
  {
    return hdlcChannel;
  }

}
