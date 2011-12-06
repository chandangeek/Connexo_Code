package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.PrivacyEnhancingDataAggregation;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
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
public class Apollo5MessageExecutor extends AS300MessageExecutor {

    private static final String GENERATE_NEW_KEY_PAIR = "GenerateNewKeyPair";
    private static final String KEY_PAIR = "KeyPair";
    private static final String READ_OWN_PUBLIC_KEYS = "ReadOwnPublicKeys";
    private static final String SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP = "SetPublicKeysOfAggregationGroup";
    private static final ObisCode PUBLIC_KEYS = ObisCode.fromString("0.128.0.2.0.255");

    public Apollo5MessageExecutor(final AbstractSmartDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public MessageResult executeMessageEntry(MessageEntry messageEntry) {
        success = true;

        try {
            String content = messageEntry.getContent();
            if (isGenerateNewKeyPair(content)) {
                return generateNewKeyPair(messageEntry);
            } else if (isReadOwnPublicKeys(content)) {
                return readOwnPublicKeys(messageEntry);
            } else if (isSetPublicKeysOfAggregationGroup(content)) {
                return setPublicKeysOfAggregationGroup(messageEntry);
            } else {
                return super.executeMessageEntry(messageEntry);      //Handles the other AS300 messages
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        }

        if (success) {
            log(Level.INFO, "Message has FINISHED.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            log(Level.INFO, "Message has FAILED.");
            return MessageResult.createFailed(messageEntry);
        }
    }

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

        PrivacyEnhancingDataAggregation privacyEnhancingDataAggregation = protocol.getDlmsSession().getCosemObjectFactory().getPrivacyEnhancingDataAggregation(PUBLIC_KEYS);
        privacyEnhancingDataAggregation.setPublicKeysOfAggregationGroup(allKeyPairsPairsExceptOwn);
        return MessageResult.createSuccess(messageEntry);
    }

    protected List<String> parseKeyPairs(MessageEntry messageEntry) throws IOException {
        String content = messageEntry.getContent();
        int index = 1;
        List<String> keyPairs = new ArrayList<String>();
        while (true) {
            try {
                String keypair = getValueFromXML(KEY_PAIR + String.valueOf(index), content);
                keyPairs.add(keypair);
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
     * Reads the public key pair and stores it in the register.
     *
     * @param messageEntry
     * @return message result, including the register value containing the public key pair
     * @throws IOException
     */
    private MessageResult readOwnPublicKeys(MessageEntry messageEntry) throws IOException {
        String keyPair = getOwnPublicKeyPair();

        ObisCode publicKeysObisCode = ProtocolTools.setObisCodeField(PUBLIC_KEYS, 5, (byte) 2);
        RegisterValue registerValue = new RegisterValue(publicKeysObisCode, keyPair);

        MeterReadingData meterReadingData = new MeterReadingData();
        meterReadingData.add(registerValue);

        MeterData meterData = new MeterData();
        meterData.setMeterReadingData(meterReadingData);

        return MeterDataMessageResult.createSuccess(messageEntry, "Succesfully read out the public keypair: " + keyPair, meterData);
    }

    private String getOwnPublicKeyPair() throws IOException {
        PrivacyEnhancingDataAggregation privacyEnhancingDataAggregation = protocol.getDlmsSession().getCosemObjectFactory().getPrivacyEnhancingDataAggregation(PUBLIC_KEYS);
        Structure ownPublicKey = privacyEnhancingDataAggregation.getOwnPublicKey();
        OctetString public_x = (OctetString) ownPublicKey.getDataType(0);
        OctetString public_y = (OctetString) ownPublicKey.getDataType(1);
        return public_x.stringValue() + "," + public_y.stringValue();
    }

    private MessageResult generateNewKeyPair(MessageEntry messageEntry) throws IOException {
        byte[] randomBytes = null;
        String[] parts = messageEntry.getContent().split("=");
        String random = parts[1].substring(1).split("\"")[0];
        if (random != null || random.length() == 32) {
            try {
                randomBytes = ProtocolTools.getBytesFromHexString(random, "");
            } catch (NumberFormatException e) {
                //Move on with null byte array
            }
        }

        PrivacyEnhancingDataAggregation privacyEnhancingDataAggregation = protocol.getDlmsSession().getCosemObjectFactory().getPrivacyEnhancingDataAggregation(PUBLIC_KEYS);
        privacyEnhancingDataAggregation.generateNewKeyPair(randomBytes);

        return MessageResult.createSuccess(messageEntry);
    }

    private boolean isSetPublicKeysOfAggregationGroup(String content) {
        return (content != null) && content.contains(SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP);
    }

    private boolean isReadOwnPublicKeys(String content) {
        return (content != null) && content.contains(READ_OWN_PUBLIC_KEYS);
    }

    private boolean isGenerateNewKeyPair(String content) {
        return (content != null) && content.contains(GENERATE_NEW_KEY_PAIR);
    }
}