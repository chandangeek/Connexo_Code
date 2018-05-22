package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.Message;

import java.io.File;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This test is ignored while it is intended to be run only manually while it relies on a certain HSM and keys structure within HSM.
 *
 */
@Ignore
public class HsmAuthServiceImplTest {

    private static HsmAuthServiceImpl hsmAuthService;

    @BeforeClass
    public static void before() throws Exception {
        HsmConfigurationService hsmCfgService = new HsmConfigurationService();
        hsmCfgService.init(new File(HsmAuthServiceImplTest.class.getClassLoader().getResource("hsm-runtime-configuration.json").getFile()).getAbsolutePath());
        hsmAuthService = new HsmAuthServiceImpl();
        hsmAuthService.setHsmConfigurationService(hsmCfgService);
    }


    @Test
    public void testEncrypt() throws EncryptBaseException {
        String keyLabel = "ENXS-KEY-P1";
        Message msg = new Message("abcd1234", Charset.defaultCharset());
        AuthDataEncryptResponse encrypt = hsmAuthService.encrypt(new AuthDataEncryptRequest(keyLabel, msg.getBytes(), null, null));
        AuthDataDecryptResponse decrypt = hsmAuthService.decrypt(new AuthDataDecryptRequest(keyLabel, encrypt.getBytes(), null, encrypt.getInitialVector(), encrypt.getAuthTag()));

        Assert.assertEquals(msg.toString(), decrypt.toString());

    }

}