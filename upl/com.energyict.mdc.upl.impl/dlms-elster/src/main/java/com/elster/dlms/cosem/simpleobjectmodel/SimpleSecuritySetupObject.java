/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleSecuritySetupObject.java $
 * Version:     
 * $Id: SimpleSecuritySetupObject.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.09.2011 13:39:09
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class64.KeyData;
import com.elster.dlms.security.DlmsSecurityProviderGcm;
import com.elster.dlms.types.data.DlmsDataArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Object for the Security Setup
 *
 * @author osse
 */
public class SimpleSecuritySetupObject extends SimpleCosemObject
{

  SimpleSecuritySetupObject(final SimpleCosemObjectDefinition definition,
                            final SimpleCosemObjectManager objectManager)
  {
    super(definition, objectManager);
  }

  public void wrapAndTransferKeys(final byte[] masterKey, final byte[] globalUnicastEncryptionKey,
                                  final byte[] globalBroadcastEncryptionKey, final byte[] authenticationKey)
          throws IOException
  {
    if (masterKey == null)
    {
      throw new IllegalArgumentException("The master key must not be null");
    }

    if (globalUnicastEncryptionKey == null && globalBroadcastEncryptionKey == null && authenticationKey
                                                                                      == null)
    {
      throw new IllegalArgumentException("At least one to transfer must not be null");
    }

    checkKeyLengthIfNotNull(globalUnicastEncryptionKey, 16);
    checkKeyLengthIfNotNull(globalBroadcastEncryptionKey, 16);
    checkKeyLengthIfNotNull(authenticationKey, 16);

    final byte[] wrappedGlobalUnicastEncryptionKey = wrapKeyIfNotNull(globalUnicastEncryptionKey, masterKey);
    final byte[] wrappedGlobalBroadcastEncryptionKey = wrapKeyIfNotNull(globalBroadcastEncryptionKey,
                                                                        masterKey);
    final byte[] wrappedAuthenticationKey = wrapKeyIfNotNull(authenticationKey, masterKey);

    transferKeys(wrappedGlobalUnicastEncryptionKey, wrappedGlobalBroadcastEncryptionKey,
                 wrappedAuthenticationKey);
  }

  public void transferKeys(final byte[] wrappedGlobalUnicastEncryptionKey,
                           final byte[] wrappedGlobalBroadcastEncryptionKey,
                           final byte[] wrappedAuthenticationKey) throws
          IOException
  {
    if (wrappedGlobalUnicastEncryptionKey == null && wrappedGlobalBroadcastEncryptionKey == null && wrappedAuthenticationKey
                                                                                                    == null)
    {
      throw new IllegalArgumentException("At least one to transfer must not be null");
    }
    checkKeyLengthIfNotNull(wrappedGlobalUnicastEncryptionKey, 24);
    checkKeyLengthIfNotNull(wrappedGlobalBroadcastEncryptionKey, 24);
    checkKeyLengthIfNotNull(wrappedAuthenticationKey, 24);


    final List<KeyData> keyDataList = new ArrayList<KeyData>(3);

    if (wrappedGlobalUnicastEncryptionKey != null)
    {
      keyDataList.add(new KeyData(KeyData.KeyId.GLOBAL_UNICAST_ENCRYPTION_KEY,
                                  wrappedGlobalUnicastEncryptionKey));
    }

    if (wrappedGlobalBroadcastEncryptionKey != null)
    {
      keyDataList.add(new KeyData(KeyData.KeyId.GLOBAL_BROADCAST_ENCRYPTION_KEY,
                                  wrappedGlobalBroadcastEncryptionKey));
    }

    if (wrappedAuthenticationKey != null)
    {
      keyDataList.add(new KeyData(KeyData.KeyId.AUTHETICATION_KEY,
                                  wrappedAuthenticationKey));
    }

    getManager().executeMethod(getDefinition(), 2, new DlmsDataArray(keyDataList.toArray(
            KeyData.EMPTY_KEY_DATA_ARRAY)));

  }

  private byte[] wrapKeyIfNotNull(final byte[] key, final byte[] kek)
  {
    if (key == null)
    {
      return null;
    }
    else
    {
      return DlmsSecurityProviderGcm.wrapKey(key, kek);
    }
  }

  private void checkKeyLengthIfNotNull(final byte[] key, final int expectedKeyLength)
  {
    if (key != null && key.length != expectedKeyLength)
    {
      throw new IllegalArgumentException("Wrong key length:" + key.length + ", Expected:" + expectedKeyLength);
    }
  }

}
