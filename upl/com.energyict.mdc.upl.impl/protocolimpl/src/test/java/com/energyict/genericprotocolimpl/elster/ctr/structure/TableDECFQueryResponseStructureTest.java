package com.energyict.genericprotocolimpl.elster.ctr.structure;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.energyict.genericprotocolimpl.elster.ctr.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 9:10:27
 */
public class TableDECFQueryResponseStructureTest {

    byte[] response = ProtocolTools.getBytesFromHexString(
            "0A000000213400123456789000000A0A1208040000001A00000000000000000C0000000000000000000000000000000000000000000000000C000000000000000000000005010100000000FFFFFFFFFDFFFFFFFFFDFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF0000000000000000000000000000000000000000DEAA3BEA18EB0D", ""
    );
    
    @Test
    public void testRequestResponse() throws Exception {
//        GprsRequestFactory factory = getDummyRequestFactory(response);
//        TableDECFQueryResponseStructure structure = factory.queryTableDECF();
//        assertNotNull(structure);
    }
    
}
