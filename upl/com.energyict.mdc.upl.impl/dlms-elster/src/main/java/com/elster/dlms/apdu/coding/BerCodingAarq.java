/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/BerCodingAarq.java $
 * Version:     
 * $Id: BerCodingAarq.java 6743 2013-06-12 09:48:59Z osse $
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
 * IDs for AARQ APDUs and factory for an AARQ APDU decoder.
 *
 * @author osse
 */
public final class BerCodingAarq
{

  private BerCodingAarq()
  {
    //no instances allowed
  }
  
  
  public static final BerId ID_AARQ= new BerId(BerId.Tag.APPLICATION, true, 0);

  public static final BerId ID_PROTOCOL_VERSION= new BerId(BerId.Tag.CONTEXT_SPECIFIC, false, 0);

  public static final BerId ID_APPLICATION_CONTEXT_NAME= new BerId(BerId.Tag.CONTEXT_SPECIFIC, true,1);
  public static final BerId ID_CALLING_AP_TITLE= new BerId(BerId.Tag.CONTEXT_SPECIFIC, true,6);

  public static final BerId ID_SENDER_ACSE_REQUIREMENTS= new BerId(BerId.Tag.CONTEXT_SPECIFIC, false,10);
  public static final BerId ID_MECHANISM_NAME= new BerId(BerId.Tag.CONTEXT_SPECIFIC, false,11);

  public static final BerId ID_CALLING_AUTHENTICATION_VALUE= new BerId(BerId.Tag.CONTEXT_SPECIFIC, true,12);

  public static final BerId ID_USER_INFO= new BerId(BerId.Tag.CONTEXT_SPECIFIC, true,30);


   /**
   * Builds an decoder for decoding AARQ APDUs (BER).<P>
   *
   * @return The decoder.
   */
  public static BerDecoderCollection buildDecoderAarq()
  {
    BerDecoderMappedCollection result= new BerDecoderMappedCollection();

    result.addMapping(ID_PROTOCOL_VERSION, new BerDecoderBitString());
    result.addMapping(ID_SENDER_ACSE_REQUIREMENTS , new BerDecoderBitString());
    result.addMapping(ID_MECHANISM_NAME , new BerDecoderObjectIdentifer());
    result.addMapping(ID_CALLING_AUTHENTICATION_VALUE, buildAuthenticationValueDecoder());

    return result;
  }

  private static BerDecoderCollection buildAuthenticationValueDecoder()
  {
    BerDecoderMappedCollection result= new BerDecoderMappedCollection();
    //Octetstring instead of "Graphic String" to prevent String<-->bytes conversations.
    result.addMapping(BerAuthValueConverter.ID_AUTHENTICATION_VALUE_CHARSTRING, new BerDecoderOctetString());
    result.addMapping(BerAuthValueConverter.ID_AUTHENTICATION_VALUE_BITSTRING, new BerDecoderBitString());
    return result;
  }

}
