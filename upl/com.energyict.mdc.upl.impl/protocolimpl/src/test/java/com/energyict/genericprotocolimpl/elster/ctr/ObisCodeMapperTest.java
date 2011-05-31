package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.obis.ObisCode;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 16-nov-2010
 * Time: 9:27:51
 */
public class ObisCodeMapperTest extends TestCase {

    private static final String[] OBIS_CODES = new String[]{
            "7.0.13.26.0.0",
            "7.0.13.0.0.255",
            "7.0.13.2.0.255",
            "7.0.128.1.0.255",
            "7.0.43.0.0.255",
            "7.0.43.1.0.255",
            "7.0.42.0.0.255",
            "7.0.41.0.0.255",
            "7.0.52.0.0.255",
            "7.0.53.0.0.255",
            "7.0.128.2.1.255",
            "7.0.128.2.2.255",
            "7.0.128.2.3.255",
            "7.0.128.4.0.255",
            "7.0.128.5.0.0",
            "7.0.128.6.1.0",
            "7.0.128.6.2.0",
            "7.0.128.6.3.0",
            "7.0.128.8.0.255",
            "7.0.13.2.1.255",
            "7.0.13.2.2.255",
            "7.0.13.2.3.255",
            "7.0.13.2.1.0",
            "7.0.13.2.2.0",
            "7.0.13.2.3.0",
            "0.0.96.10.1.255",
            "0.0.96.10.2.255",
            "0.0.96.10.3.255",
            "0.0.96.10.4.255",
            "0.0.96.12.5.255",
            "7.0.0.9.4.255",
            "0.0.96.6.6.255",
            "0.0.96.6.0.255",
            "0.0.96.6.3.255",
            "7.0.0.2.0.255",
            "7.0.0.2.1.255",
            "7.0.0.2.2.255",
            "7.0.0.2.3.255",
    };

    @Test
    public void testObisCodes() {
        ObisCodeMapper obisCodeMapper = new ObisCodeMapper(null, null);
        for (String obisCode : OBIS_CODES) {
            CTRRegisterMapping mapping = obisCodeMapper.searchRegisterMapping(ObisCode.fromString(obisCode));
            assertNotNull(mapping);
            assertNotNull(mapping.getObisCode());
            assertNotNull(mapping.getDescription());
        }
    }


}