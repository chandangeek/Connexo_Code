/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/profiles/hdlc/DlmsHdlcStack.java $
 * Version:
 * $Id: DlmsHdlcStack.java 6756 2013-06-14 06:57:36Z osse $
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
import com.elster.protocols.comportdriver.ComPortDriver;
import com.elster.protocols.errortest.ErrorTestProtocol;
import com.elster.protocols.hdlc.HdlcProtocol;
import com.elster.protocols.logger.AnalyseTraceLogHandler;
import com.elster.protocols.logger.AsyncLogHandler;
import com.elster.protocols.logger.ILogHandler;
import com.elster.protocols.logger.LoggingProtocol;
import com.elster.protocols.modem.ModemProtocol;
import com.elster.protocols.optical.ProtocolOptical;
import com.elster.protocols.tcpip.TcpIpProtocol;
import com.elster.protocols.tcpip.TcpIpSingleListenerProtocol;
import java.io.IOException;
import java.util.Stack;

/**
 * HDLC Stack.<P> Complete DLMS stack using the HDLC profile and an RS232 port.
 *
 * @author osse
 */
public class DlmsHdlcStack implements IProtocolStateObservable
{
  public enum ConnectionMode
  {
    DIRECT, MODEM, TCPIP, TCPIP_LISTENER
  };

  private ConnectionMode connectionMode = ConnectionMode.DIRECT;
  private IStreamProtocol physicalLayer;
  private IStreamProtocol manualPhysicalLayer;
  private DlmsLlcHdlc dlmsLlc;
  private HdlcProtocol hdlcProtocol;
  private CosemApplicationLayer cosemApplicationLayer;
  private final Stack<IProtocol> protocols = new Stack<IProtocol>();
  private int serverLowerHdlcAddress = 0x1826;
  private int serverUpperHdlcAddress = 0x1;
  private int comPort = 1;
  private int baudRate = 19200;
  private int clientId = 0x10;
  private boolean modeE = false;
  private String telNumber = "";
  private int pollingIntervall = 8000;
  private int responseTimeOut = 1000;
  private int iFrameTimeOut = 6000;
  private ILogHandler comPortDriverLogger;
  private String logFile = null;
  private String tcpIpDestAddress = "localhost";
  private int tcpIpDestPort = 40000;
  private int clientMaxReceivePduSize = 65535;
  private int errorRateSend = 0;
  private int errorRateReceive = 0;
  private int errorDelaySend = 0;
  private final ProtocolStateObservableSupport openStateObservableSupport =
          new ProtocolStateObservableSupport(this);

  protected ComPortDriver openComPort() throws IOException
  {
    final ComPortDriver comPortDriver = new ComPortDriver(100);
    physicalLayer = comPortDriver;
    protocols.push(comPortDriver);
    comPortDriver.setFileLogger(comPortDriverLogger);
    comPortDriver.setComPort(comPort);
    comPortDriver.setBaudrate(baudRate);
    comPortDriver.open();
    return comPortDriver;
  }

  protected IStreamProtocol openModem() throws IOException
  {
    final ComPortDriver comPortDriver = openComPort();
    final ModemProtocol modemProtocol = new ModemProtocol(comPortDriver);
    modemProtocol.setNumber(telNumber);
    modemProtocol.open();
    return comPortDriver;
  }

  protected IStreamProtocol openTcpIP() throws IOException
  {
    final TcpIpProtocol tcpIpProtocol = new TcpIpProtocol(tcpIpDestAddress, tcpIpDestPort);
    protocols.push(tcpIpProtocol);
    physicalLayer = tcpIpProtocol;

    tcpIpProtocol.open();
    return tcpIpProtocol;
  }

  private IStreamProtocol openTcpIPListener() throws IOException
  {
    final TcpIpSingleListenerProtocol tcpIpProtocol = new TcpIpSingleListenerProtocol(tcpIpDestPort, 60*1000);
    protocols.push(tcpIpProtocol);
    physicalLayer = tcpIpProtocol;

    tcpIpProtocol.open();
    return tcpIpProtocol;
  }

