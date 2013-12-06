package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.servers.ComServer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class FieldResource {

    /**
     * This method will return a JSON list of all available field descriptions in this resource
     */
    @GET
    public Object getAllFields() {
        final List<Object> allFields = new ArrayList<>();
        for (Method method : FieldResource.class.getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                Path annotation = method.getAnnotation(Path.class);
                final String path = annotation.value();
                if (path.length()>1) {
                    allFields.add(new Object() {
                        public String field = path.substring(1);
                    });
                }
            }
        }

        return new Object() {
            public List<Object> fields = allFields;
        };

    }

    @GET
    @Path("/logLevel")
    public Object getLogLevelValues() {
        return asJsonArrayObject("logLevels", "logLevel", ComServer.LogLevel.values());
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
            timeUnitStrings.add(new Object() {
                public String timeUnit = TimeDuration.getTimeUnitDescription(timeDuration);
            });
        }

        return new Object() {
            public List<Object> timeUnits = timeUnitStrings;
        };

    }

    @GET
    @Path("/comPortType")
    public Object getComPortTypes() {
        return asJsonArrayObject("comPortTypes", "comPortType", ComPortType.values());
    }

    @GET
    @Path("/parity")
    public Object getParities() {
        return asJsonArrayObject("parities", "parity", Parities.values());
    }

    @GET
    @Path("/flowControl")
    public Object getFlowControls() {
        return asJsonArrayObject("flowControls", "flowControl", FlowControl.values());
    }

    @GET
    @Path("/nrOfDataBits")
    public Object getNrOfDataBits() {
        return asJsonArrayObject("nrOfDataBits", NrOfDataBits.values());
    }

    @GET
    @Path("/nrOfStopBits")
    public Object getNrOfStopBits() {
        return asJsonArrayObject("noOfStopBits", NrOfStopBits.values());
    }

    @GET
    @Path("/baudRate")
    public Object getBaudRate() {
        return asJsonArrayObject("baudRates", "baudRate", BaudrateValue.values());
    }

    private <T extends Enum> HashMap<String, Object> asJsonArrayObject(String fieldName, T[] values) {
        return asJsonArrayObject(fieldName, fieldName, values);
    }
    private <T extends Enum> HashMap<String, Object> asJsonArrayObject(String fieldName, String valueName, T[] values) {
        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(fieldName, list);
        for (final T baudRateEnum : values) {
            HashMap<String, Object> subMap = new HashMap<>();
            subMap.put(valueName, baudRateEnum.name());
            list.add(subMap);
        }
        return map;
    }

}
