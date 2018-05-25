package com.elster.jupiter.hsm.model;

public enum PaddingAlgorithm {
    NULL {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.NULL;
        }
    },
    LEFT_NULL {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.LEFT_NULL;
        }
    },
    ISO_9797_80M {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.ISO_9797_80M;
        }
    },
    ISO_9797_O80 {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.ISO_9797_O80;
        }
    },
    ANSI_X9_23 {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.ANSI_X9_23;
        }
    },
    EMSA_PKCS1_V1_5 {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.EMSA_PKCS1_V1_5;
        }
    },
    EME_PKCS1_V1_5 {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.EME_PKCS1_V1_5;
        }
    },
    EMSA_PKCS1_PSS {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.EMSA_PKCS1_PSS;
        }
    },
    PKCS {
        @Override
        public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm.PKCS;
        }
    };

    public com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm toJssFormat() throws EncryptBaseException {
        throw new EncryptBaseException("Unsupported mapping on JSS format for padding algorithm:" + this);
    }
}
