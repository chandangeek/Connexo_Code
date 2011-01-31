package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace_C;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 25-jan-2011
 * Time: 13:07:10
 */
public class DummyRequestFactory extends GprsRequestFactory {

    private final MTU155Properties properties;
    private boolean printRxTx = false;

    private static final Map<String, String> RESPONSE_MAP = new HashMap<String, String>();
    static {
        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$01$13$02$0B$01$18$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$02$01$13$0B$01$18$00$00$00$A6$3B$00$00$01$BA$00$00$01$75$00$00$01$41$00$00$01$4C$00$00$01$4A$00$00$00$0B$00$00$00$0B$00$00$01$89$00$00$01$68$00$00$01$65$00$00$01$58$00$00$01$6F$00$00$00$0D$00$00$00$0C$00$00$01$B6$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$40$08$52$9A$BC$30$0D"
        );
        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$01$13$02$0B$01$19$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$FF$FF$00$2C$02$01$13$0B$01$19$00$00$00$A6$3B$00$00$01$75$00$00$01$41$00$00$01$4C$00$00$01$4A$00$00$00$0B$00$00$00$0B$00$00$01$89$00$00$01$68$00$00$01$65$00$00$01$58$00$00$01$6F$00$00$00$0D$00$00$00$0C$00$00$01$B6$10$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$AB$88$7E$D1$18$D5$0D"
        );
        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$01$13$02$0B$01$09$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$FF$FF$00$2C$02$01$13$0B$01$09$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$AB$88$7E$D1$18$D5$0D"
        );
        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$01$13$02$0A$0C$1D$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$FF$FF$00$2C$02$01$13$0A$0C$1D$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$AB$88$7E$D1$18$D5$0D"
        );

        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$07$02$01$0B$01$18$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$01$07$02$0B$01$18$00$00$00$00$44$03$04$20$4A$03$04$26$12$03$04$31$FC$03$04$36$D4$03$04$3B$0C$03$04$42$28$03$04$49$1C$03$04$51$82$03$04$4C$FA$03$04$48$4A$03$04$44$58$03$04$3F$A8$03$04$3A$4E$03$04$35$08$03$04$31$0C$03$04$2D$E2$03$04$2B$80$03$04$29$DC$03$04$27$8E$03$04$25$04$03$04$22$F2$03$04$21$30$03$04$20$4A$03$04$1F$50$10$00$00$00$BB$5B$BF$7D$14$BA$0D"
        );
        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$07$02$01$0B$01$19$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$01$07$02$0B$01$19$00$00$00$00$44$03$04$20$4A$03$04$26$12$03$04$31$FC$03$04$36$D4$03$04$3B$0C$03$04$42$28$03$04$49$1C$03$04$51$82$03$04$4C$FA$03$04$48$4A$03$04$44$58$03$04$3F$A8$03$04$3A$4E$03$04$35$08$03$04$31$0C$03$04$2D$E2$03$04$2B$80$03$04$29$DC$03$04$27$8E$03$04$25$04$03$04$22$F2$03$04$21$30$03$04$20$4A$03$04$1F$50$10$00$00$00$BB$5B$BF$7D$14$BA$0D"
        );
        RESPONSE_MAP.put(
                "$0A$00$00$00$3F$53$01$30$30$30$30$30$31$07$02$01$0B$01$17$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$0D",
                "$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$01$07$02$0B$01$17$00$00$00$00$44$03$04$20$4A$03$04$26$12$03$04$31$FC$03$04$36$D4$03$04$3B$0C$03$04$42$28$03$04$49$1C$03$04$51$82$03$04$4C$FA$03$04$48$4A$03$04$44$58$03$04$3F$A8$03$04$3A$4E$03$04$35$08$03$04$31$0C$03$04$2D$E2$03$04$2B$80$03$04$29$DC$03$04$27$8E$03$04$25$04$03$04$22$F2$03$04$21$30$03$04$20$4A$03$04$1F$50$10$00$00$00$BB$5B$BF$7D$14$BA$0D"
        );

    }


    public DummyRequestFactory() {
        this(new MTU155Properties(), TimeZone.getDefault());
    }

    public DummyRequestFactory(MTU155Properties properties, TimeZone timeZone) {
        this(new MTU155Properties(), TimeZone.getDefault(), false);
    }

    public DummyRequestFactory(MTU155Properties properties, TimeZone timeZone, boolean printRxTx) {
        super(new DummyLink(), Logger.getLogger(DummyRequestFactory.class.getName()), properties, timeZone);
        this.properties = properties;
        this.printRxTx = printRxTx;
    }

    public void setPrintRxTx(boolean printRxTx) {
        this.printRxTx = printRxTx;
    }

    public boolean isPrintRxTx() {
        return printRxTx;
    }

    @Override
    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRException {
        GPRSFrame request = getTrace_CRequest(id, period, referenceDate);
        GPRSFrame response = getResponse(request);
        response.doParse();

        //Parse the records in the response into objects.
        Trace_CQueryResponseStructure trace_CResponse;
        if (response.getData() instanceof Trace_CQueryResponseStructure) {
            trace_CResponse = (Trace_CQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected Trace_CQueryResponseStructure but was " + response.getData().getClass().getSimpleName());
        }

        return trace_CResponse;
    }

    private GPRSFrame getResponse(GPRSFrame request) throws CTRException {
        String rawRequest = ProtocolTools.getHexStringFromBytes(request.getBytes());
        String rawResponse = RESPONSE_MAP.get(rawRequest);
        if (rawResponse == null) {
            throw new CTRException("Received unexpected frame during tests. " + request);
        } else {
            GPRSFrame response = new GPRSFrame();
            response.parse(ProtocolTools.getBytesFromHexString(rawResponse), 0);
            if (isPrintRxTx()) {
                System.out.println("TX = " + request);
                System.out.println("RX = " + response);
            }
            return response;
        }
    }

}
