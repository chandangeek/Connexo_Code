package com.elster.utils.lis200.events;

/**
 *
 * User: heuckeg
 * Date: 14.07.2010
 * Time: 13:49:00
 */
public class EventInterpreterFactory {

    public static EventInterpreter getInterpreterFor(String deviceType) {
        if (deviceType.equalsIgnoreCase("DL210"))
            return new Dl210EventInterpreter();
        if (deviceType.equalsIgnoreCase("DL220"))
            return new Dl220EventInterpreter();
        if (deviceType.equalsIgnoreCase("DL240"))
            return new Dl240EventInterpreter();
        if (deviceType.equalsIgnoreCase("EK220"))
            return new Ek220EventInterpreter();
        if (deviceType.equalsIgnoreCase("EK230"))
            return new Ek230EventInterpreter();
        if (deviceType.equalsIgnoreCase("Ek260"))
            return new Ek260EventInterpreter();
        return new EventInterpreter();
    }
}
