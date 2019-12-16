/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcAddress.java $
 * Version:     
 * $Id: HdlcAddress.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 17:46:12
 */
package com.elster.protocols.hdlc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class represents a address for the HDLC protocol using with DLMS
 *  <P>
 *  (See 8.4.2.2 of the DLMS Green Book 7th edition.)
 *
 * @author osse
 */
public class HdlcAddress
{
  private int upperHdlcAddress;
  private int lowerHdlcAddress;
  private int addressLength;

  /**
   * Creates a new HDLC address.
   *
   * @param upperHdlcAddress the upper hdlc address
   * @param lowerHdlcAddress the lower hdlc address
   * @param addressLength
   * The encoded length of the address. Valid values are 1,2 and 4. By an value of 1
   * the lower hdlc address will be ignored (and must be set to 0)
   *
   */
  public HdlcAddress(int upperHdlcAddress, int lowerHdlcAddress, int addressLength)
  {
    this.upperHdlcAddress = upperHdlcAddress;
    this.lowerHdlcAddress = lowerHdlcAddress;
    this.addressLength = addressLength;

    String message = checkAddress();
    if (message != null)
    {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Creates an HDLC Address with an address length of 1. <P>
   * The lower HDLC address is ignored by an address length of 1.
   *
   * @param upperHdlcAddress
   */
  public HdlcAddress(int upperHdlcAddress)
  {
    this.upperHdlcAddress = upperHdlcAddress;
    this.lowerHdlcAddress = 0;
    this.addressLength = 1;

    String message = checkAddress();
    if (message != null)
    {
      throw new IllegalArgumentException(message);
    }

  }

  /*
   * Creates an new empty HdlcAddress.
   *
   */
  public HdlcAddress()
  {
  }

  /**
   * Decode one HDLC Address from an input stream.
   *
   * @param inputStream The input stream.
   * @return The count of bytes read.
   * @throws IOException
   */
  public int decode(InputStream inputStream) throws IOException
  {
    int bytes[] = new int[4];
    int bytesRead = 0;
    int lastByte = 0;

    do
    {
      if (bytesRead >= 4)
      {
        throw new HdlcDecodingIOException("Unexpected address length");
      }

      lastByte = inputStream.read();
      bytes[bytesRead] = lastByte;
      bytesRead++;

    }
    while (0 == (lastByte & 0x01));

    addressLength = bytesRead;

    switch (addressLength)
    {
      case 1:
        upperHdlcAddress = bytes[0] >> 1;
        lowerHdlcAddress = 0;
        break;
      case 2:
        upperHdlcAddress = bytes[0] >> 1;
        lowerHdlcAddress = bytes[1] >> 1;
        break;
      case 4:
        upperHdlcAddress = ((bytes[0] >> 1) << 7) | bytes[1] >> 1;
        lowerHdlcAddress = ((bytes[2] >> 1) << 7) | bytes[3] >> 1;
        break;
      default:
        throw new HdlcDecodingIOException("Unexpected address length");
    }
    return bytesRead;
  }

  /**
   * Encodes this HDLC address to an output stream.
   * <P>
   * The address must be in an proper form at this moment.
   *
   * @param outputStream The output stream to write this address.
   * @throws IOException
   */
  public void encode(OutputStream outputStream) throws IOException
  {

    String message = checkAddress();
    if (message != null)
    {
      throw new IOException(message);
    }

    switch (addressLength)
    {
      case 1:
        outputStream.write((upperHdlcAddress << 1) | 0x01);
        break;
      case 2:
        outputStream.write((upperHdlcAddress << 1));
        outputStream.write((lowerHdlcAddress << 1) | 0x01);
        break;
      case 4:
        outputStream.write((((upperHdlcAddress >> 7) & 0x7F) << 1));
        outputStream.write((((upperHdlcAddress) & 0x7F) << 1));
        outputStream.write((((lowerHdlcAddress >> 7) & 0x7F) << 1));
        outputStream.write((((lowerHdlcAddress) & 0x7F) << 1) | 0x01);
        break;
    }
  }

  /**
   * Returns the address length in bytes.
   *
   * @return The address length.
   */
  public int getAddressLength()
  {
    return addressLength;
  }

  /**
   * Sets the address length.
   * <P>
   * Valid address lengths are 1,2 and 4.
   *
   * @param addressLength
   */
  public void setAddressLength(int addressLength)
  {
    this.addressLength = addressLength;
  }

  /**
   * Returns the lower HDLC address.
   * @return The lower HDLC address.
   */
  public int getLowerHdlcAddress()
  {
    return lowerHdlcAddress;
  }

  /**
   * Sets the lower HDLC address.<P>
   * If an address length of 1 is used, the lower HDLC address must be 0.
   * 
   * @param lowerHdlcAddress The lower HDLC address.
   */
  public void setLowerHdlcAddress(int lowerHdlcAddress)
  {
    this.lowerHdlcAddress = lowerHdlcAddress;
  }

  /**
   * Returns the upper HDLC address.
   *
   * @return The upper HDLC address.
   */
  public int getUpperHdlcAddress()
  {
    return upperHdlcAddress;
  }

  /**
   * Sets the upper HDLC address.
   *
   * @param upperHdlcAddress
   */
  public void setUpperHdlcAddress(int upperHdlcAddress)
  {
    this.upperHdlcAddress = upperHdlcAddress;
  }

  @Override
  public String toString()
  {
    String result = "";
    switch (addressLength)
    {
      case 1:
        result = "upper:" + upperHdlcAddress;
        break;
      default:
        result = "upper:" + upperHdlcAddress + " lower:" + lowerHdlcAddress;
    }
    return result + " length:" + addressLength + " Bytes";
  }

  private String checkAddress()
  {
    String message = null;

    switch (addressLength)
    {
      case 1:
        if (lowerHdlcAddress != 0)
        {
          message = "The lower HDLC address must be 0 in the combination with an address lentgh of 1";
        }
        if (upperHdlcAddress < 0 || upperHdlcAddress > 255)
        {
          message = "The upper HDLC address must be between 0 and 255 (including)";
        }
        break;
      case 2:
        if (lowerHdlcAddress < 0 || lowerHdlcAddress > 255)
        {
          message = "The lower HDLC address must be between 0 and 255 (including)";
        }
        if (upperHdlcAddress < 0 || upperHdlcAddress > 255)
        {
          message = "The upper HDLC address must be between 0 and 255 (including)";
        }
        break;
      case 4:
        if (lowerHdlcAddress < 0 || lowerHdlcAddress > 65535)
        {
          message = "The lower HDLC address must be between 0 and 65535 (including)";
        }
        if (upperHdlcAddress < 0 || upperHdlcAddress > 65535)
        {
          message = "The upper HDLC address must be between 0 and 65535 (including)";
        }
        break;
      default:
        message = "Unsupported address length";
    }
    return message;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final HdlcAddress other = (HdlcAddress)obj;
    if (this.upperHdlcAddress != other.upperHdlcAddress)
    {
      return false;
    }
    if (this.lowerHdlcAddress != other.lowerHdlcAddress)
    {
      return false;
    }
    if (this.addressLength != other.addressLength)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 79 * hash + this.upperHdlcAddress;
    hash = 79 * hash + this.lowerHdlcAddress;
    hash = 79 * hash + this.addressLength;
    return hash;
  }

}
