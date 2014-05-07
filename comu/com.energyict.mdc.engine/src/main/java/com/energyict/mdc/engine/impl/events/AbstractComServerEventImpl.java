package com.energyict.mdc.engine.impl.events;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.model.EngineModelService;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides code reuse opportunities for components
 * that will represent {@link ComServerEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (11:01)
 */
public abstract class AbstractComServerEventImpl implements ComServerEvent {

    private static final SimpleDateFormat OCCURRENCE_TIMESTAMP_FORMAT;
    private final Clock clock;
    private final DeviceDataService deviceDataService;
    private final EngineModelService engineModelService;

    static {
        OCCURRENCE_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS (Z)");
        OCCURRENCE_TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Date occurrenceTimestamp;

    /**
     * For the externalization process only.
     * @param clock
     * @param deviceDataService
     * @param engineModelService
     */
    protected AbstractComServerEventImpl(Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService) {
        this(clock, deviceDataService, engineModelService, clock.now());
    }

    protected AbstractComServerEventImpl(Clock clock, DeviceDataService deviceDataService, EngineModelService engineModelService, Date occurrenceTimestamp) {
        super();
        this.clock = clock;
        this.deviceDataService = deviceDataService;
        this.engineModelService = engineModelService;
        this.occurrenceTimestamp = occurrenceTimestamp;
    }

    protected Clock getClock() {
        return clock;
    }

    protected DeviceDataService getDeviceDataService() {
        return deviceDataService;
    }

    protected EngineModelService getEngineModelService() {
        return engineModelService;
    }

    @Override
    public Date getOccurrenceTimestamp () {
        return this.occurrenceTimestamp;
    }

    protected String getOccurrenceTimestampUTCString () {
        return this.formatOccurrenceTimeStamp(this.getOccurrenceTimestamp());
    }

    protected String formatOccurrenceTimeStamp (Date occurrenceTimestamp) {
        if (occurrenceTimestamp != null) {
            return OCCURRENCE_TIMESTAMP_FORMAT.format(occurrenceTimestamp);
        }
        else {
            return "null";
        }
    }

    @Override
    public boolean isDeviceRelated () {
        return false;
    }

    @Override
    public boolean isConnectionTaskRelated () {
        return false;
    }

    @Override
    public boolean isComPortRelated () {
        return false;
    }

    @Override
    public boolean isComPortPoolRelated () {
        return false;
    }

    @Override
    public boolean isComTaskExecutionRelated () {
        return false;
    }

    @Override
    public boolean isLoggingRelated () {
        return false;
    }

    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeLong(this.occurrenceTimestamp.getTime());
    }

    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        this.occurrenceTimestamp = new Date(in.readLong());
    }

    @Override
    public String toString () {
        try {
            JSONWriter jsonWriter = new JSONStringer().object();
            this.toString(jsonWriter);
            jsonWriter.endObject();
            return jsonWriter.toString();
        }
        catch (JSONException e) {
            e.printStackTrace(System.err);
            return super.toString();
        }
    }

    protected void toString (JSONWriter writer) throws JSONException {
        writer.key("class").value(this.getClass().getSimpleName());
        writer.key("timestamp").value(this.getOccurrenceTimestampUTCString());
    }

}