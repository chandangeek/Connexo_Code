package com.elster.jupiter.hsm.model.krypto;


import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.impl.HsmFormatable;

public enum ChainingMode implements HsmFormatable<com.atos.worldline.jss.api.basecrypto.ChainingMode> {
    ECB {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toHsmFormat() {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.ECB;
        }
    },
    CBC {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toHsmFormat()  {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.CBC;
        }
    },
    CFB {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toHsmFormat()  {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.CFB;
        }
    },
    OFB {
        @Override
        public com.atos.worldline.jss.api.basecrypto.ChainingMode toHsmFormat()  {
            return com.atos.worldline.jss.api.basecrypto.ChainingMode.OFB;
        }
    };


    @Override
    public com.atos.worldline.jss.api.basecrypto.ChainingMode toHsmFormat() throws EncryptBaseException {
        throw new EncryptBaseException("Unsupported mapping on JSS format for padding algorithm:" + this);
    }

    public static ChainingMode from(String encryptionSpec) {
        return null;
    }
}
