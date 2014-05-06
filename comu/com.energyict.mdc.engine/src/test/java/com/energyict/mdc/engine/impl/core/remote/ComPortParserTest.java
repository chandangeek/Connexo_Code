package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.impl.core.remote.ComPortParser;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.ComPortParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (13:45)
 */
public class ComPortParserTest {

    private static final String OUTBOUND_COMPORT_AS_QUERY_RESULT = "{\"query-id\":\"refreshComPort\",\"single-value\":{\"name\":\"TCP\",\"active\":true,\"numberOfSimultaneousConnections\":5,\"type\":\"OutboundComPortImpl\"}}";

    @Test
    public void testOutbound () throws JSONException {
        // Business method
        ComPort comPort = new ComPortParser().parse(new JSONObject(OUTBOUND_COMPORT_AS_QUERY_RESULT));

        // Asserts
        Assertions.assertThat(comPort).isInstanceOf(OutboundComPort.class);
        OutboundComPort outboundComPort = (OutboundComPort) comPort;
        Assertions.assertThat(outboundComPort.getName()).isEqualTo("TCP");
        Assertions.assertThat(outboundComPort.isActive()).isTrue();
        Assertions.assertThat(outboundComPort.getNumberOfSimultaneousConnections()).isEqualTo(5);
    }

}