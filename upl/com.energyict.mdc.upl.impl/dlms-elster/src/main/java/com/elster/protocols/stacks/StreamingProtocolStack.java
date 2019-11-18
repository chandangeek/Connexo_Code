/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/stacks/StreamingProtocolStack.java $
 * Version:     
 * $Id: StreamingProtocolStack.java 6772 2013-06-14 15:12:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  26.07.2012 09:53:49
 */
package com.elster.protocols.stacks;

import com.elster.protocols.IBaudrateSupport;
import com.elster.protocols.IProtocol;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.bluetooth.ProtocolBluetooth;
import com.elster.protocols.comportdriver.ComPortDriver;
import com.elster.protocols.logger.ILogHandler;
import com.elster.protocols.logger.LoggingProtocol;
import com.elster.protocols.modem.ModemProtocol;
import com.elster.protocols.optical.ProtocolOptical;
import com.elster.protocols.tcpip.TcpIpProtocol;
import com.elster.protocols.tcpip.TcpIpSingleListenerProtocol;
import java.io.IOException;
import java.util.Stack;

/**
 * This class ...
 *
 * @author osse
 */
public class StreamingProtocolStack extends ProtocolStack
{
  private final IStreamProtocol topLayer;
  private final IBaudrateSupport baudrateSupport;

  public StreamingProtocolStack(final Stack<IStreamProtocol> protocolLayers,
                                final IBaudrateSupport baudrateSupport)
  {
    super(protocolLayers);
    topLayer = protocolLayers.peek();
    this.baudrateSupport = baudrateSupport;
  }

  public IStreamProtocol getStreamProtocol()
  {
    return topLayer;
  }

  public IBaudrateSupport getBaudrateSupport()
  {
    return baudrateSupport;
  }

  public static abstract class AbstractBuilder
  {
    private ILogHandler logHandler = null;
    private boolean useModeE = false;
    private int modeEMinBaudRate = 0;
    private int modeEMaxBaudRate = 0;
    private final Stack<IStreamProtocol> protocolLayers = new Stack<IStreamProtocol>();

    protected void pushLayer(final IStreamProtocol layer)
    {
      if (protocolLayers.isEmpty() || protocolLayers.peek() != layer)
      {
        protocolLayers.push(layer);
      }
    }

    public AbstractBuilder()
    {
    }

    public AbstractBuilder(boolean useModeE)
    {
      this.useModeE = useModeE;
    }

    public ILogHandler getLogHandler()
    {
      return logHandler;
    }

    public void setLogHandler(ILogHandler logHandler)
    {
      this.logHandler = logHandler;
    }

    public boolean isUseModeE()
    {
      return useModeE;
    }

    public void setUseModeE(boolean useModeE)
    {
      this.useModeE = useModeE;
    }

    public IBaudrateSupport findBaudrateSupport()
    {
      IBaudrateSupport result = null;
      for (IProtocol prot : protocolLayers)
      {
        if (prot instanceof IBaudrateSupport)
        {
          result = (IBaudrateSupport)prot; //no break to find the highest layer with baudrate support.
        }
      }
      return result;
    }

    protected IStreamProtocol extendPhysicalLayer(IStreamProtocol physicalLayer) throws IOException
    {
      IStreamProtocol topLayer = physicalLayer;
      if (logHandler != null)
      {
        topLayer = new LoggingProtocol(topLayer, logHandler);
        pushLayer(topLayer);
      }
      return topLayer;
    }

    protected IStreamProtocol addModeELayer(IStreamProtocol sublayer) throws
            IOException
    {
      if (isUseModeE())
      {
        ProtocolOptical optical = new ProtocolOptical(sublayer, findBaudrateSupport());
        optical.setModeToReach(ProtocolOptical.Mode.MODE_E);
        if (modeEMinBaudRate > 0)
        {
          optical.setMinBaudrate(ProtocolOptical.IEC1107Baudrate.findByBaudrate(modeEMinBaudRate));
        }
        if (modeEMaxBaudRate > 0)
        {
          optical.setMaxBaudrate(ProtocolOptical.IEC1107Baudrate.findByBaudrate(modeEMaxBaudRate));
        }
        optical.open();
      }
      return sublayer;
    }

    public void cleanup()
    {
      while (!protocolLayers.isEmpty())
      {
        try
        {
          protocolLayers.pop().close();
        }
        catch (Exception ignore)
        {
        }
      }
    }

