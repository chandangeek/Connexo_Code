//
////TODO: make it compatible with HSM
//package com.energyict.common.tls;
//
//import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
//import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
//import com.energyict.mdc.upl.properties.PropertySpecService;
//import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
//import com.energyict.protocol.exceptions.ConnectionException;
//import com.energyict.protocolimplv2.messages.nls.Thesaurus;
//
//import javax.net.ssl.KeyManager;
//
//public class TLSHSMConnectionType extends TLSConnectionType {
//
//    public TLSHSMConnectionType(PropertySpecService propertySpecService, CertificateWrapperExtractor certificateWrapperExtractor) {
//        super(propertySpecService, certificateWrapperExtractor);
//    }
//
//    @Override
//    protected KeyManager getKeyManager() throws ConnectionException {
//        try {
//            return new HsmKeyManagerImpl(getKeyStore());
//        } catch (Exception e) {
//            throw new ConnectionException(Thesaurus.ID.toString(), ProtocolExceptionMessageSeeds.FAILED_TO_SETUP_HSM_KEY_MANAGER, e);
//        }
//    }
//
//}
