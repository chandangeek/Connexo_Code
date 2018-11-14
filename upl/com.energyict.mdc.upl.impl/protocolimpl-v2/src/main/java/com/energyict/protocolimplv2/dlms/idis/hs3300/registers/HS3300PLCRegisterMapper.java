package com.energyict.protocolimplv2.dlms.idis.hs3300.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HS3300PLCRegisterMapper extends G3RegisterMapper {

    private static final ObisCode PLC_G3_TIMEOUT = ObisCode.fromString("0.0.94.33.10.255");
    private static final ObisCode PLC_G3_KEEP_ALIVE = ObisCode.fromString("0.0.94.33.11.255");
    private static final ObisCode LOAD_PROFILE_CONTROL_STATUS = ObisCode.fromString("0.0.96.5.3.255");
    private static final ObisCode LOAD_PROFILE_DISPLAY_CONTROL_STATUS = ObisCode.fromString("0.0.96.5.4.255");

    private final DlmsSession dlmsSession;

    public HS3300PLCRegisterMapper(DlmsSession dlmsSession) {
        super(dlmsSession.getCosemObjectFactory(), dlmsSession.getTimeZone(), dlmsSession.getLogger());
        this.dlmsSession = dlmsSession;
    }

    @Override
    protected void initializeMappings() {
        this.getMappings().addAll(getPLCStatisticsMappings());
        this.getMappings().addAll(getAdditionalPLCMappings());
        this.getMappings().addAll(getDisconnectControlRegistering());
        this.getMappings().addAll(getImageTransferMappings());
        this.getMappings().addAll(getLimiterMappings());
        this.getMappings().addAll(getBillingSchedulerMappings());
        this.getMappings().addAll(getDisconnectControlSchedulerMappings());
        this.getMappings().addAll(getImageTransferActivationSchedulerMappings());
    }

    private final List<G3Mapping> getAdditionalPLCMappings() {
        final List<G3Mapping> mappings = new ArrayList<>();
        mappings.add(new DataValueMapping(PLC_G3_TIMEOUT, Unit.get(BaseUnit.MINUTE)));
        mappings.add(new PLCG3KeepAliveDataValueMapping(PLC_G3_KEEP_ALIVE));
        mappings.add(new LoadProfileControlStatusMapping(LOAD_PROFILE_CONTROL_STATUS));
        mappings.add(new LoadProfileDisplayControlStatusMapping(LOAD_PROFILE_DISPLAY_CONTROL_STATUS));
        return mappings;
    }

    /**
     * Read out and return the G3 PLC register, or null if it's not supported in this mapper
     */
    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {   //TODO: implement bulk request (after ticket COMMUNICATION-1116 is finished)
        List<G3Mapping> g3Mappings = getMappings();
        for (G3Mapping mapping : g3Mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                return mapping.readRegister(dlmsSession.getCosemObjectFactory());
            }
        }
        return null;
    }
}