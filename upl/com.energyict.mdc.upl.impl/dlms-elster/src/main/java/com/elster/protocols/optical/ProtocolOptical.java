/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/optical/ProtocolOptical.java $
 * Version:     
 * $Id: ProtocolOptical.java 6101 2013-02-19 10:45:59Z SchulteM $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.04.2010 13:16:54
 */
package com.elster.protocols.optical;

import com.elster.protocols.IBaudrateSupport;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.dataformat.ProtocolDataFormat;
import com.elster.protocols.streams.TimeoutIOException;
import com.elster.protocols.streams.TimeoutInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Protocol to reach the protocol Mode E or the "Programming Mode" for direct local data exchange.
 * 
 * (see GB 7th Ed. page 42) <br>
 *
 * @author osse
 */
public class ProtocolOptical
{
  public enum Mode
  {
    MODE_E, PROGRAMMING, READOUT
  };

  public enum IEC1107Baudrate
  {
    BR_300('0', 300),
    BR_600('1', 600),
    BR_1200('2', 1200),
    BR_2400('3', 2400),
    BR_4800('4', 4800),
    BR_9600('5', 9600),
    BR_19200('6', 19200);

    private IEC1107Baudrate(final char baudrateChar,final  int baudrate)
    {
      this.baudrateChar = baudrateChar;
      this.baudrate = baudrate;
    }

    private final char baudrateChar;
    private final int baudrate;

    public int getBaudrate()
    {
      return baudrate;
    }

    public char getBaudrateChar()
    {
      return baudrateChar;
    }

    public boolean isLowerThan(final IEC1107Baudrate other)
    {
      return baudrate < other.baudrate;
    }

    public boolean isHigherThan(final IEC1107Baudrate other)
    {
      return baudrate > other.baudrate;
    }

    public static IEC1107Baudrate findByChar(final char c)
    {
      for (IEC1107Baudrate br : values())
      {
        if (br.getBaudrateChar() == c)
        {
          return br;
        }
      }
      return null;
    }

    public static IEC1107Baudrate findByBaudrate(final int baudrate)
    {
      for (IEC1107Baudrate br : values())
      {
        if (br.getBaudrate() == baudrate)
        {
          return br;
        }
      }
      return null;
    }

  };

  private final IBaudrateSupport baudRateControl;
  private final IStreamProtocol sublayer;
  private IStreamProtocol topLayer;
  private int startBaudrate = 300;
  private boolean noBaudrateSwitch = false;
  private boolean ignoreModeAck = false;
  
  private boolean withParity= true;
  private TimeoutInputStream topInputStream;
  
  private int timeoutModeEAck= 3000;
  private int timeoutAfterRequest= 8000;
  
  private String deviceAddress="";
  
  private Mode modeToReach = Mode.MODE_E;
  private IEC1107Baudrate minBaudrate = null;
  private IEC1107Baudrate maxBaudrate = null;
  private IEC1107Baudrate reachedBaudrate = null;
  //private IEC1107Baudrate restrictBaudrate = null;
  private DeviceIdentification deviceIdentification;
  private final static String ACK = "\006";
  
  

  /**
   * Creates the protocol with the specified sub layer.<P>
   * The sub layer should implement {@link IBaudrateSupport }, to support baud rate changes.
   *
   * @param sublayer The sub layer.
   */
  public ProtocolOptical(final IStreamProtocol sublayer)
  {
    this(sublayer, sublayer instanceof IBaudrateSupport?(IBaudrateSupport) sublayer: null );
  }

  /**
   * Creates the protocol with the specified sub layer.<P>
   *
   * @param sublayer The sub layer.
   */
  public ProtocolOptical(final IStreamProtocol sublayer, final IBaudrateSupport baudRateControl)
  {
    this.sublayer = sublayer;
    this.baudRateControl = baudRateControl;
  }

  /**
   * Opens the protocol.
   * <P>
   * If the sub layer is not open the sub layer will be also opened.
   *
   * @throws IOException
   */
  public void open() throws IOException
  {
    if (!sublayer.isOpen())
    {
      sublayer.open();
    }
    
    if (withParity)
    {
      topLayer = new ProtocolDataFormat(sublayer);
    }
    else
    {
      topLayer= sublayer;
    }
    topInputStream = new TimeoutInputStream(topLayer.getInputStream());
    
    if (baudRateControl != null && !noBaudrateSwitch)
    {
      baudRateControl.setBaudrate(startBaudrate);
    }

    sendZeros();
    sleep(1600);
    clearInputStream();
    sendRequestMessage();
    deviceIdentification = receiveIdentification();
    reachedBaudrate = switchMode(deviceIdentification);
    if (modeToReach.equals(Mode.MODE_E))
    {
      receiveModeEAck(reachedBaudrate);
    }
  }

  /**
   * Returns the identification from the device identification string.
   *
   * @return the device identification.
   */
  public DeviceIdentification getDeviceIdentification()
  {
    return deviceIdentification;
  }

