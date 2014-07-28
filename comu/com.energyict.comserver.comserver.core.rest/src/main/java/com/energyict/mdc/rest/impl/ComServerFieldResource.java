package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.rest.impl.comserver.BaudrateAdapter;
import com.energyict.mdc.rest.impl.comserver.ComPortTypeAdapter;
import com.energyict.mdc.rest.impl.comserver.FlowControlAdapter;
import com.energyict.mdc.rest.impl.comserver.LogLevelAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfDataBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfStopBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.ParitiesAdapter;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class ComServerFieldResource extends FieldResource {

    @Inject
    public ComServerFieldResource(NlsService nlsService) {
        super(nlsService.getThesaurus(MdcApplication.COMPONENT_NAME, Layer.UI));
    }

    @GET
    @Path("/logLevel")
    public Object getLogLevelValues() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }

    @GET
    @Path("/timeUnit")
    public Object getTimeUnits() {
        List<Integer> timeDurations = Arrays.asList(
                TimeDuration.MILLISECONDS,
                TimeDuration.SECONDS,
                TimeDuration.MINUTES,
                TimeDuration.HOURS,
                TimeDuration.DAYS,
                TimeDuration.WEEKS,
                TimeDuration.MONTHS,
                TimeDuration.YEARS
        );

        List<String> timeDurationTranslationKeys = Arrays.asList(
            "milliseconds","seconds","minutes","hours","days","weeks","months","years"
        );

        return asJsonArrayObjectWithTranslation("timeUnits", "timeUnit", timeDurations, timeDurationTranslationKeys);
    }

    @GET
    @Path("/comPortType")
    public Object getComPortTypes() {
        return asJsonArrayObjectWithTranslation("comPortTypes", "comPortType", new ComPortTypeAdapter().getClientSideValues());
    }

    @GET
    @Path("/parity")
    public Object getParities() {
        return asJsonArrayObjectWithTranslation("parities", "parity", new ParitiesAdapter().getClientSideValues());
    }

    @GET
    @Path("/flowControl")
    public Object getFlowControls() {
        return asJsonArrayObjectWithTranslation("flowControls", "flowControl", new FlowControlAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfDataBits")
    public Object getNrOfDataBits() {
        return asJsonArrayObjectWithTranslation("nrOfDataBits", "nrOfDataBits", new NrOfDataBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfStopBits")
    public Object getNrOfStopBits() {
        return asJsonArrayObjectWithTranslation("nrOfStopBits", "nrOfStopBits", new NrOfStopBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/baudRate")
    public Object getBaudRate() {
        return asJsonArrayObjectWithTranslation("baudRates", "baudRate", new BaudrateAdapter().getClientSideValues());
    }

}
