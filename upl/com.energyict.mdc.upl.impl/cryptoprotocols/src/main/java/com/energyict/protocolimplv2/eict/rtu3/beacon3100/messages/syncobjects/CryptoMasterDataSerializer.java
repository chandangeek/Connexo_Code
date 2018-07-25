package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am540.CryptoAM540;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;

/**
 * Extension of the {@link MasterDataSerializer}, supporting reversible and irreversible keys, and using the HSM to AES wrap keys
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/11/2016 - 10:09
 */
public class CryptoMasterDataSerializer extends MasterDataSerializer {

//    private final SecurityPropertyValueParser securityPropertyValueParser;

    public CryptoMasterDataSerializer(ObjectMapperService objectMapperService, PropertySpecService propertySpecService, DeviceMasterDataExtractor extractor, Beacon3100Properties beacon3100Properties, NlsService nlsService) {
        super(objectMapperService, propertySpecService, extractor, beacon3100Properties, nlsService);
//        securityPropertyValueParser = new SecurityPropertyValueParser();
    }

    /**
     * If the device in EIServer is configured to use the crypto protocol, sync the 'normal' AM540 to the Beacon.
     * The Beacon has no access to an HSM and does not know the crypto protocols.
     */
    @Override
    protected String getJavaClassName(Device device) {
        String javaClassName = super.getJavaClassName(device);
        if (javaClassName.equals(CryptoAM540.class.getName())) {
            javaClassName = AM540.class.getName();
        }
        return javaClassName;
    }

    /**
     * Parse the given property value as a key. This value can be a plain key, a reversible key or an irreversible key.
     * The result is either a plain key (16 bytes), or the bytes of an irreversible key to be used by the HSM.
     */
    @Override
    public byte[] parseKey(Device device, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            throw missingProperty(propertyName);
        }
//        return securityPropertyValueParser.parseSecurityPropertyValue(propertyName, propertyValue);
        return null; //TODO: get the key from security accessor?
    }

    /**
     * HLS secret is parsed in the exact same way as other keys.
     * LLS is not used at EVN.
     */
    @Override
    protected byte[] parseASCIIPassword(Device device, String propertyName, String propertyValue) {
        return parseKey(device, propertyName, propertyValue);
    }

    /**
     * Use the HSM to wrap the key in the case of irreversible keys.
     * Otherwise, do the wrapping manually.
     */
    @Override
    public byte[] wrap(byte[] dlmsMeterKEK, byte[] keyToEncrypt) {
//        try {
//            //If the keys are not reversible, use the HSM to calculate the wrapped keys.
//            return ProtocolService.INSTANCE.get().wrapMeterKeyForConcentrator(IrreversibleKey.fromByteArray(keyToEncrypt), IrreversibleKey.fromByteArray(dlmsMeterKEK));
//        } catch (HsmException e) {
//            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
//        }
        return null;
    }
}