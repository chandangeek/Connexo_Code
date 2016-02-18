package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Carrier for collected data
 * @param <T> Type of collected data that is stored
 *
 * Copyrights EnergyICT
 * Date: 16/02/2016
 * Time: 9:15
 */
public abstract class AbstractCollectedDataProcessingEventImpl<T extends CollectedData> extends AbstractComServerEventImpl implements CollectedDataProcessingEvent {

    private T payload;
    private Issue issue;

    protected AbstractCollectedDataProcessingEventImpl(ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    protected AbstractCollectedDataProcessingEventImpl(ServiceProvider serviceProvider, T payload) {
        super(serviceProvider);
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public boolean hasIssue() {
        return (issue != null);
    }

    protected void toString(JSONWriter writer) throws JSONException {
        super.toString(writer);
        addEventInfo(writer);
        addIssue(writer);
    }

    protected void addEventInfo(JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.key("description").value(getDescription());
        if (this.getPayload() != null) {
            addPayload(writer);
        }
        addIssue(writer);
    }

    /**
     * To be overriden by classes for which the source needs to be marschalled and written by the given writer
     * @param writer used to marschal this event
     * @throws JSONException when marschalling fails
     */
    protected void addPayload(JSONWriter writer) throws JSONException{
        // By default nothing is written on the writer
    }

    private void addIssue(JSONWriter writer) throws JSONException {
        if (hasIssue()) {
            writer.key("issue");
            writer.object();

            writer.key("description").value(issue.getDescription());
            writer.key("isWarning").value(issue.isWarning());
            writer.key("isProblem").value(issue.isProblem());

            writer.endObject();
        }
    }

}