  protected HdlcProtocol openHdlc() throws IOException
  {

    if (manualPhysicalLayer != null)
    {
      physicalLayer = manualPhysicalLayer;
    }
    else
    {
      switch (connectionMode)
      {
        case DIRECT:
          physicalLayer = openComPort();
          break;
        case MODEM:
          physicalLayer = openModem();
          break;
        case TCPIP:
          physicalLayer = openTcpIP();
          break;
        case TCPIP_LISTENER:
          physicalLayer = openTcpIPListener();
          break;
        default:
          throw new IllegalStateException("Unknown connection mode");
      }
    }

    IBaudrateSupport baudrateSupport = null;
    if (physicalLayer instanceof IBaudrateSupport)
    {
      baudrateSupport = (IBaudrateSupport)physicalLayer;
    }


    if (modeE)
    {
      ProtocolOptical protocolOptical = new ProtocolOptical(physicalLayer, baudrateSupport);
      if (connectionMode == ConnectionMode.MODEM)
      {
        protocolOptical.setStartBaudrate(baudRate);
        protocolOptical.setNoBaudrateSwitch(true);
      }
      protocolOptical.setIgnoreModeAck(true);
      protocolOptical.open();
    }

    if (errorRateReceive > 0 || errorRateSend > 0 || errorDelaySend > 0)
    {
      physicalLayer = new ErrorTestProtocol(physicalLayer, errorRateReceive, errorRateSend, errorDelaySend);
    }


    if (logFile
        != null && logFile.length() > 0)
    {
      LoggingProtocol loggingLayer = new LoggingProtocol(physicalLayer,
                                                         new AsyncLogHandler(new AnalyseTraceLogHandler(
              logFile)));
      protocols.push(loggingLayer);
      hdlcProtocol = new HdlcProtocol(loggingLayer);
      protocols.push(hdlcProtocol);
    }
    else
    {
      hdlcProtocol = new HdlcProtocol(physicalLayer);
      protocols.push(hdlcProtocol);
    }


//      if (useModem)
//      {
//        hdlcProtocol.setResponseTimeout(8000);
//      }
    hdlcProtocol.setPollingIntervall(pollingIntervall);
    hdlcProtocol.setResponseTimeout(responseTimeOut);
    hdlcProtocol.open();

    return hdlcProtocol;
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
  public OpenResponse open(OpenRequest openRequest) throws IOException
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

  public OpenResponse openHls(byte[] systemTitle, byte[] encryptionKey, byte[] authenticationKey) throws
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
    openStateObservableSupport.setState(ProtocolState.CLOSE, true);
  }

  /**
   * Closes all Layers and "frees" the resources.<P> Throws no exception.
   */
  public void cleanup()
  {
    physicalLayer = null;
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
    openStateObservableSupport.setState(ProtocolState.CLOSE, true);
  }

  /**
   * Closes the stack without sending anything.<P> Normally a HDLC DISC frame will be sent.
   */
  public void abort()
  {
    if (physicalLayer != null)
    {
      try
      {
        physicalLayer.close();
      }
      catch (Exception e)
      {
      }
    }
    cleanup();
  }

  public IStreamProtocol getManualPhysicalLayer()
  {
    return manualPhysicalLayer;
  }

  public void setManualPhysicalLayer(IStreamProtocol manualPhysicalLayer)
  {
    this.manualPhysicalLayer = manualPhysicalLayer;
  }

//  public HdlcProtocol getHdlcProtocol()
//  {
//    return hdlcProtocol;
//  }
  public CosemApplicationLayer getCosemApplicationLayer()
  {
    return cosemApplicationLayer;
  }

  public int getBaudRate()
  {
    return baudRate;
  }

