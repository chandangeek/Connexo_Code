package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.engine.model.security.Privileges;
import com.energyict.mdc.rest.impl.comserver.BaudrateAdapter;
import com.energyict.mdc.rest.impl.comserver.ComPortTypeAdapter;
import com.energyict.mdc.rest.impl.comserver.FlowControlAdapter;
import com.energyict.mdc.rest.impl.comserver.LogLevelAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfDataBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfStopBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.ParitiesAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
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
    public ComServerFieldResource(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @GET
    @Path("/logLevel")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getLogLevelValues() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }

    @GET
    @Path("/timeUnit")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
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

        return asJsonArrayObjectWithTranslation("timeUnits", "timeUnit", timeUnitStrings);
    }

    @GET
    @Path("/comPortType")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getComPortTypes() {
        return asJsonArrayObjectWithTranslation("comPortTypes", "comPortType", new ComPortTypeAdapter().getClientSideValues());
    }

    @GET
    @Path("/parity")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getParities() {
        return asJsonArrayObjectWithTranslation("parities", "parity", new ParitiesAdapter().getClientSideValues());
    }

    @GET
    @Path("/flowControl")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getFlowControls() {
        return asJsonArrayObjectWithTranslation("flowControls", "flowControl", new FlowControlAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfDataBits")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getNrOfDataBits() {
        return asJsonArrayObjectWithTranslation("nrOfDataBits", "nrOfDataBits", new NrOfDataBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfStopBits")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getNrOfStopBits() {
        return asJsonArrayObjectWithTranslation("nrOfStopBits", "nrOfStopBits", new NrOfStopBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/baudRate")
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public Object getBaudRate() {
        return asJsonArrayObjectWithTranslation("baudRates", "baudRate", new BaudrateAdapter().getClientSideValues());
    }

}
