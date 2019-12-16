/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/BerCodingAare.java $
 * Version:     
 * $Id: BerCodingAare.java 6743 2013-06-12 09:48:59Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 16:35:36
 */
package com.elster.dlms.apdu.coding;

import com.elster.ber.coding.BerDecoderBitString;
import com.elster.ber.coding.BerDecoderCollection;
import com.elster.ber.coding.BerDecoderMappedCollection;
import com.elster.ber.coding.BerDecoderObjectIdentifer;
import com.elster.ber.coding.BerDecoderOctetString;
import com.elster.ber.types.BerId;

/**
 * IDs for AARE APDUs and factory for an AARE APDU decoder.
 *
 * @author osse
 */
public final class BerCodingAare
{
  private BerCodingAare()
  {
    //no instances allowed
  }

  public static final BerId ID_AARE = new BerId(BerId.Tag.APPLICATION, true, 1);
  public static final BerId ID_PROTOCOL_VERSION = new BerId(BerId.Tag.CONTEXT_SPECIFIC, false, 0);
  public static final BerId ID_APPLICATION_CONTEXT_NAME = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 1);
  public static final BerId ID_RESULT = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 2);
  public static final BerId ID_RESULT_SOURCE_DIAGNOSTIC = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 3);
  public static final BerId ID_RESULT_SOURCE_DIAGNOSTIC_ACSE_SERVICE_USER = new BerId(
          BerId.Tag.CONTEXT_SPECIFIC, true, 1);
  public static final BerId ID_RESULT_SOURCE_DIAGNOSTIC_ACSE_SERVICE_PROVIDER = new BerId(
          BerId.Tag.CONTEXT_SPECIFIC, true, 2);
  public static final BerId ID_RESPONDING_AP_TITLE = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 4);
  public static final BerId ID_RESPONDER_ACSE_REQUIREMENTS = new BerId(BerId.Tag.CONTEXT_SPECIFIC, false, 8);
  public static final BerId ID_MECHANISM_NAME = new BerId(BerId.Tag.CONTEXT_SPECIFIC, false, 9);
  public static final BerId ID_RESPONDING_AUTHENTICATION_VALUE = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true,
                                                                           10);
  public static final BerId ID_USER_INFO = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 30);

  /**
   * Builds an decoder for decoding AARE APDUs (BER).<P>
   *
   * @return The decoder.
   */
  public static BerDecoderCollection buildDecoderAare()
  {
    BerDecoderMappedCollection result = new BerDecoderMappedCollection();

    result.addMapping(ID_PROTOCOL_VERSION, new BerDecoderBitString());
    result.addMapping(ID_RESPONDER_ACSE_REQUIREMENTS, new BerDecoderBitString());
    result.addMapping(ID_MECHANISM_NAME, new BerDecoderObjectIdentifer());
    result.addMapping(ID_RESPONDING_AUTHENTICATION_VALUE, buildAuthenticationValueDecoder());

    return result;
  }

  private static BerDecoderCollection buildAuthenticationValueDecoder()
  {
    BerDecoderMappedCollection result = new BerDecoderMappedCollection();
    //Octetstring instead of "Graphic String" to prevent conversion to String and back to bytes.
    result.addMapping(BerAuthValueConverter.ID_AUTHENTICATION_VALUE_CHARSTRING, new BerDecoderOctetString());
    result.addMapping(BerAuthValueConverter.ID_AUTHENTICATION_VALUE_BITSTRING, new BerDecoderBitString());
    return result;
  }

}
