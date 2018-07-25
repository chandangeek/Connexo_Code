//package com.energyict.common;
//
//import com.energyict.hsm.worldline.keywrappers.DeviceKey;
//import com.energyict.hsm.worldline.keywrappers.DeviceKeyFactory;
//import com.energyict.hsm.worldline.keywrappers.PlainTextViewableKey;
//import com.energyict.hsm.worldline.keywrappers.impl.IrreversibleKey;
//import com.energyict.hsm.worldline.model.exception.HsmException;
//import com.energyict.hsm.worldline.services.ProtocolService;
//import com.energyict.protocol.exception.DeviceConfigurationException;
//
///**
// * Copyrights EnergyICT
// *
// * @author khe
// * @since 4/11/2016 - 15:16
// */
//public class SecurityPropertyValueParser {
//
//    private Boolean reversible = null;
//
//    /**
//     * In the case of reversible keys: the configured security properties are reversed to plain keys so they can be used by the 'normal' protocols
//     * In the case of irreversible keys: the configured security properties are encoded to byte arrays so they can be used by {@link ProtocolService}
//     */
//    public byte[] parseSecurityPropertyValue(String securityPropertyName, String securityPropertyValue) {
//        if (securityPropertyValue == null || securityPropertyValue.isEmpty()) {
//            return new byte[0];
//        }
//        try {
//            DeviceKey deviceKey = DeviceKeyFactory.INSTANCE.get().wrap(securityPropertyValue);
//            if (deviceKey instanceof PlainTextViewableKey) {
//                if (reversible != null && !reversible) {
//                    throw DeviceConfigurationException.invalidPropertyFormat(securityPropertyName, securityPropertyValue, "The configured security properties should be either all reversible, or all irreversible");
//                }
//                reversible = true;
//                return (((PlainTextViewableKey) deviceKey).getPlainTextValue());
//            } else {
//                if (reversible != null && reversible) {
//                    throw DeviceConfigurationException.invalidPropertyFormat(securityPropertyName, securityPropertyValue, "The configured security properties should be either all reversible, or all irreversible");
//                }
//                reversible = false;
//                return (((IrreversibleKey) deviceKey).toByteArray());
//            }
//        } catch (HsmException e) {
//            DeviceConfigurationException deviceConfigurationException = DeviceConfigurationException.invalidPropertyFormat(securityPropertyName, securityPropertyValue, e.getMessage());
//            deviceConfigurationException.initCause(e);
//            throw deviceConfigurationException;
//        }
//    }
//
//    public boolean isReversible() {
////        return reversible == null ? true : reversible;
//        return false; //TODO: for now in Connexo, only IrreversibleKeys will be used. On EIServer we have the possibility to use also reversible and plain keys. If that will be needed also in Connexo at some point, please uncomment the code and everything related to reversible keys
//    }
//}