package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.impl.EngineModelServiceImpl;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.ComPortParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (13:45)
 */
public class ComPortParserTest {

    private static final String OUTBOUND_COMPORT_AS_QUERY_RESULT = "{\"query-id\":\"refreshComPort\",\"single-value\":{\"name\":\"TCP\",\"active\":true,\"numberOfSimultaneousConnections\":5,\"type\":\"OutboundComPortImpl\"}}";

    @Test
    public void testDelegateToEngineModelService () throws JSONException {
        EngineModelService engineModelService = mock(EngineModelService.class);

        // Business method
        JSONObject jsonObject = new JSONObject(OUTBOUND_COMPORT_AS_QUERY_RESULT);
        ComPort comPort = new ComPortParser(engineModelService).parse(jsonObject);

        // Asserts
        verify(engineModelService.parseComPortQueryResult(jsonObject));
    }

    @Test
    public void testOutbound () throws JSONException {
        // Business method
        JSONObject jsonObject = new JSONObject(OUTBOUND_COMPORT_AS_QUERY_RESULT);
        EngineModelServiceImpl engineModelService = new EngineModelServiceImpl(mock(OrmService.class), mock(NlsService.class), mock(ProtocolPluggableService.class));
        ComPort comPort = new ComPortParser(engineModelService).parse(jsonObject);

        // Asserts
        assertThat(comPort).isInstanceOf(OutboundComPort.class);
        OutboundComPort outboundComPort = (OutboundComPort) comPort;
        assertThat(outboundComPort.getName()).isEqualTo("TCP");
        assertThat(outboundComPort.isActive()).isTrue();
        assertThat(outboundComPort.getNumberOfSimultaneousConnections()).isEqualTo(5);
    }

}