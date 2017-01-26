package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.events.ComServerEvent;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Provides code reuse opportunities for components
 * that will represent {@link ComServerEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (11:01)
 */
public abstract class AbstractComServerEventImpl implements ComServerEvent {

    private static final DateTimeFormatter OCCURRENCE_TIMESTAMP_FORMAT;

    static {
        OCCURRENCE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS (Z)");
    }

    private final ServiceProvider serviceProvider;
    private Instant occurrenceTimestamp;

    protected AbstractComServerEventImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.occurrenceTimestamp = serviceProvider.clock().instant();
    }

    private ServiceProvider getServiceProvider() {
        return this.serviceProvider;
    }

    protected Clock getClock() {
        return this.getServiceProvider().clock();
    }

    protected DeviceMessageService getDeviceMessageService() {
        return this.getServiceProvider().deviceMessageService();
    }

    @Override
    public Instant getOccurrenceTimestamp() {
        return this.occurrenceTimestamp;
    }

    protected String getOccurrenceTimestampUTCString() {
        return this.formatOccurrenceTimeStamp(this.getOccurrenceTimestamp());
    }

    protected String formatOccurrenceTimeStamp(Instant occurrenceTimestamp) {
        if (occurrenceTimestamp != null) {
            return OCCURRENCE_TIMESTAMP_FORMAT.format(occurrenceTimestamp.atOffset(ZoneOffset.UTC));
        } else {
            return "null";
        }
    }

    @Override
    public boolean isDeviceRelated() {
        return false;
    }

    @Override
    public boolean isConnectionTaskRelated() {
        return false;
    }

    @Override
    public boolean isComPortRelated() {
        return false;
    }

    @Override
    public boolean isComPortPoolRelated() {
        return false;
    }

    @Override
    public boolean isComTaskExecutionRelated() {
        return false;
    }

    @Override
    public boolean isLoggingRelated() {
        return false;
    }

    @Override
    public String toString() {
        try {
            JSONWriter jsonWriter = new JSONStringer().object();
            this.toString(jsonWriter);
            jsonWriter.endObject();
            return jsonWriter.toString();
        } catch (JSONException e) {
            e.printStackTrace(System.err);
            return super.toString();
        }
    }

    protected void toString(JSONWriter writer) throws JSONException {
        writer.key("class").value(this.getClass().getSimpleName());
        writer.key("timestamp").value(this.getOccurrenceTimestampUTCString());
    }

    public interface ServiceProvider {

        Clock clock();

        DeviceMessageService deviceMessageService();

    }
}