package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 15:58:37
 */
public class AesCMac128Test {

    byte[] key = ProtocolTools.getBytesFromHexString("$2B$7E$15$16$28$AE$D2$A6$AB$F7$15$88$09$CF$4F$3C");

    @Test
    public void testEncrypt0() throws Exception {
        byte[] T = ProtocolTools.getBytesFromHexString("$bb$1d$69$29$e9$59$37$28$7f$a3$7d$12$9b$75$67$46");
        byte[] M = ProtocolTools.getBytesFromHexString("");
        assertArrayEquals(T, new AesCMac128(key).getAesCMac128(M));
    }

    @Test
    public void testEncrypt16() throws Exception {
        byte[] T = ProtocolTools.getBytesFromHexString("$07$0a$16$b4$6b$4d$41$44$f7$9b$dd$9d$d0$4a$28$7c");
        byte[] M = ProtocolTools.getBytesFromHexString("$6b$c1$be$e2$2e$40$9f$96$e9$3d$7e$11$73$93$17$2a");
        assertArrayEquals(T, new AesCMac128(key).getAesCMac128(M));
    }

    @Test
    public void testEncrypt40() throws Exception {
        byte[] T = ProtocolTools.getBytesFromHexString("$df$a6$67$47$de$9a$e6$30$30$ca$32$61$14$97$c8$27");
        byte[] M = ProtocolTools.getBytesFromHexString("$6b$c1$be$e2$2e$40$9f$96$e9$3d$7e$11$73$93$17$2a" +
                "$ae$2d$8a$57$1e$03$ac$9c$9e$b7$6f$ac$45$af$8e$51" +
                "$30$c8$1c$46$a3$5c$e4$11"
        );
        assertArrayEquals(T, new AesCMac128(key).getAesCMac128(M));
    }

    @Test
    public void testEncrypt64() throws Exception {
        byte[] T = ProtocolTools.getBytesFromHexString("$51$F0$BE$BF$7E$3B$9D$92$FC$49$74$17$79$36$3C$FE");
        byte[] M = ProtocolTools.getBytesFromHexString("$6b$c1$be$e2$2e$40$9f$96$e9$3d$7e$11$73$93$17$2a" +
                "$ae$2d$8a$57$1e$03$ac$9c$9e$b7$6f$ac$45$af$8e$51" +
                "$30$c8$1c$46$a3$5c$e4$11$e5$fb$c1$19$1a$0a$52$ef" +
                "$f6$9f$24$45$df$4f$9b$17$ad$2b$41$7b$e6$6c$37$10"
        );
        assertArrayEquals(T, new AesCMac128(key).getAesCMac128(M));
    }

}
