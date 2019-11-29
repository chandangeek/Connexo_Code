/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/BerAuthValueConverter.java $
 * Version:     
 * $Id: BerAuthValueConverter.java 6722 2013-06-11 10:17:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  06.08.2010 17:44:53
 */
package com.elster.dlms.apdu.coding;

import com.elster.ber.types.BerCollection;
import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueBitString;
import com.elster.ber.types.BerValueOctetString;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import com.elster.dlms.types.basic.BitString;
import java.io.IOException;

/**
 * Converts an {@link AuthenticationValue} to an BER object structure and the other way around.
 *
 * @author osse
 */
public class BerAuthValueConverter
{
  public static final BerId ID_AUTHENTICATION_VALUE_CHARSTRING = new BerId(BerId.Tag.CONTEXT_SPECIFIC, false,
                                                                           0);
  public static final BerId ID_AUTHENTICATION_VALUE_BITSTRING =
          new BerId(BerId.Tag.CONTEXT_SPECIFIC, false, 1);

  public AuthenticationValue buildAuthenticationValue(final BerCollection container) throws IOException
  {
    AuthenticationValue result = null;

    if (container.valueExists(BitString.class,
                              ID_AUTHENTICATION_VALUE_BITSTRING))
    {
      BitString authValue = container.findValue(BitString.class, ID_AUTHENTICATION_VALUE_BITSTRING);
      result = AuthenticationValue.createBitString(authValue);
    }
    else if (container.valueExists(byte[].class, ID_AUTHENTICATION_VALUE_CHARSTRING))
    {
      byte[] authValue = container.findValue( byte[].class, ID_AUTHENTICATION_VALUE_CHARSTRING);
      result = AuthenticationValue.createCharstring(authValue);
    }

    if (result == null)
    {
      throw new IOException("Calling authentication value type not supported: " + container.toString());
    }

    return result;
  }

  public BerCollection buildBerCollection(BerId id, AuthenticationValue authenticationValue) throws
          IOException
  {
    BerCollection result = new BerCollection(id);

    switch (authenticationValue.getType())
    {
      case BITSTRING:
        result.add(new BerValueBitString(ID_AUTHENTICATION_VALUE_BITSTRING, (BitString) (authenticationValue.getValue())));
        break;
      case CHARSTRING:
        result.add(new BerValueOctetString(ID_AUTHENTICATION_VALUE_CHARSTRING, authenticationValue.toBytes())); //Octetstring instead of graphicstring to prevent string<-->byte conversations.
        break;
      default:
        throw new IOException("Calling authentication value type not supported: " + authenticationValue.
                getType());
    }
    return result;
  }

}