  public void setBaudRate(int baudRate)
  {
    this.baudRate = baudRate;
  }

  public int getComPort()
  {
    return comPort;
  }

  public void setComPort(int comPort)
  {
    this.comPort = comPort;
  }

  public int getServerLowerHdlcAddress()
  {
    return serverLowerHdlcAddress;
  }

  public void setServerLowerHdlcAddress(int lowerHdlcAddress)
  {
    this.serverLowerHdlcAddress = lowerHdlcAddress;
  }

  public int getClientId()
  {
    return clientId;
  }

  public void setClientId(int clientId)
  {
    this.clientId = clientId;
  }

  public boolean isModeE()
  {
    return modeE;
  }

  public void setModeE(boolean modeE)
  {
    this.modeE = modeE;
  }

  public String getTelNumber()
  {
    return telNumber;
  }

  public void setTelNumber(String telNumber)
  {
    this.telNumber = telNumber;
  }

  public void addProtocolStateListener(IProtocolStateObserver observer)
  {
    openStateObservableSupport.addProtocolStateListener(observer);
  }

  public void removeProtocolStateListener(IProtocolStateObserver observer)
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

  public void setPollingIntervall(int pollingIntervall)
  {
    this.pollingIntervall = pollingIntervall;
  }

//  public DlmsLlcHdlc getDlmsLlc()
//  {
//    return dlmsLlc;
//  }
  public int getResponseTimeOut()
  {
    return responseTimeOut;
  }

  public void setResponseTimeOut(int responseTimeOut)
  {
    this.responseTimeOut = responseTimeOut;
  }

  public ILogHandler getComPortDriverLogger()
  {
    return comPortDriverLogger;
  }

  public void setComPortDriverLogger(ILogHandler comPortDriverLogger)
  {
    this.comPortDriverLogger = comPortDriverLogger;
  }

  public String getLogFile()
  {
    return logFile;
  }

  public void setLogFile(String logFile)
  {
    this.logFile = logFile;
  }

  public int getIFrameTimeOut()
  {
    return iFrameTimeOut;
  }

  public void setIFrameTimeOut(int iFrameTimeOut)
  {
    this.iFrameTimeOut = iFrameTimeOut;
  }

  public ConnectionMode getConnectionMode()
  {
    return connectionMode;
  }

  public void setConnectionMode(ConnectionMode connectionMode)
  {
    this.connectionMode = connectionMode;
  }

  public String getTcpIpDestAddress()
  {
    return tcpIpDestAddress;
  }

  public void setTcpIpDestAddress(String tcpIpDestAddress)
  {
    this.tcpIpDestAddress = tcpIpDestAddress;
  }

  public int getTcpIpDestPort()
  {
    return tcpIpDestPort;
  }

  public void setTcpIpDestPort(int tcpIpDestPort)
  {
    this.tcpIpDestPort = tcpIpDestPort;
  }

  public IStreamProtocol getPhysicalLayer()
  {
    return physicalLayer;
  }

  public DlmsLlcHdlc getDlmsLlc()
  {
    return dlmsLlc;
  }

  public int getErrorRateReceive()
  {
    return errorRateReceive;
  }

  public void setErrorRateReceive(int errorRateReceive)
  {
    this.errorRateReceive = errorRateReceive;
  }

  public int getErrorRateSend()
  {
    return errorRateSend;
  }

  public void setErrorRateSend(int errorRateSend)
  {
    this.errorRateSend = errorRateSend;
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

  public int getErrorDelaySend()
  {
    return errorDelaySend;
  }

  public void setErrorDelaySend(int errorDelaySend)
  {
    this.errorDelaySend = errorDelaySend;
  }

  public void setClientMaxReceivePduSize(int maxPduSize)
  {
    this.clientMaxReceivePduSize = maxPduSize;
  }

  public int getClientMaxReceivePduSize()
  {
    return clientMaxReceivePduSize;
  }

}
