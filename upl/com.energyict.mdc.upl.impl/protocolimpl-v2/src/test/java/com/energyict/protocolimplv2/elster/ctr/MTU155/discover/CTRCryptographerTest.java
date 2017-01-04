package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author sva
 * @since 4/07/13 - 16:41
 */
public class CTRCryptographerTest {

    @Test
    public void testDecryptionOfSMS() throws Exception {
        CTRCryptographer cryptographer = new CTRCryptographer();

        assertEquals("Expecting the cryptographer not to be used before.", false, cryptographer.wasUsed());

        // Business methods
        byte[] encryptedMessage = ProtocolTools.getBytesFromHexString("0000007F2F1D0B82DFF7BE9C63BE9D3E061428B739E5D1EFED263C2B28C43603F4D7580663DFAE7FE77F897D66C5C5390DEAA120652B38699495DD7D5FB1484F41A75F60720E7FA777C36109BC696A3BDB381DB47679A5719AD006034E837055467ECDBF26729CD0CEE70C56E2907FD9AF3849CF0616E9337BC2A7815D206A9BFF9732473CEC15C090190000", "");
        SMSFrame smsFrame = cryptographer.decryptSMS(getDefaultMTU155Properties(), encryptedMessage);

        // Asserts
        String expectedDecryptedFrame = "0000003F50013030303030310503080009200C2113711372000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000015C090190000";
        assertEquals("The decrypted frame was different from what was expected.", expectedDecryptedFrame, ProtocolTools.getHexStringFromBytes(smsFrame.getBytes(), ""));
        assertEquals("Expecting the cryptographer to be used.", true, cryptographer.wasUsed());
    }

    private MTU155Properties getDefaultMTU155Properties() {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("Password", "000001");
        typedProperties.setProperty("SecurityLevel", "000001");
        typedProperties.setProperty("KeyC", "0000000000000001");

        return new MTU155Properties(typedProperties);
    }
}
