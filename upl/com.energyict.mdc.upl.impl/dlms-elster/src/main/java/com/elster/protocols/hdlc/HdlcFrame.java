/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcFrame.java $
 * Version:     
 * $Id: HdlcFrame.java 3657 2011-09-30 17:25:40Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 17:21:14
 */
package com.elster.protocols.hdlc;

import com.elster.coding.CodingUtils;
import com.elster.protocols.streams.CountingInputStream;
import com.elster.protocols.streams.FcsChecksumOutputStream;
import com.elster.protocols.streams.FcsChecksumInputStream;
import com.elster.protocols.streams.SafeReadInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class represents a single HDLC frame
 *
 * @author osse
 */
public class HdlcFrame
{
  private int formatType = 0x0A;
  private boolean segmentationBit;
  private int frameLength;
  private HdlcAddress destAddress;
  private HdlcAddress sourceAddress;
  private HdlcControlField controllField;
  private byte[] information;

  private void decodeFrameFormat(int byte1, int byte2)
  {
    int frameFormat = byte1 << 8 | byte2;
    formatType = (frameFormat >> 12) & 0x0F;
    segmentationBit = 0 != (frameFormat & 0x800);
    frameLength = frameFormat & 0x7FF;
  }

  private int encodeFrameFormat(OutputStream outputStream) throws IOException
  {
    int frameFormat = (formatType << 12);

    if (segmentationBit)
    {
      frameFormat = frameFormat | 0x800;
    }

    frameFormat = frameFormat | frameLength;
    outputStream.write(0xFF & (frameFormat >> 8));
    outputStream.write(0xFF & (frameFormat));
    return 2;
  }

  /**
   * Encodes (writes) this frame to the specified output stream.
   *
   * @param outputStream
   * @throws IOException
   */
  public void encode(OutputStream outputStream) throws IOException
  {
    //--- frameLength bestimmen ---
    frameLength =
            2 + getDestAddress().getAddressLength() + getSourceAddress().getAddressLength() + 1 + 2;

    if (information != null && information.length > 0)
    {
      frameLength += 2 + information.length; //2: the additionally checksum.
    }

    FcsChecksumOutputStream checksumOutputStream = new FcsChecksumOutputStream(outputStream);

    encodeFrameFormat(checksumOutputStream);

    destAddress.encode(checksumOutputStream);
    sourceAddress.encode(checksumOutputStream);

    getControllField().encode(checksumOutputStream);

    checksumOutputStream.writeChecksum();
//    outputStream.flush();

    if (information != null && information.length > 0)
    {
      checksumOutputStream.write(information);
      checksumOutputStream.writeChecksum();
//      outputStream.flush();
    }
  }

  /**
   * Read one frame from the input stream.<P>
   * Up to two leading markers (0x7E) will be ignored.<br>
   * Tailing markers will not be read.<br>
   * A leading marker is not required.
   *
   * @param inputStream the input stream.
   * @return the number of bytes read.
   * @throws IOException
   */
  public int decode(InputStream inputStream) throws IOException
  {
    int singleByte;
    int singleByte2;
    int skippedMarkers = 0;

    CountingInputStream countingInputStream = new CountingInputStream(new SafeReadInputStream(inputStream));
    FcsChecksumInputStream fcsInputStream = new FcsChecksumInputStream(countingInputStream);

    //--- skip Markers (0x7E) and read frame format ---
    do
    {
      singleByte = fcsInputStream.read();

      if (singleByte == 0x7E)
      {
        skippedMarkers++;
        countingInputStream.resetCount();
        fcsInputStream.resetChecksum();
      }

      if (skippedMarkers > 2)
      {
        throw new HdlcDecodingIOException("More the two markers (0x7E) read.");
      }


    }
    while (singleByte == 0x7E);

    //Check the frame format
    if (((singleByte >> 4) & 0x0F) != 0x0A)
    {
      throw new HdlcDecodingIOException("unexpected frame format type. First byte: " + singleByte
                                        + " Frame format:" + (singleByte >> 4));
    }

    singleByte2 = fcsInputStream.read();

    decodeFrameFormat(singleByte, singleByte2);


    //--- dest. address ---
    destAddress = new HdlcAddress();
    destAddress.decode(fcsInputStream);

    //--- source address ---
    sourceAddress = new HdlcAddress();
    sourceAddress.decode(fcsInputStream);


    //--- controll field ---
    controllField = new HdlcControlField();
    controllField.decode(fcsInputStream);

    //--- Read and check header checksum (or frame checksum if frame doesn't contains data) ---
    fcsInputStream.read();
    fcsInputStream.read();
    fcsInputStream.checkChecksum();

    int informationLength = frameLength - countingInputStream.getCount() - 2;

    if (informationLength > 0)
    {
      information = new byte[informationLength];
      fcsInputStream.read(information); //Full amount of data is ensured by the underlying SafeInputStream

      //--- Read and check frame checksum ---
      fcsInputStream.read();
      fcsInputStream.read();
      fcsInputStream.checkChecksum();
    }
    else
    {
      information = new byte[0];
    }

    if (frameLength != (countingInputStream.getCount()))
    {
      throw new HdlcDecodingIOException("Wrong frame length");
    }

    return countingInputStream.getCount() + skippedMarkers;
  }

