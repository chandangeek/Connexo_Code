package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPort;

import org.json.JSONException;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Provides an implementation for the {@link LoggingEvent} interface
 * for events that relate to operational aspects of a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:07)
 */
public class ComPortOperationsLoggingEvent extends AbstractComServerEventImpl implements LoggingEvent, ComPortRelatedEvent, ComPortPoolRelatedEvent {

    private ComPort comPort;
    private LogLevel logLevel;
    private String logMessage;

    /**
     * For the externalization process only.
     */
    public ComPortOperationsLoggingEvent() {
        super();
    }

    public ComPortOperationsLoggingEvent(ComPort comPort, LogLevel logLevel, String logMessage) {
        super();
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.comPort = comPort;
    }

    @Override
    public Category getCategory () {
        return Category.LOGGING;
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
    public boolean isComPortPoolRelated () {
        return this.isComPortRelated() && this.getComPort().isInbound();
    }

    @Override
    public ComPortPool getComPortPool () {
        InboundComPort inboundComPort = (InboundComPort) this.getComPort();
        return inboundComPort.getComPortPool();
    }

    @Override
    public boolean isComPortRelated () {
        return this.comPort != null;
    }

    @Override
    public ComPort getComPort () {
        return this.comPort;
    }

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        LoggingEventExternalizationAssistant.writeExternal(this, out);
        out.writeLong(this.extractId(this.getComPort()));
    }

    private long extractId(HasId hasId) {
        if (hasId == null) {
            return 0;
        }
        else {
            return hasId.getId();
        }
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        LoggingEventExternalizationAssistant.LoggingEventPojo pojo = LoggingEventExternalizationAssistant.readExternal(in);
        this.logLevel = pojo.getLogLevel();
        this.logMessage = pojo.getLogMessage();
        this.comPort = this.findComPort(in.readLong());
    }

    private ComPort findComPort (long comPortId) {
        if (comPortId != 0) {
            return this.getEngineModelService().findComPort(comPortId);
        }
        else {
            return null;
        }
    }

    @Override
    protected void toString (JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
            key("com-port").
            value(this.extractId(this.getComPort())).
            key("log-level").
            value(String.valueOf(this.getLogLevel())).
            key("message").
            value(this.getLogMessage());
    }

}
