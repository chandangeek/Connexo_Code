package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.CtrTest;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 9:10:27
 */
public class TableDECFQueryResponseStructureTest extends CtrTest {

    byte[] response = ProtocolTools.getBytesFromHexString(
            "0A000000213400123456789000000A0A1208040000001A00000000000000000C0000000000000000000000000000000000000000000000000C000000000000000000000005010100000000FFFFFFFFFDFFFFFFFFFDFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF0000000000000000000000000000000000000000DEAA3BEA18EB0D", ""
    );
    
    @Test
    public void testRequestResponse() throws Exception {
        GprsRequestFactory factory = getDummyRequestFactory(response);
        TableDECFQueryResponseStructure structure = factory.queryTableDECF();
        assertNotNull(structure);
    }
    
}