  private void clearInputStream() throws IOException
  {
    sublayer.getInputStream().skip(sublayer.getInputStream().available());
  }

  private void sendZeros() throws IOException
  {
    final long end = 2100 + System.currentTimeMillis();
    topLayer.getOutputStream().write(0);
    while (end > System.currentTimeMillis())
    {
      sleep(50);
      topLayer.getOutputStream().write(0);
    }
  }

  private void sendRequestMessage() throws IOException
  {
    topLayer.getOutputStream().write(("/?"+deviceAddress+"!\r\n").getBytes());
  }

  private String receiveString(final int timeout, final byte endChar, final int additionalChars) throws
          IOException
  {
    StringBuilder result = new StringBuilder();

    byte[] buffer = new byte[64];
    int b;
    int index = -1;

    do
    {

      b = topInputStream.readTO(timeout);
      if (b >= 0)
      {
        index++;
        if (index >= buffer.length)
        {
          result.append(new String(buffer));
          index = 0;
        }
        buffer[index] = (byte)b;
      }
    }
    while (b >= 0 && b != endChar);


    if (b >= 0)
    {
      for (int i = 0; i < additionalChars; i++)
      {
        b = topInputStream.readTO(timeout);
        if (b < 0)
        {
          break;
        }
      }
    }

    if (index >= 0)
    {
      result.append(new String(buffer, 0, index + 1));
    }

    return result.toString();
  }

  public boolean isWithParity()
  {
    return withParity;
  }

  public void setWithParity(final boolean withParity)
  {
    this.withParity = withParity;
  }

  public int getTimeoutModeEAck()
  {
    return timeoutModeEAck;
  }

  public void setTimeoutModeEAck(final int timeoutModeEAck)
  {
    this.timeoutModeEAck = timeoutModeEAck;
  }

  public int getTimeoutAfterRequest()
  {
    return timeoutAfterRequest;
  }

  public void setTimeoutAfterRequest(final int timeoutAfterRequest)
  {
    this.timeoutAfterRequest = timeoutAfterRequest;
  }

  public String getDeviceAddress()
  {
    return deviceAddress;
  }

  public void setDeviceAddress(String deviceAddress)
  {
    this.deviceAddress = deviceAddress;
  }
  
  
  private DeviceIdentification receiveIdentification() throws IOException
  {
    final String ident = receiveString(timeoutAfterRequest, (byte)10, 0);
    DeviceIdentification.checkIdentification(ident, modeToReach);
    return new DeviceIdentification(ident);
  }

  private IEC1107Baudrate switchMode(final DeviceIdentification identification) throws IOException
  {

    IEC1107Baudrate iec1107Baudrate = identification.getIec1107Baudrate();

    if (minBaudrate != null && iec1107Baudrate.isLowerThan(minBaudrate))
    {
      iec1107Baudrate = minBaudrate;
    }

    if (maxBaudrate != null && iec1107Baudrate.isHigherThan(maxBaudrate))
    {
      iec1107Baudrate = maxBaudrate;
    }

    String baudrateCommand;
    switch (modeToReach)
    {
      case MODE_E:
        baudrateCommand = ACK + "2" + iec1107Baudrate.getBaudrateChar() + "2\r\n";
        break;
      case PROGRAMMING:
        baudrateCommand = ACK + "0" + iec1107Baudrate.getBaudrateChar() + "1\r\n";
        break;
      case READOUT:
        baudrateCommand = ACK + "0" + iec1107Baudrate.getBaudrateChar() + "0\r\n";
        break;
      default:
        throw new IllegalStateException("Unexpected mode to reach");
    }

    topLayer.getOutputStream().write(baudrateCommand.getBytes());

    if (baudRateControl != null && !noBaudrateSwitch)
    {
      sleep(50 + 35 * 6);
      baudRateControl.setBaudrate(iec1107Baudrate.getBaudrate());
    }

    return iec1107Baudrate;
  }

  private void sleep(final int millisecs) throws InterruptedIOException
  {
    try
    {
      Thread.sleep(millisecs);
    }
    catch (InterruptedException ex)
    {
      throw new InterruptedIOException();
    }
  }

  private void receiveModeEAck(final IEC1107Baudrate identification) throws IOException
  {
    try
    {
      final String modeAck = receiveString(timeoutModeEAck, (byte)10, 0);

      if (modeAck.length() != 6)
      {
        throw new IOException("Switch to mode E failed (wrong length of mode ACK answer): ");
      }

      if (!modeAck.equals(ACK + "2" + identification.getBaudrateChar() + "2\r\n"))
      {
        throw new IOException("Switch to mode E failed (wrong mode ACK answer)");
      }
    }
    catch (TimeoutIOException te)
    {
      if (!ignoreModeAck)
      {
        throw te;
      }
    }

  }

  public int getStartBaudrate()
  {
    return startBaudrate;
  }

  public void setStartBaudrate(final int startBaudrate)
  {
    this.startBaudrate = startBaudrate;
  }

  public boolean isNoBaudrateSwitch()
  {
    return noBaudrateSwitch;
  }

