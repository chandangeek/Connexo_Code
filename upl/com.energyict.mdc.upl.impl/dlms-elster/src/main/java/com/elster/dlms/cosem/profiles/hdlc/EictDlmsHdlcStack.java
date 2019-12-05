/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/profiles/hdlc/EictDlmsHdlcStack.java $
 * Version:     
 * $Id: EictDlmsHdlcStack.java 6742 2013-06-12 09:46:38Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  18.05.2010 13:16:02
 */
package com.elster.dlms.cosem.profiles.hdlc;

import com.elster.dlms.cosem.application.services.open.OpenResponse;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocols.*;
import com.elster.protocols.hdlc.HdlcProtocol;
import com.elster.protocols.optical.ProtocolOptical;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * HDLC Stack.<P>
 * DLMS stack using the HDLC profile.
 *
 * @author osse
 */
public class EictDlmsHdlcStack implements IProtocolStateObservable
{
  public enum SecurityLevel
  {
    LOWEST_LEVEL_SECURITY, LOW_LEVEL_SECURITY, HIGH_LEVEL_SECURITY_USING_GMAC
  };

  private final InputStream in;
  private final OutputStream out;
  private HdlcProtocol hdlcProtocol;
  private CosemApplicationLayer cosemApplicationLayer;
  //--- Timings ---
  private int pollingInterval = 8000;
  private int responseTimeOut = 3000;
  //--- Connection parameters ---
  private int serverLowerHdlcAddress = 0x1826;
  private int logicalDeviceId = 0x01; //=upper HDLC address
  private int clientId = 0x10;
  private boolean modeE = false;
  private int clientMaxReceivePduSize = 0;
  //--- Security ---
  SecurityLevel securityLevel = SecurityLevel.LOWEST_LEVEL_SECURITY;
  private byte[] systemTitle;
  private byte[] encryptionKey;
  private byte[] authenticationKey;
  private String lowLevelSecurityKey;
  private final ProtocolStateObservableSupport openStateObservableSupport =
          new ProtocolStateObservableSupport(this);
  //--- For tests ---
  private IBaudrateSupport baudrateSupport = null;

  /**
   * Creates the stack
   *
   * @param in An open input stream (all data will be read thru this stream)
   * @param out An open output stream (all data will be written thru this stream)
   */
  public EictDlmsHdlcStack(InputStream in, OutputStream out)
  {
    this.in = in;
    this.out = out;
  }

  /**
   * Creates the stack
   *
   * @param in An open input stream (all data will be read thru this stream)
   * @param out An open output stream (all data will be written thru this stream)
   * @param baudrateSupport For controlling the baud rate (required for optical connections with baud rate switch)
   */
  public EictDlmsHdlcStack(InputStream in, OutputStream out, IBaudrateSupport baudrateSupport)
  {
    this.in = in;
    this.out = out;
    this.baudrateSupport = baudrateSupport;
  }

  /**
   * Opens the stack.<P>
   * An exception will be thrown if stack could not completely opened.<P>
   * If no exception was thrown the application association was successful.
   *
   *
   * @return The open response of the device.
   * @throws IOException
   */
  public OpenResponse open() throws IOException
  {
    openStateObservableSupport.setState(ProtocolState.OPENING, true);

    final IStreamProtocol streamProtocol = new StreamProtocol(in, out, false);

    if (modeE)
    {
      ProtocolOptical protocolOptical = new ProtocolOptical(streamProtocol, baudrateSupport);
      protocolOptical.setIgnoreModeAck(true);
      protocolOptical.open();
    }

    OpenResponse response = null;
    try
    {
      hdlcProtocol = new HdlcProtocol(streamProtocol);

      hdlcProtocol.setPollingIntervall(pollingInterval);
      hdlcProtocol.setResponseTimeout(responseTimeOut);

      hdlcProtocol.open();

      DlmsLlcHdlc dlmsLlc = new DlmsLlcHdlc(hdlcProtocol, serverLowerHdlcAddress);

      dlmsLlc.addProtocolStateListener(new IProtocolStateObserver()
      {
        //@Override
        public void openStateChanged(Object sender, ProtocolState oldState, ProtocolState newState)
        {
        }

        //@Override
        public void connectionBroken(Object sender, Object orign, Exception reason)
        {
          openStateObservableSupport.setState(ProtocolState.CLOSE, true);
          openStateObservableSupport.notifyConnectionBroken(orign, reason);
          cleanup();
        }

      });

      cosemApplicationLayer =
              new CosemApplicationLayer(dlmsLlc, clientId);
      cosemApplicationLayer.setClientMaxReceivePduSize(clientMaxReceivePduSize);

      switch (securityLevel)
      {
        case LOWEST_LEVEL_SECURITY:
        {
          response = cosemApplicationLayer.open(logicalDeviceId);
          break;
        }
        case LOW_LEVEL_SECURITY:
        {
          response = cosemApplicationLayer.open(logicalDeviceId, lowLevelSecurityKey);
          break;
        }
        case HIGH_LEVEL_SECURITY_USING_GMAC:
        {
          response = cosemApplicationLayer.openHls(logicalDeviceId, systemTitle, encryptionKey,
                                                   authenticationKey);
          break;
        }
      }

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
   * Closes the stack.
   *
   * @throws IOException
   */
  public void close() throws IOException
  {
    try
    {
      openStateObservableSupport.setState(ProtocolState.CLOSING, true);
      if (cosemApplicationLayer != null)
      {
        cosemApplicationLayer.close();
      }

      if (hdlcProtocol != null)
      {
        hdlcProtocol.close();
      }
    }
    finally
    {
      cleanup();
      openStateObservableSupport.setState(ProtocolState.CLOSE, true);

    }
  }

  /**
   * Returns the COSEM application layer for communicating with the device.
   *
   * @return The COSEM application layer.
   */
  public CosemApplicationLayer getCosemApplicationLayer()
  {
    return cosemApplicationLayer;
  }

  public int getServerLowerHdlcAddress()
  {
    return serverLowerHdlcAddress;
  }

  /**
   * The lower HDLC address of the device.
   *
   * @param lowerHdlcAddress The lower HDLC address of the device.
   */
  public void setServerLowerHdlcAddress(int lowerHdlcAddress)
  {
    this.serverLowerHdlcAddress = lowerHdlcAddress;
  }

  /**
   * The client id
   *
   * @return The client id
   */
  public int getClientId()
  {
    return clientId;
  }

  /**
   * The client id
   *
   * @param clientId The client id
   */
  public void setClientId(int clientId)
  {
    this.clientId = clientId;
  }

  public boolean isModeE()
  {
    return modeE;
  }

  /**
   * If true a mode E will be used.<P>
   * See {@link ProtocolOptical}
   *
   * @param modeE
   */
  public void setModeE(boolean modeE)
  {
    this.modeE = modeE;
  }

  //@Override
  public void addProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateObservableSupport.addProtocolStateListener(observer);
  }

  //@Override
  public void removeProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateObservableSupport.removeProtocolStateListener(observer);
  }

