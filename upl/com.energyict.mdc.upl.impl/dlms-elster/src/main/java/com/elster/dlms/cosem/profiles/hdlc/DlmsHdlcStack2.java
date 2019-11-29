/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/profiles/hdlc/DlmsHdlcStack2.java $
 * Version:
 * $Id: DlmsHdlcStack2.java 5802 2013-01-08 16:03:58Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  18.05.2010 13:16:02
 */
package com.elster.dlms.cosem.profiles.hdlc;

import com.elster.dlms.cosem.application.services.open.OpenRequest;
import com.elster.dlms.cosem.application.services.open.OpenResponse;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocols.*;
import com.elster.protocols.hdlc.HdlcProtocol;
import com.elster.protocols.optical.ProtocolOptical;
import com.elster.protocols.stacks.StreamingProtocolStack;
import java.io.IOException;
import java.util.Stack;

/**
 * HDLC Stack.<P> Complete DLMS stack using the HDLC profile and an RS232 port.
 *
 * @author osse
 */
public class DlmsHdlcStack2 implements IProtocolStateObservable
{
  private final StreamingProtocolStack subStack;
  private DlmsLlcHdlc dlmsLlc;
  private CosemApplicationLayer cosemApplicationLayer;
  private final Stack<IProtocol> protocols = new Stack<IProtocol>();
  private int serverLowerHdlcAddress = 0x1826;
  private int serverUpperHdlcAddress = 0x1;
  private int clientId = 0x10;
  private int pollingIntervall = 8000;
  private int responseTimeOut = 1000;
  private int iFrameTimeOut = 6000;
  private int clientMaxReceivePduSize = 65535;
  private boolean modeE = false;
  private final ProtocolStateObservableSupport openStateObservableSupport =
          new ProtocolStateObservableSupport(this);
  private HdlcProtocol hdlcProtocol;

  public DlmsHdlcStack2(StreamingProtocolStack subStack)
  {
    this.subStack = subStack;
  }

  protected void openHdlc() throws IOException
  {
    if (modeE)
    {
      ProtocolOptical protocolOptical = new ProtocolOptical(subStack.getStreamProtocol(), subStack.getBaudrateSupport());
      protocolOptical.setIgnoreModeAck(false);
      protocolOptical.open();
    }
    
    hdlcProtocol = new HdlcProtocol(subStack.getStreamProtocol());
    hdlcProtocol.setPollingIntervall(pollingIntervall);
    hdlcProtocol.setResponseTimeout(responseTimeOut);
    hdlcProtocol.open();
  }

  protected CosemApplicationLayer createCosemApplicationLayer() throws IOException
  {
    if (hdlcProtocol == null || !hdlcProtocol.isOpen())
    {
      openHdlc();
    }

    dlmsLlc = new DlmsLlcHdlc(hdlcProtocol, serverLowerHdlcAddress);

    dlmsLlc.addProtocolStateListener(
            new IProtocolStateObserver()
            {
              //@Override
              public void openStateChanged(final Object sender, final ProtocolState oldState,
                                           final ProtocolState newState)
              {
                //nothing to do.
              }

              //@Override
              public void connectionBroken(final Object sender, final Object orign, final Exception reason)
              {
                openStateObservableSupport.setState(ProtocolState.CLOSE, true);
                openStateObservableSupport.notifyConnectionBroken(orign, reason);
                //cleanup();
              }

            });

    cosemApplicationLayer =
            new CosemApplicationLayer(dlmsLlc, clientId);
    cosemApplicationLayer.setPduRxTimeout(iFrameTimeOut);
    cosemApplicationLayer.setClientMaxReceivePduSize(clientMaxReceivePduSize);

    return cosemApplicationLayer;
  }

  /**
   * Opens the stack using the specified open request.
   *
   * @param openRequest The open request to open the stack.
   * @return The open response from the device.
   * @throws IOException
   */
  public OpenResponse open(final OpenRequest openRequest) throws IOException
  {
    OpenResponse response = null;
    openStateObservableSupport.setState(ProtocolState.OPENING, true);
    try
    {
      createCosemApplicationLayer();
      response = cosemApplicationLayer.open(serverUpperHdlcAddress, openRequest);
      openStateObservableSupport.setState(ProtocolState.OPEN, true);
    }
    catch (IOException ex)
    {
      cleanup();
      openStateObservableSupport.setState(ProtocolState.CLOSE, true);
      throw ex;
    }
    return response;
  }

  /**
   * Opens the stack using the specified open request.
   *
   * @param key The open request to open the stack.
   * @return The open response from the device.
   * @throws IOException
   */
  public OpenResponse openLls(final String key) throws IOException
  {
    OpenResponse response = null;
    openStateObservableSupport.setState(ProtocolState.OPENING, true);
    try
    {
      createCosemApplicationLayer();
      response = cosemApplicationLayer.open(serverUpperHdlcAddress, key);
      openStateObservableSupport.setState(ProtocolState.OPEN, true);
    }
    catch (IOException ex)
    {
      cleanup();
      openStateObservableSupport.setState(ProtocolState.CLOSE, true);
      throw ex;
    }

    return response;
  }

