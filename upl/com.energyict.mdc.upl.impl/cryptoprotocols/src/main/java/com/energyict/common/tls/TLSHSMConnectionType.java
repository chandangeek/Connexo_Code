
//TODO: make it compatible with HSM
//package com.energyict.common.tls;
//
//import com.atos.worldline.jss.api.jca.JSSJCAProvider;
//import com.atos.worldline.jss.api.jca.spec.KeyLabelKeySpec;
//import com.energyict.cpo.Environment;
//import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
//import com.energyict.protocol.exceptions.ConnectionException;
//
//import javax.net.ssl.KeyManager;
//import java.security.*;
//import java.security.spec.InvalidKeySpecException;
//
//public class TLSHSMConnectionType extends TLSConnectionType {
//
//    protected KeyManager[] getKeyManagers(KeyStore keyStore) throws ConnectionException {
//        KeyManager[] keyManagers;
//        try {
//            keyManagers = new KeyManager[]{new HsmKeyManagerImpl(keyStore)};
//        } catch (Exception e) {
//            String pattern = Environment.getDefault().getTranslation("failedToSetupKeyManager",
//                    "Failed to setup HSM Key Manager, TLS connection will not be setup.");
//            throw new ConnectionException(pattern, e);
//        }
//        return keyManagers;
//    }
//
//    protected class HsmKeyManagerImpl extends X509KeyManagerImpl {
//        protected HsmKeyManagerImpl(KeyStore keyStore) throws Exception {
//            super(keyStore);
//        }
//
//        @Override
//        public String[] getClientAliases(String paramString, Principal[] paramArrayOfPrincipal) {
//            String pattern = Environment.getDefault().getTranslation("notSupportedOnClient",
//                    "Method not supported on client side");
//            throw new UnsupportedOperationException(pattern);
//        }
//
//        /**
//         * Returns EC private key from a HSM associated with a given alias. Note that corresponding X.509 certificate
//         * is stored in a keystore instead of HSM.
//         *
//         * @param alias the alias which refers EC private key in a HSM
//         * @return the private key instance, otherwise, null
//         */
//        @Override
//        public PrivateKey getPrivateKey(String alias) {
//            PrivateKey privateKey;
//            try {
//                KeyLabelKeySpec privateKeySpec = new KeyLabelKeySpec(alias);
//                KeyFactory keyFactory = KeyFactory.getInstance(JSSJCAProvider.ALGORITHM_NAME_EC, JSSJCAProvider.NAME);
//                privateKey = keyFactory.generatePrivate(privateKeySpec);
//            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
//                getLogger().warning("A matching alias for private key stored in HSM could not be found. " + e);
//                return null;
//            }
//            return privateKey;
//        }
//    }
//}
