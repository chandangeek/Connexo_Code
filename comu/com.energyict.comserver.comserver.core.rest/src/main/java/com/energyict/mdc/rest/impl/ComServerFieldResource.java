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
import java.util.ArrayList;
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
        return asJsonArrayObject("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }

    @GET
    @Path("/timeUnit")
    public Object getTimeUnits() {
        final List<Object> timeUnitStrings = new ArrayList<>();
        int[] timeDurations = new int[] {
                TimeDuration.MILLISECONDS,
                TimeDuration.SECONDS,
                TimeDuration.MINUTES,
                TimeDuration.HOURS,
                TimeDuration.DAYS,
                TimeDuration.WEEKS,
                TimeDuration.MONTHS,
                TimeDuration.YEARS
        };

        for (final int timeDuration : timeDurations) {
            timeUnitStrings.add(TimeDuration.getTimeUnitDescription(timeDuration));
        }

        return asJsonArrayObject("timeUnits", "timeUnit", timeUnitStrings);
    }

    @GET
    @Path("/comPortType")
    public Object getComPortTypes() {
        return asJsonArrayObject("comPortTypes", "comPortType", new ComPortTypeAdapter().getClientSideValues());
    }

    @GET
    @Path("/parity")
    public Object getParities() {
        return asJsonArrayObject("parities", "parity", new ParitiesAdapter().getClientSideValues());
    }

    @GET
    @Path("/flowControl")
    public Object getFlowControls() {
        return asJsonArrayObject("flowControls", "flowControl", new FlowControlAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfDataBits")
    public Object getNrOfDataBits() {
        return asJsonArrayObject("nrOfDataBits", "nrOfDataBits", new NrOfDataBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfStopBits")
    public Object getNrOfStopBits() {
        return asJsonArrayObject("nrOfStopBits", "nrOfStopBits", new NrOfStopBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/baudRate")
    public Object getBaudRate() {
        return asJsonArrayObject("baudRates", "baudRate", new BaudrateAdapter().getClientSideValues());
    }

}