    public StreamingProtocolStack openStack() throws IOException
    {
      try
      {
        IStreamProtocol topLevelProtocol = buildPhysicalLayer();
        pushLayer(topLevelProtocol);
        topLevelProtocol = extendPhysicalLayer(topLevelProtocol);

        topLevelProtocol = buildAdditionalLayer(topLevelProtocol);
        pushLayer(topLevelProtocol);


        addModeELayer(topLevelProtocol);

      }
      catch (IOException ex)
      {
        cleanup();
        throw ex;
      }
      catch (RuntimeException ex)
      {
        cleanup();
        throw ex;
      }

      return new StreamingProtocolStack(protocolLayers, findBaudrateSupport());
    }

    protected abstract IStreamProtocol buildPhysicalLayer() throws IOException;

    protected IStreamProtocol buildAdditionalLayer(IStreamProtocol sublayer) throws IOException
    {
      return sublayer;
    }

  }

  public static class SerialStackBuilder extends AbstractBuilder
  {
    private final int comPort;
    private final int baudrate;
    private boolean useBluetooth = false;

    public SerialStackBuilder(final int comPort, final int baudrate)
    {
      super(false);
      this.comPort = comPort;
      this.baudrate = baudrate;
    }

    @Override
    protected IStreamProtocol buildPhysicalLayer() throws IOException
    {
      final ComPortDriver comPortDriver = new ComPortDriver(100);
      comPortDriver.setBaudrate(useBluetooth ? 19200 : baudrate);
      comPortDriver.setComPort(comPort);
      comPortDriver.open();

      if (!useBluetooth)
      {
        return comPortDriver;
      }

      pushLayer(comPortDriver);

      final ProtocolBluetooth bt = new ProtocolBluetooth(comPortDriver);
      bt.open();
      bt.setBaudrate(baudrate);
      return bt;

    }

    public boolean isUseBluetooth()
    {
      return useBluetooth;
    }

    public SerialStackBuilder setUseBluetooth(boolean useBluetooth)
    {
      this.useBluetooth = useBluetooth;
      return this;
    }

  }

  public static class TcpIpStackBuilder extends AbstractBuilder
  {
    private final String host;
    private final int port;

    public TcpIpStackBuilder(String host, int port)
    {
      super(false);
      this.host = host;
      this.port = port;
    }

    @Override
    protected IStreamProtocol buildPhysicalLayer() throws IOException
    {
      final TcpIpProtocol tcpIpProtocol = new TcpIpProtocol(host, port);
      tcpIpProtocol.open();
      return tcpIpProtocol;
    }

  }

  public static class TcpIpListenerStackBuilder extends AbstractBuilder
  {
    private final int port;
    private final int timeoutMs;

    public TcpIpListenerStackBuilder(final int port, final int timeoutMs)
    {
      super(false);
      this.port = port;
      this.timeoutMs = timeoutMs;
    }

    @Override
    protected IStreamProtocol buildPhysicalLayer() throws IOException
    {
      final TcpIpSingleListenerProtocol tcpIpListener = new TcpIpSingleListenerProtocol(port, timeoutMs);
      tcpIpListener.open();
      return tcpIpListener;
    }

  }

  public static class ModemStackBuilder extends AbstractBuilder
  {
    private final int comPort;
    private final int baudrate;
    private final String dialString;
    private final String modemIni;

    public ModemStackBuilder(int comPort, int baudrate, String modemIni, String dialString)
    {
      this.comPort = comPort;
      this.baudrate = baudrate;
      this.modemIni = modemIni;
      this.dialString = dialString;
    }

    @Override
    protected IStreamProtocol buildPhysicalLayer() throws IOException
    {

      final ComPortDriver comPortDriver = new ComPortDriver(100);
      comPortDriver.setBaudrate(baudrate);
      comPortDriver.setComPort(comPort);
      comPortDriver.open();
      return comPortDriver;
    }

    @Override
    protected IStreamProtocol buildAdditionalLayer(IStreamProtocol sublayer) throws IOException
    {
      final ModemProtocol modemProtocol = new ModemProtocol(sublayer);
      modemProtocol.setNumber(dialString);
      if (modemIni.toLowerCase().startsWith("at"))
      {
        modemProtocol.setModemInit(modemIni);
      }
      else
      {
        modemProtocol.setModemInit("AT" + modemIni);
      }

      modemProtocol.open();
      return modemProtocol;
    }

    @Override
    public IBaudrateSupport findBaudrateSupport()
    {
      return null; //Baurate switching not allowed.
    }

  }

}
