package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Provides an implementation for the {@link LoggingEvent} interface
 * that does not relate to any other ComServer object.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (17:01)
 */
public class UnrelatedLoggingEvent extends AbstractComServerEventImpl implements LoggingEvent {

    private LogLevel logLevel;
    private String logMessage;

    /**
     * For the externalization process only.
     *
     * @param serviceProvider The ServiceProvider
     */
    public UnrelatedLoggingEvent (ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    public UnrelatedLoggingEvent (LogLevel logLevel, String logMessage, ServiceProvider serviceProvider) {
        super(serviceProvider);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
    }

    @Override
    public boolean isLoggingRelated () {
        return true;
    }

    @Override
    public LogLevel getLogLevel () {
        return logLevel;
    }

    @Override
    public String getLogMessage () {
        return logMessage;
    }

    @Override
    public Category getCategory () {
        return Category.LOGGING;
    }

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        LoggingEventExternalizationAssistant.writeExternal(this, out);
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        LoggingEventExternalizationAssistant.LoggingEventPojo pojo = LoggingEventExternalizationAssistant.readExternal(in);
        this.logLevel = pojo.getLogLevel();
        this.logMessage = pojo.getLogMessage();
    }

    @Override
    public void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("log-level").
            value(String.valueOf(this.getLogLevel())).
            key("message").
            value(this.getLogMessage());
    }

}