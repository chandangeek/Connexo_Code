package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.request.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.request.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.Message;

import java.io.File;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test will be run by failsafe maven plugin only when E2E profile will be enabled (see pom.xml).
 * See pom.xml configuration for details.
 * <p>
 * The purpose of this test is just to check some JSS/HSM call flows and they are intended to be run only on demand,
 * not integrated in C.I (at least for the moment)
 */
@Ignore
public class HsmAuthServiceImpIT {

    private static HsmAuthServiceImpl hsmAuthService;

    @BeforeClass
    public static void before() {
        HsmConfigurationServiceImpl hsmCfgService = new HsmConfigurationServiceImpl();
        hsmCfgService.init(new File(HsmAuthServiceImpIT.class.getClassLoader().getResource("hsm-runtime-configuration-be.json").getFile()).getAbsolutePath());
        hsmAuthService = new HsmAuthServiceImpl();
        hsmAuthService.setHsmConfigurationService(hsmCfgService);
    }


    @Test
    public void testEncrypt() throws HsmBaseException {
        String keyLabel = "ENXS-KEY-P1";
        Message msg = new Message("abcd1234", Charset.defaultCharset());
        AuthDataEncryptResponse encrypt = hsmAuthService.encrypt(new AuthDataEncryptRequest(keyLabel, msg.getBytes(), null, null));
        AuthDataDecryptResponse decrypt = hsmAuthService.decrypt(new AuthDataDecryptRequest(keyLabel, encrypt.getBytes(), null, encrypt.getInitialVector(), encrypt.getAuthTag()));

        Assert.assertEquals(msg.toString(), decrypt.toString());

    }

}