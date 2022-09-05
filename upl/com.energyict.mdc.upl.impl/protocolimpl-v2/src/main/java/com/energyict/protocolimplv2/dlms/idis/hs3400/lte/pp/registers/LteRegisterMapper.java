package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.List;

public class LteRegisterMapper {

    protected final List<LteMapping> mappings = new ArrayList<LteMapping>();
    private static final ObisCode GRPS_APN  = ObisCode.fromString("0.0.25.4.2.255");
    private final CosemObjectFactory cosemObjectFactory;

    public LteRegisterMapper(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
        initializeMappings();
    }

    protected void initializeMappings() {
        this.mappings.addAll(getAPNMapping());
    }

    public final List<LteMapping> getAPNMapping() {
        List<LteMapping> apnList = new ArrayList<>();
        apnList.add(new GPRSModemSetupMapping(GRPS_APN));
        return apnList;
    }

    public List<LteMapping> getMappings() {
        return mappings;
    }

    public LteMapping getLteMapping(ObisCode obisCode) {
        for (LteMapping lteMapping : getMappings()) {
            if (lteMapping.getObisCode().equals(obisCode)) {
                return lteMapping;
            }
        }
        return null;
    }
}
