package com.energyict.protocolimpl.iec1107.as220;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;

/**
 * @author jme
 * @since 17-aug-2009
 */
public class AS220Test {

    @Mock
    private NlsService nlsService;
    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void testMethods() {
        AS220 as220 = new AS220(propertySpecService, nlsService);
        assertNotNull(as220.getProtocolVersion());
    }

}
