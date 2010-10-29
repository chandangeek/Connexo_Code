package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.mdw.amr.RtuRegisterMappingFactory;
import com.energyict.mdw.amrimpl.RtuRegisterMappingImpl;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuTypeShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterMappingShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterSpecShadow;
import com.energyict.obis.ObisCode;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-okt-2010
 * Time: 14:45:25
 */
public class CtrMasterData {

    public static void main(String[] args) {
        createDeviceRegisterMappings();
    }

    private static void createDeviceRegisterMappings() {

        RegSpec[] specs = new RegSpec[]{
                new RegSpec("7.0.13.26.0.0", "2.1.6", "DECF", "9", "Tot_Vb_pf (end of previous billing period)"),
                new RegSpec("7.0.13.0.0.255", "2.0.0", "DEC", "9", "Tot_Vm"),
                new RegSpec("7.0.13.2.0.255", "2.1.0", "DEC", "9", "Tot_Vb"),
                new RegSpec("7.0.128.1.0.255", "2.3.0", "DEC", "9", "Tot_Vme "),

                new RegSpec("7.0.43.0.0.255", "1.0.0", "DEC", "5", "Qm"),
                new RegSpec("7.0.43.1.0.255", "1.2.0", "DEC", "6", "Qb"),
                new RegSpec("7.0.42.0.0.255", "4.0.0", "DEC", "-", "P Pressure"),
                new RegSpec("7.0.41.0.0.255", "7.0.0", "DEC", "-", "T Temperature"),
                new RegSpec("7.0.52.0.0.255", "A.0.0", "DEC", "-", "C conversion factor"),
                new RegSpec("7.0.53.0.0.255", "A.1.6", "DEC", "-", "Z compressibility"),

                new RegSpec("7.0.128.2.1.255", "2.3.7", "DECF", "9", "Tot_Vme_f1"),
                new RegSpec("7.0.128.2.2.255", "2.3.8", "DECF", "9", "Tot_Vme_f2"),
                new RegSpec("7.0.128.2.3.255", "2.3.9", "DECF", "9", "Tot_Vme_f3"),

                new RegSpec("7.0.128.4.0.255", "C.0.0", "DEC/DECF", "-", "PDR"),
                new RegSpec("7.0.128.5.0.0", "2.3.6", "DECF", "9", "Tot_Vme_pf (end of previous billing period)"),
                new RegSpec("7.0.128.6.1.0", "2.3.A", "DECF", "9", "Tot_Vme_pf_f1"),
                new RegSpec("7.0.128.6.2.0", "2.3.B", "DECF", "9", "Tot_Vme_pf_f2"),
                new RegSpec("7.0.128.6.3.0", "2.3.C", "DECF", "9", "Tot_Vme_pf_f3"),
                new RegSpec("7.0.128.7.0.255", "2.3.0", "DECF", "9", "Tot_Vme"),
                new RegSpec("7.0.128.8.0.255", "10.1.0", "DEC", "-", "Number of elements in event array"),

                new RegSpec("7.0.13.2.1.255", "2.5.0", "DECF", "9", "Tot_Vcor_f1"),
                new RegSpec("7.0.13.2.2.255", "2.5.1", "DECF", "9", "Tot_Vcor_f2"),
                new RegSpec("7.0.13.2.3.255", "2.5.2", "DECF", "9", "Tot_Vcor_f3"),
                new RegSpec("7.0.13.2.1.0", "2.5.3", "DECF", "9", "Tot_Vpre_f1"),
                new RegSpec("7.0.13.2.2.0", "2.5.4", "DECF", "9", "Tot_Vpre_f2"),
                new RegSpec("7.0.13.2.3.0", "2.5.5", "DECF", "9", "Tot_Vpre_f3"),

                new RegSpec("0.0.96.10.1.255", "12.0.0", "DEC", "-", "Device status : status register 1"),
                new RegSpec("0.0.96.10.2.255", "D.9.0", "DEC", "-", "Seal status : status register 2"),
                new RegSpec("0.0.96.10.3.255", "12.1.0", "REG", "-", "Diagn : status register 3"),
                new RegSpec("0.0.96.10.4.255", "12.2.0", "DEC", "-", "DiagnR : status register 4 "),

                new RegSpec("0.0.96.12.5.255", "E.C.0", "DEC", "-", "Gsm signal strength (dB)"),
                new RegSpec("7.0.0.9.4.255", "8.1.2", "DEC", "-", "Remaining shift in time"),
                new RegSpec("0.0.96.6.6.255", "F.5.0", "REG", "-", "Battery time remaining (hours)"),
                new RegSpec("0.0.96.6.0.255", "F.5.1", "REG", "-", "Battery hours used"),
                new RegSpec("0.0.96.6.3.255", "F.5.2", "REG", "-", "Battery voltage"),
        };


        RtuRegisterMappingFactory factory = CommonUtils.mw().getRtuRegisterMappingFactory();

        for (RegSpec spec : specs) {
            try {
                factory.create(spec.getRtuRegisterMappingShadow());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }


        RtuType rtuType = CommonUtils.mw().getRtuTypeFactory().find("MTU155");
        RtuTypeShadow rtuTypeShadow = rtuType.getShadow();
        List<RtuRegisterMappingImpl> mappings = factory.findAll();
        for (RtuRegisterMappingImpl mapping : mappings) {
            RtuRegisterSpecShadow specShadow = new RtuRegisterSpecShadow();
            specShadow.setDeviceChannelIndex(0);
            specShadow.setRegisterMappingId(mapping.getId());
            specShadow.setNumberOfDigits(9);
            specShadow.setNumberOfDigits(3);
            specShadow.setIntegral(false);
            rtuTypeShadow.getRegisterSpecShadows().add(specShadow);
        }
        try {
            rtuType.update(rtuTypeShadow);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static class RegSpec {

        private final String obis, id, source, digits, description;

        public RegSpec(String obis, String id, String source, String digits, String description) {
            this.obis = obis;
            this.id = id;
            this.source = source;
            this.digits = digits;
            this.description = description;
        }

        public RtuRegisterMappingShadow getRtuRegisterMappingShadow() {
            RtuRegisterMappingShadow shadow = new RtuRegisterMappingShadow();
            shadow.setObisCode(ObisCode.fromString(obis));
            shadow.setName("MTU155 - " + description + " [" + id + "]");
            shadow.setDescription("MTU155 - " + description + " [" + id + "]");
            shadow.setProductSpecId(0);
            return shadow;
        }

    }
}
