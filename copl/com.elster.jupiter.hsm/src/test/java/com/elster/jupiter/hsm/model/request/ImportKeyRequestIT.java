package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.impl.HsmConfigurationPropFileImpl;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import com.atos.worldline.jss.api.custom.energy.AESDeviceKey;
import com.atos.worldline.jss.api.custom.energy.DeviceKey;
import com.atos.worldline.jss.api.custom.energy.TransportKey;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImportKeyRequestIT {

    private final static String CONFIG_FILE = "hsm-test-bundle-configuration.properties";

    private static final String LABEL = "Pub_KEK_SM";
    private static final AsymmetricAlgorithm TRANSPORT_KEY_ALGORITHM = AsymmetricAlgorithm.RSA_15;;
    private static final byte[] TRANSPORT_KEY = new byte[]{1,2,3,4,5};
    private static final SymmetricAlgorithm DEVICE_KEY_ENCRYPTION_ALGORHITM = SymmetricAlgorithm.AES_256_CBC;;
    private static final byte[] DEVICE_KEY = new byte[]{9,8,7,6,5};
    private static final byte[] DEVICE_KEY_INIT_VECTOR = new byte[]{5,4,3,2,1};;

    private static HsmConfiguration HSM_CONFIG;
    private static ImportKeyRequest IKR;

    @BeforeClass
    public static void setUp() throws Exception {
        HSM_CONFIG = new HsmConfigurationPropFileImpl(ImportKeyRequestIT.class.getClassLoader().getResource(CONFIG_FILE).getFile());
        IKR = new ImportKeyRequest(LABEL, TRANSPORT_KEY_ALGORITHM, TRANSPORT_KEY,  DEVICE_KEY_ENCRYPTION_ALGORHITM, DEVICE_KEY, DEVICE_KEY_INIT_VECTOR);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        HSM_CONFIG = null;
        IKR = null;
    }


    @Test
    public void testGetImportLabel() throws HsmBaseException {
        Assert.assertEquals("S-DB", IKR.getImportLabel(HSM_CONFIG));
    }

    @Test
    public void testGetTransportKey() throws HsmBaseException, UnsupportedKEKEncryptionMethodException {
        TransportKey tk = new TransportKey(new KeyLabel("IMP-SM-KEK"),DEVICE_KEY_ENCRYPTION_ALGORHITM.getKeySize(), TRANSPORT_KEY);
        Assert.assertEquals(tk, IKR.getTransportKey(HSM_CONFIG));
    }

    @Test
    public void getDeviceKey() throws HsmBaseException {
        DeviceKey dk = new AESDeviceKey(DEVICE_KEY_INIT_VECTOR, DEVICE_KEY_ENCRYPTION_ALGORHITM.getHsmSpecs().getKekEncryptionMethod(), 16, DEVICE_KEY);
        Assert.assertEquals(dk, IKR.getDeviceKey(HSM_CONFIG));
    }

    @Test
    public void getImportSessionCapability() throws HsmBaseException {
        Assert.assertEquals(SessionKeyCapability.SM_KEK_NONAUTHENTIC, IKR.getImportSessionCapability(HSM_CONFIG));
    }
}