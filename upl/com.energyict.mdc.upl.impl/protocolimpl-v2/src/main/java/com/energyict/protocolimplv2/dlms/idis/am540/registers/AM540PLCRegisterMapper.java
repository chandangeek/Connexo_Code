package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.DataValueMapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;
import com.energyict.protocolimpl.dlms.g3.registers.PLCG3KeepAliveDataValueMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for the G3 PLC registers, reusing the functionality of the G3 protocol
 */
public class AM540PLCRegisterMapper extends G3RegisterMapper {

    private static final ObisCode PLC_G3_TIMEOUT = ObisCode.fromString("0.0.94.33.10.255");
    private static final ObisCode PLC_G3_KEEP_ALIVE = ObisCode.fromString("0.0.94.33.11.255");


    private final DlmsSession dlmsSession;

    public AM540PLCRegisterMapper(DlmsSession dlmsSession) {
        super(dlmsSession.getCosemObjectFactory(), dlmsSession.getTimeZone(), dlmsSession.getLogger());
        this.dlmsSession = dlmsSession;
    }

    @Override
    protected void initializeMappings() {
        this.getMappings().addAll(getPLCStatisticsMappings());
        this.getMappings().addAll(getAdditionalPLCMappings());
    }

    private final List<G3Mapping> getAdditionalPLCMappings() {
        final List<G3Mapping> mappings = new ArrayList<>();
        mappings.add(new DataValueMapping(PLC_G3_TIMEOUT, Unit.get(BaseUnit.MINUTE)));
        mappings.add(new PLCG3KeepAliveDataValueMapping(PLC_G3_KEEP_ALIVE));
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