package com.elster.us.protocolimplv2.sel.frame.data;

import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public abstract class Data {
  
  public abstract byte[] toByteArray(boolean includeEtx) throws IOException;

  /**
   * Generates the CRC for this data (including ETX)
   * @return a four-byte byte array representing the CRC for data with ETX
   * @throws IOException
   */
  public byte[] generateCRC() {
      try {
          int crc = CRCGenerator.calcCCITTCRC(toByteArray(true));
          byte[] crcBytes = ProtocolTools.getBytesFromInt(crc, 2);
          String crcASCII = ProtocolTools.getHexStringFromBytes(crcBytes, "");
          return crcASCII.getBytes();
      } catch (IOException ioe) {
          // TODO: is this the correct type of exception?
          throw ConnectionCommunicationException.unexpectedIOException(ioe);
      }
  }

}
