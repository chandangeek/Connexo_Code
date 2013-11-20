package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.servers.ComServer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class FieldResource {

    @GET
    @Path("/")
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
        final List<Object> logLevelStrings = new ArrayList<>();
        for (ComServer.LogLevel logLevel : ComServer.LogLevel.values()) {
            final String myLogLevel = logLevel.name();
            logLevelStrings.add(new Object() {
                public String logLevel = myLogLevel;
            });
        }

        return new Object() {
            public List<Object> logLevels = logLevelStrings;
        };

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
        final List<Object> allComPortTypes = new ArrayList<>();
        final Object wrapper = new Object(){
            public ComPortType[] comPortTypes = ComPortType.values();
        };
        for (final ComPortType comPortTypeEnum : ComPortType.values()) {
            allComPortTypes.add(new Object(){
                public Enum comPortType = comPortTypeEnum;
            });
        }
        return wrapper;
    }

    @GET
    @Path("/parity")
    public Object getParities() {
        final List<Object> allParities = new ArrayList<>();
        final Object wrapper = new Object(){
            public List<Object> parities = allParities;
        };
        for (final Parities parityEnum : Parities.values()) {
            allParities.add(new Object() {
                public Enum parity = parityEnum;
            });
        }
        return wrapper;
    }

    @GET
    @Path("/flowControl")
    public Object getFlowControls() {
        final List<Object> allFlowControls = new ArrayList<>();
        final Object wrapper = new Object(){
            public List<Object> flowControls = allFlowControls;
        };
        for (final FlowControl flowControlEnum : FlowControl.values()) {
            allFlowControls.add(new Object() {
                public Enum flowControl = flowControlEnum;
            });
        }

        return wrapper;
    }

    @GET
    @Path("/noOfDataBits")
    public Object getNrOfDataBits() {
        final List<Object> allDataBits = new ArrayList<>();
        final Object wrapper = new Object(){
            public List<Object> nrOfDataBits = allDataBits;
        };
        for (final NrOfDataBits nrOfDataBitsEnum : NrOfDataBits.values()) {
            allDataBits.add(new Object() {
                public Enum nrOfDataBits = nrOfDataBitsEnum;
            });
        }

        return wrapper;
    }

    @GET
    @Path("/noOfStopBits")
    public Object getNrOfStopBits() {
        final List<Object> allStopBits = new ArrayList<>();
        final Object wrapper = new Object(){
            public List<Object> nrOfStopBits = allStopBits;
        };
        for (final NrOfStopBits nrOfStopBitsEnum : NrOfStopBits.values()) {
            allStopBits.add(new Object() {
                public Enum nrOfStopBits = nrOfStopBitsEnum;
            });
        }

        return wrapper;
    }

}