  public void setNoBaudrateSwitch(final boolean noBaudrateSwitch)
  {
    this.noBaudrateSwitch = noBaudrateSwitch;
  }

  public boolean isIgnoreModeAck()
  {
    return ignoreModeAck;
  }

  public void setIgnoreModeAck(final boolean ignoreModeAck)
  {
    this.ignoreModeAck = ignoreModeAck;
  }

  public Mode getModeToReach()
  {
    return modeToReach;
  }

  public void setModeToReach(final Mode modeToReach)
  {
    this.modeToReach = modeToReach;
  }

  /**
   * See {@link #setMinBaudrate(com.elster.protocols.optical.ProtocolOptical.IEC1107Baudrate)}
   */
  public IEC1107Baudrate getMinBaudrate()
  {
    return minBaudrate;
  }

  /**
   * Forces a minimum baud rate.<P>
   * If the proposed baud rate is lower than this baud rate the switch mode command will use this (higher) baud rate.<br>
   * This behavior is not supported by the IEC1107 norm.<br>
   * If the {@link #getMaxBaudrate()} is lower than this baud rate, this baud rate has no effect.
   * 
   * @param minBaudrate The minimum baud rate to force or {@code null} if no baud rate should be forced.
   */
  public void setMinBaudrate(final IEC1107Baudrate minBaudrate)
  {
    this.minBaudrate = minBaudrate;
  }

  /**
   * See {@link #setMaxBaudrate(com.elster.protocols.optical.ProtocolOptical.IEC1107Baudrate)}
   */
  public IEC1107Baudrate getMaxBaudrate()
  {
    return maxBaudrate;
  }

  /**
   * Restricts the maximum baud rate.<P>
   * 
   * @param maxBaudrate The maximum baud rate or {@code null} if the baud rate should not be restricted.
   */
  public void setMaxBaudrate(final IEC1107Baudrate maxBaudrate)
  {
    this.maxBaudrate = maxBaudrate;
  }

  public IEC1107Baudrate getReachedBaudrate()
  {
    return reachedBaudrate;
  }

  /**
   * Class for storing the device identification.
   */
  public static class DeviceIdentification
  {
    private final IEC1107Baudrate iec1107Baudrate;
    private final String manufacturer;
    private final String ident;
    private final String indetString;

    /**
     * Checks the identification string of a device.<P>
     * Throws an IOException if the identification string has an wrong format.
     *
     * @param identification The identification string to check.
     * @throws IOException
     */
    public static void checkIdentification(final String identification, final Mode modeToReach) throws
            IOException
    {
      if ((modeToReach.equals(Mode.MODE_E) && identification.length() < 9)
          || identification.length() < 7)
      {
        throw new IOException("Identification message to short: " + identification);
      }

      if (identification.charAt(0) != '/')
      {
        throw new IOException("Error in identification message (Identification does not start with \"/\"): "
                              + identification);
      }

      if (null == IEC1107Baudrate.findByChar(identification.charAt(4)))
      {
        throw new IOException("Error in identification message (Unknown baudrate ID): " + identification);
      }

      if (modeToReach.equals(Mode.MODE_E))
      {
        if (identification.charAt(5) != '\\' || identification.charAt(6) != '2')
        {
          throw new IOException("Error in identification message (\"\\2\" missing after baudrate ID- required for Mode E): "
                                + identification);
        }
      }


      if (!identification.endsWith("\r\n"))
      {
        throw new IOException("Error in identification message (Message does not end with CR NL): "
                              + identification);
      }
    }

    /**
     * Creates the DeviceIdentification.<P>
     * The identification should be checked before using this constructor.
     *
     * @param deviceResponse The identification as received from the device.
     */
    public DeviceIdentification(final String deviceResponse)
    {
      indetString = deviceResponse;
      manufacturer = indetString.substring(1, 4);
      iec1107Baudrate = IEC1107Baudrate.findByChar(indetString.charAt(4));
      ident = indetString.substring(7).trim();
    }

    /**
     * The (interpreted) baud rate.
     *
     * @return The baud rate.
     */
    public int getBaudrate()
    {
      return iec1107Baudrate.getBaudrate();
    }

    /**
     * The baud rate character.
     *
     * @return The baud rate character.
     */
    public Character getBaudrateChar()
    {
      return iec1107Baudrate.getBaudrateChar();
    }

    /**
     * The ident part of the identification string.<P>
     *
     * @return The ident part.
     */
    public String getIdent()
    {
      return ident;
    }

    /**
     * The manufacturer code of the device.
     *
     * @return The manufacturer code.
     */
    public String getManufacturer()
    {
      return manufacturer;
    }

    public IEC1107Baudrate getIec1107Baudrate()
    {
      return iec1107Baudrate;
    }

    @Override
    public String toString()
    {
      return "DeviceIdentification{" + "baudrate=" + iec1107Baudrate + ", manufacturer=" + manufacturer
             + ", ident="
             + ident + '}';
    }

  }

}
