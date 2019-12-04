package com.energyict.mdc.engine.offline.gui.decorators;

import org.eclipse.jetty.util.ajax.JSON;

import java.util.HashMap;

/**
 * @author sva
 * @since 11/03/14 - 8:17
 */
public class EventDecoratorFactory {

    public static EventDecorator decorate(String jsonEventString) {
        // Filter the reserved keyword 'class' out of the JSON
        jsonEventString = jsonEventString.replaceAll("\"class\":", "\"clazz\":");
        HashMap<String, Object> event = (HashMap) JSON.parse(jsonEventString);

        if (event.containsKey("clazz")) {
            if (event.get("clazz").equals("ComPortOperationsLoggingEvent")) {
                return null;  // Do not log these events
            }

            if (((String) event.get("clazz")).contains("LoggingEvent")) {
                return new LoggingEventDecorator(event);
            }
            if (event.get("clazz").equals("ReadEvent") || event.get("clazz").equals("WriteEvent")) {
                return new ReadWriteEventDecorator(event);
            }
            return new DefaultEventDecorator(event);
        }
        return null;
    }
}
