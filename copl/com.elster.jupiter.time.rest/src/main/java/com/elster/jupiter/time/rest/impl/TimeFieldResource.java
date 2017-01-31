/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.security.Privileges;

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

@Path("/field")
public class TimeFieldResource {

    private final Thesaurus thesaurus;

    @Inject
    public TimeFieldResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/timeUnit")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_RELATIVE_PERIOD})
    public Object getTimeUnits() {
        int[] timeDurations = new int[]{
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
        for (final int timeDuration : timeDurations) {
            String timeUnitDescription = TimeDuration.getTimeUnitDescription(timeDuration);
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("timeUnit", timeUnitDescription);
            subMap.put("localizedValue", thesaurus.getString(timeUnitDescription.toString(), timeUnitDescription.toString()));
            subMap.put("code", timeDuration);
            list.add(subMap);
        }
        return response;
    }
}
