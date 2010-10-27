package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 16:36:31
 */
public class EncryptionTest {

    private static final String KEYC = "c34c052cc0da8d73451afe5f03be297f";
    private static final String KEYT = "c34c052cc0da8d73451afe5f03be297f";
    private static final String KEYF = "c34c052cc0da8d73451afe5f03be297f";

    private byte[] unencryptedTestFrame = ProtocolTools.getBytesFromHexString("0A0000003F53013030303030310702020A0A0B0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FB31CFA800000D", "");
    private byte[] encryptedTestFrame = ProtocolTools.getBytesFromHexString("0A0000007FC6E17593FF989FCFDAA64C1C2E6773D13BF18C2479F1F577A012087E54E56A8DEF9B849EABBCF1661259CC6953881DE649FCB03FB4F2CE6CAD5E20B104C0F82AFBC76696AC6DE041B53A19F1BEBC0E009A0E3253B83ACCBC245536CA5C220728D0C246062BDDB5279C13988CAAF8E48C474847C0B973D59668E46AA5B90E91E78F08FFD31E33722A0D", "");

    @Test
    public void testEncryption() {
        CTREncryption ctrEncryption = new CTREncryption(KEYC, KEYT, KEYF, 1);
        Frame request = new GPRSFrame();
        Frame unencryptedFrame = new GPRSFrame();
        try {
            Frame encrypted = ctrEncryption.encryptFrame(request);
            unencryptedFrame = (GPRSFrame) ctrEncryption.decryptFrame(encrypted);
        } catch (CtrCipheringException e) {
            fail(e.getMessage());
        }
        assertArrayEquals(request.getBytes(), unencryptedFrame.getBytes());
    }

    @Test
    public void testDecryption() throws Exception {
        String keyC = "32323232323232323232323232323232";

        String[] tests = new String[]{
                "0A0000007FBAE7852E9D8527D081D8C9103C05E0F12A71DDD1A5468BFEC584A71D7888D2B755CF01D07749731A8E7BA587C2DEF38C3F1F44EB25497C6B5A8E57309CB6909670AFBA333443E0E3984803FD8A7C40DFAA3EC464253549588D8DB90702EFBD406B70C8C5BD6101D5B2B6072F934A2D8DE16B15CDD8F0901F0F91045D72FEFC8C7C4B5CCAC8DABF9B0D",
                "0A0000003F53013030303030310102020A0A1400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005CCAC8DABF9B0D",
                "0A000000611F2767BBE601BA5968B6B367D7DA7F61A1A6676F9D340F9ED2D9D9262D2CEE3A847FECC29128F2ABB53AE2B02D58EBA0F88DC9EBFAF92D54ADAAC278D3E1277DD3EFAA94E00C16FD2037B70595DEF47A80A1D5F70B017681A29D16EF727F6ED83F2605AC99D91DD993DE2C4A01D076DA623D1FC2EC364B3227FF33D132D795640E14FFC23DAF7CDB0D",
                "0A000000215300123456789000000A0A1A1000060000001C0107020A0A1510000000000304727A030471D003047324030475FE03047B4E030485A8030486E8030486B60304886E03048792030487B0030487100304855803048440030483320304823803048166030480D0030480620304800803047FC203047F7C03047F3603047ED210000000FFC23DAF7CDB0D"
        };

        for (int i = 0; i < tests.length; i += 2) {
            byte[] encrypted = ProtocolTools.getBytesFromHexString(tests[i], "");
            byte[] expected = ProtocolTools.getBytesFromHexString(tests[i+1], "");
            CTREncryption ctrEncryption = new CTREncryption(keyC, keyC, keyC, 1);
            assertArrayEquals(expected, ctrEncryption.decryptFrame(new GPRSFrame().parse(encrypted, 0)).getBytes());
        }
    }


}
