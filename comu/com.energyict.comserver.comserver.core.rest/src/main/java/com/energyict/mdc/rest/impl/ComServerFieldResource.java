/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.Transactional;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class ComServerFieldResource extends FieldResource {

    private final Thesaurus thesaurus;

    @Inject
    public ComServerFieldResource(Thesaurus thesaurus) {
        super(thesaurus);
        this.thesaurus = thesaurus;
    }

    @GET @Transactional
    @Path("/logLevel")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getLogLevelValues() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/timeUnit")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getTimeUnits() {
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

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        response.put("timeUnits", list);
        for (final int timeDuration: timeDurations) {
            String timeUnitDescription = TimeDuration.getTimeUnitDescription(timeDuration);
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("timeUnit", timeUnitDescription);
            subMap.put("localizedValue", thesaurus.getString(timeUnitDescription.toString(), timeUnitDescription.toString()));
            subMap.put("code", timeDuration);
            list.add(subMap);
        }
        sortList(list);
        return response;
    }


    @GET @Transactional
    @Path("/comPortType")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getComPortTypes() {
        return asJsonArrayObjectWithTranslation("comPortTypes", "id", new ComPortTypeAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/parity")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getParities() {
        return asJsonArrayObjectWithTranslation("parities", "id", new ParitiesAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/flowControl")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getFlowControls() {
        return asJsonArrayObjectWithTranslation("flowControls", "id", new FlowControlAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/nrOfDataBits")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getNrOfDataBits() {
        return asJsonArrayObjectWithTranslation("nrOfDataBits", "nrOfDataBits", new NrOfDataBitsAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/nrOfStopBits")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getNrOfStopBits() {
        return asJsonArrayObjectWithTranslation("nrOfStopBits", "nrOfStopBits", new NrOfStopBitsAdapter().getClientSideValues());
    }

    @GET @Transactional
    @Path("/baudRate")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public Object getBaudRate() {
        return asJsonArrayObjectWithTranslation("baudRates", "baudRate", new BaudrateAdapter().getClientSideValues());
    }

}
