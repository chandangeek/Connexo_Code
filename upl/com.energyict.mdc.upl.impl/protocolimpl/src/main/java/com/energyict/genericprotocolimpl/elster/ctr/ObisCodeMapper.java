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
/*
        registerMapping.add(new CTRRegisterMapping("1.0.0.0.0.255", "C.0.0"));
*/
    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws CTRException, NoSuchRegisterException {
        throw new NoSuchRegisterException(obisCode.toString());
    }

}
