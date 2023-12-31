package com.elster.jupiter.hsm.model.request;

import com.atos.worldline.jss.api.custom.energy.AESDeviceKey;
import com.atos.worldline.jss.api.custom.energy.DeviceKey;
import com.atos.worldline.jss.api.custom.energy.TransportKey;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.config.HsmConfigurationPropFileImpl;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImportKeyRequestIT {

    private final static String CONFIG_FILE = "hsm-test-bundle-configuration.properties";

    private static final String LABEL = "Pub_KEK_SM";
    private static final AsymmetricAlgorithm WRAPPER_KEY_ALGORITHM = AsymmetricAlgorithm.RSA_15;;
    private static final byte[] TRANSPORT_KEY = new byte[]{1,2,3,4,5};
    private static final SymmetricAlgorithm DEVICE_KEY_ENCRYPTION_ALGORHITM = SymmetricAlgorithm.AES_256_CBC;;
    private static final byte[] DEVICE_KEY = new byte[]{9,8,7,6,5};
    private static final byte[] DEVICE_KEY_INIT_VECTOR = new byte[]{5,4,3,2,1};;
    public static final int KEY_SIZE = 32;
    private static final HsmKeyType HSM_KEY_TYPE = new HsmKeyType(HsmJssKeyType.AES, LABEL, SessionKeyCapability.SM_KEK_AGREEMENT, SessionKeyCapability.SM_KEK_RENEWAL, KEY_SIZE, false);

    private static HsmConfiguration HSM_CONFIG;
    private static ImportKeyRequest IKR;

    @BeforeClass
    public static void setUp() throws Exception {
        HSM_CONFIG = new HsmConfigurationPropFileImpl(ImportKeyRequestIT.class.getClassLoader().getResource(CONFIG_FILE).getFile());
        IKR = new ImportKeyRequest(LABEL, WRAPPER_KEY_ALGORITHM, TRANSPORT_KEY,  DEVICE_KEY_ENCRYPTION_ALGORHITM, DEVICE_KEY, DEVICE_KEY_INIT_VECTOR, HSM_KEY_TYPE);
    }

    @AfterClass
    public static void tearDown() {
        HSM_CONFIG = null;
        IKR = null;
    }

    @Test
    public void testGetAlgorithms(){
        Assert.assertEquals(WRAPPER_KEY_ALGORITHM, IKR.getWrapperKeyAlgorithm());
        Assert.assertEquals(DEVICE_KEY_ENCRYPTION_ALGORHITM, IKR.getDeviceKeyAlgorhitm());
    }

    @Test
    public void testGetLabels() throws HsmBaseException {
        Assert.assertEquals(HSM_KEY_TYPE.getLabel(), IKR.getStorageLabel());
        Assert.assertEquals(new KeyLabel(HSM_CONFIG.map(LABEL)), IKR.getWrapLabel(HSM_CONFIG));
    }

    @Test
    public void getHsmKeyType()  {
        Assert.assertEquals(HSM_KEY_TYPE, IKR.getHsmKeyType());
    }

    @Test
    public void testDeviceCipher() throws HsmBaseException {
        Assert.assertEquals(DEVICE_KEY_INIT_VECTOR, IKR.getDeviceKeyInitVector());
        Assert.assertEquals(DEVICE_KEY, IKR.getEncryptedDeviceKey());
        DeviceKey dk = new AESDeviceKey(DEVICE_KEY_INIT_VECTOR, DEVICE_KEY_ENCRYPTION_ALGORHITM.getHsmSpecs().getKekEncryptionMethod(), KEY_SIZE, DEVICE_KEY);
        Assert.assertEquals(dk, IKR.getDeviceKey());
        Assert.assertArrayEquals(DEVICE_KEY, IKR.getEncryptedDeviceKey());
    }

    @Test
    public void testGetTransportKey() throws HsmBaseException, UnsupportedKEKEncryptionMethodException {
        TransportKey tk = new TransportKey(new KeyLabel("IMP-SM-KEK"),DEVICE_KEY_ENCRYPTION_ALGORHITM.getKeySize(), TRANSPORT_KEY);
        Assert.assertEquals(tk, IKR.getTransportKey(HSM_CONFIG));
    }

}