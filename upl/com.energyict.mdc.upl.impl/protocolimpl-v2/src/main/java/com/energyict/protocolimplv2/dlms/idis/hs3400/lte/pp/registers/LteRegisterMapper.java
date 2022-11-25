package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.List;

public class LteRegisterMapper {

    protected final List<LteMapping> mappings = new ArrayList<>();

    private static final ObisCode GRPS_APN  = ObisCode.fromString("0.0.25.4.2.255");
    private static final ObisCode GRPS_LOGICALNAME  = ObisCode.fromString("0.0.25.4.1.255");
    private static final ObisCode GRPS_PINCODE  = ObisCode.fromString("0.0.25.4.3.255");
    private static final ObisCode GRPS_QUALITYOFSERVICE  = ObisCode.fromString("0.0.25.4.4.255");

    private static final ObisCode SETUP_PPP_AUTH  = ObisCode.fromString("0.0.25.3.5.255");
    private static final ObisCode SETUP_PLCOPTIONS  = ObisCode.fromString("0.0.25.3.3.255");
    private static final ObisCode SETUP_IPCPOPTIONS  = ObisCode.fromString("0.0.25.3.4.255");
    private static final ObisCode SETUP_PHYREFERENCE  = ObisCode.fromString("0.0.25.3.2.255");
    private static final ObisCode SETUP_LOGICALNAME  = ObisCode.fromString("0.0.25.3.1.255");
    private final CosemObjectFactory cosemObjectFactory;

    public LteRegisterMapper(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
        initializeMappings();
    }

    protected void initializeMappings() {
        this.mappings.addAll(getAPNMapping());
        this.mappings.addAll(getSETUPMapping());
    }

    public final List<LteMapping> getAPNMapping() {
        List<LteMapping> apnList = new ArrayList<>();
        apnList.add(new GPRSModemSetupMapping(GRPS_APN));
        apnList.add(new GPRSModemSetupMapping(GRPS_LOGICALNAME));
        apnList.add(new GPRSModemSetupMapping(GRPS_PINCODE));
        apnList.add(new GPRSModemSetupMapping(GRPS_QUALITYOFSERVICE));

        return apnList;
    }

    public final List<LteMapping> getSETUPMapping() {
        List<LteMapping> apnList = new ArrayList<>();
        apnList.add(new PPPSetupMapping(SETUP_PPP_AUTH));
        apnList.add(new PPPSetupMapping(SETUP_PLCOPTIONS));
        apnList.add(new PPPSetupMapping(SETUP_IPCPOPTIONS));
        apnList.add(new PPPSetupMapping(SETUP_PHYREFERENCE));
        apnList.add(new PPPSetupMapping(SETUP_LOGICALNAME));
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
