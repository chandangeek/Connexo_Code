package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.crypto.HsmProtocolService;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
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

    private final HsmProtocolService hsmProtocolService;

    public CryptoMasterDataSerializer(ObjectMapperService objectMapperService, PropertySpecService propertySpecService, DeviceMasterDataExtractor extractor, Beacon3100Properties beacon3100Properties, NlsService nlsService, HsmProtocolService hsmProtocolService) {
        super(objectMapperService, propertySpecService, extractor, beacon3100Properties, nlsService);
        this.hsmProtocolService = hsmProtocolService;
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
     * Use the HSM to wrap the key in the case of irreversible keys.
     * Otherwise, do the wrapping manually.
     */
    @Override
    public byte[] wrap(byte[] dlmsMeterKEK, byte[] keyToEncrypt) {
        IrreversibleKey irreversibleDlmsMeterKEK = IrreversibleKeyImpl.fromByteArray(dlmsMeterKEK);
        IrreversibleKey irreversibleKeyToEncrypt = IrreversibleKeyImpl.fromByteArray(keyToEncrypt);
        return hsmProtocolService.wrapMeterKeyForConcentrator(irreversibleKeyToEncrypt, irreversibleDlmsMeterKEK);
    }
}