  //@Override
  public ProtocolState getProtocolState()
  {
    return openStateObservableSupport.getProtocolState();
  }

  /**
   * Returns true if the stack is open.
   *
   * @return {@code true} if the stack is open.
   */
  //@Override
  public boolean isOpen()
  {
    return openStateObservableSupport.isOpen();
  }

  /**
   * Closes all open Layers and "frees" the resources.<P>
   * Throws no exception.
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
      }
      cosemApplicationLayer = null;
    }

    if (hdlcProtocol != null)
    {
      try
      {
        hdlcProtocol.close();
      }
      catch (Exception e)
      {
      }
      hdlcProtocol = null;
    }
    openStateObservableSupport.setState(ProtocolState.CLOSE, true);

  }

  public int getPollingIntervall()
  {
    return pollingInterval;
  }

  /**
   * Set the HDLC polling interval.<P>
   * (RR polling)
   *
   * @param pollingInterval The polling interval in milliseconds.
   */
  public void setPollingIntervall(int pollingInterval)
  {
    this.pollingInterval = pollingInterval;
  }

  public int getResponseTimeOut()
  {
    return responseTimeOut;
  }

  /**
   * Sets the HDLC response timeout.
   *
   * @param responseTimeOut The HDLC response timeout in milliseconds.
   */
  public void setResponseTimeOut(int responseTimeOut)
  {
    this.responseTimeOut = responseTimeOut;
  }

  public byte[] getAuthenticationKey()
  {
    return authenticationKey == null ? null : authenticationKey.clone();
  }

  /**
   * Sets the authentication key for the high level security modes.
   *
   * @param authenticationKey The authentication key.
   */
  public void setAuthenticationKey(final byte[] authenticationKey)
  {
    this.authenticationKey = authenticationKey.clone();
  }

  public byte[] getEncryptionKey()
  {
    return encryptionKey == null ? null : encryptionKey.clone();
  }

  /**
   * Sets the encryption key for the high level security modes.
   *
   * @param encryptionKey
   */
  public void setEncryptionKey(final byte[] encryptionKey)
  {
    this.encryptionKey = encryptionKey.clone();
  }

  public int getLogicalDeviceId()
  {
    return logicalDeviceId;
  }

  /**
   * Sets the logical device of the server.<P>
   * This is the upper HDLC address.<P>
   * The default is 1 for the public client.
   *
   * @param logicalDeviceId The logical device id.
   */
  public void setLogicalDeviceId(int logicalDeviceId)
  {
    this.logicalDeviceId = logicalDeviceId;
  }

  public String getLowLevelSecurityKey()
  {
    return lowLevelSecurityKey;
  }

  /**
   * Sets the key for the low level security mode.
   *
   * @param lowLevelSecurityKey
   */
  public void setLowLevelSecurityKey(String lowLevelSecurityKey)
  {
    this.lowLevelSecurityKey = lowLevelSecurityKey;
  }

  public SecurityLevel getSecurityLevel()
  {
    return securityLevel;
  }

  /**
   * Sets the security level.
   *
   * @param securityLevel The security level.
   */
  public void setSecurityLevel(SecurityLevel securityLevel)
  {
    this.securityLevel = securityLevel;
  }

  public byte[] getSystemTitle()
  {
    return systemTitle == null ? null : systemTitle.clone();
  }

  /**
   * Sets the system title for the high level security modes.
   *
   * @param systemTitle
   */
  public void setSystemTitle(final byte[] systemTitle)
  {
    this.systemTitle = systemTitle.clone();
  }

  /**
   * Client maximum PDU size sent in InitiateRequest.
   *
   * @return The client maximum PDU size.
   */
  public int getClientMaxReceivePduSize()
  {
    return clientMaxReceivePduSize;
  }

  /**
   * Client maximum PDU size sent in InitiateRequest.
   *
   * @param clientMaxReceivePduSize The client maximum PDU size. (must be set before calling {@link #open()}
   */
  public void setClientMaxReceivePduSize(int clientMaxReceivePduSize)
  {
    this.clientMaxReceivePduSize = clientMaxReceivePduSize;
  }

}
