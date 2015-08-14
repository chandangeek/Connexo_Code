package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.io.IOException;
import java.util.List;

/**
 * Mapper for the G3 PLC registers, reusing the functionality of the G3 protocol
 */
public class AM540PLCRegisterMapper extends G3RegisterMapper {

    private final DlmsSession dlmsSession;

    public AM540PLCRegisterMapper(DlmsSession dlmsSession) {
        super(dlmsSession.getCosemObjectFactory(), dlmsSession.getTimeZone(), dlmsSession.getLogger());
        this.dlmsSession = dlmsSession;
    }

    @Override
    protected void initializeMappings() {
        this.getMappings().addAll(getPLCStatisticsMappings());
    }

    /**
     * Read out and return the G3 PLC register, or null if it's not supported in this mapper
     */
    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        List<G3Mapping> g3Mappings = getMappings();
        for (G3Mapping mapping : g3Mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                return mapping.readRegister(dlmsSession.getCosemObjectFactory());
            }
        }
        return null;
    }
}