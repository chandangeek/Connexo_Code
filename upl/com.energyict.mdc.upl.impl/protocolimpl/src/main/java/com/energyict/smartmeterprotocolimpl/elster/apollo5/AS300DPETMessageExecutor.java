package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.PrivacyEnhancingDataAggregation;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 28/11/11
 * Time: 16:02
 */
public class AS300DPETMessageExecutor extends AS300MessageExecutor {

    private static final ObisCode PET_SETUP = ObisCode.fromString("0.128.0.2.0.255");

    public AS300DPETMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
        super(protocol, calendarFinder);
    }

    @Override
    public MessageResult executeMessageEntry(MessageEntry messageEntry) {
        success = true;

        try {
            String content = messageEntry.getContent();
            if (isGenerateNewPublicKey(content)) {
                return generateNewPublicKey(messageEntry);
            } else if (isSetPublicKeysOfAggregationGroup(content)) {
                return setPublicKeysOfAggregationGroup(messageEntry);
            } else {
                return super.executeMessageEntry(messageEntry);      //Handles the other AS300 messages
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    /**
     * Check the length of all key pairs (all keys should be 32 bytes long), write the key pairs (except the own) to the meter
     *
     * @param messageEntry
     * @return
     * @throws java.io.IOException
     */
    protected MessageResult setPublicKeysOfAggregationGroup(MessageEntry messageEntry) throws IOException {

        List<String> allKeyPairs = parseKeyPairs(messageEntry);
        List<String> allKeyPairsPairsExceptOwn = new ArrayList<String>();        //A list of all key pairs, except our own public key pair.
        String ownKeyPair = getOwnPublicKeyPair();

        for (String keyPair : allKeyPairs) {
            String[] keys = keyPair.split(",");
            for (String key : keys) {
                try {
                    byte[] keyBytes = ProtocolTools.getBytesFromHexString(key, "");
                    if (keyBytes.length != 32) {
                        return MessageResult.createFailed(messageEntry, "One of the RTU's has an invalid public key (should be 32 bytes long)");
                    }
                } catch (Exception e) {
                    return MessageResult.createFailed(messageEntry, "One of the RTU's has an invalid public key");
                }
            }
            if (!keyPair.equals(ownKeyPair)) {
                allKeyPairsPairsExceptOwn.add(keyPair);   //Don't add the own public key pair to the list
            }
        }

        getPETSetup().setPublicKeysOfAggregationGroup(allKeyPairsPairsExceptOwn);
        return MessageResult.createSuccess(messageEntry);
    }

    protected List<String> parseKeyPairs(MessageEntry messageEntry) throws IOException {
        String content = messageEntry.getContent();
        int index = 1;
        List<String> keyPairs = new ArrayList<String>();
        while (true) {
            try {
                String keyPair = getValueFromXML(AS300DPETMessaging.KEY + String.valueOf(index), content);
                keyPairs.add(keyPair);
                index++;
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return keyPairs;
    }

    private String getValueFromXML(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        int endIndex = content.indexOf("</" + tag);
        return content.substring(startIndex + tag.length() + 2, endIndex);
    }

    /**
     * Read the own public key pair. The result is key_x and key_y, comma separated. Each part is 32 bytes long.
     *
     * @return
     * @throws java.io.IOException
     */
    private String getOwnPublicKeyPair() throws IOException {
        PrivacyEnhancingDataAggregation privacyEnhancingDataAggregation = getPETSetup();
        Structure ownPublicKey = privacyEnhancingDataAggregation.getOwnPublicKey();
        OctetString public_x = (OctetString) ownPublicKey.getDataType(0);
        OctetString public_y = (OctetString) ownPublicKey.getDataType(1);
        String public_x_string = ProtocolTools.getHexStringFromBytes(public_x.getOctetStr(), "");
        String public_y_string = ProtocolTools.getHexStringFromBytes(public_y.getOctetStr(), "");

        return public_x_string + "," + public_y_string;
    }

    private PrivacyEnhancingDataAggregation getPETSetup() throws IOException {
        return protocol.getDlmsSession().getCosemObjectFactory().getPrivacyEnhancingDataAggregation(PET_SETUP);
    }

    private MessageResult generateNewPublicKey(MessageEntry messageEntry) throws IOException {
        byte[] randomBytes = null;
        String[] parts = messageEntry.getContent().split("=");
        String random = null;
        if (parts.length > 1) {
            random = parts[1].substring(1).split("\"")[0];
        }
        if (random != null && random.length() == 64) {   //64 hex characters form 32 bytes
            try {
                randomBytes = ProtocolTools.getBytesFromHexString(random, "");
            } catch (NumberFormatException e) {
                //Move on with null byte array
            }
        }

        getPETSetup().generateNewKeyPair(randomBytes);
        log(Level.INFO, "Successfully generated new public key");

        return MessageResult.createSuccess(messageEntry);
    }

    private boolean isSetPublicKeysOfAggregationGroup(String content) {
        return (content != null) && content.contains(AS300DPETMessaging.SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP);
    }

    private boolean isGenerateNewPublicKey(String content) {
        return (content != null) && content.contains(AS300DPETMessaging.GENERATE_NEW_PUBLIC_KEY);
    }
}