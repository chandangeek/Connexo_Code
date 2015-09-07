package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.rest.impl.comserver.BaudrateAdapter;
import com.energyict.mdc.rest.impl.comserver.ComPortTypeAdapter;
import com.energyict.mdc.rest.impl.comserver.FlowControlAdapter;
import com.energyict.mdc.rest.impl.comserver.LogLevelAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfDataBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfStopBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.ParitiesAdapter;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getLogLevelValues() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }

    @GET
    @Path("/timeUnit")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getTimeUnits() {
        final List<Object> timeUnitStrings = new ArrayList<>();
        int[] timeDurations = new int[] {
                TimeDuration.TimeUnit.MILLISECONDS.getCode(),
                TimeDuration.TimeUnit.SECONDS.getCode(),
                TimeDuration.TimeUnit.MINUTES.getCode(),
                TimeDuration.TimeUnit.HOURS.getCode(),
                TimeDuration.TimeUnit.DAYS.getCode(),
                TimeDuration.TimeUnit.WEEKS.getCode(),
                TimeDuration.TimeUnit.MONTHS.getCode(),
                TimeDuration.TimeUnit.YEARS.getCode()
        };

        for (final int timeDuration : timeDurations) {
            timeUnitStrings.add(TimeDuration.getTimeUnitDescription(timeDuration));
        }

        return asJsonArrayObjectWithTranslation("timeUnits", "timeUnit", timeUnitStrings);
    }

    @GET
    @Path("/comPortType")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getComPortTypes() {
        return asJsonArrayObjectWithTranslation("comPortTypes", "comPortType", new ComPortTypeAdapter().getClientSideValues());
    }

    @GET
    @Path("/parity")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getParities() {
        return asJsonArrayObjectWithTranslation("parities", "parity", new ParitiesAdapter().getClientSideValues());
    }

    @GET
    @Path("/flowControl")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getFlowControls() {
        return asJsonArrayObjectWithTranslation("flowControls", "flowControl", new FlowControlAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfDataBits")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getNrOfDataBits() {
        return asJsonArrayObjectWithTranslation("nrOfDataBits", "nrOfDataBits", new NrOfDataBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/nrOfStopBits")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getNrOfStopBits() {
        return asJsonArrayObjectWithTranslation("nrOfStopBits", "nrOfStopBits", new NrOfStopBitsAdapter().getClientSideValues());
    }

    @GET
    @Path("/baudRate")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getBaudRate() {
        return asJsonArrayObjectWithTranslation("baudRates", "baudRate", new BaudrateAdapter().getClientSideValues());
    }

}
