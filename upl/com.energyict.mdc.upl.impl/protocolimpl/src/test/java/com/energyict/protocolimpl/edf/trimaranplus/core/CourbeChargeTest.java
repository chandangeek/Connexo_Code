package com.energyict.protocolimpl.edf.trimaranplus.core;


import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.edf.trimaranplus.TrimaranPlus;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author gna
 * @since 6-jan-2010
 */
public class CourbeChargeTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private PropertySpecService propertySpecService;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    //@Test
    public void doParseTest() throws IOException {
        byte[] data = ProtocolTools.getBytesFromHexString("$01$89$62$10$E1$40$10$4E$D2$10$4E$6F$10$4F$35$10$50$0D$10$50$0D$10$4F$6B$10$E1$50$10$4E$4B$10$4D$D6$10$4D$A9$10$4E$6F$10$4E$54$10$4E$03$10$E7$60$10$F2$82$10$4C$FE$10$4D$73$10$4D$4F$10$4D$BB$10$4D$85$10$4B$7B$10$E1$70$10$48$87$10$45$27$10$42$72$10$40$32$10$3C$C9$10$3B$CD$10$E0$00$10$3B$2B$10$3A$DA$10$3A$89$10$3A$53$10$3A$4A$10$3A$26$10$E0$10$10$39$CC$10$39$8D$10$39$9F$10$39$8D$10$39$60$10$39$69$10$C3$5C$10$E0$20$10$39$0F$10$39$21$10$39$06$10$39$84$10$39$3C$10$39$96$10$E0$30$10$39$18$10$38$EB$10$39$06$10$39$06$10$39$45$10$3A$14$10$E0$40$10$3A$14$10$40$71$10$40$44$10$3F$AB$10$3F$AB$10$40$7A$10$E0$50$10$42$7B$10$44$F1$10$45$DB$10$46$11$10$46$8F$10$49$05$10$E6$60$10$F3$82$10$4B$69$10$4C$80$10$4D$34$10$4F$B3$10$50$EE$10$52$DD$10$E0$70$10$53$13$10$52$9E$10$52$7A$10$52$4D$10$54$7B$10$55$38$10$E0$80$10$55$4A$10$55$14$10$55$77$10$55$E3$10$55$A4$10$55$65$10$E0$90$10$54$F0$10$54$DE$10$54$F9$10$55$2F$10$53$FD$10$53$9A$10$E0$A0$10$52$68$10$51$7E$10$50$A6$10$4F$BC$10$4F$C5$10$4F$98$10$E0$B0$10$4F$3E$10$4F$2C$10$4F$AA$10$50$28$10$50$C1$10$51$90$10$E0$C0$10$52$B0$10$53$76$10$53$76$10$53$AC$10$52$F8$10$51$7E$10$E0$D0$10$50$94$10$50$0D$10$4F$47$10$4F$E9$10$4F$47$10$4E$81$10$E0$E0$10$4E$27$10$4E$6F$10$50$0D$10$4F$FB$10$50$8B$10$51$1B$10$E0$F0$10$51$24$10$50$67$10$4F$F2$10$50$70$10$50$55$10$50$3A$10$E1$00$10$4E$93$10$4E$0C$10$4C$F5$10$4C$5C$10$4C$4A$10$4C$38$10$E1$10$10$4C$A4$10$4C$F5$10$4D$DF$10$4E$6F$10$4F$50$10$51$48$10$E1$20$10$52$32$10$51$D8$10$52$56$10$52$8C$10$52$DD$10$51$E1$10$E1$30$10$51$2D$10$51$75$10$52$0E$10$51$F3$10$52$32$10$52$20$10$E1$40$10$51$D8$10$51$12$10$51$87$10$52$68$10$52$56$10$51$24$10$E1$50$10$50$55$10$4F$B3$10$4E$FF$10$4F$74$10$4F$62$10$4E$F6$10$E7$60$10$F2$82$10$4E$9C$10$4E$D2$10$4E$AE$10$4E$FF$10$4E$8A$10$4C$5C$10$E1$70$10$48$A2$10$46$62$10$44$7C$10$40$29$10$3E$70$10$3E$43$10$E0$00$10$3D$D7$10$3D$CE$10$3D$C5$10$3D$8F$10$3D$11$10$3C$F6$10$E0$10$10$3C$54$10$3C$27$10$3C$81$10$3D$08$10$3C$ED$10$3C$AE$10$C3$5D$10$E0$20$10$3B$BB$10$3B$73$10$3B$34$10$3B$85$10$3B$22$10$3B$07$10$E0$30$10$3A$E3$10$3A$C8$10$3A$F5$10$3A$E3$10$3D$62$10$41$40$10$E0$40$10$40$32$10$3F$AB$10$40$20$10$40$4D$10$40$B0$10$41$64$10$E0$50$10$44$19$10$45$93$10$47$A6$10$47$A6$10$48$48$10$4A$40$10$E6$60$10$F3$82$10$4C$38$10$4D$46$10$4E$15$10$4E$FF$10$50$55$10$51$A2$10$E0$70$10$51$F3$10$51$EA$10$51$7E$10$51$2D$10$51$D8$10$52$71$10$E0$80$10$52$95$10$52$F8$10$52$C2$10$52$56$10$51$7E$10$52$4D$10$E0$90$10$52$20$10$52$32$10$52$20$10$51$EA$10$51$FC$10$51$99$10$E0$A0$10$50$DC$10$50$70$10$4F$1A$10$4F$11$10$4F$23$10$4E$39$10$E0$B0$10$4D$BB$10$4E$1E$10$4E$6F$10$4E$15$10$4E$93$10$4F$AA$10$E0$C0$10$50$28$10$50$C1$10$50$E5$10$50$43$10$50$43$10$4F$8F$10$E0$D0$10$4F$2C$10$4F$3E$10$4E$4B$10$4D$8E$10$4D$BB$10$4D$4F$10$E0$E0$10$4C$EC$10$4D$07$10$4C$BF$10$4D$22$10$4E$4B$10$4E$5D$10$E0$F0$10$4E$F6$10$4E$F6$10$4E$4B$10$4D$7C$10$4D$3D$10$4D$6A$10$E1$00$10$4C$A4$10$4B$96$10$4B$84$10$4B$CC$10$4B$D5$10$4B$F9$10$E1$10$10$4C$80$10$4C$41$10$4D$A0$10$4E$E4$10$4F$E9$10$50$E5$10$E1$20$10$51$2D$10$52$05$10$52$8C$10$52$C2$10$51$90$10$51$24$10$E1$30$10$51$12$10$50$28$10$4F$D7$10$50$67$10$51$B4$10$51$7E$10$E1$40$10$50$E5$10$50$94$10$50$55$10$50$A6$10$50$94$10$51$BD$10$E1$50$10$51$6C$10$51$75$10$50$55$10$4A$A3$10$49$B0$10$4A$37$10$E7$60$10$F2$82$10$4A$13$10$49$EF$10$49$B9$10$4C$6E$10$4D$6A$10$4B$60$10$E1$70$10$47$9D$10$45$81$10$44$4F$10$3F$C6$10$3D$62$10$3D$50$10$E0$00$10$3D$08$10$3D$3E$10$3D$2C$10$3C$F6$10$3C$93$10$3C$39$10$E0$10$10$3B$8E$10$3B$A9$10$3B$DF$10$3B$DF$10$3B$85$10$3B$6A$10$C3$5E$10$E0$20$10$3B$19$10$3A$F5$10$3A$F5$10$3B$34$10$3B$3D$10$BB$10$10$C3$5E$10$E2$30$10$F0$00$10$C3$5E$10$E2$20$10$F0$00$10$BA$EC$10$3A$E3$10$3B$10$10$3A$FE$10$3A$77$10$3A$A4$10$E0$30$10$3A$A4$10$3A$6E$10$3A$38$10$3A$6E$10$3A$38$10$40$20$10$E0$40$10$3F$2D$10$3E$A6$10$3F$2D$10$3F$2D$10$40$05$10$40$B0$10$E0$50$10$42$CC$10$45$4B$10$47$16$10$47$A6$10$48$3F$10$49$8C$10$E0$60$10$4B$69$10$4D$85$10$4E$9C$10$4F$47$10$50$70$10$51$00$10$E0$70$10$52$0E$10$52$A7$10$52$17$10$50$B8$10$4F$74$10$4F$50$10$E0$80$10$4F$1A$10$4F$1A$10$4E$42$10$4D$A9$10$4E$6F$10$4E$30$10$E0$90$10$4F$2C$10$4F$59$10$4E$AE$10$4E$78$10$4E$78$10$4F$62$10$E0$A0$10$4D$BB$10$4D$58$10$50$E5$10$50$5E$10$4F$A1$10$50$67$10$E0$B0$10$4F$B3$10$4E$D2$10$50$70$10$50$82$10$50$5E$10$50$94$10$E0$C0$10$51$2D$10$53$64$10$53$5B$10$53$88$10$52$71$10$52$20$10$E0$D0$10$51$B4$10$51$12$10$50$9D$10$50$AF$10$50$5E$10$4F$A1$10$E0$E0$10$4C$14$10$4B$3C$10$4B$7B$10$4B$84$10$4C$14$10$4C$AD$10$E0$F0$10$4B$72$10$4C$77$10$4C$F5$10$4C$B6$10$4C$77$10$4B$69$10$E1$00$10$4B$33$10$4A$37$10$4A$B5$10$4B$7B$10$4B$D5$10$4C$4A$10$E1$10$10$4D$10$10$4E$03$10$50$16$10$51$12$10$51$36$10$50$C1$10$E1$20$10$51$2D$10$51$B4$10$51$BD$10$51$7E$10$51$48$10$50$82$10$E1$30$10$4F$C5$10$4F$B3$10$4F$62$10$4F$3E$10$4F$23$10$4F$7D$10$E1$40$10$4E$8A$10$4E$DB$10$4F$50$10$4F$E9$10$4F$F2$10$4F$E9$10$E1$50$10$4F$08$10$4E$78$10$4D$B2$10$4E$1E$10$4D$6A$10$4C$DA$10$E1$60$10$4C$DA$10$4C$A4$10$4C$2F$10$4C$80$10$4C$53$10$4A$D0$10$E1$70$10$47$DC$10$45$ED$10$43$53$10$41$B5$10$40$9E$10$3D$A1$10$E0$00$10$3D$2C$10$3C$9C$10$3C$78$10$3C$9C$10$3A$5C$10$38$A3$10$E0$10$10$37$EF$10$37$D4$10$37$D4$10$37$C2$10$38$25$10$37$5F$10$C3$5F$10$E0$20$10$36$CF$10$36$E1$10$36$C6$10$36$99$10$36$7E$10$36$B4$10$E0$30$10$36$CF$10$36$BD$10$36$C6$10$36$EA$10$37$29$10$39$E7$10$E0$40$10$3D$35$10$3C$39$10$3C$1E$10$3C$A5$10$3C$AE$10$3D$BC$10$E0$50$10$3F$6C$10$41$D0$10$43$9B$10$44$58$10$45$54$10$46$D7$10$E6$60$10$F3$82$10$48$48$10$49$71$10$4A$37$10$4B$F9$10$4F$FB$10$51$3F$10$E0$70$10$52$A7$10$53$76$10$53$40$10$52$17$10$51$CF$10$52$29$10$E0$80$10$51$EA$10$51$09$10$4F$F2$10$4F$CE$10$50$0D$10$4F$2C$10$E0$90$10$4F$7D$10$4F$35$10$4F$08$10$4E$B7$10$4F$11$10$4F$35$10$E0$A0$10$4E$39$10$4D$7C$10$4C$AD$10$4C$AD$10$4C$B6$10$4C$80$10$E0$B0$10$4C$38$10$4C$DA$10$4D$46$10$4D$8E$10$4D$E8$10$4E$93$10$E0$C0$10$4E$C0$10$4E$D2$10$4F$50$10$4F$59$10$4E$C9$10$4E$4B$10$E0$D0$10$4D$3D$10$4D$7C$10$4D$3D$10$4C$F5$10$4C$FE$10$4C$BF$10$E0$E0$10$4D$22$10$4D$34$10$4D$61$10$4E$0C$10$4F$AA$10$4F$59$10$E0$F0$10$4F$62$10$4F$E9$10$4F$BC$10$4F$E9$10$4E$FF$10$4E$ED$10$E1$00$10$4E$8A$10$4D$CD$10$4D$D6$10$4D$97$10$4D$97$10$4E$54$10$E1$10$10$4F$98$10$51$A2$10$52$3B$10$53$40$10$54$9F$10$55$2F$10$E1$20$10$55$77$10$55$5C$10$54$D5$10$55$89$10$54$33$10$53$B5$10$E1$30$10$52$C2$10$53$01$10$51$87$10$51$63$10$51$51$10$54$2A$10$E1$40$10$55$53$10$55$53$10$55$DA$10$55$F5$10$55$92$10$54$E7$10$E1$50$10$54$DE$10$54$C3$10$53$0A$10$52$20$10$51$AB$10$51$CF$10$E7$60$10$F2$82$10$51$A2$10$52$A7$10$53$5B$10$53$76$10$53$9A$10$50$AF$10$E1$70$10$4C$53$10$4A$52$10$48$48$10$44$07$10$41$FD$10$3D$CE$10$E0$00$10$3D$C5$10$3D$AA$10$3D$AA$10$3D$C5$10$3D$50$10$3C$8A$10$E0$10$10$3B$CD$10$3B$A0$10$3B$B2$10$3B$97$10$3B$A0$10$3B$BB$10$C3$61$10$E0$20$10$3B$7C$10$3B$85$10$3B$61$10$3B$19$10$3B$22$10$3B$34$10$E0$30$10$3A$C8$10$3A$77$10$3A$9B$10$3A$80$10$3A$9B$10$3A$9B$10$E0$40$10$3F$E1$10$40$71$10$40$05$10$40$4D$10$40$A7$10$41$D0$10$E0$50$10$43$4A$10$44$97$10$46$F2$10$47$31$10$48$75$10$4A$49$10$E6$60$10$F3$82$10$4B$84$10$4D$F1$10$4F$08$10$50$E5$10$51$BD$10$52$05$10$E0$70$10$52$95$10$52$32$10$51$48$10$50$4C$10$50$F7$10$50$70$10$E0$80$10$50$D3$10$50$82$10$4F$C5$10$50$43$10$50$9D$10$50$DC$10$E0$90$10$51$7E$10$51$48$10$50$C1$10$4F$F2$10$4E$C0$10$4E$42$10$E0$A0$10$4E$27$10$4D$E8$10$4E$0C$10$4E$E4$10$4E$D2$10$4E$42$10$E0$B0$10$4F$2C$10$4E$8A$10$4E$8A$10$50$1F$10$50$8B$10$51$24$10$E0$C0$10$51$12$10$51$48$10$51$5A$10$50$CA$10$50$67$10$51$00$10$E0$D0$10$50$28$10$4F$D7$10$4E$A5$10$4E$4B$10$4D$2B$10$4C$DA$10$E0$E0$10$4C$4A$10$4C$02$10$4C$80$10$4C$26$10$4C$FE$10$4D$3D$10$E0$F0$10$4D$4F$10$4E$15$10$4E$0C$10$4D$D6$10$4D$34$10$4C$C8$10$E1$00$10$4C$38$10$4B$69$10$4B$60$10$4C$77$10$4C$BF$10$4E$9C$10$E1$10$10$4F$6B$10$4F$BC$10$4F$8F$10$50$4C$10$51$09$10$51$AB$10$E1$20$10$52$3B$10$52$20$10$51$B4$10$51$3F$10$51$00$10$50$AF$10$E1$30$10$4F$86$10$4F$C5$10$4F$C5$10$4F$BC$10$50$3A$10$50$E5$10$E1$40$10$4F$FB$10$4F$FB$10$50$1F$10$50$3A$10$50$9D$10$50$5E$10$E1$50$10$4F$AA$10$4F$08$10$4D$E8$10$4D$10$10$4C$80$10$4C$41$10$E7$60$10$F2$82$10$4C$80$10$4C$B6$10$4C$F5$10$4D$BB$10$4D$A0$10$4B$4E$10$E1$70$10$48$A2$10$46$1A$10$44$61$10$42$0F$10$40$E6$10$3F$00$10$E0$00$10$3D$6B$10$3C$F6$10$3C$A5$10$3C$8A$10$3C$1E$10$3B$CD$10$E0$10$10$38$EB$10$37$F8$10$37$D4$10$37$DD$10$37$EF$10$38$01$10$C3$62$10$E0$20$10$37$9E$10$37$83$10$37$4D$10$37$3B$10$37$3B$10$37$4D$10$E0$30$10$37$05$10$37$05$10$37$20$10$37$3B$10$37$A7$10$37$B9$10$E0$40$10$3A$9B$10$3E$EE$10$3D$86$10$3D$3E$10$3D$E0$10$3F$12$10$E0$50$10$40$95$10$41$25$10$42$18$10$44$DF$10$46$B3$10$47$4C$10$E6$60$10$F3$82$10$49$05$10$4C$9B$10$4D$FA$10$51$1B$10$51$E1$10$53$2E$10$E0$70$10$39$F0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$80$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$90$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$A0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$B0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$C0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$D0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$E0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$F0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$10$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$20$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$30$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$40$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$50$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E7$60$10$F2$82$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$70$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$10$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$C3$63$10$E0$20$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$30$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$40$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$50$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E6$60$10$F3$82$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$70$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$80$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$90$10$00$00$10$01$95$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$A0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$B0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$C0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$D0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$E0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$F0$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$10$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$20$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$30$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$40$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$50$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E7$60$10$F2$82$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E1$70$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$E0$10$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00$10$00$00", "$");

        //Data from a meter in GMT timezone!
        byte[] data2 = ProtocolTools.getBytesFromHexString("$01$89$62$10$00$46$10$00$4C$10$00$41$10$00$4B$10$00$45$10$E1$30$10$00$48$10$00$47$10$00$43$10$00$43$10$00$47$10$00$42$10$E1$40$10$00$4A$10$00$46$10$00$49$10$00$45$10$00$44$10$00$42$10$E1$50$10$00$3F$10$00$3C$10$00$3D$10$00$3B$10$00$38$10$00$36$10$E7$60$10$F2$82$10$00$36$10$00$35$10$00$35$10$00$33$10$00$2C$10$00$2C$10$E1$70$10$00$2D$10$00$2C$10$00$2D$10$00$2E$10$00$2D$10$00$2F$10$E0$00$10$00$2D$10$00$2E$10$00$2E$10$00$2D$10$00$2E$10$00$2D$10$E0$10$10$00$2D$10$00$2B$10$00$30$10$00$31$10$00$2E$10$00$2F$10$C3$5A$10$E0$20$10$00$2C$10$00$2E$10$00$2F$10$00$30$10$00$41$10$00$45$10$E0$30$10$00$3F$10$00$38$10$00$3A$10$00$35$10$00$30$10$00$33$10$E0$40$10$00$38$10$00$3B$10$00$38$10$00$39$10$00$39$10$00$3F$10$E0$50$10$00$44$10$00$59$10$00$58$10$00$5A$10$00$5B$10$00$68$10$E6$60$10$F3$82$10$00$5A$10$00$6F$10$00$63$10$00$5F$10$00$61$10$00$54$10$E0$70$10$00$52$10$00$52$10$00$4E$10$00$5C$10$00$4E$10$00$4A$10$E0$80$10$00$44$10$00$45$10$00$47$10$00$4C$10$00$44$10$00$47$10$E0$90$10$00$4B$10$00$48$10$00$4D$10$00$57$10$00$4E$10$00$50$10$E0$A0$10$00$47$10$00$43$10$00$43$10$00$4C$10$00$51$10$00$46$10$E0$B0$10$00$4F$10$00$43$10$00$50$10$00$50$10$00$4D$10$00$47$10$E0$C0$10$00$42$10$00$44$10$00$3C$10$00$49$10$00$48$10$00$51$10$E0$D0$10$00$46$10$00$4B$10$00$4B$10$00$43$10$00$3F$10$00$42$10$E0$E0$10$00$47$10$00$46$10$00$43$10$00$34$10$00$34$10$00$3C$10$E0$F0$10$00$3A$10$00$37$10$00$37$10$00$3A$10$00$36$10$00$3F$10$E1$00$10$00$3A$10$00$3C$10$00$3C$10$00$3C$10$00$39$10$00$39$10$E1$10$10$00$36$10$00$33$10$00$39$10$00$37$10$00$3F$10$00$4C$10$E1$20$10$00$4C$10$00$4F$10$00$51$10$00$4F$10$00$4C$10$00$48$10$E1$30$10$00$4A$10$00$49$10$00$4B$10$00$4E$10$00$4C$10$00$4B$10$E1$40$10$00$4F$10$00$50$10$00$4E$10$00$58$10$00$4E$10$00$4E$10$E1$50$10$00$4D$10$00$44$10$00$45$10$00$48$10$00$44$10$00$3C$10$E7$60$10$F2$82$10$00$36$10$00$36$10$00$36$10$00$34$10$00$34$10$00$30$10$E1$70$10$00$33$10$00$32$10$00$31$10$00$33$10$00$30$10$00$30$10$E0$00$10$00$32$10$00$33$10$00$31$10$00$32$10$00$32$10$00$2F$10$E0$10$10$00$30$10$00$32$10$00$30$10$00$30$10$00$35$10$00$31$10$C3$5B$10$E0$20$10$00$30$10$00$32$10$00$30$10$00$30$10$00$3F$10$00$42$10$E0$30$10$00$3F$10$00$46$10$00$3C$10$00$37$10$00$35$10$00$3A$10$E0$40$10$00$3B$10$00$3A$10$00$41$10$00$3A$10$00$3D$10$00$45$10$E0$50$10$00$52$10$00$67$10$00$5D$10$00$59$10$00$5A$10$00$63$10$E6$60$10$F3$82$10$00$61$10$00$5E$10$00$60$10$00$57$10$00$5C$10$00$5C$10$E0$70$10$00$5A$10$00$5B$10$00$59$10$00$51$10$00$4D$10$00$4A$10$E0$80$10$00$54$10$00$51$10$00$5C$10$00$51$10$00$52$10$00$5B$10$E0$90$10$00$5E$10$00$59$10$00$5C$10$00$66$10$00$5F$10$00$5A$10$E0$A0$10$00$54$10$00$4B$10$00$51$10$00$56$10$00$4F$10$00$57$10$E0$B0$10$00$59$10$00$4D$10$00$4D$10$00$4D$10$00$4C$10$00$53$10$E0$C0$10$00$4E$10$00$4C$10$00$4B$10$00$4B$10$00$42$10$00$49$10$E0$D0$10$00$3E$10$00$45$10$00$56$10$00$48$10$00$47$10$00$4A$10$E0$E0$10$00$38$10$00$3C$10$00$38$10$00$34$10$00$33$10$00$3D$10$E0$F0$10$00$3B$10$00$37$10$00$3A$10$00$3A$10$00$32$10$00$35$10$E1$00$10$00$3C$10$00$38$10$00$36$10$00$3D$10$00$3F$10$00$3B$10$E1$10$10$00$38$10$00$42$10$00$43$10$00$3C$10$00$3F$10$00$49$10$E1$20$10$00$42$10$00$43$10$00$4D$10$00$52$10$00$48$10$00$44$10$E1$30$10$00$4A$10$00$49$10$00$48$10$00$49$10$00$49$10$00$49$10$E1$40$10$00$52$10$00$4B$10$00$48$10$00$45$10$00$4D$10$00$49$10$E1$50$10$00$43$10$00$41$10$00$3C$10$00$3A$10$00$39$10$00$35$10$E7$60$10$F2$82$10$00$35$10$00$30$10$00$30$10$00$35$10$00$37$10$00$30$10$E1$70$10$00$31$10$00$2F$10$00$2E$10$00$2B$10$00$2B$10$00$2A$10$E0$00$10$00$2A$10$00$29$10$00$29$10$00$2C$10$00$2B$10$00$29$10$E0$10$10$00$29$10$00$2A$10$00$29$10$00$2A$10$00$2A$10$00$2A$10$C3$5C$10$E0$20$10$00$2B$10$00$2C$10$00$29$10$00$2D$10$00$3B$10$00$44$10$E0$30$10$00$33$10$00$3C$10$00$31$10$00$2E$10$00$33$10$00$36$10$E0$40$10$00$31$10$00$39$10$00$35$10$00$34$10$00$33$10$00$38$10$E0$50$10$00$3C$10$00$56$10$00$52$10$00$54$10$00$55$10$00$6C$10$E6$60$10$F3$82$10$00$5C$10$00$59$10$00$63$10$00$54$10$00$60$10$00$56$10$E0$70$10$00$5E$10$00$53$10$00$6C$10$00$4F$10$00$4B$10$00$48$10$E0$80$10$00$5E$10$00$57$10$00$55$10$00$50$10$00$5A$10$00$52$10$E0$90$10$00$52$10$00$43$10$00$4A$10$00$50$10$00$59$10$00$54$10$E0$A0$10$00$54$10$00$4F$10$00$51$10$00$49$10$00$46$10$00$43$10$E0$B0$10$00$42$10$00$41$10$00$43$10$00$3E$10$00$42$10$00$43$10$E0$C0$10$00$44$10$00$3F$10$00$3F$10$00$40$10$00$41$10$00$3A$10$E0$D0$10$00$3C$10$00$38$10$00$3A$10$00$3D$10$00$39$10$00$32$10$E0$E0$10$00$33$10$00$3F$10$00$3B$10$00$37$10$00$33$10$00$34$10$E0$F0$10$00$3B$10$00$3C$10$00$38$10$00$3A$10$00$3C$10$00$38$10$E1$00$10$00$34$10$00$32$10$00$2E$10$00$32$10$00$34$10$00$37$10$E1$10$10$00$39$10$00$32$10$00$3F$10$00$34$10$00$42$10$00$4D$10$E1$20$10$00$4C$10$00$49$10$00$47$10$00$4B$10$00$49$10$00$42$10$E1$30$10$00$45$10$00$3D$10$00$46$10$00$45$10$00$48$10$00$49$10$E1$40$10$00$41$10$00$43$10$00$44$10$00$44$10$00$3E$10$00$47$10$E1$50$10$00$44$10$00$40$10$00$39$10$00$3E$10$00$32$10$00$38$10$E7$60$10$F2$82$10$00$2F$10$00$2F$10$00$34$10$00$33$10$00$2D$10$00$2A$10$E1$70$10$00$29$10$00$2A$10$00$28$10$00$2A$10$00$2B$10$00$29$10$E0$00$10$00$2A$10$00$29$10$00$29$10$00$2A$10$00$28$10$00$2F$10$E0$10$10$00$2D$10$00$30$10$00$2B$10$00$2B$10$00$29$10$00$27$10$C3$5D$10$E0$20$10$00$29$10$00$28$10$00$28$10$00$2A$10$00$36$10$00$3B$10$E0$30$10$00$3A$10$00$3E$10$00$33$10$00$2B$10$00$2F$10$00$33$10$E0$40$10$00$32$10$00$35$10$00$36$10$00$34$10$00$34$10$00$35$10$E0$50$10$00$32$10$00$32$10$00$39$10$00$35$10$00$38$10$00$3A$10$E6$60$10$F3$82$10$00$4C$10$00$4A$10$00$54$10$00$4E$10$00$53$10$00$4D$10$E0$70$10$00$4F$10$00$4D$10$00$48$10$00$3E$10$00$49$10$00$50$10$E0$80$10$00$41$10$00$41$10$00$39$10$00$44$10$00$38$10$00$3E$10$E0$90$10$00$3A$10$00$40$10$00$30$10$00$42$10$00$35$10$00$31$10$E0$A0$10$00$3E$10$00$3B$10$00$40$10$00$3C$10$00$41$10$00$4B$10$E0$B0$10$00$3E$10$00$3B$10$00$39$10$00$39$10$00$3B$10$00$32$10$E0$C0$10$00$32$10$00$31$10$00$28$10$00$30$10$00$32$10$00$2E$10$E0$D0$10$00$2D$10$00$2F$10$00$2F$10$00$30$10$00$32$10$00$2D$10$E0$E0$10$00$28$10$00$2F$10$00$2C$10$00$2E$10$00$27$10$00$26$10$E0$F0$10$00$31$10$00$2E$10$00$2C$10$00$2E$10$00$32$10$00$28$10$E1$00$10$00$29$10$00$33$10$00$2E$10$00$2D$10$00$30$10$00$32$10$E1$10$10$00$2B$10$00$30$10$00$34$10$00$37$10$00$3E$10$00$44$10$E1$20$10$00$40$10$00$42$10$00$40$10$00$40$10$00$3C$10$00$3B$10$E1$30$10$00$3B$10$00$3A$10$00$3E$10$00$3F$10$00$43$10$00$45$10$E1$40$10$00$3F$10$00$3B$10$00$3C$10$00$39$10$00$3B$10$00$3A$10$E1$50$10$00$3B$10$00$38$10$00$36$10$00$36$10$00$33$10$00$31$10$E7$60$10$F2$82$10$00$33$10$00$31$10$00$32$10$00$33$10$00$34$10$00$36$10$E1$70$10$00$30$10$00$2F$10$00$2D$10$00$2C$10$00$2A$10$00$2A$10$E0$00$10$00$2B$10$00$2A$10$00$29$10$00$2B$10$00$29$10$00$2A$10$E0$10$10$00$2B$10$00$29$10$00$29$10$00$2A$10$00$2A$10$00$29$10$C3$5E$10$E0$20$10$00$2B$10$00$28$10$00$2B$10$00$2A$10$00$2B$10$00$2A$10$E0$30$10$00$2A$10$00$2A$10$00$2D$10$00$2D$10$00$37$10$00$37$10$E0$40$10$00$38$10$00$40$10$00$40$10$00$37$10$00$38$10$00$34$10$E0$50$10$00$3A$10$00$33$10$00$34$10$00$32$10$00$32$10$00$32$10$E0$60$10$00$36$10$00$39$10$00$37$10$00$38$10$00$3A$10$00$40$10$E0$70$10$00$58$10$00$58$10$00$5D$10$00$53$10$00$4C$10$00$39$10$E0$80$10$00$46$10$00$44$10$00$44$10$00$40$10$00$3E$10$00$45$10$E0$90$10$00$53$10$00$51$10$00$52$10$00$4A$10$00$46$10$00$4A$10$E0$A0$10$00$54$10$00$49$10$00$46$10$00$4E$10$00$58$10$00$56$10$E0$B0$10$00$4D$10$00$44$10$00$49$10$00$52$10$00$4A$10$00$40$10$E0$C0$10$00$3F$10$00$3C$10$00$33$10$00$33$10$00$35$10$00$37$10$E0$D0$10$00$3B$10$00$36$10$00$3E$10$00$45$10$00$3B$10$00$38$10$E0$E0$10$00$40$10$00$3E$10$00$37$10$00$37$10$00$33$10$00$34$10$E0$F0$10$00$2D$10$00$29$10$00$2F$10$00$34$10$00$34$10$00$2D$10$E1$00$10$00$2E$10$00$30$10$00$2E$10$00$30$10$00$34$10$00$33$10$E1$10$10$00$35$10$00$2F$10$00$30$10$00$3C$10$00$3F$10$00$46$10$E1$20$10$00$45$10$00$41$10$00$42$10$00$45$10$00$3D$10$00$3E$10$E1$30$10$00$41$10$00$44$10$00$40$10$00$41$10$00$42$10$00$40$10$E1$40$10$00$41$10$00$40$10$00$41$10$00$40$10$00$42$10$00$42$10$E1$50$10$00$3D$10$00$3E$10$00$40$10$00$40$10$00$40$10$00$3A$10$E1$60$10$00$3B$10$00$3D$10$00$37$10$00$3A$10$00$3F$10$00$37$10$E1$70$10$00$34$10$00$2D$10$00$2F$10$00$2D$10$00$2B$10$00$28$10$E0$00$10$00$29$10$00$28$10$00$2A$10$00$2C$10$00$2A$10$00$2A$10$E0$10$10$00$2B$10$00$2A$10$00$2A$10$00$2A$10$00$2A$10$00$2B$10$C3$5F$10$E0$20$10$00$2A$10$00$29$10$00$2A$10$00$27$10$00$28$10$00$28$10$E0$30$10$00$27$10$00$28$10$00$28$10$00$29$10$00$35$10$00$38$10$E0$40$10$00$3A$10$00$3A$10$00$3B$10$00$2D$10$00$2D$10$00$30$10$E0$50$10$00$33$10$00$3C$10$00$41$10$00$3B$10$00$39$10$00$3B$10$E6$60$10$F3$82$10$00$51$10$00$55$10$00$50$10$00$4B$10$00$54$10$00$55$10$E0$70$10$00$56$10$00$50$10$00$63$10$00$5B$10$00$42$10$00$47$10$E0$80$10$00$3F$10$00$45$10$00$45$10$00$4E$10$00$49$10$00$44$10$E0$90$10$00$4D$10$00$54$10$00$4F$10$00$4E$10$00$54$10$00$5A$10$E0$A0$10$00$5B$10$00$51$10$00$50$10$00$60$10$00$5D$10$00$5F$10$E0$B0$10$00$52$10$00$47$10$00$4F$10$00$51$10$00$45$10$00$4F$10$E0$C0$10$00$4B$10$00$4E$10$00$46$10$00$4C$10$00$50$10$00$44$10$E0$D0$10$00$3C$10$00$37$10$00$41$10$00$40$10$00$47$10$00$36$10$E0$E0$10$00$37$10$00$33$10$00$31$10$00$39$10$00$37$10$00$34$10$E0$F0$10$00$32$10$00$32$10$00$34$10$00$2E$10$00$2D$10$00$3B$10$E1$00$10$00$3E$10$00$3A$10$00$32$10$00$32$10$00$2E$10$00$34$10$E1$10$10$00$30$10$00$2E$10$00$33$10$00$34$10$00$40$10$00$45$10$E1$20$10$00$41$10$00$42$10$00$3E$10$00$41$10$00$42$10$00$43$10$E1$30$10$00$3C$10$00$43$10$00$46$10$00$46$10$00$44$10$00$44$10$E1$40$10$00$48$10$00$4B$10$00$46$10$00$49$10$00$43$10$00$40$10$E1$50$10$00$44$10$00$40$10$00$3D$10$00$3E$10$00$40$10$00$3B$10$E7$60$10$F2$82$10$00$3B$10$00$3D$10$00$36$10$00$3A$10$00$3B$10$00$35$10$E1$70$10$00$33$10$00$36$10$00$36$10$00$38$10$00$34$10$00$31$10$E0$00$10$00$2B$10$00$2A$10$00$2C$10$00$2C$10$00$2B$10$00$2D$10$E0$10$10$00$2E$10$00$2D$10$00$2B$10$00$2E$10$00$2C$10$00$2D$10$C3$61$10$E0$20$10$00$2C$10$00$2B$10$00$2C$10$00$2B$10$00$2C$10$00$2B$10$E0$30$10$00$2A$10$00$2B$10$00$31$10$00$36$10$00$33$10$00$32$10$E0$40$10$00$36$10$00$37$10$00$36$10$00$34$10$00$2E$10$00$2E$10$E0$50$10$00$35$10$00$31$10$00$3C$10$00$43$10$00$3D$10$00$3F$10$E6$60$10$F3$82$10$00$3F$10$00$54$10$00$52$10$00$4F$10$00$51$10$00$5B$10$E0$70$10$00$55$10$00$54$10$00$42$10$00$42$10$00$32$10$00$35$10$E0$80$10$00$3B$10$00$37$10$00$35$10$00$33$10$00$33$10$00$2C$10$E0$90$10$00$3B$10$00$40$10$00$3B$10$00$3D$10$00$30$10$00$36$10$E0$A0$10$00$40$10$00$46$10$00$40$10$00$4A$10$00$4C$10$00$4C$10$E0$B0$10$00$41$10$00$3B$10$00$3A$10$00$41$10$00$52$10$00$41$10$E0$C0$10$00$41$10$00$43$10$00$38$10$00$3F$10$00$3F$10$00$41$10$E0$D0$10$00$41$10$00$3C$10$00$3C$10$00$3E$10$00$39$10$00$3C$10$E0$E0$10$00$39$10$00$33$10$00$35$10$00$39$10$00$35$10$00$37$10$E0$F0$10$00$37$10$00$37$10$00$35$10$00$3C$10$00$3A$10$00$32$10$E1$00$10$00$32$10$00$35$10$00$3D$10$00$37$10$00$38$10$00$39$10$E1$10$10$00$38$10$00$35$10$00$39$10$00$40$10$00$46$10$00$4A$10$E1$20$10$00$49$10$00$46$10$00$45$10$00$45$10$00$43$10$00$48$10$E1$30$10$00$45$10$00$49$10$00$49$10$00$4B$10$00$4A$10$00$50$10$E1$40$10$00$46$10$00$49$10$00$47$10$00$43$10$00$46$10$00$46$10$E1$50$10$00$46$10$00$45$10$00$42$10$00$42$10$00$4B$10$00$4A$10$E7$60$10$F2$82$10$00$41$10$00$3A$10$00$39$10$00$3D$10$00$36$10$00$36$10$E1$70$10$00$32$10$00$32$10$00$31$10$00$31$10$00$2C$10$00$2C$10$E0$00$10$00$2A$10$00$2B$10$00$2C$10$00$2B$10$00$2D$10$00$2B$10$E0$10$10$00$2B$10$00$2A$10$00$2C$10$00$2B$10$00$2B$10$00$30", "$");

        TrimaranPlus trimaranPlus = new TrimaranPlus(propertySpecService, nlsService);
        CourbeChargePartielle courbeChargePartielle = new CourbeChargePartielle(new TrimaranObjectFactory(trimaranPlus));
        courbeChargePartielle.parse(data);

        CourbeCharge courbeCharge = new CourbeCharge(null);
        courbeCharge.setProfileData(new ProfileData());
        courbeCharge.now = new Date();
        courbeCharge.setElements(new ArrayList());
        courbeCharge.addValues(courbeChargePartielle.getValues());
        courbeCharge.doParse(false);

        assertEquals(courbeCharge.getProfileData().getIntervalData(286).getEndTime().getTime(), 1319932200000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(287).getEndTime().getTime(), 1319932800000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(288).getEndTime().getTime(), 1319933400000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(289).getEndTime().getTime(), 1319934000000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(290).getEndTime().getTime(), 1319934600000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(291).getEndTime().getTime(), 1319935200000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(292).getEndTime().getTime(), 1319935800000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(293).getEndTime().getTime(), 1319936400000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(294).getEndTime().getTime(), 1319937000000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(295).getEndTime().getTime(), 1319937600000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(296).getEndTime().getTime(), 1319938200000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(297).getEndTime().getTime(), 1319938800000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(298).getEndTime().getTime(), 1319939400000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(299).getEndTime().getTime(), 1319940000000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(300).getEndTime().getTime(), 1319940600000L);
        assertEquals(courbeCharge.getProfileData().getIntervalData(301).getEndTime().getTime(), 1319941200000L);

        assertEquals(courbeCharge.getProfileData().getIntervalData(286).get(0).intValue(), 15237);
        assertEquals(courbeCharge.getProfileData().getIntervalData(287).get(0).intValue(), 15210);
        assertEquals(courbeCharge.getProfileData().getIntervalData(288).get(0).intValue(), 15129);
        assertEquals(courbeCharge.getProfileData().getIntervalData(289).get(0).intValue(), 15093);
        assertEquals(courbeCharge.getProfileData().getIntervalData(290).get(0).intValue(), 15093);
        assertEquals(courbeCharge.getProfileData().getIntervalData(291).get(0).intValue(), 15156);
        assertEquals(courbeCharge.getProfileData().getIntervalData(292).get(0).intValue(), 15165);
        assertEquals(courbeCharge.getProfileData().getIntervalData(293).get(0).intValue(), 15120);
        assertEquals(courbeCharge.getProfileData().getIntervalData(294).get(0).intValue(), 15084);
        assertEquals(courbeCharge.getProfileData().getIntervalData(295).get(0).intValue(), 15075);
        assertEquals(courbeCharge.getProfileData().getIntervalData(296).get(0).intValue(), 15120);
        assertEquals(courbeCharge.getProfileData().getIntervalData(297).get(0).intValue(), 15102);
        assertEquals(courbeCharge.getProfileData().getIntervalData(298).get(0).intValue(), 14967);
        assertEquals(courbeCharge.getProfileData().getIntervalData(299).get(0).intValue(), 15012);
        assertEquals(courbeCharge.getProfileData().getIntervalData(300).get(0).intValue(), 15012);
        assertEquals(courbeCharge.getProfileData().getIntervalData(301).get(0).intValue(), 14958);
    }

    @Test
    public void constructDecenniumTable() {
        CourbeCharge cc = new CourbeCharge(null);

        long currentTimeInMillis = Long.valueOf("915611294000");    // somewhere in 1999
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999}, cc.getDecenniumYearTable());

        currentTimeInMillis = Long.valueOf("1262766495000");    // somewhere in 2010
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{2010, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009}, cc.getDecenniumYearTable());

        currentTimeInMillis = Long.valueOf("1231230071000");    // somewhere in 2009
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009}, cc.getDecenniumYearTable());

        currentTimeInMillis = Long.valueOf("1010305694000");    // somewhere in 2002
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{2000, 2001, 2002, 1993, 1994, 1995, 1996, 1997, 1998, 1999}, cc.getDecenniumYearTable());

        currentTimeInMillis = Long.valueOf("1578299294000");    // somewhere in 2020
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{2020, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019}, cc.getDecenniumYearTable());

        currentTimeInMillis = Long.valueOf("1736152094000");    // somewhere in 2025
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{2020, 2021, 2022, 2023, 2024, 2025, 2016, 2017, 2018, 2019}, cc.getDecenniumYearTable());

        currentTimeInMillis = Long.valueOf("4891825694000");    // somewhere in 2125
        cc.setCurrentTime(currentTimeInMillis);
        cc.constructDecenniumTable();
        assertArrayEquals(new int[]{2120, 2121, 2122, 2123, 2124, 2125, 2116, 2117, 2118, 2119}, cc.getDecenniumYearTable());
    }


}