  /**
   * Opens the stack using the specified open request.
   *
   * @return The open response from the device.
   * @throws IOException
   */
  public OpenResponse openNoSecurity() throws IOException
  {
    OpenResponse response = null;
    openStateObservableSupport.setState(ProtocolState.OPENING, true);
    try
    {
      createCosemApplicationLayer();
      response = cosemApplicationLayer.open(serverUpperHdlcAddress);
      openStateObservableSupport.setState(ProtocolState.OPEN, true);
    }
    catch (IOException ex)
    {
      cleanup();
      openStateObservableSupport.setState(ProtocolState.CLOSE, true);
      throw ex;
    }

    return response;
  }

  public OpenResponse openHls(final byte[] systemTitle, final byte[] encryptionKey,
                              final byte[] authenticationKey) throws
          IOException
  {
    OpenResponse response = null;

    openStateObservableSupport.setState(ProtocolState.OPENING, true);
    try
    {
      createCosemApplicationLayer();
      response = cosemApplicationLayer.openHls(serverUpperHdlcAddress, systemTitle, encryptionKey,
                                               authenticationKey);
      openStateObservableSupport.setState(ProtocolState.OPEN, true);
    }
    catch (IOException ex)
    {
      cleanup();
      openStateObservableSupport.setState(ProtocolState.CLOSE, true);
      throw ex;
    }
    return response;
  }

//  public ComPortDriver getComPortDriver()
//  {
//    return comPortDriver;
//  }
  public void close() throws IOException
  {
    try
    {
      openStateObservableSupport.setState(ProtocolState.CLOSING, true);
      if (cosemApplicationLayer != null)
      {
        cosemApplicationLayer.close();
      }

      while (!protocols.isEmpty())
      {
        protocols.pop().close();
      }
    }
    finally
    {
      cleanup();
      openStateObservableSupport.setState(ProtocolState.CLOSE, true);
    }
  }

  public void closeApplicationLayer() throws IOException
  {
    if (cosemApplicationLayer != null)
    {
      cosemApplicationLayer.close();
    }
    
//    if (hdlcProtocol!=null)
//    {
//      hdlcProtocol.close();
//    }
    openStateObservableSupport.setState(ProtocolState.CLOSE, true);
  }

  /**
   * Closes all Layers and "frees" the resources.<P> Throws no exception.
   */
  public void cleanup()
  {
    if (cosemApplicationLayer != null)
    {
      try
      {
        cosemApplicationLayer.close();
      }
      catch (Exception e)
      {
        //no exeption handling in cleanup.
      }
      cosemApplicationLayer = null;
    }

    while (!protocols.isEmpty())
    {
      try
      {
        protocols.pop().close();
      }
      catch (Exception e)
      {
      }
    }
    
    subStack.cleanup();
    openStateObservableSupport.setState(ProtocolState.CLOSE, true);
  }

  /**
   * Closes the stack without sending anything.<P> Normally a HDLC DISC frame will be sent.
   */
  public void abort()
  {
    subStack.cleanup();
    cleanup();
  }

//  public HdlcProtocol getHdlcProtocol()
//  {
//    return hdlcProtocol;
//  }
  public CosemApplicationLayer getCosemApplicationLayer()
  {
    return cosemApplicationLayer;
  }

  public int getServerLowerHdlcAddress()
  {
    return serverLowerHdlcAddress;
  }

  public void setServerLowerHdlcAddress(final int lowerHdlcAddress)
  {
    this.serverLowerHdlcAddress = lowerHdlcAddress;
  }

  public int getClientId()
  {
    return clientId;
  }

  public void setClientId(final int clientId)
  {
    this.clientId = clientId;
  }

  public void addProtocolStateListener(final IProtocolStateObserver observer)
  {
    openStateObservableSupport.addProtocolStateListener(observer);
  }

  public void removeProtocolStateListener(final IProtocolStateObserver observer)
  {
    openStateObservableSupport.removeProtocolStateListener(observer);
  }

  public ProtocolState getProtocolState()
  {
    return openStateObservableSupport.getProtocolState();
  }

  public boolean isOpen()
  {
    return openStateObservableSupport.isOpen();
  }

  public int getPollingIntervall()
  {
    return pollingIntervall;
  }

  public void setPollingIntervall(final int pollingIntervall)
  {
    this.pollingIntervall = pollingIntervall;
  }

  public int getResponseTimeOut()
  {
    return responseTimeOut;
  }

  public void setResponseTimeOut(final int responseTimeOut)
  {
    this.responseTimeOut = responseTimeOut;
  }

  public int getIFrameTimeOut()
  {
    return iFrameTimeOut;
  }

  public void setIFrameTimeOut(final int iFrameTimeOut)
  {
    this.iFrameTimeOut = iFrameTimeOut;
  }

  public DlmsLlcHdlc getDlmsLlc()
  {
    return dlmsLlc;
  }

  /**
   * Logical device
   *
   * @return
   */
  public int getServerUpperHdlcAddress()
  {
    return serverUpperHdlcAddress;
  }

  /**
   * Logical Device
   *
   * @param serverUpperHdlcAddress
   */
  public void setServerUpperHdlcAddress(int serverUpperHdlcAddress)
  {
    this.serverUpperHdlcAddress = serverUpperHdlcAddress;
  }

  public void setClientMaxReceivePduSize(int maxPduSize)
  {
    this.clientMaxReceivePduSize = maxPduSize;
  }

  public int getClientMaxReceivePduSize()
  {
    return clientMaxReceivePduSize;
  }

  public boolean isModeE()
  {
    return modeE;
  }

  public void setModeE(boolean modeE)
  {
    this.modeE = modeE;
  }

}
