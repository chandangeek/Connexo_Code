package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2010
 * Time: 10:20:17
 */
public class ObisCodeMapper {

    private List<CTRRegisterMapping> registerMapping = new ArrayList<CTRRegisterMapping>();
    private final GprsRequestFactory requestFactory;

    public ObisCodeMapper(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        initRegisterMapping();
    }

    private void initRegisterMapping() {

        //Daily readings = register values
        registerMapping.add(new CTRRegisterMapping("7.0.13.29.0.255", "1.3.3"));    //Vb
        registerMapping.add(new CTRRegisterMapping("7.0.13.30.0.255", "1.1.3"));    //Vm
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.0.255", "2.0.3"));     //Tot_Vm
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.0.255", "2.1.3"));     //Tot_Vb
        registerMapping.add(new CTRRegisterMapping("7.0.43.25.0.255", "1.A.3"));    //Qcb_max

        registerMapping.add(new CTRRegisterMapping("7.0.128.0.0.255", "12.6.3"));   //DiagnRS       = Manufacturer specific code!!  TODO: add in release notes
        registerMapping.add(new CTRRegisterMapping("7.0.128.1.0.255", "12.2.0"));   //DiagnR        = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.2.0.255", "12.1.0"));   //Diagn         = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.3.0.255", "2.3.3"));    //Tot_Vme_g     = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.4.0.255", "2.3.7"));    //Tot_Vme_f1    = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.5.0.255", "2.3.8"));    //Tot_Vme_f2    = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.6.0.255", "2.3.9"));    //Tot_Vme_f3    = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.7.0.255", "18.6.3"));   //Tot_Vme_f1_g  = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.8.0.255", "18.7.3"));   //Tot_Vme_f2_g  = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.9.0.255", "18.8.3"));   //Tot_Vme_f3_g  = Manufacturer specific code!!


        registerMapping.add(new CTRRegisterMapping("7.0.13.0.1.255", "2.5.0"));     //Tot_Vcor_f1
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.2.255", "2.5.1"));     //Tot_Vcor_f2
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.3.255", "2.5.2"));     //Tot_Vcor_f3

    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws CTRException, NoSuchRegisterException {
        return new RegisterValue(obisCode);
    }

}