  /**
   * Return the control field.
   *
   *
   * @return the control field
   */
  public HdlcControlField getControllField()
  {
    if (controllField == null)
    {
      controllField = new HdlcControlField();
    }
    return controllField;
  }

  /**
   * Sets the control field.
   *
   * @param controllField the control field
   */
  public void setControllField(HdlcControlField controllField)
  {
    this.controllField = controllField;
  }

  /**
   * Returns the destination address.
   *
   * @return the destination address
   */
  public HdlcAddress getDestAddress()
  {
    if (destAddress == null)
    {
      destAddress = new HdlcAddress();
    }
    return destAddress;
  }

  /**
   * Sets the destination address.
   *
   * @param destAddress
   */
  public void setDestAddress(HdlcAddress destAddress)
  {
    this.destAddress = destAddress;
  }

  /**
   * Returns the format type of the frame.<P>
   * The default format type is 0x0A. No other format type can be
   * decoded by this class.
   *
   * @return the format type.
   */
  public int getFormatType()
  {
    return formatType;
  }

  /**
   * Sets the format type of the frame. A changed format type will be encoded by
   * same rules as the default format type 0x0A.
   *
   * @param formatType
   * @deprecated A changed format type leads (probably) to an invalid frame. This
   * frame cannot be decoded by this class. Change it only for testing purposes.
   */
  @Deprecated
  public void setFormatType(int formatType)
  {
    this.formatType = formatType;
  }

  /**
   * The frame length of an decoded or encoded frame.<P>
   * This length will be set in the encode and decode methods.
   *
   * @return the frame length (if allready calculated)
   */
  public int getFrameLength()
  {
    return frameLength;
  }

  /**
   * Returns directly the internal information byte array (not a copy).
   * <P>
   * This method can be used if the byte array will not be changed outside or if this
   * frame will be not be used anymore after calling this method.
   * <P>
   * To get a copy of the byte array use {@link #getInformation()}.
   *
   * @return the information byte array.
   */
  public byte[] getInformationBytes()
  {
    return information;
  }

  /**
   * Sets directly the information byte array.<P> This array <b>must not
   * be changed</b> after calling this method. If this cannot be ensured use
   * {@link #setInformation(byte[]) }
   * 
   * @param information The information.
   */
  public void setInformationBytes(byte[] information)
  {
    this.information = information.clone();
  }

  /**
   * Returns a copy of the information byte array.
   *
   * @return the information byte array.
   */
  public byte[] getInformation()
  {
    if (information == null)
    {
      return null;
    }
    else
    {
      return information.clone();
    }
  }

  /**
   * Copies the information or an part of it to an internal array.
   *
   * @param information The information
   * @param off The offset.
   * @param len The length.
   */
  public void setInformation(byte[] information, int off, int len)
  {
    this.information = new byte[len];
    System.arraycopy(information, off, this.information, 0, len);
  }

  /**
   * Copies the information to an internal array.
   * 
   * @param information The information
   */
  public void setInformation(byte[] information)
  {
    setInformation(information, 0, information.length);
  }

  /**
   * Returns {@code true} if the segmentation bit is set.
   *
   * @return {@code true} if the segmentation bit is set.
   */
  public boolean isSegmentationBit()
  {
    return segmentationBit;
  }

  /**
   * Sets the segmentation bit.
   *
   * @param segmentationBit The segmentation bit.
   */
  public void setSegmentationBit(boolean segmentationBit)
  {
    this.segmentationBit = segmentationBit;
  }

  /**
   * Returns the source address of this frame.
   *
   * @return the source address.
   */
  public HdlcAddress getSourceAddress()
  {
    if (sourceAddress == null)
    {
      sourceAddress = new HdlcAddress();
    }
    return sourceAddress;
  }

  /**
   * Sets the source address of this frame.
   *
   * @param sourceAddress the source address.
   */
  public void setSourceAddress(HdlcAddress sourceAddress)
  {
    this.sourceAddress = sourceAddress;
  }

  @Override
  public String toString()
  {
    return "HdlcFrame{" + "formatType=" + formatType + ", segmentationBit=" + segmentationBit
           + ", frameLength=" + frameLength + ", destAddress=" + destAddress + ", sourceAddress="
           + sourceAddress + ", controllField=" + controllField + ", information=" + CodingUtils.
            byteArrayToString(information) + '}';
  }

}
