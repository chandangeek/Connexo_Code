package com.elster.jupiter.hsm.model;


public enum ChainingMode {
    ECB {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toJssFormat() {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.ECB;
        }
    },
    CBC {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toJssFormat()  {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.CBC;
        }
    },
    CFB {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toJssFormat()  {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.CFB;
        }
    },
    OFB {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toJssFormat()  {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.OFB;
        }
    };


    public com.atos.worldline.jss.api.basecrypto.ChainingMode toJssFormat() throws EncryptBaseException {
        throw new EncryptBaseException("Unsupported mapping on JSS format for padding algorithm:" + this);
    }
}
