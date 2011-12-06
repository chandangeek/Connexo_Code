package com.energyict.dlms.cosem;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.PrivacyEnhancingDataAggregationAttributes;
import com.energyict.dlms.cosem.methods.PrivacyEnhancingDataAggregationMethods;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 30/11/11
 * Time: 15:52
 */
public class PrivacyEnhancingDataAggregation extends AbstractCosemObject implements CosemObject {

    public static final byte[] LN = new byte[]{0, (byte) 128, 0, 2, 0, (byte) 255};

    private Structure ownPublicKey = null;
    private Array aggregationGroupKey = null;

    /**
     * Creates a new instance of PrivacyEnhancingDataAggregation
     *
     * @param protocolLink
     * @param objectReference
     */
    public PrivacyEnhancingDataAggregation(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PRIVACY_ENHANCING_DATA_AGGREGATION.getClassId();
    }

    public long getValue() throws IOException {
        return 0;
    }

    public Date getCaptureTime() throws IOException {
        return null;
    }

    public ScalerUnit getScalerUnit() throws IOException {
        return null;
    }

    public Quantity getQuantityValue() throws IOException {
        return null;
    }

    public Date getBillingDate() throws IOException {
        return null;
    }

    public int getResetCounter() {
        return 0;
    }

    public String getText() throws IOException {
        return "";
    }

    public Structure getOwnPublicKey() throws IOException {
        if (ownPublicKey == null) {
            ownPublicKey = readOwnPublicKey();
        }
        return ownPublicKey;
    }

    private Structure readOwnPublicKey() throws IOException {
        this.ownPublicKey = new Structure(getResponseData(PrivacyEnhancingDataAggregationAttributes.OWN_PUBLIC_KEY), 0, 0);
        return ownPublicKey;
    }

    /**
     * List should contain the key pairs for the other meters in the aggregation group
     * A key pair consists of 2 comma separated keys.
     * Each key should be 32 bytes long.
     *
     * @param keysOfAggregationGroup list of key pairs
     * @throws java.io.IOException communication error
     */
    public void setPublicKeysOfAggregationGroup(List<String> keysOfAggregationGroup) throws IOException {
        List<Structure> keyPairs = new ArrayList<Structure>();
        Structure structure;
        for (String keyPair : keysOfAggregationGroup) {
            String[] keys = keyPair.split(",");
            structure = new Structure();
            structure.addDataType(new OctetString(ProtocolTools.getBytesFromHexString(keys[0], "")));
            structure.addDataType(new OctetString(ProtocolTools.getBytesFromHexString(keys[1], "")));
            keyPairs.add(structure);
        }
        Array array = new Array();

        for (Structure keyPair : keyPairs) {
            array.addDataType(keyPair);
        }
        write(PrivacyEnhancingDataAggregationAttributes.PUBLIC_KEYS_OF_AGGREGATION_GROUP, array.getBEREncodedByteArray());
    }

    public Array getAggregationGroupKey() throws IOException {
        if (aggregationGroupKey == null) {
            aggregationGroupKey = readAggregationGroupKey();
        }
        return aggregationGroupKey;
    }

    private Array readAggregationGroupKey() throws IOException {
        this.aggregationGroupKey = new Array(getResponseData(PrivacyEnhancingDataAggregationAttributes.PUBLIC_KEYS_OF_AGGREGATION_GROUP), 0, 0);
        return aggregationGroupKey;
    }

    /**
     * This is the setter for the own public keys.
     *
     * @param randomNumber 32 random bytes
     * @throws IOException
     */
    public void generateNewKeyPair(byte[] randomNumber) throws IOException {
        if (randomNumber == null || randomNumber.length != 32) {
            SecureRandom secureRandom = new SecureRandom();
            randomNumber = secureRandom.generateSeed(32);
        }
        methodInvoke(PrivacyEnhancingDataAggregationMethods.GENERATE_NEW_KEYPAIR, new OctetString(randomNumber));
    }
}